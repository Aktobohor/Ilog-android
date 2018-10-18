package it.unitn.disi.witmee.sensorlog.model.virtual;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Mattia
 * Date: 23/05/13
 * Time: 18.27
 */
public class SI extends AbstractSensorEvent {

	public static final String PHONE_NUMER = "phonenumber";
	public static final String CONTACT_NUMBER = "contactnumber";
	public static final String PREVIEW = "preview";
	public static final String SMS_TEXT = "smstext";
	
    /**
     * Acceleration minus Gx on the x-axis
     */
    private String phoneNumber;
    private String contactName;

    public SI(long timestamp, int accuracy, String phoneNumber, String contactName) {
        super(timestamp, accuracy, 0);
        this.phoneNumber= Utils.removeComma(phoneNumber);
        this.contactName=Utils.removeComma(contactName);
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
		//SmsInEvent,Enrico Bignotti,+393407864903,1478617813242
		return this.getClass().getSimpleName()+ Utils.SEPARATOR+
				getContactName()+Utils.SEPARATOR+
				getPhoneNumber()+Utils.SEPARATOR+
				Utils.longToStringFormat(getTimestamp());
	}
}
