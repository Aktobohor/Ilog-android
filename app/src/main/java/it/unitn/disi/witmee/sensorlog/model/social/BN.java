package it.unitn.disi.witmee.sensorlog.model.social;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 27/05/13
 * Time: 22.16
 */
public class BN extends BluetoothEvent {

    public BN() {
    }

    public BN(long timestamp, String name, String address, String bondstate,
              double rssi, float accuracy) {
        super(timestamp, name, address, bondstate, rssi);
    }

    @Override
    public String toString() {
        //BluetoothNormalEvent,6D:E8:75:7C:37:C4,BOND_DONE,null,-82.0,1476969579589
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getAddress()+Utils.SEPARATOR+
                getBondstate()+Utils.SEPARATOR+
                getName()+Utils.SEPARATOR+
                getRssi()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
