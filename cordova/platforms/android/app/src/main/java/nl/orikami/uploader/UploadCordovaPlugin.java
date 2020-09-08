package nl.orikami.uploader;

import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class UploadCordovaPlugin extends CordovaPlugin {

    private final static String TAG = UploadCordovaPlugin.class.getSimpleName();

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        
        if (action.equals("uploadLogs")) {
            String bucket = args.getString(0);
            String s3prefix = args.getString(1);
            new Upload.Logs(bucket, s3prefix).schedule(cordova.getContext());
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            callbackContext.sendPluginResult(pluginResult);            
            return true;
        } else if(action.equals("uploadTest")) {
            String bucket = args.getString(0);
            String s3prefix = args.getString(1);
            int mb = args.getInt(2);
            upload(bucket, s3prefix, mb);
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            callbackContext.sendPluginResult(pluginResult);
            return true;
        }
        return false;
    }

    protected void upload(String bucket, String s3prefix, int mb) {
        int bytes = mb * 1024 * 1024;

        String filename = DateFormat.format("yyyy-MM-dd-HHmmss", new java.util.Date()) + ".txt";

        File dir = cordova.getContext().getExternalCacheDir();
        try {
            File file = new File(dir, filename);
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            for(int i = 0; i < bytes; i += 10) {
                writer.write("0123456789");
            }
            writer.close();
            new Upload(file, bucket,String.format("%s%s", s3prefix, filename))
                    .schedule(cordova.getContext());

        } catch(IOException error) {
            error.printStackTrace();
        }
    }
}
