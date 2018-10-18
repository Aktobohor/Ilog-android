package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.system.AM;
import it.unitn.disi.witmee.sensorlog.model.system.HP;

import static android.content.ContentValues.TAG;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link HP} event
 */
public class HeadsetBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. The intent we are interested in is {@link AudioManager#ACTION_HEADSET_PLUG}. Depending on the state,
     * we generate an event with {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case 0:
                    iLogApplication.persistInMemoryEvent(new HP(System.currentTimeMillis(), true));
                    break;
                case 1:
                    iLogApplication.persistInMemoryEvent(new HP(System.currentTimeMillis(), false));
                    break;
                default:
                    Log.d(TAG, "I have no idea what the headset state is");
            }
        }
    }
}