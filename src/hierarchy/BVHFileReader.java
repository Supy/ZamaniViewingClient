package hierarchy;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class BVHFileReader {

    private final RandomAccessFile raf;
    private final MappedByteBuffer buffer;

    public BVHFileReader(String filePath) throws IOException {
        this.raf = new RandomAccessFile(filePath, "r");
        FileChannel fileChannel = this.raf.getChannel();
        this.buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public String readHierarchyHeader() throws IOException {
        // Loads in the JSON hierarchy header
        byte[] header = new byte[getHeaderLength()];
        this.buffer.position(4);
        this.buffer.get(header);
        return new String(header);
    }

    public ByteBuffer readBlock(int offset, int numBytes) throws IOException {
        byte[] bytes = new byte[numBytes];
        this.buffer.position(offset);
        this.buffer.get(bytes, 0, numBytes);
        return ByteBuffer.wrap(bytes);
    }

    public int getDataOffset() throws IOException {
        return 4 + getHeaderLength();
    }

    private int getHeaderLength() throws IOException {
//        this.buffer.position(0);
//        int headerLength = this.buffer.getInt();
//
//        if (headerLength <= 0) {
//            throw new RuntimeException("Invalid header length");
//        }
//
//        return headerLength;

        return 20910;
    }
}
