package avi.bio.huesaturationvictory;

/**
 * Created by avi on 11/24/16.
 */

// Easing functions from Robert Penner's easing functions
public class Easing {

    public static float easeInQuad(float t, float b, float c, float d) {
        return c * (t /= d) * t + b;
    }

    public static float easeInSine(float t, float b, float c, float d) {
        return -c * (float)Math.cos(t/d * (Math.PI/2)) + c + b;
    }

}
