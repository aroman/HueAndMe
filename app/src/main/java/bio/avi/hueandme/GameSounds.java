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

    static final int MAX_SOUND_STREAMS = 10;

    private SoundPool mSoundPool;
    private int[] mSoundIds;

    public GameSounds(Context context) {
        initializeSoundPool();

        mSoundIds = new int[MAX_SOUND_STREAMS];
        mSoundIds[0] = mSoundPool.load(context, R.raw.zap, 1);
        mSoundIds[1] = mSoundPool.load(context, R.raw.fail, 1);
        mSoundIds[2] = mSoundPool.load(context, R.raw.timer_blip, 1);
        mSoundIds[3] = mSoundPool.load(context, R.raw.timer_expired, 1);
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
        mSoundPool.play(mSoundIds[soundId], 1, 1, 1, 0, 1.0f);
    }


    public void release() {
        mSoundPool.release();
    }

}
