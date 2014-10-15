package utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class DataInterleaver {
    public static ByteBuffer mergePositionNormal(final FloatBuffer positionBuffer, final FloatBuffer normalBuffer) {
        return mergePositionNormalColour(positionBuffer, normalBuffer, null);
    }

    public static ByteBuffer mergePositionNormalColour(final FloatBuffer positionBuffer, final FloatBuffer normalBuffer, final ByteBuffer colourBuffer) {
        // Sanity check.
        if (positionBuffer.capacity() != normalBuffer.capacity() || (colourBuffer != null && positionBuffer.capacity() != colourBuffer.capacity())) {
            throw new IllegalArgumentException("Buffers must be the same size.");
        }

        int numVertices = positionBuffer.capacity() / 3;

        ByteBuffer positionNormalColourBuffer;

        if (colourBuffer != null) {
            final int bytesPerVertex = 3 * ByteSize.FLOAT + 3 * ByteSize.FLOAT + 3 * 1;
            positionNormalColourBuffer = ByteBuffer.allocateDirect(numVertices * bytesPerVertex);
            positionNormalColourBuffer.order(ByteOrder.nativeOrder());

            for (int i = 0; i < positionNormalColourBuffer.capacity(); i += bytesPerVertex) {
                int index = i / bytesPerVertex;
                int base = index * 3;

                positionNormalColourBuffer.putFloat(i, positionBuffer.get(base));
                positionNormalColourBuffer.putFloat(i + 4, positionBuffer.get(base + 1));
                positionNormalColourBuffer.putFloat(i + 8, positionBuffer.get(base + 2));

                positionNormalColourBuffer.putFloat(i + 12, normalBuffer.get(base));
                positionNormalColourBuffer.putFloat(i + 16, normalBuffer.get(base + 1));
                positionNormalColourBuffer.putFloat(i + 20, normalBuffer.get(base + 2));

                base = index * 3;
                positionNormalColourBuffer.put(i + 24, colourBuffer.get(base));
                positionNormalColourBuffer.put(i + 25, colourBuffer.get(base + 1));
                positionNormalColourBuffer.put(i + 26, colourBuffer.get(base + 2));
            }
        } else {
            final int bytesPerVertex = 3 * ByteSize.FLOAT + 3 * ByteSize.FLOAT ;
            positionNormalColourBuffer = ByteBuffer.allocateDirect(numVertices * bytesPerVertex);
            positionNormalColourBuffer.order(ByteOrder.nativeOrder());

            for (int i = 0; i < positionNormalColourBuffer.capacity(); i += bytesPerVertex) {
                int index = i / bytesPerVertex;
                int base = index * 3;

                positionNormalColourBuffer.putFloat(i, positionBuffer.get(base));
                positionNormalColourBuffer.putFloat(i + 4, positionBuffer.get(base + 1));
                positionNormalColourBuffer.putFloat(i + 8, positionBuffer.get(base + 2));

                positionNormalColourBuffer.putFloat(i + 12, normalBuffer.get(base));
                positionNormalColourBuffer.putFloat(i + 16, normalBuffer.get(base + 1));
                positionNormalColourBuffer.putFloat(i + 20, normalBuffer.get(base + 2));
            }
        }

        return positionNormalColourBuffer;
    }
}
