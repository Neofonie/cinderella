package de.neofonie.common.cinderella.config.xml;

import de.neofonie.common.cinderella.config.CinderellaConfig;
import de.neofonie.common.cinderella.config.loader.CinderellaXmlConfigLoader;
import de.neofonie.common.cinderella.config.loader.CinderellaXmlConfigLoaderFactory;
import de.neofonie.common.cinderella.config.xml.condition.RequestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.Test;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class CinderellaXmlConfigLoaderImplTest {

    private static final Logger logger = LoggerFactory.getLogger(CinderellaXmlConfigLoaderImplTest.class);

    @Test
    public void testLoad() throws Exception {
        InputStream testXml = CinderellaXmlConfig.class.getClassLoader().getResourceAsStream("test.xml");
        assertNotNull(testXml);
        CinderellaXmlConfigLoader cinderellaXmlConfigLoader = createCinderellaXmlConfigLoader(new InputStreamResource(testXml));
        CinderellaConfig cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
        assertNotNull(cinderellaConfig);
        assertEquals(cinderellaConfig.getBlacklistMinutes(), 60);
    }

    @Test
    public void testLoadInvalid() throws Exception {
        InputStream testXml = CinderellaXmlConfig.class.getClassLoader().getResourceAsStream("testInvalid.xml");
        assertNotNull(testXml);
        CinderellaXmlConfigLoader cinderellaXmlConfigLoader = createCinderellaXmlConfigLoader(new InputStreamResource(testXml));
        CinderellaConfig cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
        assertNull(cinderellaConfig);
    }

    @Test
    public void testWrite() throws Exception {
        CinderellaXmlConfig ddosXmlConfig = new CinderellaXmlConfig();
        Rule rule = new Rule();
        Rule rule2 = new Rule();
        ReflectionTestUtils.setField(ddosXmlConfig, "rules", Arrays.asList(rule, rule2));

        ReflectionTestUtils.setField(rule, "conditions", Arrays.asList(new RequestPath("foo"), new RequestPath("bar")));
        JAXBContext jc = JAXBContext.newInstance(CinderellaXmlConfig.CLASSES);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(ddosXmlConfig, System.out);
    }

    @Test
    public void testReload() throws Exception {
        File file = createTempFile();

        copyFile(file, "test.xml");
        CinderellaXmlConfigLoader cinderellaXmlConfigLoader = createCinderellaXmlConfigLoader(new FileSystemResource(file));

        //Test first load
        CinderellaConfig cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
        assertNotNull(cinderellaConfig);
        assertEquals(cinderellaConfig.getBlacklistMinutes(), 60);

        TimeUnit.SECONDS.sleep(1);
        //Test second load
        copyFile(file, "test2.xml");
        TimeUnit.SECONDS.sleep(2);
        cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
        assertNotNull(cinderellaConfig);
        assertEquals(cinderellaConfig.getBlacklistMinutes(), 30);
    }

    private File createTempFile() throws IOException {
        File tempDirectory = Files.createTempDirectory("CinderellaXmlConfigLoaderImplReloadableTest").toFile();
        tempDirectory.deleteOnExit();

        File file = new File(tempDirectory, "temp.xml");
        file.deleteOnExit();
        return file;
    }

    private void copyFile(File file, String name) throws IOException {
        InputStream testXml = CinderellaXmlConfig.class.getClassLoader().getResourceAsStream(name);
        file.delete();
        Files.copy(testXml, file.toPath());
        logger.info("Copy " + name + " to " + file.getAbsolutePath());
    }

    private CinderellaXmlConfigLoader createCinderellaXmlConfigLoader(Resource resource) {
        CinderellaXmlConfigLoaderFactory cinderellaXmlConfigLoader = new CinderellaXmlConfigLoaderFactory();
        cinderellaXmlConfigLoader.setXmlConfigPath(resource);
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        ReflectionTestUtils.setField(cinderellaXmlConfigLoader, "validator", validatorFactory.getValidator());
        return cinderellaXmlConfigLoader.createInstance();
    }
}