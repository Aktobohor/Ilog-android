package it.unitn.disi.witmee.sensorlog.model.sensors;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static it.unitn.disi.witmee.sensorlog.utils.Utils.roundFloat;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 30/05/13
 * Time: 14.11
 */
public class LA extends AbstractSensorEvent {
    public static final String X_ACCELERATION = "xacceleration";
    public static final String Y_ACCELERATION = "yacceleration";
    public static final String Z_ACCELERATION = "zacceleration";

    /**
     * Acceleration force along the x axis (excluding gravity), m/s2
     */
    private int xAcceleration;

    /**
     * Acceleration force along the y axis (excluding gravity), m/s2
     */
    private int yAcceleration;

    /**
     * Acceleration force along the z axis (excluding gravity), m/s2
     */
    private int zAcceleration;

    public LA() {
    }

    public LA(long timestamp, int accuracy, float xAcceleration, float yAcceleration, float zAcceleration) {
        super(timestamp, accuracy, 0);
        this.xAcceleration = roundFloat(xAcceleration, 100);
        this.yAcceleration = roundFloat(yAcceleration, 100);
        this.zAcceleration = roundFloat(zAcceleration, 100);
    }

    public int getxAcceleration() {
        return xAcceleration;
    }

    public void setxAcceleration(float xAcceleration) {
        this.xAcceleration = roundFloat(xAcceleration, 100);
    }

    public int getyAcceleration() {
        return yAcceleration;
    }

    public void setyAcceleration(float yAcceleration) {
        this.yAcceleration = roundFloat(yAcceleration, 100);
    }

    public int getzAcceleration() {
        return zAcceleration;
    }

    public void setzAcceleration(float zAcceleration) {
        this.zAcceleration = roundFloat(zAcceleration, 100);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getxAcceleration()+Utils.SEPARATOR+
                getyAcceleration()+Utils.SEPARATOR+
                getzAcceleration()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
