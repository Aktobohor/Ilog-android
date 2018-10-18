package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.ambience.WN;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.system.MU;
import it.unitn.disi.witmee.sensorlog.runnables.WIFINetworksRunnable;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link WN} event
 */
public class WIFIBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. The intent we are interested in is {@link WifiManager#SCAN_RESULTS_AVAILABLE_ACTION}. An event is created with
     * {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
            //Get the list of networks available
            final List<ScanResult> mScanResults = WIFINetworksRunnable.mWifiManager.getScanResults();
            final long time = System.currentTimeMillis();

            Thread thread = new Thread(new Runnable() {
                public void run() {
                    if (iLogApplication.lastSensorTimestamp.get(WN.class) != null) {
                        if ((time - iLogApplication.lastSensorTimestamp.get(WN.class)) > iLogApplication.WN_FILTERING) {
                            for(int index=0; index<mScanResults.size(); index++) {
                                WN wifiNetworksEvent = new WN(time, mScanResults.get(index).BSSID, mScanResults.get(index).SSID, mScanResults.get(index).capabilities, mScanResults.get(index).level, mScanResults.get(index).frequency);
                                iLogApplication.persistInMemoryEvent(wifiNetworksEvent);
                            }

                            iLogApplication.lastSensorTimestamp.put(WN.class, time);
                        }
                    } else {
                        for(int index=0; index<mScanResults.size(); index++) {
                            WN wifiNetworksEvent = new WN(time, mScanResults.get(index).BSSID, mScanResults.get(index).SSID, mScanResults.get(index).capabilities, mScanResults.get(index).level, mScanResults.get(index).frequency);
                            iLogApplication.persistInMemoryEvent(wifiNetworksEvent);
                        }

                        iLogApplication.lastSensorTimestamp.put(WN.class, time);
                    }
                }
            });
            thread.start();
        }
    }
}
