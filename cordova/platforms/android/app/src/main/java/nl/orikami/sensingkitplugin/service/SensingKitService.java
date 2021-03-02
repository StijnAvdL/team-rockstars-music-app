package nl.orikami.sensingkitplugin.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKSensorType;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import nl.orikami.sensingkitplugin.util.Constants;
import nl.orikami.sensingkitplugin.util.EventBusUtil;
import nl.orikami.sensingkitplugin.util.FileUtil;
import nl.orikami.sensingkitplugin.util.IntentUtil;
import nl.orikami.sensingkitplugin.util.NotificationUtil;
import nl.orikami.sensingkitplugin.util.SKError;
import nl.orikami.sensingkitplugin.util.SoundUtil;
import nl.orikami.sensingkitplugin.util.VolumeUtil;
import nl.orikami.sensingkitplugin.util.Warning;
import nl.orikami.sensingkitplugin.util.ZipUtil;
import nl.orikami.uploader.Upload;

/**
 * Created by Frank on 26-Mar-18.
 */

public class SensingKitService extends Service {

    private final static String TAG = SensingKitService.class.getSimpleName();

    private final static int COUNTDOWN_DURATION = 5000;
    private final static int COUNTDOWN_INTERVAL = 500;

    private final static int EXPERIMENT_DURATION = 120000;
    //    private final static int EXPERIMENT_DURATION = 7500;
    private final static int EXPERIMENT_INTERVAL = 500;

    private final static int SERVICE_STOP_DELAY = 500;

    public final static String START_ACTION = "start";
    public final static String NEXT_STAGE_ACTION = "next";
    public final static String STOP_ACTION = "stop";

    public final static String EXPERIMENT_ID_ARG = "experiment_id";
    public final static String S3_BUCKET_ARG = "s3_bucket";
    public final static String WALK_TEST_ARG = "start_walk_test";
    public final static String S3_PREFIX_ARG = "s3_prefix";
    public final static String SENSOR_TYPE_LIST_ARG = "sensor_type_list";
    public final static String URL_ARG = "url";

    // Default sensors that are used when collecting sensor data.
    public final static List<SKSensorType> DEFAULT_SENSOR_TYPES = Arrays.asList(
            SKSensorType.ACCELEROMETER, SKSensorType.GYROSCOPE, SKSensorType.MAGNETOMETER,
            SKSensorType.LOCATION, SKSensorType.ROTATION, SKSensorType.GRAVITY,
            SKSensorType.LINEAR_ACCELERATION, SKSensorType.BATTERY_STATUS, SKSensorType.STEP_DETECTOR,
            SKSensorType.STEP_COUNTER, SKSensorType.MOTION_ACTIVITY, SKSensorType.AUDIO_LEVEL
    );

    /**
     * The default value for originalMusicVolume while the volume has not been retrieved from the
     * system.
     */
    private final static int DEFAULT_VOLUME_VALUE = -1;

    private SummaryHandler summaryHandler;

    private boolean running;

    /**
     * Whether the current running service is used to do the walk test
     */
    private boolean walkTest;

    /**
     * Wether the walk test (countdown and actual walking) is currently ongoing, used
     * to make sure that no duplicate nextStage is started.
     */
    private boolean walkTestActive;

    /**
     * Whether the walk test has been successfully completed, used to know whether or not to clean
     * up
     */
    private boolean walkTestCompleted;

    private String experimentId;
    private String s3Prefix;
    private String s3Bucket;
    private String url;

    private PowerManager.WakeLock wakeLock;

    private SensingManager sensingManager;

    private MediaPlayer countdownMediaPlayer;

    private int originalMusicVolume = DEFAULT_VOLUME_VALUE;

    /**
     * Starting point for the service and handles what action should be taken based on the action
     * sent with the intent.
     *
     * @param intent  Intent containing relevant information for the service.
     * @param flags   Unused. Additional data.
     * @param startId Unused. Int representing this specific start.
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action == null) {
            throw new IllegalArgumentException("Action must not be null");
        }
        if (START_ACTION.equals(action)) {
            return start(intent);
        } else if (NEXT_STAGE_ACTION.equals(action)) {
            return nextStage();
        } else if (STOP_ACTION.equals(action)) {
            return stop();
        } else {
            throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    /**
     * Starts recording and storing sensors. The intent must contain the extra strings
     * {@link #EXPERIMENT_ID_ARG} and {@link #S3_BUCKET_ARG} to store the data in the appropriate
     * locations. EventBus is updated with a new event that the experiment has started.
     *
     * @param intent Intent containing information required to start the experiment.
     * @return Int flag to indicate start type.
     */
    private int start(Intent intent) {
        if (running) {
            EventBusUtil.postWarning(Warning.ALREADY_STARTED);
            return START_STICKY;
        }
        try {
            experimentId = IntentUtil.extractRequiredStringExtra(intent, EXPERIMENT_ID_ARG);
            s3Prefix = IntentUtil.extractRequiredStringExtra(intent, S3_PREFIX_ARG);
            s3Bucket = IntentUtil.extractRequiredStringExtra(intent, S3_BUCKET_ARG);
            url = IntentUtil.extractRequiredStringExtra(intent, URL_ARG);
            List<SKSensorType> sensorTypes = IntentUtil.extractSensorTypeExtras(
                    intent, SENSOR_TYPE_LIST_ARG, DEFAULT_SENSOR_TYPES
            );

            if (summaryHandler != null) {
                summaryHandler.stop();
            }
            summaryHandler = new SummaryHandler(this);
            summaryHandler.start(experimentId);

            if (NotificationUtil.isDndEnabled(this)) {
                EventBusUtil.postWarning(Warning.DND_IS_ENABLED);
            }

            File experimentDirectory = FileUtil.getExperimentDirectory(experimentId);

            walkTest = IntentUtil.extractBooleanExtra(intent, WALK_TEST_ARG);

            Notification notification = NotificationUtil.createNotification(this);
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);

            acquireWakeLock();

            if (sensingManager != null) {
                sensingManager.stop();
                sensingManager.close();
            }
            sensingManager = new SensingManager(this, experimentDirectory, sensorTypes);
            sensingManager.sendAccuracyUpdates(walkTest);
            sensingManager.start();

            running = true;
            EventBusUtil.postStarted();
        } catch (SKException e) {
            e.printStackTrace();
            EventBusUtil.postError(SKError.UNKNOWN);
        }

        return START_STICKY;
    }

    /**
     * Starts a next stage for the experiment. Depending on whether or not the walking test is
     * active it will also start a countdown followed by the actual 2mwt. It also sends feedback
     * back over EventBus to indicate a new stage has started.
     *
     * @return Int flag to indicate start type.
     */
    private int nextStage() {
        if (!running) {
            EventBusUtil.postWarning(Warning.NOT_STARTED);
            return START_STICKY;
        }
        if (sensingManager == null) {
            EventBusUtil.postError(SKError.UNKNOWN);
            return 0;
        }
        Log.d(TAG, "Starting next stage");
        if (walkTest) {
            if (!walkTestActive) {
                Log.d(TAG, "Next stage for walking test");
                startWalkTestCountdown();
                sensingManager.nextStage();
            }
        } else {
            sensingManager.nextStage();
        }
        return START_STICKY;
    }

    /**
     * Stops the current experiment. This stops all timers, sounds, the sensing manager, it zips
     * the data, removes original data, starts upload jobs and posts feedback via EventBus that it
     * has been completed (if 2mwt completed successfully) and stopped.
     *
     * @return Int flag to indicate start type.
     */
    private int stop() {
        Log.d(TAG, "Stop service");
        if (!running) {
            EventBusUtil.postWarning(Warning.NOT_STARTED);
            return START_STICKY;
        }

        running = false;

        // Stop media player and timers
        if (countdownMediaPlayer != null) {
            countdownMediaPlayer.stop();
        }
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        if (walkTestTimer != null) {
            walkTestTimer.cancel();
        }

        // Stop sensors
        if (sensingManager != null) {
            sensingManager.stop();
            sensingManager.close();
        }

        // TODO make variable to turn remove on and off
        ZipUtil.zipAndRemoveSensingData(this, experimentId, s3Prefix);

        try {
            File zipFile = FileUtil.getZipFile(experimentId);
            new Upload(zipFile, s3Bucket, s3Prefix + experimentId + ".zip", url).schedule(getApplicationContext());
        } catch (Exception error) {
            error.printStackTrace();
        }
        if (walkTest && walkTestCompleted) {
            EventBusUtil.postCompleted();
        }

        EventBusUtil.postStopped();

        if (summaryHandler != null) {
            summaryHandler.stop();
        }

        // A delay is required before the volume can be restored, as starting the mediaplayer for
        // the notification also has a slight delay. If we were to call the restoreVolume directly
        // here then the volume would be restored during or before the notification sound.
        // Unfortunately because we need a delay for the volume restore we also need to delay
        // shutting the service down.
        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (walkTest && originalMusicVolume != DEFAULT_VOLUME_VALUE) {
                    VolumeUtil.restoreVolume(SensingKitService.this, originalMusicVolume);
                }
                stopForeground(true);
                releaseWakeLock();
                stopSelf();
            }
        }, SERVICE_STOP_DELAY);

        // TODO Figure out what exactly to return.
        return START_STICKY;
    }

    /**
     * Starts the walking test countdown with counting down soudns and enables location updates for
     * the plugin.
     */
    private void startWalkTestCountdown() {
        Log.d(TAG, "Start walk test countdown");
        walkTestActive = true;
        sensingManager.sendAccuracyUpdates(false);

        originalMusicVolume = VolumeUtil.increaseVolume(this);

        countdownMediaPlayer = SoundUtil.createMediaPlayerFromRawFile(this, "skp_countdown");

        countdownMediaPlayer.start();
        countdownTimer.start();
    }

    /**
     * Start the walk test by starting a next phase for the experiment and notifying the user that
     * they should start walking.
     */
    private void startWalkTest() {
        Log.d(TAG, "Start walk test");

        sensingManager.nextStage();

        NotificationUtil.vibratePhone(this);
        NotificationUtil.playNotificationSound(this);

        EventBusUtil.postRunning(EXPERIMENT_DURATION * 1.0f / 1000);

        walkTestTimer.start();
    }

    /**
     * Called if the walktest has been completed successfully (2 minutes are over). This notifies
     * the user and stops the service.
     */
    private void walkTestCompleted() {
        Log.d(TAG, "Walk test completed");
        NotificationUtil.vibratePhone(this);
        NotificationUtil.playNotificationSound(this);
        walkTestCompleted = true;
        stop();
    }

    /**
     * Acquires a wakelock to make sure that the service keeps receiving the most accurate data and
     * there are no gaps in the data.
     */
    private void acquireWakeLock() {
        if ((wakeLock == null) || (!wakeLock.isHeld())) {
            PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (powerManager == null) return;
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sensingkit:WakeLock");
            wakeLock.acquire(300000);
        }
    }

    /**
     * Release the previously acquired wakelock so the phone can go back to sleep.
     */
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    /**
     * See {@link Service#onBind(Intent)}
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * A countdown timer used for the initial five second ({@link #COUNTDOWN_DURATION} countdown.
     * Each tick a new event is posted to EventBus which can be sent back through the Cordova
     * plugin. Once it has finished the actual 2mwt is automatically started.
     */
    private CountDownTimer countdownTimer = new CountDownTimer(COUNTDOWN_DURATION, COUNTDOWN_INTERVAL) {

        @Override
        public void onTick(long millisUntilFinished) {
            EventBusUtil.postCountdown(millisUntilFinished * 1.0f / 1000);
        }

        @Override
        public void onFinish() {
            startWalkTest();
        }
    };

    /**
     * A countdown timer used for the actual two minute ({@link #EXPERIMENT_DURATION}) walk test.
     * During each tick an event is posted indicating how long the event still takes and once it
     * has finished it is automatically stopped.
     */
    private CountDownTimer walkTestTimer = new CountDownTimer(EXPERIMENT_DURATION, EXPERIMENT_INTERVAL) {
        @Override
        public void onTick(long millisUntilFinished) {
            EventBusUtil.postRunning(millisUntilFinished * 1.0f / 1000);
        }

        @Override
        public void onFinish() {
            walkTestCompleted();
        }
    };
}
