package bio.avi.hueandme;

/**
 * Created by avi on 11/24/16.
 */

// MathUtils functions from Robert Penner's easing functions
public class MathUtils {

    public static float easeInQuad(float t, float b, float c, float d) {
        return c * (t /= d) * t + b;
    }

    public static float easeInSine(float t, float b, float c, float d) {
        return -c * (float)Math.cos(t/d * (Math.PI/2)) + c + b;
    }

    public static float rescale(float oldVal, float oldMin, float oldMax, float newMin, float newMax) {
        float oldRange = (oldMax - oldMin);
        float newRange = (newMax - newMin);
        float newVal = (((oldVal - oldMin) * newRange) / oldRange) + newMin;
        return newVal;
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(min, value));
    }

}
