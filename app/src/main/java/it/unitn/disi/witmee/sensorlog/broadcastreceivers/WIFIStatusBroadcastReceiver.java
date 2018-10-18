package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.ambience.WF;
import it.unitn.disi.witmee.sensorlog.runnables.AmbienceRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.LocationGPSRunnable;

/**
 * {@link BroadcastReceiver} used to detect the status of the wifi adapter and not to collect data directly. It listens to {@link WifiManager#SUPPLICANT_STATE_CHANGED_ACTION}
 * actions.
 */
public class WIFIStatusBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. If the state of the {@link WifiManager} is {@link SupplicantState#COMPLETED}, it means that the phone is now connected to
     * a network and we need to: stop the {@link LocationGPSRunnable} is running and start the {@link it.unitn.disi.witmee.sensorlog.runnables.LocationNetworkRunnable}. At the
     * same time we need to call {@link #createWIFIEvent(long, String, String, int, boolean)} to create a wifi connection event ({@link AmbienceRunnable}) and call {@link iLogApplication#uploadAllIfConnected()}.
     * If instead the state of the {@link WifiManager} is {@link SupplicantState#DISCONNECTED}, we need to to the opposite. Additionally this {@link BroadcastReceiver} listens
     * also for actions "android.net.wifi.WIFI_STATE_CHANGED" to detect when the {@link WifiManager} is turned on/off.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        long timestamp = System.currentTimeMillis();

        LocationManager locationManager = (LocationManager) iLogApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);

        if(intent!=null) {
            if(intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                //Log.d(this.toString(), state.toString());
                if(state.equals(SupplicantState.COMPLETED)) {
                    if(!iLogApplication.lastWIFIState) {
                        System.out.println("WIFI CONNECTED!");
                        iLogApplication.lastWIFIState = true;
                        iLogApplication.ambienceRunnable.run();
                        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            iLogApplication.locationNetworkRunnable.run();
                        }
                        iLogApplication.locationGPSRunnable.stop();
                    }
                    if(!AmbienceRunnable.isStopped) {
                        String bssid = getBSSID();
                        String ssid = getSSID();
                        int rssi = getRSSI();
                        if (AmbienceRunnable.lastBSSID == null) {
                            Log.d(this.toString(), "WIFI CONNECTED");
                            createWIFIEvent(timestamp, bssid, ssid, rssi, true);
                            AmbienceRunnable.lastBSSID = bssid;
                            AmbienceRunnable.lastSSID = ssid;
                            AmbienceRunnable.lastRSSI = rssi;
                        } else {
                            if (!AmbienceRunnable.lastBSSID.equals(bssid)) {
                                createWIFIEvent(timestamp, AmbienceRunnable.lastBSSID, AmbienceRunnable.lastSSID, AmbienceRunnable.lastRSSI, false);
                                createWIFIEvent(timestamp, bssid, ssid, rssi, true);
                                AmbienceRunnable.lastBSSID = bssid;
                            }
                        }
                    }
                    iLogApplication.uploadAllIfConnected();
                }
                if(state.equals(SupplicantState.DISCONNECTED)) {
                    if(iLogApplication.lastWIFIState) {
                        System.out.println("WIFI DISCONNECTED!");
                        if (!AmbienceRunnable.isStopped && AmbienceRunnable.lastBSSID != null) {
                            Log.d(this.toString(), "WIFI DISCONNECTED");
                            createWIFIEvent(timestamp, AmbienceRunnable.lastBSSID, AmbienceRunnable.lastSSID, AmbienceRunnable.lastRSSI, false);
                            AmbienceRunnable.lastBSSID = null;
                            AmbienceRunnable.lastSSID = null;
                            AmbienceRunnable.lastRSSI = 0;
                        }
                        iLogApplication.lastWIFIState = false;
                        iLogApplication.ambienceRunnable.stop();
                        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            iLogApplication.locationGPSRunnable.run();
                        }
                        iLogApplication.locationNetworkRunnable.stop();
                    }
                }
                iLogApplication.lastSensorTimestamp.put(WF.class, timestamp);
            }
            if(intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                //Log.d(this.toString(), wifiState+"");
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        System.out.println("WIFI DISABLED!");
                        //iLogApplication.updateService(WIFINetworksLoggingService.class, iLogApplication.WIFI_NETWORKS_SENSOR_ID, false);
                        iLogApplication.ambienceRunnable.stop();

                        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            iLogApplication.locationGPSRunnable.run();
                        }
                        iLogApplication.locationNetworkRunnable.stop();

                        iLogApplication.lastWIFIState=false;

                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        System.out.println("WIFI ENABLED");
                        //iLogApplication.updateService(WIFINetworksLoggingService.class, iLogApplication.WIFI_NETWORKS_SENSOR_ID, true);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Method that persists in memory a {@link WF} event
     * @param timestamp Long representing the current timestamp, in milliseconds from epoch
     * @param bssid String representing the bssid of the network
     * @param ssid String representing the ssid of the network
     * @param rssi Integer representing the rssi (signal strength) in dbm, of the network
     * @param isConnected Boolean representing the status, conencted if true, disconnected if false
     */
    private void createWIFIEvent(long timestamp, String bssid, String ssid, int rssi, boolean isConnected) {
        WF wifiEvent = new WF(timestamp, 0f, bssid, isConnected, ssid, rssi);
        Log.d(this.toString(), wifiEvent.toString());
        iLogApplication.persistInMemoryEvent(wifiEvent);
    }

    /**
     * Method that generated the bssid of the connected network
     * @return String with the bssid of the network
     */
    private String getBSSID() {
        //known issue: on some models BSSID not updted
        // https://code.google.com/p/android/issues/detail?id=38483
        WifiManager wifimanager = (WifiManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifimanager.getConnectionInfo();
        return wifiInfo.getBSSID();
    }

    /**
     * Method that generated the ssid of the connected network
     * @return String with the ssid of the network
     */
    private String getSSID() {
        WifiManager wifimanager = (WifiManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifimanager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    /**
     * Method that generated the rssi of the connected network
     * @return Integer with the rssi of the network
     */
    private int getRSSI() {
        WifiManager wifimanager = (WifiManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifimanager.getConnectionInfo();
        return wifiInfo.getRssi();
    }
}
