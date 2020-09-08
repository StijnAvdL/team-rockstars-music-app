package nl.orikami.uploader;

/**
 * Created by mark on 11/07/2018.
 */

import android.content.SharedPreferences;
import android.util.Log;

import com.amazonaws.services.s3.model.PartETag;

import java.util.ArrayList;
import java.util.List;

/**
 * UploadState
 *
 * Persist the state relevant to resume a MultiPart upload:
 * - uploadId
 * - ETags of uploaded parts
 *
 * The rest is based on defaults:
 * - ETag part number is simply the index (starting from 1, as per AWS spec)
 * - File Offset is simply based on index * a fixed PART_SIZE (5 mb)
 *
 * When all parts are uploaded, the CompleteMultiPartUpload request can be done
 * with all ETags and the uploadId
 */
class UploadState {
    private static final String TAG = "UploadState";

    /**
     * Default PART_SIZE, also used to calculate file offset based on
     * number of parts already uploaded
     */
    public static final long PART_SIZE = 5 * 1024 * 1024; // Set part size to 5 MB.
    /**
     * We're using two keys per upload in the SharedPreferences
     * (figure at this level of complexity, SharedPreferences is still a valid choice
     *  any more fields, and we might need to switch to Room...)
     */
    private static String ETAG_KEY = ".etags";
    private static String UPLOAD_ID_KEY = ".id";

    /**
     * uploadId received from AWS after initiating the MultiPart upload
     * needed to upload parts and complete the multipart upload.
     *
     * so this will be saved in SharedPreferences in the setter
     */
    private String uploadId;
    /**
     * All the ETags of uploaded parts. Is returned from all individual upload request
     * and needed to complete the multipart upload
     */
    private List<PartETag> partETags = new ArrayList<PartETag>();

    /**
     * filename is used in SharedPreferences to uniquely identify the upload
     */
    private String filename = null;
    /**
     * SharedPreferences are used to persist state across job restarts.
     */
    private SharedPreferences prefs = null;

    /**
     * Initialize an UploadState
     *
     * Will query SharedPreferences to restore existing state.
     *
     * @param filename unique id of the upload, as identified by filename
     * @param prefs the SharedPreferences we're using to persist state
     */
    UploadState(String filename, SharedPreferences prefs) {
        this.filename = filename;
        this.prefs = prefs;

        // Restore uploadId (default to null)
        this.uploadId = prefs.getString(filename + UPLOAD_ID_KEY, null);

        // Restore ETags. These are stored as an csv of strings, so they
        // need to be converted back to proper PartETag.
        // (might be a poor man's serialization, but it'll work fine for this use case)
        String etags = prefs.getString(filename + ETAG_KEY, null);
        if (etags != null) {
            int i = 1;
            for (String etag : etags.split(",")) {
                partETags.add(new PartETag(i, etag));
                i++;
            }
        }
    }

    /**
     * When a new MultiPartUpload is started, this method will allow us to
     * saveEtag the state for subsequent jobs
     *
     * @param uploadId
     */
    public void saveUploadId(String uploadId) {
        this.uploadId = uploadId;
        prefs.edit().putString(filename+UPLOAD_ID_KEY, uploadId).apply();
    }

    /**
     * Return uploadId for all upload part requests and completion request.
     * @return
     */
    public String getUploadId() {
        return this.uploadId;
    }


    /**
     * After an upload of a part, saveEtag the ETag to persistent storage
     * so it can be restored when the job crashes and restarts.
     *
     * @param finishedETag
     */
    public void saveEtag(PartETag finishedETag) {
        partETags.add(finishedETag);

        // Persist
        StringBuilder etags = new StringBuilder(32 * partETags.size());
        for(PartETag etag : partETags) {
            etags.append(etag.getETag()).append(",");
        }
        etags.deleteCharAt(etags.lastIndexOf(","));
        Log.d(TAG,"saved etags: "+etags);
        prefs.edit().putString(filename+ETAG_KEY, etags.toString()).apply();
    }

    /**
     * Return ETags for the CompleteMultiPart request
     * @return
     */
    public List<PartETag> getPartETags() {
        return partETags;
    }

    /**
     * After all parts are uploaded, both file and state are deleted
     */
    public void delete() {
        prefs.edit().remove(filename+ETAG_KEY).remove(filename+UPLOAD_ID_KEY).apply();
    }

    /**
     * Part Number is based on the number of ETags collected + 1
     * ( + 1 to start with 1 instead of 0, per AWS spec)
     * @return
     */
    public int getPartNumber() {
        return partETags.size() + 1;
    }

    /**
     * Number of parts or chunks completed, as counted by the number of etags
     * @return
     */
    public int getCompleted() {
        return partETags.size();
    }

    /**
     * Figure out if we need to start a new MultiPartUpload or
     * if we can resume an older one
     * @return
     */
    public boolean hasUploadId() {
        return uploadId != null;
    }

    /**
     * Calculate file position based on default PART_SIZE and number of parts
     * already uploaded
     * @return
     */
    public long getFilePosition() {
        return this.partETags.size() * PART_SIZE;
    }

}