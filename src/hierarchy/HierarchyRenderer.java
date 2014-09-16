package hierarchy;

import com.jogamp.opengl.util.awt.TextRenderer;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import utils.Stopwatch;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import java.awt.*;
import java.util.HashSet;
import java.util.List;

public class HierarchyRenderer {

    private TextRenderer textRenderer;

    private Hierarchy hierarchy;

    public HierarchyRenderer(Hierarchy hierarchy) {
        if (hierarchy == null) {
            throw new NullPointerException("Hierarchy cannot be null.");
        }

        this.hierarchy = hierarchy;

        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 18));
        // JOGL TR doesn't play nicely when you turn on normal arrays.
        textRenderer.setUseVertexArrays(false);
    }

    public void draw(GLAutoDrawable glAutoDrawable) {
        GL2 gl = (GL2) glAutoDrawable.getGL();

        HashSet<Node> visibleNodes = hierarchy.getVisibleNodes();
        HashSet<Node> activeNodes = hierarchy.getActiveNodes();

        textRenderer.beginRendering(glAutoDrawable.getWidth(), glAutoDrawable.getHeight());
        textRenderer.draw(visibleNodes.size() + " visible nodes", 40, 40);
        textRenderer.draw(activeNodes.size() + " active nodes", 40, 20);
        textRenderer.draw("Hierarchy update: " + Stopwatch.getTime("hierarchyRenderer.update") + " ms", 40, 60);
        textRenderer.endRendering();

        gl.glColor4f(1, 1, 0, 0.5f);
        gl.glLineWidth(3);
        gl.glBegin(GL2.GL_LINES);

        for(Node node : visibleNodes) {
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
    }
}
