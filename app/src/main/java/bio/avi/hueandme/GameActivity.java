package bio.avi.hueandme;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ViewSwitcher;


public class GameActivity extends AppCompatActivity {

    private GameSurfaceView mGameSurfaceView;
    private ViewSwitcher mGameViewSwitcher;

    private SensorManager mSensorManager;
    private SensorEngine mSensorEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        mGameSurfaceView = (GameSurfaceView) findViewById(R.id.game_surfaceview);

        if (mGameSurfaceView == null) {
            throw new IllegalStateException("game surface view not found");
        }

        mGameViewSwitcher = (ViewSwitcher) findViewById(R.id.game_viewswitcher);

        if (mGameViewSwitcher == null) {
            throw new IllegalStateException("game view switcher not found");
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorEngine = new SensorEngine(new SensorChangedListener() {
            @Override
            public void onSensorChanged(float[] rotationVector) {
                mGameSurfaceView.onRotationChange(rotationVector[1]);
            }
        });

    }

    public void switcheroo() {
        mGameViewSwitcher.showNext();
    }

    @Override
    protected void onStart() {
        Log.d("GameActivity", "onStart()");
        super.onResume();
    }

    @Override
    protected void onResume() {
        Log.d("GameActivity", "onResume()");
        super.onResume();

        mGameSurfaceView.resume();

        mSensorManager.registerListener(
                mSensorEngine,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_FASTEST
        );
    }

    @Override
    protected void onPause() {
        Log.d("HSV", "onPause()");
        super.onPause();

        mGameSurfaceView.pause();

        mSensorManager.unregisterListener(
                mSensorEngine,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        );
    }

}
