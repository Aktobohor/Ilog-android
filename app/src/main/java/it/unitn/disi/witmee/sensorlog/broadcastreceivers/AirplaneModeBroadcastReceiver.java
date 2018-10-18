package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.system.AM;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link AM} event
 */
public class AirplaneModeBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. The intent we are interested in is {@link Intent#ACTION_AIRPLANE_MODE_CHANGED}. Depending on the state,
     * we generate an event with {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            if(intent.getBooleanExtra("state", false)) {
                iLogApplication.persistInMemoryEvent(new AM(System.currentTimeMillis(), intent.getBooleanExtra("state", false)));
            }
            else {
                iLogApplication.persistInMemoryEvent(new AM(System.currentTimeMillis(), intent.getBooleanExtra("state", false)));
            }
        }
    }
}
