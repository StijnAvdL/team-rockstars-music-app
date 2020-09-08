package nl.orikami.sensingkitplugin.service;

import android.util.Log;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKSensorType;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;
import org.sensingkit.sensingkitlib.configuration.SKAccelerometerConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKGravityConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKGyroscopeConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKLinearAccelerationConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKLocationConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKMagnetometerConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKMotionActivityConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKRotationConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKStepCounterConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKStepDetectorConfiguration;

import java.io.File;

/**
 * Created by Frank on 13-Mar-18.
 */

public class SensorHandler {

    private final static String TAG = SensorHandler.class.getSimpleName();

    private SensingKitLibInterface sensingKitLib;
    private SKSensorType sensorType;
    private ModelWriter modelWriter;
    private int sensorDelay;

    /**
     * Initializes the SensorHandler with the provided information by creating the ModelWriters
     * and setting several local variables.
     *
     * @param sensingKitLib
     * @param sensorType
     * @param experimentDirectory
     * @param sensorDelay
     * @throws SKException
     */
    public SensorHandler(SensingKitLibInterface sensingKitLib,
                         SKSensorType sensorType, File experimentDirectory, int sensorDelay,
                         int stage
    ) throws SKException {
        this.sensingKitLib = sensingKitLib;
        this.sensorType = sensorType;
        this.sensorDelay = sensorDelay;

        if (sensorType == SKSensorType.LOCATION) {
            modelWriter = new LocationModelWriter(
                    sensorType, experimentDirectory, getFilename(), stage
            );
        } else {
            modelWriter = new ModelWriter(
                    sensorType, experimentDirectory, getFilename(), stage
            );
        }
    }

    /**
     * Safely registers a sensor with a specific configuration and starts listening with the
     * internal ModelWriter. For registering it first checks whether this sensor is already
     * registered and if it is it deregisters it and registers with the new configuration.
     */
    public void registerAndSubscribeToSensor() {
        try {
            if (sensingKitLib.isSensorRegistered(sensorType)) {
                sensingKitLib.deregisterSensor(sensorType);
            }
            sensingKitLib.registerSensor(sensorType, getConfiguration(sensorType));
        } catch (SKException e) {
            Log.d(TAG, "Could not safely register sensor " + sensorType);
            e.printStackTrace();
        }

        try {
            sensingKitLib.subscribeSensorDataListener(sensorType, modelWriter);
        } catch (SKException e) {
            Log.d(TAG, "Could not subscribe a listener to " + sensorType);
            e.printStackTrace();
        }
    }

    /**
     * Start sensing with the sensor.
     */
    public void start() {
        try {
            sensingKitLib.startContinuousSensingWithSensor(sensorType);
        } catch (SKException e) {
            Log.e(TAG, "Could not start continuous sensing with sensor for " + sensorType);
            e.printStackTrace();
        }
    }

    /**
     * Sets the next stage for the model writer.
     *
     * @param stage The number of the next stage.
     */
    public void setStage(int stage) {
        modelWriter.setStage(stage);
    }

    /**
     * Stop sensing with the sensor and flush any remaining data to the file.
     */
    public void stop() {
        try {
            sensingKitLib.stopContinuousSensingWithSensor(sensorType);
        } catch (SKException e) {
            Log.e(TAG, "Could not stop sensing for " + sensorType);
            e.printStackTrace();
        }

        try {
            modelWriter.flush();
        } catch (SKException e) {
            Log.e(TAG, "Could not flush for " + sensorType);
            e.printStackTrace();
        }
    }

    /**
     * Close the sensor by unsubscribing all listeners for the sensortype, deregistering the sensor
     * and closing the file.
     */
    public void close() {
        try {
            sensingKitLib.unsubscribeAllSensorDataListeners(sensorType);
        } catch (SKException e) {
            Log.e(TAG, "Could not unsubscribe all listeners from " + sensorType);
            e.printStackTrace();
        }

        try {
            sensingKitLib.deregisterSensor(sensorType);
        } catch (SKException e) {
            Log.e(TAG, "Could not deregister sensor for " + sensorType);
            e.printStackTrace();
        }

        try {
            modelWriter.close();
        } catch (SKException e) {
            Log.e(TAG, "Could not close ModelWriter for " + sensorType);
            e.printStackTrace();
        }
    }

    /**
     * Whether or not this sensor data is handling the location sensor.
     *
     * @return True if it is handling the location sensor and false otherwise.
     */
    public boolean isLocationSensor() {
        return sensorType == SKSensorType.LOCATION;
    }

    /**
     * Tells the LocationModelWriter to start sending accuracy updates. If this is called on a
     * handler that is currently not handling the location sensor it will throw an
     * IllegalArgumentException.
     *
     * @param sendAccuracyUpdates Thrown when this is called on a handler which is not handling the
     *                            locaction sensor.
     */
    public void sendAccuracyUpdates(boolean sendAccuracyUpdates) {
        if (sensorType != SKSensorType.LOCATION) {
            throw new IllegalArgumentException(
                    "Can not send accuracy updates from SKSensorType " + sensorType
            );
        }
        ((LocationModelWriter) modelWriter).sendAccuracyUpdates(sendAccuracyUpdates);
    }

    /**
     * Returns a configuration based on the provided sensorType. For all sensors except the
     * location sensor it uses the locally set sensorDelay.
     *
     * @param sensorType The sensor for which the configuration should be generated.
     * @return A configuration for the provided sensorType.
     */
    private SKConfiguration getConfiguration(SKSensorType sensorType) {
        switch (sensorType) {
            case ACCELEROMETER:
                SKAccelerometerConfiguration accelerometerConfiguration = new SKAccelerometerConfiguration();
                accelerometerConfiguration.setSamplingRate(sensorDelay);
                return accelerometerConfiguration;
            case AUDIO_LEVEL:
                // No config
                return null;
            case BATTERY:
                // No config
                return null;
            case ROTATION:
                SKRotationConfiguration rotationConfiguration = new SKRotationConfiguration();
                rotationConfiguration.setSamplingRate(sensorDelay);
                return rotationConfiguration;
            case STEP_DETECTOR:
                SKStepDetectorConfiguration stepDetectorConfiguration = new SKStepDetectorConfiguration();
                stepDetectorConfiguration.setSamplingRate(sensorDelay);
                return stepDetectorConfiguration;
            case GRAVITY:
                SKGravityConfiguration gravityConfiguration = new SKGravityConfiguration();
                gravityConfiguration.setSamplingRate(sensorDelay);
                return gravityConfiguration;
            case GYROSCOPE:
                SKGyroscopeConfiguration gyroscopeConfiguration = new SKGyroscopeConfiguration();
                gyroscopeConfiguration.setSamplingRate(sensorDelay);
                return gyroscopeConfiguration;
            case LINEAR_ACCELERATION:
                SKLinearAccelerationConfiguration linearAccelerationConfiguration = new SKLinearAccelerationConfiguration();
                linearAccelerationConfiguration.setSamplingRate(sensorDelay);
                return linearAccelerationConfiguration;
            case MAGNETOMETER:
                SKMagnetometerConfiguration magnetometerConfiguration = new SKMagnetometerConfiguration();
                magnetometerConfiguration.setSamplingRate(sensorDelay);
                return magnetometerConfiguration;
            case STEP_COUNTER:
                SKStepCounterConfiguration stepCounterConfiguration = new SKStepCounterConfiguration();
                stepCounterConfiguration.setSamplingRate(sensorDelay);
                return stepCounterConfiguration;
            case MOTION_ACTIVITY:
                SKMotionActivityConfiguration motionActivityConfiguration = new SKMotionActivityConfiguration();
                return motionActivityConfiguration;
            case LOCATION:
                SKLocationConfiguration locationConfiguration = new SKLocationConfiguration();
                locationConfiguration.setPriority(SKLocationConfiguration.Priority.HIGH_ACCURACY);
                locationConfiguration.setFastestInterval(10);
                locationConfiguration.setInterval(100);
                return locationConfiguration;
            default:
                throw new IllegalArgumentException("Unknown sensortype: " + sensorType);
        }
    }

    /**
     * Returns the filename to store the sensordata for the sensor that's handled.
     *
     * @return Filename on disk for sensordata.
     */
    private String getFilename() {
        switch (sensorType) {
            case ACCELEROMETER:
                return "accelerometer";
            case AUDIO_LEVEL:
                return "audioLevel";
            case BATTERY:
                return "battery";
            case ROTATION:
                return "rotation";
            case STEP_DETECTOR:
                return "stepDetector";
            case GRAVITY:
                return "gravity";
            case GYROSCOPE:
                return "gyroscope";
            case LINEAR_ACCELERATION:
                return "linearAcceleration";
            case MAGNETOMETER:
                return "magnetometer";
            case STEP_COUNTER:
                return "stepCounter";
            case LOCATION:
                return "location";
            case MOTION_ACTIVITY:
                return "motionActivity";
            default:
                throw new IllegalArgumentException("No filename available for " + sensorType);
        }
    }

    /**
     * Returns a string representation of this class based on the sensor type.
     *
     * @return String representation of this class.
     */
    @Override
    public String toString() {
        return sensorType.toString() + " handler";
    }
}
