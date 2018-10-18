package it.unitn.disi.witmee.sensorlog.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created by mattiazeni on 14/05/2018.
 */

public class Challenge extends Contribution {

    public static String STATUS_EXPIRED = "expired";
    public static String STATUS_ONGOING = "ongoing";
    public static String STATUS_COMPLETED = "completed";

    public static String SYNCHRONIZATION_FALSE = "false";
    public static String SYNCHRONIZATION_TRUE = "true";

    public static String TYPE_STATIC = "static";
    public static String TYPE_DYNAMIC = "dynamic";
    public static String TYPE_BACKGROUND = "background";
    public static String TYPE_FREEROAM = "freeroam";

    public static String INTERVAL_SECONDS = "seconds";
    public static String INTERVAL_MINUTES = "minutes";
    public static String INTERVAL_HOURS = "hours";
    public static String INTERVAL_DAYS = "days";

    public static String RESULT_PENDING = "pending";
    public static String RESULT_VERIFIED = "verified";
    public static String RESULT_REJECTED = "rejected";

    String definitionid;
    String status = STATUS_ONGOING;
    String project;
    String startdate;
    String enddate;
    String location;
    String target;
    String type;
    String name;
    String description;
    String instructions;
    String result;
    int pointsawarded;
    int pointpercontribution;
    String constraints;
    String participationtime;
    String completiontime;
    String participationSynchronization;

    public Challenge(String instanceid, String definitionid, String project, String startdate, String enddate, String location, String target, String type, String name, String description, String instructions, int pointsawarded, int pointpercontribution, String constraints, String content, String participationtime, String completiontime, String result) {
        super(0, 0, content, instanceid, STATUS_ONGOING, name, 0, SYNCHRONIZATION_FALSE);
        this.instanceid = instanceid;
        this.definitionid = definitionid;
        this.project = project;
        this.startdate = startdate;
        this.enddate = enddate;
        this.location = location;
        this.target = target;
        this.type = type;
        this.name = name;
        this.description = description;
        this.instructions = instructions;
        this.pointsawarded = pointsawarded;
        this.pointpercontribution = pointpercontribution;
        this.constraints = constraints;
        this.content = content;
        this.participationtime = participationtime;
        this.completiontime = completiontime;
        this.result = result;
    }

    public Challenge(String instanceid, String definitionid, String status, String synchronization, String project, String startdate, String enddate, String location, String target, String type, String name, String description, String instructions, int pointsawarded, int pointpercontribution, String constraints, String content, String participationtime, String completiontime, String result) {
        super(0, 0, content, instanceid, STATUS_ONGOING, name, 0, SYNCHRONIZATION_FALSE);
        this.instanceid = instanceid;
        this.definitionid = definitionid;
        this.status = status;
        this.synchronization = synchronization;
        this.project = project;
        this.startdate = startdate;
        this.enddate = enddate;
        this.location = location;
        this.target = target;
        this.type = type;
        this.name = name;
        this.description = description;
        this.instructions = instructions;
        this.pointsawarded = pointsawarded;
        this.pointpercontribution = pointpercontribution;
        this.constraints = constraints;
        this.content = content;
        this.participationtime = participationtime;
        this.completiontime = completiontime;
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCompletiontime() {
        return completiontime;
    }

    public String getParticipationSynchronization() {
        return participationSynchronization;
    }

    public void setParticipationSynchronization(String participationSynchronization) {
        this.participationSynchronization = participationSynchronization;
    }

    public void setCompletiontime(String completiontime) {
        this.completiontime = completiontime;
    }

    public String getInstanceid() {
        return instanceid;
    }

    public void setInstanceid(String instanceid) {
        this.instanceid = instanceid;
    }

    public String getDefinitionid() {
        return definitionid;
    }

    public void setDefinitionid(String definitionid) {
        this.definitionid = definitionid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSynchronization() {
        return synchronization;
    }

    public void setSynchronization(String synchronization) {
        this.synchronization = synchronization;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public long getEnddateAsLong() {
        return Utils.stringToLongFormat(enddate);
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getPointsawarded() {
        return pointsawarded;
    }

    public void setPointsawarded(int pointsawarded) {
        this.pointsawarded = pointsawarded;
    }

    public int getPointpercontribution() {
        return pointpercontribution;
    }

    public void setPointpercontribution(int pointpercontribution) {
        this.pointpercontribution = pointpercontribution;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getParticipationtime() {
        return participationtime;
    }

    public void setParticipationtime(String participationtime) {
        this.participationtime = participationtime;
    }

    @Override
    public int hashCode() {
        return this.getInstanceid().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Challenge)) {
            return false;
        }

        Challenge challenge = (Challenge) obj;
        return challenge.getInstanceid().equals(this.getInstanceid());
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("instanceid", this.getInstanceid());
        jsonObject.put("definitionid", this.getDefinitionid());
        jsonObject.put("status", this.getStatus());
        jsonObject.put("synchronization", this.getSynchronization());
        jsonObject.put("project", this.getProject());
        jsonObject.put("startdate", this.getStartdate());
        jsonObject.put("enddate", this.getEnddate());
        jsonObject.put("location", this.getLocation());
        jsonObject.put("target", this.getTarget());
        jsonObject.put("type", this.getType());
        jsonObject.put("name", this.getName());
        jsonObject.put("description", this.getDescription());
        jsonObject.put("instructions", this.getInstructions());
        jsonObject.put("pointsawarded", this.getPointsawarded());
        jsonObject.put("pointpercontribution", this.getPointpercontribution());
        jsonObject.put("constraints", this.getConstraints());
        jsonObject.put("content", this.getContent());
        jsonObject.put("participationtime", this.getParticipationtime());
        jsonObject.put("completiontime", this.getCompletiontime());
        jsonObject.put("result", this.getResult());
        return jsonObject;
    }
}
