package it.unitn.disi.witmee.sensorlog.services;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;

/**
 * Service that extends JobService and perform the long tunning task of uploading all the logs (if present) to the server when a specific Firebase Cloud Message is received
 * in {@link MyFirebaseMessagingService#synchronizeFiles()}
 * @see <a href="https://github.com/firebase/firebase-jobdispatcher-android#user-content-firebase-jobdispatcher-">https://github.com/firebase/firebase-jobdispatcher-android#user-content-firebase-jobdispatcher-</a>
 */

public class SynchJobService extends JobService {

    /**
     * This methos is where any asynchronous execution starts. Like most lifecycle methods, runs on the main thread; you
     * <b>must</b> offload execution to another thread (or {@link android.os.AsyncTask}, or
     * {@link android.os.Handler}, or your favorite flavor of concurrency).
     * @param jobParameters represents anything that can describe itself in terms of Job components, in this case is not used
     * @return default return value
     */
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(this.toString(), "Performing long running task in scheduled job");

        iLogApplication.uploadAllIfConnected();

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}