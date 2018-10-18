package it.unitn.disi.witmee.sensorlog.runnables;

import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.system.ST;

/**
 * Created by mattiazeni on 5/17/17.
 */

public class NotificationRunnable implements Runnable {

    private volatile boolean isStopped = true;
    private static int SENSOR_ID = iLogApplication.NOTIFICATION_ID;

    public void run() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            isStopped = false;

            if(!iLogApplication.sensorLoggingState.get(SENSOR_ID) && iLogApplication.hasNotificationAccessPermission()) {

                iLogApplication.startLoggingMonitoringService();
                Log.d(this.getClass().getSimpleName(), "Start");

                iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), true));
                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
                iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STARTED, this.getClass().getSimpleName()));
            }
        }
    }

    public boolean isStopped() {
        return isStopped;
    }

    private void setStopped(boolean isStop) {
        if (isStopped != isStop)
            isStopped = isStop;

        iLogApplication.stopLoggingMonitoringService();
    }

    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), false));
            iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STOPPED, this.getClass().getSimpleName()));
            iLogApplication.sensorLoggingState.put(SENSOR_ID, false);
            Log.d(this.getClass().getSimpleName(), "Stop");

            setStopped(true);
        }
    }

    public void restart() {
        if(!isStopped()  && iLogApplication.hasNotificationAccessPermission()) {

            iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
            //cheers, do nothing
        }
    }
}
