package it.unitn.disi.witmee.sensorlog.model.sensors;

import android.util.Base64;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 23/05/13
 * Time: 18.27
 */
public class MV extends AbstractSensorEvent {

    private String labels = null;

    public MV() {
    }

    public MV(long timestamp, int accuracy, String labels) {
        super(timestamp, accuracy, 0);
        this.labels = Base64.encodeToString(labels.getBytes(), Base64.NO_WRAP);
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getLabels()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
