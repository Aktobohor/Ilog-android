package it.unitn.disi.witmee.sensorlog.runnables;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.SmsInBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.system.ST;
import it.unitn.disi.witmee.sensorlog.model.virtual.CI;
import it.unitn.disi.witmee.sensorlog.model.virtual.SO;

/**
 * Class that implements a {@link Runnable} that manages the data collection of the {@link it.unitn.disi.witmee.sensorlog.model.virtual.SO} event. The data collection occurs through a
 * {@link SmsOutObserver}. It is registered/unregistered in this class and the data of type {@link it.unitn.disi.witmee.sensorlog.model.virtual.SO} event is generated in it.
 * TODO - need to check with Android greater then 8.0 if this still works
 * @author Mattia Zeni
 */
public class SmsOutRunnable implements Runnable {

    private volatile boolean isStopped = false;
    private static int SENSOR_ID = iLogApplication.SMS_OUT_ID;

    private static final String CONTENT_SMS = "content://sms/";
    ContentResolver contentResolver = null;
    SmsOutObserver contentObserver = null;

    /**
     * Method that starts the collection of the {@link CI} events. It performs the following operations:
     * <ul>
     *     <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *     <li>Initializes the {@link #contentObserver} variable where the detection of the mode change will occur</li>
     *     <li>Registers the receiver {@link #contentObserver} using the {@link ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)}
     *         method for an {@link Uri} of type {@link #CONTENT_SMS}</li>
     * </ul>
     */
    public void run() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            isStopped = false;

            if(!iLogApplication.sensorLoggingState.get(SENSOR_ID) && iLogApplication.hasSinglePermission(Manifest.permission.READ_SMS)) {

                iLogApplication.startLoggingMonitoringService();
                Log.d(this.getClass().getSimpleName(), "Start");

                iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), true));
                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
                iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STARTED, this.getClass().getSimpleName()));

                contentObserver = new SmsOutObserver();
                contentResolver = iLogApplication.getAppContext().getContentResolver();
                contentResolver.registerContentObserver(Uri.parse(CONTENT_SMS),true, contentObserver);
            }
        }
    }

    /**
     * Contains information about the status of the data collection for this {@link SO} event
     * @return true if the data collection is stopped, false otherwise
     */
    public boolean isStopped() {
        return isStopped;
    }

    /**
     * Method that updates the status of the Runnable
     * @param isStop boolean value that identifies the status, true if the data collection is stopped, false otherwise
     */
    private void setStopped(boolean isStop) {
        if (isStopped != isStop)
            isStopped = isStop;

        iLogApplication.stopLoggingMonitoringService();
    }

    /**
     * Method that stops the collection of the {@link it.unitn.disi.witmee.sensorlog.model.virtual.SO} events. It performs the following operations:
     * <ul>
     *     <li>Unregisters the {@link #contentObserver} using the {@link ContentResolver#unregisterContentObserver(ContentObserver)} method to stop receiving updates</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Sets this runnable as stopped</li>
     * </ul>
     */
    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(contentResolver!=null) {
                if(contentObserver!=null) {
                    try {
                        contentResolver.unregisterContentObserver(contentObserver);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), false));
            iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STOPPED, this.getClass().getSimpleName()));
            iLogApplication.sensorLoggingState.put(SENSOR_ID, false);
            Log.d(this.getClass().getSimpleName(), "Stop");

            setStopped(true);
        }
    }

    /**
     * Method that restarts the collection of the {@link it.unitn.disi.witmee.sensorlog.model.virtual.SO} events. It performs the following operations:
     * <ul>
     *     <li>Unregister the {@link #contentObserver} using the {@link ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)} method to stop receiving updates</li>
     *     <li>Registers the receiver {@link #contentObserver} using the {@link ContentResolver#registerContentObserver(Uri, boolean, ContentObserver)}
     *     method for actions of type {@link #CONTENT_SMS}</li>
     * </ul>
     */
    public void restart() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped()) {
                if(contentResolver!=null) {
                    if(contentObserver!=null) {
                        try {
                            contentResolver.unregisterContentObserver(contentObserver);
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);

                contentObserver = new SmsOutObserver();
                contentResolver = iLogApplication.getAppContext().getContentResolver();
                contentResolver.registerContentObserver(Uri.parse(CONTENT_SMS),true, contentObserver);
            }
        }
    }

    /**
     * Class that extends the {@link ContentObserver} class and is used to monitor outgoing sms.
     * @see <a href="https://stackoverflow.com/questions/5808577/listen-outgoing-sms-or-sent-box-in-android">https://stackoverflow.com/questions/5808577/listen-outgoing-sms-or-sent-box-in-android</a>
     */
    private class SmsOutObserver extends ContentObserver {

        public SmsOutObserver() {
            super(null);
        }

        /**
         * Method calles when an sms is sent. We parse the response and extract the useful information we need for the {@link SO} object
         * @param selfChange boolean value, not used
         */
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Uri uriSMSURI = Uri.parse(CONTENT_SMS);
            Cursor cur = iLogApplication.getAppContext().getContentResolver().query(uriSMSURI, null, null, null, null);
            // this will make it point to the first record, which is the last SMS sent
            cur.moveToNext();

            String body = cur.getString(cur.getColumnIndex("body")); //content of sms
            String address = cur.getString(cur.getColumnIndex("address")); //phone num
            int type = cur.getInt(cur.getColumnIndex("type")); //protocol
            String protocol = cur.getString(cur.getColumnIndex("protocol"));

            if(type == 2) {
                if(protocol == null){
                    Log.d(this.toString(), body);

                    SO smsOutEvent = new SO(System.currentTimeMillis(), 0, address, getContactName(iLogApplication.getAppContext(), address));
                    iLogApplication.persistInMemoryEvent(smsOutEvent);
                }
            }
        }
    }

    /**
     * Method used to retrieve the contact name from the smartphone Contacts list, if available, from the phone number
     * @param context context object
     * @param phoneNumber the phone number to be searche din the Contacts list
     * @return a String containing the contact name as saved in the library, it returns "unauthorized" if the user did not provide access to the Contacts list
     */
    public static String getContactName(Context context, String phoneNumber) {
        if(iLogApplication.hasSinglePermission(Manifest.permission.READ_CONTACTS)) {
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
        else {
            return "unauthorized";
        }
    }
}
