package hierarchy;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class NodeDataBlock {

    private final Node node;
    private FloatBuffer positionBuffer;
    private ByteBuffer colourBuffer;
    private final IntBuffer indexBuffer;

    private ByteBuffer vertexDataBuffer;

    private long lastRequested;

    public NodeDataBlock(final Node node, final FloatBuffer vertexBuffer, final ByteBuffer colourBuffer, final IntBuffer indexBuffer) {
        this.node = node;
        this.positionBuffer = vertexBuffer;
        this.colourBuffer = colourBuffer;
        this.indexBuffer = indexBuffer;

        this.lastRequested = System.currentTimeMillis();
    }

    public final Node getNode() {
        return this.node;
    }

    public ByteBuffer getColourBuffer() {
        return colourBuffer;
    }

    public void setColourBuffer(ByteBuffer colourBuffer) {
        this.colourBuffer = colourBuffer;
    }

    public FloatBuffer getPositionBuffer() {
        return positionBuffer;
    }

    public void setPositionBuffer(FloatBuffer positionBuffer) {
        this.positionBuffer = positionBuffer;
    }

    public final IntBuffer getIndexBuffer() {
        return this.indexBuffer;
    }

    public final ByteBuffer getVertexDataBuffer() {
        return this.vertexDataBuffer;
    }

    public void setVertexDataBuffer(ByteBuffer vertexDataBuffer) {
        this.vertexDataBuffer = vertexDataBuffer;
    }

    public void touchLastRequested() {
        this.lastRequested = System.currentTimeMillis();
    }

    public long getLastRequested() {
        return this.lastRequested;
    }
}
