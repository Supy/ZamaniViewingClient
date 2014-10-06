package utils;

import javax.media.opengl.GL2;

public class FeatureToggle {

    private static boolean drawBoundingVolumes = false;
    private static boolean drawNormals = false;
    private static int polygonFillMode = GL2.GL_FILL;
    private static int polygonMode = GL2.GL_TRIANGLES;
    private static int shaderType = GL2.GL_SMOOTH;
    private static int frontFace = GL2.GL_CCW;
    private static boolean useLighting = true;

    public static void toggleDrawBoundingVolumes() {
        drawBoundingVolumes = !drawBoundingVolumes;
    }

    public static boolean shouldDrawBoundingVolumes() {
        return drawBoundingVolumes;
    }

    public static void toggleDrawNormals() {
        drawNormals = !drawNormals;
    }

    public static boolean shouldDrawNormals() {
        return drawNormals;
    }

    public static void togglePolygonFillMode() {
        polygonFillMode = (polygonFillMode == GL2.GL_FILL) ? GL2.GL_LINE : GL2.GL_FILL;
    }

    public static int getPolygonFillMode() {
        return polygonFillMode;
    }

    public static void togglePolygonMode() {
        polygonMode = (polygonMode == GL2.GL_TRIANGLES) ? GL2.GL_POINTS : GL2.GL_TRIANGLES;
    }

    public static int getPolygonMode() {
        return polygonMode;
    }

    public static void toggleShaderType() {
        shaderType = (shaderType == GL2.GL_FLAT) ? GL2.GL_SMOOTH : GL2.GL_FLAT;
    }

    public static int getShaderType() {
        return shaderType;
    }

    public static void toggleFrontFace() {
        frontFace = (frontFace == GL2.GL_CCW) ? GL2.GL_CW : GL2.GL_CCW;
    }

    public static int getFrontFace() {
        return frontFace;
    }

    public static void toggleUseLighting() {
        useLighting = !useLighting;
    }

    public static boolean shouldUseLighting() {
        return useLighting;
    }
}
