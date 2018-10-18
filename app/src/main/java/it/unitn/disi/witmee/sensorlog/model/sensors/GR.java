package it.unitn.disi.witmee.sensorlog.model.sensors;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static it.unitn.disi.witmee.sensorlog.utils.Utils.roundFloat;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 30/05/13
 * Time: 14.07
 */
public class GR extends AbstractSensorEvent {

    public static final String X_GRAVITY = "xgravity";
    public static final String Y_GRAVITY = "ygravity";
    public static final String Z_GRAVITY = "zgravity";

    /**
     * Force of gravity along the x axis, m/s2
     */
    private int xGravity;

    /**
     * Force of gravity along the y axis, m/s2
     */
    private int yGravity;

    /**
     * Force of gravity along the z axis, m/s2
     */
    private int zGravity;

    public GR() {
    }

    public GR(long timestamp, float accuracy, float xGravity, float yGravity, float zGravity) {
        super(timestamp, accuracy, 0);
        this.xGravity = roundFloat(xGravity, 100);
        this.yGravity = roundFloat(yGravity, 100);
        this.zGravity = roundFloat(zGravity, 100);
    }

    public int getxGravity() {
        return xGravity;
    }

    public void setxGravity(float xGravity) {
        this.xGravity = roundFloat(xGravity, 100);
    }

    public int getyGravity() {
        return yGravity;
    }

    public void setyGravity(float yGravity) {
        this.yGravity = roundFloat(yGravity, 100);
    }

    public int getzGravity() {
        return zGravity;
    }

    public void setzGravity(float zGravity) {
        this.zGravity = roundFloat(zGravity, 100);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getxGravity()+Utils.SEPARATOR+
                getyGravity()+Utils.SEPARATOR+
                getzGravity()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
