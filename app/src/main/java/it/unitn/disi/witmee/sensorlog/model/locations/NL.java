package it.unitn.disi.witmee.sensorlog.model.locations;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 27/05/13
 * Time: 22.17
 */
public class NL extends LocationEvent {

    //NetworkLocationEvent

    public NL() {
    }

    public NL(long timestamp, long providerTimestamp, float accuracy, double longitude, double latitude) {
        super(timestamp, providerTimestamp, accuracy, longitude, latitude, 0);
    }

    @Override
    public String toString() {
        //NetworkLocationEvent,22.03,46.0671952,11.1500489,1476969623581
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getAccuracy()+Utils.SEPARATOR+
                getLatitude()+Utils.SEPARATOR+
                getLongitude()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
