package it.unitn.disi.witmee.sensorlog.model.sensors;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 23/05/13
 * Time: 18.27
 */
public class AC extends AbstractSensorEvent {

    public static final String X_ACCELERATION = "xacceleration";
    public static final String Y_ACCELERATION = "yacceleration";
    public static final String Z_ACCELERATION = "zacceleration";

    /**
     * Acceleration minus Gx on the x-axis
     */
    private int xAcceleration;

    /**
     * Acceleration minus Gy on the y-axis
     */
    private int yAcceleration;

    /**
     * Acceleration minus Gz on the z-axis
     */
    private int zAcceleration;

    public AC() {
    }

    public AC(long timestamp, int accuracy, float xAcceleration, float yAcceleration, float zAcceleration) {
        super(timestamp, accuracy, 0);
        this.xAcceleration = Utils.roundFloat(xAcceleration, 100);
        this.yAcceleration = Utils.roundFloat(yAcceleration, 100);
        this.zAcceleration = Utils.roundFloat(zAcceleration, 100);
    }

    public int getxAcceleration() {
        return xAcceleration;
    }

    public void setxAcceleration(int xAcceleration) {
        this.xAcceleration = Utils.roundFloat(xAcceleration, 100);
    }

    public int getyAcceleration() {
        return yAcceleration;
    }

    public void setyAcceleration(int yAcceleration) {
        this.yAcceleration = Utils.roundFloat(yAcceleration, 100);
    }

    public int getzAcceleration() {
        return zAcceleration;
    }

    public void setzAcceleration(int zAcceleration) {
        this.zAcceleration = Utils.roundFloat(zAcceleration, 100);
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
