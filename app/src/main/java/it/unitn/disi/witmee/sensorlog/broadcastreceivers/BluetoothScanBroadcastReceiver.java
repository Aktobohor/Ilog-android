package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.runnables.AudioRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.BluetoothRunnable;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * {@link BroadcastReceiver} used to start the collection process.
 */
public class BluetoothScanBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. We call the method {@link BluetoothRunnable#discoverDevices()} to start the discovery process and we also
     * schedule an {@link AlarmManager} to schedule the next scanning.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        BluetoothRunnable.alarmManager = (AlarmManager) iLogApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(BluetoothRunnable.alarmManager!=null && BluetoothRunnable.pendingIntent!=null) {
                BluetoothRunnable.alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + iLogApplication.sharedPreferences.getInt(Utils.CONFIG_BLUETOOTHCOLLECTIONFREQUENCY, 0), BluetoothRunnable.pendingIntent);
            }
        }

        BluetoothRunnable.discoverDevices();
    }
}
