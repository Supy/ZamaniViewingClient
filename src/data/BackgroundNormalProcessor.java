package data;

import hierarchy.NodeDataBlock;
import utils.NormalsCalculator;
import utils.Stopwatch;

import java.nio.FloatBuffer;

public class BackgroundNormalProcessor extends DataStore implements Runnable {

    @Override
    public void run() {
        while(true) {
            try {
                NodeDataBlock dataBlock = processingQueue.take();

                Stopwatch.start("total time calculating normals");

                FloatBuffer vertexNormalData = NormalsCalculator.mergeWithVertices(dataBlock.getVertexDataBuffer().array(), dataBlock.getIndexBuffer().array());
                dataBlock.setVertexDataBuffer(vertexNormalData);

                nodeData.put(dataBlock.getNode(), dataBlock);
                markNodeNotInTransit(dataBlock.getNode());
                Stopwatch.stop("total time calculating normals");
            } catch (InterruptedException e) {

            }
        }
    }
}
