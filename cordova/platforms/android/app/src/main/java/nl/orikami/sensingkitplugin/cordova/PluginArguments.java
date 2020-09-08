package nl.orikami.sensingkitplugin.cordova;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 * The arguments that can be provided to the Cordova plugin.
 */
public class PluginArguments {
    public String id;
    @SerializedName("s3bucket")
    public String s3Bucket;
    @SerializedName("s3prefix")
    public String s3Prefix;
    public ArrayList<String> sensors = new ArrayList<String>();

    public PluginArguments(String id, String s3Bucket, String s3Prefix, ArrayList<String> sensors) {
        this.id = id;
        this.s3Bucket = s3Bucket;
        this.s3Prefix = s3Prefix;
        this.sensors = sensors;
    }

    @Override
    public String toString() {
        return "PluginArguments{" +
                "id='" + id + '\'' +
                ", s3Bucket='" + s3Bucket + '\'' +
                ", s3Prefix='" + s3Prefix + '\'' +
                ", sensors='" + sensors + '\'' +
                '}';
    }
}