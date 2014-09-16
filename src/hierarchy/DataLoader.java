package hierarchy;

import com.sun.istack.internal.logging.Logger;
import ply.PLYReader;
import utils.NormalsCalculator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DataLoader {

    private static final Logger log = Logger.getLogger(DataLoader.class);

    private static BVHFileReader fileReader;
    private static int baseDataOffset;
    private static final ConcurrentHashMap<Node, NodeDataBlock> nodeData = new ConcurrentHashMap<>();

    public static void loadAllNodeData(List<Node> nodeList) {
        // Sort list to make more sequential reading.
        Collections.sort(nodeList);

        for (Node node : nodeList) {
            loadNodeData(node);
        }
    }

    public static void loadNodeData(Node node) {
        // Don't need to load node data if we still have it in memory
        if (!nodeData.containsKey(node)) {
            try {
                log.log(Level.INFO, "Loading data for node " + node.getId() + ". Offset: " + node.getDataBlockOffset() + " Length: " + node.getDataBlockLength());

                ByteBuffer nodeDataBuffer = fileReader.readBlock(baseDataOffset + node.getDataBlockOffset(), node.getDataBlockLength());
                nodeDataBuffer.order(ByteOrder.LITTLE_ENDIAN);
                PLYReader plyReader = new PLYReader(nodeDataBuffer);

                float[] vertices = plyReader.getVertices();
                int[] indices = plyReader.getIndices();
                
                float[] vertexNormalData = NormalsCalculator.mergeWithVertices(vertices, indices);

                NodeDataBlock nodeDataBlock = new NodeDataBlock(node, FloatBuffer.wrap(vertexNormalData), IntBuffer.wrap(plyReader.getIndices()));
                nodeData.put(node, nodeDataBlock);

                log.log(Level.INFO, "Loaded data for node " + node.getId());
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to data block for node " + node.getId(), e);
            }
        }
    }

    public static final NodeDataBlock getNodeData(Node node) {
        return nodeData.get(node);
    }

    public static void setFileReader(BVHFileReader fr) throws IOException {
        fileReader = fr;
        baseDataOffset = fr.getDataOffset();
    }
}
