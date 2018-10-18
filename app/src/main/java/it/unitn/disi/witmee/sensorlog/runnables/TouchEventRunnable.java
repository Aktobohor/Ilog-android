package it.unitn.disi.witmee.sensorlog.runnables;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.TouchEventListener;
import it.unitn.disi.witmee.sensorlog.model.metalog.SM;
import it.unitn.disi.witmee.sensorlog.model.system.ST;

/**
 * Class that implements a {@link Runnable} that manages the data collection of the {@link it.unitn.disi.witmee.sensorlog.model.virtual.TE} event. It requires a particular
 * approach that draws a transparent view {@link #mDummyView} on top of anything on the screen, and we are allowed to detect
 * touches on this view. We can detect only the time of the touch and not the position on the screen. <br>
 *     Starting from Android O, the {@link WindowManager.LayoutParams} TYPE changed
 * and instead of using {@link WindowManager.LayoutParams#TYPE_PHONE} we need to use {@link WindowManager.LayoutParams#TYPE_APPLICATION_OVERLAY}.
 * @author Mattia Zeni
 */
public class TouchEventRunnable implements Runnable {

    private volatile boolean isStopped = false;
    private static WindowManager mWindowManager = null;
    private static LinearLayout mDummyView = null;
    private static int SENSOR_ID = iLogApplication.TOUCH_ID;

    /**
     * Method that starts the collection of the {@link it.unitn.disi.witmee.sensorlog.model.system.AM} events. It performs the following operations:
     * <ul>
     *      <li>Starts the {@link it.unitn.disi.witmee.sensorlog.services.LoggingMonitoringService} if not already running using the {@link iLogApplication#startLoggingMonitoringService()}</li>
     *      <li>Persists a {@link SM} event that indicates that the sensor just started collecting data</li>
     *      <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is running</li>
     *      <li>Persists a {@link ST} event that indicates that this runnable has been started</li>
     *      <li>Initializes the {@link TouchEventListener} variable where the detection of the event occurs</li>
     *      <li>Sets the {@link TouchEventListener} using {@link LinearLayout#setOnTouchListener(View.OnTouchListener)} on the {@link #mDummyView}</li>
     * </ul>
     */
    public void run() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            isStopped = false;

            if(!iLogApplication.sensorLoggingState.get(SENSOR_ID) && iLogApplication.hasDrawOnTopPermissions()) {

                iLogApplication.startLoggingMonitoringService();
                Log.d(this.getClass().getSimpleName(), "Start");

                iLogApplication.persistInMemoryEvent(new SM(SENSOR_ID, System.currentTimeMillis(), true));
                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);
                iLogApplication.persistInMemoryEvent(new ST(ST.EVENT_SERVICE_STARTED, this.getClass().getSimpleName()));

                TouchEventListener touchEventListener = new TouchEventListener();
                mWindowManager = (WindowManager) iLogApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
                mDummyView = new LinearLayout(iLogApplication.getAppContext());

                WindowManager.LayoutParams params = null;
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    params = new WindowManager.LayoutParams(
                            1, /* width */
                            1, /* height */
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                            PixelFormat.TRANSPARENT
                    );
                }
                else {
                    params = new WindowManager.LayoutParams(
                            1, /* width */
                            1, /* height */
                            WindowManager.LayoutParams.TYPE_PHONE,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                            PixelFormat.TRANSPARENT
                    );
                }

                params.gravity = Gravity.LEFT | Gravity.TOP;
                mDummyView.setLayoutParams(params);
                mDummyView.setOnTouchListener(touchEventListener);
                mWindowManager.addView(mDummyView, params);
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
     *     <li>Removes the view {@link #mDummyView} from the {@link #mWindowManager} using {@link android.view.WindowManager#removeView(View)}</li>
     *     <li>Persists a {@link SM} event that indicates that the sensor just stopped collecting data</li>
     *     <li>Updates the {@link iLogApplication#sensorLoggingState} variable to indicate that the sensor is stopped</li>
     *     <li>Persists a {@link ST} event that indicates that this runnable has been stopped</li>
     *     <li>Sets this runnable as stopped</li>
     * </ul>
     */
    public void stop() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped() && mWindowManager!=null && mDummyView!=null) {
                try {
                    mWindowManager.removeView(mDummyView);
                } catch (Exception e) {

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
     *     <li>Removes the view {@link #mDummyView} from the {@link #mWindowManager} using {@link android.view.WindowManager#removeView(View)}</li>
     *     <li>Initializes the {@link TouchEventListener} variable where the detection of the event occurs</li>
     *     <li>Sets the {@link TouchEventListener} using {@link LinearLayout#setOnTouchListener(View.OnTouchListener)} on the {@link #mDummyView}</li>
     * </ul>
     */
    public void restart() {
        if(iLogApplication.sensorLoggingState.get(SENSOR_ID) != null) {
            if(!isStopped() && iLogApplication.hasDrawOnTopPermissions()) {
                if(!isStopped() && mWindowManager!=null && mDummyView!=null) {
                    try {
                        mWindowManager.removeView(mDummyView);
                    } catch (Exception e) {

                    }
                }

                iLogApplication.sensorLoggingState.put(SENSOR_ID, true);

                TouchEventListener touchEventListener = new TouchEventListener();
                mWindowManager = (WindowManager) iLogApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE);
                mDummyView = new LinearLayout(iLogApplication.getAppContext());

                WindowManager.LayoutParams params = null;
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    params = new WindowManager.LayoutParams(
                            1, /* width */
                            1, /* height */
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                            PixelFormat.TRANSPARENT
                    );
                }
                else {
                    params = new WindowManager.LayoutParams(
                            1, /* width */
                            1, /* height */
                            WindowManager.LayoutParams.TYPE_PHONE,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                            PixelFormat.TRANSPARENT
                    );
                }
                params.gravity = Gravity.LEFT | Gravity.TOP;
                mDummyView.setLayoutParams(params);
                mDummyView.setOnTouchListener(touchEventListener);
                mWindowManager.addView(mDummyView, params);
            }
        }
    }
}
