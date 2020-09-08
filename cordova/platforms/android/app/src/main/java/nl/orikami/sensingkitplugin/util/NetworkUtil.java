package nl.orikami.sensingkitplugin.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Utilities for getting network information.
 */
public class NetworkUtil {
    /**
     * Check whether there is a network available, be it wifi or mobile. It should be noted that
     * while a network may be available, this does not immediately mean that internet is also
     * available.
     *
     * @param context Context used to get the connectivity service system service.
     * @return True if a network is available, false otherwise.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
