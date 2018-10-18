package it.unitn.disi.witmee.sensorlog.elements;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.activities.ChallengeStaticActivity;
import it.unitn.disi.witmee.sensorlog.activities.ContributionActivity;
import it.unitn.disi.witmee.sensorlog.adapters.AnswersArrayAdapter;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.Answer;
import it.unitn.disi.witmee.sensorlog.model.Challenge;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Custom Preference screen that allows to display ongoing {@link Challenge} objects in the dedicated menu.
 */
public class PreferenceOngoing extends CustomPreference {

    private Activity activity = null;
    GoogleMap googleMap = null;
    String freeroamMapReferenceId = null;

    @Override
    public CharSequence getTitle() {
        return super.getTitle();
    }

    public PreferenceOngoing(Challenge availableChallenge, Activity activity) {
        super(availableChallenge, activity);
        this.activity = activity;

        /**
         * Different layouts based on the type of the challenges to be visualized
         */
        if (availableChallenge.getType().equals(Challenge.TYPE_STATIC)) {
            setLayoutResource(R.layout.challenge_ongoing_static);
        } else if (availableChallenge.getType().equals(Challenge.TYPE_DYNAMIC)) {
        } else if (availableChallenge.getType().equals(Challenge.TYPE_BACKGROUND)) {
            setLayoutResource(R.layout.challenge_ongoing_background);
        } else if (availableChallenge.getType().equals(Challenge.TYPE_FREEROAM)) {
            setLayoutResource(R.layout.challenge_ongoing_freeroam);
        }
    }

    /**
     * Method where the layout elements that characterize the view are specified. This specific view is an element of a Preference Screen, which by default is a
     * lis tof elements, but in this case we want to show a single, full screen element.
     * @param view default return
     */
    @Override
    protected void onBindView(final View view) {
        super.onBindView(view);

        if(getAvailableChallenge().getType().equals(Challenge.TYPE_FREEROAM) || getAvailableChallenge().getType().equals(Challenge.TYPE_STATIC)) {
            int height = (getScreenHeight(activity) - getStatusBarHeight(activity) - getActionBarHeight(activity));

            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.mainChallengeView);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
            linearLayout.setLayoutParams(layoutParams);

            final TextView challengeType = (TextView) view.findViewById(R.id.challengeType);
            TextView challengeStart = (TextView) view.findViewById(R.id.challengeStart);
            TextView challengeEnd = (TextView) view.findViewById(R.id.challengeEnd);
            final Button contributeButton = (Button) view.findViewById(R.id.participateButton);
            final ListView listView = (ListView) view.findViewById(R.id.listViewContributions);

            challengeType.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    challengeType.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    if(getAvailableChallenge().getType().equals(Challenge.TYPE_FREEROAM)) {
                        ViewGroup.LayoutParams params = listView.getLayoutParams();
                        params.height = getListViewHeight(challengeType.getPaddingTop(), challengeType.getHeight(), 200) - (int) (getAvailableScreenHeight() / 2.5f) - 50;
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        listView.setLayoutParams(params);
                    }
                    else if(getAvailableChallenge().getType().equals(Challenge.TYPE_STATIC)) {
                        ViewGroup.LayoutParams params = listView.getLayoutParams();
                        params.height = getListViewHeight(challengeType.getPaddingTop(), challengeType.getHeight(), 200);
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        listView.setLayoutParams(params);
                    }
                }
            });

            //System.out.println(getAvailableChallenge().getType());
            challengeType.setText(getAvailableChallenge().getType());
            challengeStart.setText(Utils.changeDateStringFormat(getAvailableChallenge().getStartdate()));
            challengeEnd.setText(Utils.changeDateStringFormat(getAvailableChallenge().getEnddate()));

            //System.out.println(iLogApplication.db.getAllAnswersByInstanceId(availableChallenge.getInstanceid()).size());

            AnswersArrayAdapter answersArrayAdapter = new AnswersArrayAdapter(getAvailableChallenge().getInstanceid());
            listView.setAdapter(answersArrayAdapter);
            listView.setOnTouchListener(new ListView.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        listView.scrollBy(0, 1);
                    }
                    return false;
                }
            });

            //contributeButton.setEnabled(false);
            contributeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getAvailableChallenge().getType().equals(Challenge.TYPE_STATIC)) {
                        Intent myIntent = new Intent(activity, ChallengeStaticActivity.class);
                        myIntent.putExtra("challenge", getAvailableChallenge());
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.startActivity(myIntent);
                    }
                    else if(getAvailableChallenge().getType().equals(Challenge.TYPE_FREEROAM)) {
                        googleMap.setMyLocationEnabled(false);

                        Intent myIntent = new Intent(activity, ChallengeStaticActivity.class);
                        myIntent.putExtra("challenge", getAvailableChallenge());
                        myIntent.putExtra("freeroamMapReferenceId", freeroamMapReferenceId);
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.startActivity(myIntent);
                    }
                }
            });

            if(getAvailableChallenge().getType().equals(Challenge.TYPE_FREEROAM)) {
                ScrollableMapView mMapView = (ScrollableMapView) view.findViewById(R.id.challengeLocation);
                ViewGroup.LayoutParams params = mMapView.getLayoutParams();
                params.height = (int) (getAvailableScreenHeight() / 2.5f);
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                mMapView.setPadding(20, 20, 20, 0);
                mMapView.setLayoutParams(params);
                mMapView.onCreate(null);
                mMapView.onResume();

                try {
                    MapsInitializer.initialize(activity.getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Method call when the map is ready
                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(final GoogleMap mMap) {
                        googleMap = mMap;
                        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                            }
                        });
                        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                            @Override
                            public void onCameraIdle() {
                            }
                        });
                        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                            @Override
                            public void onCameraMove() {
                            }
                        });
                        googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                            @Override
                            public void onCameraMoveStarted(int reason) {
                            }
                        });
                        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(iLogApplication.getAppContext(), iLogApplication.gso);
                                googleSignInClient.silentSignIn()
                                        .addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
                                            @Override
                                            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                                                try {
                                                    GoogleSignInAccount account = task.getResult(ApiException.class);
                                                    String idToken = account.getIdToken();

                                                    new downloadLocationPoints().execute(idToken, getAvailableChallenge().getInstanceid());
                                                } catch (ApiException e) {
                                                    e.printStackTrace();
                                                    if(e.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED && !iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, "").equals("")) {
                                                        iLogApplication.startSignInActivity();
                                                    }
                                                }
                                            }
                                        });
                            }
                        });
                        googleMap.setMyLocationEnabled(true);
                    }
                });
            }
        }
        else {
            int height = (getScreenHeight(activity) - getStatusBarHeight(activity) - getActionBarHeight(activity));

            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.mainChallengeView);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
            linearLayout.setLayoutParams(layoutParams);

            TextView challengeType = (TextView) view.findViewById(R.id.challengeType);
            TextView challengeStart = (TextView) view.findViewById(R.id.challengeStart);
            TextView challengeEnd = (TextView) view.findViewById(R.id.challengeEnd);
            TextView description = (TextView) view.findViewById(R.id.challengeDescription);
            TextView challengeRules = (TextView) view.findViewById(R.id.challengeRules);

            challengeType.setText(getAvailableChallenge().getType());
            challengeStart.setText(Utils.changeDateStringFormat(getAvailableChallenge().getStartdate()));
            challengeEnd.setText(Utils.changeDateStringFormat(getAvailableChallenge().getEnddate()));
            description.setText(getAvailableChallenge().getDescription());
            challengeRules.setText(getAvailableChallenge().getInstructions());
            Linkify.addLinks(challengeRules, Linkify.WEB_URLS);
        }
    }

    /**
     * Method that returns the height of the list view, which is, the total height minus the height of different elements.
     * @param paddingTop Height in pixels of the paddingTop
     * @param singleLineHeight Height in pixels of a single text line
     * @param buttonHeight  Height in pixels of the main button
     * @return Integer representing the height of the available screen
     */
    public int getListViewHeight(int paddingTop, int singleLineHeight, int buttonHeight) {
        return getAvailableScreenHeight() - ((paddingTop*4)+(6*singleLineHeight)+buttonHeight);
    }


    private void populatePoints(JSONArray array, GoogleMap mMap) {
        for(int index=0; index<array.length(); index++) {
            try {
                JSONObject object = array.getJSONObject(index);
                mMap.addMarker(new MarkerOptions().position(new LatLng(object.getDouble("lat"), object.getDouble("long"))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param array
     * @param mMap
     */
    private void moveCamera(JSONArray array, GoogleMap mMap) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : Utils.jsonArrayToList(array)) {
            builder.include(latLng);
        }

        try {
            final LatLngBounds bounds = builder.build();

            //BOUND_PADDING is an int to specify padding of bound.. try 100.
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
            mMap.animateCamera(cu);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class downloadLocationPoints extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(final Object... data) {
            String token = data[0].toString();
            String challengeid = data[1].toString();

            Log.d(this.toString(), "Downloading map");

            return downloadPoints(token, challengeid);
        }
        @Override
        protected void onPostExecute(String result) {
            try {

                googleMap.setMyLocationEnabled(false);

                JSONObject response = new JSONObject(result);
                if(response.getString("status").equals("done_message")) {
                    System.out.println(response.getJSONObject("payload"));
                    final JSONObject payload = response.getJSONObject("payload");
                    freeroamMapReferenceId = payload.getString("mapid");
                    final JSONArray points = payload.getJSONArray("points");

                    //if the payload contains the key rules, then we need to filter out, otherwise we just show the available ones
                    if(payload.has("rules")) {
                        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(iLogApplication.getAppContext());
                        //Either number or radius. For both, I need to calculate the distance between each point in the list and the current position
                        //{"payload":{"mapid":"informacontributionsanswertionaboutthismap","rules":{"number":5,"radius":300},"points":[{"lat":46.110016,"long":11.177229},{"lat":45.928082,"long":10.976601},{"lat":46.111291,"long":11.047485},{"lat":46.050213,"long":11.124505},{"lat":46.023458,"long":11.238991}]},"status":"done_message"}
                        if (ActivityCompat.checkSelfPermission(iLogApplication.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(iLogApplication.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            mFusedLocationClient.getLastLocation()
                                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                                        @Override
                                        public void onSuccess(Location location) {
                                            if (location != null) {
                                                try {
                                                    JSONObject rules = payload.getJSONObject("rules");
                                                    if(rules.has("radius")) {
                                                        double radius = rules.getDouble("radius");
                                                        JSONArray filteredPoints = new JSONArray();

                                                        for(int index=0; index<points.length();index++) {
                                                            try {
                                                                Location locationPoint = new Location("point B");
                                                                locationPoint.setLatitude(points.getJSONObject(index).getDouble("lat"));
                                                                locationPoint.setLongitude(points.getJSONObject(index).getDouble("long"));

                                                                if(location.distanceTo(locationPoint) < radius) {
                                                                    filteredPoints.put(new JSONObject().put("lat", locationPoint.getLatitude()).put("long", locationPoint.getLongitude()));
                                                                }
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        populatePoints(filteredPoints, googleMap);
                                                        moveCamera(filteredPoints, googleMap);
                                                    }
                                                    else if(rules.has("number")) {
                                                        int number = rules.getInt("number");
                                                        ArrayList<JSONObject> filteredPoints = new ArrayList<JSONObject>();

                                                        for(int index=0; index<points.length();index++) {
                                                            try {
                                                                Location locationPoint = new Location("point B");
                                                                locationPoint.setLatitude(points.getJSONObject(index).getDouble("lat"));
                                                                locationPoint.setLongitude(points.getJSONObject(index).getDouble("long"));

                                                                filteredPoints.add(new JSONObject().put("lat", locationPoint.getLatitude()).put("long", locationPoint.getLongitude()).put("distance", location.distanceTo(locationPoint)));

                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                        Collections.sort(filteredPoints, new Comparator<JSONObject>() {
                                                            @Override
                                                            public int compare(JSONObject o1, JSONObject o2) {
                                                                try {
                                                                    if(o1.getDouble("distance") < o2.getDouble("distance")) {
                                                                        return -1;
                                                                    }
                                                                    else {
                                                                        return 1;
                                                                    }
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                return 0;
                                                            }
                                                        });

                                                        JSONArray array = arraylistToJSONArray(filteredPoints.subList(0, number));
                                                        populatePoints(array, googleMap);
                                                        moveCamera(array, googleMap);
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                    else {
                        populatePoints(points, googleMap);
                        moveCamera(points, googleMap);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static String downloadPoints(String token, String challengeid) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("challengeid", challengeid);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(iLogApplication.sharedPreferences.getString(Utils.CONFIG_SERVERBASEURL, "")+iLogApplication.sharedPreferences.getString(Utils.CONFIG_PORTSEPATATOR, "")+iLogApplication.sharedPreferences.getInt(Utils.CONFIG_PORTAVAILABLECHALLENGES, 0)+iLogApplication.sharedPreferences.getString(Utils.CONFIG_SEPARATOR, "") + "downloadmap");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpEntity<String> response = restTemplate.exchange(
                    builder.build().encode().toUri(),
                    HttpMethod.GET,
                    entity,
                    String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private JSONArray arraylistToJSONArray(List<JSONObject> list) {
        JSONArray array = new JSONArray();
        for(JSONObject element: list) {
            array.put(element);
        }
        return array;
    }
}
