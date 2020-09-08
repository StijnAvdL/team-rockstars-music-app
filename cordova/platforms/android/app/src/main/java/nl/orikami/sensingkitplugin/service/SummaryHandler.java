package nl.orikami.sensingkitplugin.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import nl.orikami.sensingkitplugin.util.ExperimentStatusEvent;

/**
 * Keeps track of any experiment status events that are sent and uses these to update an internal
 * representation of the current state of the service.
 */
public class SummaryHandler {

    private final static String TAG = SummaryHandler.class.getSimpleName();

    private final static String SUMMARY_KEY = "summary";

    private Context context;

    private Summary summary;

    /**
     * Construct a new summary handler.
     *
     * @param context The context that will be used to access the shared preferences.
     */
    public SummaryHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Start keeping track of all experiment status events that are sent. Additionally this wipes
     * any remaining summary in the cache.
     *
     * @param experimentId The id for the experiment that is going to be tracked.
     */
    public void start(String experimentId) {
        wipeCache();
        summary = new Summary(experimentId);
        EventBus.getDefault().register(this);
    }

    /**
     * Stop tracking experiment status events and write the current summary to cache.
     */
    public void stop() {
        EventBus.getDefault().unregister(this);
        storeToCache();
    }

    /**
     * Store the current summary in shared preferences.
     */
    private void storeToCache() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(SUMMARY_KEY, summary.toString()).apply();
    }

    /**
     * Clear any existing summary from shared preferences.
     */
    private void wipeCache() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(SUMMARY_KEY).apply();
    }

    /**
     * Handle any experiment status event that is published through eventbus. Each time a new event
     * is received the summary representation is updated and the current stated is cached.
     *
     * @param event The new event that should be used to update the summary.
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onExperimentStatusEvent(ExperimentStatusEvent event) {
        if (summary == null) {
            throw new NullPointerException("summary object is null but should not be.");
        }
        summary.updateSummary(event);
        storeToCache();
        Log.d(TAG, "Summary: " + summary);
    }

    /**
     * Try and load a summary from the shared preferences cache. This returns null if no summary is
     * cached.
     *
     * @param context The context to access shared preferences on.
     * @return The cached summary or null if no summary was present.
     */
    public static Summary getSummaryFromCache(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String summaryJson = prefs.getString(SUMMARY_KEY, null);
        try {
            return new Gson().fromJson(summaryJson, Summary.class);
        } catch (JsonSyntaxException ex) {
            Log.d(TAG, "Could not parse the stored json summary data: " + summaryJson);
            return null;
        }
    }
}
