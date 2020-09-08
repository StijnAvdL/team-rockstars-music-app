package nl.orikami.sensingkitplugin.util;

import org.greenrobot.eventbus.EventBus;

/**
 * A collection of methods to post experiment status events on EventBus for communication with the
 * application that is using this plugin.
 * <p>
 * If it's running as a plugin and not as native app then events are received in
 * {@link nl.orikami.sensingkitplugin.cordova.SensingKitPlugin#onExperimentStatusEvent(ExperimentStatusEvent)}
 * and then sent back through callbackContext to Cordova.
 */
public class EventBusUtil {

    /**
     * Post an experiment status event on EventBus that the plugin has started.
     */
    public static void postStarted() {
        postEvent(new ExperimentStatusEvent.ExperimentStatusEventBuilder(
                ExperimentStatus.STARTED
        ).build());
    }

    /**
     * Post an experiment status event on EventBus that the plugin is tracking the accuracy of the
     * GPS signal.
     *
     * @param value The last received GPS accuracy.
     */
    public static void postSearchingGps(float value) {
        postEvent(new ExperimentStatusEvent.ExperimentStatusEventBuilder(
                ExperimentStatus.SEARCH_GPS
        ).value(value).build());
    }

    /**
     * Post an experiment status event on EventBus that the plugin has started a next stage.
     */
    public static void postStartedNextStage(float stage) {
        postEvent(new ExperimentStatusEvent.ExperimentStatusEventBuilder(
                ExperimentStatus.STARTED_NEXT_STAGE
        ).value(stage).build());
    }

    /**
     * Post an experiment status event on EventBus that the plugin is doing a countdown with the
     * amount of time left.
     *
     * @param value Time remaining in seconds for the countdown.
     */
    public static void postCountdown(float value) {
        postEvent(new ExperimentStatusEvent.ExperimentStatusEventBuilder(
                ExperimentStatus.COUNTDOWN
        ).value(value).build());
    }

    /**
     * Post an experiment status event on EventBus that the 2mwt is active with the amount of time
     * remaining in seconds.
     *
     * @param value The amount of time remaining in seconds for the 2mwt.
     */
    public static void postRunning(float value) {
        postEvent(new ExperimentStatusEvent.ExperimentStatusEventBuilder(
                ExperimentStatus.RUNNING
        ).value(value).build());
    }

    /**
     * Post an experiment status event on EventBus that the 2mwt has completed successfully.
     */
    public static void postCompleted() {
        postEvent(new ExperimentStatusEvent.ExperimentStatusEventBuilder(
                ExperimentStatus.COMPLETED
        ).build());
    }

    /**
     * Post an experiment status event on EventBus that the plugin has stopped.
     */
    public static void postStopped() {
        postEvent(new ExperimentStatusEvent.ExperimentStatusEventBuilder(
                ExperimentStatus.STOPPED
        ).build());
    }

    /**
     * Post an warning status event on EventBus where the value indicates the type of warning.
     *
     * @param warning Warning enum indicating the type of error.
     */
    public static void postWarning(Warning warning) {
        postEvent(new ExperimentStatusEvent.ExperimentStatusEventBuilder(
                ExperimentStatus.WARNING
        ).value(warning.getValue()).build());
    }

    /**
     * Post an error status event on EventBus where the value indicates the type of error.
     *
     * @param error SKError enum indicating the type of error.
     */
    public static void postError(SKError error) {
        postEvent(new ExperimentStatusEvent.ExperimentStatusEventBuilder(
                ExperimentStatus.ERROR
        ).value(error.getValue()).build());
    }

    /**
     * Post a provided experiment status event on EventBus as a sticky event.
     *
     * @param experimentStatusEvent The plugin status event that should be posted on EventBus.
     */
    private static void postEvent(ExperimentStatusEvent experimentStatusEvent) {
        EventBus.getDefault().postSticky(
                experimentStatusEvent
        );
    }
}
