package it.unitn.disi.witmee.sensorlog.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.fragments.ContributionFragment;
import it.unitn.disi.witmee.sensorlog.fragments.MessageFragment;
import it.unitn.disi.witmee.sensorlog.model.Answer;
import it.unitn.disi.witmee.sensorlog.model.Message;
import it.unitn.disi.witmee.sensorlog.model.Task;

/**
 * Activity that display a {@link Message} to the user and allows him to interact with it.
 */
public class MessageActivity extends FragmentActivity {

    private Message message = null;
    static int selectedFragment = 0;
    public ArrayList<MessageFragment> fragments = new ArrayList<MessageFragment>();
    Menu menu = null;

    static FragmentManager fragmentManager = null;

    long startingTime = 0;

    /**
     * Default method called when the Activity is created. It is mainly used to initialize the variables. Since it is a FragmentActivity it uses Fragments
     * to display content to the users and specifically to {@link ContributionFragment}
     * @param savedInstanceState what
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        fragmentManager = getSupportFragmentManager();

        Intent intent = getIntent();
        selectedFragment = 0;
        fragments = new ArrayList<MessageFragment>();
        message = (Message) intent.getSerializableExtra("message");

        try {
            JSONArray subMessages = new JSONObject(message.getContent()).getJSONArray("pg");
            for(int index=0; index<subMessages.length();index++) {
                JSONObject singleMessage = subMessages.getJSONObject(index);

                MessageFragment actualFragment = new MessageFragment();

                Bundle args = new Bundle();
                System.out.println(singleMessage.toString());
                args.putString("selectedfragment", singleMessage.toString());
                args.putLong("notifiedtime", message.getNotifiedTime());
                args.putLong("questiontime", message.getMessageTimestamp());

                actualFragment.setArguments(args);
                fragments.add(actualFragment);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        replaceFragment();

        startingTime = System.currentTimeMillis();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * Method called the the Activity is creating the option menu. We inflate the menu layout {@link R.menu#questionnaire_options_menu} and update the buttons in it depending
     * on the number of subquestions the main question has:
     * <ul>
     *     <li>The next button (top right) is set to Finish if there is only one question, to Next otherwise.</li>
     *     <li>The previous button is set to false independently because being in the first question we should not allow to go previously.</li>
     * </ul>
     * @param menu {@link Menu} object
     * @return default value true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.messages_options_menu, menu);
        this.menu = menu;

        if(fragments.size() == 1) {
            buttonPreviousStatus(false);
            buttonNextStatus(false);
        }
        else {
            buttonPreviousStatus(false);
            buttonNextStatus(true);
        }

        buttonCloseStatus(true);
        return true;
    }

    /**
     * Method triggered when an item (button) in the option menu is pressed by the user. If the button pressed is the Previous, we remove the current fragment and replace it
     * with the previous. If instead the button is the Next one, we replace it with the next. Additionally, there is the Close button, which saves the {@link Answer} for the
     * message (empty).
     * @param item {@link MenuItem} pressed
     * @return Default return True
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.next:
                selectedFragment++;
                replaceFragment();

                return true;

            case R.id.previous:
                selectedFragment--;
                replaceFragment();

                return true;

            case R.id.close:
                Answer answer = new Answer(message.getMessageTimestamp(), System.currentTimeMillis(), message.getNotifiedTime(), System.currentTimeMillis()-startingTime, new JSONArray(), new JSONArray(), message.getMessageid(), Answer.TYPE_MESSAGE, Answer.SYNCHRONIZATION_FALSE, Answer.SYNCHRONIZATION_FALSE);
                iLogApplication.db.addAnswer(answer);
                iLogApplication.db.updateMessage(message, Message.STATUS_READ);
                iLogApplication.uploadAllContributions();

                iLogApplication.updateMessageNotification();

                if(iLogApplication.db.getAllMessagesByStatus(Message.STATUS_UNREAD).size() > 0) {
                    Intent notificationIntent = new Intent(iLogApplication.getAppContext(), HomeActivity.class);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    notificationIntent.putExtra(iLogApplication.INTENT_TASK, "");
                    startActivity(notificationIntent);
                }
                else {
                    finish();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Method used to replace the current fragment with the next/previous one depending which button in the option menu the user presses.
     */
    public void replaceFragment() {
        if (findViewById(R.id.fragment_container) != null && selectedFragment<fragments.size()) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragments.get(selectedFragment));
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    /**
     * Method used to detect when the user presses the back button on the smartphone. If this is the case, we close the activity by calling {@link #finish()}.
     * @param keyCode code on the pressed key
     * @param event {@link KeyEvent} object
     * @return default boolean
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
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

    @Override
    protected void onStop() {
        Log.d(this.getClass().getSimpleName(), "Stop");
        finish();
        super.onStop();
    }

    /**
     * Method used to update the status (enabled or disabled) of the next button in the option menu. Additionally, when the status is updated we generate the index
     * for the next fragment to be displayed when the user pushes the button.
     * @param status True if the button is enabled, false otherwise
     */
    public void buttonNextStatus(boolean status) {
        if(menu!=null) {
            System.out.println("SETTING NEXT STATUS "+status);
            menu.findItem(R.id.next).setEnabled(status);
        }
    }

    /**
     * Method used to update the status (enabled or disabled) of the previous button in the option menu.
     * @param status True if the button is enabled, false otherwise
     */
    public void buttonPreviousStatus(boolean status) {
        if(menu!=null) {
            System.out.println("SETTING PREVIOUS STATUS "+status);
            menu.findItem(R.id.previous).setEnabled(status);
        }
    }

    /**
     * Method used to update the status (enabled or disabled) of the Close button in the option menu.
     * @param status True if the button is enabled, false otherwise
     */
    public void buttonCloseStatus(boolean status) {
        if(menu!=null) {
            menu.findItem(R.id.close).setEnabled(status);
        }
    }
}

