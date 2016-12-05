package bio.avi.hueandme;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by avi on 11/29/16.
 */

enum GameStatus {
    IN_ROUND,
    POST_ROUND,
}

public class GameState {

    public static final int HUE_MAX = 360; // degrees
    public static final int MAX_GUESS_TIME = 5_000; // ms
    public static final int MAX_ROUND_TIME = 30_000; // ms
    // it's hard to turn your phone fully vertically
    // so we want to treat the upper and lower 60 degrees
    // as actually being 0 values, to make them more easily
    // accessible.
    public static final int HUE_HANDICAP = 60; // degrees

    public GameStatus status;

    public int points;
    public int backgroundColor;
    public int circleColor;
    public List<HSVGuess> guesses;

    public int lastBlip;
    public int secondsRemainingInRound;
    public long timeGuessStarted;
    public long timeRoundStarted;

    public GameState() {
        points = 0;
        backgroundColor = Color.BLACK;
        circleColor = Color.RED;
        timeRoundStarted = System.currentTimeMillis();
        guesses = Collections.synchronizedList(new ArrayList<HSVGuess>());
        status = GameStatus.IN_ROUND;
    }

    public GameState(GameState orig) {
        status = orig.status;

        points = orig.points;
        backgroundColor = orig.backgroundColor;
        circleColor = orig.circleColor;
        guesses = Collections.synchronizedList(new ArrayList<>(orig.guesses));

        lastBlip = orig.lastBlip;
        secondsRemainingInRound = orig.secondsRemainingInRound;
        timeGuessStarted = orig.timeGuessStarted;
        timeRoundStarted = orig.timeRoundStarted;
    }

}
