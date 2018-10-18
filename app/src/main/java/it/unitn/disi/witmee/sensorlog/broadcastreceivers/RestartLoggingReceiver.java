package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AC;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;

/**
 * {@link BroadcastReceiver} that is triggered when there is the need to restart the logging process
 */
public class RestartLoggingReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if(iLogApplication.isMonitoringServiceRunning) {
            if (iLogApplication.lastSensorTimestamp.get(AC.class) != null) {
                System.out.println("Restarting all active sensors " + (System.currentTimeMillis() - iLogApplication.lastSensorTimestamp.get(AC.class)));
            }

            iLogApplication.airplaneModeRunnable.restart();
            iLogApplication.ambienceRunnable.restart();
            iLogApplication.applicationsRunnable.restart();
            iLogApplication.audioRunnable.restart();
            iLogApplication.batteryChargeRunnable.restart();
            iLogApplication.batteryLevelRunnable.restart();
            if(iLogApplication.bluetoothLERunnable!=null) {
                iLogApplication.bluetoothLERunnable.restart();
            }
            if(iLogApplication.bluetoothRunnable!=null) {
                iLogApplication.bluetoothRunnable.restart();
            }
            iLogApplication.dozeRunnable.restart();
            iLogApplication.headsetRunnable.restart();
            iLogApplication.locationGPSRunnable.restart();
            iLogApplication.locationNetworkRunnable.restart();
            iLogApplication.musicRunnable.restart();
            iLogApplication.phoneCallOutRunnable.restart();
            iLogApplication.phoneCallInRunnable.restart();
            iLogApplication.ringModeRunnable.restart();
            iLogApplication.screenRunnable.restart();
            iLogApplication.sensorAccelerometerRunnable.restart();
            iLogApplication.sensorAmbientTemperatureRunnable.restart();
            iLogApplication.sensorGravityRunnable.restart();
            iLogApplication.sensorGyroscopeRunnable.restart();
            iLogApplication.sensorLightRunnable.restart();
            iLogApplication.sensorLinearAccelerometerRunnable.restart();
            iLogApplication.sensorMagneticFieldRunnable.restart();
            iLogApplication.sensorOrientationRunnable.restart();
            iLogApplication.sensorPressureRunnable.restart();
            iLogApplication.sensorProximityRunnable.restart();
            iLogApplication.sensorRelativeHumidityRunnable.restart();
            iLogApplication.sensorRotationVectorRunnable.restart();
            iLogApplication.smsInRunnable.restart();
            iLogApplication.smsOutRunnable.restart();
            iLogApplication.userPresentRunnable.restart();
            iLogApplication.wifiNetworksRunnable.restart();
            iLogApplication.notificationRunnable.restart();
            iLogApplication.touchEventRunnable.restart();
            iLogApplication.cellInfoRunnable.restart();
            iLogApplication.movementActivityRunnable.restart();
        }
        else {
            System.out.println("Restarting all active sensors not required");
        }
    }
}