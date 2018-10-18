package it.unitn.disi.witmee.sensorlog.model.ambience;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: Ilya
 * Modified by: Mattia
 * Date: 31/05/13
 * Time: 8.56
 */
public class WN extends AbstractSensorEvent {

    private String bssid = "";
    private String ssid = "";
    private String capabilities = "";
    private int level = 0;
    private int frequency = 0;

    public WN() {
    }

    public WN(long timestamp, String bssid, String ssid, String capabilities, int level, int frequency) {
        super(timestamp, 0, 0);
        this.bssid = bssid;
        this.ssid = Utils.removeComma(ssid);
        this.capabilities = Utils.removeComma(capabilities);
        this.level = Utils.roundFloat(level, 100);
        this.frequency = frequency;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        //WN,d0:d0:fd:68:bc:7e,[WPA2-EAP-CCMP][ESS],5180,-61.0,unitn-x,2017-05-19 11:14:53.353
        return this.getClass().getSimpleName()+Utils.SEPARATOR+
                getBssid()+Utils.SEPARATOR+
                getCapabilities()+Utils.SEPARATOR+
                getFrequency()+Utils.SEPARATOR+
                getLevel()+Utils.SEPARATOR+
                getSsid()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
