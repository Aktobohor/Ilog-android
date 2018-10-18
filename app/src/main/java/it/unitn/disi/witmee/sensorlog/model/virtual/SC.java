package it.unitn.disi.witmee.sensorlog.model.virtual;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Mattia
 * Date: 23/05/13
 * Time: 18.27
 */
public class SC extends AbstractSensorEvent {

	public static String SCREEN_ON = "SCREEN_ON";
	public static String SCREEN_OFF = "SCREEN_OFF";

    /**
     * Acceleration minus Gx on the x-axis
     */
    private String status;
    private long timestampstart;
    private long timestampend;

    public SC() {
    }

    public SC(long timestamp, String status, long start, long end) {
        super(timestamp, 0, 0);
        this.status = Utils.removeComma(status);
        this.timestampstart = start;
        this.timestampend = end;
    }

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

    public long getStart() {
        return timestampstart;
    }

    public void setStart(long start) {
        this.timestampstart = start;
    }

    public long getEnd() {
        return timestampend;
    }

    public void setEnd(long end) {
        this.timestampend = end;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getStatus()+Utils.SEPARATOR+
                Utils.longToStringFormat(getStart())+Utils.SEPARATOR+
                Utils.longToStringFormat(getEnd());
    }
}
