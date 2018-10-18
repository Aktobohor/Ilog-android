package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.runnables.WIFINetworksRunnable;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created by mattiazeni on 4/4/16.
 */
public class WIFINetworksBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        WIFINetworksRunnable.alarmManager = (AlarmManager) iLogApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(WIFINetworksRunnable.alarmManager!=null && WIFINetworksRunnable.pendingIntent!=null) {
                WIFINetworksRunnable.alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + iLogApplication.sharedPreferences.getInt(Utils.CONFIG_WIFICOLLECTIONFREQUENCY, 0), WIFINetworksRunnable.pendingIntent);
            }
        }

        WIFINetworksRunnable.mWifiManager = (WifiManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(WIFINetworksRunnable.mWifiManager != null) {
            WIFINetworksRunnable.mWifiManager.startScan();
        }
    }
}
