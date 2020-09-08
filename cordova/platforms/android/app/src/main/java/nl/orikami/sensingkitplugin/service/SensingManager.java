package nl.orikami.sensingkitplugin.service;

import android.content.Context;
import android.util.Log;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKSensorType;
import org.sensingkit.sensingkitlib.SensingKitLib;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nl.orikami.sensingkitplugin.util.EventBusUtil;

/**
 * Created by frank on 14-12-17.
 */

public class SensingManager {

    private final static String TAG = SensingManager.class.getSimpleName();

    private final static int SENSOR_DELAY = 10000;

    private List<SensorHandler> sensorHandlers;

    private int stage;

    /**
     * Initialize the SensingManager. It iterates over all preferred sensors provided in sensorTypes
     * and adds those that are actually present and working to the list of sensors
     * {@link #sensorHandlers}.
     *
     * @param context             Context required to access sensingkit.
     * @param experimentDirectory Directory the sensor handlers should write the files to.
     * @param sensorTypes         List of SensingKit SensorTypes which should be used to collect
     *                            data from.
     * @throws SKException Thrown if data can't be accessed or if there was an issue with the
     *                     sensor.
     */
    public SensingManager(
            Context context, File experimentDirectory, List<SKSensorType> sensorTypes
    ) throws SKException {

        SensingKitLibInterface sensingKitLib = SensingKitLib.getSensingKitLib(context);

        sensorHandlers = new ArrayList<SensorHandler>();

        stage = 1;

        for (SKSensorType sensorType : sensorTypes) {
            // If the sensor isn't available on the device we can just move on to the next one
            // without adding it to the handler list.
            if (!sensingKitLib.isSensorAvailable(sensorType)) {
                Log.d(TAG, "Sensor not available: " + sensorType);
                continue;
            }

            sensorHandlers.add(new SensorHandler(
                    sensingKitLib,
                    sensorType,
                    experimentDirectory,
                    SENSOR_DELAY,
                    stage
            ));
        }

        for (SensorHandler sensorHandler : sensorHandlers) {
            sensorHandler.registerAndSubscribeToSensor();
        }
    }

    /**
     * Start all registered sensors.
     */
    public void start() {
        for (SensorHandler sensorHandler : sensorHandlers) {
            sensorHandler.start();
        }
    }

    /**
     * Stop all registered sensors.
     */
    public void stop() {
        for (SensorHandler sensorHandler : sensorHandlers) {
            sensorHandler.stop();
        }
    }

    /**
     * Close all registered sensors. Sensors should be stopped with {@link #stop()} before this is
     * called.
     */
    public void close() {
        for (SensorHandler sensorHandler : sensorHandlers) {
            sensorHandler.close();
        }
    }

    /**
     * Enable accuracy event updates. This iterates over all sensors to find the one that is
     * responsible for the location sensor and enables it for that specific sensor handler.
     *
     * @param sendAccuracyUpdates
     */
    public void sendAccuracyUpdates(boolean sendAccuracyUpdates) {
        for (SensorHandler sensorHandler : sensorHandlers) {
            if (sensorHandler.isLocationSensor()) {
                sensorHandler.sendAccuracyUpdates(sendAccuracyUpdates);
            }
        }
    }

    /**
     * Start a next stage. The sensing manager keeps track of the actual number of the stage and
     * this is also incremented here.
     */
    public void nextStage() {
        stage++;
        EventBusUtil.postStartedNextStage(stage);
        for (SensorHandler sensorHandler : sensorHandlers) {
            sensorHandler.setStage(stage);
        }
    }
}
