package it.unitn.disi.witmee.sensorlog.model.metalog;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

public class SM extends AbstractSensorEvent {

    //SensorMonitoringLog
    /**
     * the ID of the sensor whose logging started or stopped
     */
    private int sensorId;

    /**
     * the timestamp when the sensor logging was started or stopped
     */
    private long timestamp;

    /**
     * indicates if the logging was started (when TRUE) or stopped (when FALSE)
     */
    private boolean isLogging;

    public SM() {
    }

    public SM(int sensorId, long timestamp, boolean logging) {
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        isLogging = logging;
    }

    public int getSensorId() {
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isLogging() {
        return isLogging;
    }

    public void setLogging(boolean logging) {
        isLogging = logging;
    }

    @Override
    public String toString() {
        //SM,true,1000022,2017-05-19 11:15:03.601
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                isLogging()+Utils.SEPARATOR+
                getSensorId()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
