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

public class Task extends Contribution {

    public Task(long taskTime, long notifiedTime, String subtasks, String taskid, String status, String task, long validityFor, String synchronization) {
        super(taskTime, notifiedTime, subtasks, taskid, status, task, validityFor, synchronization);
    }
}
