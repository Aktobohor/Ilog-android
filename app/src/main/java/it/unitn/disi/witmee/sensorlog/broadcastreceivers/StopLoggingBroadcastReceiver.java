package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;

/**
 * {@link BroadcastReceiver} that is triggered when there is the need to stop the logging process
 */
public class StopLoggingBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        String action=intent.getStringExtra("action");
        if(action.equals("stoplogging")){
            Log.d(this.toString(), "stopping");
            iLogApplication.stopLogging("close");
        }
    }
}
