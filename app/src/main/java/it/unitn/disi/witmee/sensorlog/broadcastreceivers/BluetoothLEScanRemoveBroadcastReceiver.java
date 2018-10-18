package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import it.unitn.disi.witmee.sensorlog.runnables.BluetoothLERunnable;

/**
 * {@link BroadcastReceiver} used to stop the collection process.
 */
public class BluetoothLEScanRemoveBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. It flushes the latest detected devices and stops the scanning procedure with
     * {@link BluetoothLeScanner#stopScan(ScanCallback)}.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        if (BluetoothLERunnable.myScanner != null  && BluetoothLERunnable.callback != null) {
            BluetoothLERunnable.myScanner.flushPendingScanResults(BluetoothLERunnable.callback);
            BluetoothLERunnable.myScanner.stopScan(BluetoothLERunnable.callback);
            Log.d(this.getClass().getSimpleName(), "discovery phase stop");
        }
    }
}
