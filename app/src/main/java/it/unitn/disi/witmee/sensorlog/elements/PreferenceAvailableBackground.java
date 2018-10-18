package it.unitn.disi.witmee.sensorlog.elements;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.activities.HomeActivity;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.Challenge;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Custom Preference screen that allows to display available {@link Challenge} objects in the dedicated menu.
 */
public class PreferenceAvailableBackground extends CustomPreference {

    @Override
    public CharSequence getTitle() {
        return super.getTitle();
    }

    public PreferenceAvailableBackground(Challenge availableChallenge, Activity activity) {
        super(availableChallenge, activity);
        setLayoutResource(R.layout.challenge_available_background);
    }

    /**
     * Method where the layout elements that characterize the view are specified. This specific view is an element of a Preference Screen, which by default is a
     * lis tof elements, but in this case we want to show a single, full screen element.
     * @param view default return
     */
    @Override
    protected void onBindView(final View view) {
        super.onBindView(view);

        int height = (getScreenHeight(getActivity()) - getStatusBarHeight(getActivity()) - getActionBarHeight(getActivity()));

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.mainChallengeView);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
        linearLayout.setLayoutParams(layoutParams);

        final TextView challengeType = (TextView) view.findViewById(R.id.challengeType);
        TextView challengeStart = (TextView) view.findViewById(R.id.challengeStart);
        TextView challengeEnd = (TextView) view.findViewById(R.id.challengeEnd);
        final TextView challengeDescription = (TextView) view.findViewById(R.id.challengeDescription);
        TextView challengeReward = (TextView) view.findViewById(R.id.challengeReward);
        TextView challengeRules = (TextView) view.findViewById(R.id.challengeRules);
        Button participateButton = (Button) view.findViewById(R.id.participateButton);

        challengeType.setText(getAvailableChallenge().getType());
        challengeStart.setText(Utils.changeDateStringFormat(getAvailableChallenge().getStartdate()));
        challengeEnd.setText(Utils.changeDateStringFormat(getAvailableChallenge().getEnddate()));

        challengeType.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                challengeType.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                challengeDescription.setMaxLines(getDescriptionHeight(challengeType.getPaddingTop(), challengeType.getHeight()) / challengeType.getHeight());
            }
        });

        //Since the description field is srollable, we need to override this method to avoid interferences with the scroll of the Preference Screen
        challengeDescription.setText(getAvailableChallenge().getDescription());
        challengeDescription.setMovementMethod(new ScrollingMovementMethod());
        View.OnTouchListener listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean isLarger;
                isLarger = ((TextView) v).getLineCount() * ((TextView) v).getLineHeight() > v.getHeight();
                if (event.getAction() == MotionEvent.ACTION_MOVE && isLarger) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        };
        challengeDescription.setOnTouchListener(listener);
        challengeReward.setText(String.valueOf(getAvailableChallenge().getPointsawarded()));
        challengeRules.setText(getAvailableChallenge().getInstructions());
        Linkify.addLinks(challengeRules, Linkify.WEB_URLS);

        /**
         Adds the {@link #availableChallenge} to the database after having updated the {@link Challenge#participationtime} with {@link Challenge#setParticipationtime(String)}, Then it logins and
         tries to upload the challenges participation info to the database with {@link iLogApplication#uploadChallengesParticipationInfo(String)}. Finally it exits and
         goes to the main menu.
         */
        participateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAvailableChallenge().setParticipationtime(Utils.longToStringFormat(System.currentTimeMillis()));
                iLogApplication.db.addChallenge(getAvailableChallenge());

                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), iLogApplication.gso);
                googleSignInClient.silentSignIn()
                        .addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
                            @Override
                            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                                try {
                                    GoogleSignInAccount account = task.getResult(ApiException.class);
                                    String idToken = account.getIdToken();

                                    iLogApplication.uploadChallengesParticipationInfo(idToken);
                                } catch (ApiException e) {
                                    e.printStackTrace();
                                    if(e.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED && !iLogApplication.sharedPreferences.getString(Utils.CONFIG_PROJECTDATA, "").equals("")) {
                                        iLogApplication.startSignInActivity();
                                    }
                                }
                            }
                        });
                Intent myIntent = new Intent(getActivity(), HomeActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().startActivity(myIntent);
            }
        });
    }

    /**
     * Method that calculates the height of the description, based on the height of a single line and of the padding we selected in the view
     * @param paddingTop Height of the padding used in the view
     * @param singleLineHeight Height of a single line
     * @return Integer representing the description field
     */
    public int getDescriptionHeight(int paddingTop, int singleLineHeight) {
        return getAvailableScreenHeight() - ((paddingTop * 5)+(getAvailableScreenHeight() / 3) + (10 * singleLineHeight));
    }
}
