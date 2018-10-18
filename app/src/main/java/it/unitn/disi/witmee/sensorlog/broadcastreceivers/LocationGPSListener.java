package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.locations.GL;
import it.unitn.disi.witmee.sensorlog.model.locations.LocationEvent;
import it.unitn.disi.witmee.sensorlog.runnables.LocationGPSRunnable;

/**
 * {@link LocationListener} triggered by the operating system when a new location is available, after that {@link LocationGPSRunnable} registered the location updates.
 */
public class LocationGPSListener implements LocationListener {

    /**
     * Method triggered when the location from the {@link LocationManager#GPS_PROVIDER} provider is available.
     * @param location {@link Location} object
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(this.toString(), "LOCATION GPS: "+location.toString());

        GL locationEvent = new GL(System.currentTimeMillis(), location.getTime(),
                location.getAccuracy(), location.getLongitude(), location.getLatitude());

        //Persist in memory the GL event
        fillLocationEventMetaData(locationEvent, location);
        iLogApplication.persistInMemoryEvent(locationEvent);

        iLogApplication.lastSensorTimestamp.put(GL.class, location.getTime());

        //The location updates are collected till the accuracy is below 16.0 meters. This is a strategy used to save battery and at the same time collect accurate data
        if(location.getAccuracy() < 16.0) {
            LocationGPSRunnable.locationReceived();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    /**
     * Method used to fill additional information such as {@link GL#altitude}, {@link GL#bearing} and {@link GL#speed}
     * @param event
     * @param location
     */
    private void fillLocationEventMetaData(LocationEvent event, Location location) {
        if(event instanceof GL) {
            ((GL) event).setAltitude(location.getAltitude());
            ((GL) event).setBearing(location.getBearing());
            ((GL) event).setSpeed(location.getSpeed());
        }
    }
}
