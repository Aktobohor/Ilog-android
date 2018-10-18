package it.unitn.disi.witmee.sensorlog.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;

/**
 * Created by mattiazeni on 14/05/2018.
 */

public class Message implements Serializable {

    public static String SYNCHRONIZATION_FALSE = "false";
    public static String SYNCHRONIZATION_TRUE = "true";

    public static final String STATUS_READ = "read";
    public static final String STATUS_UNREAD = "unread";
    public static final String STATUS_EXPIRED = "expired";

    long receivedTimestamp = 0;
    long notifiedTime = 0;
    long validityFor = 0;

    String messageid = null;
    String content = null;
    String status = null;
    String title = null;

    String synchronization = null;

    public Message(long receivedTimestamp, long notifiedTime, long validityFor, String messageid, String content, String status, String title, String synchronization) {
        this.receivedTimestamp = receivedTimestamp;
        this.notifiedTime = notifiedTime;
        this.validityFor = validityFor;
        this.messageid = messageid;
        this.content = content;
        this.status = status;
        this.title = title;
        this.synchronization = synchronization;
    }

    public String getSynchronization() {
        return synchronization;
    }

    public void setSynchronization(String synchronization) {
        this.synchronization = synchronization;
    }

    public long getMessageTimestamp() {
        return receivedTimestamp;
    }

    public void setMessageTimestamp(long receivedTimestamp) {
        this.receivedTimestamp = receivedTimestamp;
    }

    public long getNotifiedTime() {
        return notifiedTime;
    }

    public void setNotifiedTime(long notifiedTime) {
        this.notifiedTime = notifiedTime;
    }

    public long getValidityFor() {
        return validityFor;
    }

    public void setValidityFor(long validityFor) {
        this.validityFor = validityFor;
    }

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        try {
            return iLogApplication.getLocalizedContent(new JSONObject(this.getContent()).getJSONArray("dsc"));
        } catch (JSONException e) {
            e.printStackTrace();
            return "Description";
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("notifiedtime", this.getNotifiedTime());
        jsonObject.put("instancetime", this.getMessageTimestamp());
        jsonObject.put("instanceid", this.getMessageid());
        return jsonObject;
    }
}
