package it.unitn.disi.witmee.sensorlog.model.social;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;


public abstract class BluetoothEvent extends AbstractSensorEvent {
	
	public static final String NORMAL = "bluetoothNormal";
	public static final String LE = "bluetoothLE";

    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String BONDSTATE = "bondstate";
    public static final String RSSI = "rssi";

    private String name;
    private String address;
    private String bondstate;
    private double rssi;

    protected BluetoothEvent() {
    }

    public BluetoothEvent(long timestamp, String name, String address, String bondstate,
                         double rssi) {
        super(timestamp, 0, 0);
        this.name = Utils.removeComma(name);
        this.address = address;
        this.bondstate = bondstate;
        this.rssi = rssi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBondstate() {
        return bondstate;
    }

    public void setBondstate(String bondstate) {
        this.bondstate = bondstate;
    }

    public double getRssi() {
        return rssi;
    }

    public void setRssi(double rssi) {
        this.rssi = rssi;
    }
}
