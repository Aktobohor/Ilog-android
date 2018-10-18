package it.unitn.disi.witmee.sensorlog.runnables;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.AirplaneModeBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.ApplicationBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.system.ST;

/**
 * Class that implements a {@link Runnable} that manages the data collection of the {@link it.unitn.disi.witmee.sensorlog.model.virtual.AP} event. The data
 * collection occurs through a {@link ApplicationBroadcastReceiver}. It is registered/unregistered in this class and the data of type
 * {@link it.unitn.disi.witmee.sensorlog.model.virtual.AP} event is generated in it. With respect to other Runnables in the application, this one does not leverage
 * on the system {@link android.content.BroadcastReceiver} that broadcast an event whenever it occurs (like {@link AirplaneModeRunnable}). For this reason we need an
 * {@link AlarmManager} in combination with a {@link PendingIntent}, {@link #pendingIntent} that schedules the operation at fixed time intervals.
 */
public class ApplicationsRunnable implements Runnable {

    private volatile boolean isStopped = false;
    private static int SENSOR_ID = iLogApplication.APP_USAGE_ID;

    public static AlarmManager alarmManager;
    public static PendingIntent pendingIntent = null;

    /**
     * Method that starts the collection of the {@link it.unitn.disi.witmee.sensorlog.model.system.AM} events. It performs the following operations:
     * <ul>
     *     <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *     <li>Initializes a {@link #pendingIntent} that runs the {@link ApplicationBroadcastReceiver} class</li>
     *     <li>Starts the collection process by calling the method {@link #start()}</li>
     * </ul>
     */
    public void run() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            isStopped = false;

            if(!iLogApplication.sensorLoggingState.get(SENSOR_ID) && iLogApplication.hasUsageStatsPermission()) {

                iLogApplication.startLoggingMonitoringService();
                Log.d(this.getClass().getSimpleName(), "Start");

                iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), true));
                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
                iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STARTED, this.getClass().getSimpleName()));

                pendingIntent = PendingIntent.getBroadcast(
                        iLogApplication.getAppContext(),
                        0, // id, optional
                        new Intent(iLogApplication.getAppContext(), ApplicationBroadcastReceiver.class), // intent to launch
                        PendingIntent.FLAG_CANCEL_CURRENT);

                start();
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
     *     <li>Cancels the {@link #pendingIntent} from the {@link #alarmManager}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Sets this runnable as stopped</li>
     * </ul>
     */
    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
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
     * Method that restarts the collection of the {@link it.unitn.disi.witmee.sensorlog.model.system.AM} events. It performs the following operations:
     * <ul>
     *     <li>Cancels the {@link #pendingIntent} from the {@link #alarmManager}</li>
     *     <li>Starts the collection process by calling the method {@link #start()}</li>
     * </ul>
     */
    public void restart() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped() && iLogApplication.hasUsageStatsPermission()) {
                if(alarmManager!=null && pendingIntent!=null) {
                    alarmManager.cancel(pendingIntent);
                }

                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
                start();
            }
        }
    }

    /**
     * Method that sets the {@link #alarmManager} to execute the {@link #pendingIntent} that runs the {@link ApplicationBroadcastReceiver} class.
     */
    public void start() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if (pendingIntent != null) {
                alarmManager = (AlarmManager) iLogApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
            }
        }
    }
}
