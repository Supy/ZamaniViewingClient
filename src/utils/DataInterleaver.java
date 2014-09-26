package utils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class DataInterleaver {
    public static ByteBuffer mergePositionNormal(final FloatBuffer positionBuffer, final FloatBuffer normalBuffer) {
        return mergePositionNormalColour(positionBuffer, normalBuffer, null);
    }

    public static ByteBuffer mergePositionNormalColour(final FloatBuffer positionBuffer, final FloatBuffer normalBuffer, final ByteBuffer colourBuffer) {
        // Sanity check.
        if (positionBuffer.capacity() != normalBuffer.capacity() || (colourBuffer != null && positionBuffer.capacity() != (colourBuffer.capacity() / 4 * 3))) {
            throw new IllegalArgumentException("Buffers must be the same size.");
        }

        int numVertices = positionBuffer.capacity() / 3;

        ByteBuffer positionNormalColourBuffer;

        if (colourBuffer != null) {
            final int bytesPerVertex = 3 * ByteSize.FLOAT + 3 * ByteSize.FLOAT + 4 * 1;
            positionNormalColourBuffer = ByteBuffer.allocate(numVertices * bytesPerVertex);

            for (int i = 0; i < positionNormalColourBuffer.capacity(); i += bytesPerVertex) {
                positionNormalColourBuffer.putFloat(positionBuffer.get());
                positionNormalColourBuffer.putFloat(positionBuffer.get());
                positionNormalColourBuffer.putFloat(positionBuffer.get());

                positionNormalColourBuffer.putFloat(normalBuffer.get());
                positionNormalColourBuffer.putFloat(normalBuffer.get());
                positionNormalColourBuffer.putFloat(normalBuffer.get());

                positionNormalColourBuffer.put(colourBuffer.get());
                positionNormalColourBuffer.put(colourBuffer.get());
                positionNormalColourBuffer.put(colourBuffer.get());
                positionNormalColourBuffer.put(colourBuffer.get());
            }
        } else {
            final int bytesPerVertex = 3 * ByteSize.FLOAT + 3 * ByteSize.FLOAT ;
            positionNormalColourBuffer = ByteBuffer.allocate(numVertices * bytesPerVertex);

            for (int i = 0; i < positionNormalColourBuffer.capacity(); i += bytesPerVertex) {
                positionNormalColourBuffer.putFloat(positionBuffer.get());
                positionNormalColourBuffer.putFloat(positionBuffer.get());
                positionNormalColourBuffer.putFloat(positionBuffer.get());

                positionNormalColourBuffer.putFloat(normalBuffer.get());
                positionNormalColourBuffer.putFloat(normalBuffer.get());
                positionNormalColourBuffer.putFloat(normalBuffer.get());
            }
        }

        return positionNormalColourBuffer;
    }
}
