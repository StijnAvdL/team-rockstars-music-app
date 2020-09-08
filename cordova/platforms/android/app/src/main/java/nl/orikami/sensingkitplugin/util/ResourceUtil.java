package nl.orikami.sensingkitplugin.util;

import android.content.Context;

/**
 * Utility methods for resource access.
 */
public class ResourceUtil {
    /**
     * Utility method to access resources. The default R.id.{...} does not work when running as a
     * Cordova plugin as the import turns invalid. This loads the resource programmatically.
     *
     * @param context Context used to get the resource and the package name.
     * @param type    The type of the resource.
     * @param id      The ID of the resource.
     * @return Returns an int referring to the requested resource if the parameters were valid.
     */
    public static int r(Context context, String type, String id) {
        return context.getApplicationContext().getResources().getIdentifier(
                id,
                type,
                context.getApplicationContext().getPackageName()
        );
    }
}
