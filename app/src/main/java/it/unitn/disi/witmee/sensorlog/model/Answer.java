package it.unitn.disi.witmee.sensorlog.model;

import android.util.Base64;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 22/05/13
 * Time: 18.03
 */

public class Answer extends AbstractSensorEvent {

    public static final String ANSWERTIME = "answertime";
    public static final String TYPE_TIMEDIARY = "timediary";
    public static final String TYPE_TASK = "task";
    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_CHALLENGE = "challenge";

    public static String SYNCHRONIZATION_FALSE = "false";
    public static String SYNCHRONIZATION_TRUE = "true";

    long answertime = 0;
    long notifiedtime = 0;
    long instancetime = 0;
    long delta = 0;
    long answerDuration = 0;
    String instanceid = null;
    JSONArray answer = null;
    JSONArray payload = null;
    String type = null;
    String answersSynchronization = "false";
    String payloadSynchronization = "false";

    public Answer(long instancetime, long answerTime, long notifiedTime, long answerduration, JSONArray answer, JSONArray payload, String instanceid, String type, String answersSynchronization, String payloadSynchronization) {
        this.notifiedtime= notifiedTime;
        this.instancetime = instancetime;
        this.answertime = answerTime;
        this.delta=answertime-notifiedtime;//milliseconds
        this.answerDuration = answerduration;
        this.answer = answer;
        //this.payload= Base64.encodeToString(payload.getBytes(), Base64.NO_WRAP);
        this.payload= payload;
        this.instanceid = instanceid;
        this.type = type;
        this.answersSynchronization = answersSynchronization;
        this.payloadSynchronization = payloadSynchronization;
    }

    public Answer(Answer answer) {
        this.notifiedtime = answer.getNotifiedtime();
        this.instancetime = answer.getInstancetime();
        this.answertime = answer.getAnswertime();
        this.delta = answer.getDelta();
        this.answerDuration = answer.getAnswerDuration();
        this.answer = answer.getAnswer();
        try {
            this.payload = modifyPicturePayload(new JSONArray(answer.getPayload().toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.instanceid = answer.getInstanceid();
        this.type = answer.getType();
        this.answersSynchronization = answer.getAnswersSynchronization();
        this.payloadSynchronization = answer.getPayloadSynchronization();
    }

    private JSONArray modifyPicturePayload(JSONArray answersPayload) {
        for(int index=0; index<answersPayload.length();index++) {
            try {
                JSONObject payload = answersPayload.getJSONObject(index).getJSONObject("payload");
                String picture = payload.getString("picture");
                File file = new File(iLogApplication.getAppContext().getFilesDir() + "/" + picture);
                payload.put("picture", Base64.encodeToString(FileUtils.readFileToByteArray(file), Base64.NO_WRAP));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return answersPayload;
    }

    public String getAnswersSynchronization() {
        return answersSynchronization;
    }

    public void setAnswersSynchronization(String answersSynchronization) {
        this.answersSynchronization = answersSynchronization;
    }

    public String getPayloadSynchronization() {
        return payloadSynchronization;
    }

    public void setPayloadSynchronization(String payloadSynchronization) {
        this.payloadSynchronization = payloadSynchronization;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getAnswertime() {
        return answertime;
    }

    public void setAnswertime(long answertime) {
        this.answertime = answertime;
    }

    public long getNotifiedtime() {
        return notifiedtime;
    }

    public void setNotifiedtime(long notifiedtime) {
        this.notifiedtime = notifiedtime;
    }

    public long getInstancetime() {
        return instancetime;
    }

    public void setInstancetime(long instancetime) {
        this.instancetime = instancetime;
    }

    public long getDelta() {
        return delta;
    }

    public void setDelta(long delta) {
        this.delta = delta;
    }

    public long getAnswerDuration() {
        return answerDuration;
    }

    public void setAnswerDuration(long answerDuration) {
        this.answerDuration = answerDuration;
    }

    public JSONArray getAnswer() {
        return answer;
    }

    public void setAnswer(JSONArray answer) {
        this.answer = answer;
    }

    public JSONArray getPayload() {
        return payload;
    }

    public void setPayload(JSONArray payload) {
        this.payload = payload;
    }

    public String getInstanceid() {
        return instanceid;
    }

    public void setQuestionid(String questionid) {
        this.instanceid = questionid;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("answertime", this.getAnswertime());
        jsonObject.put("notifiedtime", this.getNotifiedtime());
        jsonObject.put("instancetime", this.getInstancetime());
        jsonObject.put("delta", this.getDelta());
        jsonObject.put("answerDuration", this.getAnswerDuration());
        jsonObject.put("instanceid", this.getInstanceid());
        jsonObject.put("answers", this.getAnswer());
        jsonObject.put("payload", this.getPayload());
        jsonObject.put("type", this.getType());
        return jsonObject;
    }

    public static JSONArray generateSleepAnswer() {
        JSONArray list = new JSONArray();

        try {
            JSONObject object = new JSONObject();
            object.put("qid", 1);
            object.put("cid", 74549);
            object.put("cnt", "Expired");
            list.put(object);
            object = new JSONObject();
            object.put("qid", 2);
            object.put("cid", -1);
            object.put("cnt", "null");
            list.put(object);
            object = new JSONObject();
            object.put("qid", 4);
            object.put("cid", -1);
            object.put("cnt", "null");
            list.put(object);
            object = new JSONObject();
            object.put("qid", 5);
            object.put("cid", -1);
            object.put("cnt", "0");
            list.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static JSONArray generateSleepPayload() {
        JSONArray list = new JSONArray();

        try {
            JSONObject object = new JSONObject();
            object.put("qid", 1);
            object.put("payload", new JSONObject());
            list.put(object);
            object = new JSONObject();
            object.put("qid", 2);
            object.put("payload", new JSONObject());
            list.put(object);
            object = new JSONObject();
            object.put("qid", 4);
            object.put("payload", new JSONObject());
            list.put(object);
            object = new JSONObject();
            object.put("qid", 5);
            object.put("payload", new JSONObject());
            list.put(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
