package nl.orikami.sensingkitplugin.util;

/**
 * Previously defined events that are used for communication with the application running the
 * plugin. experimentStatus is used to indicate what the plugin is doing, while value is used
 * for any corresponding value that might be required to indicate state.
 * <p>
 * These status events are created and posted in {@link EventBusUtil}.
 */
public class ExperimentStatusEvent {

    private ExperimentStatus experimentStatus;
    private float value;

    private ExperimentStatusEvent(ExperimentStatus experimentStatus, float value) {
        this.experimentStatus = experimentStatus;
        this.value = value;
    }

    public ExperimentStatus getExperimentStatus() {
        return experimentStatus;
    }

    public float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ExperimentStatusEvent{" +
                "experimentStatus=" + experimentStatus +
                ", value=" + value +
                '}';
    }

    /**
     * Builder pattern used to create {@link ExperimentStatusEvent} instances.
     */
    public static class ExperimentStatusEventBuilder {
        private ExperimentStatus experimentStatus;
        private float value;

        public ExperimentStatusEventBuilder(ExperimentStatus experimentStatus) {
            this.experimentStatus = experimentStatus;
        }

        public ExperimentStatusEventBuilder value(float value) {
            this.value = value;
            return this;
        }

        public ExperimentStatusEvent build() {
            return new ExperimentStatusEvent(experimentStatus, value);
        }
    }
}
