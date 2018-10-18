package it.unitn.disi.witmee.sensorlog.model;

import android.util.Base64;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 22/05/13
 * Time: 18.03
 */

public class TA extends AbstractSensorEvent {



    long answertime = 0;
    long notifiedtime = 0;
    long time = 0;
    long delta = 0;
    long answerDuration = 0;
    String id = null;
    String answers = null;

    public TA(Task task) {
        this.notifiedtime= task.getNotifiedTime();
        this.time = task.getInstanceTime();
        this.answertime = task.getInstanceTime();
        this.delta=notifiedtime-notifiedtime;
        this.answerDuration = 0;
        this.answers = null;
        this.id =task.getInstanceid();
    }

    public TA(long taskTime, long answerTime, long notifiedTime, long answerduration, String answers, String id) {
        this.notifiedtime= notifiedTime;
        this.time = taskTime;
        this.answertime = answerTime;
        this.delta=answertime-notifiedtime;
        this.answerDuration = answerduration;
        this.answers= Base64.encodeToString(answers.getBytes(), Base64.NO_WRAP);
        this.id = id;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        //Answer,4581,19369,54154,1730,Lesson,Home,Friends,8740566,1478887346510,1478896087076
        return this.getClass().getSimpleName()+Utils.SEPARATOR+
                getAnswers()+Utils.SEPARATOR+
                getAnswerDuration()+Utils.SEPARATOR+
                getDelta()+Utils.SEPARATOR+
                getId()+Utils.SEPARATOR+
                Utils.longToStringFormat(getNotifiedtime())+Utils.SEPARATOR+
                Utils.longToStringFormat(getTime())+Utils.SEPARATOR+
                Utils.longToStringFormat(getAnswertime());

        //QA,W3sic3VicXVlc3Rpb25pZCI6NCwiY29uY2VwdGlkIjo1NTkxNSwic3RyaW5nIjoiQXVsYXMgdHVkaW8ifSx7InN1YnF1ZXN0aW9uaWQiOjEsImNvbmNlcHRpZCI6MzE0MjgsInN0cmluZyI6IlN0dWRpbyJ9LHsic3VicXVlc3Rpb25pZCI6NiwiY29uY2VwdGlkIjoxMTUwOTMsInN0cmluZyI6IkVycm9yZSJ9LHsic3VicXVlc3Rpb25pZCI6NSwiY29uY2VwdGlkIjo1NDE1NCwic3RyaW5nIjoiVHV0dG8gb2sifSx7InN1YnF1ZXN0aW9uaWQiOjIsImNvbmNlcHRpZCI6OTk5MzIsInN0cmluZyI6IkFsbCBhcGVydG8ifSx7InN1YnF1ZXN0aW9uaWQiOjMsImNvbmNlcHRpZCI6MjQ4MTIsInN0cmluZyI6IlRyZW5vIn1d,8154,13711,,20170524073028386,20170524073026488,20170524073042097
    }
}
