package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.system.MU;
import it.unitn.disi.witmee.sensorlog.model.system.RM;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link RM} event
 */
public class RingModeBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. The intent we are interested in is {@link AudioManager#RINGER_MODE_CHANGED_ACTION}. Depending on the state,
     * we generate an event with {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            Log.d(this.toString(), audioManager.getRingerMode()+"");
            switch(audioManager.getRingerMode()) {
                case AudioManager.RINGER_MODE_SILENT:
                    iLogApplication.persistInMemoryEvent(new RM(System.currentTimeMillis(), RM.MODE_SILENT));
                    break;
                case AudioManager.RINGER_MODE_NORMAL:
                    iLogApplication.persistInMemoryEvent(new RM(System.currentTimeMillis(), RM.MODE_NORMAL));
                    break;
            }
        }
    }
}
