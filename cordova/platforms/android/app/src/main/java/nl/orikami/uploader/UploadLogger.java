package nl.orikami.uploader;
import android.content.Context;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class UploadLogger {
    private static String VERSION = "1.0.6";
    private static String TAG = "UploadLogger";
    private static String header = "timestamp,uuid,event,bucket,key,part,message,hasUploadId,fileAttempts,completed,total,chunksHaveAttemptsLeft,fileSize,etag,chunkAttempts,isRunning,taskId,background,network,version\n";

    private Context context;
    private ConnectivityManager connectivityManager = null;


    public UploadLogger(Context context) {
        this.context = context;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private void log(String event, String message, String bucket, String key, String part, String hasUploadId,
             String fileAttempts,  String completed, String total, String fileSize, String taskId) {

        String network = "none";
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
            network = "wifi";
        } else if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) {
            network = "cellular";
        }

        String timestamp = DateFormat.format("yyyy-MM-dd HH:mm:ss.SSS", new java.util.Date()).toString();
        String uuid = context == null ? "unknown" : Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String line = String.format(Locale.getDefault(),
                "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                timestamp,
                uuid,
                event,
                bucket,
                key,
                part,
                message.replace(",",";").replace("\n",""),
                hasUploadId,
                fileAttempts,
                completed,
                total,
                "", //chunks have attempts left - iOS only
                fileSize,
                "", // etag - iOS only
                "", // chunk attempts - iOS only
                "", // is running - iOS only
                taskId,
                taskId.length() > 0 ? "true" : "false", //background; when run in UploadWorker
                network, //network
                VERSION
        );

        File logFile = getLogFile();
        try {
            boolean exists = logFile.exists();
            if (!exists) {
                new File(logFile.getParent()).mkdirs();
                logFile.createNewFile();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, exists));
            if (!exists) {
                writer.append(header);
            }
            writer.append(line);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String event, String message) {
        String icon = event.equals("error") || event.equals("warning") || event.equals("failed") ? "🔴" : "⚪";
        Log.i(TAG, String.format(">%s %s\t%s", icon, event, message));

        log(event, message, "", "", "", "", "", "" , "", "", "");
    }

    public void logFile(String event, String message, String bucket, String key) {
        String icon = event.equals("error") || event.equals("warning") || event.equals("failed") ? "🔴" : "🔷";
        Log.i(TAG, String.format(">%s %s\t%s\t%s", icon, getLogId(bucket, key, 0 ), event, message));

        log(event, message, bucket, key, "0", "false", "0", "0" , "", "", "");
    }

    public void logFile(String event, String message, UploadWorker job) {
        String icon = event.equals("error") || event.equals("warning") || event.equals("failed") ? "🔴" : "🔷";
        Log.i(TAG, String.format(">%s %s\t%s\t%s", icon, getLogId(job.bucket, job.key, 0 ), event, message));

        log(event, message, job.bucket, job.key, String.valueOf(0),
                job.state.hasUploadId() ? "true" : "false",
                String.valueOf(job.getRunAttemptCount()), String.valueOf(job.state.getCompleted()), String.valueOf(job.parts),
                String.valueOf(job.fileSize), job.getId().toString());

    }

    public void logChunk(String event, String message, UploadWorker job) {
        String icon = event.equals("error") || event.equals("warning") || event.equals("failed") ? "🔴" : "🔶";
        Log.i(TAG, String.format(">%s %s\t%s\t%s", icon, getLogId(job.bucket, job.key, job.state.getPartNumber()), event, message));

        log(event, message, job.bucket, job.key, String.valueOf(job.state.getPartNumber()),
                job.state.hasUploadId() ? "true" : "false",
                String.valueOf(job.getRunAttemptCount()), String.valueOf(job.state.getCompleted()), String.valueOf(job.parts),
                String.valueOf(job.fileSize), job.getId().toString());

    }

    public String getLogId(String bucket, String key, int partNumber) {
        String id = String.format(Locale.getDefault(), "%s_%s_%d", bucket.replace("/","_"), key.replace("/","_"), partNumber);
        return id.substring(id.length() - 30, id.length());
    }

    private File getLogFile() {
        String filename = String.format("uploader-%s.log", DateFormat.format("yyyy-MM-dd", new java.util.Date()).toString());
        File logFile = new File(context.getExternalCacheDir().toString() + "/uploader/", filename);
        return logFile;
    }

    public File[] getFiles() {
        File logDir = new File(context.getExternalCacheDir().toString() + "/uploader/");
        final File today = getLogFile();
        if(!today.exists()) return new File[0];

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.equals(today.getName());
            }
        };
        File[] yesterdayFiles = logDir.listFiles(filter);
        ArrayList<File> files = new ArrayList<File>(yesterdayFiles.length);
        files.addAll(Arrays.asList(yesterdayFiles));

        // Copy today's log to a temporary directory
        File logTmpDir = new File(context.getExternalCacheDir().toString() + "/uploader-tmp/");
        logTmpDir.mkdirs();
        File todayCopy = new File(logTmpDir, today.getName());
        try {
            copy(today, todayCopy);
            files.add(todayCopy);
        } catch(IOException error) {
            error.printStackTrace();
        }

        File[] result = new File[files.size()];
        return files.toArray(result);
    }

    private void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }
}
