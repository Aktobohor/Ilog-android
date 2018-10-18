package it.unitn.disi.witmee.sensorlog.runnables;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.ApplicationBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.AudioRemoveBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.AudioRequestBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.BluetoothLEScanRemoveBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.BluetoothLEScanRequestBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.model.audio.AU;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.social.BL;
import it.unitn.disi.witmee.sensorlog.model.system.ST;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Class that implements a {@link Runnable} that manages the data collection of the {@link BL} event. The data
 * collection occurs through the {@link ScanCallback}. It is registered/unregistered in this class and the data of type
 * {@link BL} event is generated in it. With respect to other Runnables in the application, this one does not leverage
 * on the system {@link android.content.BroadcastReceiver} that broadcast an event whenever it occurs (like {@link AirplaneModeRunnable}). For this reason we need an
 * {@link AlarmManager} in combination with two {@link PendingIntent}, {@link #pendingIntentRequest} and {@link #pendingIntentRemove} that schedule the operation at fixed time intervals.
 */
public class BluetoothLERunnable implements Runnable {

    private volatile boolean isStopped = false;
    private static int SENSOR_ID = iLogApplication.BLUETOOTHLELOGGING_ID;

    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothLeScanner myScanner;
    public static ScanSettings settings;

    private static String BOND_DONE = "BOND_NONE";
    private static String BOND_BONDING = "BOND_BONDING";
    private static String BOND_BONDED = "BOND_BONDED";

    public static AlarmManager alarmManager;
    public static PendingIntent pendingIntentRemove = null;
    public static PendingIntent pendingIntentRequest = null;

    /**
     * Method that starts the collection of the {@link AU} events. It performs the following operations:
     * <ul>
     *     <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *     <li>Initializes the {@link #pendingIntentRequest} that runs the {@link BluetoothLEScanRequestBroadcastReceiver} class</li>
     *     <li>Initializes the {@link #pendingIntentRemove} that runs the {@link BluetoothLEScanRemoveBroadcastReceiver} class</li>
     *     <li>Starts the collection process by calling the method {@link #startRequest()}</li>
     * </ul>
     */
    public void run() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            isStopped = false;

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                if(mBluetoothAdapter.isEnabled() && iLogApplication.getAppContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    if(!iLogApplication.sensorLoggingState.get(SENSOR_ID)) {
                        iLogApplication.startLoggingMonitoringService();
                        Log.d(this.getClass().getSimpleName(), "Start");

                        iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), true));
                        iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
                        iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STARTED, this.getClass().getSimpleName()));

                        myScanner = mBluetoothAdapter.getBluetoothLeScanner();
                        settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();

                        pendingIntentRequest = PendingIntent.getBroadcast(
                                iLogApplication.getAppContext(),
                                0, // id, optional
                                new Intent(iLogApplication.getAppContext(), BluetoothLEScanRequestBroadcastReceiver.class), // intent to launch
                                PendingIntent.FLAG_CANCEL_CURRENT);

                        pendingIntentRemove = PendingIntent.getBroadcast(
                                iLogApplication.getAppContext(),
                                0, // id, optional
                                new Intent(iLogApplication.getAppContext(), BluetoothLEScanRemoveBroadcastReceiver.class), // intent to launch
                                PendingIntent.FLAG_CANCEL_CURRENT);

                        if (myScanner != null) {
                            startRequest();
                        }
                    }
                }
            }
        }
    }

    /**
     * Contains information about the status of the data collection for this {@link it.unitn.disi.witmee.sensorlog.model.system.AM} event
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
     * Method that stops the collection of the {@link it.unitn.disi.witmee.sensorlog.model.system.AM} events. It performs the following operations:
     * <ul>
     *     <li>Flushes the pending scan results in the {@link #myScanner} object with the method {@link BluetoothLeScanner#flushPendingScanResults}</li>
     *     <li>Calls the {@link BluetoothLeScanner#stopScan} method to stop scanning for devices</li>
     *     <li>Cancels the {@link #pendingIntentRequest} from the {@link #alarmManager}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Sets this runnable as stopped</li>
     * </ul>
     */
    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(myScanner != null && callback!=null && mBluetoothAdapter!=null) {
                if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    myScanner.flushPendingScanResults(callback);
                    myScanner.stopScan(callback);
                }
            }
            if(alarmManager!=null && pendingIntentRequest!=null) {
                alarmManager.cancel(pendingIntentRequest);
            }

            iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), false));
            iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STOPPED, this.getClass().getSimpleName()));
            iLogApplication.sensorLoggingState.put(SENSOR_ID, false);
            Log.d(this.getClass().getSimpleName(), "Stop");

            setStopped(true);
        }
    }

    /**
     * Method that restarts the collection of the {@link AU} events. It performs the following operations:
     * <ul>
     *     <li>Flushes the pending scan results in the {@link #myScanner} object with the method {@link BluetoothLeScanner#flushPendingScanResults}</li>
     *     <li>Calls the {@link BluetoothLeScanner#stopScan} method to stop scanning for devices</li>
     *     <li>Cancels the {@link #pendingIntentRequest} from the {@link #alarmManager}</li>
     *     <li>Starts the collection process by calling the method {@link #startRequest()}</li>
     * </ul>
     */
    public void restart() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                if(mBluetoothAdapter.isEnabled() && iLogApplication.getAppContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    if(!isStopped()) {
                        if (myScanner != null && callback != null && mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                            myScanner.flushPendingScanResults(callback);
                            myScanner.stopScan(callback);
                        }
                        if(alarmManager!=null && pendingIntentRequest!=null) {
                            alarmManager.cancel(pendingIntentRequest);
                        }

                        iLogApplication.sensorLoggingState.put(SENSOR_ID, true);

                        if (!iLogApplication.getAppContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                            stop();
                        }

                        final BluetoothManager bluetoothManager =
                                (BluetoothManager) iLogApplication.getAppContext().getSystemService(Context.BLUETOOTH_SERVICE);
                        mBluetoothAdapter = bluetoothManager.getAdapter();

                        myScanner = mBluetoothAdapter.getBluetoothLeScanner();
                        settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();

                        if (myScanner != null) {
                            startRequest();
                        }
                    }
                }
            }
        }
    }

    /**
     * {@link ScanCallback} class that contains the method that is called by the {@link BluetoothLeScanner} whenever a result is ready
     */
    public static final ScanCallback callback = new ScanCallback() {

        /**
         * Method called when a scan result is available
         * @param callbackType integer representing the type of the callback
         * @param result {@link ScanResult} object containing the result of the scan
         */
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            String bondState = null;

            /**
             * Type of the connection of the result:
             * <ul>
             *     <li>{@link #BOND_DONE} if the smartphone is not connected to the detected device</li>
             *     <li>{@link #BOND_BONDING} if the smartphone is connecting to the detected device</li>
             *     <li>{@link #BOND_BONDED} if the smartphone is connected to the detected device</li>
             * </ul>
             */
            switch(result.getDevice().getBondState()) {
                case BluetoothDevice.BOND_NONE: bondState = BOND_DONE;
                    break;
                case BluetoothDevice.BOND_BONDING: bondState = BOND_BONDING;
                    break;
                case BluetoothDevice.BOND_BONDED: bondState = BOND_BONDED;
                    break;
            }

            long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            long timestamp = bootTime + result.getTimestampNanos()/1000000;

            BL bluetoothEvent = new BL(timestamp, result.getDevice().getName(), result.getDevice().getAddress(),
                    bondState, result.getRssi(), 0);
            //Log.d(this.toString(), bluetoothEvent.toString());
            iLogApplication.persistInMemoryEvent(bluetoothEvent);
        }
    };

    /**
     * Method that tells the {@link #alarmManager} to execute the {@link #pendingIntentRemove} that runs the {@link BluetoothLEScanRemoveBroadcastReceiver} class. This class
     * stops collecting audio from the device's microphone. The {@link #pendingIntentRemove} is executed X seconds after the {@link #pendingIntentRequest}, meaning
     * that scans for devices for X seconds. The amount of seconds is store in the {@link android.content.SharedPreferences} of the application
     * with the key {@link Utils#CONFIG_BLUETOOTHLESCANDURATION}.
     */
    public static void startRemove() {
        if(pendingIntentRemove!=null) {
            alarmManager = (AlarmManager) iLogApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + iLogApplication.sharedPreferences.getInt(Utils.CONFIG_BLUETOOTHLESCANDURATION, 0), pendingIntentRemove);
        }
    }

    /**
     * Method that tells the {@link #alarmManager} to execute the {@link #pendingIntentRequest} that runs the {@link BluetoothLEScanRequestBroadcastReceiver} class. This class
     * starts collecting audio from the device's microphone.
     */
    public static void startRequest() {
        if(pendingIntentRequest!=null) {
            alarmManager = (AlarmManager) iLogApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntentRequest);
        }
    }
}
