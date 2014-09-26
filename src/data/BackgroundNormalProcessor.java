package data;

import hierarchy.Node;
import hierarchy.NodeDataBlock;
import utils.NormalsCalculator;
import utils.Stopwatch;

import java.nio.FloatBuffer;

public class BackgroundNormalProcessor extends DataStore implements Runnable {

    private final int id;

    public BackgroundNormalProcessor(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        while(true) {
            try {
                NodeDataBlock dataBlock = processingQueue.take();

                Stopwatch.start("total time calculating normals <thread " + this.id + ">");

                try {
                    FloatBuffer vertexNormalData = NormalsCalculator.mergeWithVertices(dataBlock.getVertexDataBuffer().array(), dataBlock.getIndexBuffer().array());
                    dataBlock.setVertexDataBuffer(vertexNormalData);
                    nodeData.put(dataBlock.getNode(), dataBlock);
                } catch (ArrayIndexOutOfBoundsException e) {
                    Node node = dataBlock.getNode();
                    System.err.println(
                            String.format(
                                    "Failed to calculate normals for node %s. Read from %s for %s bytes. Vertices: %s Indices: %s",
                                    node.getId(),
                                    node.getDataBlockOffset(),
                                    node.getDataBlockLength(),
                                    dataBlock.getNumVertices(),
                                    dataBlock.getNumIndices()
                            )
                    );
                    e.printStackTrace(System.err);
                }

                markNodeNotInTransit(dataBlock.getNode());
                Stopwatch.stop("total time calculating normals <thread " + this.id + ">");
            } catch (InterruptedException e) {

            }
        }
    }
}
