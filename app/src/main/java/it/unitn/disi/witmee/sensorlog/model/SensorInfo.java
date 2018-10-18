package it.unitn.disi.witmee.sensorlog.model;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 22/05/13
 * Time: 18.03
 */

public class SensorInfo {
    int sensorId;
    String sensorName;
    String sensorDisplayData1;
    String sensorDisplayData2;
    String sensorDisplayData3;

    boolean isSupported = false;
    int iconSupported;
    int iconUnsupported;

    public SensorInfo(String sensorName, int sensorId, int iconSupported, int iconUnsupported) {
        this.sensorName = Utils.removeComma(sensorName);
        this.sensorId = sensorId;
        this.iconSupported = iconSupported;
        this.iconUnsupported = iconUnsupported;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getSensorDisplayData1() {
        return sensorDisplayData1;
    }

    public void setSensorDisplayData1(String sensorDisplayData1) {
        this.sensorDisplayData1 = sensorDisplayData1;
    }

    public String getSensorDisplayData2() {
        return sensorDisplayData2;
    }

    public void setSensorDisplayData2(String sensorDisplayData2) {
        this.sensorDisplayData2 = sensorDisplayData2;
    }

    public String getSensorDisplayData3() {
        return sensorDisplayData3;
    }

    public void setSensorDisplayData3(String sensorDisplayData3) {
        this.sensorDisplayData3 = sensorDisplayData3;
    }

    public boolean isSupported() {
        return isSupported;
    }

    public void setSupported(boolean supported) {
        isSupported = supported;
    }

    public int getSensorId() {
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    public int getIconSupported() {
        return iconSupported;
    }

    public void setIconSupported(int iconSupported) {
        this.iconSupported = iconSupported;
    }

    public int getIconUnsupported() {
        return iconUnsupported;
    }

    public void setIconUnsupported(int iconUnsupported) {
        this.iconUnsupported = iconUnsupported;
    }
}
