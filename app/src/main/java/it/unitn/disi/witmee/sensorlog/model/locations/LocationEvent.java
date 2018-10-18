package it.unitn.disi.witmee.sensorlog.model.locations;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

public abstract class LocationEvent extends AbstractSensorEvent {

    /**
     * longitude, in degrees.
     */
    private String longitude;
    /**
     * latitude, in degrees.
     */
    private String latitude;

    protected LocationEvent() {
    }

    public LocationEvent(long timestamp, long providerTimestamp, float accuracy, double longitude, double latitude, int minSampleRate) {
        super(timestamp, accuracy, minSampleRate);

        this.latitude = Utils.roundToDecimalPlace(latitude, 5);
        this.longitude = Utils.roundToDecimalPlace(longitude, 5);
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @Override
    public String toString() {
        return "lon: " + longitude + "; lat: " + latitude;
    }
}
