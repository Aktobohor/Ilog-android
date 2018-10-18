package it.unitn.disi.witmee.sensorlog.runnables;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.SensorListener;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.system.ST;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Class that implements a {@link Runnable} that manages the data collection of the {@link it.unitn.disi.witmee.sensorlog.model.sensors.LA} event. The data
 * collection occurs through the {@link SensorListener} class.
 * They are registered/unregistered in this class and the data of type {@link it.unitn.disi.witmee.sensorlog.model.sensors.LA} and  event is generated in them.
 * It instantiates a {@link SensorManager} object that request sensor updates through the method {@link SensorManager#registerListener} on the {@link SensorListener}
 * {@link #sensorListener}.
 * @author Mattia Zeni
 */
public class SensorLinearAccelerometerRunnable implements Runnable {

    private volatile boolean isStopped = false;
    private SensorManager mSensorManager = null;
    private SensorListener sensorListener = null;
    private static int SENSOR_ID = Sensor.TYPE_LINEAR_ACCELERATION;
    private Sensor sensor = null;
    private HandlerThread mSensorThread;
    private Handler mSensorHandler;

    /**
     * Method that starts the collection of the {@link it.unitn.disi.witmee.sensorlog.model.sensors.LA} events. It performs the following operations:
     * <ul>
     *     <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *     <li>Initializes the {@link #mSensorManager}</li>
     *     <li>Registers the listener {@link #sensorListener} using the {@link SensorManager#registerListener(SensorEventListener, Sensor, int)}</li>
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

                mSensorManager = (SensorManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
                sensorListener = new SensorListener();
                sensor = mSensorManager.getDefaultSensor(SENSOR_ID);

                mSensorThread = new HandlerThread("Sensor thread", Thread.MAX_PRIORITY);
                mSensorThread.start();
                mSensorHandler = new Handler(mSensorThread.getLooper()); //Blocks until looper is prepared, which is fairly quick
                mSensorManager.registerListener(sensorListener, sensor, iLogApplication.getMinSampleRateForSensorId(SENSOR_ID), Utils.BATCH_LATENCY, mSensorHandler);
            }
        }
    }


    /**
     * Contains information about the status of the data collection for this {@link it.unitn.disi.witmee.sensorlog.model.sensors.LA} event
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
     * Method that stops the collection of the {@link it.unitn.disi.witmee.sensorlog.model.sensors.LA} events. It performs the following operations:
     * <ul>
     *     <li>Removes the updates by calling {@link SensorManager#unregisterListener(SensorEventListener)} on {@link #sensorListener}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Sets this runnable as stopped</li>
     * </ul>
     */
    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {

            if(!isStopped() && sensor!=null && sensorListener!=null && mSensorManager!=null) {
                try {
                    mSensorManager.unregisterListener(sensorListener, sensor);
                } catch (Exception e) {

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
     * Method that stops the collection of the {@link it.unitn.disi.witmee.sensorlog.model.sensors.LA} events. It performs the following operations:
     * <ul>
     *     <li>Removes the updates by calling {@link SensorManager#unregisterListener(SensorEventListener)} on {@link #sensorListener}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *     <li>Initializes the {@link #mSensorManager}</li>
     *     <li>Registers the listener {@link #sensorListener} using the {@link SensorManager#registerListener(SensorEventListener, Sensor, int)}</li>
     * </ul>
     */
    public void restart() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped()) {
                if(sensor!=null && sensorListener!=null && mSensorManager!=null) {
                    try {
                        mSensorManager.unregisterListener(sensorListener, sensor);
                    } catch (Exception e) {

                    }
                }

                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);

                mSensorManager = (SensorManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
                sensorListener = new SensorListener();
                sensor = mSensorManager.getDefaultSensor(SENSOR_ID);

                mSensorThread = new HandlerThread("Sensor thread", Thread.MAX_PRIORITY);
                mSensorThread.start();
                mSensorHandler = new Handler(mSensorThread.getLooper()); //Blocks until looper is prepared, which is fairly quick
                mSensorManager.registerListener(sensorListener, sensor, iLogApplication.getMinSampleRateForSensorId(SENSOR_ID), Utils.BATCH_LATENCY, mSensorHandler);
            }
        }
    }
}
