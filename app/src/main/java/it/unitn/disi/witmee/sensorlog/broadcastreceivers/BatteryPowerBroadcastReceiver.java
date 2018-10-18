package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.rvalerio.fgchecker.AppChecker;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.system.BC;
import it.unitn.disi.witmee.sensorlog.model.virtual.AP;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link BC} event
 */
public class BatteryPowerBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. The intents we are interested in are {@link Intent#ACTION_POWER_CONNECTED} and {@link Intent#ACTION_POWER_DISCONNECTED}.
     * Depending on the state, we generate an event with {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        //If the user started charging the phone
        if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            final Intent mIntent = context.getApplicationContext().registerReceiver(null, intentFilter);

            //Integer representing the source from which the user is charging the phone
            int chargePlug = mIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            boolean wirelessCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS;

            BC batteryChargeEvent = null;
            if(usbCharge) {
                batteryChargeEvent = new BC(System.currentTimeMillis(), true, BC.CHARGING_USB);
            }
            else if (acCharge) {
                batteryChargeEvent = new BC(System.currentTimeMillis(), true, BC.CHARGING_AC);
            }
            else if (wirelessCharge) {
                batteryChargeEvent = new BC(System.currentTimeMillis(), true, BC.CHARGING_WIFI);
            }
            else {
                batteryChargeEvent = new BC(System.currentTimeMillis(), true, BC.CHARGING_UNKNOWN);
            }

            if(batteryChargeEvent!=null) {
                iLogApplication.persistInMemoryEvent(batteryChargeEvent);
            }
        }
        else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            BC batteryChargeEvent = new BC(System.currentTimeMillis(), false, "");
            iLogApplication.persistInMemoryEvent(batteryChargeEvent);
        }
    }
}