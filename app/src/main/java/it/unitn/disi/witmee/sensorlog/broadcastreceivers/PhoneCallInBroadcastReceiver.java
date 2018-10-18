package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.virtual.CI;
import it.unitn.disi.witmee.sensorlog.model.virtual.CO;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link CI} event
 */
public class PhoneCallInBroadcastReceiver extends BroadcastReceiver {

    long startTime = 0;
    long endTime = 0;
    long duration = 0;
    String contactNumber = "";
    String contactName = "";
    String status = CI.CALL_LOST;
    boolean inCallStarted = false;

    /**
     * Method called when the {@link Intent} is received. The intent we are interested in is {@link TelephonyManager#ACTION_PHONE_STATE_CHANGED}.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            Log.d(this.toString(), state);

            if(state != null && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                startTime = System.currentTimeMillis();
                contactNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                contactName = getContactName(iLogApplication.getAppContext(), contactNumber);
                inCallStarted = true;
            }
            else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                if (inCallStarted) {
                    endTime = System.currentTimeMillis();
                    duration = (endTime-startTime);

                    CI phoneCallInEvent = new CI(System.currentTimeMillis(), 0, contactNumber, contactName, status, startTime, endTime, duration);
                    iLogApplication.persistInMemoryEvent(phoneCallInEvent);

                    inCallStarted = false;
                }
            }
            else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                if (inCallStarted) {
                    startTime = System.currentTimeMillis();
                    status = CI.CALL;
                }
            }
        }
    }

    /**
     * Method that returns the contact name of the user who sent the received sms, if it is available in the agenda.
     * @param context {@link Context} object
     * @param phoneNumber String representing the phone number to be reversed
     * @return String representing the name according to which the sender is saved in the agenda on the phone
     */
    public static String getContactName(Context context, String phoneNumber) {
        try {
            ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor == null) {
                return null;
            }
            String contactName = null;
            if(cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }

            if(cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            return contactName;
        }
        catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
