package it.unitn.disi.witmee.sensorlog.runnables;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.AirplaneModeBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.UserPresentBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.system.ST;

/**
 * Class that implements a {@link Runnable} that manages the detection of the user presence. This is difference from screen on/off, is more sofisticated and detects
 * if the user is actuallly using the smarpthone. For example, if the phone receives a notification, the screen turns on but maybe the user is not reading it. The intention
 * when this class was created was to be able to detect the user presence on request, and not on change.
 * TODO - at the moment we are not storing this information, we need to create a dedicated object in the model and modify the backend accordingly, if needed
 * @author Mattia Zeni
 */
public class UserPresentRunnable implements Runnable {

    private volatile boolean isStopped = false;
    private static UserPresentBroadcastReceiver userPresentBroadcastReceiver = null;
    private static int SENSOR_ID = iLogApplication.USER_PRESENT_ID;

    /**
     * Method that starts the detection of the user presence. It performs the following operations:
     * <ul>
     *     <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *     <li>Initializes the {@link #userPresentBroadcastReceiver} variable where the detection of the mode change will occur</li>
     *     <li>Registers the receiver {@link #userPresentBroadcastReceiver} using the {@link android.content.Context#registerReceiver(BroadcastReceiver, IntentFilter)}
     *         method for actions of type {@link Intent#ACTION_USER_PRESENT}</li>
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

                userPresentBroadcastReceiver = new UserPresentBroadcastReceiver();
                iLogApplication.getAppContext().registerReceiver(userPresentBroadcastReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
            }
        }
    }

    /**
     * Contains information about the status of the data collection for this event
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
     * Method that stops the detection of the user presence events. It performs the following operations:
     * <ul>
     *     <li>Unregisters the {@link #userPresentBroadcastReceiver} using the {@link android.content.Context#unregisterReceiver(BroadcastReceiver)} method to stop receiving updates</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Sets this runnable as stopped</li>
     * </ul>
     */
    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped() && userPresentBroadcastReceiver!=null) {
                try {
                    iLogApplication.getAppContext().unregisterReceiver(userPresentBroadcastReceiver);
                } catch (Exception e) {

                }
            }
            userPresentBroadcastReceiver=null;

            iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), false));
            iLogApplication.sensorLoggingState.put(SENSOR_ID, false);
            Log.d(this.getClass().getSimpleName(), "Stop");

            setStopped(true);
        }
    }

    /**
     * Method that restarts the detection of the user presence events. It performs the following operations:
     * <ul>
     *     <li>Unregister the {@link #userPresentBroadcastReceiver} using the {@link android.content.Context#unregisterReceiver(BroadcastReceiver)} method to stop receiving updates</li>
     *     <li>Registers the receiver {@link #userPresentBroadcastReceiver} using the {@link android.content.Context#registerReceiver(BroadcastReceiver, IntentFilter)}
     *     method for actions of type {@link Intent#ACTION_USER_PRESENT}</li>
     * </ul>
     */
    public void restart() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped()) {
                if(!isStopped() && userPresentBroadcastReceiver!=null) {
                    try {
                        iLogApplication.getAppContext().unregisterReceiver(userPresentBroadcastReceiver);
                    } catch (Exception e) {

                    }
                }

                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);

                userPresentBroadcastReceiver = new UserPresentBroadcastReceiver();
                iLogApplication.getAppContext().registerReceiver(userPresentBroadcastReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
            }
        }
    }
}
