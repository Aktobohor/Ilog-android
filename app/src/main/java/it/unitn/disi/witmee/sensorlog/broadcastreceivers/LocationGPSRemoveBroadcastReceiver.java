package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.unitn.disi.witmee.sensorlog.runnables.LocationGPSRunnable;

/**
 * {@link BroadcastReceiver} used to stop the collection process.
 */
public class LocationGPSRemoveBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        //Stop the collection process
        LocationGPSRunnable.locationReceived();
    }
}
