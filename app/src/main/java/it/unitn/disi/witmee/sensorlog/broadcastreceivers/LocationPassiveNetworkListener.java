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

/**
 * Created by mattiazeni on 10/9/16.
 */

public class LocationPassiveNetworkListener implements LocationListener {

    @Override
    public void onLocationChanged(Location location) {
        Log.d(this.toString(), "LOCATION PAS: "+location.toString());

        switch(location.getProvider()) {
            case LocationManager.GPS_PROVIDER: {
                GL locationEvent = new GL(System.currentTimeMillis(), location.getTime(),
                        location.getAccuracy(), location.getLongitude(), location.getLatitude());

                //Persist in memory the GL event
                fillLocationEventMetaData(locationEvent, location);
                iLogApplication.persistInMemoryEvent(locationEvent);
                iLogApplication.lastSensorTimestamp.put(GL.class, location.getTime());

                break;
            }
            case LocationManager.NETWORK_PROVIDER: {
                NL locationEvent = new NL(System.currentTimeMillis(),
                        location.getTime(), location.getAccuracy(), location.getLongitude(), location.getLatitude());

                //Persist in memory the NL event
                fillLocationEventMetaData(locationEvent, location);
                iLogApplication.persistInMemoryEvent(locationEvent);
                iLogApplication.lastSensorTimestamp.put(NL.class, location.getTime());

                break;
            }
            default:
                break;
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
