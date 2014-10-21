package data;

import com.sun.istack.internal.logging.Logger;
import hierarchy.Hierarchy;
import hierarchy.Node;
import hierarchy.NodeDataBlock;
import ply.PLYReader;
import utils.ByteSize;
import utils.Stopwatch;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;

public class BackgroundDataLoader extends DataStore implements Runnable {

    private static final Logger log = Logger.getLogger(DataStore.class);
    @Override
    public void run() {
        Thread.currentThread().setName("backgroundDataLoader");

        while(true) {
            try {
                Node node = loadQueue.take();
                markNodeInTransit(node);
                Stopwatch.start("total time loading node data");
                loadNodeData(node);
                Stopwatch.stop("total time loading node data");
            } catch (Exception e) {

            }
        }
    }

    private static void loadNodeData(Node node) {
        // Don't need to load node data if we still have it in memory
        if (!nodeData.containsKey(node)) {
            try {
                //log.log(Level.INFO, "Loading data for node " + node.getId() + ". Offset: " + node.getDataBlockOffset() + " Length: " + node.getDataBlockLength());

                ByteBuffer buffer = fileReader.readBlock(baseDataOffset + node.getDataBlockOffset(), node.getDataBlockLength());
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                // VERTICES
                float[] vertices = new float[node.getNumVertices() * 3];

                // Create a view of the byte buffer as a float buffer to allow us to read all vertices
                // into our array using a single call - good sequential read.
                FloatBuffer fb = buffer.asFloatBuffer();
                fb.get(vertices);

                // Forward the marker of the original buffer past the vertex data we just read.
                buffer.position(node.getNumVertices() * 3 * ByteSize.FLOAT);

                byte[] colours = new byte[0];
                if (Hierarchy.hasColour) {
                    // COLOURS
                    // * 3 because RGB
                    colours = new byte[node.getNumVertices() * 3];
                    buffer.get(colours);
                }

                // INDICES
                int[] indices = new int[node.getNumFaces() * 3];

                // Read in all the indices
                IntBuffer ib = buffer.asIntBuffer();
                ib.get(indices);

                NodeDataBlock nodeDataBlock;
                if (Hierarchy.hasColour) {
                    nodeDataBlock = new NodeDataBlock(node, FloatBuffer.wrap(vertices), ByteBuffer.wrap(colours), IntBuffer.wrap(indices));
                } else {
                    nodeDataBlock = new NodeDataBlock(node, FloatBuffer.wrap(vertices), null, IntBuffer.wrap(indices));
                }

                processingQueue.put(nodeDataBlock);

                //log.log(Level.INFO, "Loaded data for node " + node.getId());
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to data block for node " + node.getId(), e);
            }
        }
    }
}
