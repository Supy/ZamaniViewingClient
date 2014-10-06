package hierarchy;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Node implements Comparable<Node> {

    private int id;
    private int depth;

    // Data information
    private int dataBlockOffset;
    private int dataBlockLength;
    private int numVertices;
    private int numFaces;

    // Hierarchy information
    private Node parent = null;
    private int parentId;
    private List<Node> children = new ArrayList<>();
    private List<Node> siblings = null;
    private boolean leafNode = true;

    private Vector3D center;
    private List<Vector3D> corners = new ArrayList<>(8);

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public boolean isRootNode() {
        return this.id == 0;
    }

    public boolean isLeafNode() {
        return this.leafNode;
    }

    private void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return this.depth;
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

    public int getNumVertices() {
        return numVertices;
    }

    private void setNumVertices(int numVertices) {
        this.numVertices = numVertices;
    }

    public int getNumFaces() {
        return numFaces;
    }

    private void setNumFaces(int numFaces) {
        this.numFaces = numFaces;
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

    public Vector3D getCenter() {
        return this.center;
    }

    public void calculateCenter() {
        Vector3D sum = Vector3D.ZERO;
        for(Vector3D corner : this.corners) {
            sum.add(corner);
        }

        this.center = sum.scalarMultiply(1.0 / 8.0);
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
        node.setDepth(jo.getInt("depth"));
        node.setParentId(jo.optInt("parent_id", -1));
        node.setDataBlockOffset(jo.getInt("block_offset"));
        node.setDataBlockLength(jo.getInt("block_length"));
        node.setNumVertices(jo.getInt("num_vertices"));
        node.setNumFaces(jo.getInt("num_faces"));

        // Corners
        node.addCorner(new Vector3D(minX, minY, minZ));
        node.addCorner(new Vector3D(minX, maxY, minZ));
        node.addCorner(new Vector3D(maxX, maxY, minZ));
        node.addCorner(new Vector3D(maxX, minY, minZ));
        node.addCorner(new Vector3D(minX, minY, maxZ));
        node.addCorner(new Vector3D(minX, maxY, maxZ));
        node.addCorner(new Vector3D(maxX, maxY, maxZ));
        node.addCorner(new Vector3D(maxX, minY, maxZ));

        node.calculateCenter();

        return node;
    }

    @Override
    public int compareTo(Node o) {
        return getId() - o.getId();
    }
}
