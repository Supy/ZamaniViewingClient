package interactive;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class InputReader implements KeyListener, MouseMotionListener {

    // Stores the list of currently pressed keys.
    private static final HashMap<Integer, Boolean> keysDown = new HashMap<>();

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
    }

    private static boolean isKeyDown(final int e) {
        Boolean down = keysDown.get(e);
        return down == null ? false : down;
    }

    @Override
    public void keyTyped(final KeyEvent e) {

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
        Camera.move2D(mouseCenterX - e.getXOnScreen(), mouseCenterY - e.getYOnScreen());

        // Move mouse back to center of window.
        try {
            Robot robot = new Robot();
            robot.mouseMove(mouseCenterX, mouseCenterY);
        } catch (Exception ignored) { }
    }
}
