package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.runnables.AudioRunnable;

/**
 * {@link BroadcastReceiver} used to start the collection process.
 */
public class AudioRequestBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. We call the method {@link AudioRunnable#startRecording()} to start the collection process but we also schedule
     * an {@link AlarmManager} to start again the recording.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        AudioRunnable.alarmManager = (AlarmManager) iLogApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(AudioRunnable.alarmManager!=null && AudioRunnable.pendingIntentRequest!=null) {
                AudioRunnable.alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (60*1000), AudioRunnable.pendingIntentRequest);
            }
        }

        AudioRunnable.startRecording();
    }
}
