package nl.orikami.sensingkitplugin.service;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKExceptionErrorCode;
import org.sensingkit.sensingkitlib.SKSensorDataHandler;
import org.sensingkit.sensingkitlib.SKSensorType;
import org.sensingkit.sensingkitlib.data.SKAccelerometerData;
import org.sensingkit.sensingkitlib.data.SKAudioLevelData;
import org.sensingkit.sensingkitlib.data.SKBatteryStatusData;
import org.sensingkit.sensingkitlib.data.SKGravityData;
import org.sensingkit.sensingkitlib.data.SKGyroscopeData;
import org.sensingkit.sensingkitlib.data.SKLinearAccelerationData;
import org.sensingkit.sensingkitlib.data.SKLocationData;
import org.sensingkit.sensingkitlib.data.SKMagnetometerData;
import org.sensingkit.sensingkitlib.data.SKMotionActivityData;
import org.sensingkit.sensingkitlib.data.SKRotationData;
import org.sensingkit.sensingkitlib.data.SKSensorData;
import org.sensingkit.sensingkitlib.data.SKStepCounterData;
import org.sensingkit.sensingkitlib.data.SKStepDetectorData;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import nl.orikami.sensingkitplugin.util.FileUtil;

/**
 * Handles data callbacks from a single sensor and writes the data to a file if possible.
 * Multiple ModelWriters have to be created if data should be received from multiple sensors.
 */
public class ModelWriter implements SKSensorDataHandler {

    private final static String TAG = ModelWriter.class.getSimpleName();

    private final SKSensorType sensorType;

    private String filename;

    private File directory;

    private File file;

    private BufferedOutputStream outputStream;

    private boolean canWrite;

    public ModelWriter(SKSensorType sensorType, File directory, String filename, int stage)
            throws SKException {
        this.sensorType = sensorType;
        this.directory = directory;
        this.filename = filename;
        setStage(stage);
    }

    /**
     * Receives new sensor data for a specific
     *
     * @param skSensorType
     * @param skSensorData
     */
    @Override
    public void onDataReceived(SKSensorType skSensorType, SKSensorData skSensorData) {
        String line = skSensorData.getDataInCSV() + "\n";
        try {
            if (canWrite) {
                outputStream.write(line.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set a new stage to write data to. The provided int indicates what the number of the new
     * stage should be. If a new one is started then writing data to files is temporarily put on
     * hold by setting the {@link #canWrite} field to false while a new output file is created.
     *
     * @param stage Number of the new stage.
     */
    public void setStage(int stage) {
        if (outputStream != null) {
            try {
                canWrite = false;
                outputStream.flush();
                outputStream.close();

                // Currently disabled because we won't need to access the raw files via pc
//                FileUtil.scanFile(context, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            file = FileUtil.getExperimentFile(directory, stage, filename);
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
            writeHeader();
            canWrite = true;
        } catch (SKException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Flush data for the currently active file. Should be called before {@link #close()} to make
     * sure any data in the buffer is written to the file before it is closed.
     *
     * @throws SKException Thrown if an IOException occurred while flushing data to the file.
     */
    public void flush() throws SKException {
        try {
            outputStream.flush();
        } catch (IOException ex) {
            throw new SKException(TAG, ex.getMessage(), SKExceptionErrorCode.UNKNOWN_ERROR);
        }
    }

    public void close() throws SKException {
        try {
            outputStream.close();
        } catch (IOException ex) {
            throw new SKException(TAG, ex.getMessage(), SKExceptionErrorCode.UNKNOWN_ERROR);
        }
    }

    /**
     * Writes the header for the relevant sensor to the active file.
     *
     * @throws SKException If the header could not be written.
     */
    private void writeHeader() throws SKException {
        try {
            outputStream.write(getHeader().getBytes());
        } catch (IOException e) {
            throw new SKException(
                    TAG,
                    "Header could not be written.",
                    SKExceptionErrorCode.UNKNOWN_ERROR
            );
        }
    }

    /**
     * Get the header in string format for the currently selected accelerometer that is sending
     * callbacks to this class.
     *
     * @return The header for the sensor in string format.
     */
    private String getHeader() {
        switch (sensorType) {
            case ACCELEROMETER:
                return SKAccelerometerData.csvHeader() + "\n";
            case AUDIO_LEVEL:
                return SKAudioLevelData.csvHeader() + "\n";
            case BATTERY_STATUS:
                return SKBatteryStatusData.csvHeader() + "\n";
            case ROTATION:
                return SKRotationData.csvHeader() + "\n";
            case STEP_DETECTOR:
                return SKStepDetectorData.csvHeader() + "\n";
            case LOCATION:
                return SKLocationData.csvHeader() + "\n";
            case GRAVITY:
                return SKGravityData.csvHeader() + "\n";
            case GYROSCOPE:
                return SKGyroscopeData.csvHeader() + "\n";
            case LINEAR_ACCELERATION:
                return SKLinearAccelerationData.csvHeader() + "\n";
            case MAGNETOMETER:
                return SKMagnetometerData.csvHeader() + "\n";
            case STEP_COUNTER:
                return SKStepCounterData.csvHeader() + "\n";
            case MOTION_ACTIVITY:
                return SKMotionActivityData.csvHeader() + "\n";
            default:
                throw new IllegalArgumentException("Unknown sensortype: " + sensorType);
        }
    }
}

