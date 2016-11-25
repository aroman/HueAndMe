package avi.bio.huesaturationvictory;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by avi on 11/19/16.
 */

enum GameState {
    PRE_ROUND,
    IN_ROUND,
    ROUND_WON,
    ROUND_LOST,
}

public class GameSurfaceView extends SurfaceView implements Runnable {

    static final int UI_PADDING = 20; // px
    static final int MAX_GUESS_TIME = 5_000; // ms
    static final int MAX_ROUND_TIME = 30_000; // ms

    GameState mState;

    Thread mGameThread = null;

    int mPoints;
    int mSecondsRemainingInRound;

    long mTimeGuessStarted;
    long mTimeRoundStarted;

    int mBackgroundColor;
    int mCircleColor;

    List<HSVGuess> mGuesses;
    SoundPool mSoundPool;
    int[] mSoundIds;
    SurfaceHolder mHolder;
    Vibrator mVibrator;

    volatile boolean mIsPlaying;

    Canvas mCanvas;
    Paint mDefaultPaint;

    long fps;

    // Used to help calculate the fps
    private long timeThisFrame;

    public GameSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mHolder = getHolder();

        mDefaultPaint = new Paint();
        mDefaultPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        Typeface azoSans = Typeface.createFromAsset(context.getAssets(), "fonts/azo-sans-uber.ttf");
        mDefaultPaint.setTypeface(azoSans);
        mDefaultPaint.setColor(Color.WHITE);
        mDefaultPaint.setTextSize(80);

//        MediaPlayer mPlayer = MediaPlayer.create(context, R.raw.music);
//        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        mPlayer.start();
//        mPlayer.setLooping(true);

        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        mSoundIds = new int[10];
        mSoundIds[0] = mSoundPool.load(context, R.raw.zap, 1);

        mState = GameState.PRE_ROUND;

        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        mIsPlaying = true;
    }

    public void shutdown() {
        mIsPlaying = false;
        mSoundPool.release();
    }

    private void playSound(int soundId) {
        mSoundPool.play(mSoundIds[soundId], 1, 1, 1, 0, 1.0f);
    }

    private void startNewRound() {
        mState = GameState.IN_ROUND;
        mPoints = 0;
        mTimeRoundStarted = System.currentTimeMillis();
        mGuesses = Collections.synchronizedList(new ArrayList<HSVGuess>());
        resetColors();
    }

    public void setColorFromRotationVector(float[] rotationVector) {
        // TODO: Sometimes the vector is empty. Not sure why.
        if (rotationVector[0] == 0) return;

        float[] hsv = new float[3];
        Color.colorToHSV(mCircleColor, hsv);
        float hue = (rotationVector[0] + 1) * 180;
        hsv[0] = hue;

        mBackgroundColor = Color.HSVToColor(hsv);
    }

    private void update() {
        long currentTime = System.currentTimeMillis();
        long msSinceRoundStarted = currentTime - mTimeRoundStarted + 1;
        mSecondsRemainingInRound = Math.round((MAX_ROUND_TIME - msSinceRoundStarted) / 1000);

        if (mSecondsRemainingInRound == 0) {
            mState = GameState.ROUND_WON;
        }
    }

    private void resetColors() {
        mTimeGuessStarted = SystemClock.elapsedRealtime();
        mCircleColor = Color.HSVToColor(new float[]{ (float)Math.random() * 360, 1, 1 });
    }

    private void draw() {
        // Make sure our drawing surface is valid or we crash
        if (!mHolder.getSurface().isValid()) return;

        mCanvas = mHolder.lockCanvas();

        if (mCanvas == null) return;

        // Timestamps used for animation
        long msSinceGuessStarted = SystemClock.elapsedRealtime() - mTimeGuessStarted + 1;
        long currentTime = System.currentTimeMillis();

        // Clear the background
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Paint statePaint = new Paint(mDefaultPaint);
        statePaint.setTextSize(200);
        statePaint.setTextAlign(Paint.Align.CENTER);

        // Center text in screen
        float centerX = (mCanvas.getWidth() / 2);
        float centerY = ((mCanvas.getHeight() / 2) - ((statePaint.descent() + statePaint.ascent()) / 2));

        if (mState == GameState.PRE_ROUND) {
            drawTextWithOutline("HUE & ME",
                    centerX,
                    centerY,
                    statePaint
            );
        }
        else if (mState == GameState.ROUND_LOST) {
            drawTextWithOutline("ROUND LOST!",
                    Color.RED,
                    Color.BLACK,
                    centerX,
                    centerY,
                    statePaint
            );
        }
        else if (mState == GameState.ROUND_WON) {
            drawTextWithOutline("Time's up!",
                    centerX,
                    centerY,
                    statePaint
            );

            Paint pointsPaint = new Paint(statePaint);
            pointsPaint.setTextSize(120);
            drawTextWithOutline(String.format("score: %d", mPoints),
                    centerX,
                    centerY + 200,
                    pointsPaint
            );
        } else {
            if (msSinceGuessStarted > MAX_GUESS_TIME) {
                resetColors();
            }

            // Draw background color
            mCanvas.drawColor(mBackgroundColor);

            // Draw circle
            Paint circlePaint = new Paint(mDefaultPaint);
            circlePaint.setColor(mCircleColor);
            float radius = mCanvas.getHeight() / 4;
            mCanvas.drawCircle(
                    mCanvas.getWidth() / 2,
                    mCanvas.getHeight() / 2,
                    radius - Easing.easeInSine(msSinceGuessStarted, 0, radius, MAX_GUESS_TIME),
                    circlePaint
            );

            // Draw FPS
            Paint fpsPaint = new Paint(mDefaultPaint);
            fpsPaint.setTextSize(45);
            fpsPaint.setTextAlign(Paint.Align.RIGHT);
            drawTextWithOutline("FPS: " + fps,
                    mCanvas.getWidth() - UI_PADDING,
                    mCanvas.getHeight() - UI_PADDING,
                    fpsPaint
            );


            // Draw score
            Paint scorePaint = new Paint(mDefaultPaint);
            scorePaint.setTextAlign(Paint.Align.RIGHT);
            drawTextWithOutline("Score: " + mPoints,
                    mCanvas.getWidth() - UI_PADDING,
                    80,
                    scorePaint
            );

            // Draw time remaining
            Paint timerPaint = new Paint(mDefaultPaint);
            timerPaint.setTextAlign(Paint.Align.LEFT);
            drawTextWithOutline("Time remaining: " + mSecondsRemainingInRound,
                    UI_PADDING,
                    80,
                    timerPaint
            );

            // Draw guess quality text (animated)
            float animationDuration = 1_500;
            synchronized(mGuesses) {
                Iterator i = mGuesses.iterator();
                while (i.hasNext()) {
                    HSVGuess guess = (HSVGuess) i.next();
                    float timeSinceGuess = (currentTime - guess.tsCreated);
                    Boolean guessAlreadyAnimated = timeSinceGuess > animationDuration;
                    if (guessAlreadyAnimated) continue;


                    // Set text style
                    Paint guessPaint = new Paint(mDefaultPaint);
                    guessPaint.setTextAlign(Paint.Align.CENTER);
                    guessPaint.setTextSize(150);

                    // Update center
                    centerX = (mCanvas.getWidth() / 2);
                    centerY = ((mCanvas.getHeight() / 2) - ((guessPaint.descent() + guessPaint.ascent()) / 2));

                    // Fade
                    float textOpacity = 255 - Easing.easeInQuad(timeSinceGuess, 0, 255, animationDuration);
                    int outlineColor = Color.argb(Math.round(textOpacity), 25, 25, 25);
                    int fillColor = Color.argb(Math.round(textOpacity), 255, 255, 255);

                    // Drift
                    float offset = -Easing.easeInQuad(timeSinceGuess, 0, centerY, animationDuration);
                    centerY += offset;

                    String guessString;
                    if (guess.quality == GuessQuality.LOW) {
                        guessString = "YIKES!";
                    } else {
                        guessString = String.format(
                                Locale.getDefault(),
                                "%s +%d",
                                getQualityString(guess.quality),
                                guess.points
                        );
                    }

                    drawTextWithOutline(guessString, fillColor, outlineColor, centerX, centerY, guessPaint);
                }
            }
        }

        // Draw everything to the screen
        // and unlock the drawing surface
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    private String getQualityString(GuessQuality quality) {
        switch (quality) {
            case LOW:
                return getContext().getString(R.string.guess_quality_low);
            case MEDIUM:
                return getContext().getString(R.string.guess_quality_medium);
            case HIGH:
                return getContext().getString(R.string.guess_quality_high);
            case MAX:
                return getContext().getString(R.string.guess_quality_max);
            default:
                return "Invalid quality";
        }
    }

    private void drawTextWithOutline(String text, float x, float y, Paint paint) {
        drawTextWithOutline(text, Color.WHITE, Color.BLACK, x, y, paint);
    }

    private void drawTextWithOutline(String text, int fillColor, int outlineColor, float x, float y, Paint paint) {
        paint.setColor(fillColor);
        Paint outlinePaint = new Paint(paint);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(paint.getTextSize() / 20);
        outlinePaint.setColor(outlineColor);
        mCanvas.drawText(text, x, y, paint);
        mCanvas.drawText(text, x, y, outlinePaint);
    }

    private void addGuess() {
        playSound(0);
        HSVGuess guess = new HSVGuess(mBackgroundColor, mCircleColor);
        mPoints += guess.points;
        mGuesses.add(guess);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mState == GameState.PRE_ROUND) {
            startNewRound();
        }
        if (mState == GameState.IN_ROUND) {
            long msSinceGuessStarted = SystemClock.elapsedRealtime() - mTimeGuessStarted + 1;
            if (msSinceGuessStarted > 150) {
                addGuess();
                mVibrator.vibrate(250);
                resetColors();
            }
        }
        else if (mState == GameState.ROUND_WON) {
            startNewRound();
        }

        return super.onTouchEvent(event);
    }

    // If SimpleGameEngine Activity is paused/stopped
    // shutdown our thread.
    public void pause() {
        mIsPlaying = false;
        try {
            mGameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    // If SimpleGameEngine Activity is started theb
    // start our thread.
    public void resume() {
        mIsPlaying = true;
        mGameThread = new Thread(this);
        mGameThread.start();
    }

    @Override
    public void run() {

        while (true) {
            long startFrameTime = System.currentTimeMillis();

            update();

            draw();

            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame > 0 && timeThisFrame % 5 == 0) {
                fps = 1000 / timeThisFrame;
            }

        }

    }
}