package it.unitn.disi.witmee.sensorlog.model.sensors;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static it.unitn.disi.witmee.sensorlog.utils.Utils.roundFloat;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 23/05/13
 * Time: 18.46
 */
public class RH extends AbstractSensorEvent {

    public static final String HUMIDITY = "humidity";

    /**
     * Relative ambient air humidity in percent
     */
    private int humidity;

    public RH() {
    }

    public RH(long timestamp, int accuracy, float humidity) {
        super(timestamp, accuracy, 0);
        this.humidity = roundFloat(humidity, 100);
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = roundFloat(humidity, 100);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getHumidity()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
