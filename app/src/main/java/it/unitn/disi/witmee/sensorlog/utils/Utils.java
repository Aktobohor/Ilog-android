package it.unitn.disi.witmee.sensorlog.utils;

import android.os.Environment;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.unitn.disi.witmee.sensorlog.application.iLogApplication;

/**
 * Class that contains global variables and global methods used in the application.
 */
public class Utils {

    public static final String PACKAGE_NAME = "it.unitn.disi.witmee.sensorlog";
    public static final String ROLE_KEY = "it.unitn.disi.witmee.sensorlog.role";

    public static final String SEPARATOR = ",";
    public static final String SENSOR_ID = "sensor_id";
    public static final String CONFIG_PORTSEPATATOR = "portseparator";
    public static final String CONFIG_SERVERBASEURL = "serverbaseurl";
    public static final String CONFIG_APP_STARTED_TIME = "appstartedtime";
    public static final String CONFIG_LOGGINGRESTARTINTERVAL = "loggingrestartinterval";
    public static final String CONFIG_SENSORCOLLECTIONFREQUENCY = "sensorcollectionfrequency";
    public static final String CONFIG_MINIMUMBATTERYLEVEL = "minimumbatterylevel";
    public static final String CONFIG_ENDPOINTUPLOAD = "endpointupload";
    public static final String CONFIG_LOGDIR = "logdir";
    public static final String CONFIG_BLUETOOTHCOLLECTIONFREQUENCY = "bluetoothcollectionfrequency";
    public static final String CONFIG_CELLINFOFREQUENCY = "cellinfofrequency";
    public static final String CONFIG_COMPRESSEDLOGEXTENSION = "compressedlogextension";
    public static final String CONFIG_QUESTIONNAIRENOTIFICATIONID = "questionnairenotificationid";
    public static final String CONFIG_UPDATENOTIFICATIONID = "updatenotificationid";
    private static final String CONFIG_WITMEEDATAPATH = "witmeedatapath";
    public static final String CONFIG_WIFICOLLECTIONFREQUENCY = "wificollectionfrequency";
    public static final String CONFIG_MAINNOTIFICATIONID = "mainnotificationid";
    public static final String CONFIG_SEPARATOR = "separator";
    private static final String CONFIG_SERVERPORT = "serverport";
    public static final String CONFIG_APPLICATIONCOLLECTIONFREQUENCY = "applicationcollectionfrequency";
    public static final String CONFIG_BLUETOOTHLECOLLECTIONFREQUENCY = "bluetoothlecollectionfrequency";
    public static final String CONFIG_BLUETOOTHLESCANDURATION = "bluetoothlescanduration";
    public static final String CONFIG_LOCATIONCOLLECTIONFREQUENCY = "locationcollectionfrequency";
    public static final String CONFIG_LOGFILESIZELIMITSIZE = "logfilesizelimitsize";
    public static final String CONFIG_ENDPOINTLOGIN = "endpointlogin";
    public static final String CONFIG_ENDPOINTPROJECTS = "endpointprojects";
    public static final String CONFIG_ENDPOINTUPLOADPROFILE = "endpointuploadprofile";
    public static final String CONFIG_ENDPOINTUPLOADCHALLENGESSYNCHRONIZATIONINFO = "endpointuploadchallengessynchronizationinfo";
    public static final String CONFIG_ENDPOINTUPLOADRECEPTIONCONFIRMATION = "endpointuploadreceptionconfirmation";
    public static final String CONFIG_ENDPOINTUPLOADANSWERS = "endpointuploadanswers";
    public static final String CONFIG_ENDPOINTSIGNUP = "endpointsignup";
    public static final String CONFIG_PROJECTDATA = "projectdata";
    public static final String CONFIG_PROJECTSELECTIONDONE = "projectsselectiondone";
    public static final String CONFIG_LOGINDONE = "logindone";
    public static final String CONFIG_PROFILEDONE = "profiledone";
    public static final String CONFIG_NOPROFILE = "noprofile";
    public static final String CONFIG_CONSENTDONE = "consentdone";
    public static final String CONFIG_PERMISSIONSDONE = "permissionsdone";
    public static final String CONFIG_UPLOAD_IF_WIFI = "uploadifonwifi";
    public static final String CONFIG_SNOOZENOTIFICATIONS = "snoozenotifications";
    public static final String CONFIG_SENSORS_MAX_VALUES = "sensorsmaxvalues";
    public static final String CONFIG_PROFILEANSWERS = "profileanswers";
    public static final String CONFIG_PROFILE_AND_SENSORS_UPLOADED = "profileandsensorsuploaded";
    public static final String CONFIG_SLEEP_INTERVAL_HOURS = "sleepintervalhours";
    public static final String CONFIG_SLEEP_TILL = "sleeptill";

    public static final String CONFIG_ENDPOINTAVAILABLECHALLENGES = "endpointgetavailablechallenges";
    public static final String CONFIG_PORTAVAILABLECHALLENGES = "portgetavailablechallenges";
    public static final String CONFIG_ENDPOINTRESULTCHALLENGES = "endpointresultchallenges";

    public static final int TASKSNOTIFICATIONID = 8616;
    public static final int MESSAGENOTIFICATIONID = 9616;
    public static final int TIMEDIARIESNOTIFICATIONID = 404;

    /**
    By setting the constant {@link #BATCH_LATENCY} to the different values presented it is possible to set how sensor batching works. BATCH_LATENCY_0 means no batching
     */
    public static final int BATCH_LATENCY_0 = 0; // no batching
    public static final int BATCH_LATENCY_60s = 60000000;
    public static final int BATCH_LATENCY_30s = 30000000;
    public static final int BATCH_LATENCY_10s = 10000000;
    public static final int BATCH_LATENCY_5s = 5000000;
    public static final int BATCH_LATENCY = BATCH_LATENCY_0;

    //public static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss.SSS");
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmssSSS");
    public static final DateTimeFormatter dateTimeFormatterTask = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");
    public static final DateTime dateTime = new DateTime();

    /**
     * Method that converts a {@link JSONArray} of {@link JSONObject} into a {@link List}
     * @param array {@link JSONArray} to be converted
     * @return {@link List} resulting from the conversion
     */
    public static List<LatLng> jsonArrayToList(JSONArray array) {
        List<LatLng> list = new ArrayList<LatLng>();
        for(int index=0; index<array.length(); index++) {
            try {
                list.add(new LatLng(array.getJSONObject(index).getDouble("lat"), array.getJSONObject(index).getDouble("long")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * Method that converts a timestamp expressed as unixtimestamp in long to a String
     * @param epoch long representing the milliseconds from January 1st, 1970 at UTC
     * @return String formatted according to {@link #dateTimeFormatter}
     * @see <a href="https://www.unixtimestamp.com/">https://www.unixtimestamp.com/</a>
     */
    public static final String longToStringFormat(long epoch) {
        return dateTime.withMillis(epoch).toString(dateTimeFormatter);
    }

    /**
     * Method that converts a timestamp expressed String to a unixtimestamp in long
     * @param datetime String formatted according to {@link #dateTimeFormatter}
     * @return a long representing the unixtimestamp
     */
    public static final long stringToLongFormat(String datetime) {
        try {
            return dateTimeFormatter.parseDateTime(datetime).toDate().getTime();
        } catch(Exception e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
    }

    /**
     * Method that converts a timestamp expressed as unixtimestamp in long to a String
     * @param epoch long representing the milliseconds from January 1st, 1970 at UTC
     * @return String formatted according to {@link #dateTimeFormatterTask}
     * @see <a href="https://www.unixtimestamp.com/">https://www.unixtimestamp.com/</a>
     */
    public static final String longToStringFormatTasks(long epoch) {
        return dateTime.withMillis(epoch).toString(dateTimeFormatterTask);
    }

    /**
     * Method that converts a timestamp expressed as unixtimestamp in long to a String
     * @param epoch long representing the milliseconds from January 1st, 1970 at UTC
     * @return String formatted according to {@link #timeFormatter}
     * @see <a href="https://www.unixtimestamp.com/">https://www.unixtimestamp.com/</a>
     */
    public static final String longToStringFormatTime(long epoch) {
        return dateTime.withMillis(epoch).toString(timeFormatter);
    }

    /**
     * Method that returns a String representing the path of the external storage where to dump the logs (for debug purposes).
     * Normally the logs will be stored in the internal storage of the application, inside the application package.
     * @return String of the absolute path of the external storage
     */
    public static String returnAppDataPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + iLogApplication.sharedPreferences.getString(CONFIG_WITMEEDATAPATH, "");
    }

    /**
     * Method that returns the server URL up to the host and port, example: http://www.google.com:80/ using the default port specified in the experiment configuration
     * @return String of the URL
     */
    public static String returnServerUrl() {
        return iLogApplication.sharedPreferences.getString(Utils.CONFIG_SERVERBASEURL, "")+iLogApplication.sharedPreferences.getString(Utils.CONFIG_PORTSEPATATOR, "")+iLogApplication.sharedPreferences.getInt(Utils.CONFIG_SERVERPORT, 0)+iLogApplication.sharedPreferences.getString(Utils.CONFIG_SEPARATOR, "");
    }

    /**
     * Method that returns the server URL up to the host and port, example: http://www.google.com:80/ using a specific port passed as parameter
     * @param port String containing the port of the server
     * @return String of the URL
     */
    public static String returnServerUrl(String port) {
        return iLogApplication.sharedPreferences.getString(Utils.CONFIG_SERVERBASEURL, "")+iLogApplication.sharedPreferences.getString(Utils.CONFIG_PORTSEPATATOR, "")+port+iLogApplication.sharedPreferences.getString(Utils.CONFIG_SEPARATOR, "");
    }

    /**
     * Method used to cut the decimals of every sensor reading. It multiplies the float input value by a multiple of 10 according to the digits we want to keep
     * (e.g., with 100 it keeps 2 digits, etc.) and finally casts the resulting number to int to take only the integer part.<br>
     *     We decided to adopt this workaround (that requires processing at the server side before storing the values in the db so that to convert them back to float)
     *     because we needed to obtain the best efficiency. In fact, this method is called hundreds of times per second, avery time a sensor reading is generated.
     * @param d Original float number to be trimmed
     * @param f Multiple of 10 that represents the numbe rof decimals we want to keep (e.g., 1000 keeps 3 decimals)
     * @return An integer number representing the sensor reading with a limited amount of decimals number
     */
    public static int roundFloat(float d, int f) {//100, 1000, 10000
        return (int) (d * f);
    }

    /**
     * Method used to trim the decimals of the values. With respect to {@link #roundFloat(float, int)}, this method is specific for locations and specifically geo-coordinates.
     * In this case accuracy is more important than efficiency since this method is called once every minute.
     * @param doubleValue Coordinate (Latitude or Longitude) to be trimmed
     * @param decimalPlaces Number of decimal places
     * @return String representing the trimmed value
     */
    public static String roundToDecimalPlace(double doubleValue, int decimalPlaces) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(decimalPlaces);
        formatter.setMinimumFractionDigits(decimalPlaces);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        return formatter.format(doubleValue);
    }

    /**
     * Method used to remove commas and other characters from String that otherwise will create problems in the CSV based log file.
     * @param string Original String from which the method needs to remove the commas and other characters.
     * @return Clean String
     */
    public static String removeComma(String string) {
        if(string!=null) {
            return string.replace("\n", " ").replace(",", ";").replace("'", " ");
            //return string.replace(",", ";").replaceAll("[^a-zA-Z0-9]","");
        }
        else {
            return "";
        }
    }

    /**
     * Method that converts the String representing a {@link java.util.Date} in a user friendly format
     * @param original Original String containing a {@link java.util.Date}, formatted as "yyyyMMddHHmmssSSS"
     * @return String formatted according to the format "dd/MM/yyyy HH:mm"
     */
    public static String changeDateStringFormat(String original) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).format(new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.ENGLISH).parse(original));
        } catch (ParseException e) {
            e.printStackTrace();
            return original;
        }
    }

    public static String generateSalt() throws NoSuchAlgorithmException {
        SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
        String randomNum = new Integer(prng.nextInt()).toString();
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[] result =  sha.digest(randomNum.getBytes());
        String resultString = hexEncode(result);
        if(Character.isDigit(resultString.charAt(0))) {
            return generateSalt();
        }
        else {
            return resultString;
        }
    }

    /**
     * Function used to convert a byte array to a hex string
     * @param aInput inpt byte array
     * @return returns a string
     */
    private static String hexEncode(byte[] aInput){
        StringBuilder result = new StringBuilder();
        char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f'};
        for (int idx = 0; idx < aInput.length; ++idx) {
            byte b = aInput[idx];
            result.append(digits[ (b&0xf0) >> 4 ]);
            result.append(digits[ b&0x0f]);
        }
        return result.toString();
    }

    /**
     * Returns an {@link ArrayList} of {@link File} objects containing the files to be synchronized that are stored inside the application package
     * @return {@link ArrayList} of {@link File}
     */
    public static ArrayList<File> returnPicturesToSync() {
        File[] logFiles = iLogApplication.getLogDirectory().listFiles();
        final ArrayList<File> logFilesToSync = new ArrayList<File>();

        for (File f : logFiles) {
            if(f.getAbsolutePath().contains("jpeg")) {
                logFilesToSync.add(f);
            }
        }

        return logFilesToSync;
    }
}
