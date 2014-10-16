package interactive;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

/**
 * Camera code adapted from http://hamelot.co.uk/visualization/moderngl-camera/
 */

enum CameraDirection {
    UP, DOWN, LEFT, RIGHT, FORWARD, BACK
}

public class Camera {

    private static int viewportX;
    private static int viewportY;

    private static int windowWidth;
    private static int windowHeight;

    private static double aspect;
    private static double fieldOfView;
    private static double nearClip;
    private static double farClip;

    private static float cameraScale;
    private static float cameraHeading;
    private static float cameraPitch;

    private static float maxPitchRate;
    private static float maxHeadingRate;

    private static Vector3D cameraPosition;
    private static Vector3D cameraPositionDelta;
    private static Vector3D cameraLookAt;
    private static Vector3D cameraDirection;

    private static Vector3D cameraUp;

    private static boolean staticCamera = false;

    private final static GLU glu = new GLU();

    private final static double[] projectionMatrix = new double[16];
    private final static double[] modelViewMatrix = new double[16];
    private final static int[] viewportBoundaries = new int[4];

    // Setup defaults
    static {
        cameraUp = new Vector3D(0, 1, 0);
        fieldOfView = 45;
        cameraPositionDelta = new Vector3D(0, 0, 0);
        cameraScale = 4f;
        maxPitchRate = 5;
        maxHeadingRate = 5;
    }

    public static void update(GL2 gl) {

        cameraDirection = cameraLookAt.subtract(cameraPosition).normalize();

        // Don't allow looking straight up or down.
        if (cameraDirection.equals(Vector3D.PLUS_J) || cameraDirection.equals(Vector3D.MINUS_J)) {
            cameraPosition.add(new Vector3D(1, 0, 0));
            cameraDirection = cameraLookAt.subtract(cameraPosition).normalize();
        }

        // Calculate camera pitch rotation
        Vector3D pitchAxis = cameraDirection.crossProduct(cameraUp);
        Rotation pitchRotation = new Rotation(pitchAxis, cameraPitch);

        // Calculate camera heading rotation
        Rotation headingRotation = new Rotation(cameraUp, cameraHeading);

        // Combine the rotations
        Rotation combinedRotation = pitchRotation.applyTo(headingRotation);

        // Update camera direction
        cameraDirection = combinedRotation.applyTo(cameraDirection);

        // Don't move into a completely vertical view direction.
        cameraPosition = cameraPosition.add(cameraPositionDelta);
        cameraLookAt = cameraPosition.add(cameraDirection);

        cameraHeading = 0;
        cameraPitch = 0;
        cameraPositionDelta = Vector3D.ZERO;

        // Set new camera look at matrix
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(
            cameraPosition.getX(),
            cameraPosition.getY(),
            cameraPosition.getZ(),
            cameraLookAt.getX(),
            cameraLookAt.getY(),
            cameraLookAt.getZ(),
            cameraUp.getX(),
            cameraUp.getY(),
            cameraUp.getZ()
        );

        gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, modelViewMatrix, 0);
    }

    public static void setPosition(Vector3D pos) {
        cameraPosition = pos;
    }

    public static Vector3D getPosition() { return cameraPosition; }

    public static Vector3D getLookAt() {
        return cameraLookAt;
    }

    public static void setLookAt(Vector3D pos) {
        cameraLookAt = pos;
    }

    public static void setFOV(double fov) {
        fieldOfView = fov;
}

    public static void setViewport(int locX, int locY, int width, int height) {
        viewportX = locX;
        viewportY = locY;
        windowWidth = width;
        windowHeight = height;
        aspect = (double) width / (double) height;
    }
    public static void setClipping(double nearClipDistance, double farClipDistance) {
        nearClip = nearClipDistance;
        farClip = farClipDistance;
    }

    public static void move(CameraDirection dir) {
        if (!staticCamera) {
            switch (dir) {
                case UP:
                    cameraPositionDelta = cameraPositionDelta.add(cameraUp.scalarMultiply(cameraScale));
                    break;
                case DOWN:
                    cameraPositionDelta = cameraPositionDelta.subtract(cameraUp.scalarMultiply(cameraScale));
                    break;
                case LEFT:
                    cameraPositionDelta = cameraPositionDelta.subtract(cameraDirection.crossProduct(cameraUp).scalarMultiply(cameraScale));
                    break;
                case RIGHT:
                    cameraPositionDelta = cameraPositionDelta.add(cameraDirection.crossProduct(cameraUp).scalarMultiply(cameraScale));
                    break;
                case FORWARD:
                    cameraPositionDelta = cameraPositionDelta.add(cameraDirection.scalarMultiply(cameraScale));
                    break;
                case BACK:
                    cameraPositionDelta = cameraPositionDelta.subtract(cameraDirection.scalarMultiply(cameraScale));
                    break;
            }
        }
    }

    public static void changePitch(float degrees) {
        //Check bounds with the max pitch rate so that we aren't moving too fast
        if (degrees < -maxPitchRate) {
            degrees = -maxPitchRate;
        } else if (degrees > maxPitchRate) {
            degrees = maxPitchRate;
        }
        cameraPitch += degrees;

        //Check bounds for the camera pitch
        if (cameraPitch > 360.0f) {
            cameraPitch -= 360.0f;
        } else if (cameraPitch < -360.0f) {
            cameraPitch += 360.0f;
        }
    }

    public static void changeHeading(float degrees) {
        //Check bounds with the max heading rate so that we aren't moving too fast
        if (degrees < -maxHeadingRate) {
            degrees = -maxHeadingRate;
        } else if (degrees > maxHeadingRate) {
            degrees = maxHeadingRate;
        }

        // This controls how the heading is changed if the camera is pointed straight up or down
        // the heading delta direction changes
        if (cameraPitch > 90 && cameraPitch < 270 || (cameraPitch < -90 && cameraPitch > -270)) {
            cameraHeading -= degrees;
        } else {
            cameraHeading += degrees;
        }

        //Check bounds for the camera heading
        if (cameraHeading > 360.0f) {
            cameraHeading -= 360.0f;
        } else if (cameraHeading < -360.0f) {
            cameraHeading += 360.0f;
        }
    }

    public static void move2D(int deltaX, int deltaY) {
        if (!staticCamera) {
            changeHeading(0.0005f * deltaX);
            changePitch(0.0005f * deltaY);
        }
    }

    public static void calculateProjectionMatrix(GL2 gl) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glViewport(viewportX, viewportY, windowWidth, windowHeight);
        glu.gluPerspective(fieldOfView, aspect, nearClip, farClip);

        gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projectionMatrix, 0);
        gl.glGetIntegerv(GL.GL_VIEWPORT, viewportBoundaries, 0);
    }

    public static double[] getProjectionMatrix() {
        return projectionMatrix;
    }

    public static double[] getModelViewMatrix() {
        return modelViewMatrix;
    }

    public static int[] getViewportBoundaries() {
        return viewportBoundaries;
    }

    public static double getProjectedScreenSize(Vector3D[] corners) {
        double  minX = Double.MAX_VALUE, maxX = 0,
                minY = Double.MAX_VALUE, maxY = 0;

        for(Vector3D corner : corners) {
            double[] screenCoordinates = new double[4];
            boolean success = glu.gluProject(corner.getX(), corner.getY(), corner.getZ(), modelViewMatrix, 0, projectionMatrix, 0, viewportBoundaries, 0, screenCoordinates, 0);

            // Don't count corners which fail the projection
            if (success) {
                minX = Math.min(minX, screenCoordinates[0]);
                maxX = Math.max(maxX, screenCoordinates[0]);
                minY = Math.min(minY, screenCoordinates[1]);
                maxY = Math.max(maxY, screenCoordinates[1]);
            }
        }

        return Math.abs((maxX - minX)) * Math.abs((maxY - minY)) / (windowWidth * windowHeight) * 100;
    }

    public static void setStaticCamera(boolean isStatic) {
        staticCamera = isStatic;
    }

    public static void toggleStaticCamera() {
        staticCamera = !staticCamera;
    }
}
