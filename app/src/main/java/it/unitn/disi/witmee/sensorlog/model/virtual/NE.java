package it.unitn.disi.witmee.sensorlog.model.virtual;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Mattia
 * Date: 23/05/13
 * Time: 18.27
 */
public class NE extends AbstractSensorEvent {

    public static final String NOTIFICATION_POSTED = "notification_posted";
    public static final String NOTIFICATION_REMOVED = "notification_removed";

    private String notificationStatus;
    private String nodificationId;
    private String notificationTickerText;
    private String notificationPackage;
    private String notificationTimestamp;
    private boolean notificationIsClearable;
    private boolean notificationIsOngoing;
    private String notificationContent;

    public NE(String notificationStatus, String nodificationId, String notificationTickerText, String notificationPackage, long notificationTimestamp, boolean notificationIsClearable, boolean notificationIsOngoing, String notificationContent) {
        this.notificationStatus = notificationStatus;
        this.nodificationId = nodificationId;
        if(notificationTickerText!=null) {
            //this.notificationTickerText = Utils.removeComma(notificationTickerText);
            this.notificationTickerText="";
        }
        else {
            this.notificationTickerText="";
        }
        this.notificationPackage = notificationPackage;
        this.notificationTimestamp = Utils.longToStringFormat(notificationTimestamp);
        this.notificationIsClearable = notificationIsClearable;
        this.notificationIsOngoing = notificationIsOngoing;
        //this.notificationContent = notificationContent;
        this.notificationContent = "";
    }

    public String getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(String notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public String getNodificationId() {
        return nodificationId;
    }

    public void setNodificationId(String nodificationId) {
        this.nodificationId = nodificationId;
    }

    public String getNotificationTickerText() {
        return notificationTickerText;
    }

    public void setNotificationTickerText(String notificationTickerText) {
        this.notificationTickerText = notificationTickerText;
    }

    public String getNotificationPackage() {
        return notificationPackage;
    }

    public void setNotificationPackage(String notificationPackage) {
        this.notificationPackage = notificationPackage;
    }

    public String getNotificationTimestamp() {
        return notificationTimestamp;
    }

    public void setNotificationTimestamp(String notificationTimestamp) {
        this.notificationTimestamp = notificationTimestamp;
    }

    public boolean isNotificationIsClearable() {
        return notificationIsClearable;
    }

    public void setNotificationIsClearable(boolean notificationIsClearable) {
        this.notificationIsClearable = notificationIsClearable;
    }

    public boolean isNotificationIsOngoing() {
        return notificationIsOngoing;
    }

    public void setNotificationIsOngoing(boolean notificationIsOngoing) {
        this.notificationIsOngoing = notificationIsOngoing;
    }

    public String getNotificationContent() {
        return notificationContent;
    }

    public void setNotificationContent(String notificationContent) {
        this.notificationContent = notificationContent;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                notificationStatus + Utils.SEPARATOR+
                nodificationId + Utils.SEPARATOR+
                notificationTickerText + Utils.SEPARATOR+
                notificationPackage + Utils.SEPARATOR+
                notificationContent + Utils.SEPARATOR+
                notificationIsClearable + Utils.SEPARATOR+
                notificationIsOngoing + Utils.SEPARATOR+
                notificationTimestamp;
    }
}
