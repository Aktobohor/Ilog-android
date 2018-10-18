package it.unitn.disi.witmee.sensorlog.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.elements.PreferenceAvailableBackground;
import it.unitn.disi.witmee.sensorlog.elements.PreferenceAvailableMap;
import it.unitn.disi.witmee.sensorlog.elements.PreferenceCompleted;
import it.unitn.disi.witmee.sensorlog.elements.PreferenceOngoing;
import it.unitn.disi.witmee.sensorlog.model.Answer;
import it.unitn.disi.witmee.sensorlog.model.Challenge;
import it.unitn.disi.witmee.sensorlog.model.Message;
import it.unitn.disi.witmee.sensorlog.model.Question;
import it.unitn.disi.witmee.sensorlog.model.Task;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Activity that extends {@link PreferenceActivity} that displays the Home menu about the user contributions. In this menu, the user can visualize all the available contributions,
 * reply to them, visualize the already replied ones.
 */
public class HomeActivity extends PreferenceActivity {

    PreferenceScreen tasksScreenUnsolved, tasksScreenSolved, tasksScreenExpired, timediariesScreenUnsolved, timediariesScreenSolved, timediariesScreenExpired, messagesScreenUnread, messagesScreenRead, messagesScreenExpired, challengesAvailable, challengesOngoing, challengesCompleted, challengesExpired;
    FusedLocationProviderClient mFusedLocationClient;

    /**
     * Method called when the Activity is created,in it we initialize the variables and the view.
     * @param savedInstanceState default {@link Bundle}
     */
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Add te preferences from xml
        addPreferencesFromResource(R.xml.home);

        /**
         * Initialize the difference {@link PreferenceScreen} used in the menu
         */
        tasksScreenUnsolved = (PreferenceScreen) findPreference("pref_tasks_unsolved_preferencescreen");
        tasksScreenSolved = (PreferenceScreen) findPreference("pref_tasks_solved_preferencescreen");
        tasksScreenExpired = (PreferenceScreen) findPreference("pref_tasks_expired_preferencescreen");
        messagesScreenUnread = (PreferenceScreen) findPreference("pref_message_unread_preferencescreen");
        messagesScreenRead = (PreferenceScreen) findPreference("pref_message_read_preferencescreen");
        messagesScreenExpired = (PreferenceScreen) findPreference("pref_message_expired_preferencescreen");
        challengesAvailable = (PreferenceScreen) findPreference("pref_challenges_available_preferencescreen");
        challengesOngoing = (PreferenceScreen) findPreference("pref_challenges_ongoing_preferencescreen");
        challengesCompleted = (PreferenceScreen) findPreference("pref_challenges_completed_preferencescreen");
        challengesExpired = (PreferenceScreen) findPreference("pref_challenges_expired_preferencescreen");
        timediariesScreenUnsolved = (PreferenceScreen) findPreference("pref_timediaries_unsolved_preferencescreen");
        timediariesScreenSolved = (PreferenceScreen) findPreference("pref_timediaries_solved_preferencescreen");
        timediariesScreenExpired = (PreferenceScreen) findPreference("pref_timediaries_expired_preferencescreen");

        /**
         * Initialize the {@link FusedLocationProviderClient}
         */
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(HomeActivity.this);

        /**
         * Initialize the single {@link PreferenceScreen} items
         */
        if(iLogApplication.db.getAllMessagesByStatus(Message.STATUS_READ).size() > 0) {
            messagesScreenRead.setEnabled(true);
            messagesScreenRead.setSummary(this.getResources().getString(R.string.message_read_settings_summary, String.valueOf(iLogApplication.db.getAllMessagesByStatus(Message.STATUS_READ).size())));
        }
        else {
            messagesScreenRead.setEnabled(false);
            messagesScreenRead.setSummary(this.getResources().getString(R.string.message_no_read_settings_summary));
        }
        if(iLogApplication.db.getAllMessagesByStatus(Message.STATUS_UNREAD).size() > 0) {
            messagesScreenUnread.setEnabled(true);
            messagesScreenUnread.setSummary(this.getResources().getString(R.string.message_unread_settings_summary, String.valueOf(iLogApplication.db.getAllMessagesByStatus(Message.STATUS_UNREAD).size())));
        }
        else {
            messagesScreenUnread.setEnabled(false);
            messagesScreenUnread.setSummary(this.getResources().getString(R.string.message_no_unread_settings_summary));
        }
        if(iLogApplication.db.getAllMessagesByStatus(Message.STATUS_EXPIRED).size() > 0) {
            messagesScreenExpired.setEnabled(true);
            messagesScreenExpired.setSummary(this.getResources().getString(R.string.messages_expired_settings_summary, String.valueOf(iLogApplication.db.getAllMessagesByStatus(Message.STATUS_EXPIRED).size())));
        }
        else {
            messagesScreenExpired.setEnabled(false);
            messagesScreenExpired.setSummary(this.getResources().getString(R.string.messages_no_expired_settings_summary));
        }
        if(iLogApplication.db.getAllTasksByStatus(Task.STATUS_SOLVED).size() > 0) {
            tasksScreenSolved.setEnabled(true);
            tasksScreenSolved.setSummary(this.getResources().getString(R.string.tasks_solved_settings_summary, String.valueOf(iLogApplication.db.getAllTasksByStatus(Task.STATUS_SOLVED).size())));
        }
        else {
            tasksScreenSolved.setEnabled(false);
            tasksScreenSolved.setSummary(this.getResources().getString(R.string.tasks_no_solved_settings_summary));
        }
        if(iLogApplication.db.getAllTasksByStatus(Task.STATUS_UNSOLVED).size() > 0) {
            tasksScreenUnsolved.setEnabled(true);
            tasksScreenUnsolved.setSummary(this.getResources().getString(R.string.tasks_unsolved_settings_summary, String.valueOf(iLogApplication.db.getAllTasksByStatus(Task.STATUS_UNSOLVED).size())));
        }
        else {
            tasksScreenUnsolved.setEnabled(false);
            tasksScreenUnsolved.setSummary(this.getResources().getString(R.string.tasks_no_unsolved_settings_summary));
        }
        if(iLogApplication.db.getAllTasksByStatus(Task.STATUS_EXPIRED).size() > 0) {
            tasksScreenExpired.setEnabled(true);
            tasksScreenExpired.setSummary(this.getResources().getString(R.string.tasks_expired_settings_summary, String.valueOf(iLogApplication.db.getAllTasksByStatus(Task.STATUS_EXPIRED).size())));
        }
        else {
            tasksScreenExpired.setEnabled(false);
            tasksScreenExpired.setSummary(this.getResources().getString(R.string.tasks_no_expired_settings_summary));
        }
        if(iLogApplication.db.getAllQuestionsByStatus(Question.STATUS_RECEIVED).size() > 0) {
            timediariesScreenUnsolved.setEnabled(true);
            timediariesScreenUnsolved.setSummary(this.getResources().getString(R.string.timediaries_unsolved_settings_summary, String.valueOf(iLogApplication.db.getAllQuestionsByStatus(Question.STATUS_RECEIVED).size())));
        }
        else {
            timediariesScreenUnsolved.setEnabled(false);
            timediariesScreenUnsolved.setSummary(this.getResources().getString(R.string.timediaries_no_unsolved_settings_summary));
        }
        if(iLogApplication.db.getAllQuestionsByStatus(Question.STATUS_ANSWERED).size() > 0) {
            timediariesScreenSolved.setEnabled(true);
            timediariesScreenSolved.setSummary(this.getResources().getString(R.string.timediaries_solved_settings_summary, String.valueOf(iLogApplication.db.getAllQuestionsByStatus(Question.STATUS_ANSWERED).size())));
        }
        else {
            timediariesScreenSolved.setEnabled(false);
            timediariesScreenSolved.setSummary(this.getResources().getString(R.string.timediaries_no_solved_settings_summary));
        }
        if(iLogApplication.db.getAllQuestionsByStatus(Question.STATUS_EXPIRED).size() > 0) {
            timediariesScreenExpired.setEnabled(true);
            timediariesScreenExpired.setSummary(this.getResources().getString(R.string.timediaries_expired_settings_summary, String.valueOf(iLogApplication.db.getAllQuestionsByStatus(Question.STATUS_EXPIRED).size())));
        }
        else {
            timediariesScreenExpired.setEnabled(false);
            timediariesScreenExpired.setSummary(this.getResources().getString(R.string.timediaries_no_expired_settings_summary));
        }

        /**
         * Set the {@link OnPreferenceClickListener} for the {@link #challengesAvailable} and the {@link challengesOngoing}. The former downloads the available challenges for the
         * user from the server while the latter updates the {@link Challenge} that are {@link Challenge#STATUS_ONGOING} before opening the view.
         */
        challengesAvailable.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                preference.setSummary(HomeActivity.this.getResources().getString(R.string.challenges_checking));
                if(challengesAvailable.getRootAdapter().isEmpty()) {
                    downloadAvailableChallenges(preference);
                }
                return false;
            }
        });
        challengesOngoing.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                for(int index=0; index<challengesOngoing.getRootAdapter().getCount(); index++) {
                    challengesOngoing.removePreference((Preference) challengesOngoing.getRootAdapter().getItem(index));
                }
                //ask location permission
                if(hasLocationPermissions()) {
                    populateChallengesOngoing(challengesOngoing);
                }
                else {
                    iLogApplication.requestAllSinglePermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, HomeActivity.this);
                }
                return false;
            }
        });

        challengesAvailable.setSummary(this.getResources().getString(R.string.challenges_check));

        /**
         * Populate the different items in the {@link PreferenceScreen} items of the main view
         */
        populateTasks(tasksScreenUnsolved, Task.STATUS_UNSOLVED);
        populateTasks(tasksScreenSolved, Task.STATUS_SOLVED);
        populateTasks(tasksScreenExpired, Task.STATUS_EXPIRED);
        populateMessages(messagesScreenUnread, Message.STATUS_UNREAD);
        populateMessages(messagesScreenRead, Message.STATUS_READ);
        populateMessages(messagesScreenExpired, Message.STATUS_EXPIRED);
        populateChallengesCompleted(challengesCompleted);
        populateChallengesExpired(challengesExpired);
        populateTimediaries(timediariesScreenSolved, Question.STATUS_ANSWERED);
        populateTimediaries(timediariesScreenUnsolved, Question.STATUS_RECEIVED);
        populateTimediaries(timediariesScreenExpired, Question.STATUS_EXPIRED);

        //check location permission
        if(hasLocationPermissions()) {
            populateChallengesOngoing(challengesOngoing);
        }
        else {
            iLogApplication.requestAllSinglePermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, this);
        }
    }

    @Override
    public void onPause() {
    	super.onPause();
        Log.d(this.toString(), "PAUSED");
    }
    
    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(this.getClass().getSimpleName(), "STOP");
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(this.toString(), "BACK PRESSED");
        finish();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.d(this.toString(), "HOME");
        finish();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(this.toString(), "DESTROYED");
    }

    /**
     * Method that populates the {@link PreferenceScreen} about {@link Task}, interrogates the database for the {@link Task} objects that have as {@link Task#status} the tag
     * passed as argument. In order to no delay the UI, we run it inside a {@link Thread}. Each {@link Preference} object added to the {@link PreferenceScreen} has set a
     * {@link OnPreferenceClickListener} that opens a {@link TaskActivity}.
     * @param screen {@link PreferenceScreen} where we have to attach the {@link Preference} items, one per {@link Task}
     * @param tag String representing the status according to which we have to filter the saved {@link Task} objects
     */
    private void populateTasks(final PreferenceScreen screen, final String tag) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                for(final it.unitn.disi.witmee.sensorlog.model.Task task: iLogApplication.db.getAllTasksByStatus(tag)) {
                    Preference preferenceTask = new Preference(HomeActivity.this);
                    preferenceTask.setSummary(Utils.longToStringFormatTasks(task.getInstanceTime()));
                    preferenceTask.setTitle(task.getTitle());
                    preferenceTask.setEnabled(true);
                    if(!tag.equals(Task.STATUS_SOLVED)) {
                        preferenceTask.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                            public boolean onPreferenceClick(Preference preference) {
                                Intent myIntent = new Intent(HomeActivity.this, TaskActivity.class);
                                myIntent.putExtra("task", task);
                                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                HomeActivity.this.startActivity(myIntent);
                                return true;
                                }
                        });
                    }
                    screen.addPreference(preferenceTask);
                }
            }
        });
        thread.start();
    }

    /**
     * Method that populates the {@link PreferenceScreen} about {@link Message}, interrogates the database for the {@link Message} objects that have as {@link Message#status} the tag
     * passed as argument. In order to no delay the UI, we run it inside a {@link Thread}. Each {@link Preference} object added to the {@link PreferenceScreen} has set a
     * {@link OnPreferenceClickListener} that opens a {@link MessageActivity}.
     * @param screen {@link PreferenceScreen} where we have to attach the {@link Preference} items, one per {@link Message}
     * @param tag String representing the status according to which we have to filter the saved {@link Message} objects
     */
    private void populateMessages(final PreferenceScreen screen, final String tag) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                for(final Message message: iLogApplication.db.getAllMessagesByStatus(tag)) {
                    Preference preferenceMessage = new Preference(HomeActivity.this);
                    preferenceMessage.setSummary(Utils.longToStringFormatTasks(message.getNotifiedTime()));
                    preferenceMessage.setTitle(message.getTitle());
                    preferenceMessage.setEnabled(true);
                    preferenceMessage.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            Intent myIntent = new Intent(HomeActivity.this, MessageActivity.class);
                            myIntent.putExtra("message", message);
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            HomeActivity.this.startActivity(myIntent);
                            return true;
                        }
                    });

                    screen.addPreference(preferenceMessage);
                }
            }
        });
        thread.start();
    }

    /**
     * Method that populates the {@link PreferenceScreen} about {@link Question}, interrogates the database for the {@link Question} objects that have as {@link Question#status} the tag
     * passed as argument. In order to no delay the UI, we run it inside a {@link Thread}. Each {@link Preference} object added to the {@link PreferenceScreen} has set a
     * {@link OnPreferenceClickListener} that opens a {@link QuestionActivity}.
     * @param screen {@link PreferenceScreen} where we have to attach the {@link Preference} items, one per {@link Question}
     * @param tag String representing the status according to which we have to filter the saved {@link Question} objects
     */
    private void populateTimediaries(final PreferenceScreen screen, final String tag) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                for(final Question question: iLogApplication.db.getAllQuestionsByStatus(tag)) {
                    Preference preferenceMessage = new Preference(HomeActivity.this);
                    preferenceMessage.setSummary(Utils.longToStringFormatTasks(question.getNotifiedTime()));
                    preferenceMessage.setTitle(question.getTitle());
                    preferenceMessage.setEnabled(true);

                    if(tag.equals(Question.STATUS_RECEIVED)) {
                        preferenceMessage.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                            public boolean onPreferenceClick(Preference preference) {
                                Intent myIntent = new Intent(HomeActivity.this, QuestionActivity.class);
                                myIntent.putExtra("question", question);
                                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                HomeActivity.this.startActivity(myIntent);
                                return true;
                            }
                        });
                    }
                    screen.addPreference(preferenceMessage);
                }
            }
        });
        thread.start();
    }

    /**
     * Method that performs a call to {@link GoogleSignInClient#silentSignIn()} to retrieve the user Token so that to be able to identify the user on the server
     * @param screen {@link PreferenceScreen} where the results of the download have to be appended to
     */
    private void downloadAvailableChallenges(final Preference screen) {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(iLogApplication.getAppContext(), iLogApplication.gso);
        googleSignInClient.silentSignIn()
                .addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<GoogleSignInAccount> task) {
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            String idToken = account.getIdToken();

                            new HttpAsyncTask().execute(idToken, screen);
                        } catch (ApiException e) {
                            e.printStackTrace();

                            /**
                             * If there is an error in the {@link GoogleSignInClient#silentSignIn()}, and if the error is {@link CommonStatusCodes#SIGN_IN_REQUIRED}, we need to ask the user to manually sign in again
                             */
                            if(e.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED && !iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, "").equals("")) {
                                iLogApplication.startSignInActivity();
                            }
                        }
                    }
                });
    }

    /**
     * {@link AsyncTask} that downlaods the available {@link Challenge} for the logged in user from the server. Once the download is complete, the resulting items are appended
     * to the object
     */
    public class HttpAsyncTask extends AsyncTask<Object, Void, ArrayList<Object>> {
        @Override
        protected ArrayList<Object> doInBackground(Object... data) {

            ArrayList<Object> returns = new ArrayList<Object>();
            returns.add(data[0]);//googletoken
            returns.add(data[1]);//Screen
            returns.add(iLogApplication.GETAVAILABLECHALLENGES(data[0].toString()));
            return returns;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(ArrayList<Object> result) {
            Log.d(this.getClass().getSimpleName(), result.get(2).toString());
            if(!result.get(2).equals("error")) {
                JSONObject response = null;
                try {
                    response = new JSONObject(result.get(2).toString());
                    if(response.getString("status").equals("done_message")) {
                        JSONArray availableChallenges = new JSONArray(response.getString("payload"));
                        Log.d(this.getClass().getSimpleName(), availableChallenges.length()+"");

                        List<Challenge> challenges = new ArrayList<Challenge>();
                        for (int index=0; index<availableChallenges.length(); index++) {

                            JSONObject availableChallenge = availableChallenges.getJSONObject(index);

                            String constraints = "";
                            try {
                                constraints = availableChallenge.getString("constraints");
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                Challenge challenge = new Challenge(availableChallenge.getString("instanceid"), availableChallenge.getString("definitionid"), availableChallenge.getString("project"), availableChallenge.getString("startdate"), availableChallenge.getString("enddate"), availableChallenge.getString("location"), availableChallenge.getString("target"), availableChallenge.getString("type"), availableChallenge.getString("name"), availableChallenge.getString("description"), availableChallenge.getString("instructions"), availableChallenge.getInt("pointsawarded"), availableChallenge.getInt("pointpercontribution"), constraints, availableChallenge.getString("content"), "", "", "");
                                challenges.add(challenge);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        populateAvailableChallenges((Preference) result.get(1), challenges);
                    }
                    else {
                        if(response.getString("payload").contains("No challenges")) {
                            Log.d(this.toString(), "No new challenges");
                            Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getString(R.string.no_challenges_available), Toast.LENGTH_SHORT).show();
                            ((Preference)result.get(1)).setSummary(iLogApplication.getAppContext().getResources().getString(R.string.no_challenges_available));
                        }
                        else {
                            Log.d(this.toString(), "Error server side");
                            Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getString(R.string.errorDownloadingAvailableChallenges), Toast.LENGTH_SHORT).show();
                            ((Preference)result.get(1)).setSummary(iLogApplication.getAppContext().getResources().getString(R.string.no_challenges_available));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(this.toString(), "Processing error");
                    Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getString(R.string.errorDownloadingAvailableChallenges), Toast.LENGTH_SHORT).show();
                    ((Preference)result.get(1)).setSummary(iLogApplication.getAppContext().getResources().getString(R.string.no_challenges_available));
                }
            }
            else {
                Log.d(this.toString(), "Connection error");
                Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getString(R.string.errorDownloadingAvailableChallenges), Toast.LENGTH_SHORT).show();
                ((PreferenceScreen)result.get(1)).setSummary(iLogApplication.getAppContext().getResources().getString(R.string.no_challenges_available));
            }
        }
    }

    /**
     * Method used to populate the {@link Challenge} which are available on the server.
     * @param screen {@link PreferenceScreen} where we have to attach the {@link Preference} items, one per {@link Question}
     * @param downloadedChallenges {@link List} of {@link Challenge} objects downloaded from the server
     */
    private void populateAvailableChallenges(final Preference screen, final List<Challenge> downloadedChallenges) {
        //Remove all the challenges that I already have in my database, either expired or the ones I am participating in
        downloadedChallenges.removeAll(iLogApplication.db.getAllChallenges());
        final int availableChallengesCount = downloadedChallenges.size();

        //If there are no more challenges left, display a message in the summary of the Preference item
        if (availableChallengesCount == 0) {
            screen.setSummary(iLogApplication.getAppContext().getResources().getString(R.string.no_challenges_available));
            screen.setEnabled(false);
        }
        else {
            screen.setSummary(String.format(iLogApplication.getAppContext().getResources().getString(R.string.challenges_available_settings_summary), String.valueOf(availableChallengesCount)));
            Toast.makeText(iLogApplication.getAppContext(), String.format(iLogApplication.getAppContext().getResources().getString(R.string.challenges_available_settings_summary), String.valueOf(availableChallengesCount)), Toast.LENGTH_SHORT).show();

            for (int index=0; index<availableChallengesCount; index++) {
                Challenge challenge = downloadedChallenges.get(index);

                /**
                 * Create a PreferenceScreen and append one single PreferenceAvailable item to it, which is full screen. In this way, we don't need to use pop ups or
                 * additional activities but we can show everything inside the Preferences
                 */
                PreferenceScreen preferencescreen = getPreferenceManager().createPreferenceScreen(HomeActivity.this);
                preferencescreen.setKey(challenge.getInstanceid());
                preferencescreen.setTitle(challenge.getName());
                preferencescreen.setSummary(iLogApplication.getAppContext().getResources().getString(R.string.challenges_started_on, challenge.getStartdate()));

                if(challenge.getType().equals(Challenge.TYPE_FREEROAM) || challenge.getType().equals(Challenge.TYPE_STATIC)) {
                    PreferenceAvailableMap preferenceMessage = new PreferenceAvailableMap(challenge, HomeActivity.this);
                    preferenceMessage.setSummary( challenge.getDescription());
                    preferenceMessage.setTitle(challenge.getName());
                    preferenceMessage.setEnabled(true);
                    preferencescreen.addPreference(preferenceMessage);
                }
                else {
                    PreferenceAvailableBackground preferenceMessage = new PreferenceAvailableBackground(challenge, HomeActivity.this);
                    preferenceMessage.setSummary( challenge.getDescription());
                    preferenceMessage.setTitle(challenge.getName());
                    preferenceMessage.setEnabled(true);
                    preferencescreen.addPreference(preferenceMessage);
                }

                ((PreferenceScreen) screen).addPreference(preferencescreen);
            }
        }
    }

    /**
     * Method used to populate the {@link Challenge} which are ongoing in the user database. To enable the item of one {@link Challenge} that has the {@link Challenge#status} set
     * to {@link Challenge#STATUS_ONGOING}, we need to check if the time constraints and the geographical constraints are met.
     * @param screen {@link PreferenceScreen} where we have to attach the {@link Preference} items, one per {@link Question}
     */
    private void populateChallengesOngoing(final PreferenceScreen screen) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                final List<Challenge> challenges = iLogApplication.db.getAllChallengesByStatus(Challenge.STATUS_ONGOING);

                //If there are no more challenges left, display a message in the summary of the Preference item
                if(challenges.size() == 0) {
                    screen.setEnabled(false);
                    screen.setSummary(HomeActivity.this.getResources().getString(R.string.challenges_no_ongoing_settings_summary));
                }
                else {
                    screen.setEnabled(true);
                    screen.setSummary(HomeActivity.this.getResources().getString(R.string.challenges_ongoing_settings_summary, String.valueOf(challenges.size())));

                    //If the user granted the location permissions to the application
                    Log.d(this.toString(), (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)+"");
                    Log.d(this.toString(), (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)+"");
                    if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        //Get the last known location from the {@link #mFusedLocationClient}
                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(HomeActivity.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        //If the location is not null, loop over the challenges and update the item in the menu, one by one
                                        if (location != null) {
                                            for(final Challenge challenge: challenges) {
                                                if(areConstraintsSatisfied(challenge)) {
                                                    if(iLogApplication.pointInPolygon(challenge.getLocation(), new LatLng(location.getLatitude(), location.getLongitude()))) {
                                                        System.out.println("CHALLENGE INSIDE");
                                                        instantiatePreferenceScreenOngoing(screen, challenge, true, "something");
                                                    }
                                                    else {
                                                        System.out.println("CHALLENGE OUTSIDE");
                                                        instantiatePreferenceScreenOngoing(screen, challenge, false, "Outside boundaries");
                                                    }
                                                }
                                                else {
                                                    instantiatePreferenceScreenOngoing(screen, challenge, false, getPreferenceScreenSummaryFromConstraints(challenge));
                                                }
                                            }
                                        }
                                        else {
                                            for(final Challenge challenge: challenges) {
                                                System.out.println("CHALLENGE LOCATION NULL");
                                                instantiatePreferenceScreenOngoing(screen, challenge, false, "No location available");
                                            }
                                        }
                                    }
                                });
                    }
                    else {
                        for(final Challenge challenge: challenges) {
                            System.out.println("CHALLENGE NO PERMISSIONS");
                            instantiatePreferenceScreenOngoing(screen, challenge, false, "No location available");
                        }
                    }
                }
            }
        });
        thread.start();
    }

    private void populateChallengesCompleted(final PreferenceScreen screen) {
        Thread thread = new Thread(new Runnable() {
            public void run() {

                final List<Challenge> challenges = iLogApplication.db.getAllChallengesByStatus(Challenge.STATUS_COMPLETED);
                if(challenges.size() == 0) {
                    screen.setEnabled(false);
                    screen.setSummary(HomeActivity.this.getResources().getString(R.string.challenges_no_completed_settings_summary));
                }
                else {
                    screen.setEnabled(true);
                    screen.setSummary(HomeActivity.this.getResources().getString(R.string.challenges_completed_settings_summary, String.valueOf(challenges.size())));

                    for(final Challenge challenge: challenges) {
                        instantiatePreferenceScreenCompleted(screen, challenge);
                    }
                }
            }
        });
        thread.start();
    }

    private void populateChallengesExpired(final PreferenceScreen screen) {
        Thread thread = new Thread(new Runnable() {
            public void run() {

                final List<Challenge> challenges = iLogApplication.db.getAllChallengesByStatus(Challenge.STATUS_EXPIRED);
                if(challenges.size() == 0) {
                    screen.setEnabled(false);
                    screen.setSummary(HomeActivity.this.getResources().getString(R.string.challenges_no_expired_settings_summary));
                }
                else {
                    screen.setEnabled(true);
                    screen.setSummary(HomeActivity.this.getResources().getString(R.string.challenges_expired_settings_summary, String.valueOf(challenges.size())));

                    for(final Challenge challenge: challenges) {
                        instantiatePreferenceScreenExpired(screen, challenge);
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * Method used to add the {@link PreferenceScreen} of the single {@link Challenge} which are {@link Challenge#STATUS_ONGOING} to the main one
     * @param screen Main {@link PreferenceScreen} where to append the single {@link PreferenceScreen} objects
     * @param challenge {@link Challenge} to be added
     * @param enable boolean variable that indicates if the {@link Challenge} should be enabled or not
     * @param summary String that will be visualized in the Summary of the item
     */
    private void instantiatePreferenceScreenOngoing(PreferenceScreen screen, Challenge challenge, boolean enable, String summary) {
        PreferenceScreen preferencescreen = getPreferenceManager().createPreferenceScreen(HomeActivity.this);
        preferencescreen.setKey(challenge.getInstanceid());
        preferencescreen.setTitle(challenge.getName());

        if(enable) {
            try {
                if(challenge.getType().equals(Challenge.TYPE_STATIC)) {
                    preferencescreen.setSummary(iLogApplication.getAppContext().getResources().getString(R.string.challenges_ongoing_status, String.valueOf(iLogApplication.db.getAllAnswersByInstanceId(challenge.getInstanceid()).size()), new JSONObject(challenge.getConstraints()).getString("max")));
                }
                else if (challenge.getType().equals(Challenge.TYPE_FREEROAM)) {
                    preferencescreen.setSummary(iLogApplication.getAppContext().getResources().getString(R.string.challenges_ongoing_freeroam_status, String.valueOf(iLogApplication.db.getAllAnswersByInstanceId(challenge.getInstanceid()).size())));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            preferencescreen.setEnabled(enable);
        }
        else {
            preferencescreen.setSummary(summary);
            preferencescreen.setEnabled(enable);
        }

        final PreferenceOngoing preferenceMessage = new PreferenceOngoing(challenge, HomeActivity.this);
        preferencescreen.addPreference(preferenceMessage);

        screen.addPreference(preferencescreen);
    }

    /**
     * Method used to add the {@link PreferenceScreen} of the single {@link Challenge} which are {@link Challenge#STATUS_COMPLETED} to the main one
     * @param screen Main {@link PreferenceScreen} where to append the single {@link PreferenceScreen} objects
     * @param challenge {@link Challenge} to be added
     */
    private void instantiatePreferenceScreenCompleted(PreferenceScreen screen, Challenge challenge) {
        PreferenceScreen preferencescreen = getPreferenceManager().createPreferenceScreen(HomeActivity.this);
        preferencescreen.setKey(challenge.getInstanceid());
        preferencescreen.setTitle(challenge.getName());
        preferencescreen.setSummary(iLogApplication.getAppContext().getResources().getString(R.string.challenges_completed_on, challenge.getCompletiontime(), challenge.getResult()));

        updateChallengeCompleteness(preferencescreen, challenge);

        final PreferenceCompleted preferenceMessage = new PreferenceCompleted(challenge, HomeActivity.this);
        preferencescreen.addPreference(preferenceMessage);

        screen.addPreference(preferencescreen);
    }

    private void instantiatePreferenceScreenExpired(PreferenceScreen screen, Challenge challenge) {
        PreferenceScreen preferencescreen = getPreferenceManager().createPreferenceScreen(HomeActivity.this);
        preferencescreen.setKey(challenge.getInstanceid());
        preferencescreen.setTitle(challenge.getName());
        preferencescreen.setSummary(iLogApplication.getAppContext().getResources().getString(R.string.challenges_expired_on, Utils.changeDateStringFormat(challenge.getEnddate())));

        final PreferenceCompleted preferenceMessage = new PreferenceCompleted(challenge, HomeActivity.this);
        preferencescreen.addPreference(preferenceMessage);

        screen.addPreference(preferencescreen);
    }

    /**
     * This method downloads information about a challenge result status. If the result is pending, which means we have to check it, then the information are downloaded. Once
     * finished, the result of the challenge is updated in the database and also the summary in the preference screen item.
     * @param screen Main {@link PreferenceScreen} object
     * @param challenge {@link Challenge} to be checked
     */
    private void updateChallengeCompleteness(final PreferenceScreen screen, final Challenge challenge) {
        if(challenge.getResult().equals(Challenge.RESULT_PENDING)) {
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(iLogApplication.getAppContext(), iLogApplication.gso);
            googleSignInClient.silentSignIn()
                    .addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<GoogleSignInAccount> task) {
                            try {
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                String idToken = account.getIdToken();

                                new GetChallengesStatus().execute(idToken, screen, challenge);
                            } catch (ApiException e) {
                                e.printStackTrace();
                                if(e.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED && !iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, "").equals("")) {
                                    iLogApplication.startSignInActivity();
                                }
                            }
                        }
                    });
        }
    }

    /**
     * {@link AsyncTask} that checks the status of the {@link Challenge} with the server and updates it locally. This is done once a {@link Challenge} is complete and needs to
     * be reviewed server-side
     * to the object
     */
    public class GetChallengesStatus extends AsyncTask<Object, Void, ArrayList<Object>> {
        @Override
        protected ArrayList<Object> doInBackground(Object... data) {

            ArrayList<Object> returns = new ArrayList<Object>();
            returns.add(data[0]);//googletoken
            returns.add(data[1]);//Screen
            returns.add(data[2]);//Challenge
            returns.add(iLogApplication.GetChallengesStatus(data[0].toString(), (Challenge) data[2]));
            return returns;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(ArrayList<Object> result) {
            PreferenceScreen screen = (PreferenceScreen) result.get(1);
            Challenge challenge = (Challenge) result.get(2);
            String serverResponse = result.get(3).toString();

            Log.d(this.getClass().getSimpleName(), serverResponse);

            if(!serverResponse.equals("error")) {
                JSONObject response = null;
                try {
                    response = new JSONObject(serverResponse);
                    if(response.getString("status").equals("done_message")) {
                        JSONObject responsePayload = new JSONObject(response.getString("payload"));
                        String challengeResult = responsePayload.getString("result");
                        int pointsAwarded = 0;
                        int pointsPerContribution = 0;
                        if(challengeResult.equals("verified")) {
                            pointsAwarded = responsePayload.getJSONObject("points").getInt("pointsawarded");
                        }
                        System.out.println(challengeResult);

                        iLogApplication.db.updateChallengeResult(challenge, challengeResult, pointsAwarded, pointsPerContribution);
                        screen.setSummary(iLogApplication.getAppContext().getResources().getString(R.string.challenges_completed_on, challenge.getCompletiontime(), challengeResult));
                    }
                    else {
                        Log.d(this.toString(), "Error server side");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(this.toString(), "Processing error");
                }
            }
            else {
                Log.d(this.toString(), "Connection error");
            }
        }
    }

    /**
     * Method that checkes if the constraint of the {@link Challenge} are satisfied. It gets all the answers to the specific {@link Challenge} saved in the database and compares
     * with the maximum.
     * @param challenge {@link Challenge} to be checked
     * @return True if the constraints are satisfied, false otherwise
     */
    private boolean areConstraintsSatisfied(Challenge challenge) {
        //{"max": 10, "rule": {"number": 2, "time": 10, "interval": "minutes"}}
        try {
            if(!challenge.getConstraints().equals("")) {
                JSONObject constraints = new JSONObject(challenge.getConstraints());
                JSONObject rule = constraints.getJSONObject("rule");
                int number = rule.getInt("number");
                int time = rule.getInt("time");
                String interval = rule.getString("interval");

                List<Answer> answers = iLogApplication.db.getAllAnswersInTheLast(time, interval);

                if(answers.size() == 0) {
                    return true;
                }
                else {
                    if(number > answers.size()) {
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            }
            else {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Method that returns the String to be displayed as Summary in the item
     * @param challenge {@link Challenge} to be checked
     * @return String representing the summary to be visualized for the specific {@link Challenge}
     */
    private String getPreferenceScreenSummaryFromConstraints(Challenge challenge) {
        try {
            if(!challenge.getConstraints().equals("")) {
                JSONObject constraints = new JSONObject(challenge.getConstraints());
                //JSONObject constraints = new JSONObject("{\"max\": 10, \"rule\": {\"number\": 2, \"time\": 30, \"interval\": \"seconds\"}}");
                JSONObject rule = constraints.getJSONObject("rule");
                int number = rule.getInt("number");
                int time = rule.getInt("time");
                String interval = rule.getString("interval");

                List<Answer> answers = iLogApplication.db.getAllAnswersInTheLast(time, interval);

                if (answers.size() != 0 && number == answers.size()) {
                    return iLogApplication.getAppContext().getResources().getString(R.string.already_constraints, String.valueOf(number), String.valueOf(time), interval, computeTimeDifference(answers.get(0), time, interval));
                } else {
                    return null;
                }
            }
            else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return iLogApplication.getAppContext().getResources().getString(R.string.already_constraints_no_variables);
        }
    }

    /**
     * Method that generates the String representing the time to be displayed, converting the constraints in human readable text
     * @param answer Last {@link Answer} generated for the {@link Challenge} according to which we have to compute the time difference
     * @param time Integer reresenting the time units
     * @param interval String representing the time interval, days, hours, minutes, seconds
     * @return Human readable String representing the time, e.g., time=10 and interval=MINUTE, the output is "10 minute(s)"
     */
    private String computeTimeDifference(Answer answer, int time, String interval) {
        long answerTime = answer.getAnswertime();
        long computedTime = 0;
        if(interval.equals(Challenge.INTERVAL_SECONDS)) {
            computedTime = answerTime + (time*1000);
        }
        else if(interval.equals(Challenge.INTERVAL_MINUTES)) {
            computedTime = answerTime + (time*60*1000);
        }
        else if(interval.equals(Challenge.INTERVAL_HOURS)) {
            computedTime = answerTime + (time*60*60*1000);
        }
        else if(interval.equals(Challenge.INTERVAL_DAYS)) {
            computedTime = answerTime + (time*24*60*60*1000);
        }

        long delta = computedTime - System.currentTimeMillis();


        return selectMessage(delta);
    }

    /**
     * Method that converts the delta time represented in milliseconds from the current time to the last {@link Answer} provided to a human readable String
     * @param delta time interval in milliseconds
     * @return Human readable String representing the time difference
     */
    private static String selectMessage(long delta) {
        if(delta < 60*1000) {
            return delta/(1000)+" "+iLogApplication.getAppContext().getResources().getString(R.string.seconds);
        } else if(delta >= 60*1000 && delta < 59*60*1000) {
            return delta/(60*1000)+" "+iLogApplication.getAppContext().getResources().getString(R.string.minutes);
        } else if(delta >= 59*60*1000 && delta < 24*59*60*1000) {
            return delta/(59*60*1000)+" "+iLogApplication.getAppContext().getResources().getString(R.string.hours);
        } else {
            return delta/(24*59*60*1000)+" "+iLogApplication.getAppContext().getResources().getString(R.string.days);
        }
    }

    private boolean hasLocationPermissions() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            return false;
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
                    if(permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION) || permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        populateChallengesOngoing(challengesOngoing);
                    }
                }

                return;
            }
        }
    }
}