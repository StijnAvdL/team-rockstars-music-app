package nl.orikami.sensingkitplugin.cordova;

import android.Manifest;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import org.sensingkit.sensingkitlib.SKSensorType;

import nl.orikami.sensingkitplugin.service.SensingKitService;
import nl.orikami.sensingkitplugin.service.Summary;
import nl.orikami.sensingkitplugin.service.SummaryHandler;
import nl.orikami.sensingkitplugin.util.ExperimentStatus;
import nl.orikami.sensingkitplugin.util.ExperimentStatusEvent;
import nl.orikami.sensingkitplugin.util.SKError;
import nl.orikami.sensingkitplugin.util.IntentUtil;

/**
 * Created by Orikami on 03-Jan-18.
 */

public class SensingKitPlugin extends CordovaPlugin {

    private final static String TAG = SensingKitPlugin.class.getSimpleName();

    private CallbackContext callbackContext;
    private Gson gson;
    private Intent intent = null;

    public static final int SENSINGKIT_REQUEST = 2;

    private String[] PERMISSIONS; 

    /**
     * Initializes the plugin, which in this case is only initialization of gson.
     *
     * @param cordova Application's Cordova instance.
     * @param webView Application's WebView instance.
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        gson = new Gson();
    }

    /**
     * Called when the activity this plugin is connected to becomes visible, this registers the
     * EventBus for callbacks.
     */
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    /**
     * Called when the activity this plugin is connected to is no longer visible, this unregisters
     * the EventBus so events are no longer received.
     */
    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * Receives events sent by the application using the plugin and performs specific tasks based
     * on the actions and arguments. The current options are:
     * 1. Subscribe to updates.
     * 2. Starting the sensingkit plugin.
     * 3. Starting the sensingkit plugin for use in the 2mwt, this also enables accuracy callbacks.
     * 4. Start a next stage, if this is used while the plugin is used for the 2mwt then a 5 sec
     * countdown is started after which the actual 2mwt starts. Afterwards the plugin is
     * automatically stopped and data is uploaded.
     * 5. Stopping the sensingkit plugin, this also uploads any collected data.
     * <p>
     * On subscribe this method also stores the reference to the callback context for later use in
     * the events received by EventBus.
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments, wrapped with some Cordova helpers.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return True if the  action was valid, false otherwise.
     * @throws JSONException If there was an issue parsing json.
     */
    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Log.d(TAG, "SensingKit plugin execute action: " + action);
        Log.d(TAG, "Args: " + args);

        if (action.equals("subscribe")) {
            this.callbackContext = callbackContext;
            // Summary summary = SummaryHandler.getSummaryFromCache(cordova.getContext()); // Cordova 7.1.x
            Summary summary = SummaryHandler.getSummaryFromCache(cordova.getActivity().getApplicationContext()); // Cordova 6.4.x
            if (summary != null) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, summary.toString());
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
            return true;
        }

        intent = new Intent(cordova.getActivity(), SensingKitService.class);

        if (action.equals("start")) {
            PluginArguments arguments = extractArguments(args);
            Log.d(TAG, "Plugin arguments: " + arguments);
            intent.setAction(SensingKitService.START_ACTION);
            intent.putExtra(SensingKitService.EXPERIMENT_ID_ARG, arguments.id);
            intent.putExtra(SensingKitService.S3_BUCKET_ARG, arguments.s3Bucket);
            intent.putExtra(SensingKitService.S3_PREFIX_ARG, arguments.s3Prefix);
            intent.putExtra(SensingKitService.SENSOR_TYPE_LIST_ARG, arguments.sensors);
        } else if (action.equals("startWalkingExperiment")) {
            PluginArguments arguments = extractArguments(args);
            Log.d(TAG, "Plugin arguments: " + arguments);
            intent.setAction(SensingKitService.START_ACTION);
            intent.putExtra(SensingKitService.EXPERIMENT_ID_ARG, arguments.id);
            intent.putExtra(SensingKitService.S3_BUCKET_ARG, arguments.s3Bucket);
            intent.putExtra(SensingKitService.S3_PREFIX_ARG, arguments.s3Prefix);
            intent.putExtra(SensingKitService.WALK_TEST_ARG, true);
        } else if (action.equals("nextStage")) {
            intent.setAction(SensingKitService.NEXT_STAGE_ACTION);
        } else if (action.equals("stop")) {
            intent.setAction(SensingKitService.STOP_ACTION);
        } else {
            Log.d(TAG, "Unknown action");
            return false;
        }

        setPermissions();
        requestPermissionsAndStartService();
        return true;
    }

    public void setPermissions() {
        if(PERMISSIONS == null) {
            ArrayList<String> temp = new ArrayList<>();
            boolean walkingTest = IntentUtil.extractBooleanExtra(intent, SensingKitService.WALK_TEST_ARG);

            if(walkingTest) {
                temp.add(Manifest.permission.ACCESS_FINE_LOCATION);
                temp.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            } else {
                List<SKSensorType> sensorTypes = IntentUtil.extractSensorTypeExtras(
                    intent, SensingKitService.SENSOR_TYPE_LIST_ARG, SensingKitService.DEFAULT_SENSOR_TYPES
                );
                for (SKSensorType sensorType : sensorTypes) {
                    if (SKSensorType.AUDIO_LEVEL == sensorType) {
                        temp.add(Manifest.permission.RECORD_AUDIO);
                    } else if(SKSensorType.LOCATION == sensorType) {
                        temp.add(Manifest.permission.ACCESS_FINE_LOCATION);
                        temp.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                    }
                }
            }
            temp.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            PERMISSIONS = new String[temp.size()];
            PERMISSIONS = temp.toArray(PERMISSIONS);
        }
    }

    public boolean checkPermissions() {
        for (String permission : PERMISSIONS) {
            if (!cordova.hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    public void requestPermissionsAndStartService() {
        if (!checkPermissions()) {
            cordova.requestPermissions(this, SENSINGKIT_REQUEST, PERMISSIONS);
        } else {
            startService();
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        super.onRequestPermissionResult(requestCode, permissions, grantResults);
        if (requestCode == SENSINGKIT_REQUEST) {
            if(checkPermissions()) {
                startService();
            } else {
                onExperimentStatusEvent(new ExperimentStatusEvent.ExperimentStatusEventBuilder(
                        ExperimentStatus.ERROR
                ).value(SKError.NO_PERMISSION.getValue()).build());
            }
        }
    }

    public void startService(){
        if(intent != null) {
            cordova.getActivity().startService(intent);
        } else {
            onExperimentStatusEvent(new ExperimentStatusEvent.ExperimentStatusEventBuilder(
                    ExperimentStatus.ERROR
            ).value(SKError.UNKNOWN.getValue()).build());
        }
    }

    /**
     * Receive sticky events sent by other parts of the plugin so they can be sent back to the
     * application through the callback context.
     * <p>
     * Received events are converted into JSON before they are sent. It should be made sure that
     * the plugin results are defined separately so they can be set to keep the callback enabled.
     * <p>
     * Once an event has been sent the sticky event is removed.
     *
     * @param event The event that should be sent back to the application.
     */
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onExperimentStatusEvent(ExperimentStatusEvent event) {
        if(callbackContext != null) {
            Log.d(TAG, "Received: " + event.toString());
            Log.d(TAG, "Formatted: " + gson.toJson(event));

            JSONObject jsonEvent = new JSONObject();
            try {
                jsonEvent.put("experimentStatus", event.getExperimentStatus().toString());
                jsonEvent.put("value", event.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jsonEvent);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);

            EventBus.getDefault().removeStickyEvent(ExperimentStatusEvent.class);
        }
    }

    /**
     * Extracts the required arguments from those provided to the cordova plugin.
     *
     * @param args CordovaArgs received when the plugin is called.
     * @return PluginArguments containing all values.
     * @throws JSONException Called if one of the arguments is missing.
     */
    private PluginArguments extractArguments(CordovaArgs args) throws JSONException {
        return gson.fromJson(args.getString(0), PluginArguments.class);
    }
}
