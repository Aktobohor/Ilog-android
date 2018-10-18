package it.unitn.disi.witmee.sensorlog.application;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.GeoJSONObject;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import it.unitn.disi.witmee.sensorlog.activities.HomeActivity;
import it.unitn.disi.witmee.sensorlog.activities.MessageActivity;
import it.unitn.disi.witmee.sensorlog.activities.PermissionsActivity;
import it.unitn.disi.witmee.sensorlog.activities.ProjectActivity;
import it.unitn.disi.witmee.sensorlog.activities.ProjectSelectionActivity;
import it.unitn.disi.witmee.sensorlog.activities.QuestionActivity;
import it.unitn.disi.witmee.sensorlog.activities.SignInActivity;
import it.unitn.disi.witmee.sensorlog.activities.TaskActivity;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.ExecuteOnPhoneStartup;
import it.unitn.disi.witmee.sensorlog.activities.SettingActivity;
import it.unitn.disi.witmee.sensorlog.adapters.QuestionsArrayAdapter;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.RestartLoggingReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.SensorListenerBatchTraining;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.StopLoggingBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.database.DatabaseHelper;
import it.unitn.disi.witmee.sensorlog.model.Answer;
import it.unitn.disi.witmee.sensorlog.model.BatchObject;
import it.unitn.disi.witmee.sensorlog.model.Challenge;
import it.unitn.disi.witmee.sensorlog.model.Message;
import it.unitn.disi.witmee.sensorlog.model.Question;
import it.unitn.disi.witmee.sensorlog.model.sensors.MV;
import it.unitn.disi.witmee.sensorlog.model.system.ST;
import it.unitn.disi.witmee.sensorlog.runnables.AirplaneModeRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.AmbienceRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.ApplicationsRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.AudioRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.BatteryChargeRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.BatteryLevelRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.BluetoothLERunnable;
import it.unitn.disi.witmee.sensorlog.runnables.BluetoothRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.CellInfoRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.DozeRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.HeadsetRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.LocationGPSRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.LocationNetworkRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.MovementActivityRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.MusicRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.NotificationRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.PhoneCallInRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.PhoneCallOutRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.RingModeRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.ScreenRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SensorAccelerometerRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SensorAmbientTemperatureRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SensorGravityRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SensorGyroscopeRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SensorLightRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SensorLinearAccelerometerRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SensorMagneticFieldRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SensorOrientationRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SensorPressureRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SensorProximityRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SensorRelativeHumidityRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SensorRotationVectorRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SmsInRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.SmsOutRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.TouchEventRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.UserPresentRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.WIFINetworksRunnable;
import it.unitn.disi.witmee.sensorlog.services.NotificationService;
import it.unitn.disi.witmee.sensorlog.utils.Utils;
import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.model.Choice;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService;

/**
 * Created by mattiazeni.<br>
 * From the Android documentation: **The Application class in Android is the base class within an Android app that contains all other components such as activities and services. The Application class, or any subclass of the Application class, is instantiated before any other class when the process for your application/package is created**<br>
 * This is the entry point for the entire application, it contains all the main method and initializations.
 */
public class iLogApplication extends Application {

    private static Context context;

    public static ByteArrayOutputStream tmpStorage = new ByteArrayOutputStream();
    public static boolean inside = false;
    public static long firstElementTimestamp = 0;
    public static String firstElementForName = "";
    public static String lastElementForName = "";

    public static SharedPreferences sharedPreferences = null;
    public static NotificationManager notificationManager = null;
    public static Notification.Builder mainBuilder = null;
    public static Notification.Builder timediariesBuilder = null;
    public static Notification.Builder taskBuilder = null;
    public static Notification.Builder messageBuilder = null;
    public static Thread archiveCrashThread = null;

    public static boolean isMonitoringServiceRunning = false;
    public static boolean isUserPresent = true;

    public static long startTimestamp = 0; //logging since

    public static final String INTENT_TASK = "intent_task";

    public static final int GPS_ID = 1000000;//todo change to platform IDs ?
    public static final int NETWORK_ID = 1000001;
    public static final int WIFI_SENSOR_ID = 1000002;
    public static final int AUDIO_ID = 1000005;
    public static final int BLUETOOTHLOGGING_ID = 1000006;
    public static final int BLUETOOTHLELOGGING_ID = 1000007;
    public static final int PHONECALL_IN_ID = 1000011;
    public static final int PHONECALL_OUT_ID = 1000012;
    public static final int SMS_IN_ID = 1000013;
    public static final int SMS_OUT_ID = 1000014;
    public static final int APP_USAGE_ID = 1000015;
    public static final int WIFI_NETWORKS_SENSOR_ID = 1000016;
    public static final int SCREEN_ID = 1000017;
    public static final int DOZE_ID = 1000018;
    public static final int BATTERY_CHARGE_ID = 1000019;
    public static final int HEADSET_ID = 1000020;
    public static final int MUSIC_ID = 1000021;
    public static final int AIRPLANE_MODE_ID = 1000022;
    public static final int RING_MODE_ID = 1000023;
    public static final int USER_PRESENT_ID = 1000024;
    public static final int BATTERY_LEVEL_ID = 1000025;
    public static final int NOTIFICATION_ID = 1000026;
    public static final int TOUCH_ID = 1000027;
    public static final int CELLINFO_ID = 1000028;
    public static final int MOVEMENT_ACTIVITY_ID = 1000029;

    public static Map<Integer, Boolean> sensorLoggingState = new HashMap<Integer, Boolean>();
    public static Map<Class, Long> lastSensorTimestamp = new HashMap<Class, Long>();

    public static boolean stopping = false;
    public static boolean lastWIFIState = false;

    public static AirplaneModeRunnable airplaneModeRunnable = null;
    public static AmbienceRunnable ambienceRunnable = null;
    public static ApplicationsRunnable applicationsRunnable = null;
    public static AudioRunnable audioRunnable = null;
    public static BatteryChargeRunnable batteryChargeRunnable = null;
    public static BatteryLevelRunnable batteryLevelRunnable = null;
    public static BluetoothLERunnable bluetoothLERunnable = null;
    public static BluetoothRunnable bluetoothRunnable = null;
    public static DozeRunnable dozeRunnable = null;
    public static HeadsetRunnable headsetRunnable = null;
    public static LocationGPSRunnable locationGPSRunnable = null;
    public static LocationNetworkRunnable locationNetworkRunnable = null;
    public static MusicRunnable musicRunnable = null;
    public static PhoneCallInRunnable phoneCallInRunnable = null;
    public static PhoneCallOutRunnable phoneCallOutRunnable = null;
    public static RingModeRunnable ringModeRunnable = null;
    public static ScreenRunnable screenRunnable = null;
    public static SmsInRunnable smsInRunnable = null;
    public static SmsOutRunnable smsOutRunnable = null;
    public static UserPresentRunnable userPresentRunnable = null;
    public static WIFINetworksRunnable wifiNetworksRunnable = null;
    public static SensorAccelerometerRunnable sensorAccelerometerRunnable = null;
    public static SensorAmbientTemperatureRunnable sensorAmbientTemperatureRunnable = null;
    public static SensorGravityRunnable sensorGravityRunnable = null;
    public static SensorGyroscopeRunnable sensorGyroscopeRunnable = null;
    public static SensorLightRunnable sensorLightRunnable = null;
    public static SensorLinearAccelerometerRunnable sensorLinearAccelerometerRunnable = null;
    public static SensorMagneticFieldRunnable sensorMagneticFieldRunnable = null;
    public static SensorOrientationRunnable sensorOrientationRunnable = null;
    public static SensorPressureRunnable sensorPressureRunnable = null;
    public static SensorProximityRunnable sensorProximityRunnable = null;
    public static SensorRelativeHumidityRunnable sensorRelativeHumidityRunnable = null;
    public static SensorRotationVectorRunnable sensorRotationVectorRunnable = null;
    public static NotificationRunnable notificationRunnable = null;
    public static TouchEventRunnable touchEventRunnable = null;
    public static CellInfoRunnable cellInfoRunnable = null;
    public static MovementActivityRunnable movementActivityRunnable = null;

    public static int archiveSizeLimit = 0;

    public static long AC_FILTERING = 80;
    public static long AT_FILTERING = 1000;
    public static long GR_FILTERING = 80;
    public static long GY_FILTERING = 80;
    public static long LA_FILTERING = 80;
    public static long LI_FILTERING = 1000;
    public static long MF_FILTERING = 80;
    public static long OR_FILTERING = 80;
    public static long PE_FILTERING = 1000;
    public static long PO_FILTERING = 0;
    public static long RH_FILTERING = 1000;
    public static long RV_FILTERING = 80;
    public static long WN_FILTERING = 900;

    public static final int CODE_RESULT_PERMISSIONS_DRAW_ON_TOP = 101;
    public static final int CODE_RESULT_PERMISSIONS_USAGE_STATS = 202;
    public static final int CODE_RESULT_PERMISSIONS_NOTIFICATION = 303;
    public static final int CODE_RESULT_PERMISSIONS_BATTERY = 404;
    public static final int CODE_RESULT_PERMISSIONS_SENSORS = 505;
    public static final int CODE_RESULT_ACCOUNT = 606;

    public static final HashMap<Integer, ArrayList<BatchObject>> batchSensing = new HashMap<Integer, ArrayList<BatchObject>>();
    public static final HashMap<Integer, Long> batchSensingOffsets = new HashMap<Integer, Long>();
    public static final HashMap<Integer, Long> batchSensingDivisors = new HashMap<Integer, Long>();

    public static PendingIntent pendingIntent;
    public static AlarmManager alarms;

    public static DatabaseHelper db;

    public static GoogleSignInOptions gso;

    public static boolean isSensorBatchingSupported = false;

    private static boolean isSynchronizing = false;

    private static String CHANNEL_ID = "italia1";

    /**
     * Method call when the Application class is created. It is executed before any other Activity in the application. In this method we inizialize the useful components
     */
    public void onCreate() {
        super.onCreate();

        startTimestamp = System.currentTimeMillis();

        //The context to be used in the different components of the application that are not Activity (that have their own context)
        iLogApplication.context = getApplicationContext();

        //Initialization of the Google SignIN component that allows to authenticate the user using Google services
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(iLogApplication.getAppContext().getString(R.string.server_client_id))
                .requestEmail()
                .build();

        /*
        Initialization of the Database {@link it.unitn.disi.witmee.sensorlog.database.DatabaseHelper} that deals with the user feedback elements,
         Tasks {@link it.unitn.disi.witmee.sensorlog.model.Task}, Messages {@link it.unitn.disi.witmee.sensorlog.model.Message},
         Questions {@link it.unitn.disi.witmee.sensorlog.model.Question} and Challenges {@link it.unitn.disi.witmee.sensorlog.model.Challenge}
         */
        db = new DatabaseHelper(getApplicationContext());

        //Initialization of the sharedpreferences, used to store some information like experiments info, among others..
        sharedPreferences = iLogApplication.context.getSharedPreferences(Utils.PACKAGE_NAME, Context.MODE_PRIVATE);

        /*
        Insert in sharedpreferences the startup time of the application.
         TODO: probably it is wort moving it to MainActivity, not suer this method is called at every startup
         */
        sharedPreferences.edit().putLong(Utils.CONFIG_APP_STARTED_TIME, System.currentTimeMillis()).commit();

        //If no project is configured, meaning that this is the first startup of the application, load configuration info from the default json
        if(!iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_PROJECTSELECTIONDONE, false)) {
            Log.d(this.toString(), "FirstExecution");
            iLogApplication.loadDefaultConfigFromJSON();
        }

        //Initialize the sensors based on the selected project
        initialization();

        lastWIFIState = isNetworkConnected();

        //Initialization of the MotificationManager component
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //Initialization of the notifications
        initializeNotifications();

        //Override the default behaviour when the application crashes.
        overrideDefaultCrashBehavior();

        //Method used to initialize the runnables used to collect the data
        runnablesInit();

        /*
        Initialize the limit of the data log file. Once the limit is reached the content is dumped to a file and compressed using the bz2 algorithm.
        The limit is in bytes.
         */
        archiveSizeLimit = sharedPreferences.getInt(Utils.CONFIG_LOGFILESIZELIMITSIZE, 0);

        //Method that periodically restarts the loggin process - TODO: check if this is really needed and if it is working properly
        checkLogging();

        //Method that checks if the current device supports sensor batching
        isSensorBatchingSupported = isSensorBatchingSupported();

        //Information needed to process the data, requested by InfAI, partner in the QROWD project
        if(iLogApplication.sharedPreferences.getString(Utils.CONFIG_SENSORS_MAX_VALUES, "").equals("")) {
            iLogApplication.sharedPreferences.edit().putString(Utils.CONFIG_SENSORS_MAX_VALUES, getSensorMaxValues()).commit();
        }

        //iLogApplication.sharedPreferences.edit().putString(Utils.CONFIG_ENDPOINTRESULTCHALLENGES, "getchallengeresult").commit();
        //startSensorBatchFix();
        //db.addTask(new Task(System.currentTimeMillis(), System.currentTimeMillis(), "[{\"q\": {\"id\": 1,\"c\": [],\"t\": \"t\",\"p\": [{\"l\": \"en-US\",\"t\": \"What was your mood?\"}, {\"l\": \"it-IT\",\"t\": \"Come giudicavi il tuo umore?\"}]},\"a\": [{\"id\": 1,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"10 (Positive)\"}, {\"l\": \"it-IT\",\"t\": \"10 (Positivo)\"}]}, {\"id\": 2,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"9\"}, {\"l\": \"it-IT\",\"t\": \"9\"}]}, {\"id\": 3,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"8\"}, {\"l\": \"it-IT\",\"t\": \"8\"}]}, {\"id\": 4,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"7\"}, {\"l\": \"it-IT\",\"t\": \"7\"}]}, {\"id\": 5,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"6\"}, {\"l\": \"it-IT\",\"t\": \"6\"}]}, {\"id\": 6,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"5\"}, {\"l\": \"it-IT\",\"t\": \"5\"}]}, {\"id\": 7,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"4\"}, {\"l\": \"it-IT\",\"t\": \"4\"}]}, {\"id\": 8,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"3\"}, {\"l\": \"it-IT\",\"t\": \"3\"}]}, {\"id\": 8,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"2\"}, {\"l\": \"it-IT\",\"t\": \"2\"}]}, {\"id\": 8,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"1 (Negative)\"}, {\"l\": \"it-IT\",\"t\": \"1 (Negativo)\"}]}]}]", "2j3h4", "unsolved", "Scatta una foto1", 180000));
        //db.addTask(new Task(223124, 33423543, "[{\"q\": {\"id\": 1,\"c\": [],\"t\": \"t\",\"p\": [{\"l\": \"en-US\",\"t\": \"What was your mood?\"}, {\"l\": \"it-IT\",\"t\": \"Come giudicavi il tuo umore?\"}]},\"a\": [{\"id\": 1,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"10 (Positive)\"}, {\"l\": \"it-IT\",\"t\": \"10 (Positivo)\"}]}, {\"id\": 2,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"9\"}, {\"l\": \"it-IT\",\"t\": \"9\"}]}, {\"id\": 3,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"8\"}, {\"l\": \"it-IT\",\"t\": \"8\"}]}, {\"id\": 4,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"7\"}, {\"l\": \"it-IT\",\"t\": \"7\"}]}, {\"id\": 5,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"6\"}, {\"l\": \"it-IT\",\"t\": \"6\"}]}, {\"id\": 6,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"5\"}, {\"l\": \"it-IT\",\"t\": \"5\"}]}, {\"id\": 7,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"4\"}, {\"l\": \"it-IT\",\"t\": \"4\"}]}, {\"id\": 8,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"3\"}, {\"l\": \"it-IT\",\"t\": \"3\"}]}, {\"id\": 8,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"2\"}, {\"l\": \"it-IT\",\"t\": \"2\"}]}, {\"id\": 8,\"c\": [],\"c_id\": -1,\"p\": [{\"l\": \"en-US\",\"t\": \"1 (Negative)\"}, {\"l\": \"it-IT\",\"t\": \"1 (Negativo)\"}]}]}]", "2j3h4", "unsolved", "Scatta una foto2", "20171123180000000"));
        //printAllChallenges();

        Log.d("i-Log app", "Application Created");
    }

    /**
     * Method that overrides the default behavior of the application when it creashes.
     * We want to detect the crash event, log it, and close the application safely. <br>
     *     More in details the actions performed are:
     *         <ul>
     *           <li>{@link #persistError(Throwable)} persists the error with the full stack trace in our logs to be synchronized with the backend database</li>
     *           <li>{@link Crashlytics#logException(Throwable)} logs the error and allows to see if in the Firebase console</li>
     *           <li>{@link #closeApplicationSafelyAndRestart()} method that dumps the data collected since the last log generation event to disk and restars the application</li>
     *         </ul>
     */
    public void overrideDefaultCrashBehavior() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                e.printStackTrace();

                persistError(e);
                Crashlytics.logException(e);
                closeApplicationSafelyAndRestart();
            }
        });
    }

    /**
     * Persists a crash event as a {@link ST} object with the detailed stacktrace.
     * @param e this Throwable is converted to a String that contains the stacktrace of the error who caused the applicaiton to crash
     */
    public void persistError(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(new PrintWriter(sw)));
        ST event = new ST(ST.EVENT_CRASH, sw.toString().replace(",", ";").replace("\n", ""));
        persistInMemoryEvent(event);
    }

    /**
     * Method used to initialize the notifications showed in the application. <br>
     *     Depending on the version of the operating system there are different ways to build and show notification in Android. More in details the change occurred
     *     with Android Oreo (SDK 26) that requires to use Notification Channels.<br>
     *     There are four different notification types as of July 2018:<br>
     * <ul>
     *     <li>{@link #timediariesBuilder} Notification.Builder used to show notifications about time diaries {@link Question}</li>
     *     <li>{@link #taskBuilder} Notification.Builder used to show notifications about tasks {@link it.unitn.disi.witmee.sensorlog.model.Task}</li>
     *     <li>{@link #messageBuilder} Notification.Builder used to show notifications about messages {@link Message}</li>
     *     <li>{@link #mainBuilder} Notification.Builder used as the main notification in the application that shows when it is running and collecting data</li>
     * </ul>
     * @see <a href="https://developer.android.com/training/notify-user/channels">https://developer.android.com/training/notify-user/channels</a>
     */
    public void initializeNotifications() {
        Intent notificationIntent = new Intent(iLogApplication.getAppContext(), HomeActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(iLogApplication.getAppContext(), (int) System.currentTimeMillis(), notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Modifications for the new channels feature in Android >=8.0
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);

            //Set importance to Low and sound to null so that to not bother the user
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setSound(null, null);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            timediariesBuilder = new Notification.Builder(this)
                    .setContentTitle(getResources().getString(R.string.questionnaireNotificationTitle))
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_notification_bar)
                    .setAutoCancel(false)
                    .setWhen(0)
                    .setOnlyAlertOnce(true)
                    .setChannelId(CHANNEL_ID)
                    .setOngoing(true);
            taskBuilder = new Notification.Builder(this)
                    .setContentTitle(getResources().getString(R.string.taskNotificationTitle))
                    .setSmallIcon(R.drawable.ic_notification_bar)
                    //.setAutoCancel(false)
                    .setWhen(0)
                    .setOnlyAlertOnce(true)
                    .setChannelId(CHANNEL_ID)
                    .setOngoing(true);
            messageBuilder = new Notification.Builder(this)
                    .setContentTitle(getResources().getString(R.string.messageNotificationTitle))
                    .setSmallIcon(R.drawable.ic_notification_bar)
                    //.setAutoCancel(false)
                    .setWhen(0)
                    .setOnlyAlertOnce(true)
                    .setChannelId(CHANNEL_ID)
                    .setOngoing(true);

            //Main notification with buttons to open activities
            String contentText = iLogApplication.getAppContext().getResources().getString(R.string.trackingActivated);
            Intent intentActionStopLogging = new Intent(context, StopLoggingBroadcastReceiver.class);
            intentActionStopLogging.putExtra("action","stoplogging");

            Notification.Action actionStopLogging = new Notification.Action.Builder(0,
                    getResources().getString(R.string.notification_stop), PendingIntent.getBroadcast(context,1,intentActionStopLogging,PendingIntent.FLAG_UPDATE_CURRENT))
                    .build();
            Notification.Action actionSettings = new Notification.Action.Builder(0,
                    getResources().getString(R.string.notification_settings), PendingIntent.getActivity(this, 0,
                    new Intent(this.getApplicationContext(), SettingActivity.class), 0))
                    .build();
            Notification.Action actionHome = new Notification.Action.Builder(0,
                    getResources().getString(R.string.notification_home), PendingIntent.getActivity(this, 0,
                    new Intent(this.getApplicationContext(), HomeActivity.class), 0))
                    .build();

            mainBuilder = new Notification.Builder(this)
                    .setContentTitle(getResources().getString(R.string.mainNotificationTitle))
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.ic_notification_bar)
                    .setAutoCancel(false)
                    .addAction(actionStopLogging)
                    .addAction(actionSettings)
                    //.addAction(actionHome)
                    .setOnlyAlertOnce(true)
                    .setChannelId(CHANNEL_ID)
                    .setOngoing(true);

            if(true) {
                mainBuilder.addAction(actionHome);
            }
        }
        else {
            timediariesBuilder = new Notification.Builder(this)
                    .setContentTitle(getResources().getString(R.string.questionnaireNotificationTitle))
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_notification_bar)
                    .setAutoCancel(false)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setWhen(0)
                    .setOngoing(true);
            taskBuilder = new Notification.Builder(this)
                    .setContentTitle(getResources().getString(R.string.taskNotificationTitle))
                    .setSmallIcon(R.drawable.ic_notification_bar)
                    .setAutoCancel(false)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setWhen(0)
                    .setOngoing(true);
            messageBuilder = new Notification.Builder(this)
                    .setContentTitle(getResources().getString(R.string.messageNotificationTitle))
                    .setSmallIcon(R.drawable.ic_notification_bar)
                    .setAutoCancel(false)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setWhen(0)
                    .setOngoing(true);

            String contentText = iLogApplication.getAppContext().getResources().getString(R.string.trackingActivated);

            Intent intentActionStopLogging = new Intent(context, StopLoggingBroadcastReceiver.class);
            intentActionStopLogging.putExtra("action","stoplogging");

            Notification.Action actionStopLogging = new Notification.Action.Builder(0,
                    getResources().getString(R.string.notification_stop), PendingIntent.getBroadcast(context,1,intentActionStopLogging,PendingIntent.FLAG_UPDATE_CURRENT))
                    .build();
            Notification.Action actionSettings = new Notification.Action.Builder(0,
                    getResources().getString(R.string.notification_settings), PendingIntent.getActivity(this, 0,
                    new Intent(this.getApplicationContext(), SettingActivity.class), 0))
                    .build();
            Notification.Action actionHome = new Notification.Action.Builder(0,
                    getResources().getString(R.string.notification_home), PendingIntent.getActivity(this, 0,
                    new Intent(this.getApplicationContext(), HomeActivity.class), 0))
                    .build();

            mainBuilder = new Notification.Builder(this)
                    .setContentTitle(getResources().getString(R.string.mainNotificationTitle))
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.ic_notification_bar)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setAutoCancel(false)
                    .addAction(actionStopLogging)
                    .addAction(actionSettings)
                    //.addAction(actionHome)
                    .setOngoing(true);

            if(true) {
                mainBuilder.addAction(actionHome);
            }
        }
    }

    /**
     * Returns the application context in those classes that don't have their all context (all except Activities)
     * @return Context object
     */
    public static Context getAppContext() {
        return iLogApplication.context;
    }

    /**
     * Method that uploads as many data as possible if:<br>
     * <ul>
     *     <li>the user granted the permission to upload the files over Wi-Fi during experiment selection</li>
     *     <li>the smartphone is connected to a Wi-Fi network</li>
     *     <li>the user is correctly logged in</li>
     *     <li>the syncrhonization process is not already happening</li>
     * </ul>
     * <br>
     * The type of data that is synchronized are:<br>
     * <ul>
     *     <li>log files containing sensor data</li>
     *     <li>answers to the feedback the user is required to provide, Tasks, Messages, Questions, Challenges</li>
     *     <li>challenges participation info</li>
     *     <li>additional metadata about the user feedback, e.g., if an element has been received, opened, among others</li>
     * </ul>
     * To upload the data the user needs to retrieve from GoogleSignInClient API a token that will be send with the data to the server and used to
     * extract the user email and save the data to the correct user.
     *
     * @see <a href="https://developers.google.com/identity/sign-in/android/backend-auth">https://developers.google.com/identity/sign-in/android/backend-auth</a>
     */
    public static void uploadAllIfConnected() {
        if(iLogApplication.sharedPreferences.getBoolean(Utils.CONFIG_UPLOAD_IF_WIFI, false)) {
            if (!isSynchronizing && iLogApplication.isNetworkConnected() && iLogApplication.isUserLoggedIn()) {

                final ArrayList<File> logFilesToSync = returnFilesToSync();

                try {
                    isSynchronizing = true;

                    GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getAppContext(), gso);
                    googleSignInClient.silentSignIn()
                            .addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
                                @Override
                                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                                    try {
                                        GoogleSignInAccount account = task.getResult(ApiException.class);
                                        String idToken = account.getIdToken();

                                        //throw new ApiException(new Status(CommonStatusCodes.SIGN_IN_REQUIRED));

                                        if(!sharedPreferences.getBoolean(Utils.CONFIG_PROFILE_AND_SENSORS_UPLOADED, false)) {
                                            new uploadProfileInfo().execute(idToken, iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROFILEANSWERS, ""), iLogApplication.sharedPreferences.getString(Utils.CONFIG_SENSORS_MAX_VALUES, ""));
                                        }

                                        uploadAllContributions();

                                        sendData(idToken, logFilesToSync);
                                    } catch (ApiException e) {
                                        e.printStackTrace();
                                        if(e.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED && !iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, "").equals("")) {
                                            startSignInActivity();
                                        }
                                    }
                                    finally {
                                        isSynchronizing = false;
                                    }
                                }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    isSynchronizing = false;
                }
            }
            else {
                if(isSynchronizing) {
                    System.out.println("Synchronization: already running.");
                    Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getString(R.string.errorAlreadySynchronizing), Toast.LENGTH_SHORT).show();
                }
            }
        }
        else {
            System.out.println("Not configured to upload over Wi-Fi.");
        }
    }

    /**
     * Returns an {@link ArrayList} of {@link File} objects containing the files to be synchronized that are stored inside the application package
     * @return {@link ArrayList} of {@link File}
     */
    private static ArrayList<File> returnFilesToSync() {
        File[] logFiles = iLogApplication.getLogDirectory().listFiles();
        final ArrayList<File> logFilesToSync = new ArrayList<File>();

        for (File f : logFiles) {
            if(f.getAbsolutePath().contains("bz2")) {
                logFilesToSync.add(f);
            }
        }

        return logFilesToSync;
    }

    /**
     * Method that runs a {@link Thread} and uploads the log files, one by one
     * @param token is a {@link String} that contains the user token as generated by {@link GoogleSignInClient}
     * @param logFilesToSync the {@link ArrayList} of log files to be synchronized
     */
    private static void sendData(final String token, final ArrayList<File> logFilesToSync) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    for (File f : logFilesToSync) {
                        if (f.getAbsolutePath().contains("bz2") && iLogApplication.isNetworkConnected() && iLogApplication.isUserLoggedIn()) {
                            Log.d(this.toString(), "Synchronizing file " + f.getAbsolutePath());
                            Log.d(this.toString(), "Token " + token);

                            HttpParams params = new BasicHttpParams();
                            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                            HttpConnectionParams.setConnectionTimeout(params, 10000);
                            HttpConnectionParams.setSoTimeout(params, 10000);
                            DefaultHttpClient mHttpClient = new DefaultHttpClient(params);
                            HttpPost httppost = new HttpPost(Utils.returnServerUrl() + iLogApplication.sharedPreferences.getString(Utils.CONFIG_ENDPOINTUPLOAD, ""));
                            Log.d(this.toString(), Utils.returnServerUrl() + iLogApplication.sharedPreferences.getString(Utils.CONFIG_ENDPOINTUPLOAD, ""));
                            httppost.addHeader("token", token);

                            MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                            multipartEntity.addPart("file", new FileBody(f));
                            httppost.setEntity(multipartEntity);

                            mHttpClient.execute(httppost, new FileUploadResponseHandler(f));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    /**
     * TODO
     * @param fileName name of the file
     */
    public static void deleteFileCount(String fileName) {
        Map<String,?> keys = sharedPreferences.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            if(entry.getKey().contains(fileName)) {
                sharedPreferences.edit().remove(entry.getKey()).commit();
            }
        }
    }

    /**
     * Handler used to manage the response from the server when uploading log files. It implements {@link ResponseHandler}. <br>
     * If the file as been correctly synchronized it is deleted from the phone, otherwise if any error occurs it is kept. Once the upload is successfull
     * it also generate a {@link ST} event containing the filename.
     */
    private static class FileUploadResponseHandler implements ResponseHandler<Object> {

        private File file;

        FileUploadResponseHandler(File file) {
            this.file = file;
        }

        @Override
        public Object handleResponse(HttpResponse response) throws ClientProtocolException, IOException {

            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            org.apache.http.HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            System.out.println(responseString);
            if(statusCode == 200) {
                try {
                    JSONObject jsonObject = new JSONObject(responseString);
                    if(jsonObject.has("done_message")) {
                        if(jsonObject.getString("done_message").contains("uploaded")) {
                            System.out.println("File "+file.getName()+" uploaded.");

                            iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_UPLOAD_SUCCESS, file.getName()));
                            //Mint.logEvent(Utils.MINT_UPLOAD, MintLogLevel.Info, Utils.MINT_UPLOAD_SUCCESS_KEY, fileToSync.getName());

                            if(file.delete()) {
                                System.out.println("File "+file.getName()+" deleted.");
                                deleteFileCount(file.getAbsolutePath().replace(".bz2", ""));
                            }
                            else {
                                System.out.println("Error deleting file "+file.getName()+".");
                            }
                        }
                        else {
                            System.out.println("File " + file.getName() + " not synched.");
                            //Mint.logEvent(Utils.MINT_UPLOAD, MintLogLevel.Info, Utils.MINT_UPLOAD_ERROR_KEY, fileToSync.getName());
                            iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_UPLOAD_ERROR, file.getName()));
                        }
                    }
                    else {
                        System.out.println("File " + file.getName() + " not synched.");
                        //Mint.logEvent(Utils.MINT_UPLOAD, MintLogLevel.Info, Utils.MINT_UPLOAD_ERROR_KEY, fileToSync.getName());
                        iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_UPLOAD_ERROR, file.getName()));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println("File " + file.getName() + " not synched.");
                    //Mint.logEvent(Utils.MINT_UPLOAD, MintLogLevel.Info, Utils.MINT_UPLOAD_ERROR_KEY, fileToSync.getName());
                    iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_UPLOAD_ERROR, file.getName()));
                }
            }
            else {
                System.out.println("File " + file.getName() + " not synched.");
                //Mint.logEvent(Utils.MINT_UPLOAD, MintLogLevel.Info, Utils.MINT_UPLOAD_ERROR_KEY, fileToSync.getName());
                iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_UPLOAD_ERROR, file.getName()));
            }

            return null;
        }
    }

    /**
     * Method used to persist in memory any sensor event logged by the application. If the size of the {@link #tmpStorage} {@link ByteArrayOutputStream} variable is higher
     * than the limit, it starts archiving the data into a log file. Otherwise, it keeps appending to the {@link #tmpStorage} outputStream.
     * @param event any event generated by the sensors
     */
    public static void persistInMemoryEvent(AbstractSensorEvent event) {

        if (tmpStorage.size() > archiveSizeLimit && !inside) {

            byte[] tmp = bufferIsFull(event);

            archiveFile("", tmp);
        }
        else {
            try {
                writeSensorEventCsvObject(event);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method that dumps the {@link #tmpStorage} into a byte array and resets the first and last element timestamps used to name the log file.
     * @param event
     * @return
     */
    private static byte[] bufferIsFull(AbstractSensorEvent event) {
        inside = true;

        try {
            writeSensorEventCsvObject(event);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        firstElementForName = Utils.longToStringFormat(firstElementTimestamp);
        //lastElementForName = Utils.longToStringFormat(lastElementTimestamp);

        if(event!=null) {
            lastElementForName = Utils.longToStringFormat(event.getTimestamp());
        }
        else {
            lastElementForName = Utils.longToStringFormat(System.currentTimeMillis());
        }

        byte[] tmp = tmpStorage.toByteArray();
        tmpStorage.reset();
        inside = false;
        return tmp;
    }

    /**
     * Method that generates the zipped log file using bz2.
     * @param todoAction action to be performed after the creation of the log file, either "close", "restart" or "" (empty)
     * @param tmp is the byte array to be dumped to the compressed file
     */
    public static void archiveFile(final String todoAction, final byte[] tmp) {

        persistInMemoryEvent(new ST(ST.EVENT_ARCHIVE, tmpStorage.size()+""));

        if(tmp.length > 0) {
            archiveCrashThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        final String fileName = getLogArchiveFileName(firstElementForName, lastElementForName);

                        //System.out.println(new String(tmp, "UTF-8"));

                        Log.d(getAppContext().toString(), "Zip started");

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        BZip2CompressorOutputStream output = new BZip2CompressorOutputStream(baos);
                        output.write(tmp);

                        output.close();
                        baos.close();
                        //FileOutputStream fos = new FileOutputStream(Utils.returnAppDataPath() + sharedPreferences.getString(Utils.CONFIG_LOGDIR, "") + sharedPreferences.getString(Utils.CONFIG_SEPARATOR, "") + fileName + "." + sharedPreferences.getString(Utils.CONFIG_COMPRESSEDLOGEXTENSION, ""));
                        FileOutputStream fos = iLogApplication.getAppContext().openFileOutput(fileName + "." + sharedPreferences.getString(Utils.CONFIG_COMPRESSEDLOGEXTENSION, ""), 0);
                        fos.write(baos.toByteArray());
                        fos.close();

                        baos.reset();
                        output.finish();

                        Log.d(getAppContext().toString(), "Zip finished");

                        if(todoAction.equals("close")) {
                            closeApplication();
                        }
                        else if(todoAction.equals("restart")) {
                            System.out.println("Restarting");
                            PendingIntent intent = PendingIntent.getBroadcast(context, 0, new Intent(context, ExecuteOnPhoneStartup.class), PendingIntent.FLAG_UPDATE_CURRENT);
                            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + sharedPreferences.getInt(Utils.CONFIG_LOGGINGRESTARTINTERVAL, 0), intent);

                            closeApplication();
                        }
                        else {
                            uploadAllIfConnected();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            archiveCrashThread.start();
        }
    }

    /**
     * Method that returns the filename to be assigned to the log file
     * @param from String of the date of the first line in the file
     * @param to String of the date of the last line in the file
     * @return a String containing the whole filename
     */
    public static String getLogArchiveFileName(String from, String to) {
        System.out.println("FILE: logs_"+ from +"_"+ to + ".android.csv");
        return "logs_"+ from +"_"+ to + ".android.csv";
    }

    /**
     * Method that initializes the variable {@link #firstElementTimestamp} if the {@link #tmpStorage} outputStream is empty. It finally converts the {@link AbstractSensorEvent} event to String
     * @param event
     * @throws IOException
     * @throws IllegalAccessException
     */
    private static void writeSensorEventCsvObject(AbstractSensorEvent event) throws IOException, IllegalAccessException {
        if (event != null) {
            if(tmpStorage.size() == 0) {
                firstElementTimestamp = event.getTimestamp();
            }
            //lastElementTimestamp = event.getTimestamp();
            String string = event.toString();
            writeToBlockingQueue(string+"\n");
        }
    }

    /**
     * Method that writes the event passed as String to the outputStream {@link #tmpStorage}
     * @param line event passed as comma separated (CSV) string
     */
    private static void writeToBlockingQueue(String line) {
        try {
            //semaphore.acquire();
            tmpStorage.write(line.getBytes());
            //semaphore.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that checks if the smartphone connected to a Wi-Fi network
     * @return true if connected to Wi-Fi, false otherwise
     */
    public static boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Stop all the sensors, dump temporary logs to file and restart the application
     */
    public static void closeApplicationSafelyAndRestart() {
        for (Map.Entry<Integer, Boolean> entry : sensorLoggingState.entrySet()) {
            if(entry.getValue()) {
                persistInMemoryEvent(new SM(entry.getKey(), System.currentTimeMillis(), !entry.getValue()));
            }
        }

        stopLogging("restart");
    }

    /**
     * Stop all the sensors and dump temporary logs to file
     */
    public static void closeApplicationSafely() {

        Log.d(context.getClass().getSimpleName(), "Stopping all services");

        for (Map.Entry<Integer, Boolean> entry : sensorLoggingState.entrySet()) {
            if(entry.getValue()) {
                persistInMemoryEvent(new SM(entry.getKey(), System.currentTimeMillis(), !entry.getValue()));
            }
        }

        stopLogging("close");
    }

    /**
     * Method that checks if the user is logged in
     * @return true if the user is logged in, false otherwise
     */
    public static boolean isUserLoggedIn() {
        if(GoogleSignIn.getLastSignedInAccount(getAppContext()) != null && !sharedPreferences.getString(Utils.ROLE_KEY, "").equals("")) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Method used to retrieve the username (email address) of the user logged in in the application. It is used only for UI purposes since the communication with
     * the server happens using the Google token
     * @return String containing the email address of the logged in user, "User not logged in" otherwise
     */
    public static String getUserName() {
        if(isUserLoggedIn()) {
            return GoogleSignIn.getLastSignedInAccount(getAppContext()).getEmail();
        }
        else {
            return "User not logged in";
        }
    }

    /**
     * Method that returns the minimum sample frequency from the project configuration information
     * @param sensorId **not used**
     * @return integer with the value
     */
    public static int getMinSampleRateForSensorId(int sensorId) {
        //the desired delay between events in microseconds
        //return sharedPreferences.getInt(Utils.CONFIG_SENSORCOLLECTIONFREQUENCY, 0);
        //System.out.println("Sample rate: "+sharedPreferences.getInt(Utils.CONFIG_SENSORCOLLECTIONFREQUENCY, 0));
        return sharedPreferences.getInt(Utils.CONFIG_SENSORCOLLECTIONFREQUENCY, 0);
    }

    /**
     * Load default settings into SharedPreferences from the json. The settings will be overwritten once the user selects a project
     */
    public static void loadDefaultConfigFromJSON() {
        JSONObject configObject = null;
        try {
            configObject = new JSONObject(loadJSONFromAsset("default_config.json"));
            Log.d(getAppContext().toString(), "Configuration file read.");

            fromJSONObjectToSharedPreferences(configObject);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(getAppContext().toString(), "Error reading configuration file.");
        }
    }

    /**
     * Extracts the content of the json file as a String
     * @param fileName name of the file to extract data from
     * @return String with the content of the file, null if any error occurs
     */
    public static String loadJSONFromAsset(String fileName) {
        String json = null;
        try {
            InputStream is = getAppContext().getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }

    /**
     * Method that initializes the sensors to be used to collect data from based on the selected project. If something goes wrong during the
     * initialization, the app loads the default sensors, meaning that it collects data from all of them.
     */
    public static void initialization() {

        String projectData = iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, null);
        try {
            if(projectData != null) {
                JSONObject project = new JSONObject(projectData);
                String sensorsData = project.getString("sensors");
                JSONArray sensors = new JSONArray(sensorsData);

                for (int index = 0; index < sensors.length(); index++) {
                    JSONObject sensor = sensors.getJSONObject(index);
                    sensorLoggingState.put(sensor.getInt("id"), false);
                }
                Log.d(iLogApplication.getAppContext().toString(), "Sensors loaded");
            }
            else {
                loadDefaultSensors();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(iLogApplication.getAppContext().toString(), "Error in loading sensors");
            loadDefaultSensors();
            Log.d(iLogApplication.getAppContext().toString(), "Default sensors loaded");
        }

        printMap(sensorLoggingState);
    }

    /**
     * Method that loads the default sensors (all of them).
     */
    private static void loadDefaultSensors() {
        //MOTION SENSORS
        sensorLoggingState.put(Sensor.TYPE_ACCELEROMETER, false);
        sensorLoggingState.put(Sensor.TYPE_GRAVITY, false);
        sensorLoggingState.put(Sensor.TYPE_GYROSCOPE, false);
        sensorLoggingState.put(Sensor.TYPE_LINEAR_ACCELERATION, false);
        sensorLoggingState.put(Sensor.TYPE_ROTATION_VECTOR, false);
        //POSITION SENSORS
        sensorLoggingState.put(Sensor.TYPE_MAGNETIC_FIELD, false);
        sensorLoggingState.put(Sensor.TYPE_ORIENTATION, false);
        sensorLoggingState.put(Sensor.TYPE_PROXIMITY, false);
        //AMBIENT SENSORS
        sensorLoggingState.put(Sensor.TYPE_AMBIENT_TEMPERATURE, false);
        sensorLoggingState.put(Sensor.TYPE_LIGHT, false);
        sensorLoggingState.put(Sensor.TYPE_PRESSURE, false);
        sensorLoggingState.put(Sensor.TYPE_RELATIVE_HUMIDITY, false);
        sensorLoggingState.put(Sensor.TYPE_TEMPERATURE, false);
        //LOCATION SENSORS
        sensorLoggingState.put(GPS_ID, false);
        sensorLoggingState.put(NETWORK_ID, false);
        //AMBIENCE SENSORS
        sensorLoggingState.put(WIFI_SENSOR_ID, false);
        sensorLoggingState.put(WIFI_NETWORKS_SENSOR_ID, false);
        //SOCIAL SENSORS
        sensorLoggingState.put(AUDIO_ID, false);
        sensorLoggingState.put(BLUETOOTHLOGGING_ID, false);
        sensorLoggingState.put(BLUETOOTHLELOGGING_ID, false);
        sensorLoggingState.put(SCREEN_ID, false);
        sensorLoggingState.put(DOZE_ID, false);
        sensorLoggingState.put(BATTERY_CHARGE_ID, false);
        sensorLoggingState.put(HEADSET_ID, false);
        sensorLoggingState.put(MUSIC_ID, false);
        sensorLoggingState.put(AIRPLANE_MODE_ID, false);
        sensorLoggingState.put(RING_MODE_ID, false);
        sensorLoggingState.put(USER_PRESENT_ID, false);
        sensorLoggingState.put(BATTERY_LEVEL_ID, false);
        //CALL SENSORS
        sensorLoggingState.put(PHONECALL_IN_ID, false);
        sensorLoggingState.put(PHONECALL_OUT_ID, false);
        //SMS SENSORS
        sensorLoggingState.put(SMS_IN_ID, false);
        sensorLoggingState.put(SMS_OUT_ID, false);
        //SENSORS
        sensorLoggingState.put(APP_USAGE_ID, false);
        sensorLoggingState.put(SCREEN_ID, false);
        sensorLoggingState.put(NOTIFICATION_ID, false);
        sensorLoggingState.put(TOUCH_ID, false);
        sensorLoggingState.put(CELLINFO_ID, false);
        sensorLoggingState.put(MOVEMENT_ACTIVITY_ID, false);
    }

    /**
     * Returns a File object that refers to the directory where the logs are stored
     * @return File object that refers to the directory where the logs are stored, in the internal memory of the phone (inside app package)
     */
    public static File getLogDirectory() {
        return getAppContext().getFilesDir();
    }

    /**
     * Method that loads all the entry of the object into the in the application.
     * @param object JSONObject containing the configuration elements
     */
    public static void fromJSONObjectToSharedPreferences(JSONObject object) {
        try {
            Iterator<?> keys = object.keys();

            while(keys.hasNext()) {
                String key = (String)keys.next();

                System.out.println(key);

                if (object.get(key) instanceof String) {
                    sharedPreferences.edit().putString(key, String.valueOf(object.get(key))).commit();
                }
                else if (object.get(key) instanceof Integer) {
                    sharedPreferences.edit().putInt(key, (int) object.get(key)).commit();
                }
                else if (object.get(key) instanceof Boolean) {
                    sharedPreferences.edit().putBoolean(key, Boolean.valueOf(String.valueOf(object.get(key)))).commit();
                }
            }

            //sharedPreferences.edit().putLong(Utils.LAST_CONFIG_UPDATE_TIMESTAMP, object.getLong("timestamp")).commit();
            Log.d(getAppContext().toString(), "Configuration file loaded into SharedPreferences.");

        } catch(Exception e) {
            e.printStackTrace();
            Log.d(getAppContext().toString(), "ERROR: Configuration file NOT loaded into SharedPreferences.");
        }
    }

    /**
     * Method that manages the Http GET request for the login task
     * @param token String containing the user token as generated by {@link GoogleSignInClient}
     * @param firebaseToken String containing the {@link FirebaseInstanceId#getInstance()} Firebase token referring to the user
     * @param endPoint String containing the URL of the REST service on the server that allows the user to login
     * @return String with the response from the server, "null" if an error occurred
     */
    public static String GET(String token, String firebaseToken, String endPoint) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("token", token);
        headers.set("firebasetoken", firebaseToken);

        Log.d("LOGIN", headers.toString());

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Utils.returnServerUrl() + endPoint);
        Log.d(getAppContext().toString(), Utils.returnServerUrl() + endPoint);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpEntity<String> response = restTemplate.exchange(
                    builder.build().encode().toUri(),
                    HttpMethod.GET,
                    entity,
                    String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }

    /**
     * Method that manages the Http GET request for the signup task
     * @param token String containing the user token as generated by {@link GoogleSignInClient}
     * @param firebaseToken String containing the {@link FirebaseInstanceId#getInstance()} Firebase token referring to the user
     * @param project String containing the id of the project the user decided to subscribe to
     * @param endPoint String containing the URL of the REST service on the server that allows the user to login
     * @return String with the response from the server, "null" if an error occurred
     */
    public static String SIGNUP(String token, String firebaseToken, String project, String endPoint) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("token", token);
        headers.set("firebasetoken", firebaseToken);
        headers.set("project", project);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Utils.returnServerUrl() + endPoint);
        Log.d(getAppContext().toString(), Utils.returnServerUrl() + endPoint);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpEntity<String> response = restTemplate.exchange(
                    builder.build().encode().toUri(),
                    HttpMethod.GET,
                    entity,
                    String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Method that manages the Http GET request for the get projects task. Allows to get the project by the code inserted by the user
     * @param code String cointaining the code of the project as inserted by the user in the {@link ProjectSelectionActivity}
     * @return String containing the body of the response from the server
     */
    public static String GETPROJECTS(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("code", code);

        Log.d("PROJECTS", headers.toString());

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Utils.returnServerUrl() + iLogApplication.sharedPreferences.getString(Utils.CONFIG_ENDPOINTPROJECTS, ""));
        Log.d(getAppContext().toString(), Utils.returnServerUrl() + iLogApplication.sharedPreferences.getString(Utils.CONFIG_ENDPOINTPROJECTS, ""));

        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpEntity<String> response = restTemplate.exchange(
                    builder.build().encode().toUri(),
                    HttpMethod.GET,
                    entity,
                    String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Method that manages the Http GET request for the get challenges. Allows to get all the challenges available for the selected user.
     * @param token String containing the user token as generated by {@link GoogleSignInClient}
     * @return String containing the body of the response from the server
     */
    public static String GETAVAILABLECHALLENGES(String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("token", token);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(iLogApplication.sharedPreferences.getString(Utils.CONFIG_SERVERBASEURL, "")+iLogApplication.sharedPreferences.getString(Utils.CONFIG_PORTSEPATATOR, "")+iLogApplication.sharedPreferences.getInt(Utils.CONFIG_PORTAVAILABLECHALLENGES, 0)+iLogApplication.sharedPreferences.getString(Utils.CONFIG_SEPARATOR, "") + iLogApplication.sharedPreferences.getString(Utils.CONFIG_ENDPOINTAVAILABLECHALLENGES, ""));
        Log.d(getAppContext().toString(), iLogApplication.sharedPreferences.getString(Utils.CONFIG_SERVERBASEURL, "")+iLogApplication.sharedPreferences.getString(Utils.CONFIG_PORTSEPATATOR, "")+iLogApplication.sharedPreferences.getInt(Utils.CONFIG_PORTAVAILABLECHALLENGES, 0)+iLogApplication.sharedPreferences.getString(Utils.CONFIG_SEPARATOR, "") + iLogApplication.sharedPreferences.getString(Utils.CONFIG_ENDPOINTAVAILABLECHALLENGES, ""));

        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpEntity<String> response = restTemplate.exchange(
                    builder.build().encode().toUri(),
                    HttpMethod.GET,
                    entity,
                    String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Method that manages the Http GET request for the get challenges status. Allows to get the status of a challenge
     * @param token String containing the user token as generated by {@link GoogleSignInClient}
     * @param challenge {@link Challenge} challenge for which we want the status
     * @return String containing the body of the response from the server
     */
    public static String GetChallengesStatus(String token, Challenge challenge) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("token", token);
        headers.set("instanceid", challenge.getInstanceid());

        String url = iLogApplication.sharedPreferences.getString(Utils.CONFIG_SERVERBASEURL, "")+iLogApplication.sharedPreferences.getString(Utils.CONFIG_PORTSEPATATOR, "")+iLogApplication.sharedPreferences.getInt(Utils.CONFIG_PORTAVAILABLECHALLENGES, 0)+iLogApplication.sharedPreferences.getString(Utils.CONFIG_SEPARATOR, "") + iLogApplication.sharedPreferences.getString(Utils.CONFIG_ENDPOINTRESULTCHALLENGES, "");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        Log.d(getAppContext().toString(), url);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpEntity<String> response = restTemplate.exchange(
                    builder.build().encode().toUri(),
                    HttpMethod.GET,
                    entity,
                    String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Method that manages the Http POST request for posting the info about the current status of the system and app
     * @param content String containing all the statistics as generated in {@link it.unitn.disi.witmee.sensorlog.model.system.FullInfo}
     */
    public static void postFullInfo(final String content) {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getAppContext(), gso);
        googleSignInClient.silentSignIn()
                .addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            final String idToken = account.getIdToken();

                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try  {
                                        uploadStatistics(idToken, content);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            thread.start();
                        } catch (ApiException e) {
                            e.printStackTrace();
                            if(e.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED && !iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, "").equals("")) {
                                startSignInActivity();
                            }
                        }
                    }
                });
    }

    /**
     * Method that allows to get the current version of the application
     * @return String containing the current version of the application. The format is X.X.X where X is an integer number
     */
    public static String getAppVersion() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getAppContext().getPackageManager().getPackageInfo(getAppContext().getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Method that kills the application after having cancelled the scheduled {@link #pendingIntent}
     */
    public static void closeApplication() {
        if(alarms!=null && pendingIntent!=null) {
            alarms.cancel(pendingIntent);
        }

        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(0);
    }

    /**
     * Method used to check if the logged in user has superuser privileges
     * @return true is the user is superuser, false otherwise
     */
    public static boolean isUserSuperuser() {
        if(sharedPreferences.getString(Utils.ROLE_KEY, "").equals("superuser")) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Method used to init all the runnables, each of them is responsible for collecting one sensor stream
     */
    public static void runnablesInit() {
        airplaneModeRunnable = new AirplaneModeRunnable();
        ambienceRunnable = new AmbienceRunnable();
        applicationsRunnable = new ApplicationsRunnable();
        audioRunnable = new AudioRunnable();
        batteryChargeRunnable = new BatteryChargeRunnable();
        batteryLevelRunnable = new BatteryLevelRunnable();
        bluetoothLERunnable = new BluetoothLERunnable();
        bluetoothRunnable = new BluetoothRunnable();
        dozeRunnable = new DozeRunnable();
        headsetRunnable = new HeadsetRunnable();
        locationGPSRunnable = new LocationGPSRunnable();
        locationNetworkRunnable = new LocationNetworkRunnable();
        musicRunnable = new MusicRunnable();
        phoneCallOutRunnable = new PhoneCallOutRunnable();
        phoneCallInRunnable = new PhoneCallInRunnable();
        ringModeRunnable = new RingModeRunnable();
        screenRunnable = new ScreenRunnable();
        sensorAccelerometerRunnable = new SensorAccelerometerRunnable();
        sensorAmbientTemperatureRunnable = new SensorAmbientTemperatureRunnable();
        sensorGravityRunnable = new SensorGravityRunnable();
        sensorGyroscopeRunnable = new SensorGyroscopeRunnable();
        sensorLightRunnable = new SensorLightRunnable();
        sensorLinearAccelerometerRunnable = new SensorLinearAccelerometerRunnable();
        sensorMagneticFieldRunnable = new SensorMagneticFieldRunnable();
        sensorOrientationRunnable = new SensorOrientationRunnable();
        sensorPressureRunnable = new SensorPressureRunnable();
        sensorProximityRunnable = new SensorProximityRunnable();
        sensorRelativeHumidityRunnable = new SensorRelativeHumidityRunnable();
        sensorRotationVectorRunnable = new SensorRotationVectorRunnable();
        smsInRunnable = new SmsInRunnable();
        smsOutRunnable = new SmsOutRunnable();
        userPresentRunnable = new UserPresentRunnable();
        wifiNetworksRunnable = new WIFINetworksRunnable();
        notificationRunnable = new NotificationRunnable();
        touchEventRunnable = new TouchEventRunnable();
        cellInfoRunnable = new CellInfoRunnable();
        movementActivityRunnable = new MovementActivityRunnable();
    }

    /**
     * Method used to stop all the sensors. Once everything is stopped, it generates the log file by calling the method {@link #archiveFile(String, byte[])}
     * @param todoAction String of the action to be performed
     */
    public static void stopLogging(String todoAction) {
        airplaneModeRunnable.stop();
        ambienceRunnable.stop();
        applicationsRunnable.stop();
        audioRunnable.stop();
        batteryChargeRunnable.stop();
        batteryLevelRunnable.stop();
        //bluetoothLERunnable.stop();
        //bluetoothRunnable.stop();
        dozeRunnable.stop();
        headsetRunnable.stop();
        locationGPSRunnable.stop();
        locationNetworkRunnable.stop();
        musicRunnable.stop();
        phoneCallOutRunnable.stop();
        phoneCallInRunnable.stop();
        ringModeRunnable.stop();
        screenRunnable.stop();
        sensorAccelerometerRunnable.stop();
        sensorAmbientTemperatureRunnable.stop();
        sensorGravityRunnable.stop();
        sensorGyroscopeRunnable.stop();
        sensorLightRunnable.stop();
        sensorLinearAccelerometerRunnable.stop();
        sensorMagneticFieldRunnable.stop();
        sensorOrientationRunnable.stop();
        sensorPressureRunnable.stop();
        sensorProximityRunnable.stop();
        sensorRelativeHumidityRunnable.stop();
        sensorRotationVectorRunnable.stop();
        smsInRunnable.stop();
        smsOutRunnable.stop();
        userPresentRunnable.stop();
        wifiNetworksRunnable.stop();
        notificationRunnable.stop();
        touchEventRunnable.stop();
        cellInfoRunnable.stop();
        movementActivityRunnable.stop();

        stopping = true;

        System.out.println("SENSORS: " +iLogApplication.sensorLoggingState.toString());

        byte[] tmp = bufferIsFull(null);
        iLogApplication.archiveFile(todoAction, tmp);
    }

    /**
     * Method used to start all the sensors
     */
    public static void startLogging() {

        //Intent i = new Intent(iLogApplication.getAppContext(), LoggingMonitoringService.class);
        //iLogApplication.getAppContext().startService(i);

        sensorAccelerometerRunnable.run();
        sensorAmbientTemperatureRunnable.run();
        sensorGravityRunnable.run();
        sensorGyroscopeRunnable.run();
        sensorLightRunnable.run();
        sensorLinearAccelerometerRunnable.run();
        sensorMagneticFieldRunnable.run();
        sensorOrientationRunnable.run();
        sensorPressureRunnable.run();
        sensorProximityRunnable.run();
        sensorRelativeHumidityRunnable.run();
        sensorRotationVectorRunnable.run();
        airplaneModeRunnable.run();
        ambienceRunnable.run();
        applicationsRunnable.run();
        audioRunnable.run();
        batteryChargeRunnable.run();
        batteryLevelRunnable.run();
        //bluetoothLERunnable.run();
        //bluetoothRunnable.run();
        dozeRunnable.run();
        headsetRunnable.run();
        musicRunnable.run();
        phoneCallOutRunnable.run();
        phoneCallInRunnable.run();
        ringModeRunnable.run();
        screenRunnable.run();
        //
        smsInRunnable.run();
        smsOutRunnable.run();
        userPresentRunnable.run();
        wifiNetworksRunnable.run();
        notificationRunnable.run();
        touchEventRunnable.run();
        cellInfoRunnable.run();
        movementActivityRunnable.run();

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //location sensors
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !iLogApplication.isNetworkConnected()) {
            iLogApplication.locationGPSRunnable.run();
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && iLogApplication.isNetworkConnected()) {
            iLogApplication.locationNetworkRunnable.run();
        }
    }

    /**
     * Method used to pause all the sensors when Doze occurs
     */
    public static void pauseLogging() {
        airplaneModeRunnable.stop();
        ambienceRunnable.stop();
        applicationsRunnable.stop();
        audioRunnable.stop();
        batteryChargeRunnable.stop();
        batteryLevelRunnable.stop();
        //bluetoothLERunnable.stop();
        //bluetoothRunnable.stop();
        //dozeRunnable.stop();
        headsetRunnable.stop();
        locationGPSRunnable.stop();
        locationNetworkRunnable.stop();
        musicRunnable.stop();
        phoneCallOutRunnable.stop();
        phoneCallInRunnable.stop();
        ringModeRunnable.stop();
        screenRunnable.stop();
        sensorAccelerometerRunnable.stop();
        sensorAmbientTemperatureRunnable.stop();
        sensorGravityRunnable.stop();
        sensorGyroscopeRunnable.stop();
        sensorLightRunnable.stop();
        sensorLinearAccelerometerRunnable.stop();
        sensorMagneticFieldRunnable.stop();
        sensorOrientationRunnable.stop();
        sensorPressureRunnable.stop();
        sensorProximityRunnable.stop();
        sensorRelativeHumidityRunnable.stop();
        sensorRotationVectorRunnable.stop();
        smsInRunnable.stop();
        smsOutRunnable.stop();
        userPresentRunnable.stop();
        wifiNetworksRunnable.stop();
        notificationRunnable.stop();
        touchEventRunnable.stop();
        cellInfoRunnable.stop();
        movementActivityRunnable.stop();
    }

    /**
     * Method used to resume logging after Doze
     */
    public static void resumeLogging() {
        sensorAccelerometerRunnable.run();
        sensorAmbientTemperatureRunnable.run();
        sensorGravityRunnable.run();
        sensorGyroscopeRunnable.run();
        sensorLightRunnable.run();
        sensorLinearAccelerometerRunnable.run();
        sensorMagneticFieldRunnable.run();
        sensorOrientationRunnable.run();
        sensorPressureRunnable.run();
        sensorProximityRunnable.run();
        sensorRelativeHumidityRunnable.run();
        sensorRotationVectorRunnable.run();
        airplaneModeRunnable.run();
        ambienceRunnable.run();
        applicationsRunnable.run();
        audioRunnable.run();
        batteryChargeRunnable.run();
        batteryLevelRunnable.run();
        //bluetoothLERunnable.run();
        //bluetoothRunnable.run();
        //dozeRunnable.run();
        headsetRunnable.run();
        musicRunnable.run();
        phoneCallOutRunnable.run();
        phoneCallInRunnable.run();
        ringModeRunnable.run();
        screenRunnable.run();
        //
        smsInRunnable.run();
        smsOutRunnable.run();
        userPresentRunnable.run();
        wifiNetworksRunnable.run();
        notificationRunnable.run();
        touchEventRunnable.run();
        cellInfoRunnable.run();
        movementActivityRunnable.run();

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //location sensors
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !iLogApplication.isNetworkConnected()) {
            iLogApplication.locationGPSRunnable.run();
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && iLogApplication.isNetworkConnected()) {
            iLogApplication.locationNetworkRunnable.run();
        }
    }

    /**
     * Method that checks if the Service passed as parameter is running
     * @param serviceClass Class of the Service that we have to check the status
     * @return true is the service is running, false otherwise
     */
    public static boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method that checks if the {@link LoggingMonitoringService} service is running
     * @return true if the service is running, false otherwise
     */
    public static boolean isLoggingMonitoringServiceRunning () {
        return isServiceRunning(LoggingMonitoringService.class);
    }

    /**
     * Method that returns the number of files present and that need to be synchronized
     * @return number of files to synchronize, as integer
     */
    public static int getLogFilesNumber() {
        String[] files = getLogDirectory().list();
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
     * Method that detects if the app is collecting data from at least one sensor
     * @return true if at least one sensor is running, false otherwise
     */
    public static boolean atLeastOneOn() {
        boolean toReturn = false;

        for (Map.Entry<Integer, Boolean> entry : sensorLoggingState.entrySet()) {
            if(entry.getValue()) {
               toReturn = true;
            }
        }

        return toReturn;
    }

    /**
     * Method used to start the {@link LoggingMonitoringService} service. Starting from Android Oreo (8.0, SDK grater than 26) foreground services need to be executed
     * using the method {@link Context#startForegroundService(Intent)}, before that the method was {@link Context#startService(Intent)}. The service is started only
     * if not already running.
     * @see <a href="https://developer.android.com/about/versions/oreo/background">https://developer.android.com/about/versions/oreo/background</a>
     */
    public static void startLoggingMonitoringService() {
        if(!iLogApplication.isLoggingMonitoringServiceRunning()) {
            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(getAppContext().getClass().getSimpleName(), "Starting LoggingMonitoringSerice.");
                Intent i = new Intent(iLogApplication.getAppContext(), LoggingMonitoringService.class);
                iLogApplication.getAppContext().startForegroundService(i);
            }
            else {
                Log.d(getAppContext().getClass().getSimpleName(), "Starting LoggingMonitoringSerice.");
                Intent i = new Intent(iLogApplication.getAppContext(), LoggingMonitoringService.class);
                iLogApplication.getAppContext().startService(i);
            }
        }
    }

    /**
     * Method used to stop the {@link LoggingMonitoringService} service.
     */
    public static void stopLoggingMonitoringService() {
        if(iLogApplication.isLoggingMonitoringServiceRunning() && !iLogApplication.atLeastOneOn()) {
            Log.d(getAppContext().getClass().getSimpleName(), "Stopping LoggingMonitoringSerice.");
            Intent i = new Intent(iLogApplication.getAppContext(), LoggingMonitoringService.class);
            iLogApplication.getAppContext().stopService(i);
        }
    }

    /**
     * Method used to restart the logging process
     * TODO - Check if this is still needed
     */
    public static void checkLogging() {
        pendingIntent = PendingIntent.getBroadcast(
                getAppContext(),
                0, // id, optional
                new Intent(getAppContext(), RestartLoggingReceiver.class), // intent to launch
                PendingIntent.FLAG_CANCEL_CURRENT); // PendintIntent flag

        alarms = (AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE);

        alarms.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                10 * 60 * 1000,
                pendingIntent);
    }

    /**
     * Method used to stop the the intent that checks the logging process and restarts it
     */
    public static void stopCheckLogging() {
        if(iLogApplication.alarms!=null && iLogApplication.pendingIntent !=null) {
            iLogApplication.alarms.cancel(iLogApplication.pendingIntent);
        }
    }

    /**
     * Method that checks the usage stat permission at runtime. This permission allows to collect info about the application running in foreground on the smartphone
     * @return true if the permission is granted, false otherwise
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) iLogApplication.getAppContext().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), iLogApplication.getAppContext().getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    /**
     * Method that checks if the permissions that require runtime check are granted.<br>
     * Run time permissions are required only starting from Android Marshmallow (SDK 23)
     * @return true if the user granted all permissions, false otherwise
     */
    public static boolean hasSinglePermissions() {
        String[] permissions = {Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
                /*Manifest.permission.SEND_SMS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA*/};
        ArrayList<String> permissionsToRequest = new ArrayList<String>();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getAppContext() != null && permissions != null) {
            for (String permission : permissions) {
                if(iLogApplication.getAppContext().checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
        }

        if(permissionsToRequest.size() == 0) {
            return true;
        }
        else {
            System.out.println(permissionsToRequest.toString());
            return false;
        }
    }

    /**
     * Method that checks if the user has granted the runtime permission for the permission identified by the permission parameter
     * @param permission String identifying the permission to check, identified as a constant in {@link Manifest.permission}
     * @return true if the permission is granted, false otherwise
     */
    public static boolean hasSinglePermission(String permission) {
        return iLogApplication.getAppContext().checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Method that checks if the user has granted the permission to access the smartphone notifications
     * @return true if the user granted the permission, false otherwise
     */
    public static boolean hasNotificationAccessPermission() {
        ComponentName cn = new ComponentName(context, NotificationService.class);
        String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    /**
     * Method that checks if the user has granted the permission to ignore battery optimization
     * @return true if the user granted the permission, false otherwise
     */
    public static boolean hasBatteryIgnorePermission() {
        String packageName = iLogApplication.getAppContext().getPackageName();
        PowerManager pm = (PowerManager) iLogApplication.getAppContext().getSystemService(Context.POWER_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= 23){
            return pm.isIgnoringBatteryOptimizations(packageName);
        }
        return true;
    }

    /**
     * Method that checks if the user has granted the permission to visualize on top of other applications. This permission is used to detect touches.
     * @return true if the user granted the permission, false otherwise
     */
    public static boolean hasDrawOnTopPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getAppContext() != null) {
            return Settings.canDrawOverlays(iLogApplication.getAppContext());
        }
        return true;
    }

    /**
     * Method that checks if the user has granted all the available permissions<br>
     *      * Run time permissions are required only starting from Android Marshmallow (SDK 23)
     * @return true if the user granted the permissions, false otherwise
     */
    public static boolean hasAllPermissions() {
        return hasBatteryIgnorePermission() && hasUsageStatsPermission() && hasNotificationAccessPermission() && hasSinglePermissions() && hasDrawOnTopPermissions();
    }

    /**
     * Method that requires the user to grant the permissions specified in the String array passed as parameter. This method must be called
     * within an Activity context because we need the user to interact with it and we need to detect the {@link Activity#onActivityResult(int, int, Intent)}<br>
     * If you want to detect the result of this method you need to implement the {@link Activity#onActivityResult(int, int, Intent)} method in the calling activity<br>
     * Run time permissions are required only starting from Android Marshmallow (SDK 23)
     * @param permissions array of Strings as constants in {@link Manifest.permission} that need to be asked to the user
     * @param activity Activity context that need to be passed to collect the result of the request process in the {@link Activity#onActivityResult(int, int, Intent)} method
     */
    public static void requestAllSinglePermissions(String[] permissions, Activity activity) {
        ArrayList<String> permissionsToRequest = new ArrayList<String>();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null && permissions != null) {
            for (String permission : permissions) {
                if(iLogApplication.getAppContext().checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
        }

        ActivityCompat.requestPermissions(activity, Arrays.copyOf(permissionsToRequest.toArray(), permissionsToRequest.toArray().length,String[].class), CODE_RESULT_PERMISSIONS_SENSORS);
    }

    /**
     * Method that requires the user to grant the permission specified in the String passed as parameter. This method must be called
     * within an Activity context because we need the user to interact with it and we need to detect the {@link Activity#onActivityResult(int, int, Intent)}<br>
     * If you want to detect the result of this method you need to implement the {@link Activity#onActivityResult(int, int, Intent)} method in the calling activity
     * @param permission String as constant in {@link Manifest.permission} that need to be asked to the user
     * @param activity Activity context that need to be passed to collect the result of the request process in the {@link Activity#onActivityResult(int, int, Intent)} method
     */
    public static void requestSinglePermission(String permission, Activity activity) {
        ArrayList<String> permissionsToRequest = new ArrayList<String>();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null) {
            if(iLogApplication.getAppContext().checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        ActivityCompat.requestPermissions(activity, Arrays.copyOf(permissionsToRequest.toArray(), permissionsToRequest.toArray().length,String[].class), CODE_RESULT_PERMISSIONS_SENSORS);
    }

    /**
     * Method that asks the user to provide the permission to ignore the battery optimization that by default Android enables on new apps. Without this permission
     * the application cannot work in the background. Depending on the version of the operating system this method produces different behaviours.
     * More in details, what changes is the Action to be set in the {@link Intent#setAction(String)} method:<br>
     * <ul>
     *     <li>
     *         Android lower than Marshmallow (SDK 23): in this case the permission is not required since Android was not implementing this feature
     *     </li>
     *     <li>
     *         Android grater or equal than Marshmallow (SDK 23) and lower than Oreo (SDK 26): in this case we prompt the user with a window where she has to accept to ignore the battery optimization
     *     </li>
     *     <li>
     *         Android grater than Oreo (SDK 26): in this case the procedure is more complicated, the user is showed a list of all the applications and he has to scroll down till she finds
     *         our app and at that moment she can click on it and disable the battery optimization
     *     </li>
     * </ul>
     * If you want to detect the result of the request process you need to implement the {@link Activity#onActivityResult(int, int, Intent)} method in the calling activity.
     * @param activity Activity context that need to be passed to collect the result of the request process in the {@link Activity#onActivityResult(int, int, Intent)} method
     */
    public static void requestBatteryPermission(Activity activity) {
        Intent intent = new Intent();
        String packageName = activity.getPackageName();
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
            }
        }
        if(android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            }
        }
        activity.startActivityForResult(intent, CODE_RESULT_PERMISSIONS_BATTERY);
    }

    /**
     * Method that asks the user to provide the permission to access other apps' notifications<br>
     * If you want to detect the result of the request process you need to implement the {@link Activity#onActivityResult(int, int, Intent)} method in the calling activity.
     * @param activity Activity context that need to be passed to collect the result of the request process in the {@link Activity#onActivityResult(int, int, Intent)} method
     */
    public static void requestNotificationAccessPermission(Activity activity) {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), CODE_RESULT_PERMISSIONS_NOTIFICATION);
        }
    }

    /**
     * Method that asks the user to provide the permission to access the application running in the foreground<br>
     * If you want to detect the result of the request process you need to implement the {@link Activity#onActivityResult(int, int, Intent)} method in the calling activity.
     * @param activity Activity context that need to be passed to collect the result of the request process in the {@link Activity#onActivityResult(int, int, Intent)} method
     */
    public static void requestUsageStatsPermission(Activity activity) {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), CODE_RESULT_PERMISSIONS_USAGE_STATS);
        }
    }

    /**
     * Method that asks the user to detect touches on the screen<br>
     * If you want to detect the result of the request process you need to implement the {@link Activity#onActivityResult(int, int, Intent)} method in the calling activity.
     * @param activity Activity context that need to be passed to collect the result of the request process in the {@link Activity#onActivityResult(int, int, Intent)} method
     */
    public static void requestDrawOnTopPermission(Activity activity) {
        Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        myIntent.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(myIntent, CODE_RESULT_PERMISSIONS_DRAW_ON_TOP);
    }

    /**
     * Method that starts the activity that allows the user to sign in
     */
    public static void startSignInActivity() {
        Intent intent = new Intent(getAppContext(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getAppContext().startActivity(intent);
    }

    /**
     * Method that launches the {@link ProjectSelectionActivity}
     * @param activity An activity context to launch the new activity from
     */
    public static void launchProjectSelectionActivity(Activity activity) {
        Log.d(getAppContext().toString(), "Launch project selection activity");

        Intent intent = new Intent(activity, ProjectSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    /**
     * Method that launches the {@link ProjectActivity}
     * @param activity An activity context to launch the new activity from
     */
    public static void launchProjectActivity(Activity activity) {
        Log.d(getAppContext().toString(), "Launch project activity");

        Intent intent = new Intent(activity, ProjectActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    /**
     * Method used to get the locale id of the device
     * @return String containing the locale id
     */
    public static String getLocale() {
        if(iLogApplication.getAppContext().getResources().getConfiguration().locale.toString().equals("it_IT")) {
            return "it_IT";
        }
        else {
            return "en_US";
        }
    }

    /**
     * Method used to launch the permission activity
     * @param activity An activity context to launch the new activity from
     */
    public static void launchPermissionActivity(Activity activity) {
        Log.d(getAppContext().toString(), "Launch permissions activity");

        Intent intent = new Intent(activity, PermissionsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    /**
     * Method that uses {@link GoogleSignInClient} API to request the user to login with her Google account
     * @param activity An activity context to launch the new activity from
     */
    public static void requestUserLogin(Activity activity) {
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        activity.startActivityForResult(signInIntent, CODE_RESULT_ACCOUNT);
    }

    /**
     * Method that checks if the current device supports sensor batching
     * @return true if the device supports sensor batching, false otherwise
     * @see <a href="https://source.android.com/devices/sensors/batching">Android documentation</a> for more details about sensor batching
     */
    private boolean isSensorBatchingSupported() {
        SensorManager mgr = (SensorManager) getAppContext().getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        int fifoSize = accelerometer.getFifoReservedEventCount();
        return fifoSize > 0;
    }

    /**
     * Methods that verifies is any of the stored tasks is expired and if it is, it is marked as expired
     */
    public static void verifyTasks() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                for(it.unitn.disi.witmee.sensorlog.model.Task task : db.getAllTasksByStatus(it.unitn.disi.witmee.sensorlog.model.Task.STATUS_UNSOLVED)) {
                    if((task.getInstanceTime()+(task.getValidityFor()*1000)) < System.currentTimeMillis()) {
                        db.updateTask(task, it.unitn.disi.witmee.sensorlog.model.Task.STATUS_EXPIRED);
                    }
                }

                updateTaskNotification();
            }
        });
        thread.start();
    }

    /**
     * Methods that verifies is any of the stored questions from the time diary is expired and if it is, it is marked as expired
     */
    public static void verifyTimeDiaries() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                for(Question question : db.getAllQuestionsByStatus(Question.STATUS_RECEIVED)) {
                    if(question.getInstanceTime() + (question.getValidityFor() * 1000) < System.currentTimeMillis()) {
                        Answer answer = new Answer(question.getInstanceTime(), System.currentTimeMillis(), question.getNotifiedTime(), 0, Answer.generateSleepAnswer(), Answer.generateSleepPayload(), question.getInstanceid(), Answer.TYPE_TIMEDIARY, "false", "false");
                        iLogApplication.db.addAnswer(answer);
                        db.updateQuestion(question, Question.STATUS_EXPIRED);
                    }
                }
                updateQuestionNotification();
            }
        });
        thread.start();
    }

    /**
     * Methods that verifies is any of the stored challenges is expired and if it is, it is marked as expired
     */
    public static void verifyChallenges() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                for(Challenge challenge : db.getAllChallengesByStatus(Challenge.STATUS_ONGOING)) {
                    if(challenge.getEnddateAsLong() < System.currentTimeMillis()) {
                        if(challenge.getType().equals(Challenge.TYPE_STATIC)) {
                            db.updateChallengeStatus(challenge, Challenge.STATUS_EXPIRED);
                        }
                        else {
                            db.updateChallengeStatus(challenge, Challenge.STATUS_COMPLETED);
                        }
                    }
                }

                updateTaskNotification();
                iLogApplication.uploadAllContributions();
            }
        });
        thread.start();
    }

    /**
     * Methods that verifies is any of the stored messages is expired and if it is, it is marked as expired
     */
    public static void verifyMessages() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                for(Message message : db.getAllMessagesByStatus(Message.STATUS_UNREAD)) {
                    if((message.getNotifiedTime()+(message.getValidityFor()*1000)) < System.currentTimeMillis()) {
                        db.updateMessage(message, Message.STATUS_EXPIRED);
                    }
                }

                updateMessageNotification();
            }
        });
        thread.start();
    }

    /**
     * Method to sign out the logged in user using the {@link GoogleSignInClient}
     * @param activity An activity context to launch the new activity from
     */
    public static void signOut(Activity activity) {
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        sharedPreferences.edit().putString(Utils.ROLE_KEY, "").commit();
                    }
                });
    }

    /**
     * Method that updates the notification concerning time diaries
     */
    public static void updateQuestionNotification() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                List<Question> unsolvedQuestions = db.getAllQuestionsByStatus(Question.STATUS_RECEIVED);
                if(isMonitoringServiceRunning) {
                    if(unsolvedQuestions.size()!=0) {
                        timediariesBuilder.setContentText(String.format(getAppContext().getResources().getString(R.string.questionnaireNotificationSubtitle), unsolvedQuestions.size()));
                        if(unsolvedQuestions.size()==1) {
                            Intent myIntent = new Intent(iLogApplication.getAppContext(), QuestionActivity.class);
                            myIntent.putExtra("question", unsolvedQuestions.get(0));
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            PendingIntent pendingIntent = PendingIntent.getActivity(iLogApplication.getAppContext(), (int) System.currentTimeMillis(), myIntent, 0);
                            timediariesBuilder.setContentIntent(pendingIntent);
                            //show notification
                            if(notificationManager!=null) {
                                notificationManager.notify(Utils.TIMEDIARIESNOTIFICATIONID, timediariesBuilder.build());
                            }
                        }
                        else {
                            Intent notificationIntent = new Intent(iLogApplication.getAppContext(), HomeActivity.class); //Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
                            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            notificationIntent.putExtra(iLogApplication.INTENT_TASK, "");
                            PendingIntent pendingIntent = PendingIntent.getActivity(iLogApplication.getAppContext(), (int) System.currentTimeMillis(), notificationIntent, 0);
                            timediariesBuilder.setContentIntent(pendingIntent);
                            //show notification
                            if(notificationManager!=null) {
                                notificationManager.notify(Utils.TIMEDIARIESNOTIFICATIONID, timediariesBuilder.build());
                            }
                        }
                    }
                    else {
                        //hide notification
                        if(notificationManager!=null) {
                            notificationManager.cancel(Utils.TIMEDIARIESNOTIFICATIONID);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * Method that updates the notification concerning tasks
     */
    public static void updateTaskNotification() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                List<it.unitn.disi.witmee.sensorlog.model.Task> unsolvedTasks = db.getAllTasksByStatus(it.unitn.disi.witmee.sensorlog.model.Task.STATUS_UNSOLVED);
                if(isMonitoringServiceRunning) {
                    if(unsolvedTasks.size()!=0) {
                        taskBuilder.setContentText(String.format(getAppContext().getResources().getString(R.string.taskNotificationSubtitle), unsolvedTasks.size()));
                        if(unsolvedTasks.size()==1) {
                            Intent myIntent = new Intent(iLogApplication.getAppContext(), TaskActivity.class);
                            myIntent.putExtra("task", unsolvedTasks.get(0));
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            PendingIntent pendingIntent = PendingIntent.getActivity(iLogApplication.getAppContext(), (int) System.currentTimeMillis(), myIntent, 0);
                            taskBuilder.setContentIntent(pendingIntent);
                            //show notification
                            if(notificationManager!=null) {
                                notificationManager.notify(Utils.TASKSNOTIFICATIONID, taskBuilder.build());
                            }
                        }
                        else {
                            Intent notificationIntent = new Intent(iLogApplication.getAppContext(), HomeActivity.class); //Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
                            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            notificationIntent.putExtra(iLogApplication.INTENT_TASK, "");
                            PendingIntent pendingIntent = PendingIntent.getActivity(iLogApplication.getAppContext(), (int) System.currentTimeMillis(), notificationIntent, 0);
                            taskBuilder.setContentIntent(pendingIntent);
                            //show notification
                            if(notificationManager!=null) {
                                notificationManager.notify(Utils.TASKSNOTIFICATIONID, taskBuilder.build());
                            }
                        }
                    }
                    else {
                        //hide notification
                        if(notificationManager!=null) {
                            notificationManager.cancel(Utils.TASKSNOTIFICATIONID);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * Method that updates the notification concerning messages
     */
    public static void updateMessageNotification() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                List<Message> unsolvedMessages = db.getAllMessagesByStatus(Message.STATUS_UNREAD);
                if(isMonitoringServiceRunning) {
                    if(unsolvedMessages.size()!=0) {
                        if(unsolvedMessages.size()==1) {
                            Message message = unsolvedMessages.get(0);
                            messageBuilder.setContentText(message.getDescription());
                            messageBuilder.setStyle(new Notification.BigTextStyle().bigText(message.getDescription()));
                            Intent myIntent = new Intent(iLogApplication.getAppContext(), MessageActivity.class);
                            myIntent.putExtra("message", unsolvedMessages.get(0));
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            PendingIntent pendingIntent = PendingIntent.getActivity(iLogApplication.getAppContext(), (int) System.currentTimeMillis(), myIntent, 0);
                            messageBuilder.setContentIntent(pendingIntent);
                            //show notification
                            if(notificationManager!=null) {
                                notificationManager.notify(Utils.MESSAGENOTIFICATIONID, messageBuilder.build());
                            }
                        }
                        else {
                            messageBuilder.setContentText(String.format(getAppContext().getResources().getString(R.string.messageNotificationSubtitle), unsolvedMessages.size()));
                            Intent notificationIntent = new Intent(iLogApplication.getAppContext(), HomeActivity.class); //Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
                            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            notificationIntent.putExtra(iLogApplication.INTENT_TASK, "");
                            PendingIntent pendingIntent = PendingIntent.getActivity(iLogApplication.getAppContext(), (int) System.currentTimeMillis(), notificationIntent, 0);
                            messageBuilder.setContentIntent(pendingIntent);
                            //show notification
                            if(notificationManager!=null) {
                                notificationManager.notify(Utils.MESSAGENOTIFICATIONID, messageBuilder.build());
                            }
                        }
                    }
                    else {
                        //hide notification
                        if(notificationManager!=null) {
                            notificationManager.cancel(Utils.MESSAGENOTIFICATIONID);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * Method that contains the Http POST request to send the statistics to the backend server.
     * @param googleToken is a String that contains the user token as generated by {@link GoogleSignInClient}
     * @param content String containing the statistics to be sent
     * @return String containing the response from the server if everything went fine, "null" otherwise
     */
    public static String uploadStatistics(String googleToken, String content) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("token", googleToken);

        MultiValueMap<String, Object> payload = new LinkedMultiValueMap<String, Object>();
        payload.add("content", content);

        Log.d(getAppContext().toString(), headers.toString());

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Utils.returnServerUrl("8094") + "uploadstatistics");
        Log.d(getAppContext().toString(), Utils.returnServerUrl("8094") + "uploadstatistics");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(payload, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpEntity<String> response = restTemplate.exchange(
                    builder.build().encode().toUri(),
                    HttpMethod.POST,
                    entity,
                    String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }

    /**
     * Method that fixes the sensor batch delay - TODO: NOT WORKING, bug in Android Sensor framework, there is no easy way to fix this, check the references
     * @see <a href="https://issuetracker.google.com/issues/36972829">https://issuetracker.google.com/issues/36972829</a> and <a href="https://stackoverflow.com/questions/4691097/what-is-android-accelerometer-min-and-max-range">https://stackoverflow.com/questions/4691097/what-is-android-accelerometer-min-and-max-range</a>
     */
    public void startSensorBatchFix() {
        if(isSensorBatchingSupported()) {
            //initialize array for collecting timing information
            final List<Integer> sensorList = Arrays.asList(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GRAVITY, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_LINEAR_ACCELERATION,
                    Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_ORIENTATION, Sensor.TYPE_PROXIMITY, Sensor.TYPE_AMBIENT_TEMPERATURE,
                    Sensor.TYPE_LIGHT, Sensor.TYPE_PRESSURE, Sensor.TYPE_RELATIVE_HUMIDITY);

            final SensorManager mSensorManager = (SensorManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
            final SensorListenerBatchTraining sensorListener = new SensorListenerBatchTraining();

            //startall
            for(int index=0; index < sensorList.size(); index++) {
                batchSensing.put(sensorList.get(index), new ArrayList<BatchObject>());

                Sensor sensor = mSensorManager.getDefaultSensor(sensorList.get(index));
                mSensorManager.registerListener(sensorListener, sensor, 0);
            }

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //stopall after one second
                    for(int index=0; index < sensorList.size(); index++) {
                        Sensor sensor = mSensorManager.getDefaultSensor(sensorList.get(index));
                        mSensorManager.unregisterListener(sensorListener, sensor);
                    }

                    //startall
                    for(int index=0; index < sensorList.size(); index++) {
                        Sensor sensor = mSensorManager.getDefaultSensor(sensorList.get(index));
                        mSensorManager.registerListener(sensorListener, sensor, 0);
                    }

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //stopall after one second
                            for(int index=0; index < sensorList.size(); index++) {
                                Sensor sensor = mSensorManager.getDefaultSensor(sensorList.get(index));
                                mSensorManager.unregisterListener(sensorListener, sensor);
                            }

                            //calculate offsets
                            for(int index=0; index < sensorList.size(); index++) {
                                calculateOffset(sensorList.get(index));
                            }

                            printMapSize(batchSensing);
                            printMap(batchSensingOffsets);
                            printMap(batchSensingDivisors);

                            iLogApplication.startLogging();
                        }
                    }, 500);
                }
            }, 500);
        }
    }

    /**
     * Method that prints a HashMap passed as parameter, for debug purposes
     * @param mp {@link Map} to be printed
     */
    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }

    /**
     * Method that prints the size of the HashMap passed as parameter, for debug purposes
     * @param mp {@link Map} to be printed
     */
    public static void printMapSize(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + ((ArrayList<BatchObject>) pair.getValue()).size());
        }
    }

    /**
     * Method used by {@link #startSensorBatchFix()} that fixes the sensor batch delay - TODO: NOT WORKING, bug in Android Sensor framework, there is no easy way to fix this, check the references
     * @see <a href="https://issuetracker.google.com/issues/36972829">Google issuetracker</a> and <a = href="https://stackoverflow.com/questions/4691097/what-is-android-accelerometer-min-and-max-range">StackOverflow</a> for more details about the sensor batching timestamp bug
     * @param sensorID Integer representing the id of the sensor to fix
     */
    private static void calculateOffset(int sensorID) {
        if(batchSensing.get(sensorID).size() > 0) {
            BatchObject event1 = batchSensing.get(sensorID).get(0);
            BatchObject event2 = batchSensing.get(sensorID).get(batchSensing.get(sensorID).size()-1);

            long timestampDelta = event2.getFakeTimestamp() - event1.getFakeTimestamp();
            long sysTimeDelta = event2.getRealTimestamp() - event1.getRealTimestamp();
            long divisor; // to get from timestamp to milliseconds
            long offset; // to get from event milliseconds to system milliseconds

            if(sysTimeDelta != 0) {
                if (timestampDelta/sysTimeDelta > 1000) { // in reality ~1 vs ~1,000,000
                    // timestamps are in nanoseconds
                    divisor = 1000000;
                } else {
                    // timestamps are in milliseconds
                    divisor = 1;
                }
            }
            else {
                divisor = 1;
            }

            offset = event1.getRealTimestamp() - (event1.getFakeTimestamp() / divisor);

            batchSensingOffsets.put(sensorID, offset);
            batchSensingDivisors.put(sensorID, divisor);
        }
    }

    /**
     * Method used to get the mamixum range for each sensor on the device. Requested by InfAI within the QROWD project
     * @return JSONArray as String containing a list of sensors with attached maximum range value, as a float number
     */
    public static String getSensorMaxValues() {
        JSONArray values = new JSONArray();

        SensorManager mSensorManager = (SensorManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        for (Map.Entry<Integer, Boolean> entry : sensorLoggingState.entrySet()) {
            System.out.println(entry.getKey() + "/" + entry.getValue());

            try {
                Sensor sensor = mSensorManager.getDefaultSensor(entry.getKey());
                System.out.println("MAX: "+sensor.getMaximumRange());
                JSONObject sensorObject = new JSONObject();
                sensorObject.put("key", entry.getKey());
                sensorObject.put("max", sensor.getMaximumRange());
                values.put(sensorObject);
            }
            catch(Exception e) {
            }
        }

        System.out.println("MAX: "+values);

        return values.toString();
    }

    /**
     * Class that extends AsyncTask and uploads the answers content (and not payload) to the server
     */
    public static class uploadAnswerContent extends AsyncTask<Object, Void, ArrayList<Object>> {
        @Override
        protected ArrayList<Object> doInBackground(final Object... data) {
            String token = data[0].toString();
            Answer answer = (Answer) data[1];
            answer.setPayload(new JSONArray());

            Log.d(this.toString(), "Uploading answers");

            ArrayList<Object> returns = new ArrayList<Object>();
            returns.add(answer);
            returns.add(uploadAnswer(token, answer));
            return returns;
        }
        @Override
        protected void onPostExecute(ArrayList<Object> result) {
            Answer answer = (Answer) result.get(0);

            Log.d(this.getClass().getSimpleName(), result.get(1).toString());

            if(!result.get(1).equals("error")) {
                JSONObject response = null;
                try {
                    response = new JSONObject(result.get(1).toString());
                    if(response.getString("status").equals("done_message")) {
                        db.updateAnswerContentSynchronization(answer, Challenge.SYNCHRONIZATION_TRUE);
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
     * Class that extends AsyncTask and uploads the answers content (and not payload) to the server
     */
    public static class uploadAnswerPayload extends AsyncTask<Object, Void, ArrayList<Object>> {
        @Override
        protected ArrayList<Object> doInBackground(final Object... data) {
            String token = data[0].toString();
            Answer answer = (Answer) data[1];

            Log.d(this.toString(), "Uploading answers");

            ArrayList<Object> returns = new ArrayList<Object>();
            returns.add(answer);
            returns.add(uploadAnswer(token, answer));
            return returns;
        }
        @Override
        protected void onPostExecute(ArrayList<Object> result) {
            Answer answer = (Answer) result.get(0);

            Log.d(this.getClass().getSimpleName(), result.get(1).toString());

            if(!result.get(1).equals("error")) {
                JSONObject response = null;
                try {
                    response = new JSONObject(result.get(1).toString());
                    if(response.getString("status").equals("done_message")) {
                        db.updateAnswerPayloadSynchronization(answer, Challenge.SYNCHRONIZATION_TRUE);
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
     * Class that extends AsyncTask and uploads the answers content (and not payload) to the server
     */
    public static class uploadAnswer extends AsyncTask<Object, Void, ArrayList<Object>> {
        @Override
        protected ArrayList<Object> doInBackground(final Object... data) {
            String token = data[0].toString();
            Answer answer = (Answer) data[1];

            Log.d(this.toString(), "Uploading answers");

            ArrayList<Object> returns = new ArrayList<Object>();
            returns.add(answer);
            returns.add(uploadAnswer(token, answer));
            return returns;
        }
        @Override
        protected void onPostExecute(ArrayList<Object> result) {
            Answer answer = (Answer) result.get(0);

            Log.d(this.getClass().getSimpleName(), result.get(1).toString());

            if(!result.get(1).equals("error")) {
                JSONObject response = null;
                try {
                    response = new JSONObject(result.get(1).toString());
                    if(response.getString("status").equals("done_message")) {
                        db.updateAnswerContentSynchronization(answer, Challenge.SYNCHRONIZATION_TRUE);
                        db.updateAnswerPayloadSynchronization(answer, Challenge.SYNCHRONIZATION_TRUE);
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
     * Class that extends AsyncTask and uploads profile info to the server
     */
    public static class uploadProfileInfo extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(final String... data) {
            String token = data[0];
            String profile = data[1];
            String sensors = data[2];

            Log.d(this.toString(), "Uploading profile and sensors info");
            return uploadProfileInfo(token, profile, sensors, iLogApplication.sharedPreferences.getString(Utils.CONFIG_ENDPOINTUPLOADPROFILE, ""));
        }
        @Override
        protected void onPostExecute(String result) {
            System.out.println(result);
            if(!result.contains("error")) {
                Log.d(this.toString(), "Profile uploaded");
                sharedPreferences.edit().putBoolean(Utils.CONFIG_PROFILE_AND_SENSORS_UPLOADED, true).commit();
            }
        }
    }

    /**
     * Class that extends AsyncTask and uploads info about the challenges to the server
     */
    public static class uploadChallengesParticipationInfo extends AsyncTask<Object, Void, ArrayList<Object>> {
        @Override
        protected ArrayList<Object> doInBackground(final Object... data) {
            String token = data[0].toString();
            Challenge challenge = (Challenge) data[1];

            Log.d(this.toString(), "Uploading challenges synchronization info");

            ArrayList<Object> returns = new ArrayList<Object>();
            returns.add(challenge);
            returns.add(uploadChallengesParticipationInfo(token, challenge));
            return returns;
        }
        @Override
        protected void onPostExecute(ArrayList<Object> result) {
            Challenge challenge = (Challenge) result.get(0);

            Log.d(this.getClass().getSimpleName(), result.get(1).toString());

            if(!result.get(1).equals("error")) {
                JSONObject response = null;
                try {
                    response = new JSONObject(result.get(1).toString());
                    if(response.getString("status").equals("done_message")) {
                        db.updateChallengeSynchronization(challenge, Challenge.SYNCHRONIZATION_TRUE);
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
     * Class that extends AsyncTask and uploads confirmations of feedback element ({@link Challenge}, {@link Message}, {@link Question}, {@link Task}) reception to the server
     */
    public static class uploadReceptionConfirmation extends AsyncTask<Object, Void, ArrayList<Object>> {
        @Override
        protected ArrayList<Object> doInBackground(final Object... data) {
            String token = data[0].toString();
            Object object = data[1];

            Log.d(this.toString(), "Uploading challenges synchronization info");

            ArrayList<Object> returns = new ArrayList<Object>();
            returns.add(object);
            returns.add(uploadReceptionConfirmation(token, object));
            return returns;
        }
        @Override
        protected void onPostExecute(ArrayList<Object> result) {
            Object object = result.get(0);

            Log.d(this.getClass().getSimpleName(), result.get(1).toString());

            if(!result.get(1).equals("error")) {
                JSONObject response = null;
                try {
                    response = new JSONObject(result.get(1).toString());
                    if(response.getString("status").equals("done_message")) {
                        if (object instanceof Question) {
                            Question question = ((Question) object);
                            Log.d(this.toString(), "Question "+question.getInstanceid()+" synchronized");
                            db.updateQuestionSynchronization(question, Question.SYNCHRONIZATION_TRUE);
                        } else if (object instanceof Message) {
                            Message message = ((Message) object);
                            Log.d(this.toString(), "Message "+message.getMessageid()+" synchronized");
                            db.updateMessageSynchronization(message, Message.SYNCHRONIZATION_TRUE);
                        } else if (object instanceof it.unitn.disi.witmee.sensorlog.model.Task) {
                            it.unitn.disi.witmee.sensorlog.model.Task task = ((it.unitn.disi.witmee.sensorlog.model.Task) object);
                            Log.d(this.toString(), "Task "+task.getInstanceid()+" synchronized");
                            db.updateTaskSynchronization(task, it.unitn.disi.witmee.sensorlog.model.Task.SYNCHRONIZATION_TRUE);
                        }
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
     * Method that uploads all the contributions from the user
     */
    public static void uploadAllContributions() {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getAppContext(), gso);
        googleSignInClient.silentSignIn()
                .addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            String idToken = account.getIdToken();

                            //Always upload, don't check Wifi-3G
                            uploadChallengesParticipationInfo(idToken);
                            uploadMessagesSynchronize(idToken);
                            uploadTasksSynchronize(idToken);
                            uploadTimediariesSynchronize(idToken);

                            //Check policies about Wifi-3g
                            uploadAllAnswers(idToken);
                        } catch (ApiException e) {
                            e.printStackTrace();
                            if(e.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED && !iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, "").equals("")) {
                                startSignInActivity();
                            }
                        }
                        finally {
                            isSynchronizing = false;
                        }
                    }
                });
    }

    /**
     * Method that loops over the user provided answers and uploads them singularly to the database
     * @param idToken is a String that contains the user token as generated by {@link GoogleSignInClient}
     */
    public static void uploadAllAnswers(String idToken) {
        if(isUserLoggedIn()) {
            for(Answer answer: db.getAllAnswersBySynchronization(Answer.SYNCHRONIZATION_FALSE)) {
                if(iLogApplication.isNetworkConnected()) {
                    if(answer.getAnswersSynchronization().equals(Answer.SYNCHRONIZATION_FALSE) && answer.getPayloadSynchronization().equals(Answer.SYNCHRONIZATION_FALSE)) {
                        new uploadAnswer().execute(idToken, answer);
                    }
                    else if (answer.getAnswersSynchronization().equals(Answer.SYNCHRONIZATION_TRUE) && answer.getPayloadSynchronization().equals(Answer.SYNCHRONIZATION_FALSE)) {
                        new uploadAnswerPayload().execute(idToken, answer);
                    }
                }
                else {
                    if(checkPayloadUploadPolicy(answer)) {//if picture only via Wi-Fi
                        if(answer.getAnswersSynchronization().equals(Answer.SYNCHRONIZATION_FALSE)) {
                            new uploadAnswerContent().execute(idToken, answer);
                        }
                    }
                    else {
                        if(answer.getAnswersSynchronization().equals(Answer.SYNCHRONIZATION_FALSE) && answer.getPayloadSynchronization().equals(Answer.SYNCHRONIZATION_FALSE)) {
                            new uploadAnswer().execute(idToken, answer);
                        }
                        else if (answer.getAnswersSynchronization().equals(Answer.SYNCHRONIZATION_TRUE) && answer.getPayloadSynchronization().equals(Answer.SYNCHRONIZATION_FALSE)) {
                            new uploadAnswerPayload().execute(idToken, answer);
                        }
                    }
                }
            }
        }
    }

    /**
     * Method that returns true if the payload of the answer can be synched only viw Wi-Fi
     * @param answer Answer to be checked, using the type field
     * @return True if the payload has to be synchronized only via Wi-Fi
     */
    private static boolean checkPayloadUploadPolicy(Answer answer) {
        if(answer.getPayload().toString().contains("picture")) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Method that loops over the {@link Challenge} information and uploads them singularly to the database
     * @param idToken is a String that contains the user token as generated by {@link GoogleSignInClient}
     */
    public static void uploadChallengesParticipationInfo(String idToken) {
        for(Challenge challenge: db.getAllChallengesBySynchronization(Challenge.SYNCHRONIZATION_FALSE)) {
            new uploadChallengesParticipationInfo().execute(idToken, challenge);
        }
    }

    /**
     * Method that loops over the {@link Message} confirmations and uploads them singularly to the database
     * @param idToken is a String that contains the user token as generated by {@link GoogleSignInClient}
     */
    public static void uploadMessagesSynchronize(String idToken) {
        for(Message message: db.getAllMessagesBySynchronization(Message.SYNCHRONIZATION_FALSE)) {
            new uploadReceptionConfirmation().execute(idToken, message);
        }
    }

    /**
     * Method that loops over the {@link Task} confirmations and uploads them singularly to the database
     * @param idToken is a String that contains the user token as generated by {@link GoogleSignInClient}
     */
    public static void uploadTasksSynchronize(String idToken) {
        for(it.unitn.disi.witmee.sensorlog.model.Task task: db.getAllTasksBySynchronization(it.unitn.disi.witmee.sensorlog.model.Task.SYNCHRONIZATION_FALSE)) {
            new uploadReceptionConfirmation().execute(idToken, task);
        }
    }

    /**
     * Method that loops over the {@link Question} confirmations and uploads them singularly to the database
     * @param idToken is a String that contains the user token as generated by {@link GoogleSignInClient}
     */
    public static void uploadTimediariesSynchronize(String idToken) {
        for(Question question: db.getAllQuestionsBySynchronization(Question.SYNCHRONIZATION_FALSE)) {
            new uploadReceptionConfirmation().execute(idToken, question);
        }
    }

    /**
     * Method that uses Http POST to push one answer to the server
     * @param googleToken is a String that contains the user token as generated by {@link GoogleSignInClient}
     * @param answer String content of the answer to be uploaded
     * @return String containing the response from the server if everything went fine, "error" otherwise
     */
    public static String uploadAnswer(String googleToken, Answer answer) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("token", googleToken);

        try {
            MultiValueMap<String, Object> payload = new LinkedMultiValueMap<String, Object>();

            Answer answerToSend = replacePictureURIWithPicture(answer);

            Log.d(getAppContext().toString(), answerToSend.toJSON().toString());
            Log.d(getAppContext().toString(), answerToSend.toJSON().toString().length()+"");

            payload.add("answer", answerToSend.toJSON().toString());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(iLogApplication.sharedPreferences.getString(Utils.CONFIG_SERVERBASEURL, "")+iLogApplication.sharedPreferences.getString(Utils.CONFIG_PORTSEPATATOR, "")+iLogApplication.sharedPreferences.getInt(Utils.CONFIG_PORTAVAILABLECHALLENGES, 0) + iLogApplication.sharedPreferences.getString(Utils.CONFIG_SEPARATOR, "") + iLogApplication.sharedPreferences.getString(Utils.CONFIG_ENDPOINTUPLOADANSWERS, ""));

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(payload, headers);

            RestTemplate restTemplate = new RestTemplate();
            try {
                HttpEntity<String> response = restTemplate.exchange(
                        builder.build().encode().toUri(),
                        HttpMethod.POST,
                        entity,
                        String.class);
                return response.getBody().toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Method that uses Http POST to push a reception confirmation to the server
     * @param googleToken is a String that contains the user token as generated by {@link GoogleSignInClient}
     * @param object String content of the receipt to be synchronized
     * @return String containing the response from the server if everything went fine, "error" otherwise
     */
    public static String uploadReceptionConfirmation(String googleToken, Object object) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("token", googleToken);

        try {
            MultiValueMap<String, Object> payload = new LinkedMultiValueMap<String, Object>();

            if (object instanceof Question) {
                payload.add("question", ((Question) object).toJSON().toString());
            } else if (object instanceof Message) {
                payload.add("message", ((Message) object).toJSON().toString());
            } else if (object instanceof it.unitn.disi.witmee.sensorlog.model.Task) {
                payload.add("task", ((it.unitn.disi.witmee.sensorlog.model.Task) object).toJSON().toString());
            }

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(iLogApplication.sharedPreferences.getString(Utils.CONFIG_SERVERBASEURL, "")+iLogApplication.sharedPreferences.getString(Utils.CONFIG_PORTSEPATATOR, "")+iLogApplication.sharedPreferences.getInt(Utils.CONFIG_PORTAVAILABLECHALLENGES, 0) + iLogApplication.sharedPreferences.getString(Utils.CONFIG_SEPARATOR, "") + iLogApplication.sharedPreferences.getString(Utils.CONFIG_ENDPOINTUPLOADRECEPTIONCONFIRMATION, ""));

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(payload, headers);

            RestTemplate restTemplate = new RestTemplate();
            try {
                HttpEntity<String> response = restTemplate.exchange(
                        builder.build().encode().toUri(),
                        HttpMethod.POST,
                        entity,
                        String.class);
                return response.getBody().toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Method that uses Http POST to push a challenge participation to the server
     * @param googleToken is a String that contains the user token as generated by {@link GoogleSignInClient}
     * @param challenge {@link Challenge} object of the challenge we have to syncrhonize the participation status
     * @return String containing the response from the server if everything went fine, "error" otherwise
     */
    public static String uploadChallengesParticipationInfo(String googleToken, Challenge challenge) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("token", googleToken);

        try {
            MultiValueMap<String, Object> payload = new LinkedMultiValueMap<String, Object>();
            payload.add("challenge", challenge.toJSON().toString());
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(iLogApplication.sharedPreferences.getString(Utils.CONFIG_SERVERBASEURL, "")+iLogApplication.sharedPreferences.getString(Utils.CONFIG_PORTSEPATATOR, "")+iLogApplication.sharedPreferences.getInt(Utils.CONFIG_PORTAVAILABLECHALLENGES, 0) + iLogApplication.sharedPreferences.getString(Utils.CONFIG_SEPARATOR, "") + iLogApplication.sharedPreferences.getString(Utils.CONFIG_ENDPOINTUPLOADCHALLENGESSYNCHRONIZATIONINFO, ""));

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(payload, headers);

            RestTemplate restTemplate = new RestTemplate();
            try {
                HttpEntity<String> response = restTemplate.exchange(
                        builder.build().encode().toUri(),
                        HttpMethod.POST,
                        entity,
                        String.class);
                return response.getBody().toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Method that uses Http POST to push profile information to the server
     * @param googleToken is a String that contains the user token as generated by {@link GoogleSignInClient}
     * @param profile JSONObject containing profile information to be sent
     * @param sensors JSONObject containing sensor information to be sent, generated with {@link #getSensorMaxValues()}
     * @param endPoint String containing the URL of the enpoint on the server
     * @return String containing the response from the server if everything went fine, "error" otherwise
     */
    public static String uploadProfileInfo(String googleToken, String profile, String sensors, String endPoint) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("token", googleToken);

        MultiValueMap<String, Object> payload = new LinkedMultiValueMap<String, Object>();
        payload.add("profile", profile);
        payload.add("sensors", sensors);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(Utils.returnServerUrl() + endPoint);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(payload, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            HttpEntity<String> response = restTemplate.exchange(
                    builder.build().encode().toUri(),
                    HttpMethod.POST,
                    entity,
                    String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Method that returns the content of the use feedback elements ({@link Challenge}, {@link Message}, {@link Question}, {@link Task}) in the language present on the
     * smartphone, if it is Italian, the default language otherwise
     * @param content JSONArray containing the questions/answers
     * @return String in the same language as the smartphone
     */
    public static String getLocalizedContent(JSONArray content) {
        Locale current = iLogApplication.getAppContext().getResources().getConfiguration().locale;
        for(int index=0;index<content.length();index++) {
            try {
                if(current.toLanguageTag().equals(content.getJSONObject(index).getString("l"))) {
                    return content.getJSONObject(index).getString("t");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return getDefaultContent(content);
    }

    /**
     * Method that returns the content of the use feedback elements ({@link Challenge}, {@link Message}, {@link Question}, {@link Task}) in the default language,
     * American English
     * @param content JSONArray containing the questions/answers
     * @return String english
     */
    public static String getDefaultContent(JSONArray content) {
        for(int index=0;index<content.length();index++) {
            try {
                if("en-US".equals(content.getJSONObject(index).getString("l"))) {
                    return content.getJSONObject(index).getString("t");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    /**
     * Method that detects if a point expressed as a {@link LatLng} object is inside a polygon represented as a GeoJSON. If the GeoJSON is empty or not valid
     * the method returns true
     * @param GeoJSON String containing the GeoJSON information
     * @param point {@link LatLng} object containing the point to check
     * @return true if the point is inside the polygon, false otherwise
     */
    public static boolean pointInPolygon(String GeoJSON, LatLng point) {
        if(!GeoJSON.equals("")) {
            try {
                GeoJSONObject geoJSON = com.cocoahero.android.geojson.GeoJSON.parse(GeoJSON);
                Feature feature = (Feature) geoJSON;
                JSONArray coordinates = feature.getGeometry().toJSON().getJSONArray("coordinates");
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                try {
                    for (int index1 = 0; index1 < coordinates.length(); index1++) {
                        for (int index2 = 0; index2 < coordinates.getJSONArray(index1).length(); index2++) {
                            builder.include(new LatLng(coordinates.getJSONArray(index1).getJSONArray(index2).getDouble(1), coordinates.getJSONArray(index1).getJSONArray(index2).getDouble(0)));
                        }
                    }
                } catch(JSONException e) {
                    builder = new LatLngBounds.Builder();
                    for (int index1 = 0; index1 < coordinates.length(); index1++) {
                        for (int index2 = 0; index2 < coordinates.getJSONArray(index1).length(); index2++) {
                            for (int index3 = 0; index3 < coordinates.getJSONArray(index1).getJSONArray(index2).length(); index3++) {
                                builder.include(new LatLng(coordinates.getJSONArray(index1).getJSONArray(index2).getJSONArray(index3).getDouble(1), coordinates.getJSONArray(index1).getJSONArray(index2).getJSONArray(index3).getDouble(0)));
                            }
                        }
                    }
                }
                LatLngBounds bound = builder.build();

                if (bound.contains(point)) {
                    return true;
                } else {
                    return false;
                }
            }
            catch (Exception e) {
                return false;
            }
        }
        else {
            return true;
        }
    }

    /**
     * Method that deletes all the databases present in the application
     */
    public static void clearDatabase() {
        db.deleteAllTasks();
        db.deleteAllAnswers();
        db.deleteAllChallenges();
        db.deleteAllMessages();
        db.deleteAllTimeDiaries();
    }

    /**
     * Method used for debug purposes that prints all the available challenges
     */
    public static void printAllChallenges() {
        for(Challenge challenge: db.getAllChallenges()) {
            try {
                System.out.println("CHALLENGE: "+challenge.toJSON().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static Answer replacePictureURIWithPicture(Answer answer) {
        return new Answer(answer);
    }
}

/*
t
[{"q": {"id": 1,"c": [],"at": "s","t": "t","p": [{"l": "en-US","t": "What are you doing?"}, {"l": "it-IT","t": "Cosa stai facendo?"}]},"a": [{"id": 1,"c": [],"c_id": 74549,"p": [{"l": "en-US","t": "Sleeping"}, {"l": "it-IT","t": "Dormire"}]}, {"id": 2,"c": [],"c_id": 31428,"p": [{"l": "en-US","t": "Study"}, {"l": "it-IT","t": "Studio"}]}, {"id": 3,"c": [],"c_id": 4581,"p": [{"l": "en-US","t": "Lesson"}, {"l": "it-IT","t": "Lezione"}]}, {"id": 4,"c": [],"c_id": 1501,"p": [{"l": "en-US","t": "En route"}, {"l": "it-IT","t": "In viaggio/spostamento da...a..."}]}, {"id": 5,"c": [],"c_id": 4317,"p": [{"l": "en-US","t": "Eating"}, {"l": "it-IT","t": "Mangiare"}]}, {"id": 6,"c": [],"c_id": 3405,"p": [{"l": "en-US","t": "Selfcare"}, {"l": "it-IT","t": "Cura della persona"}]}, {"id": 7,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Coffee break, cigarette, beer, etc."}, {"l": "it-IT","t": "Pausa caffè, sigaretta, birra ecc"}]}, {"id": 8,"c": [],"c_id": 25864,"p": [{"l": "en-US","t": "Social life"}, {"l": "it-IT","t": "Vita Sociale/divertimento"}]}, {"id": 9,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Al the phone; in chat WhatsApp"}, {"l": "it-IT","t": "Al telefono; in chat WhatsApp"}]}, {"id": 10,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Watcing Youtube, Tv-shows, etc."}, {"l": "it-IT","t": "Guardo Youtube, Serie-Tv, ecc."}]}, {"id": 11,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Social media (Facebook, Instagram, etc.)"}, {"l": "it-IT","t": "Social media (Facebook, Instagram, ecc.)"}]}, {"id": 12,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Movie Theater, Theater, Concert, Exhibit, ..."}, {"l": "it-IT","t": "Cinema, Teatro, Concerto, Mostra, ..."}]}, {"id": 13,"c": [],"c_id": 2681,"p": [{"l": "en-US","t": "Sport"}, {"l": "it-IT","t": "Sport/Attività fisica"}]}, {"id": 14,"c": [],"c_id": 387,"p": [{"l": "en-US","t": "Shopping"}, {"l": "it-IT","t": "Shopping/Fare la spesa"}]}, {"id": 15,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Housework"}, {"l": "it-IT","t": "Lavori domestici"}]}, {"id": 16,"c": [],"c_id": 4421,"p": [{"l": "en-US","t": "Rest/nap"}, {"l": "it-IT","t": "Riposo/Pennichella"}]}, {"id": 17,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Reading a book; listening to music"}, {"l": "it-IT","t": "Leggo un libro; ascolto musica"}]}, {"id": 18,"c": [],"c_id": 2206,"p": [{"l": "en-US","t": "Hobbies"}, {"l": "it-IT","t": "Altro Hobby/tempo libero"}]}, {"id": 19,"c": [],"c_id": 112289,"p": [{"l": "en-US","t": "Work"}, {"l": "it-IT","t": "Lavoro"}]}, {"id": 20,"c": [],"c_id": 93394,"p": [{"l": "en-US","t": "Other"}, {"l": "it-IT","t": "Altro"}]}]}]
m
[{"q": {"id": 1,"c": [],"t":"m","cnt": [{"or":1, "ty":"tiv", "p":[{"l":"en-US","t":"Welcome"},{"l":"it-IT","t":"Benvenuto"}]}, {"or":2, "ty":"tv", "p":[{"l":"en-US","t":"Hi, thank you for participating in the QROWDLab! The experiment starts on May 28 and ends on June 7. We might send you messages during this time. If you wish to reply, share your ideas or make any suggestion, please contact: trento.smart@comune.trento.it"},{"l":"it-IT","t":"Ciao, grazie per partecipare al QROWDLab! L'esperimento si svolge dal 28 maggio al 7 giugno. Durante questo periodo potremmo inviarti dei messaggi. Se desideri rispondere, condividere le tue impressioni o proporre dei suggerimenti scrivi a: trento.smart@comune.trento.it"}]}, {"or":3, "ty":"iv", "p":"http://qrowd-project.eu/wp-content/uploads/2017/12/1234-1.png"}]}}]
l
[{"q": {"id": 1,"c": [],"t": "l","l": {"lat": 46.0661043,"lon": 11.121961},"p": [{"t": "We detected that on {trip.start_timestamp}, you stopped near the point on the map below, close to {trip.start_address}. Is this correct?","l": "en-US"},{"t": "We detected that on {trip.start_timestamp}, you stopped near the point on the map below, close to {trip.start_address}. Is this correct?","l": "it-IT"}]},"a": [{"id": 1,"c_id": -1,"p": [{"t": "Yes","l": "en-US"},{"t": "Si","l": "it-IT"}],"c": []},{"id": 2,"c_id": -1,"p": [{"t": "No, I was not there at that time","l": "en-US"},{"t": "No, I was not there at that time","l": "it-IT"}],"c": []},{"id": 3,"c_id": -1,"p": [{"t": "No, I was there but I did not start a trip at that time","l": "en-US"},{"t": "No, I was there but I did not start a trip at that time","l": "it-IT"}],"c": []},{"id": 4,"c_id": -1,"p": [{"t": "I don't remember","l": "en-US"},{"t": "Non mi ricordo","l": "it-IT"}],"c": []}]}]
i
[{"a": [{"c": [],"id": 1,"c_id": -1,"p": [{"t": "Reached your destination","l": "en-US"}, {"t": "Hai raggiunto la tua destinazione","l": "it-IT"}]}, {"c": [],"id": 2,"c_id": -1,"p": [{"t": "Briefly stopped to change transportation mode","l": "en-US"}, {"t": "Ti sei fermato/a per cambiare mezzo di trasporto","l": "it-IT"}]}, {"c": [],"id": 3,"c_id": -1,"p": [{"t": "I don't remember","l": "en-US"}, {"t": "Non mi ricordo","l": "it-IT"}]}], "q": {"c": [], "id": 1, "t": "i", "l": "https://www.tesla.com/sites/default/files/images/software_update.jpg"}}]
ms
[{"a": [{"c": [], "id": 1, "c_id": -1, "p": [{"t": "Yes", "l": "en-US"}, {"t": "S\u00ec", "l": "it-IT"}]}, {"c": [], "id": 2, "c_id": -1, "p": [{"t": "No", "l": "en-US"}, {"t": "No", "l": "it-IT"}]}, {"c": [], "id": 3, "c_id": -1, "p": [{"t": "I don't remember", "l": "en-US"}, {"t": "Non mi ricordo", "l": "it-IT"}]}], "q": {"c": [], "id": 1, "la": [23.43434, 43.54645], "t": "ms", "p": [{"t": "We detected that on 6/6 at around 9:09, you made a trip from Via Giovanni Battista Trener to Via Romano Guardini, arriving around 9:14. Is this correct?", "l": "en-US"}, {"t": "Abbiamo rilevato che il giorno 6/6 dalle 9:09 alle 9:14 hai effettuato uno spostamento da Via Giovanni Battista Trener a Via Romano Guardini. \u00c8 corretto?", "l": "it-IT"}], "lo": [23.43434, 43.54645]}}]
al
[{"a": [{"c": [],"id": 1,"c_id": -1,"p": [{"t": "Reached your destination","l": "en-US"}, {"t": "Hai raggiunto la tua destinazione","l": "it-IT"}]}, {"c": [],"id": 2,"c_id": -1,"p": [{"t": "Briefly stopped to change transportation mode","l": "en-US"}, {"t": "Ti sei fermato/a per cambiare mezzo di trasporto","l": "it-IT"}]}, {"c": [],"id": 3,"c_id": -1,"p": [{"t": "I don't remember","l": "en-US"}, {"t": "Non mi ricordo","l": "it-IT"}]}],"q": {"c": [{"a": 1,"q": 1}],"id": 3,"t": "al","l": {"lon": 11.11938,"lat": 46.09143,"zoom": 17},"p": [{"t": "At Via Romano Guardini, you:","l": "en-US"}, {"t": "In Via Romano Guardini:","l": "it-IT"}]}}]
ap
[{"a": [], "q": {"c": [], "id": 1, "t": "ap", "p": []}}]
*/