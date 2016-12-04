package bio.avi.hueandme;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import static java.security.AccessController.getContext;

/**
 * Created by avi on 11/23/16.
 */

public class HSVGuess {

    private static final double LAB_BIN_SPAN = 5;

    public final int points;
    public final GuessQuality quality;
    public final long tsCreated;

    public HSVGuess(int colorGuessed, int colorActual) {
        tsCreated = System.currentTimeMillis();

        LabColor a = LabColor.fromRGBr(
                Color.red(colorGuessed),
                Color.green(colorGuessed),
                Color.blue(colorGuessed),
                LAB_BIN_SPAN
        );
        LabColor b = LabColor.fromRGBr(
                Color.red(colorActual),
                Color.green(colorActual),
                Color.blue(colorActual),
                LAB_BIN_SPAN
        );

        // Calculate dE00 color difference
        double difference = LabColor.ciede2000(a, b);
//        Log.d("HSV", "Raw difference: " + difference);

        // Clamp to 0-100
        difference = Math.min(Math.max(difference, 1), 100);

        if (difference < 2) {
            quality = GuessQuality.MAX;
            points = 100;
        }
        else if (difference < 12) {
            quality = GuessQuality.HIGH;
            points = 25;
        }
        else if (difference < 30) {
            quality = GuessQuality.MEDIUM;
            points = 10;
        } else {
            quality = GuessQuality.LOW;
            points = 0;
        }
    }

    public static String getQualityString(Context context, GuessQuality quality) {
        switch (quality) {
            case LOW:
                return context.getString(R.string.guess_quality_low);
            case MEDIUM:
                return context.getString(R.string.guess_quality_medium);
            case HIGH:
                return context.getString(R.string.guess_quality_high);
            case MAX:
                return context.getString(R.string.guess_quality_max);
            default:
                return "Invalid quality";
        }
    }


}
