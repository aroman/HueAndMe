package bio.avi.hueandme;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

/**
 * Created by avi on 11/27/16.
 */

public class GameSounds {

    private static final int MAX_SOUND_STREAMS = 10;

    private SoundPool mSoundPool;
    private int[] mSoundIds;

    public static final int SOUND_ZAP = 0;
    public static final int SOUND_FAIL = 1;
    public static final int SOUND_TIMER_BLIP = 2;
    public static final int SOUND_TIMER_END = 3;

    public GameSounds(Context context) {
        initializeSoundPool();

        mSoundIds = new int[MAX_SOUND_STREAMS];
        mSoundIds[SOUND_ZAP] = mSoundPool.load(context, R.raw.zap, 1);
        mSoundIds[SOUND_FAIL] = mSoundPool.load(context, R.raw.fail, 1);
        mSoundIds[SOUND_TIMER_BLIP] = mSoundPool.load(context, R.raw.timer_blip, 1);
        mSoundIds[SOUND_TIMER_END] = mSoundPool.load(context, R.raw.timer_expired, 1);
    }

    private void initializeSoundPool() {
        // Initialize SoundPool, call specific dependent on SDK Version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d("Sound", "Initialize Recent API SoundPool.");

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mSoundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .setMaxStreams(MAX_SOUND_STREAMS)
                    .build();
        }
        else {
            Log.d("Sound", "Initialize Old API SoundPool.");
            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
    }

    public void play(int soundId) {
        // the fail sound file was recorded with low dB, so we make it louder than the other sounds
        // to compensate.
        if (soundId == SOUND_FAIL) {
            mSoundPool.play(mSoundIds[soundId], 1f, 1f, 1, 0, 1.0f);
        }
        else {
            mSoundPool.play(mSoundIds[soundId], 0.25f, 0.25f, 1, 0, 1.0f);
        }
    }

}
