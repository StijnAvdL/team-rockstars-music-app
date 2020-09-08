package nl.orikami.sensingkitplugin.util;

/**
 * Enum containing all possible errors types that can be returned by the service.
 */
public enum SKError {
    UNKNOWN(-1),
    NO_PERMISSION(1);

    private int value;
    SKError(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
