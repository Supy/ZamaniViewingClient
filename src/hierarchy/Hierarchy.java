package hierarchy;

import data.DataStore;
import interactive.Camera;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import utils.FrustumCuller;
import utils.Stopwatch;

import java.util.*;

public class Hierarchy {

    private List<Node> nodeList;
    private HashSet<Node> activeNodes = new HashSet<>();
    private HashSet<Node> visibleNodes = new HashSet<>();
    private boolean[] nodeExpanded;

    public Hierarchy(int numElements) {
        this.nodeList = Arrays.asList(new Node[numElements]);
        nodeExpanded = new boolean[numElements];
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

                if (screenSize > 70) {         // Try expand this node if it's not a leaf.
                    if (node.isLeafNode()) {
                        visibleNodes.add(node);
                        activeNodes.add(node);
                    } else {
                        activeNodes.remove(node);
                        visibleNodes.remove(node);
                        nodesToCheck.addAll(node.getChildren());
                        nodeExpanded[node.getId()] = true;
                    }
                } else if (screenSize > 15) { // Doesn't require expansion, but also doesn't have to be reduced.
                    visibleNodes.add(node);
                    activeNodes.add(node);
                } else {                        // Try reduce node.
                    Node parentNode = node.getParent();
                    if (parentNode != null) {
                        List<Node> siblings = node.getSiblings();
                        if (allNodesBelowProjectedSize(siblings, 15)) {
                            visibleNodes.removeAll(siblings);
                            activeNodes.removeAll(siblings);
                            nodesToCheck.removeAll(siblings);
                            nodesToCheck.add(parentNode);
                            nodeExpanded[parentNode.getId()] = false;
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

    /**
        A node can be rendered if it and ll its siblings have their data loaded, or if
        all their siblings have been expanded.
     */
    public boolean canBeRendered(Node node) {
        // Root node is always renderable.
        if (node.getId() == 0) {
            return true;
        }

        for (Node sibling : node.getSiblings()) {
            if (!DataStore.hasNode(sibling)) {
                if (nodeExpanded[sibling.getId()]) {
                    for (Node child : sibling.getChildren()) {
                        if (!canBeRendered(child)) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    /**
        Returns a set that includes parents and children of the given nodes.
        Useful for pre-loading node data one level up and down the current position in the tree.

        LinkedHashSet gives a predictable iteration order and is used to ensure visible / active nodes
        are loaded before parents and children so that the renderer doesn't end up showing gaps in the
        model. Simple priority queue.
     */
    public LinkedHashSet<Node> getExtendedNodeSet(HashSet<Node> nodes, boolean includeParents, boolean includeChildren) {

        // Convert to linked list so we can sort visible nodes before active nodes
        LinkedList<Node> newList = new LinkedList<>(nodes);
        Collections.sort(newList, new VisibleNodeLoadingComparator());

        if (includeParents) {
            for (Node node : nodes) {
                if (!node.isRootNode()) {
                    newList.add(node.getParent());
                }
            }
        }

        if (includeChildren) {
            for (Node node : nodes) {
                if (!node.isLeafNode()) {
                    newList.addAll(node.getChildren());
                }
            }
        }

        return new LinkedHashSet<>(newList);
    }

    class VisibleNodeLoadingComparator implements Comparator<Node> {
        @Override
        public int compare(Node n1, Node n2) {
            int i1 = visibleNodes.contains(n1) ? 0 : 1;
            int i2 = visibleNodes.contains(n2) ? 0 : 1;

            // If two nodes are visible, load whichever is closer to the camera.
            if (i1 == 0 && i2 == 0) {
                double distance1 = Camera.getPosition().distanceSq(n1.getCenter());
                double distance2 = Camera.getPosition().distanceSq(n2.getCenter());

                return (int) (distance1 - distance2);
            }

            return i1 - i2;
        }
    }
}

