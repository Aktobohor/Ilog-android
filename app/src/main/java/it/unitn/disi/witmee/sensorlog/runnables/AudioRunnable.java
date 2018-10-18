package it.unitn.disi.witmee.sensorlog.runnables;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.ApplicationBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.AudioRemoveBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.AudioRequestBroadcastReceiver;
import it.unitn.disi.witmee.sensorlog.model.audio.AU;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.system.ST;

/**
 * Class that implements a {@link Runnable} that manages the data collection of the {@link AU} event. The data
 * collection occurs through two {@link android.content.BroadcastReceiver}, {@link AudioRequestBroadcastReceiver} and {@link AudioRemoveBroadcastReceiver}.
 * They are registered/unregistered in this class and the data of type {@link AU} event is generated in them. With respect to other Runnables in the application,
 * this one does not leverage on the system {@link android.content.BroadcastReceiver} that broadcast an event whenever it occurs (like {@link AirplaneModeRunnable}).
 * For this reason we need an {@link AlarmManager} in combination with two {@link PendingIntent}, {@link #pendingIntentRequest} and {@link #pendingIntentRemove}
 * that schedule the operation at fixed time intervals. The reason for the two {@link PendingIntent} and {@link android.content.BroadcastReceiver} is that this Runnable
 * needs to trigger the event of start recording the audio and , after some seconds, stop it.
 * @author Mattia Zeni
 */
public class AudioRunnable implements Runnable {

    private volatile boolean isStopped = false;
    private static int SENSOR_ID = iLogApplication.AUDIO_ID;

    public static AlarmManager alarmManager;
    public static ByteArrayOutputStream outputStream = null;
    public static PendingIntent pendingIntentRequest = null;
    public static PendingIntent pendingIntentRemove = null;

    static int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    static int BytesPerElement = 2; // 2 bytes in 16bit format

    private static final int[] RECORDER_SAMPLERATE = {8000, 11025, 16000, 22050, 44100};
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static AudioRecord recorder = null;
    private static Thread recordingThread = null;
    private static boolean isRecording = false;

    /**
     * Method that starts the collection of the {@link AU} events. It performs the following operations:
     * <ul>
     *     <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *     <li>Initializes the {@link #pendingIntentRequest} that runs the {@link AudioRequestBroadcastReceiver} class</li>
     *     <li>Initializes the {@link #pendingIntentRemove} that runs the {@link AudioRemoveBroadcastReceiver} class</li>
     *     <li>Starts the collection process by calling the method {@link #startRequest()}</li>
     * </ul>
     */
    public void run() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            isStopped = false;

            if(!iLogApplication.sensorLoggingState.get(SENSOR_ID) && iLogApplication.hasSinglePermission(Manifest.permission.RECORD_AUDIO)) {

                iLogApplication.startLoggingMonitoringService();
                Log.d(this.getClass().getSimpleName(), "Start");

                iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), true));
                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
                iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STARTED, this.getClass().getSimpleName()));

                pendingIntentRequest = PendingIntent.getBroadcast(
                        iLogApplication.getAppContext(),
                        0, // id, optional
                        new Intent(iLogApplication.getAppContext(), AudioRequestBroadcastReceiver.class), // intent to launch
                        PendingIntent.FLAG_CANCEL_CURRENT);

                pendingIntentRemove = PendingIntent.getBroadcast(
                        iLogApplication.getAppContext(),
                        0, // id, optional
                        new Intent(iLogApplication.getAppContext(), AudioRemoveBroadcastReceiver.class), // intent to launch
                        PendingIntent.FLAG_CANCEL_CURRENT);

                startRequest();
            }
        }
    }

    /**
     * Contains information about the status of the data collection for this {@link it.unitn.disi.witmee.sensorlog.model.system.AM} event
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
     * Method that stops the collection of the {@link it.unitn.disi.witmee.sensorlog.model.system.AM} events. It performs the following operations:
     * <ul>
     *     <li>Cancels the {@link #pendingIntentRequest} from the {@link #alarmManager}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Sets this runnable as stopped</li>
     * </ul>
     */
    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(alarmManager!=null && pendingIntentRequest!=null) {
                alarmManager.cancel(pendingIntentRequest);
            }

            iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), false));
            iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STOPPED, this.getClass().getSimpleName()));
            iLogApplication.sensorLoggingState.put(SENSOR_ID, false);
            Log.d(this.getClass().getSimpleName(), "Stop");

            setStopped(true);
        }
    }

    /**
     * Method that restarts the collection of the {@link AU} events. It performs the following operations:
     * <ul>
     *     <li>Cancels the {@link #pendingIntentRequest} from the {@link #alarmManager}</li>
     *     <li>Starts the collection process by calling the method {@link #startRequest()}</li>
     * </ul>
     */
    public void restart() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped() && iLogApplication.hasSinglePermission(Manifest.permission.RECORD_AUDIO)) {

                if(alarmManager!=null && pendingIntentRequest!=null) {
                    alarmManager.cancel(pendingIntentRequest);
                }

                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
                startRequest();
            }
        }
    }

    /**
     * Method that tells the {@link #alarmManager} to execute the {@link #pendingIntentRequest} that runs the {@link AudioRequestBroadcastReceiver} class. This class
     * starts collecting audio from the device's microphone.
     */
    public static void startRequest() {
        if(pendingIntentRequest!=null) {
            alarmManager = (AlarmManager) iLogApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntentRequest);
        }
    }

    /**
     * Method that tells the {@link #alarmManager} to execute the {@link #pendingIntentRemove} that runs the {@link AudioRemoveBroadcastReceiver} class. This class
     * stops collecting audio from the device's microphone. The {@link #pendingIntentRemove} is executed 10*1000 seconds after the {@link #pendingIntentRequest}, meaning
     * that collects 10 second of audio. TODO - Use a variable like in the other Runnables as recording time for the audio, remove the 10*1000 hardcoded time interval
     */
    public static void startRemove() {
        if(pendingIntentRemove!=null) {
            alarmManager = (AlarmManager) iLogApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10*1000, pendingIntentRemove);
        }
    }

    /**
     * Method that starts the recording process. It also triggers the {@link #startRemove()} method to be able to stop the recording after the desired amount of time.
     */
    public static void startRecording() {

        Log.d(AudioRunnable.class.toString(), "Start audio recording");

        if(iLogApplication.hasSinglePermission(Manifest.permission.RECORD_AUDIO)) {
            AudioRunnable.outputStream = new ByteArrayOutputStream();

            int sampleRate = getMinimumValidSampleRate();

            if(sampleRate > 0) {
                recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        sampleRate, RECORDER_CHANNELS,
                        RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

                if(recorder!=null) {
                    recorder.startRecording();
                    isRecording = true;
                    recordingThread = new Thread(new Runnable() {
                        public void run() {
                            writeAudioDataToFile();
                        }
                    }, "AudioRecorder Thread");
                    recordingThread.start();

                    startRemove();
                }
            }
        }
    }

    /**
     * Method that writes the recorded audio data to the {@link #outputStream}
     * @todo check
     */
    private static void writeAudioDataToFile() {
        // Write the output audio in byte

        //short sData[] = new short[BufferElements2Rec];
        byte data[] = new byte[BufferElements2Rec];

        while (isRecording) {
            // gets the voice output from microphone to byte format
            if(recorder!=null) {
                recorder.read(data, 0, BufferElements2Rec);
                try {
                    //byte bData[] = short2byte(sData);
                    //TODO - check this if, needs to be fixed and removed
                    if(AudioRunnable.outputStream.size() < 10000000) {//1MB
                        AudioRunnable.outputStream.write(data);
                    }
                    else {
                        stopRecording();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that stops the recording process on the {@link AudioRecord} object. It also prepares the data to be stored in the logs and geenrates the
     * {@link AU} event and persiste it in memory using {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}. The audioContent is created by applying
     * {@link Base64} on the bytes collected in the {@link #outputStream}.
     */
    public static void stopRecording() {

        Log.d(AudioRunnable.class.toString(), "Stop audio recording");
        // stops the recording activity

        if (null != recorder) {
            try {
                isRecording = false;
                recorder.stop();
                recordingThread = null;

                if(AudioRunnable.outputStream.size() > 0) {
                    String audioContent = new String(Base64.encodeToString(AudioRunnable.outputStream.toByteArray(), Base64.NO_WRAP));
                    System.out.println("audio "+audioContent.length());
                    //If audioContent is not empty
                    if(org.springframework.util.StringUtils.countOccurrencesOf(audioContent, "A")!=audioContent.length()) {
                        AU audioEvent = new AU(System.currentTimeMillis(), 0, audioContent);
                        iLogApplication.persistInMemoryEvent(audioEvent);
                    }
                }

                AudioRunnable.outputStream.reset();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            finally {
                recorder.release();
                recorder = null;
            }
        }
    }

    /**
     * Method used to get the minimum sampling frequency available on the device. After some tests done with Ivan Kayongo it seems that the minimum sampling frequency
     * to generate good results during the analysis is 16000Hz. TODO - check and modify accordingly to the latest findings
     * @return integer representing the sampling frequency, in Hz
     */
    public static int getMinimumValidSampleRate() {
        return 16000;
        /*
        for (int rate : RECORDER_SAMPLERATE) {  // add the rates you wish to check against
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_CONFIGURATION_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                return rate;
            }
        }

        try {
            AudioManager audioManager = (AudioManager) iLogApplication.getAppContext().getSystemService(Context.AUDIO_SERVICE);
            System.out.println("PREFERRED SAMPLE RATE: "+Integer.valueOf(audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)));

            return Integer.valueOf(audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
        }
        catch(Exception e) {
            e.printStackTrace();
            return 0;
        }*/
    }
}
