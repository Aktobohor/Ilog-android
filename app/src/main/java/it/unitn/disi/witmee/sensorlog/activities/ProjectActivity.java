package it.unitn.disi.witmee.sensorlog.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.fragments.ContributionFragment;
import it.unitn.disi.witmee.sensorlog.model.Question;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Activity that presents the selected project to the user and allows him to perform the four actions required:
 * <ul>
 *     <li>Login with the Google Account</li>
 *     <li>Visualize and accept the informed consent</li>
 *     <li>Grant the necessary permissions</li>
 *     <li>Provide information about the user profile</li>
 * </ul>
 */
public class ProjectActivity extends Activity {

    private static final int INFORMED_CONSENT_RESULT_CODE = 666;
    private static final int PERMISSIONS_RESULT_CODE = 777;
    private static final int PROFILE_RESULT_CODE = 888;

    CheckedTextView consentTextView;
    CheckedTextView profileTextView;
    CheckedTextView permissionsTextView;
    CheckedTextView loginTextView;
    TextView confirmationTextView;

    /**
     * Default method called when the Activity is created. It is mainly used to initialize the variables. Since it is a FragmentActivity it uses Fragments
     * to display content to the users and specifically to {@link ContributionFragment}
     * @param savedInstanceState what
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_presentation_activity);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (Build.VERSION.SDK_INT >= 18 && !wifiManager.isScanAlwaysAvailable()) {
            startActivityForResult(new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE), 3);
        }

        try {
            final JSONObject project = new JSONObject(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""));

            //Once the user selects a project, we need to subscribe him to the project's topic
            FirebaseMessaging.getInstance().subscribeToTopic(project.getString("id"));

            //Set the title of the project
            TextView welcomeTextView = (TextView) findViewById(R.id.welcomeTextView);
            String title = project.getString("title");
            welcomeTextView.setText(new JSONObject(title).getString(iLogApplication.getLocale()));

            //Set the description of the project
            final TextView descriptionTextView = (TextView) findViewById(R.id.informedConsentTextView);
            String description = project.getString("description");
            descriptionTextView.setText(new JSONObject(description).getString(iLogApplication.getLocale()));
            descriptionTextView.setMovementMethod(new ScrollingMovementMethod());

            /**
             * If the user did not already do it, execute the {@link InformedConsentActivity} for results with {@link startActivityForResult(android.content.Intent, int)}
             */
            consentTextView = (CheckedTextView) findViewById(R.id.consentTextView);
            consentTextView.setText(getResources().getString(R.string.consent_message));
            consentTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //startactivityforresult informedConsent without closing this one
                    if(!iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_CONSENTDONE, false)) {
                        Intent informedConsentIntent = new Intent(ProjectActivity.this, InformedConsentActivity.class);
                        startActivityForResult(informedConsentIntent, INFORMED_CONSENT_RESULT_CODE);
                    }
                }
            });

            /**
             * Ask the user to loign with her Google account
             */
            loginTextView = (CheckedTextView) findViewById(R.id.loginTextView);
            loginTextView.setText(getResources().getString(R.string.login_title));
            loginTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Request the user to login
                    iLogApplication.requestUserLogin(ProjectActivity.this);
                }
            });

            /**
             * If the user did not already do it, execute the {@link ProfileActivity} for results with {@link startActivityForResult(android.content.Intent, int)}
             */
            profileTextView = (CheckedTextView) findViewById(R.id.profileTextView);
            profileTextView.setText(getResources().getString(R.string.profile_message));
            profileTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_PROFILEDONE, false)) {
                        //create question
                        Intent profileIntent = new Intent(ProjectActivity.this, ProfileActivity.class);
                        try {
                            profileIntent.putExtra("question", generateEmptyQuestion(new JSONArray(project.getString("profile")), System.currentTimeMillis(), "profile"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivityForResult(profileIntent, PROFILE_RESULT_CODE);
                    }
                }
            });

            /**
             * If the user did not already do it, execute the {@link PermissionsActivity} for results with {@link startActivityForResult(android.content.Intent, int)}
             */
            permissionsTextView = (CheckedTextView) findViewById(R.id.permissionsTextView);
            permissionsTextView.setText(getResources().getString(R.string.permissions_message));
            permissionsTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_PERMISSIONSDONE, false)) {
                        Intent permissionsIntent = new Intent(ProjectActivity.this, PermissionsActivity.class);
                        startActivityForResult(permissionsIntent, PERMISSIONS_RESULT_CODE);
                    }
                }
            });

            //When everything is done and the user clicks on the confirmation, restart the application and start logging
            confirmationTextView = (TextView) findViewById(R.id.confirmationTextView);
            confirmationTextView.setText(iLogApplication.getAppContext().getResources().getString(R.string.confirm_message));
            confirmationTextView.setVisibility(View.INVISIBLE);
            confirmationTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iLogApplication.closeApplicationSafelyAndRestart();
                }
            });

            //When the activity is created, check if the user already partially completed the procedure (in other sessions), and update the checkboxes and textviews accordingly
            if(iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_LOGINDONE, false)) {
                loginTextView.setChecked(true);
                consentTextView.setEnabled(true);
            }
            if(iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_CONSENTDONE, false)) {
                consentTextView.setChecked(true);
                permissionsTextView.setEnabled(true);
            }
            if(iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_PERMISSIONSDONE, false)) {
                permissionsTextView.setChecked(true);
                profileTextView.setEnabled(true);
            }
            if(iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_PROFILEDONE, false)) {
                profileTextView.setChecked(true);
                confirmationTextView.setVisibility(View.VISIBLE);
            }

            /**
             * If the project does not require the user to input her profile info, update the {@link iLogApplication#sharedPreferences}
             */
            if(!project.has("profile")) {
                profileTextView.setVisibility(View.INVISIBLE);
                iLogApplication.sharedPreferences.edit().putBoolean(Utils.CONFIG_NOPROFILE, true).commit();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method called when the result of each permission is returned to the caller (this activity). When a result is received the following actions are taken:
     * <ul>
     *     <li>Update the corresponding {@link iLogApplication#sharedPreferences}</li>
     *     <li>Update checkboxes and textviews</li>
     * </ul>
     * @param requestCode Integer representing the request code calling the method
     * @param resultCode Integer representing the result code of the action that calls this method
     * @param data {@link Intent} representing the data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INFORMED_CONSENT_RESULT_CODE: {
                iLogApplication.sharedPreferences.edit().putBoolean(Utils.CONFIG_CONSENTDONE, true).commit();
                consentTextView.setChecked(true);
                permissionsTextView.setEnabled(true);
                return;
            }
            case PERMISSIONS_RESULT_CODE: {
                if(resultCode == Activity.RESULT_OK) {
                    if(!iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_NOPROFILE, false)) {
                        iLogApplication.sharedPreferences.edit().putBoolean(Utils.CONFIG_PERMISSIONSDONE, true).commit();
                        permissionsTextView.setChecked(true);
                        profileTextView.setEnabled(true);
                    }
                    else {
                        iLogApplication.sharedPreferences.edit().putBoolean(Utils.CONFIG_PERMISSIONSDONE, true).commit();
                        iLogApplication.sharedPreferences.edit().putBoolean(Utils.CONFIG_PROFILEDONE, true).commit();
                        permissionsTextView.setChecked(true);
                        confirmationTextView.setVisibility(View.VISIBLE);
                    }

                    return;
                }
                else {
                    return;
                }
            }
            case PROFILE_RESULT_CODE: {
                if(resultCode == Activity.RESULT_OK) {
                    iLogApplication.sharedPreferences.edit().putBoolean(Utils.CONFIG_PROFILEDONE, true).commit();
                    profileTextView.setChecked(true);
                    confirmationTextView.setVisibility(View.VISIBLE);
                    return;
                }
                else {
                    return;
                }
            }
            case iLogApplication.CODE_RESULT_ACCOUNT: {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
                return;
            }
        }
    }

    /**
     * Method that handles the results of the sign in process with the Google account. When the result is received, we need to update the {@link FirebaseInstanceId} on the server.
     * @param completedTask {@link Task} from the {@link GoogleSignInAccount}
     */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(this.toString(), "signInResult: " + account.getEmail());
            Log.d(this.toString(), "signInResult: " + account.getId());

            final JSONObject project = new JSONObject(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""));
            new HttpAsyncTask().execute(account.getIdToken(), FirebaseInstanceId.getInstance().getToken(), project.getString("id"));

        } catch (ApiException e) {
            e.printStackTrace();
            if(e.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED && !iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, "").equals("")) {
                iLogApplication.startSignInActivity();
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
            Log.d(this.toString(), data[0]);
            //{"role":"superuser","salt":"e42b47a699d3a451957f711c8cfb6e3d8d89cab6"}
            returns.add(iLogApplication.SIGNUP(data[0], data[1], data[2], iLogApplication.sharedPreferences.getString(Utils.CONFIG_ENDPOINTSIGNUP, "")));
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

                    iLogApplication.sharedPreferences.edit().putBoolean(Utils.CONFIG_LOGINDONE, true).commit();

                    loginTextView.setChecked(true);
                    consentTextView.setEnabled(true);
                } catch (JSONException e) {
                    iLogApplication.sharedPreferences.edit().putString(Utils.ROLE_KEY, "").commit();

                    iLogApplication.signOut(ProjectActivity.this);

                    Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getResources().getString(R.string.loginError), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                    loginTextView.setChecked(false);
                    consentTextView.setEnabled(false);
                }
            }
            else {
                iLogApplication.sharedPreferences.edit().putString(Utils.ROLE_KEY, "").commit();

                iLogApplication.signOut(ProjectActivity.this);

                Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getResources().getString(R.string.loginError), Toast.LENGTH_SHORT).show();

                loginTextView.setChecked(false);
                consentTextView.setEnabled(false);
            }
        }
    }

    /**
     * Method that generates an empty {@link Question} and is used to ask the user her profile information
     * @param receivedQuestions String containing the content of the profile to be visualized
     * @param questionTimestamp The time when the profile info was received
     * @param questiondid String containing the id of the question
     * @return a {@link Question} object containing the questions about the user profile
     * @throws JSONException
     */
    private Question generateEmptyQuestion(JSONArray receivedQuestions, long questionTimestamp, String questiondid) throws JSONException {
        Question question = new Question(questionTimestamp, System.currentTimeMillis(), 86400, questiondid, receivedQuestions.toString(), Question.STATUS_RECEIVED, "Profile", Question.SYNCHRONIZATION_FALSE);
        return question;
    }
}
