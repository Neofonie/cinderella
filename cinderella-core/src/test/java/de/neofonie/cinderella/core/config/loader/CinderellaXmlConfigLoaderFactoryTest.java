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
import de.neofonie.cinderella.core.config.xml.Rule;
import de.neofonie.cinderella.core.config.xml.condition.RequestPath;
import org.easymock.EasyMock;
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
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class CinderellaXmlConfigLoaderFactoryTest {

    private static final Logger logger = LoggerFactory.getLogger(CinderellaXmlConfigLoaderFactoryTest.class);

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNothingSet() throws Exception {
        CinderellaXmlConfigLoaderFactory cinderellaXmlConfigLoaderFactory = new CinderellaXmlConfigLoaderFactory();

        final CinderellaXmlConfigLoader cinderellaXmlConfigLoader = cinderellaXmlConfigLoaderFactory.getObject();
        assertNotNull(cinderellaXmlConfigLoader);
        assertNull(cinderellaXmlConfigLoader.getCinderellaConfig());
    }

    @Test
    public void testInputStreamFails() throws Exception {
        CinderellaXmlConfigLoaderFactory cinderellaXmlConfigLoaderFactory = new CinderellaXmlConfigLoaderFactory();

        Resource xmlConfigPath = EasyMock.createMock(Resource.class);
        cinderellaXmlConfigLoaderFactory.setXmlConfigPath(xmlConfigPath);
        EasyMock.expect(xmlConfigPath.getInputStream()).andThrow(new IOException());
        EasyMock.expect(xmlConfigPath.getFile()).andThrow(new IOException());

        EasyMock.replay(xmlConfigPath);
        final CinderellaXmlConfigLoader cinderellaXmlConfigLoader = cinderellaXmlConfigLoaderFactory.getObject();
        assertNotNull(cinderellaXmlConfigLoader);
        assertNull(cinderellaXmlConfigLoader.getCinderellaConfig());
        EasyMock.verify(xmlConfigPath);
    }

    @Test
    public void testResourceThrowsIOException() throws Exception {
        CinderellaXmlConfigLoaderFactory cinderellaXmlConfigLoaderFactory = new CinderellaXmlConfigLoaderFactory();

        Resource xmlConfigPath = EasyMock.createMock(Resource.class);
        cinderellaXmlConfigLoaderFactory.setXmlConfigPath(xmlConfigPath);
        EasyMock.expect(xmlConfigPath.getInputStream()).andThrow(new IOException());
        EasyMock.expect(xmlConfigPath.getFile()).andThrow(new IOException());

        EasyMock.replay(xmlConfigPath);
        final CinderellaXmlConfigLoader cinderellaXmlConfigLoader = cinderellaXmlConfigLoaderFactory.getObject();
        assertNotNull(cinderellaXmlConfigLoader);
        assertNull(cinderellaXmlConfigLoader.getCinderellaConfig());
        EasyMock.verify(xmlConfigPath);
    }

    @Test
    public void testLoad() throws Exception {
        InputStream testXml = CinderellaXmlConfig.class.getClassLoader().getResourceAsStream("test.xml");
        assertNotNull(testXml);
        CinderellaXmlConfigLoader cinderellaXmlConfigLoader = createCinderellaXmlConfigLoader(new InputStreamResource(testXml), null);
        CinderellaConfig cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
        assertNotNull(cinderellaConfig);
        assertEquals(cinderellaConfig.getBlacklistMinutes(), 60);
    }

    @Test
    public void testLoadInvalid() throws Exception {
        InputStream testXml = CinderellaXmlConfig.class.getClassLoader().getResourceAsStream("testInvalid.xml");
        assertNotNull(testXml);
        CinderellaXmlConfigLoader cinderellaXmlConfigLoader = createCinderellaXmlConfigLoader(new InputStreamResource(testXml), null);
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
        CinderellaXmlConfigLoader cinderellaXmlConfigLoader = createCinderellaXmlConfigLoader(new FileSystemResource(file), null);

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

    @Test
    public void testCreateFileLater() throws Exception {
        File tempDirectory = createTempDir();
        TestUtils.delete(tempDirectory);
        File file = new File(tempDirectory, "temp.xml");

        CinderellaXmlConfigLoader cinderellaXmlConfigLoader = createCinderellaXmlConfigLoader(new FileSystemResource(file), null);

        //Test first load
        CinderellaConfig cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
        assertNull(cinderellaConfig);

        TimeUnit.SECONDS.sleep(10);
        assertTrue(tempDirectory.mkdir());
        copyFile(file, "test.xml");

        TimeUnit.SECONDS.sleep(15);
        cinderellaConfig = cinderellaXmlConfigLoader.getCinderellaConfig();
        assertNotNull(cinderellaConfig);
    }

    private File createTempFile() throws IOException {
        File tempDirectory = createTempDir();
        tempDirectory.deleteOnExit();

        File file = new File(tempDirectory, "temp.xml");
        file.deleteOnExit();
        return file;
    }

    private File createTempDir() throws IOException {
        return TestUtils.createTempDirectory("CinderellaXmlConfigLoaderImplReloadableTest");
    }

    private void copyFile(File file, String name) throws IOException {
        InputStream testXml = CinderellaXmlConfig.class.getClassLoader().getResourceAsStream(name);
        Files.copy(testXml, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        logger.info("Copy " + name + " to " + file.getAbsolutePath());
    }

    private CinderellaXmlConfigLoader createCinderellaXmlConfigLoader(Resource resource, Resource resource2) {
        CinderellaXmlConfigLoaderFactory cinderellaXmlConfigLoader = new CinderellaXmlConfigLoaderFactory();
        cinderellaXmlConfigLoader.setXmlConfigPath(resource);
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        ReflectionTestUtils.setField(cinderellaXmlConfigLoader, "validator", validatorFactory.getValidator());
        return cinderellaXmlConfigLoader.createInstance();
    }
}