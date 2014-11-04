package interactive;

import data.DataStore;
import hierarchy.*;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import utils.*;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;

public class RenderingCanvas implements GLEventListener {

    private Hierarchy hierarchy;
    private HierarchyRenderer hierarchyRenderer;

    private IntBuffer buffers;
    private static boolean[] bufferBound;

    private long lastLoadTime, lastClearTime;

    private double totalDataBuffered = 0;

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
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl = (GL2) glAutoDrawable.getGL();

        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, FeatureToggle.getPolygonFillMode());
        gl.glShadeModel(FeatureToggle.getShaderType());
        gl.glFrontFace(FeatureToggle.getFrontFace());
        if (FeatureToggle.shouldUseLighting()) {
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
        } else {
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        }

        InputReader.processInput();
        Camera.update(gl);
        setLightBehindCamera(gl);

        if (System.currentTimeMillis() - lastLoadTime >= 50) {
            this.hierarchy.updateNodeVisibility();
            DataStore.loadAllNodeData(this.hierarchy.getExtendedNodeSet(this.hierarchy.getActiveNodes(), true, true));
            lastLoadTime = System.currentTimeMillis();
        }

        if (FeatureToggle.shouldDrawBoundingVolumes()) {
            this.hierarchyRenderer.draw(glAutoDrawable);
        }

        gl.glColor3f(0.5f, 0.5f, 0.5f);

        int facesRendered = 0;
        int activeNodes = this.hierarchy.getActiveNodes().size();
        int visibleNodes = this.hierarchy.getVisibleNodes().size();
        int[] hlodRendered = new int[Hierarchy.maxDepth];

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
                    totalDataBuffered += dataBlock.getVertexDataBuffer().capacity() + (dataBlock.getIndexBuffer().capacity() * 4);
                }

                gl.glEnable(GL2.GL_LIGHTING);

                gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, buffers.get(node.getId() * 2));
                if (Hierarchy.hasColour) {
                    gl.glVertexPointer(3, GL2.GL_FLOAT, 27, 0);
                    gl.glNormalPointer(GL2.GL_FLOAT, 27, 12);
                    gl.glColorPointer(3, GL2.GL_UNSIGNED_BYTE, 27, 24);
                } else {
                    gl.glVertexPointer(3, GL2.GL_FLOAT, 24, 0);
                    gl.glNormalPointer(GL2.GL_FLOAT, 24, 12);
                }

                if (FeatureToggle.getPolygonMode() == GL2.GL_TRIANGLES) {
                    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
                    gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, buffers.get(node.getId() * 2 + 1));
                    gl.glDrawElements(GL2.GL_TRIANGLES, node.getNumFaces() * 3, GL2.GL_UNSIGNED_INT, 0);
                    gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
                    facesRendered += node.getNumFaces();
                } else {
                    gl.glDrawArrays(GL2.GL_POINTS, 0, dataBlock.getVertexDataBuffer().capacity());
                    gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
                }

                gl.glDisable(GL2.GL_LIGHTING);

                if (FeatureToggle.shouldDrawNormals()) {
                    ByteBuffer buffer = dataBlock.getVertexDataBuffer();
                    gl.glLineWidth(1);
                    gl.glBegin(GL2.GL_LINES);
                    gl.glColor3f(0.2f, 0.7f, 0);

                    final int stride = (Hierarchy.hasColour) ? 27 : 24;
                    for (int i = 0; i < dataBlock.getVertexDataBuffer().capacity(); i += stride) {
                        gl.glVertex3f(
                                buffer.getFloat(i),
                                buffer.getFloat(i + 4),
                                buffer.getFloat(i + 8)
                        );

                        gl.glVertex3f(
                                buffer.getFloat(i) + buffer.getFloat(i + 12),
                                buffer.getFloat(i + 4) + buffer.getFloat(i + 16),
                                buffer.getFloat(i + 8) + buffer.getFloat(i + 20)
                        );
                    }
                    gl.glEnd();
                }

                hlodRendered[node.getDepth()]++;
            }
        }

        if (System.currentTimeMillis() - lastClearTime >= 500) {
            List<Map.Entry<Node, NodeDataBlock>> clearedNodes = DataStore.clearInactiveNodeDataBlocks(this.hierarchy.getExtendedNodeSet(this.hierarchy.getActiveNodes(), true, true));
            for (Map.Entry<Node, NodeDataBlock> entry : clearedNodes) {
                Node node = entry.getKey();

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

        ViewingClient.activeNodesLabel.setText("Active nodes: " + activeNodes);
        ViewingClient.visibleNodesLabel.setText("Visible nodes: " + visibleNodes);
        ViewingClient.facesRenderedLabel.setText(String.format("Faces: %,d", facesRendered).replace(",", " "));

        if (FeatureToggle.shouldRecord()) {
            // Get current size of heap in bytes
            long heapSize = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            StatsRecorder.record("activeNodes", activeNodes);
            StatsRecorder.record("visibleNodes", visibleNodes);
            StatsRecorder.record("heapUsage", heapSize);
            StatsRecorder.record("polygonsVisible", facesRendered);
            StatsRecorder.record("totalDataBuffered", totalDataBuffered);

            for (int i = 0; i < hlodRendered.length; i++) {
                StatsRecorder.record("hlod-" + (i + 1), hlodRendered[i]);
            }
        }
    }

    private void setLightBehindCamera(GL2 gl) {
        Vector3D position = Camera.getPosition().subtract(Camera.getLookAt());
        float[] positionF = new float[]{(float) position.getX(), (float) position.getY() + 3, (float) position.getZ()};
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

    private void setupCamera() {
        Camera.setStaticCamera(true);
        Camera.setPosition(new Vector3D(227, 12.5, 410));
        Camera.setLookAt(new Vector3D(0, 14.5, 0));
        Camera.setClipping(10,3000);
        Camera.setFOV(45);
    }

    private void setupLighting(GL2 gl) {
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);

        float[] worldAmbience = {0.33f, 0.33f, 0.33f, 1f};
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, worldAmbience, 0);

        float[] diffuseColor = {0.7f, 0.7f, 0.7f, 1.0f};
        float[] specularColor = {0.9f, 0.9f, 0.9f, 1.0f};

        gl.glEnable(GL2.GL_LIGHT0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuseColor, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, specularColor, 0);

        float materialShininess = 80.0f;

        gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, materialShininess);
    }

    public static void rebindBuffers() {
        bufferBound = new boolean[bufferBound.length];
    }
}
