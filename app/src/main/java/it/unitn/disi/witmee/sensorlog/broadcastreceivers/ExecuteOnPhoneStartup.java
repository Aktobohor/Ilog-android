package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.unitn.disi.witmee.sensorlog.activities.MainActivity;

/**
 * {@link BroadcastReceiver} used to detect when the phone started up. It is registered directly in the manifest.
 */
public class ExecuteOnPhoneStartup extends BroadcastReceiver {

    public static final String AUTO_RUN = "AUTO_RUN";

    /**
     * Method called when the {@link Intent} is received. It starts the application by starting the {@link MainActivity} with {@link Context#startActivities(Intent[])}
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivityIntent.putExtra(AUTO_RUN, true);
        System.out.println("Startup");
        context.startActivity(mainActivityIntent);
    }
}
