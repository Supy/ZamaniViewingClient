package data;

import com.sun.istack.internal.logging.Logger;
import hierarchy.Node;
import hierarchy.NodeDataBlock;
import ply.PLYReader;
import utils.Stopwatch;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BackgroundDataLoader extends DataStore implements Runnable {

    private static final Logger log = Logger.getLogger(DataStore.class);

    @Override
    public void run() {
        while(true) {
            try {
                Node node = loadQueue.take();
                markNodeInTransit(node);
                Stopwatch.start("total time loading node data");
                loadNodeData(node);
                Stopwatch.stop("total time loading node data");
            } catch (InterruptedException e) {

            }
        }
    }

    private static void loadNodeData(Node node) {
        // Don't need to load node data if we still have it in memory
        if (!nodeData.containsKey(node)) {
            try {
                //log.log(Level.INFO, "Loading data for node " + node.getId() + ". Offset: " + node.getDataBlockOffset() + " Length: " + node.getDataBlockLength());

                ByteBuffer nodeDataBuffer = fileReader.readBlock(baseDataOffset + node.getDataBlockOffset(), node.getDataBlockLength());
                nodeDataBuffer.order(ByteOrder.LITTLE_ENDIAN);
                PLYReader plyReader = new PLYReader(nodeDataBuffer);

                NodeDataBlock nodeDataBlock = new NodeDataBlock(node, FloatBuffer.wrap(plyReader.getVertices()), IntBuffer.wrap(plyReader.getIndices()));
                processingQueue.put(nodeDataBlock);
                //log.log(Level.INFO, "Loaded data for node " + node.getId());
            } catch (Exception e) {
                //log.log(Level.WARNING, "Failed to data block for node " + node.getId(), e);
            }
        }
    }
}
