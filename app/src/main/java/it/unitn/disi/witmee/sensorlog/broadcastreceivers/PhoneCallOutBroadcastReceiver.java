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
import it.unitn.disi.witmee.sensorlog.model.virtual.CO;
import it.unitn.disi.witmee.sensorlog.model.virtual.SI;
import it.unitn.disi.witmee.sensorlog.runnables.SmsInRunnable;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link CO} event
 */
public class PhoneCallOutBroadcastReceiver extends BroadcastReceiver {

    long startTime = 0;
    long endTime = 0;
    long duration = 0;
    String contactNumber = "";
    String contactName = "";

    boolean outCallStarted = false;

    /**
     * Method called when the {@link Intent} is received. The intent we are interested in is {@link Intent#ACTION_NEW_OUTGOING_CALL} or {@link TelephonyManager#ACTION_PHONE_STATE_CHANGED}.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
            contactNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            contactName = getContactName(iLogApplication.getAppContext(), contactNumber);
            outCallStarted = true;
            startTime = System.currentTimeMillis();
        }
        else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
            if (outCallStarted) {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

                Log.d(this.toString(), state);

                if (state != null && state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    endTime = System.currentTimeMillis();
                    duration = (endTime-startTime);
                    outCallStarted = false;
                }
                else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    startTime = System.currentTimeMillis();
                }

                CO phoneCallOutEvent = new CO(System.currentTimeMillis(), 0, contactNumber, contactName, startTime, endTime, duration);
                Log.d(this.toString(), phoneCallOutEvent.getContactName());
                iLogApplication.persistInMemoryEvent(phoneCallOutEvent);
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
}
