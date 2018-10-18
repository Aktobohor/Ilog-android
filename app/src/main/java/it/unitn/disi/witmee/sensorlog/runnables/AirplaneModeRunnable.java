package it.unitn.disi.witmee.sensorlog.runnables;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.AirplaneModeBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.system.ST;

/**
 * Class that implements a {@link Runnable} that manages the data collection of the {@link it.unitn.disi.witmee.sensorlog.model.system.AM} event. The data
 * collection occurs through a {@link AirplaneModeBroadcastReceiver}. It is registered/unregistered in this class and the data of type
 * {@link it.unitn.disi.witmee.sensorlog.model.system.AM} event is generated in it.
 * @author Mattia Zeni
 */
public class AirplaneModeRunnable implements Runnable {

    private volatile boolean isStopped = false;
    private static AirplaneModeBroadcastReceiver airplaneModeBroadcastReceiver = null;
    private static int SENSOR_ID = iLogApplication.AIRPLANE_MODE_ID;

    /**
     * Method that starts the collection of the {@link it.unitn.disi.witmee.sensorlog.model.system.AM} events. It performs the following operations:
     * <ul>
     *     <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *     <li>Initializes the {@link #airplaneModeBroadcastReceiver} variable where the detection of the mode change will occur</li>
     *     <li>Registers the receiver {@link #airplaneModeBroadcastReceiver} using the {@link android.content.Context#registerReceiver(BroadcastReceiver, IntentFilter)}
     *         method for actions of type {@link Intent#ACTION_AIRPLANE_MODE_CHANGED}</li>
     * </ul>
     */
    public void run() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            isStopped = false;

            if(!iLogApplication.sensorLoggingState.get(SENSOR_ID)) {

                iLogApplication.startLoggingMonitoringService();
                Log.d(this.getClass().getSimpleName(), "Start logging");

                iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), true));
                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
                iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STARTED, this.getClass().getSimpleName()));

                airplaneModeBroadcastReceiver = new AirplaneModeBroadcastReceiver();
                iLogApplication.getAppContext().registerReceiver(airplaneModeBroadcastReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
            }
            else {
                Log.d(this.getClass().getSimpleName(), "Already started");
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
     *     <li>Unregisters the {@link #airplaneModeBroadcastReceiver} using the {@link android.content.Context#unregisterReceiver(BroadcastReceiver)} method to stop receiving updates</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Sets this runnable as stopped</li>
     * </ul>
     */
    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped() && airplaneModeBroadcastReceiver!=null) {
                try {
                    iLogApplication.getAppContext().unregisterReceiver(airplaneModeBroadcastReceiver);
                } catch (Exception e) {

                }
            }
            airplaneModeBroadcastReceiver = null;

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
     *     <li>Unregister the {@link #airplaneModeBroadcastReceiver} using the {@link android.content.Context#unregisterReceiver(BroadcastReceiver)} method to stop receiving updates</li>
     *     <li>Registers the receiver {@link #airplaneModeBroadcastReceiver} using the {@link android.content.Context#registerReceiver(BroadcastReceiver, IntentFilter)}
     *     method for actions of type {@link Intent#ACTION_AIRPLANE_MODE_CHANGED}</li>
     * </ul>
     */
    public void restart() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped()) {
                if(airplaneModeBroadcastReceiver!=null) {
                    try {
                        iLogApplication.getAppContext().unregisterReceiver(airplaneModeBroadcastReceiver);
                    } catch (Exception e) {

                    }
                }

                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);

                airplaneModeBroadcastReceiver = new AirplaneModeBroadcastReceiver();
                iLogApplication.getAppContext().registerReceiver(airplaneModeBroadcastReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
            }
        }
    }
}
