package it.unitn.disi.witmee.sensorlog.elements;

import android.app.Activity;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.unitn.disi.witmee.sensorlog.activities.ContributionActivity;
import it.unitn.disi.witmee.sensorlog.fragments.ContributionFragment;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Class that allows to monitor the interactions of the user with a {@link GoogleMap} object. It is able to detect zoom in/out, swipes and click events. Int he first two cases
 * it detects also the values of the interactions, how much zoom and how many meters for the movement.
 */
public class MonitorableGoogleMap {

    GoogleMap googleMap;
    int lastZoomValue = 0;
    LatLng lastPosition = null;
    boolean userMoving = false;
    int questionId = 0;
    ContributionActivity activity;
    ArrayList<JSONObject> movements = new ArrayList<JSONObject>();
    ArrayList<JSONObject> zooms = new ArrayList<JSONObject>();
    ArrayList<JSONObject> payload = new ArrayList<JSONObject>();

    public MonitorableGoogleMap(int questionId, ContributionActivity activity) {
        this.questionId = questionId;
        this.activity = activity;
        activity.updatePayload(questionId, ContributionFragment.generateEmptyPayload());
    }

    /**
     * Method that returns the payload, i.e., the interactions of the user with the map
     * @return {@link ArrayList} of {@link JSONObject} containing the payload with the statistics
     */
    public ArrayList<JSONObject> getPayload() {
        return payload;
    }

    /**
     * Method used to set the latest zoom value
     * @param lastZoomValue integer representing the zoom value
     */
    public void setLastZoomValue(int lastZoomValue) {
        this.lastZoomValue = lastZoomValue;
    }

    /**
     * Method used to set the latest position on the map
     * @param lastPosition {@link LatLng} representing the position
     */
    public void setLastPosition(LatLng lastPosition) {
        this.lastPosition = lastPosition;
    }

    /**
     * Method used to set the {@link GoogleMap} object the class will monitor. This also initializes the listeners for the different actions
     * @param googleMap {@link GoogleMap} object to be monitored
     */
    public void setGoogleMap(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        //Listener that detects clicks on the map
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                try {
                    float[] results = new float[1];
                    Location.distanceBetween(latLng.latitude, latLng.longitude, lastPosition.latitude, lastPosition.longitude, results);
                    payload.add(new JSONObject().put("action", "click").put("value", new JSONObject().put("lat", latLng.latitude).put("long", latLng.longitude)).put("delta", results[0]).put("timestamp", Utils.longToStringFormat(System.currentTimeMillis())));
                    updatePayload();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //listener that detects when the user stops interacting with the map
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if(userMoving) {
                    if(zooms.size()>0) {
                        payload.add(zooms.get(zooms.size()-1));
                    }
                    if(movements.size()>0) {
                        payload.add(movements.get(movements.size()-1));
                    }
                }

                updatePayload();

                lastZoomValue = (int)(googleMap.getCameraPosition().zoom * 1000);
                lastPosition = googleMap.getCameraPosition().target;
                zooms.clear();
                movements.clear();
            }
        });
        //listener that detects the movements while the user is interacting with the map
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if(userMoving) {
                    int zoomValue = (int)(googleMap.getCameraPosition().zoom * 1000);
                    LatLng position = googleMap.getCameraPosition().target;
                    if(lastZoomValue > zoomValue) {
                        //System.out.println("MAP: ZOOM OUT" + zoomValue + " " + (lastZoomValue-zoomValue));
                        try {
                            zooms.add(new JSONObject().put("action", "zoomout").put("value", (float)zoomValue/1000).put("delta", (float)(lastZoomValue-zoomValue)/1000).put("timestamp", Utils.longToStringFormat(System.currentTimeMillis())));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(lastZoomValue < zoomValue) {
                        //System.out.println("MAP: ZOOM IN" + zoomValue + " " + (zoomValue-lastZoomValue));
                        try {
                            zooms.add(new JSONObject().put("action", "zoomin").put("value", (float)zoomValue/1000).put("delta", (float)(zoomValue-lastZoomValue)/1000).put("timestamp", Utils.longToStringFormat(System.currentTimeMillis())));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if(lastPosition != position) {
                        //System.out.println("MAP: MOVED" + position);
                        try {
                            float[] results = new float[1];
                            Location.distanceBetween(position.latitude, position.longitude, lastPosition.latitude, lastPosition.longitude, results);
                            movements.add(new JSONObject().put("action", "move").put("value", new JSONObject().put("lat", position.latitude).put("long", position.longitude)).put("delta", results[0]).put("timestamp", Utils.longToStringFormat(System.currentTimeMillis())));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        //listener that detects when the user starts interacting with the map
        googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    userMoving = true;
                } else if (reason == GoogleMap.OnCameraMoveStartedListener
                        .REASON_API_ANIMATION) {
                    System.out.println("MAP: START The user tapped something on the map.");
                } else if (reason == GoogleMap.OnCameraMoveStartedListener
                        .REASON_DEVELOPER_ANIMATION) {
                    userMoving = false;
                }
            }
        });
    }

    /**
     * Method that returns the {@link GoogleMap} object used in this class
     * @return {@link GoogleMap} to be returned
     */
    public GoogleMap getGoogleMap() {
        return googleMap;
    }

    public void updatePayload() {
        if(getPayload().size() > 0) {
            try {
                //System.out.println(new JSONObject().put("mapstatistics", convertArrayListJSONArray(getPayload()).toString()).toString());
                System.out.println(activity.getPayload().get(questionId).length());
                if(activity.getPayload().get(questionId).length() > 0) {
                    JSONObject payload = new JSONObject(activity.getPayload().get(questionId));
                    payload.put("mapstatistics", convertArrayListJSONArray(getPayload()));
                    activity.updatePayload(questionId, payload.toString());
                }
                else {
                    activity.updatePayload(questionId, new JSONObject().put("mapstatistics", convertArrayListJSONArray(getPayload())).toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Methos that converts an {@link ArrayList} to a {@link JSONArray}
     * @param array {@link ArrayList} to be converted
     * @return {@link JSONArray to be returned}
     */
    private JSONArray convertArrayListJSONArray(ArrayList<JSONObject> array) {
        JSONArray jsonarray = new JSONArray();
        for(JSONObject object: array) {
            jsonarray.put(object);
        }
        return jsonarray;
    }
}
