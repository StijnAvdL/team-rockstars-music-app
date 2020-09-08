package nl.orikami.uploader;

import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.UUID;

import androidx.work.State;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;

/**
 * Created by mark on 19/06/2018.
 */
public class UploadResult {
    private static final String TAG = UploadResult.class.getSimpleName();

    public UUID id;
    public String filename;
    public String bucket;
    public String key;
    public boolean success;

    UploadResult(UUID id, String filename, String bucket, String key, boolean success) {
        this.id = id;
        this.filename = filename;
        this.key = key;
        this.bucket = bucket;
        this.success = success;
    }
}
