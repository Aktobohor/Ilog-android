package it.unitn.disi.witmee.sensorlog.model.sensors;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static it.unitn.disi.witmee.sensorlog.utils.Utils.roundFloat;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 23/05/13
 * Time: 18.38
 */
public class GY extends AbstractSensorEvent {

    public static final String X_ANGULAR_SPEED = "xangularspeed";
    public static final String Y_ANGULAR_SPEED = "yangularspeed";
    public static final String Z_ANGULAR_SPEED = "zangularspeed";

    /**
     * Angular speed around the x-axis
     */
    private int xAngularSpeed;

    /**
     * Angular speed around the y-axis
     */
    private int yAngularSpeed;

    /**
     * Angular speed around the z-axis
     */
    private int zAngularSpeed;

    public GY() {
    }

    public GY(long timestamp, int accuracy, float xAngularSpeed, float yAngularSpeed, float zAngularSpeed) {
        super(timestamp, accuracy, 0);
        this.xAngularSpeed = roundFloat(xAngularSpeed, 10000);
        this.yAngularSpeed = roundFloat(yAngularSpeed, 10000);
        this.zAngularSpeed = roundFloat(zAngularSpeed, 10000);
    }


    public int getxAngularSpeed() {
        return xAngularSpeed;
    }

    public void setxAngularSpeed(float xAngularSpeed) {
        this.xAngularSpeed = roundFloat(xAngularSpeed, 10000);
    }

    public int getyAngularSpeed() {
        return yAngularSpeed;
    }

    public void setyAngularSpeed(float yAngularSpeed) {
        this.yAngularSpeed = roundFloat(yAngularSpeed, 10000);
    }

    public int getzAngularSpeed() {
        return zAngularSpeed;
    }

    public void setzAngularSpeed(float zAngularSpeed) {
        this.zAngularSpeed = roundFloat(zAngularSpeed, 10000);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getxAngularSpeed()+Utils.SEPARATOR+
                getyAngularSpeed()+Utils.SEPARATOR+
                getzAngularSpeed()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
