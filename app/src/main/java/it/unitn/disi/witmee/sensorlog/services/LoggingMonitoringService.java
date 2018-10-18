package it.unitn.disi.witmee.sensorlog.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.model.system.ST;
import it.unitn.disi.witmee.sensorlog.utils.Utils;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.BluetoothStatusBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.LocationStatusBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.TickBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.WIFIStatusBroadcastReceiver;


/**
 * Class that extends {@link Service} and is the main service run as Foreground Service that is used to keep the application alive in the background.
 */
public class LoggingMonitoringService extends Service {

    BluetoothStatusBroadcastReceiver bluetoothStatusBroadcastReceiver = null;
    LocationStatusBroadcastReceiver locationStatusBroadcastReceiver = null;
    WIFIStatusBroadcastReceiver wifiStatusBroadcastReceiver = null;
    TickBroadcastReceiver notificationReceiver = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * onCreate() method called when the Service is created. In it we initialize some {@link android.content.BroadcastReceiver} that cannot be initialized in the manifest.
     * They detect status changed in the Bluetooth and Wi-Fi network interfaces plus others. Additionally, it persists a {@link ST} event for service started
     */
    @Override
    public void onCreate() {
        Log.d(this.getClass().getSimpleName(), "service created");

        bluetoothStatusBroadcastReceiver = new BluetoothStatusBroadcastReceiver();
        locationStatusBroadcastReceiver = new LocationStatusBroadcastReceiver();
        wifiStatusBroadcastReceiver = new WIFIStatusBroadcastReceiver();
        notificationReceiver = new TickBroadcastReceiver();

        registerReceiver(notificationReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        registerReceiver(bluetoothStatusBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(locationStatusBroadcastReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        registerReceiver(wifiStatusBroadcastReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        registerReceiver(wifiStatusBroadcastReceiver, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
        //System events

        iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STARTED, this.getClass().getSimpleName()));

        super.onCreate();
    }

    /**
     * Method executed when the Service starts, after the {@link #onCreate()}. When the service starts we show the main notification using {@link iLogApplication#mainBuilder}
     * and conenct the notification with the {@link Service#startForegroundService(Intent)} method that is used to let the application run in the background
     * without limitations.
     * @param intent -
     * @param flags -
     * @param startId -
     * @return It is also important to return {@link Service#START_STICKY} because it tells Android to restart the service if it gets killed due to low resources
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(this.getClass().getSimpleName(), "service started.");

        iLogApplication.notificationManager.notify(iLogApplication.sharedPreferences.getInt(Utils.CONFIG_MAINNOTIFICATIONID, 0), iLogApplication.mainBuilder.build());
        startForeground(iLogApplication.sharedPreferences.getInt(Utils.CONFIG_MAINNOTIFICATIONID, 0), iLogApplication.mainBuilder.build());

        iLogApplication.isMonitoringServiceRunning = true;

        //Verify if the user feedback elements needs to be updated because expired
        iLogApplication.verifyTimeDiaries();
        iLogApplication.verifyTasks();
        iLogApplication.verifyChallenges();
        iLogApplication.verifyMessages();

        return START_STICKY;
    }

    /**
     * When the service get destroyed (because stopped, for example) it unregister the {@link android.content.BroadcastReceiver} that it registered in the {@link Service#onCreate()} method.
     * It then persists in memory a {@link ST} and cancels all the notifications, since the application is not running anymore.
     */
    @Override
    public void onDestroy() {
        Log.d(this.getClass().getSimpleName(), "stopping service");

        try {
            if(bluetoothStatusBroadcastReceiver !=null) {
                unregisterReceiver(bluetoothStatusBroadcastReceiver);
            }
            if(locationStatusBroadcastReceiver !=null) {
                unregisterReceiver(locationStatusBroadcastReceiver);
            }
            if(wifiStatusBroadcastReceiver !=null) {
                unregisterReceiver(wifiStatusBroadcastReceiver);
            }
            if(notificationReceiver!=null) {
                unregisterReceiver(notificationReceiver);
            }
        } catch(Exception e) {
        }

        iLogApplication.isMonitoringServiceRunning = false;

        iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STOPPED, this.getClass().getSimpleName()));

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(iLogApplication.sharedPreferences.getInt(Utils.CONFIG_MAINNOTIFICATIONID, 0));
        manager.cancel(Utils.TIMEDIARIESNOTIFICATIONID);
        manager.cancel(Utils.TASKSNOTIFICATIONID);
        manager.cancel(Utils.MESSAGENOTIFICATIONID);

        stopForeground(true);

        if(!iLogApplication.stopping) {
            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent i = new Intent(iLogApplication.getAppContext(), LoggingMonitoringService.class);
                iLogApplication.getAppContext().startForegroundService(i);
            }
            else {
                Intent i = new Intent(iLogApplication.getAppContext(), LoggingMonitoringService.class);
                iLogApplication.getAppContext().startService(i);
            }
            iLogApplication.stopping = false;
        }

        super.onDestroy();
    }
}
