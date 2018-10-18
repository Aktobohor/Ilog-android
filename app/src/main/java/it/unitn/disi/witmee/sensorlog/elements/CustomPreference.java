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

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.activities.HomeActivity;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.Challenge;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Custom Preference screen that allows to display available {@link Challenge} objects in the dedicated menu.
 */
public class CustomPreference extends Preference {

    private Activity activity = null;
    private Challenge availableChallenge = null;

    @Override
    public CharSequence getTitle() {
        return super.getTitle();
    }

    public CustomPreference(Challenge availableChallenge, Activity activity) {
        super(activity);
        this.availableChallenge = availableChallenge;
        this.activity = activity;
    }

    @Override
    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
        super.setOnPreferenceClickListener(onPreferenceClickListener);
    }

    /**
     * Gets the {@link #availableChallenge} object
     * @return {@link Challenge} object
     */
    public Challenge getAvailableChallenge() {
        return availableChallenge;
    }

    /**
     * Sets the {@link #availableChallenge} object
     * @param availableChallenge {@link Challenge} object
     */
    public void setAvailableChallenge(Challenge availableChallenge) {
        this.availableChallenge = availableChallenge;
    }

    /**
     * Method that returns the smartphone screen height
     * @param activity {@link Activity} class
     * @return integer representing the height, in pixels
     */
    public static int getScreenHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    /**
     * Method that returns the height of the actionbar
     * @param context {@link Context} class
     * @return Integer representing the height, in pixels, of the actionbar
     */
    public static int getActionBarHeight(Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return 0;
    }

    /**
     * Method that returns the height of the status bar
     * @param context {@link Context} class
     * @return Integer representing the height, in pixels, of the status bar
     */
    public int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Method that returns the Activity
     * @return {@link Activity} object to be returned
     */
    public Activity getActivity() {
        return this.activity;
    }

    /**
     * Method that returns the height of the available screen, which is, the total height {@link #getScreenHeight(Activity)} minus {@link #getStatusBarHeight(Context)}
     * and {@link #getActionBarHeight(Context)}.
     * @return Integer representing the height of the available screen
     */
    public int getAvailableScreenHeight() {
        return (getScreenHeight(activity) - getStatusBarHeight(activity) - getActionBarHeight(activity));
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
