package nl.orikami.sensingkitplugin.util;

import android.content.Intent;

import org.sensingkit.sensingkitlib.SKSensorType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Methods to safely get information from intents.
 */
public class IntentUtil {

    /**
     * Get a boolean from a provided intent based on the provided string argument.
     *
     * @param intent   The intent from which the information should be extracted.
     * @param argument The key under which the information is stored in the intent.
     * @return The value which was stored under the provided argument in the provided intent. If
     * none were found then false is returned as default.
     */
    public static boolean extractBooleanExtra(Intent intent, String argument) {
        return intent.getBooleanExtra(argument, false);
    }

    /**
     * Get a string from a provided intent based on the argument. The provided argument should be
     * present in the intent and the string should not be empty. If the key is not present or
     * contains a null or empty value then an {@link IllegalArgumentException} is thrown.
     *
     * @param intent   The intent from which the information should be extracted.
     * @param argument The key under which the information is stored in the intent.
     * @return The value which was stored under the provided argument in the provided intent. If
     * the key was not present in the intent or the value was null or empty an
     * {@link IllegalArgumentException} is thrown.
     */
    public static String extractRequiredStringExtra(Intent intent, String argument) {
        if (intent.getExtras() == null) {
            throw new IllegalArgumentException("Extras must not be null");
        }
        String extra = intent.getStringExtra(argument);
        if (extra == null || extra.isEmpty()) {
            throw new IllegalArgumentException(
                    "Extra for argument \"" + argument + "\" must not be: " + extra
            );
        }
        return extra;
    }

    /**
     * Extract {@link SKSensorType} values from a String array
     * @param intent       The intent from which the information should be extracted.
     * @param argument     The key under which the information is stored in the intent.
     * @param defaultValue The default sensors that should be returned if none can be retrieved from
     *                     the intent.
     * @return List of {@link SKSensorType}s present in the provided intent.
     */
    public static List<SKSensorType> extractSensorTypeExtras(Intent intent, String argument, List<SKSensorType> defaultValue) {
        if (intent.getExtras() == null) {
            return defaultValue;
        }
        List<String> extras = intent.getStringArrayListExtra(argument);
        if (extras == null || extras.isEmpty()) {
            return defaultValue;
        }
        // Convert the string based sensor types to SKSensorTypes.
        List<SKSensorType> sensorTypes = new ArrayList<>();
        for (String type : extras) {
            sensorTypes.add(stringToSensorType(type));
        }
        return sensorTypes;
    }

    /**
     * Converts a string to the corresponding {@link SKSensorType}.
     * @param type String that should be converted to SKSensorType.
     * @return The SKSensorType that corresponds to the provided string.
     */
    private static SKSensorType stringToSensorType(String type) {
        switch(type) {
            case "accelerometer":
                return SKSensorType.ACCELEROMETER;
            case "gyroscope":
                return SKSensorType.GYROSCOPE;
            case "magnetometer":
                return SKSensorType.MAGNETOMETER;
            case "location":
                return SKSensorType.LOCATION;
            case "rotation":
                return SKSensorType.ROTATION;
            case "gravity":
                return SKSensorType.GRAVITY;
            case "linear_acceleration":
                return SKSensorType.LINEAR_ACCELERATION;
            case "battery":
                return SKSensorType.BATTERY_STATUS;
            case "step_detector":
                return SKSensorType.STEP_DETECTOR;
            case "step_counter":
                return SKSensorType.STEP_COUNTER;
            case "motion_activity":
                return SKSensorType.MOTION_ACTIVITY;
            case "audio_level":
                return SKSensorType.AUDIO_LEVEL;
            default:
                throw new IllegalArgumentException("Unknown sensor type: " + type);
        }
    }
}
