package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;

import java.util.ArrayList;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.BatchObject;
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
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Sensor event listener used to train sensor batching. TODO - not used to a bug
 */
public class SensorListenerBatchTraining implements SensorEventListener {

    @Override
    public void onSensorChanged(SensorEvent event) {

        int type = event.sensor.getType();

        ArrayList<BatchObject> timestamps = (ArrayList<BatchObject>) iLogApplication.batchSensing.get(type);
        timestamps.add(new BatchObject(System.currentTimeMillis(), event.timestamp));
        iLogApplication.batchSensing.put(type, timestamps);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}

