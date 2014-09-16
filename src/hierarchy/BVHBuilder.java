package hierarchy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Stopwatch;

public class BVHBuilder {

    public static Hierarchy fromString(final String contents) throws JSONException {
        JSONArray ja = new JSONArray(contents);
        return buildHierarchy(ja);
    }

    private static Hierarchy buildHierarchy(JSONArray ja) throws JSONException {
        Stopwatch.start("BVHBuilder.buildHierarchy");

        Hierarchy hierarchy = new Hierarchy(ja.length());

        for (int i = 0; i < ja.length(); i++) {
            JSONObject jo = ja.getJSONObject(i);

            Node node = Node.fromJSON(jo);
            hierarchy.setNode(node.getId(), node);
        }

        // Loop through again to add parent and child pointers.
        for (int i = 0; i < ja.length(); i++) {
            Node node = hierarchy.getNode(i);

            int parentPointer = node.getParentId();
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
