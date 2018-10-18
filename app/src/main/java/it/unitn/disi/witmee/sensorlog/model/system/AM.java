package it.unitn.disi.witmee.sensorlog.model.system;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 09/06/13
 * Time: 11.33
 */
public class AM extends AbstractSensorEvent {

    private boolean active = false;

    public AM(long timestamp, boolean active) {
        super(timestamp, 0, 0);
        this.active = active;
    }

    public boolean isPlaying() {
        return active;
    }

    public void setPlaying(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                isPlaying()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
