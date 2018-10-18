package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.system.AM;
import it.unitn.disi.witmee.sensorlog.model.virtual.SC;
import it.unitn.disi.witmee.sensorlog.runnables.ScreenRunnable;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link SC} event
 */
public class ScreenBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. The intents we are interested in are {@link Intent#ACTION_SCREEN_OFF} and {@link Intent#ACTION_SCREEN_ON}. Depending on the state,
     * we generate an event with {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String strAction = intent.getAction();
        if (strAction.equals(Intent.ACTION_SCREEN_OFF) || strAction.equals(Intent.ACTION_SCREEN_ON)) {
            if(strAction.equals(Intent.ACTION_SCREEN_OFF)) {
                iLogApplication.isUserPresent = false;
                Log.d(this.toString(), "SCREEN OFF");
                if(ScreenRunnable.screenEvent==null) {
                    ScreenRunnable.screenEvent = new SC();
                    ScreenRunnable.screenEvent.setStart(System.currentTimeMillis());
                    ScreenRunnable.screenEvent.setStatus(SC.SCREEN_OFF);
                }
                else {
                    ScreenRunnable.screenEvent.setEnd(System.currentTimeMillis());
                    iLogApplication.persistInMemoryEvent(ScreenRunnable.screenEvent);
                    ScreenRunnable.screenEvent = new SC();
                    ScreenRunnable.screenEvent.setStart(System.currentTimeMillis());
                    ScreenRunnable.screenEvent.setStatus(SC.SCREEN_OFF);
                }
            } else {
                Log.d(this.toString(), "SCREEN ON");
                if(ScreenRunnable.screenEvent==null) {
                    ScreenRunnable.screenEvent = new SC();
                    ScreenRunnable.screenEvent.setStart(System.currentTimeMillis());
                    ScreenRunnable.screenEvent.setStatus(SC.SCREEN_ON);
                }
                else {
                    ScreenRunnable.screenEvent.setEnd(System.currentTimeMillis());
                    iLogApplication.persistInMemoryEvent(ScreenRunnable.screenEvent);
                    ScreenRunnable.screenEvent = new SC();
                    ScreenRunnable.screenEvent.setStart(System.currentTimeMillis());
                    ScreenRunnable.screenEvent.setStatus(SC.SCREEN_ON);
                }
            }
        }

    }
}
