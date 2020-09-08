package nl.orikami.sensingkitplugin.util;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import java.lang.Math;

/**
 * Utility methods to control volume.
 */
public class VolumeUtil {

    private final static String TAG = VolumeUtil.class.getSimpleName();

    /**
     * Increase the volume of the music stream to maximum and return the original volume for later
     * restoration.
     *
     * @param context Context to get audio manager from.
     * @return The original volume of the music stream.
     * @throws IllegalArgumentException Throws if the audiomanager is null.
     */
    public static int increaseVolume(Context context) throws IllegalArgumentException {
        // TODO DND can currently not be disabled, this requires additional permissions,
        // TODO See https://stackoverflow.com/questions/39151453/in-android-7-api-level-24-my-app-is-not-allowed-to-mute-phone-set-ringer-mode/39152607
        // Get the audio manager and store the original music volume so we can restore this later
        // after the experiment is over
        AudioManager audioManager = (AudioManager) context.getSystemService(
                Context.AUDIO_SERVICE
        );
        if (audioManager == null) {
            // TODO Log the current situation to crashlytics or whatever else is in use to figure
            // TODO out exactly why audiomanager is null
            throw new IllegalArgumentException("AudioManager is null");
        }
        int originalMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        // Find the max volume for the music stream, which is the stream MediaPlayer uses and set
        // this as the device volume
        int maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int adjustedMusicVolume = (int) Math.floor(maxMusicVolume*0.75);
        try {
            audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    adjustedMusicVolume,
                    0
            );
        } catch (SecurityException securityException) {
            Log.d(TAG, "Tried to change volume while DND was enabled.");
        }

        return originalMusicVolume;
    }

    /**
     * Restores the volume to the provided music volume.
     *
     * @param context             Context to get the audio manager from.
     * @param originalMusicVolume The volume that the music stream should be set to.
     */
    public static void restoreVolume(Context context, int originalMusicVolume) {
        // Get the audio manager and restore the previously saved original music volume from
        // before the experiment began
        AudioManager audioManager = (AudioManager) context.getSystemService(
                Context.AUDIO_SERVICE
        );
        if (audioManager == null) {
            return;
        }
        try {
            audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    originalMusicVolume,
                    0
            );
        } catch (SecurityException securityException) {
            Log.d(TAG, "Tried to change volume while DND was enabled.");
        }
    }
}
