package de.neofonie.common.cinderella.config.loader;

import de.neofonie.common.cinderella.config.CinderellaConfig;
import de.neofonie.common.cinderella.config.xml.CinderellaXmlConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CinderellaXmlConfigLoaderFactory implements FactoryBean<CinderellaXmlConfigLoader> {

    private static final Logger logger = LoggerFactory.getLogger(CinderellaXmlConfigLoaderFactory.class);
    private Validator validator;
    private Resource xmlConfigPath;

    public CinderellaXmlConfigLoader createInstance() {

        CinderellaXmlConfig cinderellaConfig = null;
        try {
            cinderellaConfig = load(xmlConfigPath.getInputStream());
        } catch (IOException e) {
            logger.error("", e);
        }

        try {
            File file = xmlConfigPath.getFile();

            return new ReloadableCinderellaXmlConfigLoader(file, cinderellaConfig, this);
        } catch (IOException e) {
            //Ignore - occurs if xmlConfigPath is not a file (for example a file in a jar)
            return new StaticCinderellaXmlConfigLoader(cinderellaConfig);
        }
    }

    @Override
    public CinderellaXmlConfigLoader getObject() throws Exception {
        return createInstance();
    }

    @Override
    public Class<?> getObjectType() {
        return CinderellaXmlConfigLoader.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    private static class StaticCinderellaXmlConfigLoader implements CinderellaXmlConfigLoader {
        private final CinderellaConfig cinderellaConfig;

        private StaticCinderellaXmlConfigLoader(CinderellaConfig cinderellaConfig) {
            this.cinderellaConfig = cinderellaConfig;
        }

        @Override
        public CinderellaConfig getCinderellaConfig() throws IOException {
            return cinderellaConfig;
        }
    }

    private static class ReloadableCinderellaXmlConfigLoader implements CinderellaXmlConfigLoader {

        private final AtomicReference<CinderellaConfig> cinderellaConfig;
        private final CinderellaXmlConfigLoaderFactory cinderellaXmlConfigLoaderFactory;

        private ReloadableCinderellaXmlConfigLoader(File file, CinderellaConfig cinderellaConfig,
                                                    CinderellaXmlConfigLoaderFactory cinderellaXmlConfigLoaderFactory) {
            this.cinderellaXmlConfigLoaderFactory = cinderellaXmlConfigLoaderFactory;
            this.cinderellaConfig = new AtomicReference<>(cinderellaConfig);
            createWatcher(file);
        }

        private void createWatcher(File file) {
            FileWatcher.createWatcher(file.getParentFile(), new Function<File, Void>() {
                @Override
                public Void apply(File changedFile) {
                    if (!changedFile.equals(file.getAbsoluteFile())) {
                        return null;
                    }
                    CinderellaXmlConfig load = cinderellaXmlConfigLoaderFactory.load(changedFile);
                    if (load == null) {
                        return null;
                    }
                    cinderellaConfig.set(load);
                    logger.info(String.format("Reload %s", file.getAbsolutePath()));
                    return null;
                }
            });
        }

        @Override
        public CinderellaConfig getCinderellaConfig() throws IOException {
            return cinderellaConfig.get();
        }
    }

    private CinderellaXmlConfig load(File file) {
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            try {
                return load(inputStream);
            } finally {
                if (inputStream != null) try {
                    inputStream.close();
                } catch (IOException e) {

                }
            }
        } catch (FileNotFoundException e) {
            logger.error("", e);
            return null;
        }
    }

    private CinderellaXmlConfig load(InputStream inputStream) {
        try {
            logger.debug("load");
            JAXBContext jc = JAXBContext.newInstance(CinderellaXmlConfig.CLASSES);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            unmarshaller.setEventHandler(new ValidationEventHandler() {
                @Override
                public boolean handleEvent(ValidationEvent event) {
                    return false;
                }
            });

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream schemaInputStream = CinderellaXmlConfigLoaderFactory.class.getClassLoader().getResourceAsStream("xsd/cinderella.xsd");
            Schema schema = schemaFactory.newSchema(new StreamSource(schemaInputStream));
            unmarshaller.setSchema(schema);

            CinderellaXmlConfig ddosXmlConfig = (CinderellaXmlConfig) unmarshaller.unmarshal(inputStream);

            logger.info("loaded " + ddosXmlConfig);
            Set<ConstraintViolation<CinderellaXmlConfig>> violations = validator.validate(ddosXmlConfig);
            if (violations.isEmpty()) {
                return ddosXmlConfig;
            }

            String message = violations
                    .stream()
                    .map(s -> s.toString())
                    .collect(Collectors.joining("\n"));
            logger.error(String.format("Error loading %s:%s", xmlConfigPath, message));
            return null;
        } catch (SAXException | JAXBException e) {
            logger.error(String.format("Error loading %s", xmlConfigPath), e);
            return null;
        }
    }

    @Required
    public void setXmlConfigPath(Resource xmlConfigPath) {
        this.xmlConfigPath = xmlConfigPath;
    }

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }
}
