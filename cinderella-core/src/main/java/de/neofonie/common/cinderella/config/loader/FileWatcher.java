package de.neofonie.common.cinderella.config.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.util.function.Function;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher {

    private static final Logger logger = LoggerFactory.getLogger(FileWatcher.class);

    public static void createWatcher(File directory, Function<File, Void> function) {

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Path: " + directory.getAbsolutePath() + " is not a folder");
        }

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                watchDirectoryPath(directory, function);
            }
        };

        createWatcherThread(runnable).start();

    }

    public static void watchDirectoryPath(File directory, Function<File, Void> function) {
        // Sanity check - Check if path is a folder
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Path: " + directory.getAbsolutePath() + " is not a folder");
        }

        logger.info("Watching path: " + directory.getAbsolutePath());

        // We obtain the file system of the Path
        Path path = directory.toPath();
        FileSystem fs = path.getFileSystem();

        // We create the new WatchService using the new try() block
        try (WatchService service = fs.newWatchService()) {

            // We register the path to the service
            // We watch for creation events
            path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

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
                    break; //loop
                }
            }

        } catch (IOException | InterruptedException ioe) {
            logger.error("", ioe);
        } finally {
            logger.error("Watcher-Thread shutdown");
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
