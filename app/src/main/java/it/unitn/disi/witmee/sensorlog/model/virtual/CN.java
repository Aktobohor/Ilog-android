package it.unitn.disi.witmee.sensorlog.model.virtual;

import it.unitn.disi.witmee.sensorlog.model.sensors.AbstractSensorEvent;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 ** User: Mattia
 * Date: 23/05/13
 * Time: 18.27
 */
public class CN extends AbstractSensorEvent {

    public static final String KEY_TYPE = "type";
    public static final String KEY_DBM = "dbm";
    public static final String KEY_CELLID = "cellId";

    public static final String TYPE_WCDMA = "wcdma";
    public static final String TYPE_CDMA = "cdma";
    public static final String TYPE_GSM = "gsm";
    public static final String TYPE_LTE = "lte";

    private int dbm = -1;
    private int cellId = -1;
    private String type = "";

    public CN() {
    }

    public CN(long timestamp, int accuracy, int dbm, int cellId, String type) {
        super(timestamp, accuracy, 0);
        this.dbm = dbm;
        this.cellId = cellId;
        this.type = type;

        //System.out.println(this.toString());
    }

    public int getDbm() {
        return dbm;
    }

    public void setDbm(int dbm) {
        this.dbm = dbm;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+ Utils.SEPARATOR+
                this.cellId+ Utils.SEPARATOR+
                this.dbm+ Utils.SEPARATOR+
                this.type+ Utils.SEPARATOR+
                Utils.longToStringFormat(getTimestamp());
    }
}
