package hierarchy;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class BVHFileReader {

    private final RandomAccessFile file;

    public BVHFileReader(String filePath) throws IOException {
        this.file = new RandomAccessFile(filePath, "r");
    }

    public String readHierarchyHeader() throws IOException {
        // Loads in the JSON hierarchy header
        byte[] header = new byte[getHeaderLength()];
        this.file.seek(4);
        this.file.read(header);
        return new String(header);
    }

    public ByteBuffer readBlock(long offset, int numBytes) throws IOException {
        byte[] bytes = new byte[numBytes];
        this.file.seek(offset);
        this.file.read(bytes);
        return ByteBuffer.wrap(bytes);
    }

    public int getDataOffset() throws IOException {
        return 4 + getHeaderLength();
    }

    private int getHeaderLength() throws IOException {
        this.file.seek(0);
        int headerLength = Integer.reverseBytes(this.file.readInt());

        if (headerLength <= 0) {
            throw new RuntimeException("Invalid header length");
        }

        return headerLength;
    }
}
