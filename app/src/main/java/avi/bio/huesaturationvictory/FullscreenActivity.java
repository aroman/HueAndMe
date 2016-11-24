package avi.bio.huesaturationvictory;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private GameSurfaceView mSurfaceView;
    private SensorEngine mSensorEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HSV", "onCreate()");

        setContentView(R.layout.activity_fullscreen);

        mSurfaceView = (GameSurfaceView) findViewById(R.id.fullscreen_surface);

        if (mSurfaceView == null) {
            throw new IllegalStateException("SurfaceView not found");
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorEngine = new SensorEngine(new SensorChangedListener() {
            @Override
            public void onSensorChanged(float[] rotationVector) {
//                Log.d("HSV", String.format("%f, %f, %f", rotationVector[0], rotationVector[1], rotationVector[2]));
                mSurfaceView.setColorFromRotationVector(rotationVector);
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hide();
    }

    @Override
    protected void onStart() {
        Log.d("HSV", "onStart()");
        super.onResume();

        mSensorManager.registerListener(
                mSensorEngine,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_GAME
        );
    }

    @Override
    protected void onResume() {
        Log.d("HSV", "onResume()");
        super.onResume();

        // Tell the gameView resume method to execute
        mSurfaceView.resume();

        hide();
    }

    // This method executes when the player quits the game
    @Override
    protected void onPause() {
        Log.d("HSV", "onPause()");
        super.onPause();

        // TODO: Not working
        // Tell the gameView pause method to execute
//        mSurfaceView.pause();

        mSensorManager.unregisterListener(
                mSensorEngine,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        );
    }

    private void hide() {
        mSurfaceView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

}
