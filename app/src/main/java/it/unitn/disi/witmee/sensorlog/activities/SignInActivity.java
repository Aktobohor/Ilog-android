package it.unitn.disi.witmee.sensorlog.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;

/**
 * Activity called when the user is required to manually sign in using {@link GoogleSignInAccount}.
 */
public class SignInActivity extends Activity {

    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Requires the user to login
        iLogApplication.requestUserLogin(this);
    }

    @Override
    public void onPause() {
    	super.onPause();
        Log.d(this.toString(), "PAUSED");
    }
    
    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(this.getClass().getSimpleName(), "Stop");
        finish();
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(this.toString(), "DESTROYED");
    }

    /**
     * Method called when the result of the user login call is generated.
     * @param requestCode Integer representing the request code calling the method
     * @param resultCode Integer representing the result code of the action that calls this method
     * @param data {@link Intent} representing the data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case iLogApplication.CODE_RESULT_ACCOUNT: {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = null;
                try {
                    account = task.getResult(ApiException.class);
                } catch (ApiException e) {
                    e.printStackTrace();
                }
                finish();
                return;
            }
        }
    }
}