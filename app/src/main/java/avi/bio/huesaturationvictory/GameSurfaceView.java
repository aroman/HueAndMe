package avi.bio.huesaturationvictory;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by avi on 11/19/16.
 */

public class GameSurfaceView extends SurfaceView implements Runnable {

    // This is our thread
    Thread gameThread = null;
    private long mStartTime;

    private int mBackgroundColor;
    private int mCircleColor;

    private float[] deltaRotationVector = new float[4];

    SurfaceHolder ourHolder;

    private Vibrator mVibrator;

    // A boolean which we will set and unset
    // when the game is running- or not.
    volatile boolean playing;

    // A Canvas and a Paint object
    Canvas canvas;
    Paint paint;

    // This variable tracks the game frame rate
    long fps;

    // This is used to help calculate the fps
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
        // Initialize ourHolder and paint objects
        ourHolder = getHolder();
        paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        resetColors();

        // Set our boolean to true - game on!
        playing = true;
    }

    public void setRotationVector(float[] rotationVector) {
        deltaRotationVector = rotationVector;
    }

    private void update() {
        Log.d("HSV", String.format("%f, %f, %f", deltaRotationVector[0], deltaRotationVector[1], deltaRotationVector[2]));
        if (deltaRotationVector[0] == 0) return;
        float[] hsv = new float[3];
        Color.colorToHSV(mCircleColor, hsv);
        float hue = (deltaRotationVector[0] + 1) * 180;
        float saturation = deltaRotationVector[1] + 1;
        hsv[0] = hue;
        hsv[1] = saturation;
        // we keep value constant

        mBackgroundColor = Color.HSVToColor(hsv);
    }

    private void resetColors() {
        mStartTime = SystemClock.elapsedRealtime();
        mVibrator.vibrate(250);
        mCircleColor = Color.HSVToColor(new float[]{
                (float)Math.random() * 360,
                Math.max(10, (float)Math.random()),
                Math.max(10, (float)Math.random()),
        });
    }

    private void draw() {
        // Make sure our drawing surface is valid or we crash
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            // Make the drawing surface our canvas object
            canvas = ourHolder.lockCanvas();

            // Draw the background color
            canvas.drawColor(mBackgroundColor);

            // Choose the brush color for drawing
            paint.setColor(mCircleColor);

            // Draw the circle in the center (shrinking)
            long endTime = SystemClock.elapsedRealtime();
            long elapsedMilliSeconds = endTime - mStartTime + 1;
            canvas.drawCircle(
                    canvas.getWidth() / 2,
                    canvas.getHeight() / 2,
                    (canvas.getHeight() / 4) - elapsedMilliSeconds / 50,
                    paint
            );

//            // Choose the brush color for drawing
//            paint.setColor(Color.argb(255,  249, 129, 0));
//
//            // Make the text a bit bigger
//            paint.setTextSize(45);
//
//            // Display the current fps on the screen
//            canvas.drawText("FPS:" + fps, 20, 40, paint);


            // Draw everything to the screen
            // and unlock the drawing surface
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    // If SimpleGameEngine Activity is paused/stopped
    // shutdown our thread.
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    // If SimpleGameEngine Activity is started theb
    // start our thread.
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
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
            if (timeThisFrame > 0) {
                fps = 1000 / timeThisFrame;
            }

        }

    }
}