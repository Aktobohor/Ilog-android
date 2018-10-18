package it.unitn.disi.witmee.sensorlog.model.system;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 09/06/13
 * Time: 11.33
 */
public class RM extends AbstractSensorEvent {

    public static String MODE_SILENT = "mode_silent";
    public static String MODE_NORMAL = "mode_normal";

    private String ringMode = "";

    public RM(long timestamp, String ringMode) {
        super(timestamp, 0, 0);
        this.ringMode = Utils.removeComma(ringMode);
    }

    public String isPlaying() {
        return ringMode;
    }

    public void setPlaying(String ringMode) {
        this.ringMode = ringMode;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                isPlaying()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
