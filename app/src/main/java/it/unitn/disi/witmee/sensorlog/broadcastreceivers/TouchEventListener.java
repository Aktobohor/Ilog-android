package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.model.system.AM;
import it.unitn.disi.witmee.sensorlog.model.virtual.TE;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link TE} event
 */
public class TouchEventListener implements View.OnTouchListener {

    /**
     * Method called when the {@link Intent} is received. The intent we are interested in is {@link Intent#ACTION_AIRPLANE_MODE_CHANGED}. Depending on the state,
     * we generate an event with {@link iLogApplication#persistInMemoryEvent(AbstractSensorEvent)}
     * @param v {@link View}
     * @param event {@link MotionEvent} event
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        /**
         * Log event without coordinates, they are always set to 0.0 due to security reasons.
         * @see <a href="https://stackoverflow.com/questions/22041604/why-does-action-outside-return-0-everytime-on-kitkat-4-4-2/24502062">https://stackoverflow.com/questions/22041604/why-does-action-outside-return-0-everytime-on-kitkat-4-4-2/24502062</a>
         */
        Log.d(this.toString(), "TOUCH");
        iLogApplication.persistInMemoryEvent(new TE(System.currentTimeMillis(), 0));

        return false;
    }
}