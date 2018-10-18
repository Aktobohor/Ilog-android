package it.unitn.disi.witmee.sensorlog.model.sensors;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static it.unitn.disi.witmee.sensorlog.utils.Utils.roundFloat;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 23/05/13
 * Time: 18.41
 */
public class LI extends AbstractSensorEvent {

    public static final String LIGHT_LEVEL = "lightlevel";


    /**
     * Ambient light level in SI lux units
     */
    private int lightLevel;

    public LI() {
    }

    public LI(long timestamp, int accuracy, float lightLevel) {
        super(timestamp, accuracy, 0);
        this.lightLevel = roundFloat(lightLevel, 100);
    }

    public int getLightLevel() {
        return lightLevel;
    }

    public void setLightLevel(float lightLevel) {
        this.lightLevel = roundFloat(lightLevel, 100);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getLightLevel()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }

}
