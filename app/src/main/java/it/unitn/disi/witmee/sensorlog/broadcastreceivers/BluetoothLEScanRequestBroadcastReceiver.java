package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.app.AlarmManager;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.runnables.AudioRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.BluetoothLERunnable;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * {@link BroadcastReceiver} used to start the collection process.
 */
public class BluetoothLEScanRequestBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. It schedules the next scanning procedure with the {@link BluetoothLERunnable#alarmManager} and then performs
     * the actual scan with {@link android.bluetooth.le.BluetoothLeScanner#startScan(ScanCallback)} which results are sent to {@link BluetoothLERunnable#callback}. Finally,
     * it calls the {@link BluetoothLERunnable#startRemove()} method to schedule the stop scanning procedure.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        BluetoothLERunnable.alarmManager = (AlarmManager) iLogApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(BluetoothLERunnable.alarmManager!=null && BluetoothLERunnable.pendingIntentRequest!=null) {
                BluetoothLERunnable.alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + iLogApplication.sharedPreferences.getInt(Utils.CONFIG_BLUETOOTHLECOLLECTIONFREQUENCY, 0), BluetoothLERunnable.pendingIntentRequest);
            }
        }

        if (BluetoothLERunnable.myScanner != null && BluetoothLERunnable.mBluetoothAdapter != null && BluetoothLERunnable.callback != null && BluetoothLERunnable.settings != null) {
            if(BluetoothLERunnable.mBluetoothAdapter.isEnabled()) {
                BluetoothLERunnable.myScanner.startScan(null, BluetoothLERunnable.settings, BluetoothLERunnable.callback);
                Log.d(this.getClass().getSimpleName(), "discovery phase");
            }
        }

        BluetoothLERunnable.startRemove();
    }
}
