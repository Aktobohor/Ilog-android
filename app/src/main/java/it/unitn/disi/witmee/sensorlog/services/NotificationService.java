package it.unitn.disi.witmee.sensorlog.services;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.virtual.NE;

/**
 * Service class that extends NotificationListenerService. This type of service is registered in the manifest, and receives calls from the system when notifications
 * are posted or removed, or their ranking changed.
 * @see <a href="https://developer.android.com/reference/android/service/notification/NotificationListenerService">https://developer.android.com/reference/android/service/notification/NotificationListenerService</a>
 */
public class NotificationService extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();

    /**
     * Method called every time a notification is posted. We take its content, which is the content of the notification, and we generate a {@link NE} event with tag {@link NE#NOTIFICATION_POSTED} that is persisted
     * with {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}.
     * @param sbn StatusBarNotification object that contains information about the notification
     */
    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        if(!iLogApplication.notificationRunnable.isStopped() && iLogApplication.isMonitoringServiceRunning) {
            if(!sbn.getPackageName().equals("it.unitn.disi.witmee.sensorlog")) {
                Log.i(TAG,"onNotificationPosted "+sbn.getPackageName().toString());
                final JSONObject json = new JSONObject();
                Set<String> keys = sbn.getNotification().extras.keySet();
                for (String key : keys) {
                    try {
                        json.put(key, JSONObject.wrap(sbn.getNotification().extras.get(key)));
                    } catch(JSONException e) {
                    }
                }

                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        iLogApplication.persistInMemoryEvent(new NE(NE.NOTIFICATION_POSTED, Integer.toString(sbn.getId()), (sbn.getNotification().tickerText !=null) ? sbn.getNotification().tickerText.toString() : null, sbn.getPackageName(), sbn.getPostTime(), sbn.isClearable(), sbn.isOngoing(), Base64.encodeToString(json.toString().getBytes(), Base64.NO_WRAP)));
                    }
                });
                thread.start();
            }
        }
        else {
            this.stopSelf();
        }
    }

    /**
     * Method called every time a notification is removed. We take its content, which is the content of the notification, and we generate a {@link NE} event with tag {@link NE#NOTIFICATION_REMOVED} that is persisted
     * with {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}.
     * @param sbn StatusBarNotification object that contains information about the notification
     */
    @Override
    public void onNotificationRemoved(final StatusBarNotification sbn) {
        if(!iLogApplication.notificationRunnable.isStopped() && iLogApplication.isMonitoringServiceRunning) {
            if (!sbn.getPackageName().equals("it.unitn.disi.witmee.sensorlog")) {
                Log.i(TAG, "onNOtificationRemoved "+sbn.getPackageName().toString());
                final JSONObject json = new JSONObject();
                Set<String> keys = sbn.getNotification().extras.keySet();
                for (String key : keys) {
                    try {
                        json.put(key, JSONObject.wrap(sbn.getNotification().extras.get(key)));
                    } catch (JSONException e) {
                    }
                }

                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        iLogApplication.persistInMemoryEvent(new NE(NE.NOTIFICATION_REMOVED, Integer.toString(sbn.getId()), (sbn.getNotification().tickerText != null) ? sbn.getNotification().tickerText.toString() : null, sbn.getPackageName(), sbn.getPostTime(), sbn.isClearable(), sbn.isOngoing(), Base64.encodeToString(json.toString().getBytes(), Base64.NO_WRAP)));
                    }
                });
                thread.start();
            }
        }
        else {
            this.stopSelf();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}