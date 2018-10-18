package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.locations.GL;
import it.unitn.disi.witmee.sensorlog.model.locations.LocationEvent;
import it.unitn.disi.witmee.sensorlog.model.locations.NL;
import it.unitn.disi.witmee.sensorlog.runnables.LocationGPSRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.LocationNetworkRunnable;

/**
 * {@link LocationListener} triggered by the operating system when a new location is available, after that {@link LocationNetworkRunnable} registered the location updates.
 */
public class LocationNetworkListener implements LocationListener {

    /**
     * Method triggered when the location from the {@link LocationManager#NETWORK_PROVIDER} provider is available.
     * @param location {@link Location} object
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(this.toString(), "LOCATION NET: "+location.toString());

        NL locationEvent = new NL(System.currentTimeMillis(),
                location.getTime(), location.getAccuracy(), location.getLongitude(), location.getLatitude());

        //Persist in memory the NL event
        fillLocationEventMetaData(locationEvent, location);
        iLogApplication.persistInMemoryEvent(locationEvent);

        iLogApplication.lastSensorTimestamp.put(NL.class, location.getTime());

        Log.d(this.toString(), location.toString());

        //Once the first locatione vent is received, stop new updates
        LocationNetworkRunnable.locationReceived();
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
     * Method used to fill additional information such as {@link GL#altitude}, {@link GL#bearing} and {@link GL#speed}.
     * TODO - remove this, not used with {@link NL}
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
