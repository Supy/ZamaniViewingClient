package bvh;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Stopwatch;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BVHBuilder {

    public static Hierarchy fromFile(final String fileName) throws IOException, JSONException {
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        FileChannel fileChannel = raf.getChannel();
        ByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, Math.min(fileChannel.size(), Integer.MAX_VALUE));

        byte[] fileBytes = new byte[buffer.limit()];
        buffer.get(fileBytes, 0, buffer.limit());
        String fileContents = new String(fileBytes);

        JSONArray ja = new JSONArray(fileContents);
        return buildHierarchy(ja);
    }

    private static Hierarchy buildHierarchy(JSONArray ja) throws JSONException {
        Stopwatch.start("BVHBuilder.buildHierarchy");

        Hierarchy hierarchy = new Hierarchy(ja.length());

        for (int i = 0; i < ja.length(); i++) {
            JSONObject jo = ja.getJSONObject(i);

            Node node = Node.fromJSON(jo);
            hierarchy.setNode(i, node);

            // Add parent and child pointers
            int parentPointer = jo.optInt("parent_id", -1);
            if (parentPointer != -1) {
                Node parentNode = hierarchy.getNode(parentPointer);
                node.setParent(parentNode);
                parentNode.addChild(node);
            }
        }

        Stopwatch.stopAndPrint("BVHBuilder.buildHierarchy");

        return hierarchy;
    }
}
