package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AC;
import it.unitn.disi.witmee.sensorlog.model.sensors.AT;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.sensors.GR;
import it.unitn.disi.witmee.sensorlog.model.sensors.GY;
import it.unitn.disi.witmee.sensorlog.model.sensors.LA;
import it.unitn.disi.witmee.sensorlog.model.sensors.LI;
import it.unitn.disi.witmee.sensorlog.model.sensors.MF;
import it.unitn.disi.witmee.sensorlog.model.sensors.OR;
import it.unitn.disi.witmee.sensorlog.model.sensors.PE;
import it.unitn.disi.witmee.sensorlog.model.sensors.PO;
import it.unitn.disi.witmee.sensorlog.model.sensors.RH;
import it.unitn.disi.witmee.sensorlog.model.sensors.RV;
import it.unitn.disi.witmee.sensorlog.model.system.AM;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * {@link BroadcastReceiver} used to persist in memory all the events of type sensor
 */
public class SensorListener implements SensorEventListener {

    /**
     * Method called when the {@link Intent} is received. This method is called at a very high frequency so every operation performed in it must be efficient
     * @param event {@link SensorEvent} event that needs to be persisted
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        AbstractSensorEvent sensorEvent = null;
        long timeInMillis = getEventTimestampInMills(event);
        int type = event.sensor.getType();
        switch (type) {
            //motion sensors
            case Sensor.TYPE_ACCELEROMETER:
                sensorEvent = new AC(timeInMillis, event.accuracy, event.values[0], event.values[1], event.values[2]);
                break;
            case Sensor.TYPE_GRAVITY:
                sensorEvent = new GR(timeInMillis, event.accuracy, event.values[0], event.values[1], event.values[2]);
                break;
            case Sensor.TYPE_GYROSCOPE:
                sensorEvent = new GY(timeInMillis, event.accuracy, event.values[0], event.values[1], event.values[2]);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                sensorEvent = new LA(timeInMillis, event.accuracy, event.values[0], event.values[1], event.values[2]);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                sensorEvent = new RV(timeInMillis, event.accuracy, event.values[0], event.values[1], event.values[2], (event.values.length > 3 ? event.values[3] : null));
                break;
//            position sensors
            case Sensor.TYPE_MAGNETIC_FIELD:
                sensorEvent = new MF(timeInMillis, event.accuracy, event.values[0], event.values[1], event.values[2]);
                break;
            case Sensor.TYPE_ORIENTATION:
                sensorEvent = new OR(timeInMillis, event.accuracy, event.values[0], event.values[1], event.values[2]);
                break;
            case Sensor.TYPE_PROXIMITY:
                sensorEvent = new PO(timeInMillis, event.accuracy, event.values[0]);
                break;
//              environment
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                sensorEvent = new AT(timeInMillis, event.accuracy, event.values[0]);
                break;
            case Sensor.TYPE_LIGHT:
                sensorEvent = new LI(timeInMillis, event.accuracy, event.values[0]);
                break;
            case Sensor.TYPE_PRESSURE:
                sensorEvent = new PE(timeInMillis, event.accuracy, event.values[0]);
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                sensorEvent = new RH(timeInMillis, event.accuracy, event.values[0]);
                break;
        }
        iLogApplication.persistInMemoryEvent(sensorEvent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Method to retrieve the timestamp of the event, either the current one or calculated if batching (bug in Android Sensor framework, there is no easy way to fix this, check the references).
     * @param event {@link SensorEvent} event
     * @return long representing the milliseconds in epoch format
     * @see <a href="https://issuetracker.google.com/issues/36972829">https://issuetracker.google.com/issues/36972829</a> and <a href="https://stackoverflow.com/questions/4691097/what-is-android-accelerometer-min-and-max-range">https://stackoverflow.com/questions/4691097/what-is-android-accelerometer-min-and-max-range</a>
     */
    protected long getEventTimestampInMills(SensorEvent event) {
        if(iLogApplication.isSensorBatchingSupported && Utils.BATCH_LATENCY != 0) {
            long timestamp = (event.timestamp / iLogApplication.batchSensingDivisors.get(event.sensor.getType())) + iLogApplication.batchSensingOffsets.get(event.sensor.getType());
            return timestamp;
        }
        else {
            return System.currentTimeMillis();
        }
    }
}

