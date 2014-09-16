package interactive;

import hierarchy.*;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import utils.ByteSize;
import utils.ShaderControl;
import utils.ShaderType;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import java.nio.IntBuffer;
import java.util.LinkedList;

public class RenderingCanvas implements GLEventListener {

    private Hierarchy hierarchy;
    private HierarchyRenderer hierarchyRenderer;

    private IntBuffer buffers;
    private boolean[] bufferBound;

    private ShaderControl shaderControl;

    public RenderingCanvas(Hierarchy hierarchy) {
        if (hierarchy == null) {
            throw new IllegalArgumentException("Must be supplied with non-null hierarchy.");
        }

        this.hierarchy = hierarchy;
        this.hierarchyRenderer = new HierarchyRenderer(hierarchy);

        this.buffers = IntBuffer.allocate(this.hierarchy.getNumNodes() * 2);
        bufferBound = new boolean[hierarchy.getNumNodes()];
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = (GL2) glAutoDrawable.getGL();
        setupCamera();

        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glClearColor(0.51f, 0.72f, 0.95f, 1f);
        gl.glEnable(GL2.GL_DEPTH_TEST);                                 // Enable depth testing.
        gl.glDepthFunc(GL2.GL_LEQUAL);                                  // The type of depth test.
        gl.glClearDepth(1.0);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);   // Quality of perspective calculations. Can possibly lower this.
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glFrontFace(GL2.GL_CCW);

        gl.glGenBuffers(this.buffers.capacity(), this.buffers);

        setupLighting(gl);
        try {
            loadShaders(gl);
        } catch (Exception e) {
            System.err.println("Failed to load shaders.");
        }
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

        this.hierarchy.updateNodeVisibility();

        DataLoader.loadAllNodeData(new LinkedList<>(this.hierarchy.getActiveNodes()));

        drawAxes(gl);

        this.hierarchyRenderer.draw(glAutoDrawable);

        gl.glEnable(GL2.GL_LIGHTING);
        shaderControl.useShader();
        for (Node node : this.hierarchy.getVisibleNodes()) {
            NodeDataBlock dataBlock = DataLoader.getNodeData(node);
            if (dataBlock != null) {
                if (!bufferBound[node.getId()]) {
                    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, buffers.get(node.getId() * 2));
                    gl.glBufferData(GL2.GL_ARRAY_BUFFER, dataBlock.getVertexDataBuffer().capacity() * ByteSize.FLOAT, dataBlock.getVertexDataBuffer(), GL2.GL_STATIC_DRAW);
                    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);

                    gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, buffers.get(node.getId() * 2 + 1));
                    gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, dataBlock.getIndexBuffer().capacity() * ByteSize.INT, dataBlock.getIndexBuffer(), GL2.GL_STATIC_DRAW);
                    gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);

                    bufferBound[node.getId()] = true;
                }

                gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, buffers.get(node.getId() * 2));
                gl.glVertexPointer(3, GL2.GL_FLOAT, 24, 0);
                gl.glNormalPointer(GL2.GL_FLOAT, 24, 12);
                gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, buffers.get(node.getId() * 2 + 1));
                gl.glDrawElements(GL2.GL_TRIANGLES, dataBlock.getNumIndices(), GL2.GL_UNSIGNED_INT, 0);
            }
        }
        shaderControl.dontUseShader();
        gl.glDisable(GL2.GL_LIGHTING);
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
        Camera.setPosition(new Vector3D(1500, 1500, 1500));
        Camera.setLookAt(new Vector3D(0, 0, 0));
        Camera.setClipping(10, 10000);
        Camera.setFOV(45);
    }

    private void setupLighting(GL2 gl) {
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);

        float[] worldAmbience = {0.7f, 0.7f, 0.7f, 1f};
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, worldAmbience, 0);

        float[] lightPosition = {10000, 10000, 10000, 1};
        float[] diffuseColor = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] specularColor = {1.0f, 1.0f, 1.0f, 1.0f};

        gl.glEnable(GL2.GL_LIGHT0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuseColor, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, specularColor, 0);

        float[] materialAmbientColor = {0.2f, 0.2f, 0.2f, 1.0f};
        float[] materialDiffuseColor = {0.83f, 0.75f, 0.61f, 1.0f};
        float[] materialSpecularColor = {0.15f, 0.15f, 0.15f, 1.0f};
        float materialShininess = 80.0f;

        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, materialAmbientColor, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, materialDiffuseColor, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, materialSpecularColor, 0);
        gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, materialShininess);
    }

    private void loadShaders(GL2 gl) throws Exception {
        shaderControl = new ShaderControl(gl);
        shaderControl.loadShader("E:\\My Documents\\Workspace\\Zamani Viewing Client\\src\\shaders\\flat_vertex_shader.glsl", ShaderType.VERTEX_FLAT);
        shaderControl.loadShader("E:\\My Documents\\Workspace\\Zamani Viewing Client\\src\\shaders\\fragment_shader.glsl", ShaderType.FRAGMENT);
        shaderControl.loadShader("E:\\My Documents\\Workspace\\Zamani Viewing Client\\src\\shaders\\vertex_shader.glsl", ShaderType.VERTEX);
        shaderControl.attachShaders();
    }
}
