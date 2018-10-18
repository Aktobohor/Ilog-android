package it.unitn.disi.witmee.sensorlog.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.Answer;
import it.unitn.disi.witmee.sensorlog.model.Challenge;
import it.unitn.disi.witmee.sensorlog.model.Message;
import it.unitn.disi.witmee.sensorlog.model.Question;
import it.unitn.disi.witmee.sensorlog.model.Task;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Database helper class that manages all the operations done on the database like table creations, schema updates, writes, reads, among others.
 * It extends {@link SQLiteOpenHelper}.
 * As of July 2018 we have five main tables, that store the following objects:<br>
 * <ul>
 *     <li>
 *         {@link Message}
 *     </li>
 *     <li>
 *         {@link Question}
 *     </li>
 *     <li>
 *         {@link Task}
 *     </li>
 *     <li>
 *         {@link Challenge}
 *     </li>
 *     <li>
 *         {@link Answer}
 *     </li>
 * </ul>
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = DatabaseHelper.class.getName();

    // Database Version
    private static final int DATABASE_VERSION = 34;

    // Database Name
    private static final String DATABASE_NAME = "userData";

    // Table Names
    private static final String TABLE_MESSAGES = "messages";
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_TIMEDIARIES = "timediaries";
    private static final String TABLE_CHALLENGES = "challenges";
    private static final String TABLE_ANSWERS = "answers";

    /**
    Column names - {@link Message}
     */
    private static final String KEY_MESSAGE_ID = "id";
    private static final String KEY_MESSAGE_CONTENT = "messagescontent";
    private static final String KEY_MESSAGE_TITLE = "messagetitle";
    private static final String KEY_MESSAGE_TIME = "messagetime";
    private static final String KEY_MESSAGE_MESSAGEID = "messageid";
    private static final String KEY_MESSAGE_NOTIFIED_TIME = "messagenotifiedtime";
    private static final String KEY_MESSAGE_VALIDITY_UNTIL = "validityuntil";
    private static final String KEY_MESSAGE_STATUS = "status";
    private static final String KEY_MESSAGE_SYNCHRONIZATION = "synchronization";

    /**
     Column names - {@link Question}
     */
    private static final String KEY_TIMEDIARIES_ID = "id";
    private static final String KEY_TIMEDIARIES_TIME = "questiontime";
    private static final String KEY_TIMEDIARIES_NOTIFIEDTIME = "notifiedtime";
    private static final String KEY_TIMEDIARIES_QUESTIONID = "questionid";
    private static final String KEY_TIMEDIARIES_SUBQUESTIONS = "subquestions";
    private static final String KEY_TIMEDIARIES_STATUS = "status";
    private static final String KEY_TIMEDIARIES_TITLE = "task";
    private static final String KEY_TIMEDIARIES_VALIDITY_UNTIL = "validityuntil";
    private static final String KEY_TIMEDIARIES_SYNCHRONIZATION = "synchronization";

    /**
     Column names - {@link Task}
     */
    private static final String KEY_TASK_ID = "id";
    private static final String KEY_TASK_TIME = "tasktime";
    private static final String KEY_TASK_NOTIFIEDTIME = "notifiedtime";
    private static final String KEY_TASK_TASKID = "taskid";
    private static final String KEY_TASK_SUBTASKS = "subtasks";
    private static final String KEY_TASK_STATUS = "status";
    private static final String KEY_TASK = "task";
    private static final String KEY_TASK_VALIDITY_UNTIL = "validityuntil";
    private static final String KEY_TASK_SYNCHRONIZATION = "synchronization";

    /**
     Column names - {@link Challenge}
     */
    private static final String KEY_CHALLENGE_ID = "id";
    private static final String KEY_CHALLENGE_INSTANCEID = "instanceid";
    private static final String KEY_CHALLENGE_DEFINITIONID = "definitionid";
    private static final String KEY_CHALLENGE_STATUS = "status";
    private static final String KEY_CHALLENGE_SYNCHRONIZATION = "synchronization";
    private static final String KEY_CHALLENGE_PROJECT = "project";
    private static final String KEY_CHALLENGE_STARTDATE = "startdate";
    private static final String KEY_CHALLENGE_ENDDATE = "enddate";
    private static final String KEY_CHALLENGE_LOCATION = "location";
    private static final String KEY_CHALLENGE_TARGET = "target";
    private static final String KEY_CHALLENGE_TYPE = "type";
    private static final String KEY_CHALLENGE_NAME = "name";
    private static final String KEY_CHALLENGE_DESCRIPTION = "description";
    private static final String KEY_CHALLENGE_INSTRUCTIONS = "instructions";
    private static final String KEY_CHALLENGE_POINTSAWARDED = "pointsawarded";
    private static final String KEY_CHALLENGE_POINTSPERCONTRIBUTION = "pointpercontribution";
    private static final String KEY_CHALLENGE_CONSTRAINTS = "constraints";
    private static final String KEY_CHALLENGE_CONTENT = "content";
    private static final String KEY_CHALLENGE_PARTICIPATION_TIME = "participationtime";
    private static final String KEY_CHALLENGE_RESULT = "result";
    private static final String KEY_CHALLENGE_COMPLETION_TIME = "completiontime";

    /**
     Column names - {@link Answer}
     */
    private static final String KEY_ANSWER_ID = "id";
    private static final String KEY_ANSWER_INSTANCEID = "instanceid";
    private static final String KEY_ANSWER_CONTENT = "content";
    private static final String KEY_ANSWER_PAYLOAD = "payload";
    private static final String KEY_ANSWER_ANSWERTIME = "answertime";
    private static final String KEY_ANSWER_NOTIFIEDTIME = "notifiedtime";
    private static final String KEY_ANSWER_INSTANCETIME = "time";
    private static final String KEY_ANSWER_DELTA = "delta";
    private static final String KEY_ANSWER_TYPE = "type";
    private static final String KEY_ANSWER_ANSWERDURATION = "answerduration";
    private static final String KEY_ANSWER_ANSWER_SYNCHRONIZATION = "answer_synchronization";
    private static final String KEY_ANSWER_PAYLOAD_SYNCHRONIZATION = "payload_synchronization";

    /**
     Tag Table Create - {@link Message}
     */
    private static final String CREATE_TABLE_MESSAGES = "CREATE TABLE " +
            TABLE_MESSAGES + "(" + KEY_MESSAGE_ID + " INTEGER PRIMARY KEY," +
            KEY_MESSAGE_TIME + " INTEGER," +
            KEY_MESSAGE_NOTIFIED_TIME + " INTEGER," +
            KEY_MESSAGE_MESSAGEID + " TEXT," +
            KEY_MESSAGE_VALIDITY_UNTIL + " INTEGER," +
            KEY_MESSAGE_STATUS + " TEXT," +
            KEY_MESSAGE_TITLE + " TEXT," +
            KEY_MESSAGE_SYNCHRONIZATION + " TEXT," +
            KEY_MESSAGE_CONTENT + " TEXT" +")";

    /**
     Tag Table Create - {@link Question}
     */
    private static final String CREATE_TABLE_TIMEDIARIES = "CREATE TABLE " +
            TABLE_TIMEDIARIES + "(" + KEY_TIMEDIARIES_ID + " INTEGER PRIMARY KEY," +
            KEY_TIMEDIARIES_TIME + " INTEGER," +
            KEY_TIMEDIARIES_NOTIFIEDTIME + " INTEGER," +
            KEY_TIMEDIARIES_QUESTIONID + " TEXT," +
            KEY_TIMEDIARIES_STATUS + " TEXT," +
            KEY_TIMEDIARIES_TITLE + " TEXT," +
            KEY_TIMEDIARIES_VALIDITY_UNTIL + " INTEGER," +
            KEY_TIMEDIARIES_SYNCHRONIZATION + " TEXT," +
            KEY_TIMEDIARIES_SUBQUESTIONS + " TEXT" +")";

    /**
     Tag Table Create - {@link Task}
     */
    private static final String CREATE_TABLE_TASKS = "CREATE TABLE " +
            TABLE_TASKS + "(" + KEY_TASK_ID + " INTEGER PRIMARY KEY," +
            KEY_TASK_TIME + " INTEGER," +
            KEY_TASK_NOTIFIEDTIME + " INTEGER," +
            KEY_TASK_TASKID + " TEXT," +
            KEY_TASK_VALIDITY_UNTIL + " INTEGER," +
            KEY_TASK_STATUS + " TEXT," +
            KEY_TASK + " TEXT," +
            KEY_TASK_SYNCHRONIZATION + " TEXT," +
            KEY_TASK_SUBTASKS + " TEXT" +")";

    /**
     Tag Table Create - {@link Challenge}
     */
    private static final String CREATE_TABLE_CHALLENGES = "CREATE TABLE " +
            TABLE_CHALLENGES + "(" + KEY_CHALLENGE_ID + " INTEGER PRIMARY KEY," +
            KEY_CHALLENGE_DEFINITIONID + " TEXT," +
            KEY_CHALLENGE_INSTANCEID + " TEXT," +
            KEY_CHALLENGE_STATUS + " TEXT," +
            KEY_CHALLENGE_SYNCHRONIZATION + " TEXT," +
            KEY_CHALLENGE_PROJECT + " TEXT," +
            KEY_CHALLENGE_STARTDATE + " TEXT," +
            KEY_CHALLENGE_ENDDATE + " TEXT," +
            KEY_CHALLENGE_LOCATION + " TEXT," +
            KEY_CHALLENGE_TARGET + " TEXT," +
            KEY_CHALLENGE_TYPE + " TEXT," +
            KEY_CHALLENGE_NAME + " TEXT," +
            KEY_CHALLENGE_DESCRIPTION + " TEXT," +
            KEY_CHALLENGE_INSTRUCTIONS + " TEXT," +
            KEY_CHALLENGE_PARTICIPATION_TIME + " TEXT," +
            KEY_CHALLENGE_COMPLETION_TIME + " TEXT," +
            KEY_CHALLENGE_RESULT + " TEXT," +
            KEY_CHALLENGE_POINTSAWARDED + " INTEGER," +
            KEY_CHALLENGE_POINTSPERCONTRIBUTION + " INTEGER," +
            KEY_CHALLENGE_CONSTRAINTS + " TEXT," +
            KEY_CHALLENGE_CONTENT + " TEXT" +")";

    /**
     Tag Table Create - {@link Answer}
     */
    private static final String CREATE_TABLE_ANSWERS = "CREATE TABLE " +
            TABLE_ANSWERS + "(" + KEY_ANSWER_ID + " INTEGER PRIMARY KEY," +
            KEY_ANSWER_INSTANCEID + " TEXT," +
            KEY_ANSWER_CONTENT + " TEXT," +
            KEY_ANSWER_PAYLOAD + " TEXT," +
            KEY_ANSWER_ANSWER_SYNCHRONIZATION + " TEXT," +
            KEY_ANSWER_PAYLOAD_SYNCHRONIZATION + " TEXT," +
            KEY_ANSWER_TYPE + " TEXT," +
            KEY_ANSWER_ANSWERTIME + " INTEGER," +
            KEY_ANSWER_NOTIFIEDTIME + " INTEGER," +
            KEY_ANSWER_INSTANCETIME + " INTEGER," +
            KEY_ANSWER_DELTA + " INTEGER," +
            KEY_ANSWER_ANSWERDURATION + " INTEGER" +")";

    /**
     * Class constructure
     * @param context {@link Context}
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Method that creates the required tables
     * @param db database instance of type {@link SQLiteDatabase}
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MESSAGES);
        db.execSQL(CREATE_TABLE_TIMEDIARIES);
        db.execSQL(CREATE_TABLE_TASKS);
        db.execSQL(CREATE_TABLE_CHALLENGES);
        db.execSQL(CREATE_TABLE_ANSWERS);
    }

    /**
     * Method called when a change in the {@link #DATABASE_VERSION} occurs after a change in the schema. Every time a change in the schema is made it is mandatory
     * to increase the {@link #DATABASE_VERSION}
     * @param db db database instance of type {@link SQLiteDatabase}
     * @param oldVersion old version of the database
     * @param newVersion current version of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMEDIARIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHALLENGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANSWERS);

        // create new tables
        onCreate(db);
    }

    /**
     * Method that adds a new {@link Question} to the database
     * @param question {@link Question} object to be inserted into the database
     * @return the row ID of the newly inserted row, or -1 if an error occurred, for more details {@link SQLiteDatabase#insert(String, String, ContentValues)}
     */
    public long addQuestion(Question question) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIMEDIARIES_NOTIFIEDTIME, question.getNotifiedTime());
        values.put(KEY_TIMEDIARIES_TIME, question.getInstanceTime());
        values.put(KEY_TIMEDIARIES_QUESTIONID, question.getInstanceid());
        values.put(KEY_TIMEDIARIES_STATUS, question.getStatus());
        values.put(KEY_TIMEDIARIES_TITLE, question.getTitle());
        values.put(KEY_TIMEDIARIES_VALIDITY_UNTIL, question.getValidityFor());
        values.put(KEY_TIMEDIARIES_SUBQUESTIONS, question.getContent());
        values.put(KEY_TIMEDIARIES_SYNCHRONIZATION, question.getSynchronization());

        return db.insert(TABLE_TIMEDIARIES, null, values);
    }

    /**
     * Method used to retrieve all the {@link Question} objects from the database
     * @return Returns a list of {@link Question}
     */
    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<Question>();
        String selectQuery = "SELECT  * FROM " + TABLE_TIMEDIARIES;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                questions.add(new Question(c.getLong(c.getColumnIndex(KEY_TIMEDIARIES_TIME)), c.getLong(c.getColumnIndex(KEY_TIMEDIARIES_NOTIFIEDTIME)), c.getLong(c.getColumnIndex(KEY_TIMEDIARIES_VALIDITY_UNTIL)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_QUESTIONID)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_SUBQUESTIONS)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_STATUS)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_TITLE)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_SYNCHRONIZATION))));
            } while (c.moveToNext());
        }

        Collections.sort(questions, new Comparator<Question>() {
            @Override
            public int compare(Question o1, Question o2) {
                return o1.getInstanceTime() > o2.getInstanceTime() ? -1 : 1;
            }
        });

        c.close();

        return questions;
    }

    /**
     * Method that retrieves all the {@link Question} from the database filtering by the {@link #KEY_TIMEDIARIES_SYNCHRONIZATION} value
     * @param synchronization String that has to match for a question to be returned from the database
     * @return Returns a list of {@link Question} that match the {@link #KEY_TIMEDIARIES_SYNCHRONIZATION}
     */
    public List<Question> getAllQuestionsBySynchronization(String synchronization) {
        List<Question> questions = new ArrayList<Question>();

        String selectQuery = "SELECT  * FROM " + TABLE_TIMEDIARIES + " WHERE "
                + KEY_TIMEDIARIES_SYNCHRONIZATION + " = \"" + synchronization+"\"";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {
                    questions.add(new Question(c.getLong(c.getColumnIndex(KEY_TIMEDIARIES_TIME)), c.getLong(c.getColumnIndex(KEY_TIMEDIARIES_NOTIFIEDTIME)), c.getLong(c.getColumnIndex(KEY_TIMEDIARIES_VALIDITY_UNTIL)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_QUESTIONID)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_SUBQUESTIONS)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_STATUS)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_TITLE)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_SYNCHRONIZATION))));
                } while (c.moveToNext());
            }
        }

        c.close();

        return questions;
    }

    /**
     * Method that retrieves all the {@link Question} from the database filtering by the {@link #KEY_TIMEDIARIES_STATUS} value
     * @param status String that has to match for a question to be returned from the database
     * @return Returns a list of {@link Question} that match the {@link #KEY_TIMEDIARIES_STATUS}
     */
    public List<Question> getAllQuestionsByStatus(String status) {
        List<Question> questions = new ArrayList<Question>();

        String selectQuery = "SELECT  * FROM " + TABLE_TIMEDIARIES + " WHERE "
                + KEY_TIMEDIARIES_STATUS + " = \"" + status+"\"";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {
                    questions.add(new Question(c.getLong(c.getColumnIndex(KEY_TIMEDIARIES_TIME)), c.getLong(c.getColumnIndex(KEY_TIMEDIARIES_NOTIFIEDTIME)), c.getLong(c.getColumnIndex(KEY_TIMEDIARIES_VALIDITY_UNTIL)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_QUESTIONID)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_SUBQUESTIONS)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_STATUS)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_TITLE)), c.getString(c.getColumnIndex(KEY_TIMEDIARIES_SYNCHRONIZATION))));
                } while (c.moveToNext());
            }
        }

        c.close();

        return questions;
    }

    /**
     * Method to update a {@link Question} already existing in the database. The update involves the {@link #KEY_TIMEDIARIES_STATUS} field and is done on the question
     * that matches the {@link #KEY_TIMEDIARIES_QUESTIONID} field to the {@link Question} passed as parameter
     * @param question {@link Question} object that needs to be updated in the database, the match occurs by {@link #KEY_TIMEDIARIES_QUESTIONID}
     * @param status String representing the new status of the quesiton that has to be updated
     * @return the number of rows affected
     */
    public int updateQuestion(Question question, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIMEDIARIES_STATUS, status);

        // updating row
        return db.update(TABLE_TIMEDIARIES, values, KEY_TIMEDIARIES_QUESTIONID + " = ?",
                new String[] { String.valueOf(question.getInstanceid()) });
    }

    /**
     * Method to update a {@link Question} already existing in the database. The update involves the {@link #KEY_TIMEDIARIES_SYNCHRONIZATION} field and is done on the question
     * that matches the {@link #KEY_TIMEDIARIES_QUESTIONID} field to the {@link Question} passed as parameter
     * @param question {@link Question} object that needs to be updated in the database, the match occurs by {@link #KEY_TIMEDIARIES_QUESTIONID}
     * @param synchronization String representing the new status of the quesiton that has to be updated
     * @return the number of rows affected
     */
    public int updateQuestionSynchronization(Question question, String synchronization) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIMEDIARIES_SYNCHRONIZATION, synchronization);

        // updating row
        return db.update(TABLE_TIMEDIARIES, values, KEY_TIMEDIARIES_QUESTIONID + " = ?",
                new String[] { String.valueOf(question.getInstanceid()) });
    }

    /**
     * Method that adds a new {@link Answer} to the database
     * @param answer {@link Answer} object to be inserted into the database
     * @return the row ID of the newly inserted row, or -1 if an error occurred, for more details {@link SQLiteDatabase#insert(String, String, ContentValues)}
     */
    public long addAnswer(Answer answer) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ANSWER_INSTANCEID, answer.getInstanceid());
        values.put(KEY_ANSWER_CONTENT, answer.getAnswer().toString());
        values.put(KEY_ANSWER_PAYLOAD, answer.getPayload().toString());
        values.put(KEY_ANSWER_TYPE, answer.getType());
        values.put(KEY_ANSWER_ANSWERTIME, answer.getAnswertime());
        values.put(KEY_ANSWER_NOTIFIEDTIME, answer.getNotifiedtime());
        values.put(KEY_ANSWER_INSTANCETIME, answer.getInstancetime());
        values.put(KEY_ANSWER_DELTA, answer.getDelta());
        values.put(KEY_ANSWER_ANSWERDURATION, answer.getAnswerDuration());
        values.put(KEY_ANSWER_ANSWER_SYNCHRONIZATION, answer.getAnswersSynchronization());
        values.put(KEY_ANSWER_PAYLOAD_SYNCHRONIZATION, answer.getPayloadSynchronization());

        return db.insert(TABLE_ANSWERS, null, values);
    }

    /**
     * Method used to retrieve all the {@link Answer} objects from the database
     * @return Returns a list of {@link Answer}
     */
    public List<Answer> getAllAnswers() {
        List<Answer> answers = new ArrayList<Answer>();
        String selectQuery = "SELECT  * FROM " + TABLE_ANSWERS;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                try {
                    answers.add(new Answer(c.getLong(c.getColumnIndex(KEY_ANSWER_INSTANCETIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_NOTIFIEDTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERDURATION)), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_CONTENT))), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD))), c.getString(c.getColumnIndex(KEY_ANSWER_INSTANCEID)), c.getString(c.getColumnIndex(KEY_ANSWER_TYPE)), c.getString(c.getColumnIndex(KEY_ANSWER_ANSWER_SYNCHRONIZATION)), c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD_SYNCHRONIZATION))));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (c.moveToNext());
        }

        Collections.sort(answers, new Comparator<Answer>() {
            @Override
            public int compare(Answer o1, Answer o2) {
                return o1.getInstancetime() > o2.getInstancetime() ? -1 : 1;
            }
        });

        c.close();

        return answers;
    }

    /**
     * Method that retrieves all the {@link Answer} from the database filtering by the {@link #KEY_ANSWER_ANSWER_SYNCHRONIZATION} value
     * @param synchronization String that has to match for a {@link Answer} to be returned from the database
     * @return Returns a list of {@link Answer} that match the {@link #KEY_ANSWER_ANSWER_SYNCHRONIZATION}
     */
    public List<Answer> getAllAnswersByContentSynchronization(String synchronization) {
        List<Answer> answers = new ArrayList<Answer>();

        String selectQuery = "SELECT  * FROM " + TABLE_ANSWERS + " WHERE "
                + KEY_ANSWER_ANSWER_SYNCHRONIZATION + " = \"" + synchronization+"\"";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {
                    try {
                        answers.add(new Answer(c.getLong(c.getColumnIndex(KEY_ANSWER_INSTANCETIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_NOTIFIEDTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERDURATION)), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_CONTENT))), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD))), c.getString(c.getColumnIndex(KEY_ANSWER_INSTANCEID)), c.getString(c.getColumnIndex(KEY_ANSWER_TYPE)), c.getString(c.getColumnIndex(KEY_ANSWER_ANSWER_SYNCHRONIZATION)), c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD_SYNCHRONIZATION))));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (c.moveToNext());
            }
        }

        c.close();

        return answers;
    }

    /**
     * Method that retrieves all the {@link Answer} from the database filtering by the {@link #KEY_ANSWER_ANSWER_SYNCHRONIZATION} value
     * @param synchronization String that has to match for a {@link Answer} to be returned from the database
     * @return Returns a list of {@link Answer} that match the {@link #KEY_ANSWER_ANSWER_SYNCHRONIZATION}
     */
    public List<Answer> getAllAnswersBySynchronization(String synchronization) {
        List<Answer> answers = new ArrayList<Answer>();

        String selectQuery = "SELECT  * FROM " + TABLE_ANSWERS + " WHERE "
                + KEY_ANSWER_ANSWER_SYNCHRONIZATION + " = \"" + synchronization+"\" OR "
                + KEY_ANSWER_PAYLOAD_SYNCHRONIZATION  + " = \"" + synchronization+"\"";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {
                    try {
                        answers.add(new Answer(c.getLong(c.getColumnIndex(KEY_ANSWER_INSTANCETIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_NOTIFIEDTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERDURATION)), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_CONTENT))), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD))), c.getString(c.getColumnIndex(KEY_ANSWER_INSTANCEID)), c.getString(c.getColumnIndex(KEY_ANSWER_TYPE)), c.getString(c.getColumnIndex(KEY_ANSWER_ANSWER_SYNCHRONIZATION)), c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD_SYNCHRONIZATION))));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (c.moveToNext());
            }
        }

        c.close();

        return answers;
    }

    /**
     * Method that retrieves all the {@link Answer} from the database filtering by the {@link #KEY_ANSWER_ANSWER_SYNCHRONIZATION} value
     * @param synchronization String that has to match for a {@link Answer} to be returned from the database
     * @return Returns a list of {@link Answer} that match the {@link #KEY_ANSWER_ANSWER_SYNCHRONIZATION}
     */
    public List<Answer> getAllAnswersByPayloadSynchronization(String synchronization) {
        List<Answer> answers = new ArrayList<Answer>();

        String selectQuery = "SELECT  * FROM " + TABLE_ANSWERS + " WHERE "
                + KEY_ANSWER_PAYLOAD_SYNCHRONIZATION + " = \"" + synchronization+"\"";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {
                    try {
                        answers.add(new Answer(c.getLong(c.getColumnIndex(KEY_ANSWER_INSTANCETIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_NOTIFIEDTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERDURATION)), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_CONTENT))), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD))), c.getString(c.getColumnIndex(KEY_ANSWER_INSTANCEID)), c.getString(c.getColumnIndex(KEY_ANSWER_TYPE)), c.getString(c.getColumnIndex(KEY_ANSWER_ANSWER_SYNCHRONIZATION)), c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD_SYNCHRONIZATION))));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (c.moveToNext());
            }
        }

        c.close();

        return answers;
    }

    /**
     * Method that retrieves all the {@link Answer} from the database filtering by the {@link #KEY_ANSWER_INSTANCEID} field
     * @param instanceid String that has to match for a {@link Answer} to be returned from the database
     * @return Returns a list of {@link Answer} that match the {@link #KEY_ANSWER_INSTANCEID}
     */
    public List<Answer> getAllAnswersByInstanceId(String instanceid) {
        List<Answer> answers = new ArrayList<Answer>();

        String selectQuery = "SELECT  * FROM " + TABLE_ANSWERS + " WHERE "
                + KEY_ANSWER_INSTANCEID + " = \"" + instanceid+"\"";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {
                    try {
                        c.getLong(c.getColumnIndex(KEY_ANSWER_INSTANCETIME));
                        c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERTIME));
                        c.getLong(c.getColumnIndex(KEY_ANSWER_NOTIFIEDTIME));
                        c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERDURATION));
                        new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_CONTENT)));
                        new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD)));
                        c.getString(c.getColumnIndex(KEY_ANSWER_INSTANCEID));
                        c.getString(c.getColumnIndex(KEY_ANSWER_TYPE));
                        c.getString(c.getColumnIndex(KEY_ANSWER_ANSWER_SYNCHRONIZATION));
                        c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD_SYNCHRONIZATION));

                        answers.add(new Answer(c.getLong(c.getColumnIndex(KEY_ANSWER_INSTANCETIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_NOTIFIEDTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERDURATION)), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_CONTENT))), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD))), c.getString(c.getColumnIndex(KEY_ANSWER_INSTANCEID)), c.getString(c.getColumnIndex(KEY_ANSWER_TYPE)), c.getString(c.getColumnIndex(KEY_ANSWER_ANSWER_SYNCHRONIZATION)), c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD_SYNCHRONIZATION))));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (c.moveToNext());
            }
        }

        c.close();

        return answers;
    }

    /**
     * Method that retrieves all the {@link Answer} from the database filtering by the {@link #KEY_ANSWER_ANSWERTIME} field, it returns all the {@link Answer} between
     * the current time (now) and time*timeinterval from now in the past
     * @param time Integer representing the amount of time that needs to be considered
     * @param interval The unit of measure that merged with the time variable creates the timeinterval according to which perform the query, {@link Challenge#INTERVAL_DAYS},
     *                 {@link Challenge#INTERVAL_HOURS}, {@link Challenge#INTERVAL_MINUTES}, {@link Challenge#INTERVAL_SECONDS}.
     * @return Returns a list of {@link Answer} that match the {@link #KEY_ANSWER_ANSWERTIME}
     */
    public List<Answer> getAllAnswersInTheLast(int time, String interval) {
        List<Answer> answers = new ArrayList<Answer>();

        long now = System.currentTimeMillis();

        String selectQuery = "SELECT  * FROM " + TABLE_ANSWERS + " WHERE " + KEY_ANSWER_ANSWERTIME + " < " + now + " AND " + KEY_ANSWER_ANSWERTIME + " > " + convertToDate(now, time, interval);

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {
                    try {
                        answers.add(new Answer(c.getLong(c.getColumnIndex(KEY_ANSWER_INSTANCETIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_NOTIFIEDTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERDURATION)), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_CONTENT))), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD))), c.getString(c.getColumnIndex(KEY_ANSWER_INSTANCEID)), c.getString(c.getColumnIndex(KEY_ANSWER_TYPE)), c.getString(c.getColumnIndex(KEY_ANSWER_ANSWER_SYNCHRONIZATION)), c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD_SYNCHRONIZATION))));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (c.moveToNext());
            }
        }

        Collections.sort(answers, new Comparator<Answer>() {
            @Override
            public int compare(Answer o1, Answer o2) {
                return o1.getInstancetime() < o2.getInstancetime() ? -1 : 1;
            }
        });

        c.close();

        return answers;
    }

    /**
     * Method that adds a new {@link Task} to the database
     * @param task {@link Task} object to be inserted into the database
     * @return the row ID of the newly inserted row, or -1 if an error occurred, for more details {@link SQLiteDatabase#insert(String, String, ContentValues)}
     */
    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TASK_TIME, task.getInstanceTime());
        values.put(KEY_TASK_NOTIFIEDTIME, task.getNotifiedTime());
        values.put(KEY_TASK_TASKID, task.getInstanceid());
        values.put(KEY_TASK_SUBTASKS, task.getContent());
        values.put(KEY_TASK_STATUS, task.getStatus());
        values.put(KEY_TASK_VALIDITY_UNTIL, task.getValidityFor());
        values.put(KEY_TASK, task.getTitle());
        values.put(KEY_TASK_SYNCHRONIZATION, task.getSynchronization());

        return db.insert(TABLE_TASKS, null, values);
    }

    /**
     * Method that retrieves all the {@link Task} from the database filtering by the {@link #KEY_TASK_TASKID} field
     * @param instanceid String that has to match for a {@link Task} to be returned from the database
     * @return Returns a list of {@link Task} that match the {@link #KEY_TASK_TASKID}
     */
    public Task getTask(String instanceid) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_TASKS + " WHERE "
                + KEY_TASK_TASKID + " = \"" + instanceid+"\"";

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Task task = new Task(c.getLong(c.getColumnIndex(KEY_TASK_TIME)), c.getLong(c.getColumnIndex(KEY_TASK_NOTIFIEDTIME)), c.getString(c.getColumnIndex(KEY_TASK_SUBTASKS)), c.getString(c.getColumnIndex(KEY_TASK_TASKID)), c.getString(c.getColumnIndex(KEY_TASK_STATUS)), c.getString(c.getColumnIndex(KEY_TASK)), c.getLong(c.getColumnIndex(KEY_TASK_VALIDITY_UNTIL)), c.getString(c.getColumnIndex(KEY_TASK_SYNCHRONIZATION)));
        c.close();

        return task;
    }

    /**
     * Method used to retrieve all the {@link Task} objects from the database
     * @return Returns a list of {@link Task}
     */
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<Task>();
        String selectQuery = "SELECT  * FROM " + TABLE_TASKS;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                tasks.add(new Task(c.getLong(c.getColumnIndex(KEY_TASK_TIME)), c.getLong(c.getColumnIndex(KEY_TASK_NOTIFIEDTIME)), c.getString(c.getColumnIndex(KEY_TASK_SUBTASKS)), c.getString(c.getColumnIndex(KEY_TASK_TASKID)), c.getString(c.getColumnIndex(KEY_TASK_STATUS)), c.getString(c.getColumnIndex(KEY_TASK)), c.getLong(c.getColumnIndex(KEY_TASK_VALIDITY_UNTIL)), c.getString(c.getColumnIndex(KEY_TASK_SYNCHRONIZATION))));
            } while (c.moveToNext());
        }

        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return o1.getInstanceTime() > o2.getInstanceTime() ? -1 : 1;
            }
        });

        c.close();

        return tasks;
    }

    /**
     * Method used to retrieve all the {@link Message} objects from the database
     * @return Returns a list of {@link Message}
     */
    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<Message>();
        String selectQuery = "SELECT  * FROM " + TABLE_MESSAGES;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                //long receivedTimestamp, long notifiedTime, long validityFor, String messageid, String subMessages, String status, String title
                messages.add(new Message(c.getLong(c.getColumnIndex(KEY_MESSAGE_TIME)), c.getLong(c.getColumnIndex(KEY_MESSAGE_NOTIFIED_TIME)), c.getLong(c.getColumnIndex(KEY_MESSAGE_VALIDITY_UNTIL)) , c.getString(c.getColumnIndex(KEY_MESSAGE_MESSAGEID)), c.getString(c.getColumnIndex(KEY_MESSAGE_CONTENT)), c.getString(c.getColumnIndex(KEY_MESSAGE_STATUS)), c.getString(c.getColumnIndex(KEY_MESSAGE_TITLE)), c.getString(c.getColumnIndex(KEY_MESSAGE_SYNCHRONIZATION))));
            } while (c.moveToNext());
        }

        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o1.getNotifiedTime() > o2.getNotifiedTime() ? -1 : 1;
            }
        });

        c.close();

        return messages;
    }

    /**
     * Method that retrieves all the {@link Task} from the database filtering by the {@link #KEY_TASK_STATUS} field
     * @param status String that has to match for a {@link Task} to be returned from the database
     * @return Returns a list of {@link Task} that match the {@link #KEY_TASK_STATUS}
     */
    public List<Task> getAllTasksByStatus(String status) {
        List<Task> tasks = new ArrayList<Task>();

        String selectQuery = "SELECT  * FROM " + TABLE_TASKS + " WHERE "
                + KEY_TASK_STATUS + " = \"" + status+"\"";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {
                    tasks.add(new Task(c.getLong(c.getColumnIndex(KEY_TASK_TIME)), c.getLong(c.getColumnIndex(KEY_TASK_NOTIFIEDTIME)), c.getString(c.getColumnIndex(KEY_TASK_SUBTASKS)), c.getString(c.getColumnIndex(KEY_TASK_TASKID)), c.getString(c.getColumnIndex(KEY_TASK_STATUS)), c.getString(c.getColumnIndex(KEY_TASK)), c.getLong(c.getColumnIndex(KEY_TASK_VALIDITY_UNTIL)), c.getString(c.getColumnIndex(KEY_TASK_SYNCHRONIZATION))));
                } while (c.moveToNext());
            }
        }

        c.close();

        return tasks;
    }

    /**
     * Method that retrieves all the {@link Task} from the database filtering by the {@link #KEY_TASK_SYNCHRONIZATION} field
     * @param synchronization String that has to match for a {@link Task} to be returned from the database
     * @return Returns a list of {@link Task} that match the {@link #KEY_TASK_SYNCHRONIZATION}
     */
    public List<Task> getAllTasksBySynchronization(String synchronization) {
        List<Task> tasks = new ArrayList<Task>();

        String selectQuery = "SELECT  * FROM " + TABLE_TASKS + " WHERE "
                + KEY_TASK_SYNCHRONIZATION + " = \"" + synchronization+"\"";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {
                    tasks.add(new Task(c.getLong(c.getColumnIndex(KEY_TASK_TIME)), c.getLong(c.getColumnIndex(KEY_TASK_NOTIFIEDTIME)), c.getString(c.getColumnIndex(KEY_TASK_SUBTASKS)), c.getString(c.getColumnIndex(KEY_TASK_TASKID)), c.getString(c.getColumnIndex(KEY_TASK_STATUS)), c.getString(c.getColumnIndex(KEY_TASK)), c.getLong(c.getColumnIndex(KEY_TASK_VALIDITY_UNTIL)), c.getString(c.getColumnIndex(KEY_TASK_SYNCHRONIZATION))));
                } while (c.moveToNext());
            }
        }

        c.close();

        return tasks;
    }

    /**
     * Method that retrieves all the {@link Message} from the database filtering by the {@link #KEY_MESSAGE_STATUS} field
     * @param status String that has to match for a {@link Message} to be returned from the database
     * @return Returns a list of {@link Message} that match the {@link #KEY_MESSAGE_STATUS}
     */
    public List<Message> getAllMessagesByStatus(String status) {
        List<Message> messages = new ArrayList<Message>();

        String selectQuery = "SELECT  * FROM " + TABLE_MESSAGES + " WHERE "
                + KEY_MESSAGE_STATUS + " = \"" + status+"\"";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {//long receivedTimestamp, long notifiedTime, long validityFor, String messageid, String subMessages, String status, String title
                    messages.add(new Message(c.getLong(c.getColumnIndex(KEY_MESSAGE_TIME)), c.getLong(c.getColumnIndex(KEY_MESSAGE_NOTIFIED_TIME)), c.getLong(c.getColumnIndex(KEY_MESSAGE_VALIDITY_UNTIL)), c.getString(c.getColumnIndex(KEY_MESSAGE_MESSAGEID)), c.getString(c.getColumnIndex(KEY_MESSAGE_CONTENT)), c.getString(c.getColumnIndex(KEY_MESSAGE_STATUS)), c.getString(c.getColumnIndex(KEY_MESSAGE_TITLE)), c.getString(c.getColumnIndex(KEY_MESSAGE_SYNCHRONIZATION))));
                } while (c.moveToNext());
            }
        }

        c.close();

        return messages;
    }

    /**
     * Method that retrieves all the {@link Task} from the database filtering by the {@link #KEY_MESSAGE_SYNCHRONIZATION} field
     * @param synchronization String that has to match for a {@link Task} to be returned from the database
     * @return Returns a list of {@link Task} that match the {@link #KEY_MESSAGE_SYNCHRONIZATION}
     */
    public List<Message> getAllMessagesBySynchronization(String synchronization) {
        List<Message> messages = new ArrayList<Message>();

        String selectQuery = "SELECT  * FROM " + TABLE_MESSAGES + " WHERE "
                + KEY_MESSAGE_SYNCHRONIZATION + " = \"" + synchronization+"\"";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {//long receivedTimestamp, long notifiedTime, long validityFor, String messageid, String subMessages, String status, String title
                    messages.add(new Message(c.getLong(c.getColumnIndex(KEY_MESSAGE_TIME)), c.getLong(c.getColumnIndex(KEY_MESSAGE_NOTIFIED_TIME)), c.getLong(c.getColumnIndex(KEY_MESSAGE_VALIDITY_UNTIL)), c.getString(c.getColumnIndex(KEY_MESSAGE_MESSAGEID)), c.getString(c.getColumnIndex(KEY_MESSAGE_CONTENT)), c.getString(c.getColumnIndex(KEY_MESSAGE_STATUS)), c.getString(c.getColumnIndex(KEY_MESSAGE_TITLE)), c.getString(c.getColumnIndex(KEY_MESSAGE_SYNCHRONIZATION))));
                } while (c.moveToNext());
            }
        }

        c.close();

        return messages;
    }

    /**
     * Method that counts the number of {@link Task} stored in the database
     * @return integer representing the number of {@link Task} stored in the database
     */
    public int getTasksCount() {
        String countQuery = "SELECT  * FROM " + TABLE_TASKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /**
     * Method that counts the number of {@link Task} stored in the database filtering by the {@link #KEY_TASK_STATUS} field
     * @param status String that has to match for a {@link Task} to be returned from the database
     * @return integer representing the number of {@link Task} stored in the database
     */
    public int getTasksCountByStatus(String status) {
        String countQuery = "SELECT  * FROM " + TABLE_TASKS + " WHERE "
                + KEY_TASK_STATUS + " = \"" + status+"\"";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /**
     * Method to update a {@link Task} already existing in the database. The update involves the {@link #KEY_TASK_STATUS} field and is done on the question
     * that matches the {@link #KEY_TASK_TASKID} field to the {@link Task passed as parameter}
     * @param task {@link Task} object that needs to be updated in the database, the match occurs by {@link #KEY_TASK_TASKID}
     * @param status String representing the new status of the quesiton that has to be updated
     * @return the number of rows affected
     */
    public int updateTask(Task task, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TASK_STATUS, status);

        // updating row
        return db.update(TABLE_TASKS, values, KEY_TASK_TASKID + " = ?",
                new String[] { String.valueOf(task.getInstanceid()) });
    }

    /**
     * Method to update a {@link Task} already existing in the database. The update involves the {@link #KEY_TASK_SYNCHRONIZATION} field and is done on the question
     * that matches the {@link #KEY_TASK_TASKID} field to the {@link Task} passed as parameter
     * @param task {@link Task} object that needs to be updated in the database, the match occurs by {@link #KEY_TASK_TASKID}
     * @param synchronization String representing the new status of the quesiton that has to be updated
     * @return the number of rows affected
     */
    public int updateTaskSynchronization(Task task, String synchronization) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TASK_SYNCHRONIZATION, synchronization);

        // updating row
        return db.update(TABLE_TASKS, values, KEY_TASK_TASKID + " = ?",
                new String[] { String.valueOf(task.getInstanceid()) });
    }

    /**
     * Method to update a {@link Message} already existing in the database. The update involves the {@link #KEY_MESSAGE_STATUS} field and is done on the question
     * that matches the {@link #KEY_MESSAGE_MESSAGEID} field to the {@link Message} passed as parameter
     * @param message {@link Message} object that needs to be updated in the database, the match occurs by {@link #KEY_MESSAGE_MESSAGEID}
     * @param status String representing the new status of the quesiton that has to be updated
     * @return the number of rows affected
     */
    public int updateMessage(Message message, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE_STATUS, status);

        // updating row
        return db.update(TABLE_MESSAGES, values, KEY_MESSAGE_MESSAGEID + " = ?",
                new String[] { String.valueOf(message.getMessageid()) });
    }

    /**
     * Method to update a {@link Message} already existing in the database. The update involves the {@link #KEY_MESSAGE_SYNCHRONIZATION} field and is done on the question
     * that matches the {@link #KEY_MESSAGE_MESSAGEID} field to the {@link Message} passed as parameter
     * @param message {@link Message} object that needs to be updated in the database, the match occurs by {@link #KEY_MESSAGE_MESSAGEID}
     * @param synchronization String representing the new status of the quesiton that has to be updated
     * @return the number of rows affected
     */
    public int updateMessageSynchronization(Message message, String synchronization) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE_SYNCHRONIZATION, synchronization);

        // updating row
        return db.update(TABLE_MESSAGES, values, KEY_MESSAGE_MESSAGEID + " = ?",
                new String[] { String.valueOf(message.getMessageid()) });
    }

    /**
     * Method to delete a {@link Task} identified by its {@link #KEY_TASK_TIME} field
     * @param taskTime long representing the unixtimestamp of the {@link Task} to be deleted
     */
    public void deleteTask(long taskTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, KEY_TASK_TIME + " = ?",
                new String[] { String.valueOf(taskTime) });
    }

    /**
     * Method to delete all the {@link Task}
     */
    public void deleteAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_TASKS);
    }

    /**
     * Method to delete all the {@link Message}
     */
    public void deleteAllMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_MESSAGES);
    }

    /**
     * Method to delete all the {@link Question}
     */
    public void deleteAllTimeDiaries() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_TIMEDIARIES);
    }

    /**
     * Method to delete all the {@link Challenge}
     */
    public void deleteAllChallenges() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_CHALLENGES);
    }

    /**
     * Method to delete all the {@link Answer}
     */
    public void deleteAllAnswers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_ANSWERS);
    }

    /**
     * Method that adds a new {@link Message} to the database
     * @param message {@link Message} object to be inserted into the database
     * @return the row ID of the newly inserted row, or -1 if an error occurred, for more details {@link SQLiteDatabase#insert(String, String, ContentValues)}
     */
    public long addMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGE_TIME, message.getMessageTimestamp());
        values.put(KEY_MESSAGE_NOTIFIED_TIME, message.getNotifiedTime());
        values.put(KEY_MESSAGE_MESSAGEID, message.getMessageid());
        values.put(KEY_MESSAGE_CONTENT, message.getContent());
        values.put(KEY_MESSAGE_STATUS, message.getStatus());
        values.put(KEY_MESSAGE_VALIDITY_UNTIL, message.getValidityFor());
        values.put(KEY_MESSAGE_TITLE, message.getTitle());
        values.put(KEY_MESSAGE_SYNCHRONIZATION, message.getSynchronization());

        return db.insert(TABLE_MESSAGES, null, values);
    }

    /**
     * Method that adds a new {@link Challenge} to the database
     * @param challenge {@link Challenge} object to be inserted into the database
     * @return the row ID of the newly inserted row, or -1 if an error occurred, for more details {@link SQLiteDatabase#insert(String, String, ContentValues)}
     */
    public long addChallenge(Challenge challenge) {
        if(getChallengeByInstanceId(challenge.getInstanceid()) == null) {
            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_CHALLENGE_DEFINITIONID, challenge.getDefinitionid());
            values.put(KEY_CHALLENGE_INSTANCEID, challenge.getInstanceid());
            values.put(KEY_CHALLENGE_STATUS, challenge.getStatus());
            values.put(KEY_CHALLENGE_SYNCHRONIZATION, challenge.getSynchronization());
            values.put(KEY_CHALLENGE_PROJECT, challenge.getProject());
            values.put(KEY_CHALLENGE_STARTDATE, challenge.getStartdate());
            values.put(KEY_CHALLENGE_ENDDATE, challenge.getEnddate());
            values.put(KEY_CHALLENGE_LOCATION, challenge.getLocation());
            values.put(KEY_CHALLENGE_TARGET, challenge.getTarget());
            values.put(KEY_CHALLENGE_TYPE, challenge.getType());
            values.put(KEY_CHALLENGE_NAME, challenge.getName());
            values.put(KEY_CHALLENGE_DESCRIPTION, challenge.getDescription());
            values.put(KEY_CHALLENGE_INSTRUCTIONS, challenge.getInstructions());
            values.put(KEY_CHALLENGE_POINTSAWARDED, challenge.getPointsawarded());
            values.put(KEY_CHALLENGE_POINTSPERCONTRIBUTION, challenge.getPointpercontribution());
            values.put(KEY_CHALLENGE_CONSTRAINTS, challenge.getConstraints());
            values.put(KEY_CHALLENGE_CONTENT, challenge.getContent());
            values.put(KEY_CHALLENGE_PARTICIPATION_TIME, challenge.getParticipationtime());

            return db.insert(TABLE_CHALLENGES, null, values);
        }
        else {
            Log.d(this.toString(), "Challenge with instanceid "+challenge.getInstanceid()+" already present.");
            return 0;
        }
    }

    /**
     * Method used to retrieve all the {@link Challenge} objects from the database
     * @return Returns a list of {@link Challenge}
     */
    public List<Challenge> getAllChallenges() {
        List<Challenge> challenges = new ArrayList<Challenge>();
        String selectQuery = "SELECT  * FROM " + TABLE_CHALLENGES;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                challenges.add(new Challenge(c.getString(c.getColumnIndex(KEY_CHALLENGE_INSTANCEID)), c.getString(c.getColumnIndex(KEY_CHALLENGE_DEFINITIONID)), c.getString(c.getColumnIndex(KEY_CHALLENGE_STATUS)), c.getString(c.getColumnIndex(KEY_CHALLENGE_SYNCHRONIZATION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_PROJECT)), c.getString(c.getColumnIndex(KEY_CHALLENGE_STARTDATE)), c.getString(c.getColumnIndex(KEY_CHALLENGE_ENDDATE)), c.getString(c.getColumnIndex(KEY_CHALLENGE_LOCATION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_TARGET)), c.getString(c.getColumnIndex(KEY_CHALLENGE_TYPE)), c.getString(c.getColumnIndex(KEY_CHALLENGE_NAME)), c.getString(c.getColumnIndex(KEY_CHALLENGE_DESCRIPTION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_INSTRUCTIONS)), c.getInt(c.getColumnIndex(KEY_CHALLENGE_POINTSAWARDED)), c.getInt(c.getColumnIndex(KEY_CHALLENGE_POINTSPERCONTRIBUTION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_CONSTRAINTS)), c.getString(c.getColumnIndex(KEY_CHALLENGE_CONTENT)), c.getString(c.getColumnIndex(KEY_CHALLENGE_PARTICIPATION_TIME)), c.getString(c.getColumnIndex(KEY_CHALLENGE_COMPLETION_TIME)), c.getString(c.getColumnIndex(KEY_CHALLENGE_RESULT))));
            } while (c.moveToNext());
        }

        Collections.sort(challenges, new Comparator<Challenge>() {
            @Override
            public int compare(Challenge c1, Challenge c2) {
                return c1.getEnddateAsLong() > c2.getEnddateAsLong() ? -1 : 1;
            }
        });

        c.close();

        return challenges;
    }

    /**
     * Method that retrieves all the {@link Challenge} from the database filtering by the {@link #KEY_CHALLENGE_STATUS} field
     * @param status String that has to match for a {@link Challenge} to be returned from the database
     * @return Returns a list of {@link Challenge} that match the {@link #KEY_CHALLENGE_STATUS}
     */
    public List<Challenge> getAllChallengesByStatus(String status) {
        List<Challenge> challenges = new ArrayList<Challenge>();

        String selectQuery = "SELECT  * FROM " + TABLE_CHALLENGES + " WHERE "
                + KEY_CHALLENGE_STATUS + " = \"" + status+"\"";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {
                    challenges.add(new Challenge(c.getString(c.getColumnIndex(KEY_CHALLENGE_INSTANCEID)), c.getString(c.getColumnIndex(KEY_CHALLENGE_DEFINITIONID)), c.getString(c.getColumnIndex(KEY_CHALLENGE_STATUS)), c.getString(c.getColumnIndex(KEY_CHALLENGE_SYNCHRONIZATION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_PROJECT)), c.getString(c.getColumnIndex(KEY_CHALLENGE_STARTDATE)), c.getString(c.getColumnIndex(KEY_CHALLENGE_ENDDATE)), c.getString(c.getColumnIndex(KEY_CHALLENGE_LOCATION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_TARGET)), c.getString(c.getColumnIndex(KEY_CHALLENGE_TYPE)), c.getString(c.getColumnIndex(KEY_CHALLENGE_NAME)), c.getString(c.getColumnIndex(KEY_CHALLENGE_DESCRIPTION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_INSTRUCTIONS)), c.getInt(c.getColumnIndex(KEY_CHALLENGE_POINTSAWARDED)), c.getInt(c.getColumnIndex(KEY_CHALLENGE_POINTSPERCONTRIBUTION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_CONSTRAINTS)), c.getString(c.getColumnIndex(KEY_CHALLENGE_CONTENT)), c.getString(c.getColumnIndex(KEY_CHALLENGE_PARTICIPATION_TIME)), c.getString(c.getColumnIndex(KEY_CHALLENGE_COMPLETION_TIME)), c.getString(c.getColumnIndex(KEY_CHALLENGE_RESULT))));
                } while (c.moveToNext());
            }
        }

        c.close();

        return challenges;
    }

    /**
     * Method that retrieves all the {@link Challenge} from the database filtering by the {@link #KEY_CHALLENGE_SYNCHRONIZATION} field
     * @param synchronization String that has to match for a {@link Challenge} to be returned from the database
     * @return Returns a list of {@link Challenge} that match the {@link #KEY_CHALLENGE_SYNCHRONIZATION}
     */
    public List<Challenge> getAllChallengesBySynchronization(String synchronization) {
        List<Challenge> challenges = new ArrayList<Challenge>();

        String selectQuery = "SELECT  * FROM " + TABLE_CHALLENGES + " WHERE "
                + KEY_CHALLENGE_SYNCHRONIZATION + " = \"" + synchronization+"\"";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {
                    challenges.add(new Challenge(c.getString(c.getColumnIndex(KEY_CHALLENGE_INSTANCEID)), c.getString(c.getColumnIndex(KEY_CHALLENGE_DEFINITIONID)), c.getString(c.getColumnIndex(KEY_CHALLENGE_STATUS)), c.getString(c.getColumnIndex(KEY_CHALLENGE_SYNCHRONIZATION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_PROJECT)), c.getString(c.getColumnIndex(KEY_CHALLENGE_STARTDATE)), c.getString(c.getColumnIndex(KEY_CHALLENGE_ENDDATE)), c.getString(c.getColumnIndex(KEY_CHALLENGE_LOCATION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_TARGET)), c.getString(c.getColumnIndex(KEY_CHALLENGE_TYPE)), c.getString(c.getColumnIndex(KEY_CHALLENGE_NAME)), c.getString(c.getColumnIndex(KEY_CHALLENGE_DESCRIPTION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_INSTRUCTIONS)), c.getInt(c.getColumnIndex(KEY_CHALLENGE_POINTSAWARDED)), c.getInt(c.getColumnIndex(KEY_CHALLENGE_POINTSPERCONTRIBUTION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_CONSTRAINTS)), c.getString(c.getColumnIndex(KEY_CHALLENGE_CONTENT)), c.getString(c.getColumnIndex(KEY_CHALLENGE_PARTICIPATION_TIME)), c.getString(c.getColumnIndex(KEY_CHALLENGE_COMPLETION_TIME)), c.getString(c.getColumnIndex(KEY_CHALLENGE_RESULT))));
                } while (c.moveToNext());
            }
        }

        c.close();

        return challenges;
    }

    /**
     * Method that retrieves all the {@link Challenge} from the database filtering by the {@link #KEY_CHALLENGE_INSTANCEID} field
     * @param instaceid String that has to match for a {@link Challenge} to be returned from the database
     * @return Returns a list of {@link Challenge} that match the {@link #KEY_CHALLENGE_INSTANCEID}
     */
    public Challenge getChallengeByInstanceId(String instaceid) {
        String selectQuery = "SELECT  * FROM " + TABLE_CHALLENGES + " WHERE " + KEY_CHALLENGE_INSTANCEID + " = \"" + instaceid+"\"";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {
                    return new Challenge(c.getString(c.getColumnIndex(KEY_CHALLENGE_INSTANCEID)), c.getString(c.getColumnIndex(KEY_CHALLENGE_DEFINITIONID)), c.getString(c.getColumnIndex(KEY_CHALLENGE_STATUS)), c.getString(c.getColumnIndex(KEY_CHALLENGE_SYNCHRONIZATION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_PROJECT)), c.getString(c.getColumnIndex(KEY_CHALLENGE_STARTDATE)), c.getString(c.getColumnIndex(KEY_CHALLENGE_ENDDATE)), c.getString(c.getColumnIndex(KEY_CHALLENGE_LOCATION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_TARGET)), c.getString(c.getColumnIndex(KEY_CHALLENGE_TYPE)), c.getString(c.getColumnIndex(KEY_CHALLENGE_NAME)), c.getString(c.getColumnIndex(KEY_CHALLENGE_DESCRIPTION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_INSTRUCTIONS)), c.getInt(c.getColumnIndex(KEY_CHALLENGE_POINTSAWARDED)), c.getInt(c.getColumnIndex(KEY_CHALLENGE_POINTSPERCONTRIBUTION)), c.getString(c.getColumnIndex(KEY_CHALLENGE_CONSTRAINTS)), c.getString(c.getColumnIndex(KEY_CHALLENGE_CONTENT)), c.getString(c.getColumnIndex(KEY_CHALLENGE_PARTICIPATION_TIME)), c.getString(c.getColumnIndex(KEY_CHALLENGE_COMPLETION_TIME)), c.getString(c.getColumnIndex(KEY_CHALLENGE_RESULT)));
                } while (c.moveToNext());
            }
        }

        c.close();

        return null;
    }

    /**
     * Method that retrieves all the {@link Answer} from the database filtering by the {@link #KEY_ANSWER_INSTANCEID} field
     * @param answer {@link Answer} object that needs to be retrieved form the database
     * @return Returns a list of {@link Answer} that match the {@link #KEY_ANSWER_INSTANCEID}
     */
    public Answer getAnswerByInstanceId(Answer answer) {
        String selectQuery = "SELECT  * FROM " + TABLE_ANSWERS + " WHERE " + KEY_ANSWER_INSTANCEID + " = \"" + answer.getInstanceid()+"\"";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if(c!=null) {
            if (c.moveToFirst()) {
                do {
                    try {
                        return new Answer(c.getLong(c.getColumnIndex(KEY_ANSWER_INSTANCETIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_NOTIFIEDTIME)), c.getLong(c.getColumnIndex(KEY_ANSWER_ANSWERDURATION)), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_CONTENT))), new JSONArray(c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD))), c.getString(c.getColumnIndex(KEY_ANSWER_INSTANCEID)), c.getString(c.getColumnIndex(KEY_ANSWER_TYPE)), c.getString(c.getColumnIndex(KEY_ANSWER_ANSWER_SYNCHRONIZATION)), c.getString(c.getColumnIndex(KEY_ANSWER_PAYLOAD_SYNCHRONIZATION)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (c.moveToNext());
            }
        }

        c.close();

        return null;
    }

    /**
     * Method to update a {@link Challenge} already existing in the database. The update involves the {@link #KEY_CHALLENGE_STATUS} and the
     * {@link #KEY_CHALLENGE_COMPLETION_TIME} fields and is done on the question that matches the {@link #KEY_CHALLENGE_INSTANCEID} field to the {@link Challenge} passed as parameter
     * @param challenge {@link Challenge} object that needs to be updated in the database, the match occurs by {@link #KEY_CHALLENGE_INSTANCEID}
     * @param status String representing the new status of the {@link Challenge} that has to be updated
     * @return the number of rows affected
     */
    public int updateChallengeStatus(Challenge challenge, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CHALLENGE_STATUS, status);
        values.put(KEY_CHALLENGE_SYNCHRONIZATION, Challenge.SYNCHRONIZATION_FALSE);

        if(status.equals(Challenge.STATUS_COMPLETED) || status.equals(Challenge.STATUS_EXPIRED)) {
            values.put(KEY_CHALLENGE_COMPLETION_TIME, Utils.longToStringFormat(System.currentTimeMillis()));
            updateChallengeResult(challenge, Challenge.RESULT_PENDING, 0, 0);
        }

        // updating row
        return db.update(TABLE_CHALLENGES, values, KEY_CHALLENGE_INSTANCEID + " = ?",
                new String[] { challenge.getInstanceid() });
    }

    /**
     * Method to update a {@link Challenge} already existing in the database. The update involves the {@link #KEY_CHALLENGE_RESULT},
     * {@link #KEY_CHALLENGE_POINTSAWARDED} and {@link #KEY_CHALLENGE_POINTSPERCONTRIBUTION} fields and is done on the question that matches the
     * {@link #KEY_CHALLENGE_INSTANCEID} field to the {@link Challenge} passed as parameter
     * @param challenge {@link Challenge} object that needs to be updated in the database, the match occurs by {@link #KEY_CHALLENGE_INSTANCEID}
     * @param result String representing the new result of the {@link Challenge} that has to be updated
     * @param pointsawarded String representing the pointsawarded of the {@link Challenge} that has to be updated
     * @param pointspercontribution String representing the pointspercontribution of the {@link Challenge} that has to be updated
     * @return the number of rows affected
     */
    public int updateChallengeResult(Challenge challenge, String result, int pointsawarded, int pointspercontribution) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CHALLENGE_RESULT, result);
        values.put(KEY_CHALLENGE_POINTSAWARDED, pointsawarded);
        values.put(KEY_CHALLENGE_POINTSPERCONTRIBUTION, pointspercontribution);

        // updating row
        return db.update(TABLE_CHALLENGES, values, KEY_CHALLENGE_INSTANCEID + " = ?",
                new String[] { challenge.getInstanceid() });
    }

    /**
     * Method to update a {@link Challenge} already existing in the database. The update involves the {@link #KEY_CHALLENGE_SYNCHRONIZATION} field and is done on the question
     * that matches the {@link #KEY_CHALLENGE_INSTANCEID} field to the {@link Challenge} passed as parameter
     * @param challenge {@link Challenge} object that needs to be updated in the database, the match occurs by {@link #KEY_CHALLENGE_INSTANCEID}
     * @param synchronization String representing the new synchronization of the quesiton that has to be updated
     * @return the number of rows affected
     */
    public int updateChallengeSynchronization(Challenge challenge, String synchronization) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CHALLENGE_SYNCHRONIZATION, synchronization);

        // updating row
        return db.update(TABLE_CHALLENGES, values, KEY_CHALLENGE_INSTANCEID + " = ?",
                new String[]{challenge.getInstanceid()});
    }

    /**
     * Method to update a {@link Answer} already existing in the database. The update involves the {@link #KEY_ANSWER_ANSWER_SYNCHRONIZATION} field and is done on the question
     * that matches the {@link #KEY_ANSWER_INSTANCEID} field to the {@link Answer} passed as parameter. It also deletes the {@link #KEY_ANSWER_CONTENT} of the answer to save
     * space on disk
     * @param answer {@link Answer} object that needs to be updated in the database, the match occurs by {@link #KEY_ANSWER_INSTANCEID}
     * @param synchronization String representing the new synchronization of the quesiton that has to be updated
     * @return the number of rows affected
     */
    public int updateAnswerContentSynchronization(Answer answer, String synchronization) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ANSWER_ANSWER_SYNCHRONIZATION, synchronization);

        // updating row
        return db.update(TABLE_ANSWERS, values, KEY_ANSWER_INSTANCEID + " = ?",
                new String[]{answer.getInstanceid()});
    }

    /**
     * Method to update a {@link Answer} already existing in the database. The update involves the {@link #KEY_ANSWER_ANSWER_SYNCHRONIZATION} field and is done on the question
     * that matches the {@link #KEY_ANSWER_INSTANCEID} field to the {@link Answer} passed as parameter. It also deletes the {@link #KEY_ANSWER_CONTENT} of the answer to save
     * space on disk
     * @param answer {@link Answer} object that needs to be updated in the database, the match occurs by {@link #KEY_ANSWER_INSTANCEID}
     * @param synchronization String representing the new synchronization of the quesiton that has to be updated
     * @return the number of rows affected
     */
    public int updateAnswerPayloadSynchronization(Answer answer, String synchronization) {
        SQLiteDatabase db = this.getWritableDatabase();

        System.out.println(Utils.returnPicturesToSync().size());
        deleteAllPicturesInPayload(answer);
        System.out.println(Utils.returnPicturesToSync().size());

        ContentValues values = new ContentValues();
        values.put(KEY_ANSWER_PAYLOAD_SYNCHRONIZATION, synchronization);
        values.put(KEY_ANSWER_PAYLOAD, new JSONArray().toString());

        return db.update(TABLE_ANSWERS, values, KEY_ANSWER_INSTANCEID + " = ?",
                    new String[]{answer.getInstanceid()});
    }

    /**
     * Method used to close the database
     */
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    /**
     * Method used to convert the parameter to a data value. It takes the time, and integer values and based on the value of the String interval, it converts it to
     * a date, sobtracting it to the current time.
     * @param now current time expressed as a unixtimestamp
     * @param time time units of type interval
     * @param interval can be one among {@link Challenge#INTERVAL_DAYS}, {@link Challenge#INTERVAL_HOURS}, {@link Challenge#INTERVAL_MINUTES}, {@link Challenge#INTERVAL_SECONDS}.
     * @return unixtimestamp representing the new date
     */
    private long convertToDate(long now, int time, String interval) {
        if(interval.equals(Challenge.INTERVAL_DAYS)) {
            return now - (time * 24 * 60 * 60 * 1000);
        }
        else if (interval.equals(Challenge.INTERVAL_HOURS)) {
            return now - (time * 60 * 60 * 1000);
        }
        else if(interval.equals(Challenge.INTERVAL_MINUTES)) {
            return now - (time * 60 * 1000);
        }
        else if(interval.equals(Challenge.INTERVAL_SECONDS)) {
            return now - (time * 1000);
        }
        return now;
    }

    private void deleteAllPicturesInPayload(Answer answer) {
        JSONArray answersPayload = answer.getPayload();
        for(int index=0; index<answersPayload.length();index++) {
            try {
                JSONObject payload = answersPayload.getJSONObject(index).getJSONObject("payload");
                String picture = payload.getString("picture");
                File file = new File(iLogApplication.getAppContext().getFilesDir() + "/" + picture);
                System.out.println(file.delete());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
