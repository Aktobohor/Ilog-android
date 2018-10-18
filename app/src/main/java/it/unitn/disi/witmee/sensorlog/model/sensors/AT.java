package it.unitn.disi.witmee.sensorlog.model.sensors;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static it.unitn.disi.witmee.sensorlog.utils.Utils.roundFloat;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 23/05/13
 * Time: 18.47
 */
public class AT extends AbstractSensorEvent {

    public static final String TEMPERATURE = "temperature";

    /**
     * ambient (room) temperature in degree Celsius
     */
    private int temperature;

    public AT() {
    }

    public AT(long timestamp, int accuracy, float temperature) {
        super(timestamp, accuracy, 0);
        this.temperature = roundFloat(temperature, 2);
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = roundFloat(temperature, 100);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getTemperature()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
