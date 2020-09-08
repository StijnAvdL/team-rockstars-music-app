package nl.orikami.sensingkitplugin.util;

/**
 * Enum containing all possible warning types that can be returned by the service.
 */
public enum Warning {
    DND_IS_ENABLED(1),
    ALREADY_STARTED(2),
    NOT_STARTED(3);

    private int value;

    Warning(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
