package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.social.BN;
import it.unitn.disi.witmee.sensorlog.model.system.AM;
import it.unitn.disi.witmee.sensorlog.runnables.BluetoothRunnable;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link BN} event
 */
public class BluetoothBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. The intents we are interested in are {@link BluetoothDevice#ACTION_FOUND} and
     * {@link BluetoothAdapter#ACTION_DISCOVERY_FINISHED}. Depending on the state, we generate an event with {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, final Intent intent) {
        final String action = intent.getAction();

        /**
         * TODO - This thread probably is not needed, we put it because on our test phone (Oneplus 3) bluetooth was causing a lot of issues, battery draining and huge CPU usage but in the end we discovered it is a bug in the smartphone
         * @see <a href="https://forums.oneplus.com/threads/battery-drain-with-bluetooth-on.649160/">OnePlus bug report</a> for more details about this issue
         */
        Thread thread = new Thread(new Runnable() {
            public void run() {
                //Action triggered when a device is found
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String bondState = null;

                    //Get the bond state - None, Bonding (connecting) and Bonded (connected)
                    switch(device.getBondState()) {
                        case BluetoothDevice.BOND_NONE: bondState = "BOND_NONE";
                            break;
                        case BluetoothDevice.BOND_BONDING: bondState = "BOND_BONDING";
                            break;
                        case BluetoothDevice.BOND_BONDED: bondState = "BOND_BONDED";
                            break;
                    }

                    BN bluetoothEvent = new BN(System.currentTimeMillis(), device.getName(), device.getAddress(),
                            bondState, intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE), 0);

                    /**
                     * Since the same device can be detected multiple times in a single scan we need to filter them out. The found devices are added to
                     * {@link BluetoothRunnable#bluetoothNormalEventArrayList} while scanning.
                     */
                    if(BluetoothRunnable.bluetoothNormalEventArrayList!=null) {
                        if(getMostRecentTimestampBluetoothEventPerAddress(BluetoothRunnable.bluetoothNormalEventArrayList, device.getAddress())==-1) {
                            if(!isBTAlreadyPresent(BluetoothRunnable.bluetoothNormalEventArrayList, bluetoothEvent)) {
                                BluetoothRunnable.bluetoothNormalEventArrayList.add(bluetoothEvent);
                            }
                        }
                        if ((System.currentTimeMillis()-getMostRecentTimestampBluetoothEventPerAddress(BluetoothRunnable.bluetoothNormalEventArrayList, device.getAddress())) > iLogApplication.sharedPreferences.getInt(Utils.CONFIG_BLUETOOTHCOLLECTIONFREQUENCY, 0)) {
                            if(!isBTAlreadyPresent(BluetoothRunnable.bluetoothNormalEventArrayList, bluetoothEvent)) {
                                BluetoothRunnable.bluetoothNormalEventArrayList.add(bluetoothEvent);
                            }
                        }
                    }
                }
                //When the scan is finished the {@link BN} events are persisted, one by one
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    for(int index=0; index<BluetoothRunnable.bluetoothNormalEventArrayList.size();index++) {
                        iLogApplication.persistInMemoryEvent(BluetoothRunnable.bluetoothNormalEventArrayList.get(index));
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * Method that analyses the found bluetooth devices and returns the most recent timestamp per address
     * @param bluetoothNormalEventArrayList List of found devices
     * @param address Address of the device to be found
     * @return long representing the timestamp
     */
    private long getMostRecentTimestampBluetoothEventPerAddress(ArrayList<BN> bluetoothNormalEventArrayList, String address) {
        long timestamp = -1;
        for(int index=0;index<bluetoothNormalEventArrayList.size();index++) {
            if(bluetoothNormalEventArrayList.get(index).getAddress().equals(address)) {
                if(timestamp<bluetoothNormalEventArrayList.get(index).getTimestamp()) {
                    timestamp = bluetoothNormalEventArrayList.get(index).getTimestamp();
                }
            }
        }
        return timestamp;
    }

    /**
     * Method that detects if a device has already been found
     * @param bluetoothNormalEventArrayList List of found devices
     * @param bluetoothEvent {@link BN} event that has to be searched
     * @return True if the device is already present, false otherwise
     */
    private boolean isBTAlreadyPresent(ArrayList<BN> bluetoothNormalEventArrayList, BN bluetoothEvent) {
        for(BN network : bluetoothNormalEventArrayList) {
            if(network.getAddress().equals(bluetoothEvent.getAddress())) {
                return true;
            }
        }
        return false;
    }
}
