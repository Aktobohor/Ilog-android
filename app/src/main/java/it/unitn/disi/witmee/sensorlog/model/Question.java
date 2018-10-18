package it.unitn.disi.witmee.sensorlog.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 22/05/13
 * Time: 18.03
 */

public class Question extends Contribution {

    public static final String STATUS_RECEIVED = "received";
    public static final String STATUS_ANSWERED = "answered";
    public static final String STATUS_EXPIRED = "expired";

    public Question(long questionTime, long notifiedTime, long validityFor, String questionid, String subquestions, String status, String title, String synchronization) {
        super(questionTime, notifiedTime, subquestions, questionid, status, title, validityFor, synchronization);
    }
}
