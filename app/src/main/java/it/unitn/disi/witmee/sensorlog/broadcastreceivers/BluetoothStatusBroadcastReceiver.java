package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;

/**
 * {@link BroadcastReceiver} used to detect the status of the bluetooth adapter and not to collect data directly. It listens to {@link BluetoothAdapter#ACTION_STATE_CHANGED}
 * actions.
 */
public class BluetoothStatusBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. If the state of the {@link BluetoothAdapter} is {@link BluetoothAdapter#STATE_OFF}, means that the user just
     * disabled the bluetooth, then we need to stop the collection from {@link it.unitn.disi.witmee.sensorlog.runnables.BluetoothLERunnable} and {@link it.unitn.disi.witmee.sensorlog.runnables.BluetoothRunnable}.
     * If instead the state of the {@link BluetoothAdapter} is {@link BluetoothAdapter#STATE_ON} we should run them.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    if(iLogApplication.bluetoothLERunnable!=null) {
                        iLogApplication.bluetoothLERunnable.stop();
                    }
                    if(iLogApplication.bluetoothRunnable!=null) {
                        iLogApplication.bluetoothRunnable.stop();
                    }
                    break;
                case BluetoothAdapter.STATE_ON:
                    if(iLogApplication.bluetoothLERunnable!=null) {
                        iLogApplication.bluetoothLERunnable.run();
                    }
                    if(iLogApplication.bluetoothRunnable!=null) {
                        iLogApplication.bluetoothRunnable.run();
                    }break;
            }
        }
    }
}
