package hierarchy;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class NodeDataBlock {

    private final Node node;
    private FloatBuffer vertexDataBuffer;
    private final IntBuffer indexBuffer;

    private int numVertices;
    private final int numIndices;

    private long lastRequested;

    public NodeDataBlock(final Node node, final FloatBuffer vertexDataBuffer, final IntBuffer indexBuffer) {
        this.node = node;
        this.vertexDataBuffer = vertexDataBuffer;
        this.indexBuffer = indexBuffer;

        this.numVertices = vertexDataBuffer.capacity(); // NodeDataBlock is given the vertex buffer that has not been merged with the vertices yet.
        this.numIndices = indexBuffer.capacity();
        this.lastRequested = System.currentTimeMillis();
    }

    public final Node getNode() {
        return this.node;
    }

    public void setVertexDataBuffer(FloatBuffer vertexDataBuffer) {
        this.vertexDataBuffer = vertexDataBuffer;
        this.numVertices = vertexDataBuffer.capacity();
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

    public void touchLastRequested() {
        this.lastRequested = System.currentTimeMillis();
    }

    public long getLastRequested() {
        return this.lastRequested;
    }
}
