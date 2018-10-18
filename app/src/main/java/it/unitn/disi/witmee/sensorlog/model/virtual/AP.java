package it.unitn.disi.witmee.sensorlog.model.virtual;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Mattia
 * Date: 23/05/13
 * Time: 18.27
 */
public class AP extends AbstractSensorEvent {

    public static final String APPLICATION_NAME = "applicationname";

    /**
     * Acceleration minus Gx on the x-axis
     */
    private String applicationName;

    public AP() {
    }

    public AP(long timestamp, int accuracy, String applicationName) {
        super(timestamp, accuracy, 0);
        this.applicationName = Utils.removeComma(applicationName);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getApplicationName()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
