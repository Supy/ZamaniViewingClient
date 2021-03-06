package interactive;

import data.DataStore;
import utils.FeatureToggle;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class InputReader implements KeyListener, MouseMotionListener, MouseWheelListener {

    // Stores the list of currently pressed keys.
    private static final HashMap<Integer, Boolean> keysDown = new HashMap<>();

    private static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (Exception e) {
            System.err.println("System does not support low-level input for automated mouse movement.");
        }
    }

    private static int mouseCenterX;
    private static int mouseCenterY;

    public static void setMouseCenter(int x, int y) {
        mouseCenterX = x;
        mouseCenterY = y;
    }

    public static void processInput() {
        if (isKeyDown(KeyEvent.VK_W)) {
            Camera.move(CameraDirection.FORWARD);
        }

        if (isKeyDown(KeyEvent.VK_S)) {
            Camera.move(CameraDirection.BACK);
        }

        if (isKeyDown(KeyEvent.VK_A)) {
            Camera.move(CameraDirection.LEFT);
        }

        if (isKeyDown(KeyEvent.VK_D)) {
            Camera.move(CameraDirection.RIGHT);
        }

        if (isKeyDown(KeyEvent.VK_Q)) {
            Camera.move(CameraDirection.UP);
        }

        if (isKeyDown(KeyEvent.VK_E)) {
            Camera.move(CameraDirection.DOWN);
        }

        if (isKeyDown(KeyEvent.VK_ESCAPE)) {
            ViewingClient.exit();
        }

        if (isKeyDown(KeyEvent.VK_NUMPAD8)) {
            Camera.move2D(0,22);
        }

        if (isKeyDown(KeyEvent.VK_NUMPAD2)) {
            Camera.move2D(0,-22);
        }

        if (isKeyDown(KeyEvent.VK_NUMPAD4)) {
            Camera.move2D(-22, 0);
        }

        if (isKeyDown(KeyEvent.VK_NUMPAD6)) {
            Camera.move2D(22, 0);
        }
    }

    private static boolean isKeyDown(final int e) {
        Boolean down = keysDown.get(e);
        return down == null ? false : down;
    }

    @Override
    public void keyTyped(final KeyEvent e) {
        if (e.getKeyChar() == 'v') {
            FeatureToggle.toggleShaderType();
        }

        if (e.getKeyChar() == 'f') {
            FeatureToggle.togglePolygonFillMode();
        }

        if (e.getKeyChar() == 'p') {
            FeatureToggle.togglePolygonMode();
        }

        if (e.getKeyChar() == 'n') {
            FeatureToggle.toggleDrawNormals();
        }

        if (e.getKeyChar() == 'b') {
            FeatureToggle.toggleDrawBoundingVolumes();
        }

        if (e.getKeyChar() == 'c') {
            FeatureToggle.toggleFrontFace();
            DataStore.invalidateCache();
            RenderingCanvas.rebindBuffers();
        }

        if (e.getKeyChar() == 'l') {
            FeatureToggle.toggleUseLighting();
        }

        if (e.getKeyChar() == 'm') {
            Camera.toggleStaticCamera();
        }

        if (e.getKeyChar() == 'r') {
            FeatureToggle.toggleRecord();
        }
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        keysDown.put(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        keysDown.put(e.getKeyCode(), false);
    }

    @Override
    public void mouseDragged(final MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (robot != null) {
            Camera.move2D(mouseCenterX - e.getXOnScreen(), mouseCenterY - e.getYOnScreen());

            // Move mouse back to center of window.
            robot.mouseMove(mouseCenterX, mouseCenterY);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Camera.adjustCameraScale(e.getWheelRotation() * 0.5f);
    }
}
