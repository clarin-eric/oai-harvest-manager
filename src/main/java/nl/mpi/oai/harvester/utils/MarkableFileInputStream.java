package nl.mpi.oai.harvester.utils;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @author http://stackoverflow.com/questions/1094703/java-file-input-with-rewind-reset-capability
 */
public class MarkableFileInputStream extends FilterInputStream {
    private FileChannel myFileChannel;
    private long mark = -1;

    public MarkableFileInputStream(FileInputStream fis) {
        super(fis);
        myFileChannel = fis.getChannel();
        mark(0);
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            mark = myFileChannel.position();
        } catch (IOException ex) {
            mark = -1;
        }
    }

    @Override
    public synchronized void reset() throws IOException {
        if (mark == -1) {
            throw new IOException("not marked");
        }
        myFileChannel.position(mark);
    }
}