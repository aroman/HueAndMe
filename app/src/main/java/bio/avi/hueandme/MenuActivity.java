package bio.avi.hueandme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    private UIButton mPlayButton;
    private UIButton mScoresButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_menu);

        mPlayButton = (UIButton) findViewById(R.id.play_button);
        mScoresButton = (UIButton) findViewById(R.id.scores_button);

        if (mPlayButton == null) {
            throw new IllegalStateException("play button not found");
        }

        if (mScoresButton == null) {
            throw new IllegalStateException("scores button not found");
        }
    }

    public void playGame(View view) {
        startActivity(new Intent(this, GameActivity.class));
    }

    public void showScores(View view) {
        startActivity(new Intent(this, ScoresActivity.class));
    }
}
