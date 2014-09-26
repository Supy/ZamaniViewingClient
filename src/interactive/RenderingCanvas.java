package interactive;

import data.DataStore;
import hierarchy.*;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import utils.ByteSize;
import utils.ShaderControl;
import utils.ShaderType;
import utils.Stopwatch;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;

public class RenderingCanvas implements GLEventListener {

    private Hierarchy hierarchy;
    private HierarchyRenderer hierarchyRenderer;

    private IntBuffer buffers;
    private boolean[] bufferBound;

    private ShaderControl shaderControl;

    private static int polygonFillMode = GL2.GL_FILL;
    private static int polygonType = GL2.GL_TRIANGLES;
    private static boolean drawNormals = false;

    private long lastLoadTime, lastClearTime;

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

        gl.glShadeModel(GL2.GL_FLAT);
        gl.glClearColor(0.51f, 0.72f, 0.95f, 1f);
        gl.glEnable(GL2.GL_DEPTH_TEST);                                 // Enable depth testing.
        gl.glDepthFunc(GL2.GL_LEQUAL);                                  // The type of depth test.
        gl.glClearDepth(1.0);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_FASTEST);   // Quality of perspective calculations. Can possibly lower this.
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glFrontFace(GL2.GL_CCW);
        gl.glPointSize(5);

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
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, polygonFillMode);

        InputReader.processInput();
        Camera.update(gl);
        setLightBehindCamera(gl);

        this.hierarchy.updateNodeVisibility();

        if (System.currentTimeMillis() - lastLoadTime >= 50) {
            DataStore.loadAllNodeData(this.hierarchy.getExtendedNodeSet(this.hierarchy.getActiveNodes(), true, true));
            lastLoadTime = System.currentTimeMillis();
        }

//        drawAxes(gl);

        //this.hierarchyRenderer.draw(glAutoDrawable);

        for (Node node : this.hierarchy.getVisibleNodes()) {

            if (!this.hierarchy.canBeRendered(node)) {
                node = node.getParent();
            }

            NodeDataBlock dataBlock = DataStore.getNodeData(node);

            if (dataBlock != null) {

                if (!bufferBound[node.getId()]) {
                    Stopwatch.start("total time binding buffer data");
                    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, buffers.get(node.getId() * 2));
                    gl.glBufferData(GL2.GL_ARRAY_BUFFER, dataBlock.getVertexDataBuffer().capacity(), dataBlock.getVertexDataBuffer(), GL2.GL_STATIC_DRAW);
                    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);

                    gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, buffers.get(node.getId() * 2 + 1));
                    gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, dataBlock.getIndexBuffer().capacity() * ByteSize.INT, dataBlock.getIndexBuffer(), GL2.GL_STATIC_DRAW);
                    gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);

                    Stopwatch.stop("total time binding buffer data");
                    bufferBound[node.getId()] = true;
                }

                gl.glEnable(GL2.GL_LIGHTING);
                //shaderControl.useShader();

                gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, buffers.get(node.getId() * 2));
                if (Hierarchy.hasColour) {
                    gl.glVertexPointer(3, GL2.GL_FLOAT, 28, 0);
                    gl.glNormalPointer(GL2.GL_FLOAT, 28, 12);
                    gl.glColorPointer(4, GL2.GL_UNSIGNED_BYTE, 28, 24);
                } else {
                    gl.glVertexPointer(3, GL2.GL_FLOAT, 24, 0);
                    gl.glNormalPointer(GL2.GL_FLOAT, 24, 12);
                }

                if (polygonType == GL2.GL_TRIANGLES) {
                    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
                    gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, buffers.get(node.getId() * 2 + 1));
                    gl.glDrawElements(GL2.GL_TRIANGLES, node.getNumFaces() * 3, GL2.GL_UNSIGNED_INT, 0);
                    gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
                } else {
                    gl.glDrawArrays(GL2.GL_POINTS, 0, dataBlock.getVertexDataBuffer().capacity());
                    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
                }

                //shaderControl.dontUseShader();
                gl.glDisable(GL2.GL_LIGHTING);

//                if (drawNormals) {
//                    FloatBuffer floatBuffer = dataBlock.getVertexDataBuffer();
//                    gl.glLineWidth(1);
//                    gl.glBegin(GL2.GL_LINES);
//                    gl.glColor3f(0.2f, 0.7f, 0);
//                    for (int i = 0; i < dataBlock.getVertexDataBuffer().capacity(); i += 6) {
//                        gl.glVertex3f(floatBuffer.get(i), floatBuffer.get(i + 1), floatBuffer.get(i + 2));
//                        gl.glVertex3f(floatBuffer.get(i) + floatBuffer.get(i + 3), floatBuffer.get(i + 1) + floatBuffer.get(i + 4), floatBuffer.get(i + 2) + floatBuffer.get(i + 5));
//                    }
//                    gl.glEnd();
//                }
            }
        }

        if (System.currentTimeMillis() - lastClearTime >= 500) {
            List<Map.Entry<Node, NodeDataBlock>> clearedNodes = DataStore.clearInactiveNodeDataBlocks(this.hierarchy.getExtendedNodeSet(this.hierarchy.getActiveNodes(), true, true));
            for (Map.Entry<Node, NodeDataBlock> entry : clearedNodes) {
                Node node = entry.getKey();
                NodeDataBlock dataBlock = entry.getValue();

                gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, buffers.get(node.getId() * 2));
                gl.glBufferData(GL2.GL_ARRAY_BUFFER, 0, null, GL2.GL_STATIC_DRAW);
                gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);

                gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, buffers.get(node.getId() * 2 + 1));
                gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, 0, null, GL2.GL_STATIC_DRAW);
                gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);

                bufferBound[node.getId()] = false;
            }

            if (clearedNodes.size() > 0) {
                System.out.println("cleared " + clearedNodes.size() + " node data blocks");
            }
            lastClearTime = System.currentTimeMillis();
        }

    }

    private void setLightBehindCamera(GL2 gl) {
        Vector3D position = Camera.getPosition();
        float[] positionF = new float[] {(float) position.getX(), (float) position.getY(), (float) position.getZ()};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, positionF, 0);
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
        Camera.setPosition(new Vector3D(1000, 1000, 1000));
        Camera.setLookAt(new Vector3D(0, 0, 0));
        Camera.setClipping(10,3000);
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

        float[] materialAmbientColor = {0.5f, 0.4f, 0.4f, 1.0f};
        float[] materialDiffuseColor = {0.83f, 0.75f, 0.61f, 1.0f};
        float[] materialSpecularColor = {0.15f, 0.15f, 0.15f, 1.0f};
        float materialShininess = 80.0f;

        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, materialAmbientColor, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, materialDiffuseColor, 0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, materialSpecularColor, 0);
        gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, materialShininess);
    }

    private void loadShaders(GL2 gl) throws Exception {
        String basePath = System.getProperty("user.dir");

        shaderControl = new ShaderControl(gl);
        shaderControl.loadShader(basePath + "\\shaders\\flat_vertex_shader.glsl", ShaderType.VERTEX_FLAT);
        shaderControl.loadShader(basePath + "\\shaders\\fragment_shader.glsl", ShaderType.FRAGMENT);
        shaderControl.loadShader(basePath + "\\shaders\\vertex_shader.glsl", ShaderType.VERTEX);
        shaderControl.attachShaders();
    }

    public static void toggleFillMode() {
        polygonFillMode = (polygonFillMode == GL2.GL_FILL) ? GL2.GL_LINE : GL2.GL_FILL;
    }

    public static void togglePolygonMode() {
        polygonType = (polygonType  == GL2.GL_TRIANGLES) ? GL2.GL_POINTS : GL2.GL_TRIANGLES;
    }

    public static void toggleNormals() {
        drawNormals = !drawNormals;
    }
}
