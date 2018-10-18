package it.unitn.disi.witmee.sensorlog.activities;

/**
 * Created by mattiazeni on 5/23/17.
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.fragments.ContributionFragment;
import it.unitn.disi.witmee.sensorlog.model.Answer;
import it.unitn.disi.witmee.sensorlog.model.Challenge;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Activity that extends {@link ContributionActivity} and is intended to display the content of a {@link Challenge}.
 */
public class ChallengeStaticActivity extends ContributionActivity {

    String freeroamMapReferenceId = null;

    /**
     * Default method called when the Activity is created. It is mainly used to initialize the variables. Since it is a FragmentActivity it uses Fragments
     * to display content to the users and specifically to {@link ContributionFragment}
     * @param savedInstanceState what
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        selectedFragment = 1;
        fragmentManager = getSupportFragmentManager();
        fragmentSequence.add(1);

        Intent intent = getIntent();
        contribution = (Challenge) intent.getSerializableExtra("challenge");

        try {
            freeroamMapReferenceId = (String) intent.getSerializableExtra("freeroamMapReferenceId");
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            numberOfSubQuestions = new JSONArray(contribution.getContent()).length();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            actualFragment = new ContributionFragment();

            Bundle args = new Bundle();
            try {
                System.out.println(new JSONArray(contribution.getContent()).get(selectedFragment-1).toString());
                args.putString("selectedfragment", new JSONArray(contribution.getContent()).get(selectedFragment-1).toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            actualFragment.setArguments(args);

            fragmentManager.beginTransaction().replace(R.id.fragment_container, actualFragment).commit();
        }

        //Variable containing the starting time of this Activity, used to monitor the time used to answer
        //startingTime = System.currentTimeMillis();
    }

    /**
     * Method triggered when an item (button) in the option menu is pressed by the user. If the button pressed is the Previous, we remove the current fragment and replace it
     * with the previous. If instead the button is the next one, two things can occur:
     * <ul>
     *     <li>The button is set as Next, which means that we remove the current frgment and replace it with the next</li>
     *     <li>The button is set as Finish, in this case the answering process is finished and we need to store the answer in the database. If the maximum number of allowed
     *     answers for this challenge have been reached, update the challenge. Finally, start the {@link HomeActivity} and close this one using {@link #finish()}.</li>
     * </ul>
     * @param item {@link MenuItem} pressed
     * @return Default return True
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        MenuItem nextItem = menu.findItem(R.id.next);

        switch (item.getItemId()) {
            case R.id.next:
                if(nextItem.getTitle().equals(getString(R.string.finish))) {
                    System.out.println("Finish, saving");

                    fragmentSequence.clear();

                    long answerTime = System.currentTimeMillis();

                    //long instancetime, long answerTime, long notifiedTime, long answerduration, String answers, String instanceid, String type, String synchronization
                    if(freeroamMapReferenceId != null) {
                        JSONArray payload = payloadToJSONArray();
                        try {
                            payload.put(new JSONObject().put("payload", freeroamMapReferenceId).put("qid", -1));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Answer answer = new Answer(Utils.stringToLongFormat(((Challenge) contribution).getParticipationtime()), answerTime, answerTime, answerTime-startingTime, answersToJSONArray(), payload, contribution.getInstanceid(), Answer.TYPE_CHALLENGE, Answer.SYNCHRONIZATION_FALSE, Answer.SYNCHRONIZATION_FALSE);
                        iLogApplication.db.addAnswer(answer);
                    }
                    else {
                        Answer answer = new Answer(Utils.stringToLongFormat(((Challenge) contribution).getParticipationtime()), answerTime, answerTime, answerTime-startingTime, answersToJSONArray(), payloadToJSONArray(), contribution.getInstanceid(), Answer.TYPE_CHALLENGE, Answer.SYNCHRONIZATION_FALSE, Answer.SYNCHRONIZATION_FALSE);
                        iLogApplication.db.addAnswer(answer);
                    }

                    iLogApplication.uploadAllContributions();

                    try {
                        if(iLogApplication.db.getAllAnswersByInstanceId(contribution.getInstanceid()).size() == new JSONObject(((Challenge) contribution).getConstraints()).getInt("max")) {
                            iLogApplication.db.updateChallengeStatus((Challenge) contribution, Challenge.STATUS_COMPLETED);
                            Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getString(R.string.challenges_completed), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent homeIntent = new Intent(iLogApplication.getAppContext(), HomeActivity.class);
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    homeIntent.putExtra(iLogApplication.INTENT_TASK, "");
                    startActivity(homeIntent);
                    finish();
                }
                else {
                    fragmentSequence.add(suppostedSelectedFragment);
                    System.out.println("Sequence: "+fragmentSequence.toString());
                    selectedFragment = suppostedSelectedFragment;
                    replaceFragment();
                }

                return true;

            case R.id.previous:
                selectedFragment = fragmentSequence.remove(fragmentSequence.size() - 2);
                if(selectedFragment==1) {
                    fragmentSequence.add(1);
                }
                replaceFragment();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

