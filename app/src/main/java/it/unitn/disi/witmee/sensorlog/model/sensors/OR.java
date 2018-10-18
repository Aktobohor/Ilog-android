package it.unitn.disi.witmee.sensorlog.model.sensors;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static it.unitn.disi.witmee.sensorlog.utils.Utils.roundFloat;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 30/05/13
 * Time: 14.20
 */
public class OR extends AbstractSensorEvent {

    public static final String X_ORIENTATION = "xorientation";
    public static final String Y_ORIENTATION = "yorientation";
    public static final String Z_ORIENTATION = "zorientation";

    //todo sensor array first returnz z
    //TODO IT WAS DEPRICATED AFTER 2.2  API LEVEL 8

    /**
     * Azimuth (angle around the x-axis), Degrees
     */
    private int xOrientation;

    /**
     * Azimuth (angle around the y-axis), Degrees
     */
    private int yOrientation;

    /**
     * Azimuth (angle around the z-axis), Degrees
     */
    private int zOrientation;

    public OR() {
    }

    public OR(long timestamp, float accuracy, float xOrientation, float yOrientation, float zOrientation) {
        super(timestamp, accuracy, 0);
        this.xOrientation = roundFloat(xOrientation, 100);
        this.yOrientation = roundFloat(yOrientation, 100);
        this.zOrientation = roundFloat(zOrientation, 100);
    }

    public int getxOrientation() {
        return xOrientation;
    }

    public void setxOrientation(float xOrientation) {
        this.xOrientation = roundFloat(xOrientation, 100);
    }

    public int getyOrientation() {
        return yOrientation;
    }

    public void setyOrientation(float yOrientation) {
        this.yOrientation = roundFloat(yOrientation, 100);
    }

    public int getzOrientation() {
        return zOrientation;
    }

    public void setzOrientation(float zOrientation) {
        this.zOrientation = roundFloat(zOrientation, 100);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getxOrientation()+Utils.SEPARATOR+
                getyOrientation()+Utils.SEPARATOR+
                getzOrientation()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
