package ply;

import utils.ByteSize;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class PLYReader {

    private final ByteBuffer buffer;
    private final PLYHeader header;

    private float[] vertices;
    private int[] indices;

    public PLYReader(ByteBuffer buffer) throws InvalidFileException, IOException {
        this.buffer = buffer;
        this.header = new PLYHeader(buffer);
        readVertices();
    }

    private void readVertices() {
        vertices = new float[this.header.getVertexCount() * 3];
        indices = new int[this.header.getFaceCount() * 3];

        // Set marker to start of the data.
        this.buffer.position(this.header.getDataOffset());

        // Create a view of the byte buffer as a float buffer to allow us to read all vertices
        // into our array using a single call - good sequential read.
        FloatBuffer fb = this.buffer.asFloatBuffer();
        fb.get(vertices);

        // Forward the marker of the original buffer past the vertex data we just read.
        this.buffer.position(fb.position() * ByteSize.FLOAT + this.header.getDataOffset());

        for (int i = 0; i < this.header.getFaceCount(); i++) {
            this.buffer.get();
            indices[i * 3] = this.buffer.getInt();
            indices[i * 3 + 1] = this.buffer.getInt();
            indices[i * 3 + 2] = this.buffer.getInt();
        }
    }

    public float[] getVertices() {
        return this.vertices;
    }

    public int[] getIndices() {
        return this.indices;
    }
}
