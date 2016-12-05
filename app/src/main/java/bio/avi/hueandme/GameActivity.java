package bio.avi.hueandme;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

public class GameActivity extends AppCompatActivity implements GameStateChangedListener {

    // The maximum length a user's name can be for the high scores.
    private static final int MAX_NAME_LENGTH = 6;

    private GameState mGameState;
    private GameEngine mGameEngine;

    private UILabel mGamePostRoundScoreLabel;
    private FrameLayout mGameFramelayout;
    private LinearLayout mPostRoundView;
    private GameSurface mGameSurface;

    private SensorManager mSensorManager;
    private SensorEngine mSensorEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);

        mGameSurface = (GameSurface) findViewByIdOrDie(R.id.game_surfaceview);
        mPostRoundView = (LinearLayout) findViewByIdOrDie(R.id.game_postround);
        mGameFramelayout = (FrameLayout) findViewByIdOrDie(R.id.game_framelayout);
        mGamePostRoundScoreLabel = (UILabel) findViewByIdOrDie(R.id.game_postround_label_score);

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

    /**
     * Find and returns the view with the given id, or throws an IllegalStateException.
     * This is just a convenience method to make sanity-checking in onCreate() more DRY.
     * @param id the id of the view to find in the current context
     * @return the view from the current context, if it exists
     */
    private View findViewByIdOrDie(int id) {
        View view = findViewById(id);

        if (view == null) {
            throw new IllegalStateException(String.format("view with id %d not found", id));
        }

        return view;
    }

    public void saveScore(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.save_score_alert_title, MAX_NAME_LENGTH));

        final EditText input = new EditText(this);
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_NAME_LENGTH)});

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        builder.setView(input);

        builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                FutureCallback SubmitScoreCallback = new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) {
                            Toast.makeText(
                                    getApplication(),
                                    String.format("Error saving score: %s", e.getMessage()),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(
                                getApplication(),
                                "Score uploaded!",
                                Toast.LENGTH_SHORT).show();
                    }
                };

                GameServer.submitScore(
                        getApplicationContext(),
                        input.getText().toString(),
                        mGameState.points)
                        .setCallback(SubmitScoreCallback);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    public void restartGame(View view) {
        mGameEngine.startNewRound();
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
