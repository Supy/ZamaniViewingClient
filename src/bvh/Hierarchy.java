package bvh;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Hierarchy {

    private List<Node> nodeList;
    private HashSet<Node> activeNodes = new HashSet<>();

    public Hierarchy(int numElements) {
        this.nodeList = new ArrayList<>(numElements);
    }

    public Node getRootNode() {
        return this.nodeList.get(0);
    }

    public void setNode(int index, Node node) {
        this.nodeList.add(index, node);
    }

    public Node getNode(int index) {
        return this.nodeList.get(index);
    }

    public HashSet<Node> getActiveNodes() {
        return this.activeNodes;
    }

    public void expandNode(Node node) {
        // Can't expand leaf nodes.
        if (!node.isLeafNode()) {
            this.activeNodes.addAll(node.getChildren());
            this.activeNodes.remove(node);
        }
    }

    public void reduceNode(Node node) {
        Node parentNode = node.getParent();
        // Can't reduce the root node.
        if (parentNode != null) {
            this.activeNodes.add(parentNode);
            this.activeNodes.removeAll(parentNode.getChildren());
        }
    }
}
