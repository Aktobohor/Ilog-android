package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.rvalerio.fgchecker.AppChecker;


import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.system.AM;
import it.unitn.disi.witmee.sensorlog.model.virtual.AP;
import it.unitn.disi.witmee.sensorlog.runnables.ApplicationsRunnable;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link AP} event
 */
public class ApplicationBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. This is an intent triggered by an {@link AlarmManager}. We schedule another alarm manager so that to trigger
     * the next detection. Finally, we detect the running application using the {@link AppChecker} class and we save the {@link AP} event.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        ApplicationsRunnable.alarmManager = (AlarmManager) iLogApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(ApplicationsRunnable.alarmManager!=null && ApplicationsRunnable.pendingIntent!=null) {
                ApplicationsRunnable.alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + iLogApplication.sharedPreferences.getInt(Utils.CONFIG_APPLICATIONCOLLECTIONFREQUENCY, 0), ApplicationsRunnable.pendingIntent);
            }
        }

        Thread thread = new Thread(new Runnable() {
            public void run() {
                AppChecker appChecker = new AppChecker();
                AP applicationEvent = new AP(System.currentTimeMillis(), 0, appChecker.getForegroundApp(iLogApplication.getAppContext()));
                iLogApplication.persistInMemoryEvent(applicationEvent);
            }
        });
        thread.start();
    }
}
