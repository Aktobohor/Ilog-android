package it.unitn.disi.witmee.sensorlog.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.fragments.ContributionFragment;
import it.unitn.disi.witmee.sensorlog.model.Challenge;
import it.unitn.disi.witmee.sensorlog.model.Contribution;

/**
 * Activity that extends {@link FragmentActivity} and is intended to be the superclass for displaying the content to be presented to the user and to collect his feedback in a
 * {@link it.unitn.disi.witmee.sensorlog.model.Task}, {@link it.unitn.disi.witmee.sensorlog.model.Question} or {@link it.unitn.disi.witmee.sensorlog.model.Challenge}.
 */
public class ContributionActivity extends FragmentActivity {

    public Contribution contribution = null;
    public static int selectedFragment = 1;
    public static int suppostedSelectedFragment = 1;
    public static int numberOfSubQuestions = 0;
    public ContributionFragment previousFragment = null;
    public ContributionFragment actualFragment = null;
    public Menu menu = null;

    public FragmentManager fragmentManager = null;

    public ArrayList<Integer> fragmentSequence = new ArrayList<>();

    public long startingTime = 0;

    public HashMap<Integer, ArrayList<Integer>> answer = new HashMap<Integer, ArrayList<Integer>>();
    public HashMap<Integer, String> payload = new HashMap<Integer, String>();

    /**
     * Default method called when the Activity is created. It is mainly used to initialize the variables. Since it is a FragmentActivity it uses Fragments
     * to display content to the users and specifically to {@link ContributionFragment}
     * @param savedInstanceState what
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startingTime = System.currentTimeMillis();
        System.out.println("TIME: "+startingTime);
    }

    /**
     * Method called the the Activity is creating the option menu. We inflate the menu layout {@link R.menu#questionnaire_options_menu} and update the buttons in it depending
     *      * on the number of subquestions the main question has:
     *      * <ul>
     *      *     <li>The next button (top right) is set to Finish if there is only one question, to Next otherwise.</li>
     *      *     <li>The previous button is set to false independently because being in the first question we should not allow to go previously.</li>
     *      * </ul>
     * @param menu {@link Menu} object
     * @return default value true
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(this.toString(), "MENU CREATED");
        this.menu = menu;

        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.questionnaire_options_menu, menu);

        if(ContributionActivity.numberOfSubQuestions==1) {
            this.buttonNextSetText(R.string.finish);
        }
        else {
            this.buttonNextSetText(R.string.next);
            this.buttonNextStatus(true);
        }
        buttonPreviousStatus(false);

        return true;
    }

    /**
     * Method used to replace the current fragment with the next/previous one depending which button in the option menu the user presses. We start by removing the previous fragment
     * from the stack, we then create a new empty {@link ContributionFragment} and set the needed arguments. Finally, we add the new fragment to the Stack and commit.
     */
    public void replaceFragment() {

        previousFragment = actualFragment;
        if(previousFragment != null) {
            fragmentManager.beginTransaction().remove(previousFragment).commit();
            fragmentManager.popBackStackImmediate();
        }

        actualFragment = new ContributionFragment();
        Bundle args = new Bundle();
        try {
            args.putString("selectedfragment", new JSONArray(contribution.getContent()).get(selectedFragment-1).toString());
            args.putLong("notifiedtime", contribution.getNotifiedTime());
            args.putLong("questiontime", contribution.getInstanceTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        actualFragment.setArguments(args);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, actualFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Method called when the result of the procedure of asking a permission is generated. This refers to the permission of using the Camera before taking a picture.
     * The results can be either {@link PackageManager#PERMISSION_GRANTED} or {@link PackageManager#PERMISSION_DENIED}. In the former situation, we start the camera,
     * in the latter we close the activity by calling {@link #finish()}.
     * @param requestCode request code is used to identify the caller
     * @param permissions list of permissions in this request. Not used in the method since we request only one permission ({@link Manifest.permission#CAMERA})
     * @param grantResults list of results, for each permission requested, in this case the list has only one result at position 0
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case iLogApplication.CODE_RESULT_PERMISSIONS_SENSORS: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    actualFragment.startCamera();
                    return;
                }
                else if(grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    finish();
                    return;
                }
            }
        }
    }

    /**
     * Method used to detect when the user presses the back button on the smartphone. If this is the case, we remove all the fragments from the current stack and close the activity
     * calling {@link #finish()}.
     * @param keyCode code on the pressed key
     * @param event {@link KeyEvent} object
     * @return default boolean
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(previousFragment != null) {
                fragmentManager.beginTransaction().remove(previousFragment).commit();
                fragmentManager.popBackStackImmediate();
            }

            if(actualFragment != null) {
                fragmentManager.beginTransaction().remove(actualFragment).commit();
                fragmentManager.popBackStackImmediate();
            }

            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        Log.d(this.getClass().getSimpleName(), "Destroy");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.d(this.getClass().getSimpleName(), "Pause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(this.getClass().getSimpleName(), "Resume");
        super.onResume();
    }

    /**
     * When the activity is stopped (all those occasions when the activity is stopped, but mainly when the user presses the home button on the phone and the activity closes)
     * we close it with {@link #finish()}, we don't want to leave it open in the background (also because the activities in i-Log are removed from the recent activities' list).
     */
    @Override
    protected void onStop() {
        Log.d(this.getClass().getSimpleName(), "Stop");
        finish();
        super.onStop();
    }

    /**
     * Method called when there is the need to update the payload of a subquestion, it appends the new payload to the global one of the answer.
     * @param subquestionid Integer (from 0 to N-1) representing the subquestion we want to update
     * @param payload String containing the payload (a JSON object serialized) of the subquestion
     */
    public void updatePayload(int subquestionid, String payload) {
        this.getPayload().put(subquestionid, payload);
    }

    /**
     * Method that returns the answers' payload
     * @return {@link HashMap} containing the payload for the whole answer, composed by the single payloads of the subanswers
     */
    public HashMap<Integer, String> getPayload() {
        return this.payload;
    }

    /**
     * Method used to update the status (enabled or disabled) of the next button in the option menu. Additionally, when the status is updated we generate the index
     * for the next fragment to be displayed when the user pushes the button.
     * @param status True if the button is enabled, false otherwise
     */
    public void buttonNextStatus(boolean status) {
        if(status) {
            generateIndexForNextFragment();
        }
        if(menu!=null) {
            Log.d(this.toString(), "NEXT STATUS: "+status+"");
            menu.findItem(R.id.next).setEnabled(status);
        }
    }

    /**
     * Method used to update the status (enabled or disabled) of the previous button in the option menu.
     * @param status True if the button is enabled, false otherwise
     */
    public void buttonPreviousStatus(boolean status) {
        if(menu!=null) {
            menu.findItem(R.id.previous).setEnabled(status);
        }
    }

    /**
     * Method used to set the text of the next button (top right) in the option menu.
     * @param text String containing the text to be displayed
     */
    public void buttonNextSetText(int text) {
        if(menu!=null) {
            //Log.d(this.toString(), "Setting menu item title");
            menu.findItem(R.id.next).setTitle(text);
        }
    }

    /**
     * Method that returns the position (answerId) of the answer - TODO: check
     * @param subquestionid Integer (from 0 to N-1) representing the subquestion we want to update
     * @param answerid Integer (from 0 to N-1) representing the answer to the subquestion
     * @return Integer
     * @throws JSONException Exception thrown when a key is not in the {@link JSONObject}/{@link JSONArray}
     */
    private int answerIdByAnswer(int subquestionid, int answerid) throws JSONException {

        JSONArray question = new JSONArray(this.contribution.getContent());
        for(int index=0;index< question.length();index++) {
            JSONObject subQuestion = question.getJSONObject(index);
            if(subQuestion.getJSONObject("q").getInt("id")==(subquestionid)) {
                JSONArray subAnswers = subQuestion.getJSONArray("a");
                for(int index2=0;index2<subAnswers.length();index2++) {
                    JSONObject subAnser = subAnswers.getJSONObject(index2);
                    if(subAnser.getInt("id")==(answerid)) {
                        return subAnser.getInt("id");
                    }
                }
            }
        }
        return -2;
    }

    /**
     * Method that returns the concept ID (c_id) of the subanswer - TODO: check
     * @param subquestionid Integer (from 0 to N-1) representing the subquestion we want to update
     * @param answerid Integer (from 0 to N-1) representing the answer to the subquestion
     * @return Integer containing the id of the subanswer
     * @throws JSONException Exception thrown when a key is not in the {@link JSONObject}/{@link JSONArray}
     */
    private int conceptidByAnswer(int subquestionid, int answerid) throws JSONException {

        JSONArray question = new JSONArray(this.contribution.getContent());
        for(int index=0;index< question.length();index++) {
            JSONObject subQuestion = question.getJSONObject(index);
            if(subQuestion.getJSONObject("q").getInt("id")==(subquestionid)) {
                JSONArray subAnswers = subQuestion.getJSONArray("a");
                for(int index2=0;index2<subAnswers.length();index2++) {
                    JSONObject subAnser = subAnswers.getJSONObject(index2);
                    if(subAnser.getInt("id")==(answerid)) {
                        return subAnser.getInt("c_id");
                    }
                }
            }
        }
        return -2;
    }

    /**
     * Method that returns the concept text (t) of the subanswer
     * @param subquestionid Integer (from 0 to N-1) representing the subquestion we want to update
     * @param answerid Integer (from 0 to N-1) representing the answer to the subquestion
     * @return String containing the text of the subanswer
     * @throws JSONException Exception thrown when a key is not in the {@link JSONObject}/{@link JSONArray}
     */
    private String conceptStringByAnswer(int subquestionid, int answerid) throws JSONException {
        JSONArray question = new JSONArray(this.contribution.getContent());
        for(int index=0;index< question.length();index++) {
            JSONObject subQuestion = question.getJSONObject(index);
            if(subQuestion.getJSONObject("q").getInt("id")==subquestionid) {
                JSONArray subAnswers = subQuestion.getJSONArray("a");
                for(int index2=0;index2<subAnswers.length();index2++) {
                    JSONObject subAnswer = subAnswers.getJSONObject(index2);
                    if(subAnswer.getInt("id")==answerid) {
                        JSONObject language = subAnswer.getJSONArray("p").getJSONObject(getLanguageIndex());
                        return language.getString("t");
                    }
                }
            }
        }
        return "error";
    }

    /**
     * All the {@link Contribution} are made to have a multi language {@link Contribution#content}. This method returns the index of the language to be selected
     * (from the {@link JSONArray} of the content) so that to return the text in the correct language based on the {@link android.content.res.Configuration#locale}
     * set up on the smartphone. If the locale is not available in the {@link Contribution} the default one is selected.
     * @return Integer representing the index of the {@link JSONArray} at which we can find the desired language
     */
    public int getLanguageIndex() {
        Locale current = getResources().getConfiguration().locale;

        try {
            JSONObject subquestion = new JSONArray(contribution.getContent()).getJSONObject(selectedFragment-1).getJSONObject("q");

            for(int index=0;index<subquestion.getJSONArray("p").length();index++) {
                if(current.toLanguageTag().equals(subquestion.getJSONArray("p").getJSONObject(index).getString("l"))) {
                    return index;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getDefaultLanguage();
    }

    /**
     * Returns the index at which it is possible to find the defauls locale (en-US)
     * @return Integer representing the index of the {@link JSONArray} at which we can find the default language
     */
    public int getDefaultLanguage() {
        try {
            JSONObject subquestion = new JSONArray(contribution.getContent()).getJSONObject(selectedFragment-1).getJSONObject("q");

            for(int index=0;index<subquestion.getJSONArray("p").length();index++) {
                if("en-US".equals(subquestion.getJSONArray("p").getJSONObject(index).getString("l"))) {
                    return index;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Method used to generate the index of the next fragment {@link #suppostedSelectedFragment} based on the constraints present in the question, if any.
     */
    private void generateIndexForNextFragment() {
        ArrayList<Boolean> list = new ArrayList<Boolean>();
        suppostedSelectedFragment = selectedFragment;

        try {
            JSONArray subQuestions = new JSONArray(contribution.getContent());

            while(suppostedSelectedFragment < numberOfSubQuestions) {
                suppostedSelectedFragment++;

                //Constraints of the next question
                boolean result = showNextQuestion(subQuestions.getJSONObject(suppostedSelectedFragment-1).getJSONObject("q").getJSONArray("c"));
                list.add(result);
                if(result) {
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to verify if any of the next subquestions starting from the current one has as constraint the current one.
     * @param questiondID Integer representing the ID of the current subquestion
     * @return True if the constraint exists, false otherwise
     */
    public boolean anyNextHaveThisConstraint(int questiondID) {
        boolean toReturn = false;

        try {
            JSONArray questions = new JSONArray(contribution.getContent());

            //If this is not the last subquestion do the verification, otherwise return false
            if(questiondID == numberOfSubQuestions) {
                return false;
            } else {
                //loop over next questions
                Iterator it = getAnswers().entrySet().iterator();
                while (it.hasNext()) {
                    try {
                        Map.Entry pair = (Map.Entry)it.next();

                        for(int index=0; index<questions.length(); index++) {
                            if(index >= questiondID) {
                                JSONObject question = questions.getJSONObject(index);
                                //get constraints
                                JSONArray constraints = question.getJSONObject("q").getJSONArray("c");
                                for (int index2=0; index2<constraints.length(); index2++) {
                                    if(constraints.getJSONObject(index2).getInt("q") == Integer.valueOf(pair.getKey().toString()) && constraints.getJSONObject(index2).getInt("a") == Integer.valueOf(pair.getValue().toString().replace("[", "").replace("]", ""))) {
                                        toReturn = true;
                                    }
                                }
                                //Last modification, TODO - Test
                                if(constraints.length() == 0) {
                                    toReturn = true;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                return toReturn;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method that checks if we have to show the nextsubquestion
     * @param constraints {@link JSONArray} containing the constraints
     * @return True if the constraint exists, false otherwise
     */
    private boolean showNextQuestion(JSONArray constraints) {
        if(constraints.length()==0) {
            return true;
        }

        return skipQuestion(convertJSONArrayToArrayList(constraints), convertHasMapToArrayList());
    }

    /**
     * Method that returns the answers
     * @return {@link HashMap} containing all the answers to the subquestions
     */
    public HashMap<Integer, ArrayList<Integer>> getAnswers() {
        return this.answer;
    }

    public ArrayList<String> convertHasMapToArrayList() {
        ArrayList<String> arrayList = new ArrayList<String>();

        Iterator it = getAnswers().entrySet().iterator();
        while (it.hasNext()) {
            try {
                Map.Entry pair = (Map.Entry)it.next();
                JSONObject object = new JSONObject();
                object.put("q",pair.getKey());
                object.put("a",pair.getValue());
                arrayList.add(object.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

    /**
     * Methos used to convert a {@link JSONArray} to an {@link ArrayList}
     * @param constraints {@link JSONArray} to be converted
     * @return {@link ArrayList} of the converted {@link JSONArray}
     */
    public ArrayList<String> convertJSONArrayToArrayList(JSONArray constraints) {
        ArrayList<String> arrayList = new ArrayList<String>();
        for(int index=0;index<constraints.length();index++) {
            try {
                arrayList.add(constraints.getJSONObject(index).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }


    /**
     * Method that checks if we have to skip the next subquestion
     * @param constraints {@link ArrayList} containing the constraints
     * @param answers {@link ArrayList} containing the answers given up to now
     * @return True if the next subquestion is skipped, false otherwise
     */
    public boolean skipQuestion(ArrayList<String> constraints, ArrayList<String> answers) {
        HashMap<Integer, ArrayList<Integer>> constraintsByQuestion = convertConstraints(constraints);

        boolean skip = false;

        Iterator it = constraintsByQuestion.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            ArrayList<Integer> orderedConstraints = (ArrayList<Integer>) pair.getValue();

            if(orderedConstraints.contains(getAnsweridByQuestionid(answers, (int)pair.getKey()))) {
                skip = true;
            }
        }
        return skip;
    }

    /**
     * Methos used to convert a {@link ArrayList} to an {@link HashMap}
     * @param constraints {@link ArrayList} to be converted
     * @return {@link HashMap} of the converted {@link ArrayList}
     */
    public HashMap<Integer, ArrayList<Integer>> convertConstraints(ArrayList<String> constraints) {
        HashMap<Integer, ArrayList<Integer>> hashMap = new HashMap<Integer, ArrayList<Integer>>();

        try {
            for(int index=0;index<constraints.size();index++) {
                JSONObject object = new JSONObject(constraints.get(index));
                if(hashMap.containsKey(object.getInt("q"))) {
                    ArrayList<Integer> answers = hashMap.get(object.getInt("q"));
                    answers.add(object.getInt("a"));
                    hashMap.put(object.getInt("q"), answers);
                }
                else {
                    ArrayList<Integer> answers = new ArrayList<Integer>();
                    answers.add(object.getInt("a"));
                    hashMap.put(object.getInt("q"), answers);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return hashMap;
    }

    /**
     * Method that returns the answerid based on the subquestion
     * @param answers {@link ArrayList} of all the answers
     * @param questionid Integer representing the id of the subquestion to be checked
     * @return Integer representing the answer id
     */
    public int getAnsweridByQuestionid(ArrayList<String> answers, int questionid) {
        try {
            for(int index=0;index<answers.size();index++) {
                JSONObject answer = new JSONObject(answers.get(index));
                if(answer.getInt("q")==questionid) {
                    return Integer.valueOf(answer.getString("a").replace("[", "").replace("]", ""));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Method that converts the {@link #answer} (represented as an {@link HashMap}) to String to be pushed to the server
     * @return String representing the answers
     */
    public JSONArray answersToJSONArray() {
        JSONArray answers = new JSONArray();

        Iterator it = this.getAnswers().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            ArrayList<Integer> content = (ArrayList<Integer>) pair.getValue();

            JSONArray answerComponents = new JSONArray();
            for (int index = 0; index < content.size(); index++) {
                int singleAnswer = content.get(index);

                if(singleAnswer != -2) {
                    try {
                        JSONObject answer = new JSONObject();
                        answer.put("qid", (int) pair.getKey());
                        answer.put("aid", answerIdByAnswer((int) pair.getKey(), singleAnswer));
                        answer.put("cid", conceptidByAnswer((int) pair.getKey(), singleAnswer));
                        answer.put("cnt", conceptStringByAnswer((int) pair.getKey(), singleAnswer));
                        answerComponents.put(answer);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        JSONObject answer = new JSONObject();
                        answer.put("qid", (int) pair.getKey());
                        answer.put("aid", -2);
                        answer.put("cid", -2);
                        answer.put("cnt", "");
                        answerComponents.put(answer);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
            answers.put(answerComponents);
        }

        return answers;
    }

    /**
     * Method that converts the {@link #answer} (represented as an {@link HashMap}) to String to be pushed to the server
     * @return String representing the answers
     */
    public JSONArray payloadToJSONArray() {
        JSONArray answers = new JSONArray();

        Iterator it = this.getPayload().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            try {
                JSONObject answer = new JSONObject();
                answer.put("qid", (int) pair.getKey());
                answer.put("payload", new JSONObject((String) pair.getValue()));
                answers.put(answer);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return answers;
    }

    /**
     * Method that checks if the subquestion is a single selection or if multiple selection can be selected - TODO: check, not completed (QROWD)
     * @param answerType String representing the answers type
     * @return True if the subquestion is single selection, false otherwise
     */
    public boolean isSubQuestionSingleSelection(String answerType) {
        if(answerType.equals("s")) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Method that returns an {@link ArrayList} containing the answers to a specific subquestion, identified by its id
     * @param subquestionid Integer representing the id of the subquestion
     * @return {@link ArrayList} containing the answers
     */
    public ArrayList<Integer> getAnswersBySubQuestionId(int subquestionid) {
        if(this.getAnswers().get(subquestionid)!=null) {
            return this.getAnswers().get(subquestionid);
        }
        return new ArrayList<Integer>(Arrays.asList(-1));
    }

    /**
     * Method used to update the answer of a subquestion which has already been answered previously. An @link{#answerid} equal to -2 means an epty answer.
     * @param subquestionid Integer representing the id of the question
     * @param answerid Integer representing the id of the new answer
     * @param answerType String representing the type of the answer, single choice or multiple choice
     */
    public void updateAnswer(int subquestionid, int answerid, String answerType) {
        //Single selection
        if(isSubQuestionSingleSelection(answerType)) {
            //System.out.println(answerid);
            this.getAnswers().put(subquestionid, new ArrayList<>(Arrays.asList(answerid)));
        }
        //Multiple selection
        else {
            //If already exists need to remove it, otherwise add it
            if(this.getAnswers().containsKey(subquestionid)) {
                ArrayList<Integer> answers = this.getAnswers().get(subquestionid);
                System.out.println(answers);
                if(answers.contains(answerid)) {
                    answers.remove(new Integer(answerid));
                    if(answers.size() == 0) {
                        this.getAnswers().remove(subquestionid);
                    }
                }
                else {
                    answers.add(answerid);
                }
            }
            else {
                this.getAnswers().put(subquestionid, new ArrayList<>(Arrays.asList(answerid)));
            }
        }
    }

    /**
     * Method that checks if the answer to a subquestion has a payload
     * @param questionid Integer representing the id of the subquestion to be checked
     * @return True if the payload exists, false otherwise
     */
    private boolean subquestionHasPayload(int questionid) {
        Iterator it = getPayload().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if((int) pair.getKey() == questionid) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method that returns the payload of the answer to a subquestion, identified by its id
     * @param questionid Integer representing the id of the subquestion
     * @return String with the payload
     */
    private String getAnswersPayloadBySubquestionId(int questionid) {
        Iterator it = getPayload().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if((int) pair.getKey() == questionid) {
                return String.valueOf(pair.getValue());
            }
        }
        return null;
    }
}

