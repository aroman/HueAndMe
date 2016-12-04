package bio.avi.hueandme;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class GameActivity extends AppCompatActivity implements GameStateChangedListener {

    private GameState mGameState;
    private GameEngine mGameEngine;

    private UILabel mGamePostRoundScoreLabel;
    private FrameLayout mGameFramelayout;
    private LinearLayout mPostRoundView;
    private GameSurface mGameSurface;
    private UIButton mReplayButton;

    private SensorManager mSensorManager;
    private SensorEngine mSensorEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        mGameSurface = (GameSurface) findViewById(R.id.game_surfaceview);

        if (mGameSurface == null) {
            throw new IllegalStateException("game_surfaceview view not found");
        }

        mPostRoundView = (LinearLayout) findViewById(R.id.game_postround);

        if (mPostRoundView == null) {
            throw new IllegalStateException("post_round_layout not found");
        }

        mGameFramelayout = (FrameLayout) findViewById(R.id.game_framelayout);

        if (mGameFramelayout == null) {
            throw new IllegalStateException("game_framelayout not found");
        }

        mGamePostRoundScoreLabel = (UILabel) findViewById(R.id.game_postround_label_score);

        if (mGamePostRoundScoreLabel == null) {
            throw new IllegalStateException("game_postround_label_score not found");
        }

        mReplayButton = (UIButton) findViewById(R.id.replay_button);

        if (mReplayButton == null) {
            throw new IllegalStateException("replay_button not found");
        }

        mReplayButton.setOnTapListener(new UIButton.OnTapListener() {
            @Override
            public void onTap() {
                mGameEngine.startNewRound();
            }
        });

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorEngine = new SensorEngine(new SensorChangedListener() {
            @Override
            public void onSensorChanged(float[] rotationVector) {
                mGameEngine.onRotationChange(rotationVector[1]);
            }
        });

        mGameState = new GameState();

        mGameEngine = new GameEngine(getApplicationContext(), new GameState(mGameState));
        mGameSurface.setGameState(new GameState(mGameState));

        mGameEngine.addListener(this);

        switchToSurface();
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
        mGameEngine.resume();
        resumeGame();

    }

    private void pauseGame() {
        mGameSurface.pause();

        mSensorManager.unregisterListener(
                mSensorEngine,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        );
    }

    private void resumeGame() {
        mGameSurface.resume();

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
        mGameEngine.pause();
        pauseGame();
    }

    @Override
    public void onGameStateChanged(GameState newState) {
        GameState prevState = mGameState;
        mGameState = newState;

        mGameSurface.setGameState(mGameState);

        if (prevState.status != newState.status) {
            updateActiveView();
        }
    }

    private void updateActiveView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mGameState.status == GameStatus.POST_ROUND) {
                    switchToPostRound();
                }
                else if (mGameState.status == GameStatus.IN_ROUND) {
                    switchToSurface();
                }
            }
        });
    }

    private void switchToPostRound() {
        String labelText = getResources().getString(R.string.label_postround_score, mGameState.points);
        mGamePostRoundScoreLabel.setText(labelText);
        mGameFramelayout.bringChildToFront(mPostRoundView);
        mPostRoundView.setVisibility(View.VISIBLE);
        mGameSurface.setVisibility(View.GONE);
        pauseGame();
    }

    private void switchToSurface() {
        mGameFramelayout.bringChildToFront(mGameSurface);
        mPostRoundView.setVisibility(View.GONE);
        mGameSurface.setVisibility(View.VISIBLE);
        resumeGame();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGameEngine.onTap();
        return super.onTouchEvent(event);
    }

}
