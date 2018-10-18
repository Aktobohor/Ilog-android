package it.unitn.disi.witmee.sensorlog.elements;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.TypedValue;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import it.unitn.disi.witmee.sensorlog.adapters.AnswersArrayAdapter;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.Challenge;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Custom Preference screen that allows to display completed {@link Challenge} objects in the dedicated menu.
 */
public class PreferenceCompleted extends CustomPreference {

    @Override
    public CharSequence getTitle() {
        return super.getTitle();
    }

    public PreferenceCompleted(Challenge availableChallenge, Activity activity) {
        super(availableChallenge, activity);
        setLayoutResource(R.layout.challenge_completed);
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

        final TextView challengeStatus = (TextView) view.findViewById(R.id.challengeStatus);
        TextView challengeResult = (TextView) view.findViewById(R.id.challengeResult);
        TextView challengeReward = (TextView) view.findViewById(R.id.challengeReward);
        final ListView listView = (ListView) view.findViewById(R.id.listViewContributions);

        final TextView challengeDescription = (TextView) view.findViewById(R.id.challengeDescription);
        TextView challengeRules = (TextView) view.findViewById(R.id.challengeRules);

        challengeResult.setText(getAvailableChallenge().getResult());
        challengeStatus.setText(getAvailableChallenge().getStatus());

        challengeStatus.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                challengeStatus.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                System.out.println(getDescriptionHeight(challengeStatus.getPaddingTop(), challengeStatus.getHeight()));
                System.out.println(getDescriptionHeight(challengeStatus.getPaddingTop(), challengeStatus.getHeight()) / challengeStatus.getHeight());
                challengeDescription.setMaxLines(getDescriptionHeight(challengeStatus.getPaddingTop(), challengeStatus.getHeight()) / challengeStatus.getHeight());

                ViewGroup.LayoutParams params = listView.getLayoutParams();
                params.height = getListViewHeight(challengeStatus.getPaddingTop(), challengeStatus.getHeight(), 200);
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                listView.setLayoutParams(params);
            }
        });
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
    }

    /**
     * Method that returns the height of the list view, which is, the total height minus the height of different elements.
     * @param paddingTop Height in pixels of the paddingTop
     * @param singleLineHeight Height in pixels of a single text line
     * @param buttonHeight  Height in pixels of the main button
     * @return Integer representing the height of the available screen
     */
    public int getListViewHeight(int paddingTop, int singleLineHeight, int buttonHeight) {
        return getAvailableScreenHeight() - ((paddingTop*4)+(5*singleLineHeight)+buttonHeight);
    }
}
