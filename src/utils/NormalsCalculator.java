package utils;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import javax.media.opengl.GL2;
import java.nio.FloatBuffer;

public class NormalsCalculator {
    public static float[] calculateFrom(float[] positions, int[] faces) {
        if(positions.length % 3 !=0) {
            throw new RuntimeException("number of vertex positions must be a multiple of 3");
        }

        int[] contributions = new int[positions.length / 3];
        float[] normals = new float[positions.length];

        for(int i=0; i < faces.length; i+= 3) {
            int p1Index = faces[i] * 3;
            int p2Index = faces[i+1] * 3;
            int p3Index = faces[i+2] * 3;

            // Reconstruct vertices in 3D space.
            Vector3D v1 = new Vector3D(positions[p1Index], positions[p1Index + 1], positions[p1Index + 2]);
            Vector3D v2 = new Vector3D(positions[p2Index], positions[p2Index + 1], positions[p2Index + 2]);
            Vector3D v3 = new Vector3D(positions[p3Index], positions[p3Index + 1], positions[p3Index + 2]);

            // Need two edges of the triangle to do the cross product.
            Vector3D edge1 = v1.subtract(v3);
            Vector3D edge2 = v2.subtract(v3);

            Vector3D cross = (FeatureToggle.getFrontFace() == GL2.GL_CCW) ? edge1.crossProduct(edge2) : edge2.crossProduct(edge1);
            double[] normalD = cross.toArray();
            float[] normalF = new float[]{(float) normalD[0], (float) normalD[1], (float) normalD[2]};

            // Need to normalize the normal else we'll get poorly weighted final normal when we average it.
            float length = (float) Math.sqrt(normalF[0] * normalF[0] + normalF[1] * normalF[1] + normalF[2] * normalF[2]);
            normalF[0] /= length;
            normalF[1] /= length;
            normalF[2] /= length;

            // Add this normal to each of the face's vertices' current sum. We'll average it out later.
            normals[p1Index] += normalF[0];
            normals[p1Index + 1] += normalF[1];
            normals[p1Index + 2] += normalF[2];

            normals[p2Index] += normalF[0];
            normals[p2Index + 1] += normalF[1];
            normals[p2Index + 2] += normalF[2];

            normals[p3Index] += normalF[0];
            normals[p3Index + 1] += normalF[1];
            normals[p3Index + 2] += normalF[2];

            contributions[faces[i]]++;
            contributions[faces[i + 1]]++;
            contributions[faces[i + 2]]++;
        }

        for(int i=0; i < normals.length; i+=3){
            float contribution = contributions[1];
            normals[i] /= contribution;
            normals[i+1] /= contribution;
            normals[i+2] /= contribution;

            // Normalize the average.
            float length = (float) Math.sqrt(normals[i] * normals[i] + normals[i+1] * normals[i+1] + normals[i+2] * normals[i+2]);
            normals[i] /= length;
            normals[i+1] /= length;
            normals[i+2] /= length;
        }

        return normals;
    }
}
