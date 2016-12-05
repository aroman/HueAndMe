package bio.avi.hueandme;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

import static bio.avi.hueandme.GameState.MAX_GUESS_TIME;
import static bio.avi.hueandme.GameState.MAX_ROUND_TIME;
import static bio.avi.hueandme.GameState.HUE_HANDICAP;
import static bio.avi.hueandme.GameState.HUE_MAX;

/**
 * Created by avi on 11/30/16.
 */

interface GameStateChangedListener {
    void onGameStateChanged(GameState gameState);
}

public class GameEngine implements Runnable {

    private GameStateChangedListener mListener;
    Thread mThread = null;
    GameState mGameState;
    volatile boolean mIsStopped;

    // Vibration, music, sounds
    private MediaPlayer mPlayer;
    private Vibrator mVibrator;
    private GameSounds mGameSounds;

    public GameEngine(Context context, GameState initialState) {
        mPlayer = MediaPlayer.create(context, R.raw.music);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setLooping(false);
        mPlayer.setVolume(0.06f, 0.06f);

        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mGameSounds = new GameSounds(context);

        mGameState = initialState;
        startNewRound();
    }

    public void addListener(GameStateChangedListener listener) {
        mListener = listener;
    }

    private void update() {
        if (mGameState.status == GameStatus.IN_ROUND) {
            long currentTime = System.currentTimeMillis();
            long msSinceRoundStarted = currentTime - mGameState.timeRoundStarted + 1;
            mGameState.secondsRemainingInRound = Math.round((MAX_ROUND_TIME - msSinceRoundStarted) / 1000);
            long msSinceGuessStarted = SystemClock.elapsedRealtime() - mGameState.timeGuessStarted + 1;

            // if guess time ran out, new color
            if (msSinceGuessStarted > MAX_GUESS_TIME) {
                resetColor();
            }

            // if round time ran out, end game
            if (mGameState.secondsRemainingInRound <= 0) {
                mGameState.lastBlip = mGameState.secondsRemainingInRound;
                mGameState.status = GameStatus.POST_ROUND;
                mGameSounds.play(GameSounds.SOUND_TIMER_END);
                mPlayer.pause();
            }

            // play count down
            else if (mGameState.secondsRemainingInRound <= 5 &&
                    mGameState.lastBlip != mGameState.secondsRemainingInRound) {
                mGameState.lastBlip = mGameState.secondsRemainingInRound;
                mGameSounds.play(GameSounds.SOUND_TIMER_BLIP);
            }
        }
        mListener.onGameStateChanged(new GameState(mGameState));
    }

    public void startNewRound() {
        mGameState = new GameState();
        resetColor();
        mPlayer.seekTo(0);
        mPlayer.start();
    }

    private void addGuess() {
        HSVGuess guess = new HSVGuess(mGameState.backgroundColor, mGameState.circleColor);
        if (guess.quality == GuessQuality.LOW) {
            mGameSounds.play(GameSounds.SOUND_FAIL);
        } else {
            mGameSounds.play(GameSounds.SOUND_ZAP);
        }
        mGameState.points += guess.points;
        mGameState.guesses.add(guess);
        mListener.onGameStateChanged(mGameState);
    }

    public void onRotationChange(float rotation) {
        // Sometimes the Android API will give us no rotation, since we're polling
        // at max rate. We skip those to avoid spurious data.
        if (rotation == 0) return;

        float hue = (rotation + 1) * (HUE_MAX / 2);
        hue = MathUtils.clamp(hue, HUE_HANDICAP, HUE_MAX - HUE_HANDICAP);
        hue = MathUtils.rescale(hue, HUE_HANDICAP, HUE_MAX - HUE_HANDICAP, 0, HUE_MAX);

        mGameState.backgroundColor = Color.HSVToColor(new float[]{hue, 1, 1});
    }

    public void onTap() {
        if (mIsStopped) {
            Log.w("GameEngine", "onTap called while paused! Ignoring...");
            return;
        }
        if (mGameState.status == GameStatus.IN_ROUND) {
            long msSinceGuessStarted = SystemClock.elapsedRealtime() - mGameState.timeGuessStarted + 1;
            if (msSinceGuessStarted > 150) {
                addGuess();
                mVibrator.vibrate(250);
                resetColor();
            }
        }
        mListener.onGameStateChanged(mGameState);
    }

    private void resetColor() {
        mGameState.timeGuessStarted = SystemClock.elapsedRealtime();
        mGameState.circleColor = Color.HSVToColor(new float[]{(float) Math.random() * 360, 1, 1});
    }

    public void pause() {
        mIsStopped = true;
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    public void resume() {
        mIsStopped = false;
        mThread = new Thread(this);
        mThread.start();
        if (mPlayer.isPlaying()) {
            mPlayer.start();
        }
    }

    @Override
    public void run() {
        while (!mIsStopped) {
            update();
        }
    }

}
