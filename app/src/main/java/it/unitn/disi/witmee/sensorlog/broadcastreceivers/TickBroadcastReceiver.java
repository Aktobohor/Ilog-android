package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;

/**
 * {@link BroadcastReceiver} triggered by the operating system at every tick of the smarpthone
 */
public class TickBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. The intent we are interested in is {@link Intent#ACTION_TIME_TICK}. It updates the
     * {@link iLogApplication#mainBuilder} and then verifies all the {@link it.unitn.disi.witmee.sensorlog.model.Task}, {@link it.unitn.disi.witmee.sensorlog.model.Challenge}, {@link it.unitn.disi.witmee.sensorlog.model.Question} and {@link it.unitn.disi.witmee.sensorlog.model.Message}.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {

            iLogApplication.mainBuilder.setContentTitle(iLogApplication.getAppContext().getResources().getString(R.string.mainNotificationTitle))
                    .setSmallIcon(R.drawable.ic_notification_bar)
                    .setAutoCancel(false)
                    .setOngoing(true);

            if (iLogApplication.mainBuilder != null) {
                iLogApplication.mainBuilder.setWhen(System.currentTimeMillis());
                iLogApplication.mainBuilder.setContentText(iLogApplication.getAppContext().getResources().getString(R.string.trackingActivated));
            }

            Log.d(this.toString(), "SIZE: " + iLogApplication.tmpStorage.size() / 1000000.0 + " MB");

            iLogApplication.verifyTasks();
            iLogApplication.verifyChallenges();
            iLogApplication.verifyTimeDiaries();
            iLogApplication.verifyMessages();
        }
    }
}
