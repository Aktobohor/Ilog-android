package it.unitn.disi.witmee.sensorlog.elements;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.google.android.gms.maps.MapView;

/**
 * Custom {@link MapView} element.
 * @see <a href="https://code-examples.net/en/q/63e2bc">https://code-examples.net/en/q/63e2bc</a>
 */
public class ScrollableMapView extends MapView {

    public ScrollableMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * We need to override the {@link #dispatchTouchEvent(MotionEvent)} method because the {@link ScrollableMapView} is put inside a {@link android.preference.Preference} item.
     * If we don't override this method the user experiences problems in moving inside the map with the finger.
     * @param ev {@link MotionEvent} event to be monitored
     * @return default return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                this.getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_DOWN:
                this.getParent().requestDisallowInterceptTouchEvent(true);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
