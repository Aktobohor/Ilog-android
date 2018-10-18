package it.unitn.disi.witmee.sensorlog.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Activity that shows the informed consent to the user while the selection of an experiment.
 */
public class InformedConsentActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.informed_consent_activity);

        //Initialize the TextView where to visualize the text
        final TextView informedConsentTextView = (TextView) findViewById(R.id.informedConsentTextView);
        informedConsentTextView.setMovementMethod(new ScrollingMovementMethod());

        //Load the text from the object of the project
        try {
            JSONObject project = new JSONObject(iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, ""));
            String informedconsent = project.getString("informedconsent");
            informedConsentTextView.setText(new JSONObject(informedconsent).getString(iLogApplication.getLocale()));
        } catch (JSONException e) {
            informedConsentTextView.setText("generic error");
            e.printStackTrace();
        }

        //Checkbox that needs to be chacked to proceed
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setText(getResources().getString(R.string.authorize));

        /**
         * Click that identifies the action of the usr to grant her consent. It returns a result to the calling activity using {@link setResult(int)}
         */
        TextView confirmTextBox = (TextView) findViewById(R.id.confirmationButton);
        confirmTextBox.setText(getResources().getString(R.string.confirm_message));
        confirmTextBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked()) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", "OK");
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
                else {
                    Toast.makeText(iLogApplication.getAppContext(), iLogApplication.getAppContext().getResources().getString(R.string.give_consent), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
