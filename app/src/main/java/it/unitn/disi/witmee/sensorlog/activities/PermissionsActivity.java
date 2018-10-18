package it.unitn.disi.witmee.sensorlog.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.fragments.ContributionFragment;
import it.unitn.disi.witmee.sensorlog.fragments.PermissionsFragment;
import it.unitn.disi.witmee.sensorlog.model.Task;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Activity that handles the procedure that drives the user to grant the needed permissions. There are mandatory and non mandatory permissions. Similarly to {@link ContributionActivity}
 * also this Activity extends a {@link FragmentActivity}. Each permission is a fragment, and the user can decide to grant it or skip it (if it is not mandatory).
 */
public class PermissionsActivity extends FragmentActivity {

    static int selectedFragment = 1;
    public static int numberOfSubQuestions = 0;
    PermissionsFragment actualFragment = null;
    Menu menu = null;

    static FragmentManager fragmentManager = null;
    private static ArrayList<DialogObject> dialogs = null;

    long startingTime = 0;

    public static final String BATTERY_PERMISSION = "battery_permission";
    public static final String NOTIFICATIONS_PERMISSION = "notifications_permission";
    public static final String APPLICATIONS_PERMISSION = "applications_permission";
    public static final String TOUCH_PERMISSION = "touch_permission";
    public static final String SUMMARY_PERMISSION = "summary_permission";
    public static final String WIFI_PERMISSION = "wifi_permission";

    /**
     * Default method called when the Activity is created. It is mainly used to initialize the variables. Since it is a FragmentActivity it uses Fragments
     * to display content to the users and specifically to {@link PermissionsFragment}
     * @param savedInstanceState what
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        selectedFragment = 1;

        fragmentManager = getSupportFragmentManager();

        dialogs = populateDialogs();

        numberOfSubQuestions = dialogs.size();

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            actualFragment = new PermissionsFragment();
            actualFragment.setDialog(dialogs.get(selectedFragment-1));

            fragmentManager.beginTransaction().replace(R.id.fragment_container, actualFragment).commit();
        }

        startingTime = System.currentTimeMillis();
    }

    /**
     * Default method
     * @param menu {@link Menu} object
     * @return default value true
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * Method called the the Activity is creating the option menu. We inflate the menu layout {@link R.menu#questionnaire_options_menu} and update the buttons in it depending
     * on the number of subquestions the main question has:
     * <ul>
     *     <li>The next button (top right) is set to Finish if there is only one question, to Next otherwise.</li>
     *     <li>The previous button is set to false independently because being in the first question we should not allow to go previously.</li>
     * </ul>
     * @param menu {@link Menu} object
     * @return default value true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.permissions_options_menu, menu);
        this.menu = menu;

        if(PermissionsActivity.numberOfSubQuestions==1) {
            this.buttonNextSetText(R.string.finish);
            this.buttonNextStatus(true);
        }
        else {
            this.buttonNextSetText(R.string.next);
        }
        return true;
    }

    /**
     * Method triggered when an item (button) in the option menu is pressed by the user. If the button pressed is the Previous, we remove the current fragment and replace it
     * with the previous. If instead the button is the next one, two things can occur:
     * <ul>
     *     <li>The button is set as Next, which means that we remove the current frgment and replace it with the next</li>
     *     <li>The button is set as Finish, in this case the permission process is finished and we need to send the result back to the calling activity with {@link #setResult(int)}
     *     and close this activity using {@link #finish()}.</li>
     * </ul>
     * @param item {@link MenuItem} pressed
     * @return Default return True
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        MenuItem nextItem = menu.findItem(R.id.next);

        switch (item.getItemId()) {
            case R.id.next:
                if(nextItem.getTitle().equals(getString(R.string.finish))) {
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
                else {
                    selectedFragment++;
                    replaceFragment();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method used to replace the current fragment with the next one. It adds the new fragment to the Stack and commit.
     */
    private void replaceFragment() {
        actualFragment = new PermissionsFragment();
        actualFragment.setDialog(dialogs.get(selectedFragment-1));

        fragmentManager.beginTransaction().replace(R.id.fragment_container, actualFragment).commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        Log.d(this.getClass().getSimpleName(), "Destroy");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.d(this.getClass().getSimpleName(), "Pause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(this.getClass().getSimpleName(), "Resume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(this.getClass().getSimpleName(), "Stop");
        super.onStop();
    }

    /**
     * Method used to update the status (enabled or disabled) of the next button in the option menu. Additionally, when the status is updated we generate the index
     * for the next fragment to be displayed when the user pushes the button.
     * @param status True if the button is enabled, false otherwise
     */
    public void buttonNextStatus(boolean status) {
        if(menu!=null) {
            menu.findItem(R.id.next).setEnabled(status);
        }
    }

    /**
     * Method used to set the text of the next button (top right) in the option menu.
     * @param text String containing the text to be displayed
     */
    public void buttonNextSetText(int text) {
        if(menu!=null) {
            Log.d(this.toString(), "Setting menu item title");
            menu.findItem(R.id.next).setTitle(text);
        }
        else {
            Log.d(this.toString(), "Menu null");
        }
    }

    /**
     * Method that dynamically creates the {@link DialogObject} that will be showed to the user and will drive him in the procedure to grant the permissions. The dialogs are
     * generated from the {@link JSONObject} of the experiment downloaded from the server.
     * @return {@link ArrayList<DialogObject>} containing the current permissions to ask to the user
     */
    private ArrayList<DialogObject> populateDialogs() {
        ArrayList<DialogObject> dialogs = new ArrayList<DialogObject>();

        try {
            if(containsPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), WIFI_PERMISSION) && !iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_UPLOAD_IF_WIFI, false)) {
                JSONObject permission = returnPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), WIFI_PERMISSION);
                dialogs.add(new DialogObject(Boolean.parseBoolean(permission.getString("singlesensor")), permission.getJSONObject("message").getString(iLogApplication.getLocale()), permission.getJSONObject("confirmation").getString(iLogApplication.getLocale()), getDrawableByName(permission.getString("background")), permission.getString("permission"), Boolean.parseBoolean(permission.getString("skip")), permission.getInt("order"), permission.getJSONObject("title").getString(iLogApplication.getLocale())));
            }
            if(!iLogApplication.hasBatteryIgnorePermission() && containsPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), BATTERY_PERMISSION)) {
                JSONObject permission = returnPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), BATTERY_PERMISSION);
                dialogs.add(new DialogObject(Boolean.parseBoolean(permission.getString("singlesensor")), permission.getJSONObject("message").getString(iLogApplication.getLocale()), permission.getJSONObject("confirmation").getString(iLogApplication.getLocale()), getDrawableByName(permission.getString("background")), permission.getString("permission"), Boolean.parseBoolean(permission.getString("skip")), permission.getInt("order"), permission.getJSONObject("title").getString(iLogApplication.getLocale())));
            }
            if(!iLogApplication.hasSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION) && containsPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), Manifest.permission.ACCESS_FINE_LOCATION)) {
                JSONObject permission = returnPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), Manifest.permission.ACCESS_FINE_LOCATION);
                dialogs.add(new DialogObject(Boolean.parseBoolean(permission.getString("singlesensor")), permission.getJSONObject("message").getString(iLogApplication.getLocale()), permission.getJSONObject("confirmation").getString(iLogApplication.getLocale()), getDrawableByName(permission.getString("background")), permission.getString("permission"), Boolean.parseBoolean(permission.getString("skip")), permission.getInt("order"), permission.getJSONObject("title").getString(iLogApplication.getLocale())));
            }
            if(!iLogApplication.hasNotificationAccessPermission() && containsPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), NOTIFICATIONS_PERMISSION)) {
                JSONObject permission = returnPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), NOTIFICATIONS_PERMISSION);
                dialogs.add(new DialogObject(Boolean.parseBoolean(permission.getString("singlesensor")), permission.getJSONObject("message").getString(iLogApplication.getLocale()), permission.getJSONObject("confirmation").getString(iLogApplication.getLocale()), getDrawableByName(permission.getString("background")), permission.getString("permission"), Boolean.parseBoolean(permission.getString("skip")), permission.getInt("order"), permission.getJSONObject("title").getString(iLogApplication.getLocale())));
            }
            if(!iLogApplication.hasSinglePermission(Manifest.permission.RECORD_AUDIO) && containsPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), Manifest.permission.RECORD_AUDIO)) {
                JSONObject permission = returnPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), Manifest.permission.RECORD_AUDIO);
                dialogs.add(new DialogObject(Boolean.parseBoolean(permission.getString("singlesensor")), permission.getJSONObject("message").getString(iLogApplication.getLocale()), permission.getJSONObject("confirmation").getString(iLogApplication.getLocale()), getDrawableByName(permission.getString("background")), permission.getString("permission"), Boolean.parseBoolean(permission.getString("skip")), permission.getInt("order"), permission.getJSONObject("title").getString(iLogApplication.getLocale())));
            }
            if(!iLogApplication.hasUsageStatsPermission() && containsPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), APPLICATIONS_PERMISSION)) {
                JSONObject permission = returnPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), APPLICATIONS_PERMISSION);
                dialogs.add(new DialogObject(Boolean.parseBoolean(permission.getString("singlesensor")), permission.getJSONObject("message").getString(iLogApplication.getLocale()), permission.getJSONObject("confirmation").getString(iLogApplication.getLocale()), getDrawableByName(permission.getString("background")), permission.getString("permission"), Boolean.parseBoolean(permission.getString("skip")), permission.getInt("order"), permission.getJSONObject("title").getString(iLogApplication.getLocale())));
            }
            if(!iLogApplication.hasDrawOnTopPermissions() && containsPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), TOUCH_PERMISSION)) {
                JSONObject permission = returnPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), TOUCH_PERMISSION);
                dialogs.add(new DialogObject(Boolean.parseBoolean(permission.getString("singlesensor")), permission.getJSONObject("message").getString(iLogApplication.getLocale()), permission.getJSONObject("confirmation").getString(iLogApplication.getLocale()), getDrawableByName(permission.getString("background")), permission.getString("permission"), Boolean.parseBoolean(permission.getString("skip")), permission.getInt("order"), permission.getJSONObject("title").getString(iLogApplication.getLocale())));
            }
            if(!iLogApplication.hasSinglePermission(Manifest.permission.READ_CONTACTS) && containsPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), Manifest.permission.GET_ACCOUNTS)) {
                JSONObject permission = returnPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), Manifest.permission.GET_ACCOUNTS);
                dialogs.add(new DialogObject(Boolean.parseBoolean(permission.getString("singlesensor")), permission.getJSONObject("message").getString(iLogApplication.getLocale()), permission.getJSONObject("confirmation").getString(iLogApplication.getLocale()), getDrawableByName(permission.getString("background")), permission.getString("permission"), Boolean.parseBoolean(permission.getString("skip")), permission.getInt("order"), permission.getJSONObject("title").getString(iLogApplication.getLocale())));
            }
            if(!iLogApplication.hasSinglePermission(Manifest.permission.READ_PHONE_STATE) && containsPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), Manifest.permission.READ_PHONE_STATE)) {
                JSONObject permission = returnPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), Manifest.permission.READ_PHONE_STATE);
                dialogs.add(new DialogObject(Boolean.parseBoolean(permission.getString("singlesensor")), permission.getJSONObject("message").getString(iLogApplication.getLocale()), permission.getJSONObject("confirmation").getString(iLogApplication.getLocale()), getDrawableByName(permission.getString("background")), permission.getString("permission"), Boolean.parseBoolean(permission.getString("skip")), permission.getInt("order"), permission.getJSONObject("title").getString(iLogApplication.getLocale())));
            }
            if(!iLogApplication.hasSinglePermission(Manifest.permission.READ_SMS) && containsPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), Manifest.permission.READ_SMS)) {
                JSONObject permission = returnPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), Manifest.permission.READ_SMS);
                dialogs.add(new DialogObject(Boolean.parseBoolean(permission.getString("singlesensor")), permission.getJSONObject("message").getString(iLogApplication.getLocale()), permission.getJSONObject("confirmation").getString(iLogApplication.getLocale()), getDrawableByName(permission.getString("background")), permission.getString("permission"), Boolean.parseBoolean(permission.getString("skip")), permission.getInt("order"), permission.getJSONObject("title").getString(iLogApplication.getLocale())));
            }

            JSONObject permission = returnPermission(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""), SUMMARY_PERMISSION);
            JSONObject message = new JSONObject();
            message.put("messagelow", permission.getJSONObject("messagelow").getString(iLogApplication.getLocale()));
            message.put("messagehigh", permission.getJSONObject("messagehigh").getString(iLogApplication.getLocale()));
            dialogs.add(new DialogObject(Boolean.parseBoolean(permission.getString("singlesensor")), message.toString(), permission.getJSONObject("confirmation").getString(iLogApplication.getLocale()), getDrawableByName(permission.getString("background")), permission.getString("permission"), Boolean.parseBoolean(permission.getString("skip")), permission.getInt("order"), permission.getJSONObject("title").getString(iLogApplication.getLocale())));
        }
        catch(JSONException e) {
            e.printStackTrace();
        }
        return dialogs;
    }

    /**
     * Method that checks if a permission is contained in the {@link JSONObject} of the project.
     * @param permissions String containing the {@link JSONObject} that characterizes the project
     * @param permission String identifying the permission to be checked
     * @return True if is contained, false otherwise
     */
    public boolean containsPermission(String permissions, String permission) {
        try {
            JSONArray jsonArray = new JSONArray(new JSONObject(permissions).getString("permissions"));

            for(int index = 0 ; index < jsonArray.length(); index++) {
                if(jsonArray.getJSONObject(index).getString("permission").equals(permission)) {
                    return true;
                }
            }
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method that returns the single permission from the {@link JSONObject} of the project.
     * @param permissions String containing the {@link JSONObject} that characterizes the project
     * @param permission String identifying the permission to be checked
     * @return {@link JSONObject} of the single permission requested
     */
    public JSONObject returnPermission(String permissions, String permission) {
        try {
            JSONArray jsonArray = new JSONArray(new JSONObject(permissions).getString("permissions"));

            for(int index = 0 ; index < jsonArray.length(); index++) {
                if(jsonArray.getJSONObject(index).getString("permission").equals(permission)) {
                    return jsonArray.getJSONObject(index);
                }
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method that returns the id of the drawable from the name
     * @param name String contaning the name of the drawable
     * @return Integer with the identifier of the drawable
     */
    public int getDrawableByName(String name) {
        Resources resources = this.getResources();
        return resources.getIdentifier(name, "drawable", this.getPackageName());
    }

    /**
     * Method called when the result of each permission is returned to the caller (this activity). When a result is received the following actions are taken:
     * <ul>
     *     <li>The {@link DialogObject#activationStatus} is updated with the status of the permission</li>
     *     <li>If the permission has been granted, we update che checkbox and the status of the next button</li>
     * </ul>
     * @param requestCode Integer representing the request code calling the method
     * @param resultCode Integer representing the result code of the action that calls this method
     * @param data {@link Intent} representing the data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case iLogApplication.CODE_RESULT_PERMISSIONS_BATTERY: {
                dialogs.get(selectedFragment-1).setActivationStatus(iLogApplication.hasBatteryIgnorePermission());
                if(iLogApplication.hasBatteryIgnorePermission()) {
                    PermissionsFragment.updateCheckbox();
                    buttonNextStatus(true);
                    buttonNextSetText(R.string.next);
                }
                return;
            }
            case iLogApplication.CODE_RESULT_PERMISSIONS_NOTIFICATION: {
                dialogs.get(selectedFragment-1).setActivationStatus(iLogApplication.hasNotificationAccessPermission());
                if(iLogApplication.hasNotificationAccessPermission()) {
                    PermissionsFragment.updateCheckbox();
                    buttonNextStatus(true);
                    buttonNextSetText(R.string.next);
                }
                return;
            }
            case iLogApplication.CODE_RESULT_PERMISSIONS_USAGE_STATS: {
                dialogs.get(selectedFragment-1).setActivationStatus(iLogApplication.hasUsageStatsPermission());
                if(iLogApplication.hasUsageStatsPermission()) {
                    PermissionsFragment.updateCheckbox();
                    buttonNextStatus(true);
                    buttonNextSetText(R.string.next);
                }
                return;
            }
            case iLogApplication.CODE_RESULT_PERMISSIONS_DRAW_ON_TOP: {
                dialogs.get(selectedFragment-1).setActivationStatus(iLogApplication.hasDrawOnTopPermissions());
                if(iLogApplication.hasDrawOnTopPermissions()) {
                    PermissionsFragment.updateCheckbox();
                    buttonNextStatus(true);
                    buttonNextSetText(R.string.next);
                }
                return;
            }
        }
    }

    /**
     * Method called when the result of the procedure of asking a permission is generated. This refers to the permission of using the Camera before taking a picture.
     * The results can be either {@link PackageManager#PERMISSION_GRANTED} or {@link PackageManager#PERMISSION_DENIED}. In the former situation, we start the camera,
     * in the latter we close the activity by calling {@link #finish()}. When a result is received the following actions are taken:
     * <ul>
     *     <li>The {@link DialogObject#activationStatus} is updated with the status of the permission</li>
     *     <li>If the permission has been granted, we update che checkbox and the status of the next button</li>
     * </ul>
     * @param requestCode request code is used to identify the caller
     * @param permissions list of permissions in this request. Not used in the method since we request only one permission ({@link Manifest.permission#CAMERA})
     * @param grantResults list of results, for each permission requested, in this case the list has only one result at position 0
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case iLogApplication.CODE_RESULT_PERMISSIONS_SENSORS: {
                dialogs.get(selectedFragment-1).setActivationStatus(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);

                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PermissionsFragment.updateCheckbox();
                    buttonNextStatus(true);
                    buttonNextSetText(R.string.next);
                    return;
                }
                else if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    PermissionsFragment.updateCheckbox();
                    return;
                }
            }
        }
    }

    /**
     * Method that returns the number of permissions granted by the user during the granting procedure
     * @return Integer with the number of granted permissions
     */
    public int returnApprovedPermissions() {
        int counter = 0;
        for(int index=0; index< dialogs.size();index++) {
            if(dialogs.get(index).isActivationStatus()) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Method that returns the total number of permissions to be granted by the user
     * @return Integer with the total number of permissions
     */
    public int returnDialogSize() {
        return dialogs.size();
    }

    public void updateXiaomiBattery() {
        dialogs.get(selectedFragment-1).setActivationStatus(true);
        PermissionsFragment.updateCheckbox();
        buttonNextStatus(true);
        buttonNextSetText(R.string.next);
    }
    /**
     * Custom class that meneges a single permission that has to be granted by the user
     */
    public class DialogObject implements Serializable {

        String message;
        String confirmation;
        int background;
        String title;
        String permission;
        boolean singleSensor;
        boolean activationStatus = false;
        boolean skipButton = false;
        int order = 0;

        public DialogObject(boolean singleSensor, String message, String confirmation, int background, String permission, boolean skipButton, int order, String title) {
            this.message = message;
            this.confirmation = confirmation;
            this.background = background;
            this.permission = permission;
            this.singleSensor = singleSensor;
            this.skipButton = skipButton;
            this.order = order;
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getOrder() {
            return order;
        }

        public boolean isActivationStatus() {
            return activationStatus;
        }

        public void setActivationStatus(boolean activationStatus) {
            this.activationStatus = activationStatus;
        }

        public boolean isSingleSensor() {
            return singleSensor;
        }


        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getBackground() {
            return background;
        }

        public void setBackground(int background) {
            this.background = background;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }

        public boolean isSkipButton() {
            return skipButton;
        }
    }
}

