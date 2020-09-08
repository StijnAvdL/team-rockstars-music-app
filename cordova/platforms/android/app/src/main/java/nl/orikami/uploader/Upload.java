package nl.orikami.uploader;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.State;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;

/**
 * Created by mark on 19/06/2018.
 */

public class Upload {
    private static final String TAG = Upload.class.getSimpleName();
    // Above this upload limit, will wait for UNMETERED network instead of just any network.
    public static final int MOBILE_UPLOAD_LIMIT = 5 * 1024 * 1024;

    public static boolean isFinished(File file) {
        return Upload.isFinished(file.getAbsolutePath());
    }

    public static boolean isFinished(String filename) {
        try {
            List<WorkStatus> status = WorkManager.getInstance().getStatusesByTag(filename).get();
            if(status == null || status.size() == 0) {
                return !new File(filename).exists();
            } else {
                return State.SUCCEEDED.equals(status.get(0).getState());
            }
        } catch(Exception exception){
            return !new File(filename).exists();
        }
    }

    private File file;
    private String bucket;
    private String key;

    public Upload(@NonNull File file, @NonNull String bucket, @NonNull String key) {
        this.file = file;
        this.bucket = bucket;
        this.key = key;
    }

    public UUID schedule(Context context) {
        // Constraints: Upload on network (small files) or WIFI (big files)
        NetworkType networkType = file.length() > MOBILE_UPLOAD_LIMIT ? NetworkType.UNMETERED : NetworkType.CONNECTED;
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(networkType)
                .build();

        // Put all arguments in an work INPUT
        final String filename = file.getAbsolutePath();
        Data input = new Data.Builder()
                .putString(UploadWorker.INPUT_FILENAME, filename)
                .putString(UploadWorker.INPUT_BUCKET, bucket)
                .putString(UploadWorker.INPUT_KEY, key)
                .build();

        final UploadLogger logger = new UploadLogger(context);
        final String tag = logger.getLogId(bucket, key, 0);

        // Create work request
        final OneTimeWorkRequest uploadWork = new OneTimeWorkRequest.Builder(UploadWorker.class)
                .setConstraints(constraints)
                .setInputData(input)
                .addTag(tag)
                .addTag(filename)
                .keepResultsForAtLeast(1, TimeUnit.DAYS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
                .build();

        // Execute - but execute only ONCE per file, keeping the existing upload that is already
        // in progress
        WorkManager.getInstance().beginUniqueWork(tag, ExistingWorkPolicy.KEEP, uploadWork)
                .enqueue();

        // Correct logs for scheduled/skipped
        WorkManager.getInstance().getStatusesByTag(tag).addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    List<WorkStatus> statuses = WorkManager.getInstance().getStatusesByTag(tag).get();
                    boolean exists = statuses.size() > 0 && statuses.get(0).getId().compareTo(uploadWork.getId()) != 0;
                    logger.logFile(exists ? "skipped" : "scheduled", "",bucket, key);
                } catch(Exception error){

                }
            }
        }, AsyncTask.THREAD_POOL_EXECUTOR);


        return uploadWork.getId();
    }

    public static class Logs {
        private String bucket;
        private String s3prefix;

        public Logs(@NonNull String bucket, @NonNull String s3prefix) {
            this.bucket =  bucket;
            this.s3prefix = s3prefix;
        }
        public void schedule(Context context) {
            UploadLogger logger = new UploadLogger(context);
            for(File file : logger.getFiles()) {
                String filename = file.getName();
                new Upload(file, bucket, s3prefix + filename).schedule(context);
            }
        }
    }
}
