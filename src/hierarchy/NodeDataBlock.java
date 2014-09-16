package hierarchy;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class NodeDataBlock {

    private final Node node;
    private final FloatBuffer vertexDataBuffer;
    private final IntBuffer indexBuffer;

    private final int numVertices;
    private final int numIndices;

    public NodeDataBlock(final Node node, final FloatBuffer vertexDataBuffer, final IntBuffer indexBuffer) {
        this.node = node;
        this.vertexDataBuffer = vertexDataBuffer;
        this.indexBuffer = indexBuffer;

        this.numVertices = vertexDataBuffer.capacity() / 2 / 3; // Has normal + vertex data, each of 3 floats;
        this.numIndices = indexBuffer.capacity();
    }

    public final Node getNode() {
        return this.node;
    }

    public final FloatBuffer getVertexDataBuffer() {
        return this.vertexDataBuffer;
    }

    public final IntBuffer getIndexBuffer() {
        return this.indexBuffer;
    }

    public int getNumIndices() {
        return this.numIndices;
    }

    public int getNumVertices() {
        return this.numVertices;
    }
}
