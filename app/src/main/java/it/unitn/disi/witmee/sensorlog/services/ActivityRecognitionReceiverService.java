package it.unitn.disi.witmee.sensorlog.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.sensors.MV;
import it.unitn.disi.witmee.sensorlog.model.virtual.CN;

/**
 * This class extends IntentService and is used as a handler to detect the user Activity as provided by {@link GoogleApiClient}
 */
public class ActivityRecognitionReceiverService extends IntentService {
    public ActivityRecognitionReceiverService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognitionReceiverService(String name) {
        super(name);
    }

    /**
     * Method overridden to detect the user activity. Every time {@link GoogleApiClient} delivers an activity we detect it in this method.
     * @param intent detected activity
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result.getProbableActivities());
        }
    }

    /**
     * This method disambiguates the received activities and creates a JSONArray object containing all the {@link DetectedActivity} with an attached
     * confidence value, in percentage, from 0 to 100. The resulting JSONArray is serialized (converted to String) and persisted in the logs using
     * {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)} as a {@link MV} object
     * @param probableActivities Activities delivered by {@link GoogleApiClient}
     */
    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        JSONArray result = new JSONArray();
        for (DetectedActivity activity : probableActivities) {
            switch (activity.getType()) {
                case DetectedActivity.IN_VEHICLE: {
                    result.put(createObject("InVehicle", activity.getConfidence()));
                    Log.e("ActivityRecogition", "In Vehicle: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    result.put(createObject("OnBycicle", activity.getConfidence()));
                    Log.e("ActivityRecogition", "On Bicycle: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    result.put(createObject("OnFoot", activity.getConfidence()));
                    Log.e("ActivityRecogition", "On Foot: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.RUNNING: {
                    result.put(createObject("Running", activity.getConfidence()));
                    Log.e("ActivityRecogition", "Running: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.STILL: {
                    result.put(createObject("Still", activity.getConfidence()));
                    Log.e("ActivityRecogition", "Still: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.TILTING: {
                    result.put(createObject("Tilting", activity.getConfidence()));
                    Log.e("ActivityRecogition", "Tilting: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.WALKING: {
                    result.put(createObject("Walking", activity.getConfidence()));
                    Log.e("ActivityRecogition", "Walking: " + activity.getConfidence());
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    result.put(createObject("Unknown", activity.getConfidence()));
                    Log.e("ActivityRecogition", "Unknown: " + activity.getConfidence());
                    break;
                }
            }
        }
        iLogApplication.persistInMemoryEvent(new MV(System.currentTimeMillis(), 0, result.toString()));
    }

    /**
     * Method used to create a JSONObject containing the activity label as key and confidence value as value
     * @param activity The label describing the activity
     * @param confidence The confidence value, as an integer from 0 to 100
     * @return JSONObject containing the activity label as key and confidence value as value
     */
    private JSONObject createObject(String activity, int confidence) {
        try {
            return new JSONObject().put(activity, confidence);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }
}
