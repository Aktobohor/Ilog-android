package it.unitn.disi.witmee.sensorlog.model.metalog;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 09/06/13
 * Time: 11.33
 */
public class BM extends AbstractSensorEvent {

    /**
     * the timestamp when the sensor logging was started or stopped
     */
    private long timestamp;

    /**
     * integer field containing the current battery level, from 0 to scale
     */
    private int level;

    /**
     * integer containing the maximum battery level
     */
    private int scale;

    public BM() {
    }

    public BM(long timestamp, int level, int scale) {
        this.timestamp = timestamp;
        this.level = level;
        this.scale = scale;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    @Override
    public String toString() {
        //BM,78,100,2017-05-19 11:14:52.310
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getLevel()+Utils.SEPARATOR+
                getScale()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
