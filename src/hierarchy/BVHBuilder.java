package hierarchy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Stopwatch;

public class BVHBuilder {

    public static Hierarchy fromString(final String contents) throws JSONException {
        JSONObject jo = new JSONObject(contents);
        return buildHierarchy(jo);
    }

    private static Hierarchy buildHierarchy(JSONObject jo) throws JSONException {
        Stopwatch.start("BVHBuilder.buildHierarchy");

        boolean isColour = jo.getBoolean("vertex_colour");
        System.out.println("Hierarchy using colour: " + isColour);
        Hierarchy.hasColour = isColour;

        int maxDepth = jo.getInt("max_depth") + 1;
        System.out.println("Hierarchy depth: " + maxDepth);
        Hierarchy.maxDepth = maxDepth;

        JSONArray ja = jo.getJSONArray("nodes");

        Hierarchy hierarchy = new Hierarchy(ja.length());

        for (int i = 0; i < ja.length(); i++) {
            JSONObject jsonNodeObject = ja.getJSONObject(i);

            Node node = Node.fromJSON(jsonNodeObject);
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
