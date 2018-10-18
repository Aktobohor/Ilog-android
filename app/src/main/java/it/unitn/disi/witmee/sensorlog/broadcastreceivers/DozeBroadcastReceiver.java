package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.system.AM;
import it.unitn.disi.witmee.sensorlog.model.system.DO;


/**
 * {@link BroadcastReceiver} used to persist in memory the {@link DO} event
 */
public class DozeBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. The intent we are interested in is {@link PowerManager#ACTION_DEVICE_IDLE_MODE_CHANGED}. Depending on the state,
     * we generate an event with {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if(android.os.Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
            String action = intent.getAction();
            if(action.equals(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED)) {
                if(powerManager.isDeviceIdleMode()) {
                    Log.d(context.toString(), "Doze Mode started!");

                    //A DO event is generated and at the same time the logging is stopped
                    iLogApplication.stopCheckLogging();
                    iLogApplication.persistInMemoryEvent(new DO(System.currentTimeMillis(), 0, true));
                    iLogApplication.pauseLogging();
                }
                else {
                    Log.d(context.toString(), "Doze Mode finished!");

                    //Once the phone exits doze mode we need to check if some notifications are expired and update them
                    iLogApplication.updateQuestionNotification();

                    //A DO event is generated and at the same time the logging is resumed
                    iLogApplication.checkLogging();
                    iLogApplication.persistInMemoryEvent(new DO(System.currentTimeMillis(), 0, false));
                    iLogApplication.resumeLogging();
                }
            }
        }
    }
}
