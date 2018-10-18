package it.unitn.disi.witmee.sensorlog.runnables;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.BluetoothBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.BluetoothScanBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.WIFIBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.WIFINetworksBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.social.BN;
import it.unitn.disi.witmee.sensorlog.model.system.ST;

/**
 * Class that implements a {@link Runnable} that manages the data collection of the {@link it.unitn.disi.witmee.sensorlog.model.ambience.WN} event. The data collection occurs through a
 * {@link WIFIBroadcastReceiver}. It is registered/unregistered in this class and the data of type {@link it.unitn.disi.witmee.sensorlog.model.ambience.WN} event is generated in it.
 * @author Mattia Zeni
 */
public class WIFINetworksRunnable implements Runnable {

    private volatile boolean isStopped = false;
    public static WIFIBroadcastReceiver wifiBroadcastReceiver = null;
    private static int SENSOR_ID = iLogApplication.WIFI_NETWORKS_SENSOR_ID;

    public static WifiManager mWifiManager;
    public static AlarmManager alarmManager;
    public static PendingIntent pendingIntent = null;

    /**
     * Method that starts the collection of the {@link BN} events. It performs the following operations:
     * <ul>
     *     <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *     <li>Initializes the {@link #wifiBroadcastReceiver} variable where the detection of the mode change will occur</li>
     *     <li>Registers the receiver {@link #wifiBroadcastReceiver} using the {@link android.content.Context#registerReceiver(BroadcastReceiver, IntentFilter)}
     *         method for actions of type {@link WifiManager#SCAN_RESULTS_AVAILABLE_ACTION}.</li>
     *     <li>Initializes a {@link #pendingIntent} that runs the {@link WIFINetworksBroadcastReceiver} class that starts the scanning process at fixed time intervals.</li>
     * </ul>
     */
    public void run() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            isStopped = false;

            if(!iLogApplication.sensorLoggingState.get(SENSOR_ID)) {

                iLogApplication.startLoggingMonitoringService();
                Log.d(this.getClass().getSimpleName(), "Start");

                iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), true));
                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
                iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STARTED, this.getClass().getSimpleName()));

                mWifiManager = (WifiManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiBroadcastReceiver = new WIFIBroadcastReceiver();
                iLogApplication.getAppContext().registerReceiver(wifiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                pendingIntent = PendingIntent.getBroadcast(
                        iLogApplication.getAppContext(),
                        0, // id, optional
                        new Intent(iLogApplication.getAppContext(), WIFINetworksBroadcastReceiver.class), // intent to launch
                        PendingIntent.FLAG_CANCEL_CURRENT);

                start();
            }
        }
    }

    /**
     * Contains information about the status of the data collection for this {@link BN} event
     * @return true if the data collection is stopped, false otherwise
     */
    public boolean isStopped() {
        return isStopped;
    }

    /**
     * Method that updates the status of the Runnable
     * @param isStop boolean value that identifies the status, true if the data collection is stopped, false otherwise
     */
    private void setStopped(boolean isStop) {
        if (isStopped != isStop)
            isStopped = isStop;

        iLogApplication.stopLoggingMonitoringService();
    }

    /**
     * Method that stops the collection of the {@link BN} events. It performs the following operations:
     * <ul>
     *     <li>Unregister the {@link #wifiBroadcastReceiver} using the {@link android.content.Context#unregisterReceiver(BroadcastReceiver)} method to stop receiving scan results</li>
     *     <li>Cancels the {@link #pendingIntent} from the {@link #alarmManager}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Sets this runnable as stopped</li>
     * </ul>
     */
    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(wifiBroadcastReceiver!=null) {
                try {
                    iLogApplication.getAppContext().unregisterReceiver(wifiBroadcastReceiver);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(alarmManager!=null && pendingIntent!=null) {
                alarmManager.cancel(pendingIntent);
            }

            iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), false));
            iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STOPPED, this.getClass().getSimpleName()));
            iLogApplication.sensorLoggingState.put(SENSOR_ID, false);
            Log.d(this.getClass().getSimpleName(), "Stop");

            setStopped(true);
        }
    }

    /**
     * Method that restarts the collection of the {@link BN} events. It performs the following operations:
     * <ul>
     *     <li>Unregister the {@link #wifiBroadcastReceiver} using the {@link android.content.Context#unregisterReceiver(BroadcastReceiver)} method to stop receiving updates</li>
     *     <li>Registers the receiver {@link #wifiBroadcastReceiver} using the {@link android.content.Context#registerReceiver(BroadcastReceiver, IntentFilter)}
     *     method for actions of type {@link WifiManager#SCAN_RESULTS_AVAILABLE_ACTION}</li>
     *     <li>Cancels the {@link #pendingIntent} from the {@link #alarmManager}</li>
     *     <li>Starts the collection process by calling the method {@link #start()}</li>
     * </ul>
     */
    public void restart() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped()) {
                if(wifiBroadcastReceiver!=null) {
                    try {
                        iLogApplication.getAppContext().unregisterReceiver(wifiBroadcastReceiver);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(alarmManager!=null && pendingIntent!=null) {
                    alarmManager.cancel(pendingIntent);
                }

                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
                mWifiManager = (WifiManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifiBroadcastReceiver = new WIFIBroadcastReceiver();
                iLogApplication.getAppContext().registerReceiver(wifiBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                start();
            }
        }
    }

    /**
     * Method that sets the {@link #alarmManager} to execute the {@link #pendingIntent} that runs the {@link WIFINetworksBroadcastReceiver} class.
     */
    public void start() {
        if(pendingIntent!=null) {
            alarmManager = (AlarmManager) iLogApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        }
    }
}
