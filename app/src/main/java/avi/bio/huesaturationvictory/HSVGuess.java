package avi.bio.huesaturationvictory;

import android.content.res.Resources;
import android.graphics.Color;

import static android.content.res.Resources.getSystem;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by avi on 11/23/16.
 */

public class HSVGuess {

    public final GuessQuality quality;
    public final int points;
    public final long tsCreated;

    private int mColorGuessed;
    private int mColorActual;

    public HSVGuess(int colorGuessed, int colorActual) {
        tsCreated = System.currentTimeMillis();
        mColorGuessed = colorGuessed;
        mColorActual = colorActual;

        points = calcuatePoints();
        assert points >= 0;
        quality = qualityFromPoints();
    }

    private GuessQuality qualityFromPoints() {
        if (points >= 0 && points <= 40) {
            return GuessQuality.LOW;
        }
        if (points > 40 && points <= 75) {
            return GuessQuality.MEDIUM;
        }
        if (points > 75 && points <= 97) {
            return GuessQuality.HIGH;
        }
        if (points > 97) {
            return GuessQuality.MAX;
        }
        throw new IllegalStateException(String.format("No quality associated with point value of %d", points));
    }

    private int calcuatePoints() {
        int total = 0;

        float[] a = new float[3];
        Color.colorToHSV(mColorGuessed, a);

        float[] b = new float[3];
        Color.colorToHSV(mColorActual, b);

        for (int i = 0; i < a.length; i++) {
            total += Math.abs(b[i] - a[i]);
        }

        return Math.max(0, Math.round(((254 - total) / (float)2.55)));
    }

}
