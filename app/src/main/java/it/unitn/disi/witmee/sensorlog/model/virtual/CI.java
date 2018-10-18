package it.unitn.disi.witmee.sensorlog.model.virtual;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Mattia
 * Date: 23/05/13
 * Time: 18.27
 */
public class CI extends AbstractSensorEvent {

	public static String CALL_LOST = "CALL_LOST";
	public static String CALL = "CALL";
    
	public static final String START_TIME = "starttime";
	public static final String END_TIME = "endtime";
	public static final String DURATION = "duration";
	public static final String PHONE_NUMBER = "phonenumber";
	public static final String PHONE_NAME = "phonename";
	public static final String STATUS = "status";
	
    /**
     * Acceleration minus Gx on the x-axis
     */
    private long startTime;
	private long endTime;
	private long duration;
	private String phoneNumber;
	private String contactName;
	private String status;

    public CI() {
    }

    public CI(long timestamp, int accuracy, String phoneNumber, String contactName, String status,
              long startTime, long endTime, long duration) {
        super(timestamp, accuracy, 0);
        this.startTime=startTime;
        this.endTime=endTime;
        this.duration=duration;
        this.setPhoneNumber(Utils.removeComma(phoneNumber));
        this.setContactName(Utils.removeComma(contactName));
        this.setStatus(Utils.removeComma(status));
    }

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		//PhoneCallInEvent,Enrico Bignotti,3,1478617807182,+393407864903,1478617803454,CALL_LOST,1478617807182
		return this.getClass().getSimpleName()+ Utils.SEPARATOR+
				getContactName()+Utils.SEPARATOR+
				getDuration()+Utils.SEPARATOR+
				Utils.longToStringFormat(getEndTime())+Utils.SEPARATOR+
				getPhoneNumber()+Utils.SEPARATOR+
				Utils.longToStringFormat(getStartTime())+Utils.SEPARATOR+
				getStatus()+Utils.SEPARATOR+
				Utils.longToStringFormat(getTimestamp());
	}
}
