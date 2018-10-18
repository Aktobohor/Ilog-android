package it.unitn.disi.witmee.sensorlog.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.fragments.ContributionFragment;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Activity that asks the user to select the project he wants to participate in. The user is asked to insert a code that will be used to conenct to the server and download
 * the information about the selected project.
 */
public class ProjectSelectionActivity extends Activity {

    private static TextView configurationButtonTextView;
    ProgressBar progressBar = null;

    /**
     * Default method called when the Activity is created. It is mainly used to initialize the variables. Since it is a FragmentActivity it uses Fragments
     * to display content to the users and specifically to {@link ContributionFragment}
     * @param savedInstanceState what
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_selection_activity);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (Build.VERSION.SDK_INT >= 18 && !wifiManager.isScanAlwaysAvailable()) {
            startActivityForResult(new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE), 3);
        }

        //Title of the app, static
        TextView welcomeTextView = (TextView) findViewById(R.id.welcomeTextView);
        welcomeTextView.setText(getResources().getString(R.string.welcome_message));

        //Text with a description of the application, static
        TextView descriptionTextView = (TextView) findViewById(R.id.informedConsentTextView);
        descriptionTextView.setText(getResources().getString(R.string.description_message));

        //Initialization of the progress spinner that appears when the user puts the codes and proceeds
        progressBar = (ProgressBar) findViewById(R.id.progressBarLoading);
        progressBar.setVisibility(View.GONE);

        //EditText where the user has to input the code for the experiment
        final EditText editText = (EditText)findViewById(R.id.codeText);
        configurationButtonTextView = (TextView) findViewById(R.id.configureButtonTextView);
        configurationButtonTextView.setText(getResources().getString(R.string.start_configuration_message));

        //When the button is pressed, the spinner starts to spin and the request for the project info is sent to the server
        configurationButtonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editText.getText().toString();
                if(code.length() > 0) {
                    progressBar.setVisibility(View.VISIBLE);
                    //request project with code
                    new ProjectSelectionActivity.HttpAsyncTask().execute(code);
                }
            }
        });

        //Static image of the application
        ImageView logoImageView = (ImageView) findViewById(R.id.logoImageView);
        logoImageView.setImageResource(R.drawable.unitnlogo);
    }

    /**
     * {@link AsyncTask} that connects to the server and downloads the information about the project
     */
    public class HttpAsyncTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... data) {

            ArrayList<String> returns = new ArrayList<String>();
            returns.add(data[0]);//code
            returns.add(iLogApplication.GETPROJECTS(data[0]));
            return returns;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if(result != null) {
                try {
                    JSONObject response = new JSONObject(result.get(1).toString());
                    if(!response.has("error_message")) {
                        Log.d(this.getClass().getSimpleName(), response.toString());
                        Log.d(this.getClass().getSimpleName(), response.toString().length()+"");

                        //SharedPreferences are updated with the project info
                        iLogApplication.sharedPreferences.edit().putString(Utils.CONFIG_PROJECTDATA, response.toString()).commit();

                        try {
                            String configuration = response.getString("configuration");
                            JSONObject object = new JSONObject(configuration);
                            iLogApplication.fromJSONObjectToSharedPreferences(object);
                            iLogApplication.sharedPreferences.edit().putBoolean(Utils.CONFIG_PROJECTSELECTIONDONE, true).commit();

                            Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getResources().getString(R.string.project_retrieval_done), Toast.LENGTH_SHORT).show();

                            //The ProjectActivity is launched to start the procedure
                            iLogApplication.launchProjectActivity(ProjectSelectionActivity.this);
                            finish();
                        } catch(Exception e) {
                            Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getResources().getString(R.string.project_retrieval_error), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                    else {
                        Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getResources().getString(R.string.project_retrieval_not_found), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getResources().getString(R.string.project_retrieval_error), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}
