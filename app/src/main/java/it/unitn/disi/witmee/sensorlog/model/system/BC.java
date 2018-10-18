package it.unitn.disi.witmee.sensorlog.model.system;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 09/06/13
 * Time: 11.33
 */
public class BC extends AbstractSensorEvent {

    public static String CHARGING_USB = "charging_usb";
    public static String CHARGING_AC = "charging_ac";
    public static String CHARGING_WIFI = "charging_wifi";
    public static String CHARGING_UNKNOWN = "charging_unknown";

    private boolean onCharge = false;
    private String chargeType = "";

    public BC(long timestamp, boolean onCharge, String chargeType) {
        super(timestamp, 0, 0);
        this.onCharge = onCharge;
        this.chargeType = Utils.removeComma(chargeType);
    }

    public boolean isOnCharge() {
        return onCharge;
    }

    public void setOnCharge(boolean onCharge) {
        this.onCharge = onCharge;
    }

    public String getChargeType() {
        return chargeType;
    }

    public void setChargeType(String chargeType) {
        this.chargeType = chargeType;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getChargeType()+Utils.SEPARATOR+
                isOnCharge()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
