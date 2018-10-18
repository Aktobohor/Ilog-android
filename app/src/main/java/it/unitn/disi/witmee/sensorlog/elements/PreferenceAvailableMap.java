package it.unitn.disi.witmee.sensorlog.elements;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.activities.HomeActivity;
import it.unitn.disi.witmee.sensorlog.activities.MessageActivity;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.Challenge;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Custom Preference screen that allows to display available {@link Challenge} objects in the dedicated menu.
 */
public class PreferenceAvailableMap extends CustomPreference {

    @Override
    public CharSequence getTitle() {
        return super.getTitle();
    }

    public PreferenceAvailableMap(Challenge availableChallenge, Activity activity) {
        super(availableChallenge, activity);
        setLayoutResource(R.layout.challenge_available);
    }

    /**
     * Method where the layout elements that characterize the view are specified. This specific view is an element of a Preference Screen, which by default is a
     * lis tof elements, but in this case we want to show a single, full screen element.
     * @param view default return
     */
    @Override
    protected void onBindView(final View view) {
        super.onBindView(view);

        int height = (getScreenHeight(getActivity()) - getStatusBarHeight(getActivity()) - getActionBarHeight(getActivity()));

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.mainChallengeView);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
        linearLayout.setLayoutParams(layoutParams);

        final TextView challengeType = (TextView) view.findViewById(R.id.challengeType);
        TextView challengeStart = (TextView) view.findViewById(R.id.challengeStart);
        TextView challengeEnd = (TextView) view.findViewById(R.id.challengeEnd);
        final TextView challengeDescription = (TextView) view.findViewById(R.id.challengeDescription);
        TextView challengeReward = (TextView) view.findViewById(R.id.challengeReward);
        TextView challengeRules = (TextView) view.findViewById(R.id.challengeRules);
        Button participateButton = (Button) view.findViewById(R.id.participateButton);

        if(getAvailableChallenge().getType().equals("static")) {
            challengeType.setText("freeroam");
        }
        else if(getAvailableChallenge().getType().equals("freeroam")) {
            challengeType.setText("validation");
        }
        challengeStart.setText(Utils.changeDateStringFormat(getAvailableChallenge().getStartdate()));
        challengeEnd.setText(Utils.changeDateStringFormat(getAvailableChallenge().getEnddate()));

        challengeType.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                challengeType.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                challengeDescription.setMaxLines(getDescriptionHeight(challengeType.getPaddingTop(), challengeType.getHeight()) / challengeType.getHeight());
            }
        });

        //Since the description field is srollable, we need to override this method to avoid interferences with the scroll of the Preference Screen
        challengeDescription.setText(getAvailableChallenge().getDescription());
        challengeDescription.setMovementMethod(new ScrollingMovementMethod());
        View.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean isLarger;
                isLarger = ((TextView) v).getLineCount() * ((TextView) v).getLineHeight() > v.getHeight();
                if (event.getAction() == MotionEvent.ACTION_MOVE && isLarger) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        };
        challengeDescription.setOnTouchListener(listener);
        challengeReward.setText(String.valueOf(getAvailableChallenge().getPointsawarded()));
        challengeRules.setText(getAvailableChallenge().getInstructions());
        Linkify.addLinks(challengeRules, Linkify.WEB_URLS);

        /**
         Adds the {@link #availableChallenge} to the database after having updated the {@link Challenge#participationtime} with {@link Challenge#setParticipationtime(String)}, Then it logins and
         tries to upload the challenges participation info to the database with {@link iLogApplication#uploadChallengesParticipationInfo(String)}. Finally it exits and
         goes to the main menu.
         */
        participateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAvailableChallenge().setParticipationtime(Utils.longToStringFormat(System.currentTimeMillis()));
                iLogApplication.db.addChallenge(getAvailableChallenge());

                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), iLogApplication.gso);
                googleSignInClient.silentSignIn()
                        .addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
                            @Override
                            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                                try {
                                    GoogleSignInAccount account = task.getResult(ApiException.class);
                                    String idToken = account.getIdToken();

                                    iLogApplication.uploadChallengesParticipationInfo(idToken);
                                } catch (ApiException e) {
                                    e.printStackTrace();
                                    if(e.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED && !iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, "").equals("")) {
                                        iLogApplication.startSignInActivity();
                                    }
                                }
                            }
                        });
                Intent myIntent = new Intent(getActivity(), HomeActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().startActivity(myIntent);
            }
        });

        ScrollableMapView mMapView = (ScrollableMapView) view.findViewById(R.id.challengeLocation);
        ViewGroup.LayoutParams params = mMapView.getLayoutParams();
        params.height = getAvailableScreenHeight() / 3;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        mMapView.setPadding(20, 20, 20, 0);
        mMapView.setLayoutParams(params);
        mMapView.onCreate(null);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Method call when the map is ready
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                final GoogleMap googleMap = mMap;
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

                        //When the map is loaded we create a layer with an highlighted area
                        GeoJsonLayer layer = null;
                        try {
                            JSONObject location = new JSONObject(getAvailableChallenge().getLocation());
                            layer = new GeoJsonLayer(googleMap, location);
                            layer.addLayerToMap();
                            layer.getDefaultPolygonStyle().setStrokeColor(Color.rgb(135, 206, 235));
                            layer.getDefaultPolygonStyle().setFillColor(Color.argb(80, 135, 206, 235));

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            try {
                                JSONArray coordinates = location.getJSONObject("geometry").getJSONArray("coordinates");
                                for (int index1 = 0; index1 < coordinates.length(); index1++) {
                                    for (int index2 = 0; index2 < coordinates.getJSONArray(index1).length(); index2++) {
                                        builder.include(new LatLng(coordinates.getJSONArray(index1).getJSONArray(index2).getDouble(1), coordinates.getJSONArray(index1).getJSONArray(index2).getDouble(0)));
                                    }
                                }
                            } catch(JSONException e) {
                                builder = new LatLngBounds.Builder();
                                JSONArray coordinates = location.getJSONObject("geometry").getJSONArray("coordinates");
                                for (int index1 = 0; index1 < coordinates.length(); index1++) {
                                    for (int index2 = 0; index2 < coordinates.getJSONArray(index1).length(); index2++) {
                                        for (int index3 = 0; index3 < coordinates.getJSONArray(index1).getJSONArray(index2).length(); index3++) {
                                            builder.include(new LatLng(coordinates.getJSONArray(index1).getJSONArray(index2).getJSONArray(index3).getDouble(1), coordinates.getJSONArray(index1).getJSONArray(index2).getJSONArray(index3).getDouble(0)));
                                        }
                                    }
                                }
                            }

                            //Camera movement that centers on the area above
                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), 100);
                            googleMap.moveCamera(cu);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    /**
     * Method that calculates the height of the description, based on the height of a single line and of the padding we selected in the view
     * @param paddingTop Height of the padding used in the view
     * @param singleLineHeight Height of a single line
     * @return Integer representing the description field
     */
    public int getDescriptionHeight(int paddingTop, int singleLineHeight) {
        return getAvailableScreenHeight() - ((paddingTop * 5)+(getAvailableScreenHeight() / 3) + (10 * singleLineHeight));
    }
}
