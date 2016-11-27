package bio.avi.hueandme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

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
        Log.d("MenuActivity", "Play game!");
        Intent intent = new Intent(this, GameActivity.class);
//        EditText editText = (EditText) findViewById(R.id.edit_message);
//        String message = editText.getText().toString();
//        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

}
