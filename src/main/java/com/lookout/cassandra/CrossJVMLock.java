package com.lookout.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

/**
 * Created by rk on 6/20/15.
 */
public class CrossJVMLock {
    private static final String LOCK_FILE_NAME = "healthchk.lck";
    private FileChannel fileChannel;
    private FileLock fileLock;

    private final Path lockfile;
    private static final Logger LOG = LoggerFactory.getLogger(CrossJVMLock.class);

    public CrossJVMLock() {
        final String tmpDir = System.getProperty("java.io.tmpdir");
        lockfile = Paths.get(tmpDir, LOCK_FILE_NAME);
    }

    public void lock() throws IOException {
        synchronized (this) {
            if (fileChannel == null) {
                fileChannel = FileChannel.open(lockfile, EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE));
            }
            try {
                fileLock = fileChannel.tryLock();
            } catch (OverlappingFileLockException e) {
                LOG.error("Could not acquire lock on {}", lockfile);
                throw new IOException(e);
            }
        }
    }

    public void unlock() throws IOException {
        synchronized (this) {
            if (fileLock == null) {
                throw new IllegalStateException("Attempt to unlock without a lock");
            }
            fileLock.release();
            fileLock = null;
            fileChannel.close();
            fileChannel = null;
        }
    }
}
