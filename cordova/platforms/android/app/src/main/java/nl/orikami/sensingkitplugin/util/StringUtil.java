package nl.orikami.sensingkitplugin.util;

/**
 * Utilities to help with string handling and checking.
 */
public class StringUtil {
    /**
     * Checks whether a provided string is either null or empty.
     *
     * @param string String to check whether it is null or empty.
     * @return {@code true} if {@code string} is null or empty, {@code false} otherwise.
     */
    public static boolean isNullOrEmpty(String string) {
        return (string == null) || string.isEmpty();
    }
}
