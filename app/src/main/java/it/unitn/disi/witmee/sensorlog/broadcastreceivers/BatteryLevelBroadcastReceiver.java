package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.metalog.BM;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.system.AM;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static android.content.Context.BATTERY_SERVICE;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link BM} event and to close the application in case the battery goes below a certain threshold. TODO - Check if the automatic shut down works on new OS
 */
public class BatteryLevelBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. The intent we are interested in is {@link Intent#ACTION_BATTERY_CHANGED}. We persiste the event with
     * {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}. If the battery level is below a certain threshold saved in the {@link iLogApplication#sharedPreferences}
     * with the key {@link Utils#CONFIG_MINIMUMBATTERYLEVEL}, we turn off the application.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        Class sensorClass = BM.class;

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        long timestamp = System.currentTimeMillis();

        if(iLogApplication.lastSensorTimestamp.get(sensorClass)!=null) {
            if(iLogApplication.lastSensorTimestamp.get(sensorClass)!=level) {

                iLogApplication.persistInMemoryEvent(new BM(timestamp, level, scale));
                iLogApplication.lastSensorTimestamp.put(sensorClass, Long.valueOf(level));

                //Check if bettery level below threshold to turn off the application
                if(level<iLogApplication.sharedPreferences.getInt(Utils.CONFIG_MINIMUMBATTERYLEVEL, 0)) {
                    BatteryManager batteryManager = (BatteryManager) iLogApplication.getAppContext().getSystemService(BATTERY_SERVICE);

                    /**
                     * For OS versions above or equal to {@link Build.VERSION_CODES#M} we can detect with the method {@link BatteryManager#isCharging()} if the phone is charging
                     * in that case we do not turn off the app. For OS versions below this method is not available and then we just turn off the app without checks.
                     */
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(!batteryManager.isCharging()) {
                            iLogApplication.closeApplicationSafely();
                        }
                    }
                    else {
                        iLogApplication.closeApplicationSafely();
                    }
                }
            }
        }
        else {
            iLogApplication.persistInMemoryEvent(new BM(timestamp, level, scale));
            iLogApplication.lastSensorTimestamp.put(sensorClass, Long.valueOf(level));
        }
    }
}