package it.unitn.disi.witmee.sensorlog.model.virtual;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Mattia
 * Date: 23/05/13
 * Time: 18.27
 */
public class CO extends AbstractSensorEvent {

    public static final String START_TIME = "starttime";
	public static final String END_TIME = "endtime";
	public static final String DURATION = "duration";
	public static final String PHONE_NUMBER = "phonenumber";
	public static final String PHONE_NAME = "phonename";
	
    /**
     * Acceleration minus Gx on the x-axis
     */
    private long startTime;
	private long endTime;
	private long duration;
	private String phoneNumber;
	private String contactName;

    public CO() {
    }

    public CO(long timestamp, int accuracy, String phoneNumber, String contactName, long startTime, long endTime, long duration) {
        super(timestamp, accuracy, 0);
        this.startTime=startTime;
        this.endTime=endTime;
        this.duration=duration;
        this.setPhoneNumber(Utils.removeComma(phoneNumber));
        this.setContactName(Utils.removeComma(contactName));
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

	@Override
	public String toString() {
		//PhoneCallOutEvent,Enrico Bignotti,8,1478617738843,+393407864903,1478617730590,1478617738843
		return this.getClass().getSimpleName()+ Utils.SEPARATOR+
				getContactName()+Utils.SEPARATOR+
				getDuration()+Utils.SEPARATOR+
				Utils.longToStringFormat(getEndTime())+Utils.SEPARATOR+
				getPhoneNumber()+Utils.SEPARATOR+
				Utils.longToStringFormat(getStartTime())+Utils.SEPARATOR+
				Utils.longToStringFormat(getTimestamp());
	}
}
