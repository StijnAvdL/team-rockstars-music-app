package nl.orikami.sensingkitplugin.service;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKSensorType;
import org.sensingkit.sensingkitlib.data.SKLocationData;
import org.sensingkit.sensingkitlib.data.SKSensorData;

import java.io.File;

import nl.orikami.sensingkitplugin.util.EventBusUtil;

/**
 * Extended ModeLWriter ({@link ModelWriter}) which also supports sending location updates.
 */
public class LocationModelWriter extends ModelWriter {

    private final static String TAG = LocationModelWriter.class.getSimpleName();

    private boolean sendUpdates = false;

    public LocationModelWriter(SKSensorType sensorType, File directory, String filename, int stage) throws SKException {
        super(sensorType, directory, filename, stage);
    }

    /**
     * Extends the default data handling ({@link ModelWriter#onDataReceived(SKSensorType, SKSensorData)}
     * by also posting accuracy updates if required.
     *
     * @param skSensorType Sensor the data originates from.
     * @param skSensorData New sensor data.
     */
    @Override
    public void onDataReceived(SKSensorType skSensorType, SKSensorData skSensorData) {
        super.onDataReceived(skSensorType, skSensorData);
        SKLocationData skLocationData = (SKLocationData) skSensorData;
        if (sendUpdates) {
            EventBusUtil.postSearchingGps(skLocationData.getAccuracy());
        }
    }

    /**
     * Set whether or not accuracy updates should be sent when data is received in the callback.
     *
     * @param sendUpdates Whether or not updates should be sent.
     */
    public void sendAccuracyUpdates(boolean sendUpdates) {
        this.sendUpdates = sendUpdates;
    }
}
