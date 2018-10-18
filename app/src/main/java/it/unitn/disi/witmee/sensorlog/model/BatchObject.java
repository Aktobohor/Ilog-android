package it.unitn.disi.witmee.sensorlog.model;

/**
 * Created by mattiazeni on 04/04/2018.
 */

public class BatchObject {

    private long realTimestamp = 0;
    private long fakeTimestamp = 0;

    public BatchObject(long realTimestamp, long fakeTimestamp) {
        this.realTimestamp = realTimestamp;
        this.fakeTimestamp = fakeTimestamp;
    }

    public long getRealTimestamp() {
        return realTimestamp;
    }

    public void setRealTimestamp(long realTimestamp) {
        this.realTimestamp = realTimestamp;
    }

    public long getFakeTimestamp() {
        return fakeTimestamp;
    }

    public void setFakeTimestamp(long fakeTimestamp) {
        this.fakeTimestamp = fakeTimestamp;
    }
}
