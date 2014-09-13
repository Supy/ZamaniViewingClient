package bvh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private int id;

    // Data information
    private long dataBlockOffset;
    private long dataBlockLength;

    // Hierarchy information
    private Node parent;
    private List<Node> children = new ArrayList<>();
    private boolean leafNode = true;

    // Geometry information
    private long numVertices;
    private long numFaces;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double minZ;
    private double maxZ;

    private Vector3D center;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDataBlockOffset() {
        return dataBlockOffset;
    }

    public void setDataBlockOffset(long dataBlockOffset) {
        this.dataBlockOffset = dataBlockOffset;
    }

    public long getDataBlockLength() {
        return dataBlockLength;
    }

    public void setDataBlockLength(long dataBlockLength) {
        this.dataBlockLength = dataBlockLength;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
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

    public long getNumVertices() {
        return numVertices;
    }

    public void setNumVertices(long numVertices) {
        this.numVertices = numVertices;
    }

    public long getNumFaces() {
        return numFaces;
    }

    public void setNumFaces(long numFaces) {
        this.numFaces = numFaces;
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public double getMinZ() {
        return minZ;
    }

    public void setMinZ(double minZ) {
        this.minZ = minZ;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(double maxZ) {
        this.maxZ = maxZ;
    }

    public Vector3D getCenter() {
        return center;
    }

    public void setCenter(Vector3D center) {
        this.center = center;
    }

    public boolean isLeafNode() {
        return this.leafNode;
    }

    private void calculateCenter() {
        this.setCenter(new Vector3D(new double[]{ (this.minX + this.maxX) / 2, (this.minY + this.maxY) / 2, (this.minZ + this.maxZ) / 2 }));
    }

    public static Node fromJSON(JSONObject jo) throws JSONException {
        if(jo == null) {
            throw new NullPointerException();
        }

        Node node = new Node();

        node.setId(jo.getInt("id"));
        node.setDataBlockOffset(jo.getLong("block_offset"));
        node.setDataBlockLength(jo.getLong("block_length"));
        node.setMinX(jo.getDouble("min_x"));
        node.setMaxX(jo.getDouble("max_x"));
        node.setMinY(jo.getDouble("min_y"));
        node.setMaxY(jo.getDouble("max_y"));
        node.setMinZ(jo.getDouble("min_z"));
        node.setMaxZ(jo.getDouble("max_z"));
        node.calculateCenter();

        return node;
    }
}
