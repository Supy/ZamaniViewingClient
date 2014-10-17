package interactive;

import com.jogamp.opengl.util.FPSAnimator;
import hierarchy.BVHBuilder;
import hierarchy.BVHFileReader;
import data.DataStore;
import hierarchy.Hierarchy;
import utils.Stopwatch;

import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;
import javax.swing.border.BevelBorder;
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

    private JButton openFileButton;
    private JTextField inputFileDisplay;

    public static JLabel facesRenderedLabel;
    public static JLabel activeNodesLabel;
    public static JLabel visibleNodesLabel;

    public ViewingClient() {
        this.setTitle("Zamani Viewing Client");
        this.setLayout(new BorderLayout(1, 1));
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ViewingClient.exit();
            }
        });
        this.setVisible(true);
        this.requestFocus();
        this.setSize(1000, 800);
        setFancyLookAndFeel();

        JPanel interfacePanel = new JPanel();
        interfacePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 1;
        c.gridx = 0;
        c.weightx = 0.75;
        inputFileDisplay = new JTextField("No input file selected");
        inputFileDisplay.setEditable(false);
        interfacePanel.add(inputFileDisplay, c);

        c.gridy = 1;
        c.gridx = 1;
        c.weightx = 0.25;
        openFileButton = new JButton("Open file");
        openFileButton.addActionListener(new OpenFileListener());
        interfacePanel.add(openFileButton, c);

        this.add(interfacePanel, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));

        this.add(statusPanel, BorderLayout.SOUTH);

        facesRenderedLabel = new JLabel();
        statusPanel.add(facesRenderedLabel);
        statusPanel.add(Box.createHorizontalGlue());
        activeNodesLabel = new JLabel();
        statusPanel.add(activeNodesLabel);
        statusPanel.add(Box.createHorizontalGlue());
        visibleNodesLabel = new JLabel();
        statusPanel.add(visibleNodesLabel);

        this.validate();
    }

    protected void openFile(String filePath) {
        try {
            BVHFileReader fileReader = new BVHFileReader(filePath);
            Hierarchy hierarchy = BVHBuilder.fromString(fileReader.readHierarchyHeader());
            DataStore.setFileReader(fileReader);

            InputReader inputReader = new InputReader();
            RenderingCanvas renderingCanvas = new RenderingCanvas(hierarchy);

            GLCanvas canvas = new GLCanvas();
            canvas.addGLEventListener(renderingCanvas);
            canvas.addKeyListener(inputReader);
            canvas.addMouseMotionListener(inputReader);
            canvas.addMouseWheelListener(inputReader);

            this.add(canvas, BorderLayout.CENTER);
            this.setTitle("Zamani Viewing Client - " + filePath);
            this.inputFileDisplay.setText(filePath);
            this.openFileButton.setEnabled(false);
            this.setCursor(makeInvisibleCursor());
            this.revalidate();

            // Repeatedly calls the canvas's display() method.
            animator = new FPSAnimator(canvas, 60);
            animator.start();
            animator.setUpdateFPSFrames(60, System.out);

        } catch (IOException e) {
            System.err.println("Failed to parse BVH file");
            exit();
        }
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

    private static void setFancyLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        setupLogging();
        frame = new ViewingClient();
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
    private static Cursor makeInvisibleCursor() {
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
