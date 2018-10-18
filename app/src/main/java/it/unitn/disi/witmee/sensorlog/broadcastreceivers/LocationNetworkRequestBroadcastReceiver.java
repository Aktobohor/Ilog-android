package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.unitn.disi.witmee.sensorlog.runnables.LocationNetworkRunnable;

/**
 * {@link BroadcastReceiver} used to start the collection process.
 */
public class LocationNetworkRequestBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        //Request location updates
        LocationNetworkRunnable.requestLocationUpdates();
    }
}
