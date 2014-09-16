package utils;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Frustum culling code adapted from http://www.opengl.org/archives/resources/faq/technical/clipping.htm
 */
public class FrustumCuller {

    private static double[] modelViewMatrix;
    private static double[] projectionMatrix;
    private static double[][] planeEqs;

    public static void setFrustumMatrices(double[] modelView, double[] projection) {
        modelViewMatrix = modelView;
        projectionMatrix = projection;
        calculateViewFrustumPlanes();
    }

    private static double[] matrixConcatenate (final double[] ma, final double[] mb)
    {
        if (ma.length != 16 || mb.length != 16) {
            throw new IllegalArgumentException("Matrices must be of size 4x4.");
        }

        double[] result = new double[16];

        double mb00, mb01, mb02, mb03,
                mb10, mb11, mb12, mb13,
                mb20, mb21, mb22, mb23,
                mb30, mb31, mb32, mb33;
        double mai0, mai1, mai2, mai3;

        mb00 = mb[0];  mb01 = mb[1];
        mb02 = mb[2];  mb03 = mb[3];
        mb10 = mb[4];  mb11 = mb[5];
        mb12 = mb[6];  mb13 = mb[7];
        mb20 = mb[8];  mb21 = mb[9];
        mb22 = mb[10];  mb23 = mb[11];
        mb30 = mb[12];  mb31 = mb[13];
        mb32 = mb[14];  mb33 = mb[15];

        for (int i = 0; i < 4; i++) {
            mai0 = ma[i*4+0];  mai1 = ma[i*4+1];
            mai2 = ma[i*4+2];  mai3 = ma[i*4+3];

            result[i*4+0] = mai0 * mb00 + mai1 * mb10 + mai2 * mb20 + mai3 * mb30;
            result[i*4+1] = mai0 * mb01 + mai1 * mb11 + mai2 * mb21 + mai3 * mb31;
            result[i*4+2] = mai0 * mb02 + mai1 * mb12 + mai2 * mb22 + mai3 * mb32;
            result[i*4+3] = mai0 * mb03 + mai1 * mb13 + mai2 * mb23 + mai3 * mb33;
        }

        return result;
    }

    private static double distanceFromPlane(double[] plane, Vector3D point) {
        return plane[0] * point.getX() + plane[1] * point.getY() + plane[2] * point.getZ() + plane[3];
    }

    private static void calculateViewFrustumPlanes ()
    {

        planeEqs = new double[6][4];
        double[] modelViewProjectionMatrix = matrixConcatenate (modelViewMatrix, projectionMatrix);

        /* Calculate the six OC plane equations. */
        planeEqs[0][0] = modelViewProjectionMatrix[3] - modelViewProjectionMatrix[0];
        planeEqs[0][1] = modelViewProjectionMatrix[7] - modelViewProjectionMatrix[4];
        planeEqs[0][2] = modelViewProjectionMatrix[11] - modelViewProjectionMatrix[8];
        planeEqs[0][3] = modelViewProjectionMatrix[15] - modelViewProjectionMatrix[12];

        planeEqs[1][0] = modelViewProjectionMatrix[3] + modelViewProjectionMatrix[0];
        planeEqs[1][1] = modelViewProjectionMatrix[7] + modelViewProjectionMatrix[4];
        planeEqs[1][2] = modelViewProjectionMatrix[11] + modelViewProjectionMatrix[8];
        planeEqs[1][3] = modelViewProjectionMatrix[15] + modelViewProjectionMatrix[12];

        planeEqs[2][0] = modelViewProjectionMatrix[3] + modelViewProjectionMatrix[1];
        planeEqs[2][1] = modelViewProjectionMatrix[7] + modelViewProjectionMatrix[5];
        planeEqs[2][2] = modelViewProjectionMatrix[11] + modelViewProjectionMatrix[9];
        planeEqs[2][3] = modelViewProjectionMatrix[15] + modelViewProjectionMatrix[13];

        planeEqs[3][0] = modelViewProjectionMatrix[3] - modelViewProjectionMatrix[1];
        planeEqs[3][1] = modelViewProjectionMatrix[7] - modelViewProjectionMatrix[5];
        planeEqs[3][2] = modelViewProjectionMatrix[11] - modelViewProjectionMatrix[9];
        planeEqs[3][3] = modelViewProjectionMatrix[15] - modelViewProjectionMatrix[13];

        planeEqs[4][0] = modelViewProjectionMatrix[3] + modelViewProjectionMatrix[2];
        planeEqs[4][1] = modelViewProjectionMatrix[7] + modelViewProjectionMatrix[6];
        planeEqs[4][2] = modelViewProjectionMatrix[11] + modelViewProjectionMatrix[10];
        planeEqs[4][3] = modelViewProjectionMatrix[15] + modelViewProjectionMatrix[14];

        planeEqs[5][0] = modelViewProjectionMatrix[3] - modelViewProjectionMatrix[2];
        planeEqs[5][1] = modelViewProjectionMatrix[7] - modelViewProjectionMatrix[6];
        planeEqs[5][2] = modelViewProjectionMatrix[11] - modelViewProjectionMatrix[10];
        planeEqs[5][3] = modelViewProjectionMatrix[15] - modelViewProjectionMatrix[14];
    }

    /* Test a bounding box against the six clip planes */
    public static boolean isBoxVisible (Vector3D[] corners)
    {
        if (planeEqs.length == 0) {
            throw new RuntimeException("Must set plane matrices before calculating bounding box visibility.");
        }

        for (int i = 0; i<6; i++) {
            int culled = 0;
            for (int j = 0; j < 8; j++) {
                if (distanceFromPlane(planeEqs[i], corners[j]) < 0) {
                    culled |= 1 << j;
                }
            }
            if (culled == 0xff) {
                // All eight vertices of bounding box are trivially culled
                return false;
            }
        }
        // Not trivially culled. Probably visible.
        return true;
    }
}
