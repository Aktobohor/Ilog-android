package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.unitn.disi.witmee.sensorlog.runnables.AudioRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.LocationGPSRunnable;

/**
 * {@link BroadcastReceiver} used to start the collection process.
 */
public class LocationGPSRequestBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        //Request location updates
        LocationGPSRunnable.requestLocationUpdates();
    }
}
