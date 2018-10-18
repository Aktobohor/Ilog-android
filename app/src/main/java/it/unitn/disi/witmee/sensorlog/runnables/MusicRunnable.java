package it.unitn.disi.witmee.sensorlog.runnables;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.AirplaneModeBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.MusicBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.system.ST;

/**
 * Class that implements a {@link Runnable} that manages the data collection of the {@link it.unitn.disi.witmee.sensorlog.model.system.MU} event. The data
 * collection occurs through a {@link MusicBroadcastReceiver}. It is registered/unregistered in this class and the data of type
 * {@link it.unitn.disi.witmee.sensorlog.model.system.MU} event is generated in it.
 * @author Mattia Zeni
 */
public class MusicRunnable implements Runnable {

    private volatile boolean isStopped = false;
    private static MusicBroadcastReceiver musicBroadcastReceiver = null;
    private static int SENSOR_ID = iLogApplication.MUSIC_ID;

    /**
     * Method that starts the collection of the {@link it.unitn.disi.witmee.sensorlog.model.system.MU} events. It performs the following operations:
     * <ul>
     *     <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *     <li>Initializes the {@link #musicBroadcastReceiver} variable where the detection of the mode change will occur</li>
     *     <li>Registers the receiver {@link #musicBroadcastReceiver} using the {@link android.content.Context#registerReceiver(BroadcastReceiver, IntentFilter)}
     *         method for actions of type "com.android.music.playstatechanged" TODO - as of July 2018 there is no constant for this value
     *     </li>
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

                musicBroadcastReceiver = new MusicBroadcastReceiver();
                iLogApplication.getAppContext().registerReceiver(musicBroadcastReceiver, new IntentFilter("com.android.music.playstatechanged"));
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
     * Method that stops the collection of the {@link it.unitn.disi.witmee.sensorlog.model.system.MU} events. It performs the following operations:
     * <ul>
     *     <li>Unregisters the {@link #musicBroadcastReceiver} using the {@link android.content.Context#unregisterReceiver(BroadcastReceiver)} method to stop receiving updates</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Sets this runnable as stopped</li>
     * </ul>
     */
    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped() && musicBroadcastReceiver!=null) {
                try {
                    iLogApplication.getAppContext().unregisterReceiver(musicBroadcastReceiver);
                } catch (Exception e) {

                }
            }
            musicBroadcastReceiver=null;

            iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), false));
            iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STOPPED, this.getClass().getSimpleName()));
            iLogApplication.sensorLoggingState.put(SENSOR_ID, false);
            Log.d(this.getClass().getSimpleName(), "Stop");

            setStopped(true);
        }
    }

    /**
     * Method that restarts the collection of the {@link it.unitn.disi.witmee.sensorlog.model.system.MU} events. It performs the following operations:
     * <ul>
     *     <li>Unregister the {@link #musicBroadcastReceiver} using the {@link android.content.Context#unregisterReceiver(BroadcastReceiver)} method to stop receiving updates</li>
     *     <li>Registers the receiver {@link #musicBroadcastReceiver} using the {@link android.content.Context#registerReceiver(BroadcastReceiver, IntentFilter)}
     *     method for actions of type "com.android.music.playstatechanged"</li>
     * </ul>
     */
    public void restart() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped()) {
                if(!isStopped() && musicBroadcastReceiver!=null) {
                    try {
                        iLogApplication.getAppContext().unregisterReceiver(musicBroadcastReceiver);
                    } catch (Exception e) {

                    }
                }

                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);

                musicBroadcastReceiver = new MusicBroadcastReceiver();
                iLogApplication.getAppContext().registerReceiver(musicBroadcastReceiver, new IntentFilter("com.android.music.playstatechanged"));
            }
        }
    }
}
