package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.runnables.AmbienceRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.LocationGPSRunnable;

import static it.unitn.disi.witmee.sensorlog.application.iLogApplication.isNetworkConnected;

/**
 * {@link BroadcastReceiver} that detects the status of the location provider. The user is allowed to disable/enable it and then we need to be able to monitor its status.
 * This receiver does not handle the data collection.
 */
public class LocationStatusBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. At that point we can check the state of the provider using the {@link LocationManager#isProviderEnabled(String)}.
     * When the GPS is turned on but the Wifi is disabled, we need to run the {@link LocationGPSRunnable} while when GOS is turned off we need to stop it. Similarly for the
     * network provider.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        LocationManager locationManager = (LocationManager) iLogApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            System.out.println("GPS ON");
            if(!isNetworkConnected()) {
                iLogApplication.locationGPSRunnable.run();
            }
        }
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            System.out.println("GPS OFF");
            iLogApplication.locationGPSRunnable.stop();

        }
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            System.out.println("NETWORK ON");
            if(isNetworkConnected()) {
                iLogApplication.locationNetworkRunnable.run();
            }
        }
        if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            System.out.println("NETWORK OFF");
            iLogApplication.locationNetworkRunnable.stop();

        }
    }
}
