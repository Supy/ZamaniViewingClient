package hierarchy;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

enum OP {
    REDUCTION,
    EXPANSION
}

public class HierarchyAdjustmentInstance {
    // Hierarchy operations
    private OP lastOperation;
    private Vector3D lastOperationPosition = Vector3D.ZERO;
    private long lastOperationTime;

    public boolean isSameOperationPosition(Vector3D position) {
        return lastOperationPosition.equals(position);
    }

    public long getLastOperationTime() {
        return this.lastOperationTime;
    }

    public OP getLastOperation() {
        return lastOperation;
    }

    public void setLastOperation(OP lastHierarchyOperation, Vector3D position) {
        this.lastOperation = lastHierarchyOperation;
        this.lastOperationPosition = position;
        this.lastOperationTime = System.currentTimeMillis();
    }
}
