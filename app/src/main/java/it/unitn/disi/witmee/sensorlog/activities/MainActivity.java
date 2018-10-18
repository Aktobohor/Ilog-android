package it.unitn.disi.witmee.sensorlog.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.ExecuteOnPhoneStartup;
import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Main Activity of the application. It is executed and immediately closed. It is used mainly to autorun the application on startup and to execute the correct activity, either
 * {@link ProjectSelectionActivity} or {@link ProjectActivity} and to start the logging process.
 */
public class MainActivity extends AppCompatActivity {
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        Log.d(this.toString(), "MainActivity created.");

        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = "I_Log";

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Drawable d = getDrawable(R.color.primary);
        ActionBar actionBar = getSupportActionBar();
        if(getSupportActionBar() != null){
            actionBar.setBackgroundDrawable(d);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Handle autorun on smartphone startup
        Intent intent = getIntent();
        if (intent != null) {
            try {
                if (intent.getBooleanExtra(ExecuteOnPhoneStartup.AUTO_RUN, false)){
                    moveTaskToBack(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * If no data about any project is available it means we need to run the {@link ProjectSelectionActivity}
         */
        if(!iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_PROJECTSELECTIONDONE, false)) {
            Log.d(this.toString(), "FirstExecutionActivity");
            iLogApplication.launchProjectSelectionActivity(MainActivity.this);
        }
        else {
            /**
             * If there is data about a project, but the user did not perform the login, did not provide the consensus, did not provide permissions and did not fill the profile,
             * means that the procedure is not complete yet and then we need to show again the {@link ProjectActivity}.
             */
            if(!iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_LOGINDONE, false) ||!iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_CONSENTDONE, false) || !iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_PERMISSIONSDONE, false) || !iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_PROFILEDONE, false)) {
                iLogApplication.launchProjectActivity(MainActivity.this);
            }
            else {
                iLogApplication.startLogging();
            }
        }

        //forceCrash(30);

        //finish();
    }
    
    /**
     * Debug method used to make the application crash voluntarily
     * @param interval Time interval in seconds after which the application crashes
     */
    private void forceCrash(int interval) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Integer.valueOf("Whatever I don't care!");
            }
        }, interval * 1000);
    }
    private void addDrawerItems() {
        String[] osArray = { "Profilo", "Questionari", "Grafici", "Informazioni", "Chiudi" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //switch con i vari casi
                Toast.makeText(MainActivity.this, "Time for an upgrade!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle("I_Log");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }
}