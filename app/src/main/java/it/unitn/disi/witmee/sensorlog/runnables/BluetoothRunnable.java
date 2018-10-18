package it.unitn.disi.witmee.sensorlog.runnables;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.AirplaneModeBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.ApplicationBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.BluetoothBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.BluetoothScanBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.social.BN;
import it.unitn.disi.witmee.sensorlog.model.system.ST;

/**
 * Class that implements a {@link Runnable} that manages the data collection of the {@link BN} event. The data collection occurs through a
 * {@link BluetoothBroadcastReceiver}. It is registered/unregistered in this class and the data of type {@link BN} event is generated in it.
 * @author Mattia Zeni
 */
public class BluetoothRunnable implements Runnable {

    private volatile boolean isStopped = false;
    private BluetoothBroadcastReceiver bluetoothBroadcastBroadcastReceiver = null;
    public static BluetoothAdapter mBluetoothAdapter;
    public static ArrayList<BN> bluetoothNormalEventArrayList = null;
    private static int SENSOR_ID = iLogApplication.BLUETOOTHLOGGING_ID;

    public static AlarmManager alarmManager;
    public static PendingIntent pendingIntent = null;

    /**
     * Method that starts the collection of the {@link BN} events. It performs the following operations:
     * <ul>
     *     <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *     <li>Initializes the {@link #bluetoothBroadcastBroadcastReceiver} variable where the detection of the mode change will occur</li>
     *     <li>Registers the receiver {@link #bluetoothBroadcastBroadcastReceiver} using the {@link android.content.Context#registerReceiver(BroadcastReceiver, IntentFilter)}
     *         method for actions of type {@link BluetoothDevice#ACTION_FOUND} and {@link BluetoothAdapter#ACTION_DISCOVERY_FINISHED}.</li>
     *     <li>Initializes a {@link #pendingIntent} that runs the {@link BluetoothScanBroadcastReceiver} class that starts the scanning process at fixed time intervals. There
     *         is no need to stop the logging in this case, unlike for the {@link BluetoothLERunnable}, because the scanning process for normal bluetooth as a fixed duration.</li>
     * </ul>
     */
    public void run() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            isStopped = false;

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(mBluetoothAdapter != null) {
                if (mBluetoothAdapter.isEnabled()) {
                    if(iLogApplication.sensorLoggingState != null) {
                        if(!iLogApplication.sensorLoggingState.get(SENSOR_ID) ) {

                            iLogApplication.startLoggingMonitoringService();
                            Log.d(this.getClass().getSimpleName(), "Start");

                            iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), true));
                            iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
                            iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STARTED, this.getClass().getSimpleName()));

                            bluetoothBroadcastBroadcastReceiver = new BluetoothBroadcastReceiver();
                            bluetoothNormalEventArrayList = new ArrayList<BN>();
                            IntentFilter filter = new IntentFilter();
                            filter.addAction(BluetoothDevice.ACTION_FOUND);
                            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                            iLogApplication.getAppContext().registerReceiver(bluetoothBroadcastBroadcastReceiver, filter);

                            pendingIntent = PendingIntent.getBroadcast(
                                    iLogApplication.getAppContext(),
                                    0, // id, optional
                                    new Intent(iLogApplication.getAppContext(), BluetoothScanBroadcastReceiver.class), // intent to launch
                                    PendingIntent.FLAG_CANCEL_CURRENT);

                            start();
                        }
                    }
                }
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
     *     <li>Unregister the {@link #bluetoothBroadcastBroadcastReceiver} using the {@link android.content.Context#unregisterReceiver(BroadcastReceiver)} method to stop receiving scan results</li>
     *     <li>Cancels the {@link #pendingIntent} from the {@link #alarmManager}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Sets this runnable as stopped</li>
     * </ul>
     */
    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped() && bluetoothBroadcastBroadcastReceiver !=null) {
                try {
                    iLogApplication.getAppContext().unregisterReceiver(bluetoothBroadcastBroadcastReceiver);
                } catch (Exception e) {

                }
            }
            bluetoothBroadcastBroadcastReceiver = null;

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
     *     <li>Unregister the {@link #bluetoothBroadcastBroadcastReceiver} using the {@link android.content.Context#unregisterReceiver(BroadcastReceiver)} method to stop receiving updates</li>
     *     <li>Registers the receiver {@link #bluetoothBroadcastBroadcastReceiver} using the {@link android.content.Context#registerReceiver(BroadcastReceiver, IntentFilter)}
     *     method for actions of type {@link Intent#ACTION_AIRPLANE_MODE_CHANGED}</li>
     *     <li>Cancels the {@link #pendingIntent} from the {@link #alarmManager}</li>
     *     <li>Starts the collection process by calling the method {@link #start()}</li>
     * </ul>
     */
    public void restart() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                if (!isStopped() && mBluetoothAdapter.isEnabled()) {
                    if (bluetoothBroadcastBroadcastReceiver != null) {
                        try {
                            iLogApplication.getAppContext().unregisterReceiver(bluetoothBroadcastBroadcastReceiver);
                        } catch (Exception e) {

                        }
                    }

                    iLogApplication.sensorLoggingState.put(SENSOR_ID, true);

                    bluetoothBroadcastBroadcastReceiver = null;

                    if(alarmManager!=null && pendingIntent!=null) {
                        alarmManager.cancel(pendingIntent);
                    }

                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                    if (mBluetoothAdapter != null) {
                        if (mBluetoothAdapter.isEnabled()) {
                            bluetoothBroadcastBroadcastReceiver = new BluetoothBroadcastReceiver();
                            bluetoothNormalEventArrayList = new ArrayList<BN>();
                            IntentFilter filter = new IntentFilter();
                            filter.addAction(BluetoothDevice.ACTION_FOUND);
                            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                            iLogApplication.getAppContext().registerReceiver(bluetoothBroadcastBroadcastReceiver, filter);

                            start();
                        }
                    }
                }
            }
        }
    }

    /**
     * Method that starts the scanning process to discover new bluetooth devices
     */
    public static void discoverDevices() {
        if (mBluetoothAdapter != null) {
            bluetoothNormalEventArrayList = new ArrayList<BN>();
            mBluetoothAdapter.startDiscovery();
        }
        Log.d("BT", "discovery phase");
    }

    /**
     * Method that sets the {@link #alarmManager} to execute the {@link #pendingIntent} that runs the {@link BluetoothBroadcastReceiver} class.
     */
    public static void start() {
        if(pendingIntent!=null) {
            alarmManager = (AlarmManager) iLogApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
        }
    }
}
