package nl.orikami.sensingkitplugin.util;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Utility methods handling sound.
 */
public class SoundUtil {
    /**
     * Create a mediaplayer for a provided raw id.
     *
     * @param context Context from which the mediaplayer is made.
     * @param id      The id of a file in the raw resource directory, should be a sound file.
     * @return A mediaplayer ready to play the sound file based on the provided raw id.
     */
    public static MediaPlayer createMediaPlayerFromRawFile(Context context, String id) {
        return MediaPlayer.create(context, ResourceUtil.r(context, "raw", id));
    }
}
