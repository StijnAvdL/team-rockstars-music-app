package nl.orikami.sensingkitplugin.util;

/**
 * All possible experiment statuses describing what the plugin is currently doing and used for
 * communication back to the application.
 */
public enum ExperimentStatus {
    STARTED,
    SEARCH_GPS,
    STARTED_NEXT_STAGE,
    COUNTDOWN,
    RUNNING,
    COMPLETED,
    STOPPED,
    WARNING,
    ERROR
}
