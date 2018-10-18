package it.unitn.disi.witmee.sensorlog.model.system;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by mattiazeni on 5/15/17.
 */

public class FullInfo {

    private static String statistics;

    public FullInfo() {

        try {
            JSONObject deviceinfo = new JSONObject();
            deviceinfo.put("totalram", getTotalRAM());
            deviceinfo.put("freeram", getFreeRAM());
            deviceinfo.put("usedram", getUsedRAM());
            deviceinfo.put("freeexternalmemory", getFreeExternalMemory());
            deviceinfo.put("freeinternalmemory", getFreeInternalMemory());
            deviceinfo.put("freesystemmemory", getFreeSystemMemory());
            deviceinfo.put("screenstatus", isScreenActive(iLogApplication.getAppContext()));
            deviceinfo.put("userpresence", isUserPresent());
            deviceinfo.put("releaseversion", Build.VERSION.RELEASE);
            deviceinfo.put("sdkversion", Build.VERSION.SDK_INT);
            deviceinfo.put("brand", Build.BRAND);
            deviceinfo.put("host", Build.HOST);
            deviceinfo.put("manufacturer", Build.MANUFACTURER);
            deviceinfo.put("model", Build.MODEL);
            deviceinfo.put("product", Build.PRODUCT);
            deviceinfo.put("connectedto", getWifiSSIDConnectedTo());
            deviceinfo.put("batterystatus", getBatteryPercentage(iLogApplication.getAppContext()));
            deviceinfo.put("totalmemory", totalMemoryMB());
            deviceinfo.put("installedapps", getInstalledApps(iLogApplication.getAppContext()));

            JSONObject logginginfo = new JSONObject();
            logginginfo.put("sensorlist", getSensorsList().toString());
            logginginfo.put("userloggedin", iLogApplication.isUserLoggedIn()+"");
            logginginfo.put("permissionslist", getAllPermissions());
            logginginfo.put("sensorloggingstatelist", iLogApplication.sensorLoggingState+"");
            logginginfo.put("loggingsince", iLogApplication.startTimestamp+"");
            logginginfo.put("numberoflogfiles", iLogApplication.getLogFilesNumber()+"");
            logginginfo.put("numberofquestions", iLogApplication.db.getAllQuestions().size()+"");
            if(iLogApplication.hasSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                JSONObject location = new JSONObject();
                location.put("latitue", ((LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude());
                location.put("longitude", ((LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude());
                location.put("altitude", ((LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER).getAltitude());
                location.put("accuracy", ((LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER).getAccuracy());
                location.put("speed", ((LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER).getSpeed());
                location.put("timestamp", Utils.longToStringFormat(((LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER).getTime()));
                logginginfo.put("lastknowngpslocation", location);
            }
            if(iLogApplication.hasSinglePermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                JSONObject location = new JSONObject();
                location.put("latitue", ((LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude());
                location.put("longitude", ((LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude());
                location.put("altitude", ((LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getAltitude());
                location.put("accuracy", ((LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getAccuracy());
                location.put("speed", ((LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getSpeed());
                location.put("timestamp", Utils.longToStringFormat(((LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getTime()));
                logginginfo.put("lastknownnetworklocation", location);
            }

            JSONObject object = new JSONObject();
            object.put("deviceinfo", deviceinfo);
            object.put("logginginfo", logginginfo);
            statistics = object.toString();

            new HttpAsyncTask().execute("");
            Log.d("FINFO", statistics);
        }
        catch(Exception e) {
            statistics = "";
        }

    }

    //static, variable
    //http://stackoverflow.com/questions/3213205/how-to-detect-system-information-like-os-or-device-type
    //http://stackoverflow.com/questions/31239817/how-can-we-get-the-hardware-information-of-android-device-programmatically
    //http://stackoverflow.com/questions/7115016/how-to-find-the-amount-of-free-storage-disk-space-left-on-android
    /*
    imei;
    deviceBluetoothId;
    deviceWifiId;
    device3gId;
    buildId;
    displayRefreshRate;
    displayDensity;
    displayPhisicalSize;
    processorName;
    processorCores;
    processorFrequency;
    processorInstructorSet;
    systemMemory;
    internalStorage;
    externalStorage;
    backCameraMaxPictureSize;
    backCameraZoom;
    video...
    logInProgress;
    filesToSync;
    ipAddresses;
    connectedWifi;
    connected3g;
    connectedBT;
    batteryLevel;
    batteryVoltage;
    batteryTemperature;
    batteryStatus;
    batteryHealth;
    location;
    */

    public String getTotalRAM() {

        RandomAccessFile reader = null;
        String load = null;
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        double totRam = 0;
        String lastValue = "";
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();

            // Get the Number value from the string
            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find()) {
                value = m.group(1);
                // System.out.println("Ram : " + value);
            }
            reader.close();

            totRam = Double.parseDouble(value);
            // totRam = totRam / 1024;

            double mb = totRam / 1024.0;
            double gb = totRam / 1048576.0;
            double tb = totRam / 1073741824.0;

            if (tb > 1) {
                lastValue = twoDecimalForm.format(tb).concat(" TB");
            } else if (gb > 1) {
                lastValue = twoDecimalForm.format(gb).concat(" GB");
            } else if (mb > 1) {
                lastValue = twoDecimalForm.format(mb).concat(" MB");
            } else {
                lastValue = twoDecimalForm.format(totRam).concat(" KB");
            }



        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            // Streams.close(reader);
        }

        return lastValue;
    }

    private String getFreeRAM() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) iLogApplication.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return bytesToHuman(mi.availMem);
    }

    private String getUsedRAM() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) iLogApplication.getAppContext().getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return bytesToHuman(mi.totalMem-mi.availMem);
    }
    private boolean supportsSensorBatch() {
        SensorManager mgr = (SensorManager) iLogApplication.getAppContext().getSystemService(SENSOR_SERVICE);
        Sensor accelerometer = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        int fifoSize = accelerometer.getFifoReservedEventCount();
        if (fifoSize > 0) {
            return true;
        }
        return false;
    }

    public long getFreeInternalMemory() {
        return getFreeMemory(Environment.getDataDirectory());
    }

    // Get external (SDCARD) free space
    public long getFreeExternalMemory() {
        return getFreeMemory(Environment.getExternalStorageDirectory());
    }

    // Get Android OS (system partition) free space
    public long getFreeSystemMemory() {
        return getFreeMemory(Environment.getRootDirectory());
    }

    // Get free space for provided path
// Note that this will throw IllegalArgumentException for invalid paths
    public long getFreeMemory(File path) {
        StatFs stats = new StatFs(path.getAbsolutePath());
        return stats.getAvailableBlocksLong() * stats.getBlockSizeLong();
    }

    public static String floatForm (double d) {
        return new DecimalFormat("#.##").format(d);
    }


    public static String bytesToHuman (long size) {
        long Kb = 1  * 1024;
        long Mb = Kb * 1024;
        long Gb = Mb * 1024;
        long Tb = Gb * 1024;
        long Pb = Tb * 1024;
        long Eb = Pb * 1024;

        if (size <  Kb)                 return floatForm(        size     ) + " byte";
        if (size >= Kb && size < Mb)    return floatForm((double)size / Kb) + " Kb";
        if (size >= Mb && size < Gb)    return floatForm((double)size / Mb) + " Mb";
        if (size >= Gb && size < Tb)    return floatForm((double)size / Gb) + " Gb";
        if (size >= Tb && size < Pb)    return floatForm((double)size / Tb) + " Tb";
        if (size >= Pb && size < Eb)    return floatForm((double)size / Pb) + " Pb";
        if (size >= Eb)                 return floatForm((double)size / Eb) + " Eb";

        return "???";
    }

    /*
    public static String getDeviceInfo(Context activity, BluetoothClass.Device device) {
        try {
            switch (device) {
                case DEVICE_LANGUAGE:
                    return Locale.getDefault().getDisplayLanguage();
                case DEVICE_TIME_ZONE:
                    return TimeZone.getDefault().getID();//(false, TimeZone.SHORT);
                case DEVICE_LOCAL_COUNTRY_CODE:
                    return activity.getResources().getConfiguration().locale.getCountry();
                case DEVICE_CURRENT_YEAR:
                    return "" + (Calendar.getInstance().get(Calendar.YEAR));
                case DEVICE_CURRENT_DATE_TIME:
                    Calendar calendarTime = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    long time = (calendarTime.getTimeInMillis() / 1000);
                    return String.valueOf(time);
                //                    return DateFormat.getDateTimeInstance().format(new Date());
                case DEVICE_CURRENT_DATE_TIME_ZERO_GMT:
                    Calendar calendarTime_zero = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"), Locale.getDefault());
                    return String.valueOf((calendarTime_zero.getTimeInMillis() / 1000));
                //                    DateFormat df = DateFormat.getDateTimeInstance();
                //                    df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
                //                    return df.format(new Date());
                case DEVICE_HARDWARE_MODEL:
                    return getDeviceName();
                case DEVICE_NUMBER_OF_PROCESSORS:
                    return Runtime.getRuntime().availableProcessors() + "";
                case DEVICE_LOCALE:
                    return Locale.getDefault().getISO3Country();
                case DEVICE_IP_ADDRESS_IPV4:
                    return getIPAddress(true);
                case DEVICE_IP_ADDRESS_IPV6:
                    return getIPAddress(false);
                case DEVICE_MAC_ADDRESS:
                    String mac = getMACAddress("wlan0");
                    if (TextUtils.isEmpty(mac)) {
                        mac = getMACAddress("eth0");
                    }
                    if (TextUtils.isEmpty(mac)) {
                        mac = "DU:MM:YA:DD:RE:SS";
                    }
                    return mac;

                case DEVICE_TOTAL_MEMORY:
                    if (Build.VERSION.SDK_INT >= 16)
                        return String.valueOf(getTotalMemory(activity));
                case DEVICE_FREE_MEMORY:
                    return String.valueOf(getFreeMemory(activity));
                case DEVICE_USED_MEMORY:
                    if (Build.VERSION.SDK_INT >= 16) {
                        long freeMem = getTotalMemory(activity) - getFreeMemory(activity);
                        return String.valueOf(freeMem);
                    }
                    return "";
                case DEVICE_TOTAL_CPU_USAGE:
                    int[] cpu = getCpuUsageStatistic();
                    if (cpu != null) {
                        int total = cpu[0] + cpu[1] + cpu[2] + cpu[3];
                        return String.valueOf(total);
                    }
                    return "";
                case DEVICE_TOTAL_CPU_USAGE_SYSTEM:
                    int[] cpu_sys = getCpuUsageStatistic();
                    if (cpu_sys != null) {
                        int total = cpu_sys[1];
                        return String.valueOf(total);
                    }
                    return "";
                case DEVICE_TOTAL_CPU_USAGE_USER:
                    int[] cpu_usage = getCpuUsageStatistic();
                    if (cpu_usage != null) {
                        int total = cpu_usage[0];
                        return String.valueOf(total);
                    }
                    return "";
                case DEVICE_MANUFACTURE:
                    return android.os.Build.MANUFACTURER;
                case DEVICE_SYSTEM_VERSION:
                    return String.valueOf(getDeviceName());
                case DEVICE_VERSION:
                    return String.valueOf(android.os.Build.VERSION.SDK_INT);
                case DEVICE_IN_INCH:
                    return getDeviceInch(activity);
                case DEVICE_TOTAL_CPU_IDLE:
                    int[] cpu_idle = getCpuUsageStatistic();
                    if (cpu_idle != null) {
                        int total = cpu_idle[2];
                        return String.valueOf(total);
                    }
                    return "";
                case DEVICE_NETWORK_TYPE:
                    return getNetworkType(activity);
                case DEVICE_NETWORK:
                    return checkNetworkStatus(activity);
                case DEVICE_TYPE:
                    if (isTablet(activity)) {
                        if (getDeviceMoreThan5Inch(activity)) {
                            return "Tablet";
                        } else
                            return "Mobile";
                    } else {
                        return "Mobile";
                    }
                case DEVICE_SYSTEM_NAME:
                    return "Android OS";
                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }*/

    public String totalCPUUsage() {
        int[] cpu = getCpuUsageStatistic();
        if (cpu != null) {
            int total = cpu[0] + cpu[1] + cpu[2] + cpu[3];
            return String.valueOf(total);
        }
        return String.valueOf(0);
    }

    public String totalCPUUsageSystem() {
        int[] cpu_sys = getCpuUsageStatistic();
        if (cpu_sys != null) {
            int total = cpu_sys[1];
            return String.valueOf(total);
        }
        return String.valueOf(0);
    }

    public String totalCPUUsageUser() {
        int[] cpu_usage = getCpuUsageStatistic();
        if (cpu_usage != null) {
            int total = cpu_usage[0];
            return String.valueOf(total);
        }
        return String.valueOf(0);
    }

    public static String getDeviceId(Context context) {
        String device_uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (device_uuid == null) {
            device_uuid = "12356789"; // for emulator testing
        } else {
            try {
                byte[] _data = device_uuid.getBytes();
                MessageDigest _digest = java.security.MessageDigest.getInstance("MD5");
                _digest.update(_data);
                _data = _digest.digest();
                BigInteger _bi = new BigInteger(_data).abs();
                device_uuid = _bi.toString(36);
            } catch (Exception e) {
                if (e != null) {
                    e.printStackTrace();
                }
            }
        }
        return device_uuid;
    }

    @SuppressLint("NewApi")
    private static long getTotalMemory(Context activity) {
        try {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
            long availableMegs = mi.totalMem / 1048576L; // in megabyte (mb)

            return availableMegs;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static long getFreeMemory(Context activity) {
        try {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
            long availableMegs = mi.availMem / 1048576L; // in megabyte (mb)

            return availableMegs;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /**
     * Convert byte array to hex string
     *
     * @param bytes
     * @return
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sbuf = new StringBuilder();
        for (int idx = 0; idx < bytes.length; idx++) {
            int intVal = bytes[idx] & 0xff;
            if (intVal < 0x10)
                sbuf.append("0");
            sbuf.append(Integer.toHexString(intVal).toUpperCase());
        }
        return sbuf.toString();
    }

    /**
     * Returns MAC address of the given interface name.
     *
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return mac address or empty string
     */
    @SuppressLint("NewApi")
    private static String getMACAddress(String interfaceName) {
        try {

            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName))
                        continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null)
                    return "";
                StringBuilder buf = new StringBuilder();
                for (int idx = 0; idx < mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length() > 0)
                    buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ex) {
            return "";
        } // for now eat exceptions
        return "";
            /*
             * try { // this is so Linux hack return
             * loadFileAsString("/sys/class/net/" +interfaceName +
             * "/address").toUpperCase().trim(); } catch (IOException ex) { return
             * null; }
             */
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @return address or empty string
     */
    private static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port
                                // suffix
                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    /*
     *
     * @return integer Array with 4 elements: user, system, idle and other cpu
     * usage in percentage.
     */
    private static int[] getCpuUsageStatistic() {
        try {
            String tempString = executeTop();

            tempString = tempString.replaceAll(",", "");
            tempString = tempString.replaceAll("User", "");
            tempString = tempString.replaceAll("System", "");
            tempString = tempString.replaceAll("IOW", "");
            tempString = tempString.replaceAll("IRQ", "");
            tempString = tempString.replaceAll("%", "");
            for (int i = 0; i < 10; i++) {
                tempString = tempString.replaceAll("  ", " ");
            }
            tempString = tempString.trim();
            String[] myString = tempString.split(" ");
            int[] cpuUsageAsInt = new int[myString.length];
            for (int i = 0; i < myString.length; i++) {
                myString[i] = myString[i].trim();
                cpuUsageAsInt[i] = Integer.parseInt(myString[i]);
            }
            return cpuUsageAsInt;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("executeTop", "error in getting cpu statics");
            return null;
        }
    }

    private static String executeTop() {
        java.lang.Process p = null;
        BufferedReader in = null;
        String returnString = null;
        try {
            p = Runtime.getRuntime().exec("top -n 1");
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (returnString == null || returnString.contentEquals("")) {
                returnString = in.readLine();
            }
        } catch (IOException e) {
            Log.e("executeTop", "error in getting first line of top");
            e.printStackTrace();
        } finally {
            try {
                in.close();
                p.destroy();
            } catch (IOException e) {
                Log.e("executeTop", "error in closing and destroying top process");
                e.printStackTrace();
            }
        }
        return returnString;
    }

    public static String getNetworkType(final Context activity) {
        String networkStatus = "";

        final ConnectivityManager connMgr = (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        // check for wifi
        final android.net.NetworkInfo wifi =
                connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        // check for mobile data
        final android.net.NetworkInfo mobile =
                connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isAvailable()) {
            networkStatus = "Wifi";
        } else if (mobile.isAvailable()) {
            networkStatus = getDataType(activity);
        } else {
            networkStatus = "noNetwork";
        }
        return networkStatus;
    }

    public static String checkNetworkStatus(final Context activity) {
        String networkStatus = "";
        try {
            // Get connect mangaer
            final ConnectivityManager connMgr = (ConnectivityManager)
                    activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            // // check for wifi
            final android.net.NetworkInfo wifi =
                    connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            // // check for mobile data
            final android.net.NetworkInfo mobile =
                    connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (wifi.isAvailable()) {
                networkStatus = "Wifi";
            } else if (mobile.isAvailable()) {
                networkStatus = getDataType(activity);
            } else {
                networkStatus = "noNetwork";
                networkStatus = "0";
            }


        } catch (Exception e) {
            e.printStackTrace();
            networkStatus = "0";
        }
        return networkStatus;

    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean getDeviceMoreThan5Inch(Context activity) {
        try {
            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
            // int width = displayMetrics.widthPixels;
            // int height = displayMetrics.heightPixels;

            float yInches = displayMetrics.heightPixels / displayMetrics.ydpi;
            float xInches = displayMetrics.widthPixels / displayMetrics.xdpi;
            double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
            if (diagonalInches >= 7) {
                // 5inch device or bigger
                return true;
            } else {
                // smaller device
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String getDeviceInch(Context activity) {
        try {
            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();

            float yInches = displayMetrics.heightPixels / displayMetrics.ydpi;
            float xInches = displayMetrics.widthPixels / displayMetrics.xdpi;
            double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
            return String.valueOf(diagonalInches);
        } catch (Exception e) {
            return "-1";
        }
    }

    public static String getDataType(Context activity) {
        String type = "Mobile Data";
        TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        switch (tm.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                type = "Mobile Data 3G";
                Log.d("Type", "3g");
                // for 3g HSDPA networktype will be return as
                // per testing(real) in device with 3g enable
                // data
                // and speed will also matters to decide 3g network type
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                type = "Mobile Data 4G";
                Log.d("Type", "4g");
                // No specification for the 4g but from wiki
                // i found(HSPAP used in 4g)
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                type = "Mobile Data GPRS";
                Log.d("Type", "GPRS");
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                type = "Mobile Data EDGE 2G";
                Log.d("Type", "EDGE 2g");
                break;

        }

        return type;
    }

    /*
    public List<ResolveInfo> getInstalledApps(Context context) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return context.getPackageManager().queryIntentActivities( mainIntent, 0);
    }*/

    public JSONArray getInstalledApps(Context context) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        JSONArray list = new JSONArray();
        for(ResolveInfo ri: context.getPackageManager().queryIntentActivities( mainIntent, 0)) {
            try {
                JSONObject application = new JSONObject();
                application.put("packagename", ri.activityInfo.packageName);
                application.put("applicationname", ri.loadLabel(context.getPackageManager()));
                list.put(application);
            }
            catch(Exception e) {

            }
        }
        return list;
    }

    public boolean isScreenActive(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.isInteractive();
    }

    public boolean isUserPresent() {
        return iLogApplication.isUserPresent;
    }

    public String getCarrier(Context context) {
        TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getNetworkOperatorName();
    }

    public JSONArray getSensorsList() {
        SensorManager oSM = (SensorManager) iLogApplication.getAppContext().getSystemService(Context.SENSOR_SERVICE);
        JSONArray list = new JSONArray();
        for(Sensor s : oSM.getSensorList(Sensor.TYPE_ALL)) {
            list.put(s.getName());
        }
        return list;
    }

    public String getWifiSSIDConnectedTo() {
        try {
            WifiManager wifiManager = (WifiManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo ();
            return info.getSSID();
        }
        catch(Exception e) {
            return "";
        }
    }

    public String getAllPermissions() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Read Contacts", iLogApplication.hasSinglePermission(Manifest.permission.READ_CONTACTS));
        object.put("Access Coarse Location", iLogApplication.hasSinglePermission(Manifest.permission.ACCESS_COARSE_LOCATION));
        object.put("Access Fine Location", iLogApplication.hasSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION));
        object.put("Record Audio", iLogApplication.hasSinglePermission(Manifest.permission.RECORD_AUDIO));
        object.put("Read Sms", iLogApplication.hasSinglePermission(Manifest.permission.READ_SMS));
        object.put("Receive Sms", iLogApplication.hasSinglePermission(Manifest.permission.RECEIVE_SMS));
        object.put("Process Outgoing Calls", iLogApplication.hasSinglePermission(Manifest.permission.PROCESS_OUTGOING_CALLS));
        object.put("Read Phone State", iLogApplication.hasSinglePermission(Manifest.permission.READ_PHONE_STATE));
        object.put("Bluetooth", iLogApplication.hasSinglePermission(Manifest.permission.BLUETOOTH));
        object.put("Bluetooth Admin", iLogApplication.hasSinglePermission(Manifest.permission.BLUETOOTH_ADMIN));
        object.put("Camera", iLogApplication.hasSinglePermission(Manifest.permission.CAMERA));
        object.put("Has Battery Ignore Permissions", iLogApplication.hasBatteryIgnorePermission());
        object.put("Has Touch Permissions", iLogApplication.hasDrawOnTopPermissions());
        object.put("Has Notification Permissions", iLogApplication.hasNotificationAccessPermission());
        object.put("Read Applications Permissions", iLogApplication.hasUsageStatsPermission());
        return object.toString();
    }

    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }

    private long totalMemoryMB() {
        long KILOBYTE = 1024;

        StatFs internalStatFs = new StatFs( Environment.getRootDirectory().getAbsolutePath() );
        long internalTotal;
        long internalFree;

        StatFs externalStatFs = new StatFs( Environment.getExternalStorageDirectory().getAbsolutePath() );
        long externalTotal;
        long externalFree;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            internalTotal = ( internalStatFs.getBlockCountLong() * internalStatFs.getBlockSizeLong() ) / ( KILOBYTE * KILOBYTE );
            internalFree = ( internalStatFs.getAvailableBlocksLong() * internalStatFs.getBlockSizeLong() ) / ( KILOBYTE * KILOBYTE );
            externalTotal = ( externalStatFs.getBlockCountLong() * externalStatFs.getBlockSizeLong() ) / ( KILOBYTE * KILOBYTE );
            externalFree = ( externalStatFs.getAvailableBlocksLong() * externalStatFs.getBlockSizeLong() ) / ( KILOBYTE * KILOBYTE );
        }
        else {
            internalTotal = ( (long) internalStatFs.getBlockCount() * (long) internalStatFs.getBlockSize() ) / ( KILOBYTE * KILOBYTE );
            internalFree = ( (long) internalStatFs.getAvailableBlocks() * (long) internalStatFs.getBlockSize() ) / ( KILOBYTE * KILOBYTE );
            externalTotal = ( (long) externalStatFs.getBlockCount() * (long) externalStatFs.getBlockSize() ) / ( KILOBYTE * KILOBYTE );
            externalFree = ( (long) externalStatFs.getAvailableBlocks() * (long) externalStatFs.getBlockSize() ) / ( KILOBYTE * KILOBYTE );
        }

        return internalTotal + externalTotal;
    }

    public static class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... data) {
            iLogApplication.postFullInfo(statistics);
            return null;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        }
    }
}
