package interactive;

import hierarchy.BVHBuilder;
import hierarchy.BVHFileReader;
import hierarchy.DataLoader;
import hierarchy.Hierarchy;
import com.jogamp.opengl.util.FPSAnimator;

import javax.media.opengl.awt.GLCanvas;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;

public class ViewingClient {

    private static FPSAnimator animator;
    private static Frame frame;

    public static void main(String[] args) {
        setupLogging();

        if (args.length != 1) {
            System.out.println("Please supply the path to the hierarchy information file.");
            exit();
        }

        BVHFileReader fileReader = null;
        Hierarchy hierarchy = null;

        try {
            fileReader = new BVHFileReader(args[0]);
            hierarchy = BVHBuilder.fromString(fileReader.readHierarchyHeader());
            DataLoader.setFileReader(fileReader);
        } catch (IOException e) {
            System.err.println("Failed to parse BVH file");
            exit();
        }

        InputReader inputReader = new InputReader();
        RenderingCanvas renderingCanvas = new RenderingCanvas(hierarchy);

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

    private static void setupLogging() {
        // Set all logging.
        Logger root = Logger.getLogger("");
        root.setLevel(Level.INFO);
        for (Handler handler : root.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                handler.setLevel(Level.INFO);
            }
        }
    }
}
