package data;

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

                FloatBuffer vertexNormalData = NormalsCalculator.mergeWithVertices(dataBlock.getVertexDataBuffer().array(), dataBlock.getIndexBuffer().array());
                dataBlock.setVertexDataBuffer(vertexNormalData);

                nodeData.put(dataBlock.getNode(), dataBlock);
                markNodeNotInTransit(dataBlock.getNode());
                Stopwatch.stop("total time calculating normals <thread " + this.id + ">");
            } catch (InterruptedException e) {

            }
        }
    }
}
