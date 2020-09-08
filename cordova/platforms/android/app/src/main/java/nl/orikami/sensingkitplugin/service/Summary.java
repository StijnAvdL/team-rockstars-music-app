package nl.orikami.sensingkitplugin.service;

import com.google.gson.Gson;

import nl.orikami.sensingkitplugin.util.ExperimentStatusEvent;

/**
 * A POJO representation of the current state of the service.
 */
public class Summary {
    private String experimentId;
    private boolean isSearchingGps;
    private float accuracy;
    private boolean isCountingDown;
    private float countdownRemaining;
    private boolean isRunning;
    private float runningTimeRemaining;
    private boolean isCompleted;
    private boolean isStopped;
    private boolean hasWarning;
    private float warningCode;
    private int stage;

    /**
     * Create a new instance of the summary based on the provided experiment id.
     *
     * @param experimentId The id for the new experiment.
     */
    public Summary(String experimentId) {
        this.experimentId = experimentId;
        this.stage = 1;
    }

    /**
     * Updates the internal summary representation of the current state of the service.
     *
     * @param event An event indicating changes in the state of the service.
     */
    public void updateSummary(ExperimentStatusEvent event) {
        switch (event.getExperimentStatus()) {
            case STARTED:
                break;
            case STARTED_NEXT_STAGE:
                stage += 1;
                break;
            case SEARCH_GPS:
                isSearchingGps = true;
                accuracy = event.getValue();
                break;
            case COUNTDOWN:
                isSearchingGps = false;
                accuracy = 0;
                isCountingDown = true;
                countdownRemaining = event.getValue();
                break;
            case RUNNING:
                isCountingDown = false;
                countdownRemaining = 0;
                isRunning = true;
                runningTimeRemaining = event.getValue();
                break;
            case COMPLETED:
                runningTimeRemaining = 0;
                isRunning = false;
                isCompleted = true;
                break;
            case STOPPED:
                isRunning = false;
                isStopped = true;
                break;
            case WARNING:
                hasWarning = true;
                warningCode = event.getValue();
                break;
        }
    }

    /**
     * Returns a JSON based representation of the current state of the summary.
     *
     * @return JSON representation of the summary.
     */
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
