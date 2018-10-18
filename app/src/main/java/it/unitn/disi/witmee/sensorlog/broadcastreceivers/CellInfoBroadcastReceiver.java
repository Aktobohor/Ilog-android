package it.unitn.disi.witmee.sensorlog.broadcastreceivers;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.system.AM;
import it.unitn.disi.witmee.sensorlog.model.virtual.CN;
import it.unitn.disi.witmee.sensorlog.runnables.AudioRunnable;
import it.unitn.disi.witmee.sensorlog.runnables.CellInfoRunnable;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * {@link BroadcastReceiver} used to persist in memory the {@link CN} event
 */
public class CellInfoBroadcastReceiver extends BroadcastReceiver {

    /**
     * Method called when the {@link Intent} is received. It schedules the next detection with {@link CellInfoRunnable#alarmManager} and then detects the information
     * about the cell towers, filtering out the duplicated ones.
     * @param context {@link Context} element
     * @param intent {@link Intent} that triggered the {@link BroadcastReceiver}
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        CellInfoRunnable.alarmManager = (AlarmManager) iLogApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(CellInfoRunnable.alarmManager!=null && CellInfoRunnable.pendingIntent!=null) {
                CellInfoRunnable.alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + iLogApplication.sharedPreferences.getInt(Utils.CONFIG_CELLINFOFREQUENCY, 0), CellInfoRunnable.pendingIntent);
            }
        }

        //Get the info about the available cells
        JSONArray results = getCellInfo(iLogApplication.getAppContext());

        ArrayList<JSONObject> uniqueCells = new ArrayList<JSONObject>();

        for(int index=0;index<results.length();index++) {
            try {
                if(!contains(uniqueCells, results.getJSONObject(index))) {
                    uniqueCells.add(results.getJSONObject(index));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /**
         * One by one, persist the {@link CN} events in memory
         */
        for(int index=0;index<uniqueCells.size();index++) {
            try {
                iLogApplication.persistInMemoryEvent(new CN(System.currentTimeMillis(), 0, uniqueCells.get(index).getInt(CN.KEY_DBM), uniqueCells.get(index).getInt(CN.KEY_CELLID), uniqueCells.get(index).getString(CN.KEY_TYPE)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method that returns a {@link JSONArray} of objects that are the avialable cell towers
     * @param ctx {@link Context} element
     * @return {@link JSONArray} containing the detected cellular towers
     */
    public JSONArray getCellInfo(Context ctx){
        TelephonyManager tel = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);

        JSONArray cellList = new JSONArray();

        // Type of the network
        int phoneTypeInt = tel.getPhoneType();
        String phoneType = null;
        phoneType = phoneTypeInt == TelephonyManager.PHONE_TYPE_GSM ? "gsm" : phoneType;
        phoneType = phoneTypeInt == TelephonyManager.PHONE_TYPE_CDMA ? "cdma" : phoneType;

        /**
         * From Android {@link Build.VERSION_CODES#M} up must use {@link TelephonyManager#getAllCellInfo()} instead of {@link TelephonyManager#getNeighboringCellInfo()}
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            List<NeighboringCellInfo> neighCells = tel.getNeighboringCellInfo();
            for (int i = 0; i < neighCells.size(); i++) {
                try {
                    JSONObject cellObj = new JSONObject();
                    NeighboringCellInfo thisCell = neighCells.get(i);
                    cellObj.put(CN.KEY_CELLID, thisCell.getCid());
                    cellObj.put(CN.KEY_DBM, (-113 + (2*thisCell.getRssi())));
                    cellObj.put(CN.KEY_TYPE, thisCell.getNetworkType());
                    cellList.put(cellObj);
                } catch (Exception e) {
                }
            }

        } else {
            List<CellInfo> infos = tel.getAllCellInfo();
            if(infos!=null) {
                for (int i = 0; i<infos.size(); ++i) {
                    try {
                        JSONObject cellObj = new JSONObject();
                        CellInfo info = infos.get(i);
                        //Different types of cells, gsm, let, cdma or wcdma
                        if (info instanceof CellInfoGsm){
                            CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                            CellIdentityGsm identityGsm = ((CellInfoGsm) info).getCellIdentity();
                            cellObj.put(CN.KEY_CELLID, identityGsm.getCid());
                            cellObj.put(CN.KEY_DBM, gsm.getDbm());
                            cellObj.put(CN.KEY_TYPE, CN.TYPE_GSM);
                            cellList.put(cellObj);
                        } else if (info instanceof CellInfoLte) {
                            CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                            CellIdentityLte identityLte = ((CellInfoLte) info).getCellIdentity();
                            cellObj.put(CN.KEY_CELLID, identityLte.getCi());
                            cellObj.put(CN.KEY_DBM, lte.getDbm());
                            cellObj.put(CN.KEY_TYPE, CN.TYPE_LTE);
                            cellList.put(cellObj);
                        } else if (info instanceof CellInfoCdma) {
                            CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();
                            CellIdentityCdma identityCdma = ((CellInfoCdma) info).getCellIdentity();
                            cellObj.put(CN.KEY_CELLID, identityCdma.getBasestationId());
                            cellObj.put(CN.KEY_DBM, cdma.getDbm());
                            cellObj.put(CN.KEY_TYPE, CN.TYPE_CDMA);
                            cellList.put(cellObj);
                        } else if (info instanceof CellInfoWcdma) {
                            CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info).getCellSignalStrength();
                            CellIdentityWcdma identityWcdma = ((CellInfoWcdma) info).getCellIdentity();
                            cellObj.put(CN.KEY_CELLID, identityWcdma.getCid());
                            cellObj.put(CN.KEY_DBM, wcdma.getDbm());
                            cellObj.put(CN.KEY_TYPE, CN.TYPE_WCDMA);
                            cellList.put(cellObj);
                        }

                    } catch (Exception ex) {

                    }
                }
            }
        }

        return cellList;
    }

    /**
     * Method that checks is an {@link ArrayList} of {@link JSONObject} contains a {@link JSONObject}
     * @param uniqueCells {@link ArrayList} of {@link JSONObject} of the already found cell towers
     * @param object Cell tower to be checked
     * @return True if it's already present, false otherwise
     */
    private boolean contains(ArrayList<JSONObject> uniqueCells, JSONObject object) {
        for(int index = 0; index<uniqueCells.size(); index++) {
            try {
                if(uniqueCells.get(index).getInt(CN.KEY_DBM) == object.getInt(CN.KEY_DBM) && uniqueCells.get(index).getInt(CN.KEY_CELLID) == object.getInt(CN.KEY_CELLID) && uniqueCells.get(index).getString(CN.KEY_TYPE) == object.getString(CN.KEY_TYPE)) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
