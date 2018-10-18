package it.unitn.disi.witmee.sensorlog.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 22/05/13
 * Time: 18.03
 */

public class Contribution implements Serializable {

    public static String SYNCHRONIZATION_FALSE = "false";
    public static String SYNCHRONIZATION_TRUE = "true";

    public static final String STATUS_SOLVED = "solved";
    public static final String STATUS_UNSOLVED = "unsolved";
    public static final String STATUS_EXPIRED = "expired";

    long instanceTime = 0;
    long notifiedTime = 0;
    long validityFor = 0;

    String instanceid = null;
    String content = null;
    String status = null;
    String title = null;

    String synchronization = null;

    public Contribution(long instanceTime, long notifiedTime, String content, String instanceid, String status, String title, long validityFor, String synchronization) {
        this.notifiedTime = notifiedTime;
        this.instanceTime = instanceTime;
        this.content = content.toString();
        this.instanceid = instanceid;
        this.status = status;
        this.title = title;
        this.validityFor = validityFor;
        this.synchronization = synchronization;
    }

    public String getSynchronization() {
        return synchronization;
    }

    public void setSynchronization(String synchronization) {
        this.synchronization = synchronization;
    }

    public long getValidityFor() {
        return validityFor;
    }

    public void setValidityFor(long validityFor) {
        this.validityFor = validityFor;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getInstanceTime() {
        return instanceTime;
    }

    public String getInstanceTimeString() {
        return new SimpleDateFormat("yyyy-MM-dd").format(getInstanceTime());
    }

    public void setInstanceTime(long instanceTime) {
        this.instanceTime = instanceTime;
    }

    public long getNotifiedTime() {
        return notifiedTime;
    }

    public String getNotifiedTimeString() {
        return new SimpleDateFormat("yyyy-MM-dd").format(getNotifiedTime());
    }

    public void setNotifiedTime(long notifiedTime) {
        this.notifiedTime = notifiedTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getInstanceid() {
        return instanceid;
    }

    public void setInstanceid(String instanceid) {
        this.instanceid = instanceid;
    }

    @Override
    public String toString() {
        return String.valueOf(this.getInstanceTime());
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("notifiedtime", this.getNotifiedTime());
        jsonObject.put("instancetime", this.getInstanceTime());
        jsonObject.put("instanceid", this.getInstanceid());
        return jsonObject;
    }
}
