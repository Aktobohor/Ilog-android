package it.unitn.disi.witmee.sensorlog.model.sensors;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static it.unitn.disi.witmee.sensorlog.utils.Utils.roundFloat;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 23/05/13
 * Time: 18.42
 */
public class PE extends AbstractSensorEvent {

    public static final String ATMOSPHERIC_PRESSURE = "atmosphericpressure";

    /**
     * Atmospheric pressure in hPa (millibar)
     */
    private int atmosphericPressure;

    public PE() {
    }

    public PE(long timestamp, int accuracy, float atmosphericPressure) {
        super(timestamp, accuracy, 0);
        this.atmosphericPressure = roundFloat(atmosphericPressure, 100);
    }

    public int getAtmosphericPressure() {
        return atmosphericPressure;
    }

    public void setAtmosphericPressure(float atmosphericPressure) {
        this.atmosphericPressure = roundFloat(atmosphericPressure, 100);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getAtmosphericPressure()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }

}
