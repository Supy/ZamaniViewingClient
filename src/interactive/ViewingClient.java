package interactive;

import com.jogamp.opengl.util.FPSAnimator;
import hierarchy.BVHBuilder;
import hierarchy.BVHFileReader;
import data.DataStore;
import hierarchy.Hierarchy;
import utils.Stopwatch;

import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewingClient extends JFrame {

    private static FPSAnimator animator;
    private static Frame frame;

    private JButton openButton = new JButton("Open file");

    public ViewingClient() {
        this.setUndecorated(false);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ViewingClient.exit();
            }
        });
        this.setVisible(true);
        this.requestFocus();
        this.setSize(1000, 800);
        this.setCursor(invisibleCursor());

        openButton.addActionListener(new OpenFileListener());
        this.add(openButton);
        this.validate();
    }

    protected void openFile(String filePath) {
        BVHFileReader fileReader = null;
        Hierarchy hierarchy = null;

        try {
            fileReader = new BVHFileReader(filePath);
            hierarchy = BVHBuilder.fromString(fileReader.readHierarchyHeader());
            DataStore.setFileReader(fileReader);
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

        this.remove(openButton);
        this.add(canvas);
        this.revalidate();

        // Repeatedly calls the canvas's display() method.
        animator = new FPSAnimator(canvas, 60);
        animator.start();
        animator.setUpdateFPSFrames(60, System.out);
    }

    class OpenFileListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser c = new JFileChooser();
            c.setAcceptAllFileFilterUsed(false);
            c.setFileFilter(new FileNameExtensionFilter("PHF Files", "phf"));

            // Demonstrate "Open" dialog:
            int rVal = c.showOpenDialog(ViewingClient.this);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                openFile(c.getSelectedFile().getAbsolutePath());
            }
        }
    }

    public static void main(String[] args) {
        setupLogging();
        new ViewingClient();
    }

    public static void exit() {
        if (animator != null) {
            animator.stop();
        }

        if (frame != null) {
            frame.dispose();
        }

        Stopwatch.printTime("total time loading node data");
        Stopwatch.printTime("total time calculating normals <thread 1>");
        Stopwatch.printTime("total time calculating normals <thread 2>");
        Stopwatch.printTime("total time calculating normals <thread 3>");
        Stopwatch.printTime("total time binding buffer data");
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
