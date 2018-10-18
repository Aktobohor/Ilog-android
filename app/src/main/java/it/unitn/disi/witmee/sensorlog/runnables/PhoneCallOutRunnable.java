package it.unitn.disi.witmee.sensorlog.runnables;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.PhoneCallInBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.PhoneCallOutBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.system.ST;
import it.unitn.disi.witmee.sensorlog.model.virtual.CI;

/**
 * Class that implements a {@link Runnable} that manages the data collection of the {@link it.unitn.disi.witmee.sensorlog.model.virtual.CO} event. The data collection occurs through a
 * {@link PhoneCallOutBroadcastReceiver}. It is registered/unregistered in this class and the data of type {@link it.unitn.disi.witmee.sensorlog.model.virtual.CO} event is generated in it.
 * TODO - need to check with Android greater then 8.0 if this still works
 * @author Mattia Zeni
 */
public class PhoneCallOutRunnable implements Runnable {

    private volatile boolean isStopped = false;
    private static PhoneCallOutBroadcastReceiver phoneCallOutBroadcastReceiver = null;
    private static int SENSOR_ID = iLogApplication.PHONECALL_OUT_ID;

    /**
     * Method that starts the collection of the {@link CI} events. It performs the following operations:
     * <ul>
     *     <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *     <li>Initializes the {@link #phoneCallOutBroadcastReceiver} variable where the detection of the mode change will occur</li>
     *     <li>Registers the receiver {@link #phoneCallOutBroadcastReceiver} using the {@link android.content.Context#registerReceiver(BroadcastReceiver, IntentFilter)}
     *         method for actions of type {@link Intent#ACTION_NEW_OUTGOING_CALL} and {@link TelephonyManager#ACTION_PHONE_STATE_CHANGED}</li>
     * </ul>
     */
    public void run() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            isStopped = false;
            if(!iLogApplication.sensorLoggingState.get(SENSOR_ID) && iLogApplication.hasSinglePermission(Manifest.permission.PROCESS_OUTGOING_CALLS)) {

                iLogApplication.startLoggingMonitoringService();
                Log.d(this.getClass().getSimpleName(), "Start");
                iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), true));
                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
                iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STARTED, this.getClass().getSimpleName()));

                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
                filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                phoneCallOutBroadcastReceiver = new PhoneCallOutBroadcastReceiver();
                iLogApplication.getAppContext().registerReceiver(phoneCallOutBroadcastReceiver, filter);
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
     * Method that stops the collection of the {@link CI} events. It performs the following operations:
     * <ul>
     *     <li>Unregisters the {@link #phoneCallOutBroadcastReceiver} using the {@link android.content.Context#unregisterReceiver(BroadcastReceiver)} method to stop receiving updates</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Sets this runnable as stopped</li>
     * </ul>
     */
    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped && phoneCallOutBroadcastReceiver!=null) {
                try {
                    iLogApplication.getAppContext().unregisterReceiver(phoneCallOutBroadcastReceiver);
                } catch (Exception e) {

                }
            }
            phoneCallOutBroadcastReceiver=null;

            iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), false));
            iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STOPPED, this.getClass().getSimpleName()));
            iLogApplication.sensorLoggingState.put(SENSOR_ID, false);
            Log.d(this.getClass().getSimpleName(), "Stop");

            setStopped(true);
        }
    }

    /**
     * Method that restarts the collection of the {@link CI} events. It performs the following operations:
     * <ul>
     *     <li>Unregister the {@link #phoneCallOutBroadcastReceiver} using the {@link android.content.Context#unregisterReceiver(BroadcastReceiver)} method to stop receiving updates</li>
     *     <li>Registers the receiver {@link #phoneCallOutBroadcastReceiver} using the {@link android.content.Context#registerReceiver(BroadcastReceiver, IntentFilter)}
     *     method for actions of type {@link Intent#ACTION_NEW_OUTGOING_CALL} and {@link TelephonyManager#ACTION_PHONE_STATE_CHANGED}</li>
     * </ul>
     */
    public void restart() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped() && iLogApplication.hasSinglePermission(Manifest.permission.PROCESS_OUTGOING_CALLS)) {
                if(phoneCallOutBroadcastReceiver!=null) {
                    try {
                        iLogApplication.getAppContext().unregisterReceiver(phoneCallOutBroadcastReceiver);
                    } catch (Exception e) {

                    }
                }

                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);

                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
                filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                phoneCallOutBroadcastReceiver = new PhoneCallOutBroadcastReceiver();
                iLogApplication.getAppContext().registerReceiver(phoneCallOutBroadcastReceiver, filter);
            }
        }
    }
}
