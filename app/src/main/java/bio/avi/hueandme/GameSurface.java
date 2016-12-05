package bio.avi.hueandme;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Iterator;
import java.util.Locale;

import static bio.avi.hueandme.GameState.MAX_GUESS_TIME;

/**
 * Created by avi on 11/19/16.
 */

public class GameSurface extends SurfaceView implements Runnable {

    // Constants to tweak difficulty and timing
    private static final int UI_PADDING = 20; // px

    private Thread mThread = null;
    private volatile boolean mIsStopped;

    private GameState mGameState;

    private SurfaceHolder mHolder;
    private Paint mCirclePaint;
    private Paint mScorePaint;
    private Paint mTimerPaint;
    private Paint mGuessPaint;

    public GameSurface(Context context) {
        super(context);
        init(context);
    }

    public GameSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mHolder = getHolder();

        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        Typeface azoSans = Typeface.createFromAsset(context.getAssets(), "fonts/azo-sans-uber.ttf");
        paint.setTypeface(azoSans);
        paint.setColor(Color.WHITE);
        paint.setTextSize(80);

        mCirclePaint = new Paint(paint);

        mScorePaint = new Paint(paint);
        mScorePaint.setTextAlign(Paint.Align.RIGHT);

        mTimerPaint = new Paint(paint);
        mTimerPaint.setTextAlign(Paint.Align.LEFT);

        mGuessPaint = new Paint(paint);
        mGuessPaint.setTextAlign(Paint.Align.CENTER);
        mGuessPaint.setTextSize(150);
    }

    public void setGameState(GameState newState) {
        mGameState = newState;
    }

    private void drawGame(Canvas canvas) {
        // Clear the background
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // Timestamps used for animation
        long msSinceGuessStarted = SystemClock.elapsedRealtime() - mGameState.timeGuessStarted + 1;
        long currentTime = System.currentTimeMillis();

        // Draw background color
        canvas.drawColor(mGameState.backgroundColor);

        // Draw circle
        mCirclePaint.setColor(mGameState.circleColor);
        float radius = canvas.getHeight() / 4;
        canvas.drawCircle(
                canvas.getWidth() / 2,
                canvas.getHeight() / 2,
                radius - MathUtils.easeInSine(msSinceGuessStarted, 0, radius, MAX_GUESS_TIME),
                mCirclePaint
        );

        // Draw score
        drawTextWithOutline(canvas,
                "Score: " + mGameState.points,
                canvas.getWidth() - UI_PADDING,
                80,
                mScorePaint
        );

        // Draw time remaining
        drawTextWithOutline(canvas,
                "Time remaining: " + mGameState.secondsRemainingInRound,
                UI_PADDING,
                80,
                mTimerPaint
        );

        // Draw guess quality text (animated)
        float animationDuration = 1_500;
        synchronized (mGameState.guesses) {
            Iterator i = mGameState.guesses.iterator();
            while (i.hasNext()) {
                HSVGuess guess = (HSVGuess) i.next();
                float timeSinceGuess = (currentTime - guess.tsCreated);
                Boolean guessAlreadyAnimated = (timeSinceGuess > animationDuration);
                if (guessAlreadyAnimated) continue;

                // Calculate center point for drawing text
                float centerX = (canvas.getWidth() / 2);
                float centerY = ((canvas.getHeight() / 2) - ((mGuessPaint.descent() + mGuessPaint.ascent()) / 2));

                // Fade
                float textOpacity = 255 - MathUtils.easeInQuad(timeSinceGuess, 0, 255, animationDuration);
                int outlineColor = Color.argb(Math.round(textOpacity), 25, 25, 25);
                int fillColor = Color.argb(Math.round(textOpacity), 255, 255, 255);

                // Drift
                float offset = -MathUtils.easeInQuad(timeSinceGuess, 0, centerY, animationDuration);
                centerY += offset;

                String guessString;
                if (guess.quality == GuessQuality.LOW) {
                    guessString = "YIKES!";
                } else {
                    guessString = String.format(
                            Locale.getDefault(),
                            "%s +%d",
                            HSVGuess.getQualityString(getContext(), guess.quality),
                            guess.points
                    );
                }

                drawTextWithOutline(
                        canvas,
                        guessString,
                        fillColor, outlineColor,
                        centerX, centerY,
                        mGuessPaint
                );
            }
        }
    }

    private void drawTextWithOutline(Canvas canvas, String text, float x, float y, Paint paint) {
        drawTextWithOutline(canvas, text, Color.WHITE, Color.BLACK, x, y, paint);
    }

    private void drawTextWithOutline(Canvas canvas, String text, int fillColor, int outlineColor, float x, float y, Paint paint) {
        paint.setColor(fillColor);
        Paint outlinePaint = new Paint(paint);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(paint.getTextSize() / 20);
        outlinePaint.setColor(outlineColor);
        canvas.drawText(text, x, y, paint);
        canvas.drawText(text, x, y, outlinePaint);
    }

    public void pause() {
        mIsStopped = true;
    }

    public void resume() {
        mIsStopped = false;
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void run() {
        Canvas canvas;
        while (!mIsStopped) {
            // Make sure our drawing surface is valid or we crash
            if (!mHolder.getSurface().isValid()) {
                continue;
            }

            canvas = mHolder.lockCanvas();

            if (canvas == null) {
                continue;
            }

            drawGame(canvas);

            // Draw everything to the screen
            // and unlock the drawing surface
            mHolder.unlockCanvasAndPost(canvas);
        }
    }

}