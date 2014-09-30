package utils;

public class Useful {
    public static double[] fadeColour(double[] c1, double[] c2, double ratio) {

        double c1h = c1[0], c1s = c1[1], c1v = c1[2];
        double c2h = c2[0], c2s = c2[1], c2v = c2[2];

        double distCCW = (c1h >= c2h) ? c1h - c2h : 1 + c1h - c2h;
        double distCW = (c1h >= c2h) ? 1 + c2h - c1h : c2h - c1h;

        // interpolate h, s, v
        double h = (distCW <= distCCW) ? c1h + (distCW * ratio) : c1h - (distCCW * ratio);
        if (h < 0) h = 1 + h;
        if (h > 1) h = h - 1;
        double s = (1 - ratio) * c1s + ratio * c2s;
        double v = (1 - ratio) * c1v + ratio * c2v;

        return  hsvToRgb(h,s,v);
    }

    /**
     * Colour space conversion code taken from http://stackoverflow.com/questions/7896280/converting-from-hsv-hsb-in-java-to-rgb-without-using-java-awt-color-disallowe
     */
    public static double[] hsvToRgb(double hue, double saturation, double value) {

        int h = (int)(hue * 6);
        double f = hue * 6 - h;
        double p = value * (1 - saturation);
        double q = value * (1 - f * saturation);
        double t = value * (1 - (1 - f) * saturation);

        switch (h) {
            case 0: return new double[]{value, t, p};
            case 1: return new double[]{q, value, p};
            case 2: return new double[]{p, value, t};
            case 3: return new double[]{p, q, value};
            case 4: return new double[]{t, p, value};
            case 5: return new double[]{value, p, q};
            default: throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
        }
    }
}
