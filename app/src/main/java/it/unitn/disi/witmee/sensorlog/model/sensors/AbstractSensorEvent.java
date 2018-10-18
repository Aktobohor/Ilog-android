package it.unitn.disi.witmee.sensorlog.model.sensors;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 23/05/13
 * Time: 18.28
 */
public abstract class AbstractSensorEvent {

    public static final String TIMESTAMP = "timestamp";
    public static final String ACCURACY = "accuracy";

    /**
     * the timestamp of the event
     */
    long timestamp;

    /**
     * The accuracy of this event.
     */
    float accuracy;

    /**
     * it defines the minimum sample rate in milli-seconds for this sensor, i.e., a new record would be added only if
     * the sampleRate ms passed since the previous record or if there was no previous record at all.
     */

    protected AbstractSensorEvent() {
    }

    protected AbstractSensorEvent(long timestamp, float accuracy, int sampleRate) {
        this.timestamp = timestamp;
        this.accuracy = accuracy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

}
