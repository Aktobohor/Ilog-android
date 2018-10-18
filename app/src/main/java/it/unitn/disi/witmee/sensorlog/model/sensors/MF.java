package it.unitn.disi.witmee.sensorlog.model.sensors;

import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static it.unitn.disi.witmee.sensorlog.utils.Utils.roundFloat;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 23/05/13
 * Time: 18.34
 */
public class MF extends AbstractSensorEvent {

    public static final String X_MAGNETIC_FIELD = "xmagneticfield";
    public static final String Y_MAGNETIC_FIELD = "ymagneticfield";
    public static final String Z_MAGNETIC_FIELD = "zmagneticfield";


    /**
     * the ambient magnetic field in the X axis in micro-Tesla (uT)
     */
    private int xMagneticField;

    /**
     * the ambient magnetic field in the Y axis in micro-Tesla (uT)
     */
    private int yMagneticField;

    /**
     * the ambient magnetic field in the Z axis in micro-Tesla (uT)
     */
    private int zMagneticField;

    public MF() {
    }

    public MF(long timestamp, int accuracy, float xMagneticField, float yMagneticField, float zMagneticField) {
        super(timestamp, accuracy, 0);
        this.xMagneticField = roundFloat(xMagneticField, 100);
        this.yMagneticField = roundFloat(yMagneticField, 100);
        this.zMagneticField = roundFloat(zMagneticField, 100);
    }

    public int getxMagneticField() {
        return xMagneticField;
    }

    public void setxMagneticField(float xMagneticField) {
        this.xMagneticField = roundFloat(xMagneticField, 100);
    }

    public int getyMagneticField() {
        return yMagneticField;
    }

    public void setyMagneticField(float yMagneticField) {
        this.yMagneticField = roundFloat(yMagneticField, 100);
    }

    public int getzMagneticField() {
        return zMagneticField;
    }

    public void setzMagneticField(float zMagneticField) {
        this.zMagneticField = roundFloat(zMagneticField, 100);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                getxMagneticField()+Utils.SEPARATOR+
                getyMagneticField()+Utils.SEPARATOR+
                getzMagneticField()+Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
