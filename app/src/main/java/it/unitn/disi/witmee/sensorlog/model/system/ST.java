package it.unitn.disi.witmee.sensorlog.model.system;

import android.os.Build;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Mattia
 * Date: 09/06/13
 * Time: 11.33
 */

public class ST extends AbstractSensorEvent {

    public static String EVENT_ARCHIVE = "event_archive";
    public static String EVENT_UPLOAD_SUCCESS = "event_upload_success";
    public static String EVENT_UPLOAD_ERROR = "event_upload_error";
    public static String EVENT_NEWQUESTION = "event_newquestion";
    public static String EVENT_NEWTASK = "event_newtask";
    public static String EVENT_NEWMESSAGE = "event_newmessage";
    public static String EVENT_SERVICE_STARTED = "event_service_started";
    public static String EVENT_SERVICE_STOPPED = "event_service_stopped";
    public static String EVENT_ANSWER = "event_answer";
    public static String EVENT_TASK_SOLVED = "event_task_solved";
    public static String EVENT_EMPTY_ANSWER = "event_empty_answer";
    public static String EVENT_CRASH = "event_crash";
    public static String EVENT_UPDATE = "event_update";
    public static String EVENT_UPDATE_DOWNLOADED = "event_update_downloaded";

    private String appVersion = "";
    private String OSVersion = "";
    private String deviceModel = "";
    private String manufacturer = "";
    private long timeFromStart = 0;
    private String eventType = "";
    private String eventPayload = "";

    /*
    - Archive (# of lines)
    - Upload
    - New question
    - Start service
    - Stop service
    - Question answer
    - Question gone
    - Crash
    */

    public ST(String eventType, String eventPayload) {
        super(System.currentTimeMillis(), 0, 0);

        this.appVersion = iLogApplication.getAppVersion();
        this.OSVersion = Utils.removeComma(Build.VERSION.RELEASE);
        this.deviceModel = Utils.removeComma(Build.MODEL);
        this.manufacturer = Utils.removeComma(Build.MANUFACTURER);
        this.timeFromStart = System.currentTimeMillis()-iLogApplication.sharedPreferences.getLong(Utils.CONFIG_APP_STARTED_TIME, System.currentTimeMillis());
        this.eventType = Utils.removeComma(eventType);
        this.eventPayload = Utils.removeComma(eventPayload);
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getOSVersion() {
        return OSVersion;
    }

    public void setOSVersion(String OSVersion) {
        this.OSVersion = OSVersion;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public long getTimeFromStart() {
        return timeFromStart;
    }

    public void setTimeFromStart(long timeFromStart) {
        this.timeFromStart = timeFromStart;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventPayload() {
        return eventPayload;
    }

    public void setEventPayload(String eventPayload) {
        this.eventPayload = eventPayload;
    }

    @Override
    public String toString() {
        //ST,7.1.1,1.2.6,ONEPLUS A3003,CO,event_service_started,OnePlus,81733,2017-05-19 11:14:52.000
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getOSVersion()+Utils.SEPARATOR+
                getAppVersion()+Utils.SEPARATOR+
                getDeviceModel()+Utils.SEPARATOR+
                getEventPayload()+Utils.SEPARATOR+
                getEventType()+Utils.SEPARATOR+
                getManufacturer()+Utils.SEPARATOR+
                getTimeFromStart()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
