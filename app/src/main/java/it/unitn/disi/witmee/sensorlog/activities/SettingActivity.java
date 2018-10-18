package it.unitn.disi.witmee.sensorlog.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.Message;
import it.unitn.disi.witmee.sensorlog.utils.Utils;
import it.unitn.disi.witmee.sensorlog.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Activity that extends {@link PreferenceActivity} that displays the Settings menu about the main settings of the app. In this menu, the user can visualize information about the
 * logged in user, about the permission she granted (and allows her to grant more), information about the logs generated, allows to reset the application and the current experiment
 * subscription and finally shows the application version.
 */
public class SettingActivity extends PreferenceActivity {

    private static boolean isConnected = false;
    Timer timer;

    SharedPreferences sharedPreferences = iLogApplication.getAppContext().getSharedPreferences(Utils.PACKAGE_NAME, Context.MODE_PRIVATE);

    private static Preference logsSync, logsDeleteArchive, logsDumpArchive;
    PreferenceScreen /*logsScreen,*/ permissionScreen, mainPreference;
    Preference loginPreference, logsAppVersion, resetApp;
    SwitchPreference batteryPermission, locationPermission, audioPermission, contactsPermission, smsPermission, phonePermission,
            notificationsPermission, applicationPermission, touchPermission, cameraPermission, wifiPermission;
    SwitchPreference selectedPreference;
    ListPreference listPreference;
    PreferenceCategory snooze, data;

    int counter = 0;

    /**
     * Method called when the Activity is created,in it we initialize the variables and the view.
     * @param savedInstanceState default {@link Bundle}
     */
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(this.toString(), "Created");

        // Load the setting from an XML resource
        addPreferencesFromResource(R.xml.setting);
        
        getActionBar().setTitle(getResources().getString(R.string.settings));

        loginPreference = (Preference) findPreference(getString(R.string.pref_login_button));
        logsSync = (Preference) findPreference(getString(R.string.pref_log_sync));
        logsDeleteArchive = (Preference) findPreference(getResources().getString(R.string.pref_log_delete_archive));
        logsDumpArchive = (Preference) findPreference("pref_log_dump_archive");
        resetApp = (Preference) findPreference("pref_log_reset_app");

        listPreference = (ListPreference) findPreference("pref_notifications_sleep");

        logsAppVersion = (Preference) findPreference("pref_log_app_version");
        permissionScreen = (PreferenceScreen) findPreference("permissions_main_preferencescreen");
        mainPreference = (PreferenceScreen) findPreference("pref_main_preferencescreen");

        batteryPermission = (SwitchPreference) findPreference("battery_permission_key");
        locationPermission = (SwitchPreference) findPreference("location_permission_title");
        audioPermission = (SwitchPreference) findPreference("audio_permission_title");
        contactsPermission = (SwitchPreference) findPreference("contacts_permission_title");
        smsPermission = (SwitchPreference) findPreference("sms_permission_key");
        phonePermission = (SwitchPreference) findPreference("phone_permission_title");
        notificationsPermission = (SwitchPreference) findPreference("notifications_permission_title");
        applicationPermission = (SwitchPreference) findPreference("applications_permission_title");
        touchPermission = (SwitchPreference) findPreference("touch_permission_title");
        cameraPermission = (SwitchPreference) findPreference("camera_permission_title");
        wifiPermission = (SwitchPreference) findPreference("wifi_permission_title");

        snooze = (PreferenceCategory) findPreference("pref_category_notifications_sleep");
        data = (PreferenceCategory) findPreference("pref_category_data");

        //Timer used to update the content of the menu - TODO: is this still needed? Check
        timer = new Timer();
        TimerTask task = new TimerTask() {  
	        @Override  
			public void run() {  
	        	runOnUiThread(new Runnable() {  
	        		@Override  
	        		public void run() { 
	        			updateUI();
	        		}  
	        	});  
	        }  
        };
        timer.scheduleAtFixedRate(task, 0, 1000);

        logsSync.setOnPreferenceClickListener (new OnPreferenceClickListener() {
        	
        	@Override
            public boolean onPreferenceClick(Preference preference) {
        		iLogApplication.uploadAllIfConnected();
                return false;
            }
        });

        //When pressed, all the logs stored on the device and not synchronized yet are deleted, after a final confirmation by the user
        logsDeleteArchive.setOnPreferenceClickListener (new OnPreferenceClickListener() {
        	
        	@Override
            public boolean onPreferenceClick(Preference preference) {
        		AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setMessage(getResources().getString(R.string.deleteArchive))
                        .setPositiveButton(getResources().getString(R.string.affirmativeAnswer), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                boolean isDeleted = deleteLogFiles();
                                dialog.dismiss();
                                if (isDeleted) {
                                	Toast.makeText(SettingActivity.this, getResources().getString(R.string.archiveDeleteSuccess), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SettingActivity.this, getResources().getString(R.string.archiveDeleteError), Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancelAnswer), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
                return false;
            }
        });

        //Debug button used to dump the logs to the external memory and being able to transfer them manually on the pc
        logsDumpArchive.setOnPreferenceClickListener (new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(!iLogApplication.hasSinglePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    iLogApplication.requestSinglePermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, SettingActivity.this);
                }
                else {
                    new FileAsyncTask().execute();
                }
                return false;
            }
        });

        //Used by the user to login/logout
        loginPreference.setOnPreferenceClickListener (new OnPreferenceClickListener() {
        	
        	@Override
            public boolean onPreferenceClick(Preference preference) {

                if(!iLogApplication.isUserLoggedIn()) {
                    iLogApplication.requestUserLogin(SettingActivity.this);
                }
                else {
                    showLogoutDialog();
                }

		        return false;
            }
        });

        /**
         * {@link PreferenceScreen} showing the permissions the user granted and allows also to grant the remaining ones, if any. If a permission is already granted and the
         * user tries to remove them she is prompted with a message that redirects her to the Android Settings.
         */
        permissionScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                updateSwitchPreferencesPermissions();

                return true;
            }
        });

        //List of single permissions to be granted
        batteryPermission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean switched = ((SwitchPreference) preference).isChecked();
                selectedPreference = (SwitchPreference) preference;
                if(switched) {
                    iLogApplication.requestBatteryPermission(SettingActivity.this);
                }
                else {
                    disablePermission((SwitchPreference) preference);
                }
                return true;
            }
        });
        locationPermission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean switched = ((SwitchPreference) preference).isChecked();
                selectedPreference = (SwitchPreference) preference;
                if(switched) {
                    iLogApplication.requestSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION, SettingActivity.this);
                }
                else {
                    disablePermission((SwitchPreference) preference);
                }
                return true;
            }
        });
        audioPermission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean switched = ((SwitchPreference) preference).isChecked();
                selectedPreference = (SwitchPreference) preference;
                if(switched) {
                    iLogApplication.requestSinglePermission(Manifest.permission.RECORD_AUDIO, SettingActivity.this);
                }
                else {
                    disablePermission((SwitchPreference) preference);
                }
                return true;
            }
        });
        contactsPermission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean switched = ((SwitchPreference) preference).isChecked();
                selectedPreference = (SwitchPreference) preference;
                if(switched) {
                    iLogApplication.requestSinglePermission(Manifest.permission.READ_CONTACTS, SettingActivity.this);
                }
                else {
                    disablePermission((SwitchPreference) preference);
                }
                return true;
            }
        });
        smsPermission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean switched = ((SwitchPreference) preference).isChecked();
                selectedPreference = (SwitchPreference) preference;
                if(switched) {
                    iLogApplication.requestAllSinglePermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, SettingActivity.this);
                }
                else {
                    disablePermission((SwitchPreference) preference);
                }
                return true;
            }
        });
        phonePermission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean switched = ((SwitchPreference) preference).isChecked();
                selectedPreference = (SwitchPreference) preference;
                if(switched) {
                    iLogApplication.requestAllSinglePermissions(new String[]{Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.READ_PHONE_STATE}, SettingActivity.this);
                }
                else {
                    disablePermission((SwitchPreference) preference);
                }
                return true;
            }
        });
        notificationsPermission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean switched = ((SwitchPreference) preference).isChecked();
                selectedPreference = (SwitchPreference) preference;
                if(switched) {
                    iLogApplication.requestNotificationAccessPermission(SettingActivity.this);
                }
                else {
                    disablePermission((SwitchPreference) preference);
                }
                return true;
            }
        });
        applicationPermission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean switched = ((SwitchPreference) preference).isChecked();
                selectedPreference = (SwitchPreference) preference;
                if(switched) {
                    iLogApplication.requestUsageStatsPermission(SettingActivity.this);
                }
                else {
                    disablePermission((SwitchPreference) preference);
                }
                return true;
            }
        });
        touchPermission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean switched = ((SwitchPreference) preference).isChecked();
                selectedPreference = (SwitchPreference) preference;
                if(switched) {
                    iLogApplication.requestDrawOnTopPermission(SettingActivity.this);
                }
                else {
                    disablePermission((SwitchPreference) preference);
                }
                return true;
            }
        });
        cameraPermission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean switched = ((SwitchPreference) preference).isChecked();
                selectedPreference = (SwitchPreference) preference;
                if(switched) {
                    iLogApplication.requestSinglePermission(Manifest.permission.CAMERA, SettingActivity.this);
                }
                else {
                    disablePermission((SwitchPreference) preference);
                }
                return true;
            }
        });
        wifiPermission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean switched = ((SwitchPreference) preference).isChecked();
                selectedPreference = (SwitchPreference) preference;
                if(switched) {
                    iLogApplication.sharedPreferences.edit().putBoolean(Utils.CONFIG_UPLOAD_IF_WIFI, true).commit();
                }
                else {
                    iLogApplication.sharedPreferences.edit().putBoolean(Utils.CONFIG_UPLOAD_IF_WIFI, false).commit();
                }
                return true;
            }
        });

        /**
         * Shows the version of the application. We created a procedure similar to the one Adnroid uses to enable the developer mode. If the user clicks this button for 5 times,
         * the {@link #logsDumpArchive} is activated.
         */
        logsAppVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                counter++;
                System.out.println(counter);

                if(counter > 2 && (8 - counter) != 0) {
                    final Toast toast = Toast.makeText(SettingActivity.this, iLogApplication.getAppContext().getResources().getString(R.string.enable_dump, String.valueOf(8 - counter)), Toast.LENGTH_SHORT);
                    toast.show();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            toast.cancel();
                        }
                    }, 500);

                }
                if(counter == 8) {
                    data.addPreference(logsDumpArchive);
                }

                return true;
            }
        });

        //Functionality requested by the Socioogy Department to stop the notification for a certain amount of time..
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue){
                // as before
                Log.d(this.toString(), "VALUE: "+newValue.toString());

                iLogApplication.sharedPreferences.edit().putInt(Utils.CONFIG_SLEEP_INTERVAL_HOURS, Integer.valueOf(newValue.toString())).commit();
                iLogApplication.sharedPreferences.edit().putLong(Utils.CONFIG_SLEEP_TILL, System.currentTimeMillis() + Integer.valueOf(newValue.toString())*(60*60*1000)).commit();

                if(Integer.valueOf(newValue.toString()) != 0) {
                    listPreference.setSummary(String.format(iLogApplication.getAppContext().getResources().getString(R.string.notification_settings_subtitle_for), Integer.valueOf(listPreference.getValue()), Utils.longToStringFormatTime(iLogApplication.sharedPreferences.getLong(Utils.CONFIG_SLEEP_TILL, 0))));
                    Toast.makeText(SettingActivity.this, String.format(iLogApplication.getAppContext().getResources().getString(R.string.notification_settings_toast), Utils.longToStringFormatTime(iLogApplication.sharedPreferences.getLong(Utils.CONFIG_SLEEP_TILL, 0))), Toast.LENGTH_LONG).show();
                }
                else {
                    listPreference.setSummary(iLogApplication.getAppContext().getResources().getString(R.string.notification_settings_subtitle));
                    Toast.makeText(SettingActivity.this, iLogApplication.getAppContext().getResources().getString(R.string.notification_settings_toast_reset), Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });

        //Resets the application, all the databases, shared preferences and deletes all the logs. Before removing everything, it asks the user for confirmation.
        resetApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                showConfirmationDialog();

                return true;
            }
        });

        long timestamp = System.currentTimeMillis();

        //if the sleep_till is passed
        if(iLogApplication.sharedPreferences.getLong(Utils.CONFIG_SLEEP_TILL, 0) <= timestamp) {
            System.out.println("Expired");
            listPreference.setValue(String.valueOf(0));
            listPreference.setSummary(iLogApplication.getAppContext().getResources().getString(R.string.notification_settings_subtitle));
        }
        else {
            System.out.println("NOT Expired");
            listPreference.setValue(String.valueOf(iLogApplication.sharedPreferences.getInt(Utils.CONFIG_SLEEP_INTERVAL_HOURS, 0)));
            listPreference.setSummary(String.format(iLogApplication.getAppContext().getResources().getString(R.string.notification_settings_subtitle_for), Integer.valueOf(listPreference.getValue()), Utils.longToStringFormatTime(iLogApplication.sharedPreferences.getLong(Utils.CONFIG_SLEEP_TILL, 0))));
        }

        if(!iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_SNOOZENOTIFICATIONS, false)) {
            mainPreference.removePreference(snooze);
        }
        else {
            mainPreference.addPreference(snooze);
        }

        if(isUserSuperuser() || isTester()) {
            data.removePreference(logsDeleteArchive);
            data.removePreference(logsDumpArchive);
        }
        else {
            data.removePreference(logsDeleteArchive);
            data.removePreference(logsDumpArchive);
        }
    }

    @Override
    public void onPause() {
    	super.onPause();
        timer.cancel();

        Log.d(this.toString(), "PAUSED");
    }
    
    @Override
    public void onResume(){
        super.onResume();

        Log.d(this.toString(), "RESUMED");

        timer = new Timer();
        TimerTask task = new TimerTask() {  
	        @Override  
			public void run() {  
	        	runOnUiThread(new Runnable() {  
	        		@Override  
	        		public void run() { 
	        			updateUI();
	        		}  
	        	});  
	        }  
        };
        timer.scheduleAtFixedRate(task, 0, 2000);
    }

    @Override
    protected void onStop() {
        Log.d(this.getClass().getSimpleName(), "Stop");
        finish();
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        Log.d(this.toString(), "DESTROYED");
    }

    /**
     * Method used to periodically update the UI
     */
    private void updateUI () {

        isConnected = iLogApplication.isNetworkConnected();

        PackageInfo packageInfo = null;
        try {
            packageInfo = iLogApplication.getAppContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            logsAppVersion.setSummary(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(iLogApplication.isUserLoggedIn()) {
            loginPreference.setSummary(getResources().getString(R.string.loginAs)+" "+iLogApplication.getUserName());
        }
        else {
            loginPreference.setSummary(getResources().getString(R.string.loginAsk));
        }

		setUpSummary();
    }

    /**
     * Method used to periodically update the UI about the current logs available (or not)
     */
    public static void setUpSummary() {
        if (isConnected && iLogApplication.isUserLoggedIn()) {
            if (getLogFilesNumber()==0) {
                logsSync.setSummary(iLogApplication.getAppContext().getResources().getString(R.string.syncNoFiles));
                logsSync.setEnabled(false);

                logsDeleteArchive.setEnabled(false);
                //logsDumpArchive.setEnabled(false);
            }
            else {
                logsSync.setSummary(getLogFilesNumber() + " " +iLogApplication.getAppContext().getResources().getString(R.string.syncFiles));
                logsSync.setEnabled(true);

                logsDeleteArchive.setEnabled(true);
                //logsDumpArchive.setEnabled(true);
            }
        }
        else {
            if (getLogFilesNumber()==0) {
                logsSync.setSummary(iLogApplication.getAppContext().getResources().getString(R.string.syncNoFiles));
                logsSync.setEnabled(false);

                logsDeleteArchive.setEnabled(false);
                //logsDumpArchive.setEnabled(false);
            }
            else {
                logsSync.setSummary(getLogFilesNumber() + " " +iLogApplication.getAppContext().getResources().getString(R.string.syncFiles));
                logsSync.setEnabled(false);

                logsDeleteArchive.setEnabled(false);
                //logsDumpArchive.setEnabled(false);
            }
        }
    }

    /**
     * Method that checks the amount of log available
     * @return Integer representing the number of logs available
     */
    public static int getLogFilesNumber() {
        String[] files = iLogApplication.getLogDirectory().list();
        ArrayList<String> zippedFiles = new ArrayList<String>();
        if(files!=null) {
            for(String f : files) {
                if(f.contains(iLogApplication.sharedPreferences.getString(Utils.CONFIG_COMPRESSEDLOGEXTENSION, ""))) {
                    zippedFiles.add(f);
                }
            }
        }

        return zippedFiles.size();
    }

    /**
     * Method used to delete the logs, if any
     * @return True if all the logs have been deleted, false otherwise
     */
    public static boolean deleteLogFiles() {
        boolean isAllDeleted = true;
        for (File f : iLogApplication.getLogDirectory().listFiles()) {
            if (f.isFile()) {
                if (!f.delete()) {
                    isAllDeleted = false;
                }
            }
        }
        SettingActivity.setUpSummary();
        return isAllDeleted;
    }

    /**
     * Method used to show the dialog that asks the user to logout
     */
    public void showLogoutDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(SettingActivity.this);
        View promptView = layoutInflater.inflate(R.layout.logout_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingActivity.this);
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setTitle(getResources().getString(R.string.userInformation));

        final TextView usernameText = (TextView) promptView.findViewById(R.id.usernametext);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null) {
            usernameText.setText(account.getEmail());
        }

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.logout), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        iLogApplication.signOut(SettingActivity.this);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancelAnswer),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /**
     * Method used to show the dialog that asks the user for a confirmation about the reset of the application
     */
    public void showConfirmationDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(SettingActivity.this);
        View promptView = layoutInflater.inflate(R.layout.logout_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SettingActivity.this);
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setTitle(getResources().getString(R.string.app_deletion_title));

        final TextView usernameText = (TextView) promptView.findViewById(R.id.usernametext);
        usernameText.setText(getResources().getString(R.string.app_deletion_message));

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.reset), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //ask confirmation
                        iLogApplication.sharedPreferences.edit().clear().commit();
                        iLogApplication.clearDatabase();
                        Toast.makeText(SettingActivity.this, getString(R.string.app_resetted_message), Toast.LENGTH_LONG).show();
                        iLogApplication.closeApplicationSafelyAndRestart();
                        //delete logs

                        //reset google login
                        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(SettingActivity.this, iLogApplication.gso);
                        mGoogleSignInClient.revokeAccess();

                        //reset permissions


                        //delete tasks
                        iLogApplication.db.deleteAllTasks();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancelAnswer),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /**
     * Method that detects if the current user is superuser or not
     * @return True if the user is superuser, false otherwise
     */
    public boolean isUserSuperuser() {
        if(sharedPreferences.getString(Utils.ROLE_KEY, "").equals("superuser")) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Method that detects if the current user is a tester or not
     * @return True if the user is tester, false otherwise
     */
    public boolean isTester() {
        if(sharedPreferences.getString(Utils.ROLE_KEY, "").equals("tester")) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Method that updates the switch preferences about the permissions
     */
    private void updateSwitchPreferencesPermissions() {
        if(iLogApplication.hasBatteryIgnorePermission()) {
            batteryPermission.setChecked(true);
        }
        else {
            batteryPermission.setChecked(false);
        }
        if(iLogApplication.hasSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            locationPermission.setChecked(true);
        }
        else {
            locationPermission.setChecked(false);
        }
        if(iLogApplication.hasSinglePermission(Manifest.permission.RECORD_AUDIO)) {
            audioPermission.setChecked(true);
        }
        else {
            audioPermission.setChecked(false);
        }
        if(iLogApplication.hasSinglePermission(Manifest.permission.READ_CONTACTS)) {
            contactsPermission.setChecked(true);
        }
        else {
            contactsPermission.setChecked(false);
        }
        if(iLogApplication.hasSinglePermission(Manifest.permission.READ_SMS) && iLogApplication.hasSinglePermission(Manifest.permission.RECEIVE_SMS)) {
            smsPermission.setChecked(true);
        }
        else {
            smsPermission.setChecked(false);
        }
        if(iLogApplication.hasSinglePermission(Manifest.permission.READ_PHONE_STATE) && iLogApplication.hasSinglePermission(Manifest.permission.PROCESS_OUTGOING_CALLS)) {
            phonePermission.setChecked(true);
        }
        else {
            phonePermission.setChecked(false);
        }
        if(iLogApplication.hasNotificationAccessPermission()) {
            notificationsPermission.setChecked(true);
        }
        else {
            notificationsPermission.setChecked(false);
        }
        if(iLogApplication.hasUsageStatsPermission()) {
            applicationPermission.setChecked(true);
        }
        else {
            applicationPermission.setChecked(false);
        }
        if(iLogApplication.hasDrawOnTopPermissions()) {
            touchPermission.setChecked(true);
        }
        else {
            touchPermission.setChecked(false);
        }
        if(iLogApplication.hasSinglePermission(Manifest.permission.CAMERA)) {
            cameraPermission.setChecked(true);
        }
        else {
            cameraPermission.setChecked(false);
        }
        if(iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_UPLOAD_IF_WIFI, false)) {
            wifiPermission.setChecked(true);
        }
        else {
            wifiPermission.setChecked(false);
        }
    }

    /**
     * Method that presents a message to the user when she tries to disable a permission from inside the app
     * @param switchPreference {@link SwitchPreference} object
     */
    private void disablePermission(SwitchPreference switchPreference) {
        Toast.makeText(SettingActivity.this, getString(R.string.disable_permission_message), Toast.LENGTH_LONG).show();
        switchPreference.setChecked(true);
    }

    /**
     * Method called when the result of each permission is returned to the caller (this activity). When a result is received the corresponding {@link SwitchPreference} is enabled
     * or disabled accordingly.
     * @param requestCode Integer representing the request code calling the method
     * @param resultCode Integer representing the result code of the action that calls this method
     * @param data {@link Intent} representing the data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case iLogApplication.CODE_RESULT_PERMISSIONS_BATTERY: {
                if(iLogApplication.hasBatteryIgnorePermission()) {
                    selectedPreference.setChecked(true);
                }else{
                    selectedPreference.setChecked(false);
                }
                return;
            }
            case iLogApplication.CODE_RESULT_PERMISSIONS_NOTIFICATION: {
                if(iLogApplication.hasNotificationAccessPermission()) {
                    selectedPreference.setChecked(true);
                }else{
                    selectedPreference.setChecked(false);
                }
                return;
            }
            case iLogApplication.CODE_RESULT_PERMISSIONS_USAGE_STATS: {
                if(iLogApplication.hasUsageStatsPermission()) {
                    selectedPreference.setChecked(true);
                }else{
                    selectedPreference.setChecked(false);
                }
                return;
            }
            case iLogApplication.CODE_RESULT_PERMISSIONS_DRAW_ON_TOP: {
                if(iLogApplication.hasDrawOnTopPermissions()) {
                    selectedPreference.setChecked(true);
                }else{
                    selectedPreference.setChecked(false);
                }
                return;
            }
            case iLogApplication.CODE_RESULT_ACCOUNT: {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
                return;
            }
        }
    }

    /**
     * Method called when the result of the procedure of asking a permission is generated. This is valid for the permission that asks the user to write to the external storage.
     * Once granted, it starts copying the files to it.
     * @param requestCode request code is used to identify the caller
     * @param permissions list of permissions in this request. Not used in the method since we request only one permission ({@link Manifest.permission#CAMERA})
     * @param grantResults list of results, for each permission requested, in this case the list has only one result at position 0
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case iLogApplication.CODE_RESULT_PERMISSIONS_SENSORS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        new FileAsyncTask().execute();
                    }
                }

                return;
            }
        }
    }

    /**
     * {@link AsyncTask} that copies files from the internal to the external storage. It needs to be executed in the background due to the load and to not affect the UI
     */
    private class FileAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {}
        @Override
        protected Void doInBackground(Void... params) {
            File[] files = SettingActivity.this.getFilesDir().listFiles();
            for (int index = 0; index < files.length; index++) {
                if (files[index].getName().contains(".bz2")) {
                    Log.d(this.toString(), files[index].getName());
                    copyFileToSDCard(files[index]);
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {}
    }

    /**
     * Method that copies the files from the internal to the external storage, one by one
     * @param file {@link File} to be copied
     */
    public static void copyFileToSDCard(File file) {
        try {
            FileChannel inChannel = new FileInputStream(file).getChannel();
            FileChannel outChannel = new FileOutputStream(new File(Utils.returnAppDataPath()+iLogApplication.sharedPreferences.getString(Utils.CONFIG_LOGDIR, ""))+iLogApplication.sharedPreferences.getString(Utils.CONFIG_SEPARATOR, "")+file.getName()).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            file.delete();
        }
    }

    /**
     * Method that handles the results of the sign in process with the Google account. When the result is received, we need to update the {@link FirebaseInstanceId} on the server.
     * @param completedTask {@link Task} from the {@link GoogleSignInAccount}
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(this.toString(), "signInResult: " + account.getIdToken());

            new HttpAsyncTask().execute(account.getIdToken(), FirebaseInstanceId.getInstance().getToken());
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            iLogApplication.sharedPreferences.edit().putString(Utils.ROLE_KEY, "").commit();
            Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getResources().getString(R.string.loginError), Toast.LENGTH_SHORT).show();

            if(e.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED && !iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, "").equals("")) {
                iLogApplication.startSignInActivity();
            }

            Log.d(this.toString(), "signInResult:failed code=" + e.getStatusCode());
        }
    }

    /**
     * {@link AsyncTask} that performs the login on the serve and updates the {@link FirebaseInstanceId} that is needed to communicate with the phone, send messages that are received
     * in {@link it.unitn.disi.witmee.sensorlog.services.MyFirebaseMessagingService}.
     */
    public class HttpAsyncTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... data) {

            ArrayList<String> returns = new ArrayList<String>();
            returns.add(data[0]);//googletoken
            returns.add(data[1]);//firebasetoken
            //{"role":"superuser","salt":"e42b47a699d3a451957f711c8cfb6e3d8d89cab6"}
            returns.add(iLogApplication.GET(data[0], data[1], iLogApplication.sharedPreferences.getString(Utils.CONFIG_ENDPOINTLOGIN, "")));
            return returns;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(ArrayList<String> result) {
            Log.d(this.getClass().getSimpleName(), result.get(2));
            if(!result.get(2).equals("null")) {
                JSONObject response = null;
                try {
                    response = new JSONObject(result.get(2));
                    iLogApplication.sharedPreferences.edit().putString(Utils.ROLE_KEY, response.getString("role")).commit();

                    //iLogApplication.initFirebaseDatabase();

                    Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getString(R.string.loginSuccess), Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    iLogApplication.sharedPreferences.edit().putString(Utils.ROLE_KEY, "").commit();

                    iLogApplication.signOut(SettingActivity.this);

                    Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getResources().getString(R.string.loginError), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            }
            else {
                iLogApplication.sharedPreferences.edit().putString(Utils.ROLE_KEY, "").commit();

                iLogApplication.signOut(SettingActivity.this);

                Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getResources().getString(R.string.loginError), Toast.LENGTH_SHORT).show();
            }
        }
    }
}