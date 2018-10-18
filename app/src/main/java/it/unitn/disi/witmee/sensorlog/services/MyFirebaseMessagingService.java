package it.unitn.disi.witmee.sensorlog.services;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.Answer;
import it.unitn.disi.witmee.sensorlog.model.Message;
import it.unitn.disi.witmee.sensorlog.model.QA;
import it.unitn.disi.witmee.sensorlog.model.Question;
import it.unitn.disi.witmee.sensorlog.model.Task;
import it.unitn.disi.witmee.sensorlog.model.system.FullInfo;
import it.unitn.disi.witmee.sensorlog.model.system.ST;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Class that extends FirebaseMessagingService and is used to receive messages from Firebase Cloud Messaging service.<br>
 *     Note that Firebase messages are received only if the application is in execution on the phone, either running or stopped.
 * @see <a href="https://firebase.google.com/docs/cloud-messaging/">https://firebase.google.com/docs/cloud-messaging/</a> and <a href="https://firebase.google.com/docs/cloud-messaging/android/client">https://firebase.google.com/docs/cloud-messaging/android/client</a>
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when a message is received. TODO - implement the missing messages
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> messagePayload = remoteMessage.getData();
            Log.d(TAG, "Type: "+messagePayload.get("type")+", Content: "+messagePayload.get("content"));

            if(messagePayload.get("type").equals("topicsubscription")) {
                subscribeToTopic(messagePayload);
            }
            if(messagePayload.get("type").equals("topicunsubscription")) {
                unSubscribeToTopic(messagePayload);
            }
            if(messagePayload.get("type").equals("startapplication")) {
                startiLog(messagePayload);
            }
            if(messagePayload.get("type").equals("stopapplication")) {
                stopiLog(messagePayload);
            }
            if(messagePayload.get("type").equals("restartapplication")) {
                restartiLog(messagePayload);
            }
            if(messagePayload.get("type").equals("startallsensors")) {
                iLogApplication.startLogging();
            }
            if(messagePayload.get("type").equals("stopallsensors")) {
                iLogApplication.stopLogging("close");
            }
            if(messagePayload.get("type").equals("startmultiplesensors")) {
                //TODO
            }
            if(messagePayload.get("type").equals("stopmultiplesensors")) {
                //TODO
            }
            if(messagePayload.get("type").equals("startsinglesensor")) {
                startSingleSensor(messagePayload);
            }
            if(messagePayload.get("type").equals("stopsinglesensor")) {
                stopSingleSensor(messagePayload);
            }
            if(messagePayload.get("type").equals("compresslogs")) {
                //TODO
            }
            if(messagePayload.get("type").equals("requeststatus")) {
                new FullInfo();
            }
            if(messagePayload.get("type").equals("changeconfig")) {
                //TODO
            }
            if(messagePayload.get("type").equals("synchronizefiles")) {
                synchronizeFiles();
            }
            if(messagePayload.get("type").equals("newquestion")) {
                processNewQuestion(messagePayload);
            }
            if(messagePayload.get("type").equals("newtask")) {
                processNewTask(messagePayload);
            }
            if(messagePayload.get("type").equals("newmessage")) {
                processNewMessage(messagePayload);
            }
        }
    }

    /**
     * Method used to start the i-Log application if it is stopped.
     */
    private void startiLog(Map<String, String> message) {
        Log.d(TAG, "Type: "+message.get("type")+", Content: "+message.get("content"));

        Context ctx = iLogApplication.getAppContext();
        try {
            Intent i = ctx.getPackageManager().getLaunchIntentForPackage("it.unitn.disi.witmee.sensorlog");
            ctx.startActivity(i);
        } catch (Exception e) {
        }
    }

    /**
     * Method used to stop the i-Log application if it is running.
     */
    private void stopiLog(Map<String, String> message) {
        iLogApplication.closeApplicationSafely();
    }

    /**
     * Method used to restart the i-Log application if it is running.
     */
    private void restartiLog(Map<String, String> message) {
        //if application running
        iLogApplication.closeApplicationSafelyAndRestart();
    }

    /**
     * Method that triggers a {@link FirebaseJobDispatcher} that synchronizes the logs stored locally (if any). This kind of job is needed to execute long running tasks
     * when a message is received with Firebase Cloud Messaging
     * @see @see <a href="https://github.com/firebase/firebase-jobdispatcher-android#user-content-firebase-jobdispatcher-">General overview</a> for more details about FirebaseJobDispatcher
     */
    private void synchronizeFiles() {
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(SynchJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
    }

    /**
     * This method get the compressed .bz2 byte array and uncompresses it. Since the amount of data that can be sent using Firebase Cloud Messaging is limited to
     * 4096 bytes (as of July 2018) we need to compress the payload at the server side and uncompress it on the device. This is valid for the user contributions objects,
     * {@link Question}, {@link Message}, {@link it.unitn.disi.witmee.sensorlog.model.Challenge}, {@link Task}.
     * @param bytes array of bytes containing the payload to be unzipped
     * @return returns a String with the content of the message represented as a JSONArray
     * @throws IOException
     * @see <a href="https://firebase.google.com/docs/cloud-messaging/concept-options">Firebase Cloud Messaging documentation</a> for more details about messages size limit
     */
    static String decompressContent(byte[] bytes) throws IOException {
        BZip2CompressorInputStream in=new BZip2CompressorInputStream(new ByteArrayInputStream(bytes));
        int n=0;
        ByteArrayOutputStream out=new ByteArrayOutputStream(bytes.length * 5);
        byte[] buf=new byte[1024];
        try {
            while (-1 != (n=in.read(buf))) {
                out.write(buf,0,n);
            }
        }
        finally {
            in.close();
            out.close();
        }
        return out.toString();
    }

    /**
     * Method used to subscribe the device to a new topic. The topics are sent from the server. Topics are a way Firebase Cloud Messaging has to reach multiple people
     * that have a common characteristic, e.g., participate in the same project, same age, same gender, among others.
     * @param message the content of the message contains a String defining the identifier of the topic.
     * @see <a href="https://firebase.google.com/docs/cloud-messaging/android/topic-messaging">Firebase Cloud Messaging documentation</a> for more details about topics
     */
    private void subscribeToTopic(Map<String, String> message) {
        FirebaseMessaging.getInstance().subscribeToTopic(message.get("content"));
    }

    /**
     * Method used to unsubscribe the device to a topic he is subscribed to. See {@link #subscribeToTopic(Map)} for more details.
     * @param message the content of the message contains a String defining the identifier of the topic.
     */
    private void unSubscribeToTopic(Map<String, String> message) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(message.get("content"));
    }

    /**
     * Method used to start logging on the application from a single sensor.
     * @param message the content of the message contains a String defining the sensor to start.
     * @todo this method is only drafted, it needs to be finished and tested.
     */
    private void startSingleSensor(Map<String, String> message) {
        if(message.get("content").equals("airplanesensor")) {
            iLogApplication.airplaneModeRunnable.run();
        }
        else if(message.get("content").equals("ambiencesensor")) {
            iLogApplication.ambienceRunnable.run();
        }
        else if(message.get("content").equals("applicationsensor")) {
            iLogApplication.applicationsRunnable.run();
        }
        else if(message.get("content").equals("audiosensor")) {
            iLogApplication.audioRunnable.run();
        }
        else if(message.get("content").equals("batterychargesensor")) {
            iLogApplication.batteryChargeRunnable.run();
        }
        else if(message.get("content").equals("batterylevelsensor")) {
            iLogApplication.batteryLevelRunnable.run();
        }
        else if(message.get("content").equals("bluetoothlesensor")) {
            if(iLogApplication.bluetoothLERunnable!=null) {
                iLogApplication.bluetoothLERunnable.run();
            }
        }
        else if(message.get("content").equals("bluetoothnormalsensor")) {
            if(iLogApplication.bluetoothRunnable!=null) {
                iLogApplication.bluetoothRunnable.run();
            }
        }
        else if(message.get("content").equals("dozesensor")) {
            iLogApplication.dozeRunnable.run();
        }
        else if(message.get("content").equals("headsetsensor")) {
            iLogApplication.headsetRunnable.run();
        }
        else if(message.get("content").equals("musicsensor")) {
            iLogApplication.musicRunnable.run();
        }
        else if(message.get("content").equals("phonecalloutsensor")) {
            iLogApplication.phoneCallOutRunnable.run();
        }
        else if(message.get("content").equals("phonecallinsensor")) {
            iLogApplication.phoneCallInRunnable.run();
        }
        else if(message.get("content").equals("ringmodesensor")) {
            iLogApplication.ringModeRunnable.run();
        }
        else if(message.get("content").equals("screensensor")) {
            iLogApplication.screenRunnable.run();
        }
        else if(message.get("content").equals("accelerometersensor")) {
            iLogApplication.sensorAccelerometerRunnable.run();
        }
        else if(message.get("content").equals("ambienttemperaturesensor")) {
            iLogApplication.sensorAmbientTemperatureRunnable.run();
        }
        else if(message.get("content").equals("gravitysensor")) {
            iLogApplication.sensorGravityRunnable.run();
        }
        else if(message.get("content").equals("gyroscopesensor")) {
            iLogApplication.sensorGyroscopeRunnable.run();
        }
        else if(message.get("content").equals("lightsensor")) {
            iLogApplication.sensorLightRunnable.run();
        }
        else if(message.get("content").equals("linearaccelerometersensor")) {
            iLogApplication.sensorLinearAccelerometerRunnable.run();
        }
        else if(message.get("content").equals("magneticfieldsensor")) {
            iLogApplication.sensorMagneticFieldRunnable.run();
        }
        else if(message.get("content").equals("orientationsensor")) {
            iLogApplication.sensorOrientationRunnable.run();
        }
        else if(message.get("content").equals("pressuresensor")) {
            iLogApplication.sensorPressureRunnable.run();
        }
        else if(message.get("content").equals("proximitysensor")) {
            iLogApplication.sensorProximityRunnable.run();
        }
        else if(message.get("content").equals("relativehumiditysensor")) {
            iLogApplication.sensorRelativeHumidityRunnable.run();
        }
        else if(message.get("content").equals("rotationvectorsensor")) {
            iLogApplication.sensorRotationVectorRunnable.run();
        }
        else if(message.get("content").equals("smsinsensor")) {
            iLogApplication.smsInRunnable.run();
        }
        else if(message.get("content").equals("smsoutsensor")) {
            iLogApplication.smsOutRunnable.run();
        }
        else if(message.get("content").equals("userpresencesensor")) {
            iLogApplication.userPresentRunnable.run();
        }
        else if(message.get("content").equals("wifinetworkssensor")) {
            iLogApplication.wifiNetworksRunnable.run();
        }
        else if(message.get("content").equals("locationnetworksensor")) {
            LocationManager locationManager = (LocationManager) iLogApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && iLogApplication.isNetworkConnected()) {
                iLogApplication.locationNetworkRunnable.run();
            }
        }
        else if(message.get("content").equals("locationgpssensor")) {
            LocationManager locationManager = (LocationManager) iLogApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !iLogApplication.isNetworkConnected()) {
                iLogApplication.locationGPSRunnable.run();
            }
        }
        else if(message.get("content").equals("locationsensor")) {
            LocationManager locationManager = (LocationManager) iLogApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !iLogApplication.isNetworkConnected()) {
                iLogApplication.locationGPSRunnable.run();
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && iLogApplication.isNetworkConnected()) {
                iLogApplication.locationNetworkRunnable.run();
            }
        }
    }

    /**
     * Method used to stop logging on the application from a single sensor.
     * @param message the content of the message contains a String defining the sensor to start.
     * @todo this method is only drafted, it needs to be finished and tested.
     */
    private void stopSingleSensor(Map<String, String> message) {
        if(message.get("content").equals("airplanesensor")) {
            iLogApplication.airplaneModeRunnable.stop();
        }
        else if(message.get("content").equals("ambiencesensor")) {
            iLogApplication.ambienceRunnable.stop();
        }
        else if(message.get("content").equals("applicationsensor")) {
            iLogApplication.applicationsRunnable.stop();
        }
        else if(message.get("content").equals("audiosensor")) {
            iLogApplication.audioRunnable.stop();
        }
        else if(message.get("content").equals("batterychargesensor")) {
            iLogApplication.batteryChargeRunnable.stop();
        }
        else if(message.get("content").equals("batterylevelsensor")) {
            iLogApplication.batteryLevelRunnable.stop();
        }
        else if(message.get("content").equals("bluetoothlesensor")) {
            iLogApplication.bluetoothLERunnable.stop();
        }
        else if(message.get("content").equals("bluetoothnormalsensor")) {
            iLogApplication.bluetoothRunnable.stop();
        }
        else if(message.get("content").equals("dozesensor")) {
            iLogApplication.dozeRunnable.stop();
        }
        else if(message.get("content").equals("headsetsensor")) {
            iLogApplication.headsetRunnable.stop();
        }
        else if(message.get("content").equals("musicsensor")) {
            iLogApplication.musicRunnable.stop();
        }
        else if(message.get("content").equals("phonecalloutsensor")) {
            iLogApplication.phoneCallOutRunnable.stop();
        }
        else if(message.get("content").equals("phonecallinsensor")) {
            iLogApplication.phoneCallInRunnable.stop();
        }
        else if(message.get("content").equals("ringmodesensor")) {
            iLogApplication.ringModeRunnable.stop();
        }
        else if(message.get("content").equals("screensensor")) {
            iLogApplication.screenRunnable.stop();
        }
        else if(message.get("content").equals("accelerometersensor")) {
            iLogApplication.sensorAccelerometerRunnable.stop();
        }
        else if(message.get("content").equals("ambienttemperaturesensor")) {
            iLogApplication.sensorAmbientTemperatureRunnable.stop();
        }
        else if(message.get("content").equals("gravitysensor")) {
            iLogApplication.sensorGravityRunnable.stop();
        }
        else if(message.get("content").equals("gyroscopesensor")) {
            iLogApplication.sensorGyroscopeRunnable.stop();
        }
        else if(message.get("content").equals("lightsensor")) {
            iLogApplication.sensorLightRunnable.stop();
        }
        else if(message.get("content").equals("linearaccelerometersensor")) {
            iLogApplication.sensorLinearAccelerometerRunnable.stop();
        }
        else if(message.get("content").equals("magneticfieldsensor")) {
            iLogApplication.sensorMagneticFieldRunnable.stop();
        }
        else if(message.get("content").equals("orientationsensor")) {
            iLogApplication.sensorOrientationRunnable.stop();
        }
        else if(message.get("content").equals("pressuresensor")) {
            iLogApplication.sensorPressureRunnable.stop();
        }
        else if(message.get("content").equals("proximitysensor")) {
            iLogApplication.sensorProximityRunnable.stop();
        }
        else if(message.get("content").equals("relativehumiditysensor")) {
            iLogApplication.sensorRelativeHumidityRunnable.stop();
        }
        else if(message.get("content").equals("rotationvectorsensor")) {
            iLogApplication.sensorRotationVectorRunnable.stop();
        }
        else if(message.get("content").equals("smsinsensor")) {
            iLogApplication.smsInRunnable.stop();
        }
        else if(message.get("content").equals("smsoutsensor")) {
            iLogApplication.smsOutRunnable.stop();
        }
        else if(message.get("content").equals("userpresencesensor")) {
            iLogApplication.userPresentRunnable.stop();
        }
        else if(message.get("content").equals("wifinetworkssensor")) {
            iLogApplication.wifiNetworksRunnable.stop();
        }
        else if(message.get("content").equals("locationnetworksensor")) {
            LocationManager locationManager = (LocationManager) iLogApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && iLogApplication.isNetworkConnected()) {
                iLogApplication.locationNetworkRunnable.stop();
            }
        }
        else if(message.get("content").equals("locationgpssensor")) {
            LocationManager locationManager = (LocationManager) iLogApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !iLogApplication.isNetworkConnected()) {
                iLogApplication.locationGPSRunnable.stop();
            }
        }
        else if(message.get("content").equals("locationsensor")) {
            LocationManager locationManager = (LocationManager) iLogApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !iLogApplication.isNetworkConnected()) {
                iLogApplication.locationGPSRunnable.stop();
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && iLogApplication.isNetworkConnected()) {
                iLogApplication.locationNetworkRunnable.stop();
            }
        }
    }

    /**
     * Method called when a new {@link Question} is received. If the collection is in process, the content is parsed and inserted into the database. Then, the
     * notification is updated.
     * @param message is a String with the content of the message represented as a JSONArray
     */
    private void processNewQuestion(Map<String, String> message) {
        if(iLogApplication.isMonitoringServiceRunning) {
            JSONArray subQuestions = null;
            try {
                subQuestions = new JSONArray(decompressContent(Base64.decode(message.get("content"), Base64.NO_WRAP)).replace("\\\"", "\""));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_NEWQUESTION, ""));

            long timestamp = System.currentTimeMillis();

            /*
            This block checks if the user enabled the "Snooze notification" feature present in the Settings menu. If this functionality is enabled, the question
            is received but is automatically answered.
             */
            if(iLogApplication.sharedPreferences.getLong(Utils.CONFIG_SLEEP_TILL, 0) <= timestamp) {
                iLogApplication.db.addQuestion(new Question(Long.parseLong(message.get("timestamp")), System.currentTimeMillis(), Long.valueOf(message.get("t_until")), message.get("t_id"), subQuestions.toString(), Question.STATUS_RECEIVED, message.get("t_title"), Question.SYNCHRONIZATION_FALSE));
                iLogApplication.sharedPreferences.edit().putLong(Utils.CONFIG_SLEEP_TILL, 0).commit();
                iLogApplication.sharedPreferences.edit().putInt(Utils.CONFIG_SLEEP_INTERVAL_HOURS, 0).commit();
            }
            else {
                Question question = new Question(Long.parseLong(message.get("timestamp")), System.currentTimeMillis(), Long.valueOf(message.get("t_until")), message.get("t_id"), subQuestions.toString(), Question.STATUS_RECEIVED, message.get("t_title"), Question.SYNCHRONIZATION_FALSE);
                iLogApplication.db.addQuestion(question);

                Answer answer = new Answer(question.getInstanceTime(), System.currentTimeMillis(), question.getNotifiedTime(), 0, Answer.generateSleepAnswer(), Answer.generateSleepPayload(), question.getInstanceid(), Answer.TYPE_TIMEDIARY, "false", "false");
                iLogApplication.db.addAnswer(answer);
                iLogApplication.db.updateQuestion(question, Question.STATUS_ANSWERED);
            }
            iLogApplication.uploadAllContributions();

            iLogApplication.updateQuestionNotification();

            Log.d(this.toString(), "QUESTION RECEIVED: "+System.currentTimeMillis());
        }
    }

    /**
     * Method called when a new {@link Task} is received. Tasks are received wither if the application is collecting data or not. Then, the notification is
     * updated only if the data collection is in progress.
     *
     * @param message is a String with the content of the message represented as a JSONArray
     */
    private void processNewTask(Map<String, String> message) {
        JSONArray taskJson = null;
        try {
            taskJson = new JSONArray(decompressContent(Base64.decode(message.get("content"), Base64.NO_WRAP)).replace("\\\"", "\""));
            //taskJson = new JSONArray(messagePayload.get("content"));

            iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_NEWTASK, ""));

            Log.d(this.toString(), message.get("timestamp"));
            Log.d(this.toString(), System.currentTimeMillis()+"");
            Task task = new Task(Long.parseLong(message.get("timestamp")), System.currentTimeMillis(), taskJson.toString(), message.get("t_id"), "unsolved", message.get("t_title"), Long.valueOf(message.get("t_until")), Task.SYNCHRONIZATION_FALSE);

            iLogApplication.db.addTask(task);
            iLogApplication.uploadAllContributions();

            if(iLogApplication.isMonitoringServiceRunning) {
                iLogApplication.updateTaskNotification();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method called when a new {@link Message} is received. Messages are received wither if the application is collecting data or not. Then, the notification is
     * updated only if the data collection is in progress.
     *
     * @param message is a String with the content of the message represented as a JSONArray
     */
    private void processNewMessage(Map<String, String> message) {
        //{"ti":[{"l":"en-US","t":"New update available"},{"l":"it-IT","t":"Nuovo aggiornamento disponibile?"}], "dsc":[{"l":"en-US","t":"Ehi, did you know that a new update is available on the Google Play Store? Download it if you want to benefit from the latest iprovements we did to i-Log... bla bla"},{"l":"it-IT","t":"Ehi lo sapevi che e' diponibile un nuovo aggiornamento sul Google Play Store? Scaricalo se vuoi beneficiare delle ultime migliorie apportate ad i-Log... bla bla"}], "pg":[{"id":1,"ty":"t","cnt":[{"or":1, "ty":"tiv", "p":[{"l":"en-US","t":"This is a title TextView"},{"l":"it-IT","t":"Questa e una TextView per il titolo"}]}, {"or":2, "ty":"tv", "p":[{"l":"en-US","t":"This is a TextView"},{"l":"it-IT","t":"Questa e una TextView"}]}, {"or":3, "ty":"iv", "p":"https://i.pinimg.com/originals/53/f4/ce/53f4ce698c3a04695247357283706974.png"}]}, {"id":2,"ty":"t","cnt":[{"or":1, "ty":"tiv", "p":[{"l":"en-US","t":"This is a different title"},{"l":"it-IT","t":"Questo e' un titolo diverso"}]}, {"or":2, "ty":"tv", "p":[{"l":"en-US","t":"This is a different TextView"},{"l":"it-IT","t":"Questa e una TextView diversa"}]}, {"or":3, "ty":"iv", "p":"https://www.tesla.com/sites/default/files/images/software_update.jpg"}]}]}

        JSONObject messageJson = null;
        try {
            messageJson = new JSONObject(decompressContent(Base64.decode(message.get("content"), Base64.NO_WRAP)).replace("\\\"", "\""));

            iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_NEWMESSAGE, ""));

            Log.d(this.toString(), message.get("timestamp"));
            Log.d(this.toString(), message.get("t_id"));

            Message messageObject = new Message(Long.parseLong(message.get("timestamp")), System.currentTimeMillis(), Long.valueOf(message.get("t_until")), message.get("t_id"), messageJson.toString(), Message.STATUS_UNREAD, message.get("t_title"), Message.SYNCHRONIZATION_FALSE);
            iLogApplication.db.addMessage(messageObject);
            iLogApplication.uploadAllContributions();

            if(iLogApplication.isMonitoringServiceRunning) {
                iLogApplication.updateMessageNotification();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}