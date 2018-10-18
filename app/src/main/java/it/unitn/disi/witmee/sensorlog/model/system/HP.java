package it.unitn.disi.witmee.sensorlog.model.system;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 09/06/13
 * Time: 11.33
 */
public class HP extends AbstractSensorEvent {

    private boolean plugged = false;

    public HP(long timestamp, boolean plugged) {
        super(timestamp, 0, 0);
        this.plugged = plugged;
    }

    public boolean isPlugged() {
        return plugged;
    }

    public void setPlugged(boolean plugged) {
        this.plugged = plugged;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                isPlugged()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
