package interactive;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

public class RenderingCanvas implements GLEventListener {

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = (GL2) glAutoDrawable.getGL();
        setupCamera();

        gl.glClearColor(0.51f, 0.72f, 0.95f, 1f);
        gl.glEnable(GL2.GL_DEPTH_TEST);                                 // Enable depth testing.
        gl.glDepthFunc(GL2.GL_LEQUAL);                                  // The type of depth test.
        gl.glClearDepth(1.0);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);   // Quality of perspective calculations. Can possibly lower this.
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glFrontFace(GL2.GL_CCW);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl = (GL2) glAutoDrawable.getGL();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        InputReader.processInput();
        Camera.update(gl);

        drawAxes(gl);

        gl.glBegin(GL2.GL_QUADS);		// Draw The Cube Using quads
        gl.glColor3f(0.0f,1.0f,0.0f);	// Color Blue
        gl.glVertex3f( 1.0f, 1.0f,-1.0f);	// Top Right Of The Quad (Top)
        gl.glVertex3f(-1.0f, 1.0f,-1.0f);	// Top Left Of The Quad (Top)
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);	// Bottom Left Of The Quad (Top)
        gl.glVertex3f( 1.0f, 1.0f, 1.0f);	// Bottom Right Of The Quad (Top)
        gl.glColor3f(1.0f,0.5f,0.0f);	// Color Orange
        gl.glVertex3f( 1.0f,-1.0f, 1.0f);	// Top Right Of The Quad (Bottom)
        gl.glVertex3f(-1.0f,-1.0f, 1.0f);	// Top Left Of The Quad (Bottom)
        gl.glVertex3f(-1.0f,-1.0f,-1.0f);	// Bottom Left Of The Quad (Bottom)
        gl.glVertex3f( 1.0f,-1.0f,-1.0f);	// Bottom Right Of The Quad (Bottom)
        gl.glColor3f(1.0f,0.0f,0.0f);	// Color Red
        gl.glVertex3f( 1.0f, 1.0f, 1.0f);	// Top Right Of The Quad (Front)
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);	// Top Left Of The Quad (Front)
        gl.glVertex3f(-1.0f,-1.0f, 1.0f);	// Bottom Left Of The Quad (Front)
        gl.glVertex3f( 1.0f,-1.0f, 1.0f);	// Bottom Right Of The Quad (Front)
        gl.glColor3f(1.0f,1.0f,0.0f);	// Color Yellow
        gl.glVertex3f( 1.0f,-1.0f,-1.0f);	// Top Right Of The Quad (Back)
        gl.glVertex3f(-1.0f,-1.0f,-1.0f);	// Top Left Of The Quad (Back)
        gl.glVertex3f(-1.0f, 1.0f,-1.0f);	// Bottom Left Of The Quad (Back)
        gl.glVertex3f( 1.0f, 1.0f,-1.0f);	// Bottom Right Of The Quad (Back)
        gl.glColor3f(0.0f,0.0f,1.0f);	// Color Blue
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);	// Top Right Of The Quad (Left)
        gl.glVertex3f(-1.0f, 1.0f,-1.0f);	// Top Left Of The Quad (Left)
        gl.glVertex3f(-1.0f,-1.0f,-1.0f);	// Bottom Left Of The Quad (Left)
        gl.glVertex3f(-1.0f,-1.0f, 1.0f);	// Bottom Right Of The Quad (Left)
        gl.glColor3f(1.0f,0.0f,1.0f);	// Color Violet
        gl.glVertex3f( 1.0f, 1.0f,-1.0f);	// Top Right Of The Quad (Right)
        gl.glVertex3f( 1.0f, 1.0f, 1.0f);	// Top Left Of The Quad (Right)
        gl.glVertex3f( 1.0f,-1.0f, 1.0f);	// Bottom Left Of The Quad (Right)
        gl.glVertex3f( 1.0f,-1.0f,-1.0f);	// Bottom Right Of The Quad (Right)
        gl.glEnd();			// End Drawing The Cubepackage interactive;
    }

    @Override
    public void reshape(GLAutoDrawable glDrawable, int x, int y, int windowWidth, int windowHeight) {
        // Calculate position mouse will be reset to.
        InputReader.setMouseCenter(x + windowWidth / 2, y + windowHeight / 2);

        // Calculate new viewport perspective
        Camera.setViewport(0, 0, windowWidth, windowHeight);
        Camera.calculateProjectionMatrix((GL2) glDrawable.getGL());
    }

    private void drawAxes(GL2 gl) {
        gl.glBegin(GL2.GL_LINES);

        gl.glColor3f(1f, 0f, 0f);
        gl.glVertex3f(10000f, 0f, 0f);
        gl.glVertex3f(-10000f, 0f, 0f);

        gl.glColor3f(0, 1f, 0);
        gl.glVertex3f(0, 10000f, 0);
        gl.glVertex3f(0, -10000f, 0);

        gl.glColor3f(0, 0, 1f);
        gl.glVertex3f(0, 0, 1000f);
        gl.glVertex3f(0, 0, -10000f);

        gl.glEnd();
    }

    private void setupCamera() {
        Camera.setPosition(new Vector3D(10, 10, 10));
        Camera.setLookAt(new Vector3D(0, 0, 0));
        Camera.setClipping(1, 2000);
        Camera.setFOV(45);
    }
}
