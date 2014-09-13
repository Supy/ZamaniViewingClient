package bvh;

import javax.media.opengl.GL2;

public class HierarchyRenderer {

    private Hierarchy hierarchy;

    public HierarchyRenderer(Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    public void setup(GL2 gl) {
        hierarchy.expandNode(hierarchy.getRootNode());
    }

    public void draw(GL2 gl) {
        gl.glColor3f(1f, 1f, 0f);
        gl.glBegin(GL2.GL_LINES);

        for(Node node : hierarchy.getActiveNodes()) {
            // Left face
            gl.glVertex3d(node.getMinX(), node.getMinY(), node.getMinZ());
            gl.glVertex3d(node.getMinX(), node.getMaxY(), node.getMinZ());

            gl.glVertex3d(node.getMinX(), node.getMaxY(), node.getMinZ());
            gl.glVertex3d(node.getMaxX(), node.getMaxY(), node.getMinZ());

            gl.glVertex3d(node.getMaxX(), node.getMaxY(), node.getMinZ());
            gl.glVertex3d(node.getMaxX(), node.getMinY(), node.getMinZ());

            gl.glVertex3d(node.getMaxX(), node.getMinY(), node.getMinZ());
            gl.glVertex3d(node.getMinX(), node.getMinY(), node.getMinZ());

            // Right face
            gl.glVertex3d(node.getMinX(), node.getMinY(), node.getMaxZ());
            gl.glVertex3d(node.getMinX(), node.getMaxY(), node.getMaxZ());

            gl.glVertex3d(node.getMinX(), node.getMaxY(), node.getMaxZ());
            gl.glVertex3d(node.getMaxX(), node.getMaxY(), node.getMaxZ());

            gl.glVertex3d(node.getMaxX(), node.getMaxY(), node.getMaxZ());
            gl.glVertex3d(node.getMaxX(), node.getMinY(), node.getMaxZ());

            gl.glVertex3d(node.getMaxX(), node.getMinY(), node.getMaxZ());
            gl.glVertex3d(node.getMinX(), node.getMinY(), node.getMaxZ());

            // Top lines
            gl.glVertex3d(node.getMinX(), node.getMaxY(), node.getMinZ());
            gl.glVertex3d(node.getMinX(), node.getMaxY(), node.getMaxZ());

            gl.glVertex3d(node.getMaxX(), node.getMaxY(), node.getMinZ());
            gl.glVertex3d(node.getMaxX(), node.getMaxY(), node.getMaxZ());

            // Bottom lines
            gl.glVertex3d(node.getMinX(), node.getMinY(), node.getMinZ());
            gl.glVertex3d(node.getMinX(), node.getMinY(), node.getMaxZ());

            gl.glVertex3d(node.getMaxX(), node.getMinY(), node.getMinZ());
            gl.glVertex3d(node.getMaxX(), node.getMinY(), node.getMaxZ());
        }

        gl.glEnd();
    }
}
