/*
 *
 * The MIT License (MIT)
 * Copyright (c) 2016 Neofonie GmbH
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.neofonie.cinderella.core.config.loader;

import de.neofonie.cinderella.core.config.CinderellaConfig;
import de.neofonie.cinderella.core.config.xml.CinderellaXmlConfig;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CinderellaXmlConfigLoaderFactory implements FactoryBean<CinderellaXmlConfigLoader> {

    private static final Logger logger = LoggerFactory.getLogger(CinderellaXmlConfigLoaderFactory.class);
    private Validator validator;
    private Resource xmlConfigPath;
    private Resource xmlConfigPath2;

    public CinderellaXmlConfigLoader createInstance() {
        final List<CinderellaXmlConfigLoader> cinderellaXmlConfigLoaders = new ArrayList<>();
        if (xmlConfigPath != null) {
            cinderellaXmlConfigLoaders.add(createInstance(xmlConfigPath));
        }
        if (xmlConfigPath2 != null) {
            cinderellaXmlConfigLoaders.add(createInstance(xmlConfigPath2));
        }
        if (cinderellaXmlConfigLoaders.isEmpty()) {
            throw new IllegalArgumentException("No Resource defined");
        }
        return new CinderellaXmlConfigLoaderList(cinderellaXmlConfigLoaders);
    }

    private CinderellaXmlConfigLoader createInstance(Resource resource) {

        CinderellaXmlConfig cinderellaConfig = null;
        try {
            cinderellaConfig = load(resource.getInputStream(), resource.toString());
        } catch (FileNotFoundException e) {
            logger.info(e.getMessage());
        } catch (IOException e) {
            logger.error("", e);
        }

        try {
            File file = resource.getFile();

            return new ReloadableCinderellaXmlConfigLoader(file, cinderellaConfig, this);
        } catch (IOException e) {
            //Ignore - occurs if resource is not a file (for example a file in a jar)
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

    private static class CinderellaXmlConfigLoaderList implements CinderellaXmlConfigLoader {
        private final List<CinderellaXmlConfigLoader> cinderellaXmlConfigLoaders;

        private CinderellaXmlConfigLoaderList(List<CinderellaXmlConfigLoader> cinderellaXmlConfigLoaders) {
            this.cinderellaXmlConfigLoaders = cinderellaXmlConfigLoaders;
        }

        @Override
        public CinderellaConfig getCinderellaConfig() {
            for (CinderellaXmlConfigLoader cinderellaXmlConfigLoader : cinderellaXmlConfigLoaders) {
                final CinderellaConfig cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
                if (cinderellaConfig != null) {
                    return cinderellaConfig;
                }
            }
            return null;
        }
    }

    private static class StaticCinderellaXmlConfigLoader implements CinderellaXmlConfigLoader {
        private final CinderellaConfig cinderellaConfig;

        private StaticCinderellaXmlConfigLoader(CinderellaConfig cinderellaConfig) {
            this.cinderellaConfig = cinderellaConfig;
        }

        @Override
        public CinderellaConfig getCinderellaConfig() {
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
            logger.info(String.format("Init FileWatcher for %s", file.getAbsolutePath()));
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
        public CinderellaConfig getCinderellaConfig() {
            return cinderellaConfig.get();
        }
    }

    private CinderellaXmlConfig load(File file) {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            return load(inputStream, file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            logger.error("", e);
            return null;
        } catch (IOException e) {
            logger.error("", e);
            return null;
        }
    }

    private CinderellaXmlConfig load(InputStream inputStream, String path) {
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
            try (InputStream schemaInputStream = CinderellaXmlConfigLoaderFactory.class.getClassLoader().getResourceAsStream("xsd/cinderella.xsd")) {
                Schema schema = schemaFactory.newSchema(new StreamSource(schemaInputStream));
                unmarshaller.setSchema(schema);
            } catch (IOException e) {
                logger.warn("", e);
            }

            CinderellaXmlConfig ddosXmlConfig = (CinderellaXmlConfig) unmarshaller.unmarshal(inputStream);
            ddosXmlConfig.validate();

            logger.info("loaded " + ddosXmlConfig);
            Set<ConstraintViolation<CinderellaXmlConfig>> violations = validator.validate(ddosXmlConfig);
            if (violations.isEmpty()) {
                return ddosXmlConfig;
            }

            String message = violations
                    .stream()
                    .map(s -> s.toString())
                    .collect(Collectors.joining("\n"));
            logger.error(String.format("Error loading %s:%s", path, message));
            return null;
        } catch (SAXException | JAXBException | IllegalArgumentException e) {
            logger.error(String.format("Error loading %s", path), e);
            return null;
        }
    }

    @Required
    public void setXmlConfigPath(Resource xmlConfigPath) {
        this.xmlConfigPath = xmlConfigPath;
    }

    public void setXmlConfigPath2(Resource xmlConfigPath2) {
        this.xmlConfigPath2 = xmlConfigPath2;
    }

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }
}
