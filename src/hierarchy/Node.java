package hierarchy;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Node implements Comparable<Node> {
    private int id;

    // Data information
    private int dataBlockOffset;
    private int dataBlockLength;

    // Hierarchy information
    private Node parent = null;
    private int parentId;
    private List<Node> children = new ArrayList<>();
    private List<Node> siblings = null;
    private boolean leafNode = true;

    private List<Vector3D> corners = new ArrayList<>(8);

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public int getDataBlockOffset() {
        return dataBlockOffset;
    }

    private void setDataBlockOffset(int dataBlockOffset) {
        this.dataBlockOffset = dataBlockOffset;
    }

    public int getDataBlockLength() {
        return dataBlockLength;
    }

    private void setDataBlockLength(int dataBlockLength) {
        this.dataBlockLength = dataBlockLength;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
        this.siblings = parent.getChildren();
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void addChild(Node child) {
        if (child != null) {
            this.leafNode = false;
            this.children.add(child);
        }
    }

    public List<Node> getSiblings() {
        return siblings;
    }

    public void addCorner(Vector3D corner) {
        this.corners.add(corner);
    }

    public List<Vector3D> getCorners() {
        return this.corners;
    }

    public boolean isLeafNode() {
        return this.leafNode;
    }

    public static Node fromJSON(JSONObject jo) throws JSONException {
        if(jo == null) {
            throw new NullPointerException();
        }

        double minX = jo.getDouble("min_x");
        double maxX = jo.getDouble("max_x");
        double minY = jo.getDouble("min_y");
        double maxY = jo.getDouble("max_y");
        double minZ = jo.getDouble("min_z");
        double maxZ = jo.getDouble("max_z");

        Node node = new Node();

        node.setId(jo.getInt("id"));
        node.setParentId(jo.optInt("parent_id", -1));
        node.setDataBlockOffset(jo.getInt("block_offset"));
        node.setDataBlockLength(jo.getInt("block_length"));

        // Corners
        node.addCorner(new Vector3D(minX, minY, minZ));
        node.addCorner(new Vector3D(minX, maxY, minZ));
        node.addCorner(new Vector3D(maxX, maxY, minZ));
        node.addCorner(new Vector3D(maxX, minY, minZ));
        node.addCorner(new Vector3D(minX, minY, maxZ));
        node.addCorner(new Vector3D(minX, maxY, maxZ));
        node.addCorner(new Vector3D(maxX, maxY, maxZ));
        node.addCorner(new Vector3D(maxX, minY, maxZ));

        return node;
    }

    @Override
    public int compareTo(Node o) {
        return getId() - o.getId();
    }
}
