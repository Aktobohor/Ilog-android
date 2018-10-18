package it.unitn.disi.witmee.sensorlog.model.sensors;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static it.unitn.disi.witmee.sensorlog.utils.Utils.roundFloat;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 30/05/13
 * Time: 14.14
 */
public class RV extends AbstractSensorEvent {
    public static final String X_ROTATION = "xrotation";
    public static final String Y_ROTATION = "yrotation";
    public static final String Z_ROTATION = "zrotation";
    public static final String SCALAR = "scalar";

    /**
     * Rotation vector component along the x axis (x * sin(θ/2)).
     */
    private int xRotation;

    /**
     * Rotation vector component along the y axis (x * sin(θ/2)).
     */
    private int yRotation;

    /**
     * Rotation vector component along the z axis (x * sin(θ/2)).
     */
    private int zRotation;

    /**
     * Scalar component of the rotation vector ((cos(θ/2))
     */
    private int scalar;

    public RV() {
    }

    public RV(long timestamp, float accuracy, float xRotation, float yRotation, float zRotation, Float scalar) {
        super(timestamp, accuracy, 0);
        this.xRotation = roundFloat(xRotation, 100);
        this.yRotation = roundFloat(yRotation, 100);
        this.zRotation = roundFloat(zRotation, 100);
        this.scalar = roundFloat(scalar, 100);
    }

    public int getxRotation() {
        return xRotation;
    }

    public void setxRotation(float xRotation) {
        this.xRotation = roundFloat(xRotation, 100);
    }

    public int getyRotation() {
        return yRotation;
    }

    public void setyRotation(float yRotation) {
        this.yRotation = roundFloat(yRotation, 100);
    }

    public int getzRotation() {
        return zRotation;
    }

    public void setzRotation(float zRotation) {
        this.zRotation = roundFloat(zRotation, 100);
    }

    public int getScalar() {
        return scalar;
    }

    public void setScalar(Float scalar) {
        this.scalar = roundFloat(scalar, 100);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getxRotation()+Utils.SEPARATOR+
                getyRotation()+Utils.SEPARATOR+
                getzRotation()+Utils.SEPARATOR+
                getScalar()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
