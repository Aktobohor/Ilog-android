package it.unitn.disi.witmee.sensorlog.model.ambience;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;


public class WF extends AbstractSensorEvent {

    /**
     * The WIFI Mac Addresses of the established connection.
     */
    private String BSSID;
    private String SSID;
    /**
     * Power of the signal the smartphone is connected to
     */
    private int rssi;

    /**
     * When true, a WIFI connection with the given SSID was established; when false, a WIFI connection with the
     * given SSID was lost
     */
    private boolean isConnected;

    public WF() {
    }

    public WF(long timestamp, float accuracy, String BSSID, boolean isConnected, String SSID, int rssi) {
        super(timestamp, accuracy, 0);
        this.BSSID = BSSID;
        this.isConnected = isConnected;
        this.SSID = SSID;
        this.rssi = rssi;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+Utils.SEPARATOR+
                                    getBSSID()+Utils.SEPARATOR+
                                     getSSID()+Utils.SEPARATOR+
                                 isConnected()+Utils.SEPARATOR+
                       Utils.longToStringFormat(getTimestamp());
    }
}
