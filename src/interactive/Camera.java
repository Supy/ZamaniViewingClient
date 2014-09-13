package interactive;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

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

    private static GLU glu;

    // Setup defaults
    static {
        cameraUp = new Vector3D(0, 1, 0);
        fieldOfView = 45;
        cameraPositionDelta = new Vector3D(0, 0, 0);
        cameraScale = 0.8f;
        maxPitchRate = 5;
        maxHeadingRate = 5;
        glu = new GLU();
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
    }

    public static void setPosition(Vector3D pos) {
        cameraPosition = pos;
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
        changeHeading(0.0005f * deltaX);
        changePitch(0.0005f * deltaY);
    }

    public static void calculateProjectionMatrix(GL2 gl) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glViewport(viewportX, viewportY, windowWidth, windowHeight);
        glu.gluPerspective(fieldOfView, aspect, nearClip, farClip);
    }
}
