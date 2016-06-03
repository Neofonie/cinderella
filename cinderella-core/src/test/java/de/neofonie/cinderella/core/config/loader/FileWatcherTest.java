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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.testng.Assert.*;

public class FileWatcherTest {

    private static final Logger logger = LoggerFactory.getLogger(FileWatcherTest.class);

    @Test
    public void testCreateFileLater() throws Exception {
        File tempDirectory = createTempDir();
        LinkedBlockingQueue<File> files = createWatcher(tempDirectory);

        TimeUnit.SECONDS.sleep(2);
        File tempFile = new File(tempDirectory, "tempFile.xml");
        assertTrue(files.isEmpty());

        FileCopyUtils.copy("Fooo".getBytes(), tempFile);
        check(files, "tempFile.xml");
        assertTrue(files.isEmpty());
    }

    @Test
    public void testExistingFile() throws Exception {
        File tempDirectory = createTempDir();
        File tempFile = new File(tempDirectory, "tempFile.xml");

        FileCopyUtils.copy("Fooo".getBytes(), tempFile);
        LinkedBlockingQueue<File> files = createWatcher(tempDirectory);

        check(files, "tempFile.xml");
        assertTrue(files.isEmpty());
    }

    @Test
    public void testChangeFile() throws Exception {
        File tempDirectory = createTempDir();
        LinkedBlockingQueue<File> files = createWatcher(tempDirectory);

        TimeUnit.SECONDS.sleep(2);
        File tempFile = new File(tempDirectory, "tempFile.xml");
        assertTrue(files.isEmpty());

        FileCopyUtils.copy("Fooo".getBytes(), tempFile);
        check(files, "tempFile.xml");

        FileCopyUtils.copy("FoooBar".getBytes(), tempFile);
        check(files, "tempFile.xml");
        assertTrue(files.isEmpty());
    }

    @Test
    public void testCreateDirLater() throws Exception {
        File tempDirectory = createTempDir();
        tempDirectory.delete();
        LinkedBlockingQueue<File> files = createWatcher(tempDirectory);

        TimeUnit.SECONDS.sleep(2);
        tempDirectory.mkdir();
        File tempFile = new File(tempDirectory, "tempFile.xml");
        assertTrue(files.isEmpty());

        FileCopyUtils.copy("Fooo".getBytes(), tempFile);
        check(files, "tempFile.xml");
        assertTrue(files.isEmpty());
    }

    @Test
    public void testDeleteDirLater() throws Exception {
        File tempDirectory = createTempDir();
        LinkedBlockingQueue<File> files = createWatcher(tempDirectory);

        TimeUnit.SECONDS.sleep(2);
        tempDirectory.delete();

        TimeUnit.SECONDS.sleep(2);
        tempDirectory.mkdir();

        File tempFile = new File(tempDirectory, "tempFile.xml");
        FileCopyUtils.copy("Fooo".getBytes(), tempFile);
        final String actual = "tempFile.xml";

        check(files, actual);

        File tempFile2 = new File(tempDirectory, "tempFile2.xml");
        FileCopyUtils.copy("Fooo".getBytes(), tempFile2);
        check(files, "tempFile2.xml");

        File take = files.poll(10, TimeUnit.SECONDS);
        assertNull(take);
//        assertTrue(files.isEmpty());
    }

    private void check(LinkedBlockingQueue<File> files, String expected) throws InterruptedException {
        File take = files.poll(10, TimeUnit.SECONDS);
        assertNotNull(take);
        assertEquals(take.getName(), expected);

        //The files are added multiply
        while (true) {
            take = files.poll(1, TimeUnit.SECONDS);
            if (take == null) {
                return;
            }
            assertNotNull(take);
            assertEquals(take.getName(), expected);
        }
    }

    private LinkedBlockingQueue<File> createWatcher(File tempDirectory) {
        LinkedBlockingQueue<File> files = new LinkedBlockingQueue<File>();
        FileWatcher.createWatcher(tempDirectory, new Function<File, Void>() {
            @Override
            public Void apply(File file) {
                if (!files.contains(file)) {
                    logger.info(String.format("file %s changed", file.getAbsolutePath()));
                    files.add(file);
                }
                return null;
            }
        });
        return files;
    }

    private File createTempDir() throws IOException {
        final File file = Files.createTempDirectory("FileWatcherTest").toFile();
        logger.info(String.format("Create temp dir %s", file.getAbsolutePath()));
        file.deleteOnExit();
        return file;
    }
}