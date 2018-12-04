package it.unitn.disi.witmee.sensorlog.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import java.util.List;
import it.unitn.disi.witmee.sensorlog.adapters.MenuAdapter;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.ExecuteOnPhoneStartup;
import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.utils.MenuElement;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Main Activity of the application. It is executed and immediately closed. It is used mainly to autorun the application on startup and to execute the correct activity, either
 * {@link ProjectSelectionActivity} or {@link ProjectActivity} and to start the logging process.
 */
public class MainActivity extends AppCompatActivity {
    List<MenuElement> menuElements;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private MenuAdapter mAdapter;
    private ImageButton btnOpenMenu , btnProfile, btnSettings, btnGraph, btnSurvey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        Log.d(this.toString(), "MainActivity created.");

        //comando universale per settare il contesto della pagina su cui la singleton instanzia le componenti come il menu.
        ILog_CommonMethod.getInstance().setCurrentContext(getBaseContext());
        //comando per la generazione del menu
        ILog_CommonMethod.getInstance().CreateMenu();
        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        AttachMenu();
        InitializeHomeButtons();


        //setupDrawer();

        /*getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Drawable d = getDrawable(R.color.primary);
        ActionBar actionBar = getSupportActionBar();
        if (getSupportActionBar() != null) {
            actionBar.setBackgroundDrawable(d);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_notification_bar);
            actionBar.setTitle("I_LOG");
            //actionBar.setDisplayHomeAsUpEnabled(true);
        }*/

        //Handle autorun on smartphone startup
        Intent intent = getIntent();
        if (intent != null) {
            try {
                if (intent.getBooleanExtra(ExecuteOnPhoneStartup.AUTO_RUN, false)) {
                    moveTaskToBack(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * If no data about any project is available it means we need to run the {@link ProjectSelectionActivity}
         */
        if (!iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_PROJECTSELECTIONDONE, false)) {
            Log.d(this.toString(), "FirstExecutionActivity");
            iLogApplication.launchProjectSelectionActivity(MainActivity.this);
        } else {
            /**
             * If there is data about a project, but the user did not perform the login, did not provide the consensus, did not provide permissions and did not fill the profile,
             * means that the procedure is not complete yet and then we need to show again the {@link ProjectActivity}.
             */
            if (!iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_LOGINDONE, false) || !iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_CONSENTDONE, false) || !iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_PERMISSIONSDONE, false) || !iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_PROFILEDONE, false)) {
                iLogApplication.launchProjectActivity(MainActivity.this);
            } else {
                iLogApplication.startLogging();
            }
        }

        //forceCrash(30);

        //finish();
    }

    private void InitializeHomeButtons() {
        btnOpenMenu = (ImageButton) findViewById(R.id.btn_open_menu);
        btnOpenMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.START);
                Toast.makeText(getBaseContext(),"Apri menu",Toast.LENGTH_LONG).show();
            }
        });

        btnProfile = (ImageButton) findViewById(R.id.imagebutton_profile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(),"Cliccato",Toast.LENGTH_LONG).show();
                //Intent newIntent = new Intent(MainActivity.class, ProfileActivity.class);
                //startActivity(newIntent);
            }
        });
        btnSettings = (ImageButton) findViewById(R.id.imagebutton_settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(),"Settaggi",Toast.LENGTH_LONG).show();
            }
        });
        btnGraph = (ImageButton) findViewById(R.id.imagebutton_graph);
        btnGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(),"Grafici",Toast.LENGTH_LONG).show();
            }
        });
        btnSurvey = (ImageButton) findViewById(R.id.imagebutton_survey);
        btnSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(),"Grafici",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void AttachMenu() {
        menuElements = ILog_CommonMethod.getInstance().getMenuElement();
        mAdapter = new MenuAdapter(getBaseContext(), R.layout.menu_layout, menuElements);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //switch con i vari casi
                MenuElement selected = (MenuElement) menuElements.get(position);
                Intent intetnmaster = selected.getIntent();
                startActivity(intetnmaster);
            }
        });
    }


    /*private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            // Called when a drawer has settled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
                getSupportActionBar().setDisplayShowHomeEnabled(true);

                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            // Called when a drawer has settled in a completely closed state.
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle("I_LOG");
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_notification_bar);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }*/


    /**
     * Debug method used to make the application crash voluntarily
     *
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
}