package it.unitn.disi.witmee.sensorlog.model.sensors;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static it.unitn.disi.witmee.sensorlog.utils.Utils.roundFloat;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 23/05/13
 * Time: 18.43
 */
public class PO extends AbstractSensorEvent {

    public static final String PROXIMITY = "proximity";

    /**
     * Proximity sensor distance measured in centimeters
     * <p/>
     * Note: Some proximity sensors only support a binary near or far measurement. In this case,
     * the sensor should report its maximum range value in the far state and a lesser value in the near state.
     */
    private int proximity;

    public PO() {
    }

    public PO(long timestamp, int accuracy, float proximity) {
        super(timestamp, accuracy, 0);
        this.proximity = roundFloat(proximity, 100);
    }

    public int getProximity() {
        return proximity;
    }

    public void setProximity(float proximity) {
        this.proximity = roundFloat(proximity, 100);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getProximity()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
