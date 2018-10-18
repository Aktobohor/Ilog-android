package it.unitn.disi.witmee.sensorlog.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import it.unitn.disi.witmee.sensorlog.model.system.ST;
import it.unitn.disi.witmee.sensorlog.utils.Utils;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 22/05/13
 * Time: 18.03
 */

public class Questionnaire {

    private ArrayList<Question> questions = new ArrayList<Question>();
    private int maxQuestions = 0;

    /*
    public Questionnaire(int maxQuestions) {
        this.maxQuestions = maxQuestions;
    }

    public void addQuestion(JSONArray receivedQuestions, long questionTimestamp, String questiondid) {
        try {
            if(getNumberOfQuestions()<maxQuestions) {
                this.questions.add(generateEmptyQuestion(receivedQuestions, questionTimestamp, questiondid));
            }
            else if(getNumberOfQuestions()==maxQuestions) {
                //rimuovi prima domanda inserita e genera una risposta
                this.generateEmptyAnswer(this.questions.get(0));
                //inserisci nuova domanda in fondo
                this.questions.add(generateEmptyQuestion(receivedQuestions, questionTimestamp, questiondid));
            }
            iLogApplication.sharedPreferences.edit().putLong(Utils.CONFIG_LAST_QUESTION_TIMESTAMP, System.currentTimeMillis()).commit();
            //save in sharedprefs
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Question> getQuestions() {
        Collections.sort(this.questions, new Comparator<Question>() {
            @Override
            public int compare(Question question1, Question question2) {
                return Long.compare(question1.getQuestionTime(), question2.getQuestionTime());
            }
        });
        return this.questions;
    }

    private void generateEmptyAnswer(Question question) {
        iLogApplication.persistInMemoryEvent(new QA(question));
        removeQuestion(question);
        //remove from sharedprefs
    }

    public void generateAnswer(Question question, QA answer) {
        iLogApplication.persistInMemoryEvent(answer);
        System.out.println("Removed: "+removeQuestionByTime(question));
        if(this.getNumberOfQuestions()==0) {
            if (iLogApplication.notificationManager != null) {
                try {

                } catch(Exception e) {
                    iLogApplication.notificationManager.cancel(iLogApplication.sharedPreferences.getInt(Utils.CONFIG_QUESTIONNAIRENOTIFICATIONID, 0));
                }
            }
        }
        //remove from sharedprefs
    }

    private Question generateEmptyQuestion(JSONArray receivedQuestions, long questionTimestamp, String questiondid) throws JSONException {
        iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_NEWQUESTION, ""));

        Question question = new Question(questionTimestamp, System.currentTimeMillis(), receivedQuestions, questiondid);

        iLogApplication.saveQuestionSharedPreferences(question);

        return question;
    }

    public int getNumberOfQuestions() {
        return this.questions.size();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(int index=0;index<this.getNumberOfQuestions();index++) {
            stringBuilder.append(this.questions.get(index).toString()+" ");
        }
        return stringBuilder.toString();

    }

    public void filterOutdatedQuestions(ArrayList<Question> questions) {
        if(questions.size()>0) {
            ArrayList<Question> validQuestions = new ArrayList<Question>();
            for(int index=0; index<questions.size();index++) {
                if(questions.get(index).getQuestionTime()>(System.currentTimeMillis()-(iLogApplication.sharedPreferences.getInt(Utils.CONFIG_QUESTIONNAIRENUMBEROFQUESTIONS, 0) * iLogApplication.sharedPreferences.getInt(Utils.CONFIG_QUESTIONNAIRENOTIFICATIONINTERVAL, 0)))) {
                    validQuestions.add(questions.get(index));
                }
            }
            Log.d(iLogApplication.getAppContext().toString(), validQuestions.toString());
            this.questions = validQuestions;
        }
    }

    public boolean removeQuestionByTime(Question question) {
        for(int index=0;index<this.questions.size();index++) {
            if(this.questions.get(index).getQuestionTime()==question.getQuestionTime()) {
                return removeQuestion(this.questions.get(index));
            }
        }
        return false;
    }

    public boolean removeQuestion(Question question) {
        iLogApplication.removeQuestionSharedPreferences(question.getNotifiedTime());
        return this.questions.remove(question);
    }
    */
}
