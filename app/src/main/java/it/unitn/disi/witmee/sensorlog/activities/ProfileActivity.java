package it.unitn.disi.witmee.sensorlog.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.ybs.countrypicker.CountryPicker;
import com.ybs.countrypicker.CountryPickerListener;
import org.json.JSONArray;
import org.json.JSONException;
import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.fragments.ContributionFragment;
import it.unitn.disi.witmee.sensorlog.model.Question;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Activity that askes the user simple questions that will characterize her profile.
 */
public class ProfileActivity extends ContributionActivity {

    private Button btnSavechanges,btnDeleteChanges;
    private EditText editTextUseranme,editTextName, editTextSurname;
    private ImageButton btnDatePiker,flagCountryPiker;
    private TextView textBornDate,textCountry;
    private int mYear, mMonth, mDay;
    private DatePickerDialog datePickerDialog;
    private CountryPicker picker;
    /**
     * Default method called when the Activity is created. It is mainly used to initialize the variables. Since it is a FragmentActivity it uses Fragments
     * to display content to the users and specifically to {@link ContributionFragment}
     * @param savedInstanceState what
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        //comando universale per settare il contesto della pagina su cui la singleton instanzia le componenti come il menu.
        ILog_CommonMethod.getInstance().setCurrentContext(getBaseContext());

        //inizializzo tutte le componenti del layout in xml
        btnSavechanges = findViewById(R.id.btn_conferma);
        btnDeleteChanges = findViewById(R.id.btn_annulla);
        editTextUseranme = findViewById(R.id.profile_username);
        editTextName = findViewById(R.id.profile_name);
        editTextSurname = findViewById(R.id.profile_surname);
        flagCountryPiker = findViewById(R.id.flag_country_piker);
        textCountry = findViewById(R.id.text_country);
        btnDatePiker = findViewById(R.id.btn_date_piker);
        textBornDate = findViewById(R.id.text_born_date);

        //creo il datePiker per acquisire la data.
        datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        textBornDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);

                    }
                    //acquisisco anche le variabile per andare a settare se ci sarà una
                    // variabile della data di nascita oltre che settare il campo della data
                }, mYear, mMonth, mDay);


        //istanzio il listener sul click del bottone con il calendario per
        // mostrare il datePiker per selezionare la data.

        btnDatePiker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.updateDate(2000, 0, 1);
                datePickerDialog.show();
            }
        });


        picker = CountryPicker.newInstance("Select Country");  // dialog title
        picker.setListener(new CountryPickerListener() {
            @Override
            public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                flagCountryPiker.setImageDrawable(getResources().getDrawable(flagDrawableResID));
                textCountry.setText(name);
                picker.dismiss();
            }
        });
        flagCountryPiker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });



        selectedFragment = 1;

        fragmentManager = getSupportFragmentManager();

        //Add first question
        fragmentSequence.add(1);

        Intent intent = getIntent();
        contribution = (Question) intent.getSerializableExtra("question");

        /*try {
            numberOfSubQuestions = new JSONArray(contribution.getContent()).length();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            actualFragment = new ContributionFragment();

            Bundle args = new Bundle();
            try {
                System.out.println(new JSONArray(contribution.getContent()).get(selectedFragment - 1).toString());
                args.putString("selectedfragment", new JSONArray(contribution.getContent()).get(selectedFragment - 1).toString());
                args.putLong("notifiedtime", contribution.getNotifiedTime());
                args.putLong("questiontime", contribution.getInstanceTime());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            actualFragment.setArguments(args);

            fragmentManager.beginTransaction().replace(R.id.fragment_container, actualFragment).commit();
        }

        //startingTime = System.currentTimeMillis();
    }

    /**
     * Method triggered when an item (button) in the option menu is pressed by the user. If the button pressed is the Previous, we remove the current fragment and replace it
     * with the previous. If instead the button is the next one, two things can occur:
     * <ul>
     *     <li>The button is set as Next, which means that we remove the current fragment and replace it with the next</li>
     *     <li>The button is set as Previous, which means that we remove the current fragment and replace it with the next</li>
     *     <li>The button is set as Finish, in this case the answering process is finished and we need to store the user profile to the {@link iLogApplication#sharedPreferences}
     *     Finally, a result is sent back to the calling activity through {@link #setResult(int)} and then this activity is closed using {@link #finish()}.</li>
     * </ul>
     * @param item {@link MenuItem} pressed
     * @return Default return True
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        MenuItem nextItem = menu.findItem(R.id.next);

        switch (item.getItemId()) {
            case R.id.next:
                if (nextItem.getTitle().equals(getString(R.string.finish))) {
                    System.out.println("Finish, saving");

                    fragmentSequence.clear();

                    iLogApplication.sharedPreferences.edit().putString(Utils.CONFIG_PROFILEANSWERS, answersToJSONArray().toString()).commit();

                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    fragmentSequence.add(suppostedSelectedFragment);
                    System.out.println("Sequence: " + fragmentSequence.toString());
                    selectedFragment = suppostedSelectedFragment;
                    replaceFragment();
                    System.out.println("Answers: " + this.getAnswers());
                }

                return true;

            case R.id.previous:
                selectedFragment = fragmentSequence.remove(fragmentSequence.size() - 2);
                if (selectedFragment == 1) {
                    fragmentSequence.add(1);
                }
                System.out.println("Sequence: " + fragmentSequence.toString());
                replaceFragment();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

