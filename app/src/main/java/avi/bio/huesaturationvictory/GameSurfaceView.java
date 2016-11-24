package avi.bio.huesaturationvictory;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Locale;

import static android.R.attr.x;

/**
 * Created by avi on 11/19/16.
 */

public class GameSurfaceView extends SurfaceView implements Runnable {

    static final int UI_PADDING = 20; // px
    static final int MAX_GUESS_TIME = 5_000; // ms
    static final int MAX_ROUND_TIME = 30_000; // ms

    Thread mGameThread = null;

    int mPoints;
    int mSecondsRemainingInRound;

    long mTimeGuessStarted;
    long mTimeRoundStarted;

    int mBackgroundColor;
    int mCircleColor;

    ArrayList<HSVGuess> mGuesses;

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

        startNewRound();

        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        mIsPlaying = true;
    }

    private void startNewRound() {
        mPoints = 0;
        mTimeRoundStarted = System.currentTimeMillis();
        mGuesses = new ArrayList<>();
        resetColors();
    }

    public void setColorFromRotationVector(float[] rotationVector) {
        // TODO: Sometimes the vector is empty. Not sure why.
        if (rotationVector[0] == 0) return;

        float[] hsv = new float[3];
        Color.colorToHSV(mCircleColor, hsv);
        float hue = (rotationVector[0] + 1) * 180;
        float saturation = rotationVector[1] + 1;
        hsv[0] = hue;
        hsv[1] = saturation;
        // we keep value constant

        mBackgroundColor = Color.HSVToColor(hsv);
    }

    private void update() {
        long currentTime = System.currentTimeMillis();
        long msSinceRoundStarted = currentTime - mTimeRoundStarted + 1;
        mSecondsRemainingInRound = Math.round((MAX_ROUND_TIME - msSinceRoundStarted) / 1000);

        if (mSecondsRemainingInRound == 0) {
            startNewRound();
        }
    }

    private void resetColors() {
        mTimeGuessStarted = SystemClock.elapsedRealtime();
//        mVibrator.vibrate(250);
        mCircleColor = Color.HSVToColor(new float[]{
                (float)Math.random() * 360,
                Math.max(10, (float)Math.random()),
                Math.max(10, (float)Math.random()),
        });
    }

    private void draw() {
        // Make sure our drawing surface is valid or we crash
        if (!mHolder.getSurface().isValid()) return;

        mCanvas = mHolder.lockCanvas();

        if (mCanvas == null) return;

        // Timestamps used for animation
        // TODO: Move to update()
        long msSinceGuessStarted = SystemClock.elapsedRealtime() - mTimeGuessStarted + 1;

        if (msSinceGuessStarted > MAX_GUESS_TIME) {
            resetColors();
        }

        // Draw background color
        mCanvas.drawColor(mBackgroundColor);

        // Draw circle
        Paint circlePaint = new Paint(mDefaultPaint);
        circlePaint.setColor(mCircleColor);
        mCanvas.drawCircle(
                mCanvas.getWidth() / 2,
                mCanvas.getHeight() / 2,
                (mCanvas.getHeight() / 4) - msSinceGuessStarted / 20,
                circlePaint
        );

        // Draw FPS
        Paint fpsPaint = new Paint(mDefaultPaint);
        fpsPaint.setColor(Color.WHITE);
        fpsPaint.setTextSize(45);
        fpsPaint.setTextAlign(Paint.Align.RIGHT);
        drawTextWithOutline("FPS: " + fps,
                Color.BLACK,
                mCanvas.getWidth() - UI_PADDING,
                mCanvas.getHeight() - UI_PADDING,
                fpsPaint
        );


        // Draw score
        Paint scorePaint = new Paint(mDefaultPaint);
        scorePaint.setColor(Color.WHITE);
        scorePaint.setTextAlign(Paint.Align.RIGHT);
        drawTextWithOutline("Score: " + mPoints,
                Color.BLACK,
                mCanvas.getWidth() - UI_PADDING,
                80,
                scorePaint
        );

        // Draw time remaining
        Paint timerPaint = new Paint(mDefaultPaint);
        timerPaint.setTextAlign(Paint.Align.LEFT);
        drawTextWithOutline("Time remaining: " + mSecondsRemainingInRound,
                Color.BLACK,
                UI_PADDING,
                80,
                timerPaint
        );

        if (!mGuesses.isEmpty()) {
            HSVGuess latestGuess = mGuesses.get(mGuesses.size() - 1);

            float fadeDelay = 250;

            float textOpacity = 255 - ((msSinceGuessStarted - fadeDelay) / 5);
            float offset = - (msSinceGuessStarted) / 4;
            if (msSinceGuessStarted < fadeDelay) {
                textOpacity = 255;
            }

            int outlineColor = Color.argb(Math.round(textOpacity), 25, 25, 25);
            int innerColor = Color.argb(Math.round(textOpacity), 255, 255, 255);

            Paint guessPaint = new Paint(mDefaultPaint);
            guessPaint.setTextAlign(Paint.Align.CENTER);
            guessPaint.setTextSize(150);
            guessPaint.setColor(innerColor);

            String guessString = String.format(
                    Locale.getDefault(),
                    "%s +%d",
                    getQualityString(latestGuess.quality),
                    latestGuess.points
            );

            if (textOpacity > 0) {
                float centerX = (mCanvas.getWidth() / 2);
                float centerY = ((mCanvas.getHeight() / 2) - ((guessPaint.descent() + guessPaint.ascent()) / 2));
                centerY += offset;
                drawTextWithOutline(guessString, outlineColor, centerX, centerY, guessPaint);
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

    private void drawTextWithOutline(String text, int outlineColor, float x, float y, Paint paint) {
        Paint outlinePaint = new Paint(paint);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(paint.getTextSize() / 20);
        outlinePaint.setColor(outlineColor);
        mCanvas.drawText(text, x, y, paint);
        mCanvas.drawText(text, x, y, outlinePaint);
    }

//    private void drawTextAtCenterOfCanvasWithVOffset(String text, Paint paint, float offset) {
//        int x = (mCanvas.getWidth() / 2);
//        int y = (int) ((mCanvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
////        y = (int) (paint.descent() - paint.ascent() / 2) + 100;
//        y += offset;
//        mCanvas.drawText(text, x, y, paint);
//    }

    private void addGuess() {
        HSVGuess guess = new HSVGuess(mBackgroundColor, mCircleColor);
        mPoints += guess.points;
        mGuesses.add(guess);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        addGuess();
        resetColors();

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

            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            update();

            // Draw the frame
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