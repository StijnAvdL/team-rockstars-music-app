package nl.orikami.uploader;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Locale;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Created by mark on 15/05/2018.
 */

public class UploadWorker extends Worker {
    private static final String TAG = "UploadWorker";
    private class NoWifiException extends Exception {
        String message;
        public NoWifiException(String message)
        {
            super(message);
            this.message = message;
        }
    }

//    private static final String S3_IDENTITY_POOL_ID = "eu-west-1:c8f1d436-7c67-4d12-a8bc-a87e9f5f66ef";
//    private static final Regions S3_REGION = Regions.EU_WEST_1;
    private static final String S3_IDENTITY_POOL_ID = "eu-central-1:3d51f3b8-f5ea-4275-8dc8-01310d71233a";
    private static final Regions S3_REGION = Regions.EU_CENTRAL_1;
    private static final int MAX_ATTEMPTS = 10;

    static final String INPUT_FILENAME = "file";
    static final String INPUT_BUCKET = "bucket";
    static final String INPUT_KEY = "key";

    // Fetch all the input
    public long fileSize;
    public int parts;
    public String bucket;
    public String key;

    // Additional variables
    public File file;
    public UploadState state = null;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Execute the upload
     * (done in the worker)
     *
     * If file does not exist, will FAIL
     * If more than max attempts (per chunk), will FAIL
     * if file exists but upload fails, will RETRY
     * if upload succeeds, will SUCCEED
     *
     * @return
     */
    @NonNull
    @Override
    public Result doWork() {

        // Fetch all the input
        Data input = getInputData();
        String filename = input.getString(INPUT_FILENAME);
        bucket = input.getString(INPUT_BUCKET);
        key = input.getString(INPUT_KEY);

        // Retrieve state
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("uploads",Context.MODE_PRIVATE);
        state = new UploadState(filename, prefs);

        // Initialize additional variables
        file = new File(filename);
        fileSize = file.length();
        parts = (int) Math.ceil((double)fileSize / (double)UploadState.PART_SIZE);

        // Set this as output, so we can use this in the UI or for stats or whatever
        setOutputData(input);

        // FAIL if file does not exist
        if(!file.exists()) {
            logFile("failed", "file does not exist: "+filename);
            EventBus.getDefault().post(new UploadResult(getId(), filename, bucket, key,false));
            return Result.FAILURE;
        }

        // FAIL if file is empty
        if(fileSize == 0) {
            logFile("failed", "file is empty");
            EventBus.getDefault().post(new UploadResult(getId(), filename, bucket, key,false));
            return Result.FAILURE;
        }

        // FAIL if more than max attempts
        if(getRunAttemptCount() > MAX_ATTEMPTS * parts) {
            logFile("failed", String.format(Locale.getDefault(), "More than %d attempts in total (%d for every chunk)", MAX_ATTEMPTS * parts, MAX_ATTEMPTS));
            EventBus.getDefault().post(new UploadResult(getId(), filename, bucket, key,false));
            return Result.FAILURE;
        }

        // Start

        // Then, execute the upload
        final AmazonS3Client s3 = getS3Client();

        // The actual low-level multipart upload is adapted from:
        // https://docs.aws.amazon.com/AmazonS3/latest/dev/llJavaUploadFile.html
        try {
            // Initiate the multipart upload.
            if(!state.hasUploadId()) {
                InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucket, key);
                InitiateMultipartUploadResult initResponse = s3.initiateMultipartUpload(initRequest);
                state.saveUploadId(initResponse.getUploadId());
                logFile("started", state.getUploadId());
            } else {
                logFile("resume", "");
                logChunk("resume", "");
            }

            // Upload the file parts.
            while(state.getFilePosition() < fileSize) {
                if (fileSize > Upload.MOBILE_UPLOAD_LIMIT && !hasWifi()) {
                    throw new NoWifiException("no wifi available to upload big file");
                }

                // Because the last part could be less than 5 MB, adjust the part size as needed.
                long partSize = Math.min(UploadState.PART_SIZE, (fileSize - state.getFilePosition()));

                // Create the request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(bucket)
                        .withKey(key)
                        .withUploadId(state.getUploadId())
                        .withPartNumber(state.getPartNumber())
                        .withFileOffset(state.getFilePosition())
                        .withFile(file)
                        .withPartSize(partSize);

                // Upload the part and add the response's ETag to our list.
                logChunk("chunk-started","");
                UploadPartResult uploadResult = s3.uploadPart(uploadRequest);

                //  Log completion before incrementing the numbers (on saveEtag)
                logChunk("chunk-completed",uploadResult.getPartETag().getETag());
                state.saveEtag(uploadResult.getPartETag());
            }

            // Complete the multipart upload.
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucket, key,
                    state.getUploadId(), state.getPartETags());
            CompleteMultipartUploadResult result = s3.completeMultipartUpload(compRequest);

            // Yes, we did it!
            logFile("completed", result.getLocation());

            // Delete that stuff
            file.delete();
            state.delete();

            // Log warning if file could not be deleted (acceptence test criteria)
            if (file.exists()) {
                logFile("error", "could not delete file");
            }

            // Let others know we did it (Android EventBus)
            EventBus.getDefault().post(new UploadResult(getId(), filename, bucket, key ,true));
            return Result.SUCCESS;
        }
        catch(AmazonClientException e) {
            e.printStackTrace();

            // Uh-oh, we failed. Let's try again another time.
            logFile("warning", e.getMessage());
            return Result.RETRY;
        }
        catch(NoWifiException e){
            e.printStackTrace();

            // Uh-oh, we failed, let's try again another time.
            logFile("warning","no wifi to upload big file");
            return Result.RETRY;
        }
        catch(Exception e) {
            logFile("warning",String.format("unexpected exception: %s", e.getMessage()));
            return Result.RETRY;
        }
    }

    @Override
    public void onStopped(boolean cancelled) {
        super.onStopped(cancelled);
        if(cancelled) {
            final String filename = getInputData().getString(INPUT_FILENAME);
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("uploads",Context.MODE_PRIVATE);
            UploadState state = new UploadState(filename, prefs);
            state.delete();
        }
    }

    private ConnectivityManager connectivityManager = null;
    private boolean hasWifi() {
        if(connectivityManager == null) {
            connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
    }

    private UploadLogger logger = null;
    private UploadLogger getLogger() {
        if(logger == null) {
            logger = new UploadLogger(getApplicationContext());
        }
        return logger;
    }
    private void logFile(String event, String message) {
        getLogger().logFile(event, message, this);
    }
    private void logChunk(String event, String message) {
        getLogger().logChunk(event, message, this);
    }

    private AmazonS3Client s3 = null;
    /**
     * Private method to get and configure the AWS SDK S3 Client
     * @return AmazonS3Client
     */
    private AmazonS3Client getS3Client() {
        if(s3 != null) {
            return s3;
        }
        Context context = getApplicationContext();
        CognitoCachingCredentialsProvider creds = new CognitoCachingCredentialsProvider(
                context,
                S3_IDENTITY_POOL_ID,
                S3_REGION);

        s3 = new AmazonS3Client(creds);
        s3.setRegion(Region.getRegion(S3_REGION));
        return s3;
    }
}
