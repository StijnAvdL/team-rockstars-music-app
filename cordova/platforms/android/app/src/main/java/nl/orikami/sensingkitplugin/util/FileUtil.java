package nl.orikami.sensingkitplugin.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKExceptionErrorCode;

import java.io.File;
import java.io.IOException;

/**
 * Several utility methods to centralize data and file handling.
 */
public class FileUtil {

    private final static String TAG = FileUtil.class.getSimpleName();

    /**
     * Returns a File pointing to a directory based on the provided experiment id and bucket
     * directory name. The directories are created if they do not exist yet.
     * <p>
     * In the format of ${application_data_dir}/${experimentId}
     *
     * @param experimentId The id of the experiment and the name of the directory.
     * @return A file pointing to a directory with the name of the experiment id within a directory
     * with the name of the bucket directory name.
     * @throws SKException Thrown if the directory does not exist and could not be created.
     */
    public static File getExperimentDirectory(final String experimentId) throws SKException {
        File applicationDirectory = getApplicationDirectory();
        File experimentDirectory = new File(applicationDirectory, experimentId);

        if (!experimentDirectory.exists()) {
            if (!experimentDirectory.mkdirs()) {
                throw new SKException(
                        TAG,
                        "Directory could not be created: " + experimentDirectory,
                        SKExceptionErrorCode.UNKNOWN_ERROR
                );
            }
        }

        return experimentDirectory;
    }

    /**
     * Returns the application directory used for storing SensingKit plugin data.
     *
     * @return A file pointing to the application data directory.
     * @throws SKException Thrown if the directory does not exist and could not be created.
     */
    public static File getApplicationDirectory() throws SKException {
        // Create App folder: SensingData
        File applicationDirectory = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/SensingData/"
        );
        if (!applicationDirectory.exists()) {
            if (!applicationDirectory.mkdir()) {
                throw new SKException(
                        TAG,
                        "Directory could not be created.",
                        SKExceptionErrorCode.UNKNOWN_ERROR
                );
            }
        }
        return applicationDirectory;
    }

    /**
     * Returns a new zip file with the same name as the provided experiment id with the .zip suffix.
     * It will be located in the provided bucket directory. The file is still empty and is used to
     * write the actual contents to.
     *
     * @param experimentId Name for the zip file, which should be based on an existing data
     *                     directory.
     * @return A file pointing to a zip file within the bucket directory with the same name as the
     * experiment id.
     * @throws SKException Thrown if anything goes wrong finding the bucket directory.
     */
    public static File getZipFile(String experimentId) throws SKException {
        File applicationDirectory = getApplicationDirectory();
        return new File(applicationDirectory, experimentId + ".zip");
    }


    /**
     * Removes the directory with the provided directory name. This uses a helper functio
     * {@link #removeExperimentDirectoryHelper(File)} to recursively go through all the
     * subdirectories and removes the contents.
     *
     * @param experimentId The id for the experiment that should be removed.
     * @throws SKException Thrown if it could not get the experiment directory or if the dir could
     *                     not be removed.
     */
    public static void removeExperimentDirectory(String experimentId)
            throws SKException {
        File experimentDirectory = getExperimentDirectory(experimentId);
        removeExperimentDirectoryHelper(experimentDirectory);
    }

    /**
     * A helper function for {@link #removeExperimentDirectory(String)} which recursively
     * deletes all contents from the directory and its subdirectories.
     *
     * @param directory The file or directory that should be removed.
     */
    private static void removeExperimentDirectoryHelper(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                removeExperimentDirectoryHelper(file);
            } else {
                if (!file.delete()) {
                    Log.d(TAG, "Could not delete file: " + file);
                }
            }
        }
        if (!directory.delete()) {
            Log.d(TAG, "Could not delete directory: " + directory);
        }
    }

    /**
     * Get the file pointing to a specific experiment file belonging to a sensor.
     *
     * @param directory The directory in which the file should be created.
     * @param stage     The current stage of the experiment.
     * @param filename  The name of the file.
     * @return File pointing to a sensor file in the provided directory and stage.
     * @throws SKException Thrown if anything goes wrong getting the experiment directory or when
     *                     creating the file.
     */
    public static File getExperimentFile(File directory, int stage, String filename)
            throws SKException {
        File stageDirectory = FileUtil.getExperimentStageDirectory(directory, stage);
        File file = new File(stageDirectory, filename + ".csv");
        try {
            if (!file.createNewFile()) {
                throw new SKException(
                        TAG,
                        "File could not be created.",
                        SKExceptionErrorCode.UNKNOWN_ERROR
                );
            }
        } catch (IOException ex) {
            throw new SKException(
                    TAG,
                    ex.getMessage(),
                    SKExceptionErrorCode.UNKNOWN_ERROR
            );
        }
        return file;
    }

    /**
     * Create a directory belonging to a specific experiment stage in the provided directory.
     *
     * @param directory       The directory in which the stage directory should be created.
     * @param experimentStage The current stage of the experiment.
     * @return Return a file pointing to a directory representing the current stage.
     * @throws SKException Throws if the directory could not be created.
     */
    public static File getExperimentStageDirectory(File directory, int experimentStage)
            throws SKException {
        File experimentStageDirectory = new File(directory, "stage_" + experimentStage);
        if (!experimentStageDirectory.exists()) {
            if (!experimentStageDirectory.mkdirs()) {
                throw new SKException(
                        TAG,
                        "Directory could not be created.",
                        SKExceptionErrorCode.UNKNOWN_ERROR
                );
            }
        }
        return experimentStageDirectory;
    }

    /**
     * Remove a zip file based on the provided experiment id and bucket directory name.
     *
     * @param experimentId The name of the zip file that should be removed.
     * @throws SKException
     */
    public static void removeZipFile(String experimentId)
            throws SKException {
        File zipFile = getZipFile(experimentId);
        if (zipFile.exists()) {
            if (!zipFile.delete()) {
                throw new SKException(
                        TAG,
                        "Could not delete existing zipfile: " + zipFile,
                        SKExceptionErrorCode.UNKNOWN_ERROR
                );
            }
        }
    }

    /**
     * Let Android know that a file has been added. It's been moved here from file creation
     * because otherwise you'd end up seeing empty files in explorer
     *
     * @param context Context for the media scanner.
     * @param file    The file that should be scanned.
     */
    public static void scanFile(Context context, File file) {
        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri fileContentUri = Uri.fromFile(file);
        mediaScannerIntent.setData(fileContentUri);
        context.sendBroadcast(mediaScannerIntent);
    }
}
