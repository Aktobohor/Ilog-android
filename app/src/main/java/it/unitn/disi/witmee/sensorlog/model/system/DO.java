package it.unitn.disi.witmee.sensorlog.model.system;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: Ilya
 * Modified by: Mattia
 * Date: 31/05/13
 * Time: 8.56
 */
public class DO extends AbstractSensorEvent {//todo does it need to extend the AbstractSensor? no accuracy..

    private boolean status;

    public DO(long timestamp, float accuracy, boolean status) {
        super(timestamp, accuracy, 0);
        this.status=status;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                isStatus()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
