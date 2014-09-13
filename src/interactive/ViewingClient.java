package interactive;

import bvh.BVHBuilder;
import bvh.Hierarchy;
import com.jogamp.opengl.util.FPSAnimator;

import javax.media.opengl.awt.GLCanvas;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ViewingClient {

    private static FPSAnimator animator;
    private static Frame frame;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Please supply the path to the hierarchy information file.");
            exit();
        }

        try {
            Hierarchy hierarchy = BVHBuilder.fromFile("E:\\My Documents\\Workspace\\Zamani Viewing Client\\src\\example-hierarchy.json");
        } catch (IOException e) {
            System.err.println("Failed to parse BVH file");
            exit();
        }

        InputReader inputReader = new InputReader();
        RenderingCanvas renderingCanvas = new RenderingCanvas();

        GLCanvas canvas = new GLCanvas();
        canvas.addGLEventListener(renderingCanvas);
        canvas.addKeyListener(inputReader);
        canvas.addMouseMotionListener(inputReader);

        frame = new Frame("Zamani Renderer");
        frame.add(canvas);
        frame.setSize(1200, 880);
        frame.setUndecorated(false);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ViewingClient.exit();
            }
        });
        frame.setVisible(true);
        frame.requestFocus();
        frame.setCursor(invisibleCursor());

        // Repeatedly calls the canvas's display() method.
        animator = new FPSAnimator(canvas, 60);
        animator.start();
        animator.setUpdateFPSFrames(100, System.out);
    }

    public static void exit() {
        if (animator != null) {
            animator.stop();
        }

        if (frame != null) {
            frame.dispose();
        }

        System.exit(0);
    }

    /*
     * Java hacks for hiding a cursor - making a transparent one.
     */
    private static Cursor invisibleCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(0, 0);
        BufferedImage cursorImage = new BufferedImage(1, 1, BufferedImage.TRANSLUCENT);
        return toolkit.createCustomCursor(cursorImage, hotSpot, "InvisibleCursor");
    }
}
