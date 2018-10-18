package it.unitn.disi.witmee.sensorlog.runnables;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.Task;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.AirplaneModeBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.ApplicationBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.CellInfoBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.system.ST;
import it.unitn.disi.witmee.sensorlog.services.ActivityRecognitionReceiverService;

/**
 * Class that implements a {@link Runnable} that manages the data collection of the {@link it.unitn.disi.witmee.sensorlog.model.sensors.MV} event. The data
 * collection occurs through a {@link ActivityRecognitionReceiverService} that leverages on Google's {@link ActivityRecognition}. It is registered/unregistered in this
 * class and the data of type {@link it.unitn.disi.witmee.sensorlog.model.sensors.MV} event is generated in it.
 * @author Mattia Zeni
 */
public class MovementActivityRunnable implements Runnable {

    private volatile boolean isStopped = false;
    private static int SENSOR_ID = iLogApplication.MOVEMENT_ACTIVITY_ID;

    public static PendingIntent pendingIntent = null;
    public static GoogleApiClient mGoogleApiClient = null;
    public static ActivityRecognitionClient activityRecognitionClient = null;

    /**
     * Method that starts the collection of the {@link it.unitn.disi.witmee.sensorlog.model.system.AM} events. It performs the following operations:
     * <ul>
     *     <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *     <li>Starts the collection process by calling the method {@link #start()}</li>
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
     *     <li>Removes activity updates from {@link #pendingIntent} using the {@link ActivityRecognitionClient#removeActivityUpdates(PendingIntent)}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Sets this runnable as stopped</li>
     * </ul>
     */
    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped()) {
                if(mGoogleApiClient!=null && pendingIntent!=null) {
                    activityRecognitionClient.removeActivityUpdates(pendingIntent);
                }
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
     *     <li>Removes activity updates from {@link #pendingIntent} using the {@link ActivityRecognitionClient#removeActivityUpdates(PendingIntent)}</li>
     *     <li>Starts the collection process by calling the method {@link #start()}</li>
     * </ul>
     */
    public void restart() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped() && iLogApplication.hasSinglePermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                if(activityRecognitionClient!=null && pendingIntent!=null) {
                    activityRecognitionClient.removeActivityUpdates(pendingIntent);
                }

                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);

                start();
            }
        }
    }

    /**
     * Method that starts the process of collecting activity updates using the method {@link ActivityRecognitionClient#removeActivityUpdates(PendingIntent)}. The updates
     * are requested every 5000 milliseconds, however, the updates are delivered to {@link ActivityRecognitionReceiverService} as soon as they are available.
     * TODO - put the value for the time interval 30000 in a SharedPreferences with the other and remove it as hardcoded in this function
     */
    public void start() {
        mGoogleApiClient = new GoogleApiClient.Builder(iLogApplication.getAppContext())
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                    /**
                     * Callback triggered when the {@link GoogleApiClient} is ready, this is where we request the activity updates
                     * @param bundle
                     */
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Intent intent = new Intent(iLogApplication.getAppContext(), ActivityRecognitionReceiverService.class );
                        pendingIntent = PendingIntent.getService(iLogApplication.getAppContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        activityRecognitionClient = ActivityRecognition.getClient(iLogApplication.getAppContext());
                        Task task = activityRecognitionClient.requestActivityUpdates(30000, pendingIntent);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .build();

        mGoogleApiClient.connect();
    }
}
