package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.system.AM;
import it.unitn.disi.witmee.sensorlog.model.system.MU;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link MU} event
 */
public class MusicBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. The intent we are interested in is "com.android.music.playstatechanged". Depending on the state,
     * we generate an event with {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals("com.android.music.playstatechanged")) {
            iLogApplication.persistInMemoryEvent(new MU(System.currentTimeMillis(), intent.getBooleanExtra("playing", false)));
        }
    }
}