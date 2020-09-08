package nl.orikami.sensingkitplugin.util;

import android.content.Context;
import android.util.Log;

import org.sensingkit.sensingkitlib.SKException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility functions to zip data and related.
 * <p>
 * See https://stackoverflow.com/a/48598099/1557098
 */
public class ZipUtil {

    private final static String TAG = ZipUtil.class.getSimpleName();

    private static final int BUFFER = 2048;

    /**
     * Zips and removes a directory based on the provided experimentId and s3Prefix.
     *
     * @param context      Context used for zipping.
     * @param experimentId The id of the experiment that should be zipped.
     * @param s3Prefix     The bucket directory name where the experiment with the provided
     *                     id is stored, mirroring the s3 structure.
     */
    public static void zipAndRemoveSensingData(Context context, String experimentId,
                                               String s3Prefix) {
        try {
            // Get dir and create a zipfile
            File zipFile = FileUtil.getZipFile(experimentId);
            File directory = FileUtil.getExperimentDirectory(experimentId);
            ZipUtil.zipDirectory(context, directory, zipFile);

            // Remove the raw data that was just zipped
            FileUtil.removeExperimentDirectory(experimentId);
        } catch (SKException e) {
            e.printStackTrace();
        }
    }

    /**
     * Zip a provided directory into a provided zip file.
     *
     * @param context             Context used to let Android scan the zip file.
     * @param experimentDirectory The directory that should be zipped.
     * @param zipFile             The file the directory should be zipped into.
     */
    private static void zipDirectory(Context context, File experimentDirectory, File zipFile) {
        Log.d(TAG, "Zipping " + experimentDirectory.getName());
        try {
            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            zipDirectoryHelper(out, experimentDirectory, null, true);

            try {
                out.flush();
                dest.flush();
                out.close();
                dest.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Let Android know we created a new file, mostly used for when pulling files from the
            // phone through a pc.
            FileUtil.scanFile(context, zipFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * A helper function for the {@link #zipDirectory(Context, File, File)} method. Recursively
     * parses all subdirectories and zips them into the provided file. It also skips the root
     * directory when adding all files to the archive.
     *
     * @param zos                 The zip outputstream to which data is written.
     * @param input               The input file/directory that should be zipped.
     * @param parentDirectoryName The name of the parent directory, this is recursively updated to
     *                            make sure that the structure in the zip matches the structure in
     *                            the directory.
     * @param isRoot              Boolean to indicate whether the current directory is the root
     *                            directory, in which case this directory is not added to the
     *                            parentDirectoryName argument during recursion.
     */
    private static void zipDirectoryHelper(ZipOutputStream zos, File input,
                                           String parentDirectoryName, boolean isRoot) {
        if (input == null || !input.exists()) {
            return;
        }

        String zipEntryName = input.getName();
        if (parentDirectoryName != null && !parentDirectoryName.isEmpty()) {
            zipEntryName = parentDirectoryName + "/" + zipEntryName;
        } else if (isRoot && input.isDirectory()) {
            // If it's the root of the directory, in this case the dir with the experiment id, then
            // we want to skip it and just add the contents.
            zipEntryName = null;
        }

        if (input.isDirectory()) {
            Log.d(TAG, "+ " + zipEntryName);
            for (File file : input.listFiles()) {
                zipDirectoryHelper(zos, file, zipEntryName, false);
            }
        } else {
            try {
                Log.d(TAG, "    " + zipEntryName);
                byte buffer[] = new byte[BUFFER];
                FileInputStream fis = new FileInputStream(input);
                BufferedInputStream bis = new BufferedInputStream(fis, BUFFER);
                zos.putNextEntry(new ZipEntry(zipEntryName));
                int count;
                while ((count = bis.read(buffer, 0, BUFFER)) != -1) {
                    zos.write(buffer, 0, count);
                }
                zos.closeEntry();
                bis.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}