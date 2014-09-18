package hierarchy;

import interactive.Camera;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import utils.FrustumCuller;
import utils.Stopwatch;

import java.util.*;

public class Hierarchy {

    private List<Node> nodeList;
    private HashSet<Node> activeNodes = new HashSet<>();
    private HashSet<Node> visibleNodes = new HashSet<>();

    public Hierarchy(int numElements) {
        this.nodeList = Arrays.asList(new Node[numElements]);
    }

    public void updateNodeVisibility() {
        FrustumCuller.setFrustumMatrices(Camera.getModelViewMatrix(), Camera.getProjectionMatrix());

        Vector3D[] corners = new Vector3D[8];

        visibleNodes = new HashSet<>();
        Queue<Node> nodesToCheck = new LinkedList<>(activeNodes);

        while(!nodesToCheck.isEmpty()) {
            Node node = nodesToCheck.poll();

            node.getCorners().toArray(corners);
            if (FrustumCuller.isBoxVisible(corners)) {
                double screenSize = Camera.getProjectedScreenSize(corners);

                if (screenSize > 65) {         // Try expand this node if it's not a leaf.
                    if (node.isLeafNode()) {
                        visibleNodes.add(node);
                        activeNodes.add(node);
                    } else {
                        activeNodes.remove(node);
                        visibleNodes.remove(node);
                        nodesToCheck.addAll(node.getChildren());
                    }
                } else if (screenSize > 10) { // Doesn't require expansion, but also doesn't have to be reduced.
                    visibleNodes.add(node);
                    activeNodes.add(node);
                } else {                        // Try reduce node.
                    Node parentNode = node.getParent();
                    if (parentNode != null) {
                        List<Node> siblings = node.getSiblings();
                        if (allNodesBelowProjectedSize(siblings, 10)) {
                            visibleNodes.removeAll(siblings);
                            activeNodes.removeAll(siblings);
                            nodesToCheck.removeAll(siblings);
                            nodesToCheck.add(parentNode);
                        } else {
                            // If we can't remove all siblings of this node, then we have to render it until we can.
                            visibleNodes.add(node);
                            activeNodes.add(node);
                        }
                    } else {
                        // Always ensure root node is active even if it's reduced completely.
                        activeNodes.add(node);
                    }
                }
            } else {
                activeNodes.add(node);
            }
        }
    }

    private boolean allNodesBelowProjectedSize(final List<Node> nodes, double threshold) {
        Vector3D[] corners = new Vector3D[8];
        for (Node node : nodes) {
            node.getCorners().toArray(corners);

            double projectedScreenSize = Camera.getProjectedScreenSize(corners);
            if (projectedScreenSize > threshold) {
                return false;
            }
        }

        return true;
    }

    public int getNumNodes() {
        return nodeList.size();
    }

    public void setNode(int index, Node node) {
        this.nodeList.set(index, node);

        // Add the root node to the active nodes list to start the hierarchy.
        if (index == 0) {
            activeNodes.add(node);
        }
    }

    public Node getNode(int index) {
        return this.nodeList.get(index);
    }

    public HashSet<Node> getVisibleNodes() {
        return this.visibleNodes;
    }

    public HashSet<Node> getActiveNodes() {
        return this.activeNodes;
    }

}
