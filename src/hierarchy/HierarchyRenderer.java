package hierarchy;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import utils.Useful;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import java.util.HashSet;
import java.util.List;

public class HierarchyRenderer {

    private static final double[] boundingVolumeEndColour = new double[] {0.1667,1,1};
    private static final double[] boundingVolumeStartColour = new double[] {0.6583,1,1};

    private Hierarchy hierarchy;

    public HierarchyRenderer(Hierarchy hierarchy) {
        if (hierarchy == null) {
            throw new NullPointerException("Hierarchy cannot be null.");
        }

        this.hierarchy = hierarchy;
    }

    public void draw(GLAutoDrawable glAutoDrawable) {
        GL2 gl = (GL2) glAutoDrawable.getGL();

        HashSet<Node> visibleNodes = hierarchy.getVisibleNodes();

        gl.glLineWidth(3);
        gl.glBegin(GL2.GL_LINES);

        for(Node node : visibleNodes) {
            double[] lineColour = Useful.fadeColour(boundingVolumeStartColour, boundingVolumeEndColour, node.getDepth() / (double) Hierarchy.maxDepth);
            gl.glColor3dv(lineColour, 0);

            List<Vector3D> corners = node.getCorners();

            // Left face
            gl.glVertex3dv(corners.get(0).toArray(), 0);
            gl.glVertex3dv(corners.get(1).toArray(), 0);

            gl.glVertex3dv(corners.get(1).toArray(), 0);
            gl.glVertex3dv(corners.get(2).toArray(), 0);

            gl.glVertex3dv(corners.get(2).toArray(), 0);
            gl.glVertex3dv(corners.get(3).toArray(), 0);

            gl.glVertex3dv(corners.get(3).toArray(), 0);
            gl.glVertex3dv(corners.get(0).toArray(), 0);

            // Right face
            gl.glVertex3dv(corners.get(4).toArray(), 0);
            gl.glVertex3dv(corners.get(5).toArray(), 0);

            gl.glVertex3dv(corners.get(5).toArray(), 0);
            gl.glVertex3dv(corners.get(6).toArray(), 0);

            gl.glVertex3dv(corners.get(6).toArray(), 0);
            gl.glVertex3dv(corners.get(7).toArray(), 0);

            gl.glVertex3dv(corners.get(7).toArray(), 0);
            gl.glVertex3dv(corners.get(4).toArray(), 0);

            // Top lines
            gl.glVertex3dv(corners.get(1).toArray(), 0);
            gl.glVertex3dv(corners.get(5).toArray(), 0);

            gl.glVertex3dv(corners.get(2).toArray(), 0);
            gl.glVertex3dv(corners.get(6).toArray(), 0);

            // Bottom lines
            gl.glVertex3dv(corners.get(0).toArray(), 0);
            gl.glVertex3dv(corners.get(4).toArray(), 0);

            gl.glVertex3dv(corners.get(3).toArray(), 0);
            gl.glVertex3dv(corners.get(7).toArray(), 0);
        }

        gl.glEnd();
        gl.glLineWidth(1);
    }
}
