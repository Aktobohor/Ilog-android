package it.unitn.disi.witmee.sensorlog.fragments;

/**
 * Created by mattiazeni on 5/23/17.
 */

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.activities.PermissionsActivity;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Class that extends {@link Fragment} and is intended to display the content to be presented to the user and to collect the permissions
 */
public class PermissionsFragment extends Fragment {

    private static PermissionsActivity.DialogObject permission = null;
    private static CheckedTextView authorizeTextView = null;

    /**
     * Method called when the view of the {@link Fragment} is created. It initializes the variables and the buttons in the optionmenu. Finally, depending on the type of subquestion
     * that needs to be visualized, it populates the view.
     * @param inflater default
     * @param container default
     * @param savedInstanceState default
     * @return default {@link View}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = null;
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_permission, container, false);

        //Show the title and the description
        TextView titleTextView = (TextView) rootView.findViewById(R.id.titleTextView);
        TextView descriptionTextView = (TextView) rootView.findViewById(R.id.descriptionTextView);
        if(permission.getPermission().equals("summary_permission")) {
            titleTextView.setVisibility(View.INVISIBLE);
            if(((PermissionsActivity) getActivity()).returnApprovedPermissions() < ((PermissionsActivity) getActivity()).returnDialogSize()/2) {
                try {
                    descriptionTextView.setText(String.format(new JSONObject(permission.getMessage()).getString("messagelow"), ((PermissionsActivity) getActivity()).returnApprovedPermissions(), (((PermissionsActivity) getActivity()).returnDialogSize()-1)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    descriptionTextView.setText(new JSONObject(permission.getMessage()).getString("messagehigh"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            titleTextView.setText(permission.getTitle());
            descriptionTextView.setText(permission.getMessage());
        }

        //When the user clicks on the authorize text view, depending on the permission perform different actions
        authorizeTextView = (CheckedTextView) rootView.findViewById(R.id.authorizeTextView);
        authorizeTextView.setText(iLogApplication.getAppContext().getResources().getString(R.string.authorize));
        authorizeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!permission.isActivationStatus()) {
                    if(permission.isSingleSensor()) {
                        iLogApplication.requestSinglePermission(permission.getPermission(), (PermissionsActivity) getActivity());
                    }
                    else {
                        switch (permission.getPermission()) {
                            case PermissionsActivity.BATTERY_PERMISSION: {
                                String deviceMan = android.os.Build.MANUFACTURER;
                                if(deviceMan.equals("Xiaomi")) {
                                    ((PermissionsActivity) getActivity()).updateXiaomiBattery();
                                }
                                else {
                                    iLogApplication.requestBatteryPermission((PermissionsActivity) getActivity());
                                }

                                return;
                            }
                            //Wifi is a "fake" permission, it's just a flag saved in the shared preferences of the application
                            case PermissionsActivity.WIFI_PERMISSION: {
                                if(!iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_UPLOAD_IF_WIFI, false)) {
                                    iLogApplication.sharedPreferences.edit().putBoolean(Utils.CONFIG_UPLOAD_IF_WIFI, true).commit();
                                    authorizeTextView.setChecked(true);
                                    ((PermissionsActivity) getActivity()).buttonNextStatus(true);
                                    ((PermissionsActivity) getActivity()).buttonNextSetText(R.string.next);
                                }
                                return;
                            }
                            case PermissionsActivity.NOTIFICATIONS_PERMISSION: {
                                iLogApplication.requestNotificationAccessPermission((PermissionsActivity) getActivity());
                                return;
                            }
                            case PermissionsActivity.APPLICATIONS_PERMISSION: {
                                iLogApplication.requestUsageStatsPermission((PermissionsActivity) getActivity());
                                return;
                            }
                            case PermissionsActivity.TOUCH_PERMISSION: {
                                iLogApplication.requestDrawOnTopPermission((PermissionsActivity) getActivity());
                                return;
                            }
                            //Is a permission always present, that summarizes how many permissions the user provided
                            case PermissionsActivity.SUMMARY_PERMISSION: {
                                iLogApplication.sharedPreferences.edit().putBoolean(Utils.CONFIG_PERMISSIONSDONE, true).commit();
                                iLogApplication.stopLogging("close");
                                iLogApplication.startLogging();
                                Intent returnIntent = new Intent();
                                ((PermissionsActivity) getActivity()).setResult(Activity.RESULT_OK, returnIntent);
                                ((PermissionsActivity) getActivity()).finishAndRemoveTask();
                                return;
                            }
                        }
                    }
                }
            }
        });

        //Image used to make the process more user friendly
        ImageView logoImageView = (ImageView) rootView.findViewById(R.id.imageView);
        logoImageView.setImageResource(permission.getBackground());

        ((PermissionsActivity) getActivity()).buttonNextSetText(R.string.next);
        ((PermissionsActivity) getActivity()).buttonNextStatus(false);

        if(permission.isSkipButton()) {
            ((PermissionsActivity) getActivity()).buttonNextSetText(R.string.skip);
            ((PermissionsActivity) getActivity()).buttonNextStatus(true);
        }
        else {
            ((PermissionsActivity) getActivity()).buttonNextStatus(false);
        }

        if(permission.getPermission().equals("summary_permission")) {
            authorizeTextView.setVisibility(View.INVISIBLE);
            ((PermissionsActivity) getActivity()).buttonNextStatus(true);
            ((PermissionsActivity) getActivity()).buttonNextSetText(R.string.finish);
        }
        return rootView;
    }

    public void setDialog(PermissionsActivity.DialogObject object) {
        this.permission = object;
    }

    @Override
    public void onDestroy() {
        Log.d(this.getClass().getSimpleName(), "Destroy");
        super.onDestroy();
    }

    @Override
    public void onPause() {
        Log.d(this.getClass().getSimpleName(), "Pause");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(this.getClass().getSimpleName(), "Resume");
        super.onResume();
    }

    @Override
    public void onStop() {
        Log.d(this.getClass().getSimpleName(), "Stop");
        super.onStop();
    }

    /**
     * Method to update the checkbox of the permission
     */
    public static void updateCheckbox() {
        authorizeTextView.setChecked(permission.isActivationStatus());
    }
}

