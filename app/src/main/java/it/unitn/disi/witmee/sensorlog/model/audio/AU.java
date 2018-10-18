package it.unitn.disi.witmee.sensorlog.model.audio;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created by mattiazeni on 5/11/17.
 */

public class AU extends AbstractSensorEvent {

    //AudioEvent

    private String audioStream;

    public AU() {
    }

    public AU(long timestamp, int accuracy, String audioStream) {
        super(timestamp, accuracy, 0);
        this.audioStream = audioStream;
    }

    public String getAudioStream() {
        return audioStream;
    }

    public void setAudioStream(String audioStream) {
        this.audioStream = audioStream;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + Utils.SEPARATOR+
                getAudioStream()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
