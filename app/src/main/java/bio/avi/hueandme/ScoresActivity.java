package bio.avi.hueandme;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

public class ScoresActivity extends AppCompatActivity implements FutureCallback<JsonObject> {

    private final int BETWEEN_SCORE_MARGIN = 20;

    private LinearLayout mScoresLayout;
    private LinearLayout.LayoutParams mLabelLayoutParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        mScoresLayout = (LinearLayout) findViewById(R.id.scores_layout);
        updateScores();

        mLabelLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        mLabelLayoutParams.setMargins(0, BETWEEN_SCORE_MARGIN, 0, BETWEEN_SCORE_MARGIN);
    }

    private void updateScores() {
        GameServer
                .getScores(getApplicationContext())
                .setCallback(this);
    }

    @Override
    public void onCompleted(Exception e, JsonObject result) {
        if (e != null) {
            TextView errorTextView = new TextView(getApplicationContext());
            errorTextView.setText(e.getMessage());
            mScoresLayout.addView(errorTextView);
            return;
        }

        JsonArray scores = result.getAsJsonArray("scores");
        mScoresLayout.removeAllViews();
        for (int i = 0; i < scores.size(); i++) {
            JsonObject score = scores.get(i).getAsJsonObject();
            String name = score.get("name").getAsString();
            int points = score.get("points").getAsInt();
            UILabel scoreLabel = new UILabel(getApplicationContext());

            // Show the top scores larger than the others
            if (i == 0) scoreLabel.setTextSize(160);
            else if (i == 1) scoreLabel.setTextSize(140);
            else if (i == 2) scoreLabel.setTextSize(120);
            else scoreLabel.setTextSize(100);

            scoreLabel.setLayoutParams(mLabelLayoutParams);

            scoreLabel.setText(String.format("#%d %s (%d points)", i + 1, name, points));
            mScoresLayout.addView(scoreLabel);
        }
    }

}
