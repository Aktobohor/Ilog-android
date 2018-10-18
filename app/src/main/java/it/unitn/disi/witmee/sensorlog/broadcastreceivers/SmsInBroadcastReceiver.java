package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.ambience.WN;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.virtual.SI;
import it.unitn.disi.witmee.sensorlog.runnables.SmsInRunnable;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link SI} event
 */
public class SmsInBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. The intent we are interested in is {@link SmsInRunnable#SMS_RECEIVED}.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(SmsInRunnable.SMS_RECEIVED)) {

            Log.d(this.toString(), action);

            //Converting bundle to SI event
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[])bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                }
                if (messages.length > -1) {
                    SI smsInEvent = new SI(System.currentTimeMillis(), 0, messages[0].getOriginatingAddress(), getContactName(iLogApplication.getAppContext(), messages[0].getOriginatingAddress()));
                    iLogApplication.persistInMemoryEvent(smsInEvent);
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
