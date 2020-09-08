package nl.orikami.sensingkitplugin.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import nl.orikami.sensingkitplugin.service.SensingKitService;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Utility methods belonging to notifications or notification sounds or vibrations.
 */
public class NotificationUtil {

    private final static String TAG = NotificationUtil.class.getSimpleName();

    /**
     * Create a notification to indicate that the sensingkit plugin is active and recording.
     * This notification is used to create a foreground service to make sure that the service is
     * kept active and not killed in the background.
     *
     * @param context Context to build the notification upon.
     * @return A notification ready to be shown to indicate the plugin is active.
     */
    public static Notification createNotification(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent notificationIntent = pm.getLaunchIntentForPackage(context.getPackageName());
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                0
        );

        Intent stopIntent = new Intent(context, SensingKitService.class);
        stopIntent.setAction(SensingKitService.STOP_ACTION);
        PendingIntent pStopIntent = PendingIntent.getService(
                context,
                0,
                stopIntent,
                0
        );

        String channelId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel(context);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Location Tracker")
                .setTicker("Location Tracker")
                .setContentText("Tracking your location")
                .setOngoing(true)
                .setSmallIcon(ResourceUtil.r(context, "mipmap", "ic_launcher"))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(android.R.drawable.ic_media_pause, "Stop", pStopIntent);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            notification.setCategory(Notification.CATEGORY_PROGRESS);
        }

        return notification.build();
    }

    /**
     * Create a notification channel to put a notification in. This is required starting in Android
     * O, because otherwise the notification is not shown.
     *
     * @param context Context used to create the channel.
     * @return A notification channel ready to be used to add notifications to.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String createNotificationChannel(Context context) {
        String channelId = "walking_test_service";
        String channelName = "Walking test experiment";
        NotificationChannel notificationChannel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationChannel.setShowBadge(true);
        notificationChannel.setSound(null, null);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                NOTIFICATION_SERVICE
        );
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }

        return channelId;
    }

    /**
     * Vibrate the phone
     *
     * @param context Context used to get vibration service.
     */
    public static void vibratePhone(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(1000);
        } else {
            // TODO Log to crashlytics or something
            Log.e(TAG, "Vibrator is null");
        }
    }

    /**
     * Play notification sound
     *
     * @param context Context used to play the sound.
     */
    public static void playNotificationSound(Context context) {
        final MediaPlayer mp = SoundUtil.createMediaPlayerFromRawFile(context, "skp_notification");
        mp.start();
    }

    /**
     * Check whether or not DND is enabled. If the Android version is lower than M (introduction of
     * DND) we assume that DND is not enabled. We assume the same if we can't get access to the
     * notification manager.
     *
     * @param context Context to get the notification manager from.
     * @return True if DND is enabled, false otherwise.
     */
    public static boolean isDndEnabled(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // DND has only been added in Android M, we assume it's false.
            return false;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                NOTIFICATION_SERVICE
        );
        if (notificationManager == null) {
            // Assume DND is not enabled when we can't get a notification manager.
            return false;
        }

        switch (notificationManager.getCurrentInterruptionFilter()) {
            case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                return true;
            case NotificationManager.INTERRUPTION_FILTER_ALL:
                return false;
            case NotificationManager.INTERRUPTION_FILTER_NONE:
                return true;
            case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                // TODO gotta check if we can get the right priority for our audio and alerts.
                return true;
            case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                // We don't actually know whether or not it's enabled.
                return false;
        }

        // Because the linter doesn't seem to realize that all possible cases are handled in the
        // switch case above.
        return false;
    }
}
