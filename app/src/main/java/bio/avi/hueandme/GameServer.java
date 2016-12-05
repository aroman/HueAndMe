package bio.avi.hueandme;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.ion.Ion;

/**
 * Created by avi on 12/4/16.
 */

public class GameServer {

    public static final String API_BASE = "http://hueandme.herokuapp.com/";

    public static Future<JsonObject> getScores(Context context) {
        return Ion.with(context)
                .load(API_BASE + "scores")
                .asJsonObject();
    }

    public static Future<JsonObject> submitScore(Context context, String name, int points) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("points", points);

        return Ion.with(context)
                .load(API_BASE + "scores")
                .setJsonObjectBody(json)
                .asJsonObject();
    }
}
