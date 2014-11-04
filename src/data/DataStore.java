package data;

import hierarchy.BVHFileReader;
import hierarchy.Node;
import hierarchy.NodeDataBlock;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class DataStore {

    private final static long INACTIVE_NODE_CUTOFF_TIME = 8000;

    protected static BVHFileReader fileReader;
    protected static int baseDataOffset;
    protected static final ConcurrentHashMap<Node, NodeDataBlock> nodeData = new ConcurrentHashMap<>();

    protected static final BlockingQueue<Node> loadQueue = new LinkedBlockingQueue<>();
    protected static final BlockingQueue<NodeDataBlock> processingQueue = new LinkedBlockingQueue<>();

    protected static final HashSet<Node> nodesInTransit = new HashSet<>();


    static {
        new Thread(new BackgroundDataLoader()).start();
        new Thread(new BackgroundNormalProcessor(1)).start();
        new Thread(new BackgroundNormalProcessor(2)).start();
        new Thread(new BackgroundNormalProcessor(3)).start();
    }

    protected static synchronized boolean isNodeInTransit(Node node) {
        return nodesInTransit.contains(node);
    }
    protected static synchronized void markNodeInTransit(Node node) {
        nodesInTransit.add(node);
    }
    protected static synchronized void markNodeNotInTransit(Node node) {
        nodesInTransit.remove(node);
    }

    public static boolean hasNode(Node node) {
        return nodeData.containsKey(node);
    }

    public static void loadAllNodeData(LinkedHashSet<Node> nodeList) {
        loadQueue.clear();
        // Sort list to make more sequential reading.
        for (Node node : nodeList) {
            if (!nodeData.containsKey(node) && !isNodeInTransit(node)) {
                loadQueue.offer(node);
            }
        }
    }

    public static final NodeDataBlock getNodeData(Node node) {
        if(nodeData.containsKey(node)) {
            NodeDataBlock nodeDataBlock = nodeData.get(node);
            nodeDataBlock.touchLastRequested();
            return nodeDataBlock;
        } else {
            return null;
        }
    }

    public static void setFileReader(BVHFileReader fr) throws IOException {
        fileReader = fr;
        baseDataOffset = fr.getDataOffset();
    }

    public static List<Map.Entry<Node, NodeDataBlock>> clearInactiveNodeDataBlocks(HashSet<Node> activeNodes) {
        // List of nodes that have been removed that should be invalidated in OpenGL
        List<Map.Entry<Node, NodeDataBlock>> clearedNodes = new LinkedList<>();

        long currentTime = System.currentTimeMillis();

        for (Map.Entry<Node, NodeDataBlock> nodeSet : nodeData.entrySet()) {
            Node node = nodeSet.getKey();

            // Only clear nodes that are not in the active nodes list.
            if (!activeNodes.contains(node)) {
                NodeDataBlock dataBlock = nodeSet.getValue();

                if (currentTime - dataBlock.getLastRequested() >= INACTIVE_NODE_CUTOFF_TIME) {
                    nodeData.remove(node);
                    clearedNodes.add(nodeSet);
                }
            }
        }

        return clearedNodes;
    }

    public static void invalidateCache() {
        nodeData.clear();
    }

}
