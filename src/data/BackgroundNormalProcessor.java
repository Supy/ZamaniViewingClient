package data;

import hierarchy.Hierarchy;
import hierarchy.Node;
import hierarchy.NodeDataBlock;
import utils.DataInterleaver;
import utils.NormalsCalculator;
import utils.Stopwatch;

import java.nio.ByteBuffer;
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

                    FloatBuffer normalBuffer = FloatBuffer.wrap(NormalsCalculator.calculateFrom(dataBlock.getPositionBuffer().array(), dataBlock.getIndexBuffer().array()));
                    ByteBuffer vertexDataBuffer;

                    if (Hierarchy.hasColour) {
                        vertexDataBuffer = DataInterleaver.mergePositionNormalColour(dataBlock.getPositionBuffer(), normalBuffer, dataBlock.getColourBuffer());
                    } else {
                        vertexDataBuffer = DataInterleaver.mergePositionNormal(dataBlock.getPositionBuffer(), normalBuffer);
                    }

                    // Set interleaved buffer and clear the others
                    dataBlock.setVertexDataBuffer(vertexDataBuffer);
                    dataBlock.setPositionBuffer(null);
                    dataBlock.setColourBuffer(null);

                    nodeData.put(dataBlock.getNode(), dataBlock);

                } catch (ArrayIndexOutOfBoundsException e) {
                    Node node = dataBlock.getNode();
                    System.err.println(
                            String.format(
                                    "Failed to calculate normals for node %s. Read from %s for %s bytes. Vertices: %s Faces: %s",
                                    node.getId(),
                                    node.getDataBlockOffset(),
                                    node.getDataBlockLength(),
                                    node.getNumVertices(),
                                    node.getNumFaces()
                            )
                    );
                    e.printStackTrace(System.err);
                }

                markNodeNotInTransit(dataBlock.getNode());
                Stopwatch.stop("total time calculating normals <thread " + this.id + ">");
            } catch (InterruptedException ignored) { }
        }
    }
}
