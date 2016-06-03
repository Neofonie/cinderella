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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher {

    private static final Logger logger = LoggerFactory.getLogger(FileWatcher.class);

    public static void createWatcher(File directory, Function<File, Void> function) {

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                watchRun(directory, function);
            }
        };

        createWatcherThread(runnable).start();

    }

    private static void watchRun(File directory, Function<File, Void> function) {
        try {
            while (true) {

                // Sanity check - Check if path is a folder
                waitTillPathExists(directory);

                watchDirectoryPath(directory, function);
            }

        } finally {
            logger.error("Watcher-Thread shutdown");
        }
    }

    private static void waitTillPathExists(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            logger.info(String.format("Path: %s is not a folder or dosnt exists", directory.getAbsolutePath()));
            while (!directory.exists() || !directory.isDirectory()) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    logger.warn("", e);
                }
            }
        }
    }

    private static void watchDirectoryPath(File directory, Function<File, Void> function) {
        if (!directory.exists() || !directory.isDirectory()) {
            logger.warn(String.format("Path: %s is not a folder or dosnt exists", directory.getAbsolutePath()));
            return;
        }

        try {
            logger.info("Watching path: " + directory.getAbsolutePath());

            // We obtain the file system of the Path
            Path path = directory.toPath();

            // We create the new WatchService using the new try() block
            try (WatchService service = path.getFileSystem().newWatchService()) {

                // We register the path to the service
                // We watch for creation events
                path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

                //apply function for created files before watcher starts
                for (File file : directory.listFiles()) {
                    logger.debug(String.format("Existing file %s", file.getAbsolutePath()));
                    function.apply(file);
                }
                // Start the infinite polling loop
                while (true) {
                    WatchKey key = service.take();

                    // Dequeueing events
                    for (WatchEvent<?> watchEvent : key.pollEvents()) {
                        // Get the type of the event
                        Kind<?> kind = watchEvent.kind();
                        if (!OVERFLOW.equals(kind)) {
                            // A new Path was created
                            Path newPath = ((WatchEvent<Path>) watchEvent).context();
                            logger.info("changed " + newPath + " " + kind);
                            function.apply(path.resolve(newPath).toFile());
                        }
                    }

                    if (!key.reset()) {
                        logger.error(String.format("Watching for %s is no longer possible", directory.getAbsolutePath()));
                        break; //loop
                    }
                }

            } catch (IOException | InterruptedException ioe) {
                logger.warn("", ioe);
            }

        } finally {
            logger.error(String.format("Watcher-Thread %s shutdown", directory.getAbsolutePath()));
        }
    }

    private static Thread createWatcherThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName("ReloadableCinderellaXmlConfigLoader-Watcher");
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                logger.error(String.format("ReloadableCinderellaXmlConfigLoader fails, config-changes will no longer be loaded", e));
            }
        });
        return thread;
    }
}
