package it.unitn.disi.witmee.sensorlog.model.system;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 09/06/13
 * Time: 11.33
 */
public class MU extends AbstractSensorEvent {

    private boolean playing = false;

    public MU(long timestamp, boolean playing) {
        super(timestamp, 0, 0);
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                isPlaying()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
