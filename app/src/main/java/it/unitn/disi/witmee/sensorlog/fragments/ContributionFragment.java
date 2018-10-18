package it.unitn.disi.witmee.sensorlog.fragments;

/**
 * Created by mattiazeni on 5/23/17.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.activities.ContributionActivity;
import it.unitn.disi.witmee.sensorlog.adapters.ChoicesArrayAdapter;
import it.unitn.disi.witmee.sensorlog.adapters.ImageChoicesArrayAdapter;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.broadcastreceivers.LocationGPSListener;
import it.unitn.disi.witmee.sensorlog.elements.MonitorableGoogleMap;
import it.unitn.disi.witmee.sensorlog.model.Challenge;
import it.unitn.disi.witmee.sensorlog.model.Choice;
import it.unitn.disi.witmee.sensorlog.model.Contribution;
import it.unitn.disi.witmee.sensorlog.model.locations.GL;
import it.unitn.disi.witmee.sensorlog.model.locations.LocationEvent;
import it.unitn.disi.witmee.sensorlog.runnables.LocationGPSRunnable;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Class that extends {@link Fragment} and is intended to be the superclass for displaying the content to be presented to the user and to collect his feedback in a
 * {@link it.unitn.disi.witmee.sensorlog.model.Task}, {@link it.unitn.disi.witmee.sensorlog.model.Question} or {@link it.unitn.disi.witmee.sensorlog.model.Challenge}.
 */
public class ContributionFragment extends Fragment  {

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Camera.PictureCallback rawCallback;
    Camera.ShutterCallback shutterCallback;
    Camera.PictureCallback jpegCallback;
    private final String tag = "VideoServer";
    private float mDist;
    LocationGPSListener locationGPSListener;
    Bundle savedInstanceState;

    private boolean isCameraPreviewStarted = false;

    FusedLocationProviderClient mFusedLocationClient;

    static ViewGroup rootView = null;
    static int contributionId = 0;

    /**
     * Method called when the view of the {@link Fragment} is created. It initializes the variables and the buttons in the optionmenu. Finally, depending on the type of subquestion
     * that needs to be visualized, it populates the view.
     * @param inflater default
     * @param container default
     * @param savedInstanceState default
     * @return default {@link View}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            final JSONObject subQuestion = new JSONObject(String.valueOf(getArguments().getString("selectedfragment")));
            contributionId = subQuestion.getJSONObject("q").getInt("id");

            this.savedInstanceState = savedInstanceState;

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(((ContributionActivity) getActivity()));

            String subQuestionType = subQuestion.getJSONObject("q").getString("t");

            System.out.println("QuestionID: "+contributionId);
            System.out.println("Numberofquestions: "+ContributionActivity.numberOfSubQuestions);

            /**
             * Depending on the type of the subquestion , it visualizes different elements in the main View. The type can be:
             * <ul>
             *     <li>t: visualize a static text message</li>
             *     <li>m: visualize a dynamic message with the elements loaded from the json</li>
             *     <li>l: visualize a pointer on the map</li>
             *     <li>i: visualize an image from an URL</li>
             *     <li>il: visualize a text question instead of having text answers the user can select an image</li>
             *     <li>ms: visualize a path on the map</li>
             *     <li>ap: we ask the user to take a picture</li>
             *     <li>al: we ask the user to put the pointer on a specific position on the map</li>
             *     <li>ac: we automatically detect the user positon</li>
             * </ul>
             */
            if(subQuestionType.equals("t")) {
                rootView = (ViewGroup) inflater.inflate(R.layout.fragment_question_list, container, false);
            }
            if(subQuestionType.equals("m")) {
                rootView = (ViewGroup) inflater.inflate(R.layout.linear_layout, container, false);
            }
            if(subQuestionType.equals("l")) {
                rootView = (ViewGroup) inflater.inflate(R.layout.fragment_question_location, container, false);
            }
            if(subQuestionType.equals("ms")) {
                rootView = (ViewGroup) inflater.inflate(R.layout.fragment_question_location, container, false);
            }
            if(subQuestionType.equals("i")) {
                rootView = (ViewGroup) inflater.inflate(R.layout.fragment_question_image, container, false);
            }
            if(subQuestionType.equals("il")) {
                rootView = (ViewGroup) inflater.inflate(R.layout.fragment_question_list, container, false);
            }
            if(subQuestionType.equals("ap")) {
                rootView = (ViewGroup) inflater.inflate(R.layout.fragment_question_action_picture, container, false);
            }
            if(subQuestionType.equals("al")) {
                rootView = (ViewGroup) inflater.inflate(R.layout.fragment_question_action_location, container, false);
            }
            if(subQuestionType.equals("ac")) {
                rootView = (ViewGroup) inflater.inflate(R.layout.fragment_question_action_coordinates, container, false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            if(contributionId==1) {
                ((ContributionActivity) getActivity()).buttonPreviousStatus(false);
            }
            else {
                ((ContributionActivity) getActivity()).buttonPreviousStatus(true);
            }

            if(contributionId==ContributionActivity.numberOfSubQuestions) {
                ((ContributionActivity) getActivity()).buttonNextSetText(R.string.finish);
            }
            else {
                ((ContributionActivity) getActivity()).buttonNextSetText(R.string.next);
            }

            final JSONObject subQuestion = new JSONObject(String.valueOf(getArguments().getString("selectedfragment")));
            contributionId = subQuestion.getJSONObject("q").getInt("id");

            String subQuestionType = subQuestion.getJSONObject("q").getString("t");

            if(contributionId==1) {
                ((ContributionActivity) getActivity()).buttonPreviousStatus(false);
            }
            else {
                ((ContributionActivity) getActivity()).buttonPreviousStatus(true);
            }

            if(contributionId==ContributionActivity.numberOfSubQuestions) {
                ((ContributionActivity) getActivity()).buttonNextSetText(R.string.finish);
            }
            else {
                ((ContributionActivity) getActivity()).buttonNextSetText(R.string.next);
            }

            /**
             * Depending on the type of the subquestion , it visualizes different elements in the main View. The type can be:
             * <ul>
             *     <li>t: visualize a static text message</li>
             *     <li>m: visualize a dynamic message with the elements loaded from the json</li>
             *     <li>l: visualize a pointer on the map</li>
             *     <li>i: visualize an image from an URL</li>
             *     <li>ms: visualize a path on the map</li>
             *     <li>ap: we ask the user to take a picture</li>
             *     <li>al: we ask the user to put the pointer on a specific position on the map</li>
             * </ul>
             */
            if(subQuestionType.equals("t")) {
                //{"q": {"id": 1,"c": [],"at": "s","t": "t","p": [{"l": "en-US","t": "What are you doing?"}, {"l": "it-IT","t": "Cosa stai facendo?"}]},"a": [{"id": 1,"c": [],"c_id": 74549,"p": [{"l": "en-US","t": "Sleeping"}, {"l": "it-IT","t": "Dormire"}]}, {"id": 2,"c": [],"c_id": 31428,"p": [{"l": "en-US","t": "Study"}, {"l": "it-IT","t": "Studio"}]}, {"id": 3,"c": [],"c_id": 4581,"p": [{"l": "en-US","t": "Lesson"}, {"l": "it-IT","t": "Lezione"}]}, {"id": 4,"c": [],"c_id": 1501,"p": [{"l": "en-US","t": "En route"}, {"l": "it-IT","t": "In viaggio/spostamento da...a..."}]}, {"id": 5,"c": [],"c_id": 4317,"p": [{"l": "en-US","t": "Eating"}, {"l": "it-IT","t": "Mangiare"}]}, {"id": 6,"c": [],"c_id": 3405,"p": [{"l": "en-US","t": "Selfcare"}, {"l": "it-IT","t": "Cura della persona"}]}, {"id": 7,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Coffee break, cigarette, beer, etc."}, {"l": "it-IT","t": "Pausa caffè, sigaretta, birra ecc"}]}, {"id": 8,"c": [],"c_id": 25864,"p": [{"l": "en-US","t": "Social life"}, {"l": "it-IT","t": "Vita Sociale/divertimento"}]}, {"id": 9,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Al the phone; in chat WhatsApp"}, {"l": "it-IT","t": "Al telefono; in chat WhatsApp"}]}, {"id": 10,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Watcing Youtube, Tv-shows, etc."}, {"l": "it-IT","t": "Guardo Youtube, Serie-Tv, ecc."}]}, {"id": 11,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Social media (Facebook, Instagram, etc.)"}, {"l": "it-IT","t": "Social media (Facebook, Instagram, ecc.)"}]}, {"id": 12,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Movie Theater, Theater, Concert, Exhibit, ..."}, {"l": "it-IT","t": "Cinema, Teatro, Concerto, Mostra, ..."}]}, {"id": 13,"c": [],"c_id": 2681,"p": [{"l": "en-US","t": "Sport"}, {"l": "it-IT","t": "Sport/Attività fisica"}]}, {"id": 14,"c": [],"c_id": 387,"p": [{"l": "en-US","t": "Shopping"}, {"l": "it-IT","t": "Shopping/Fare la spesa"}]}, {"id": 15,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Housework"}, {"l": "it-IT","t": "Lavori domestici"}]}, {"id": 16,"c": [],"c_id": 4421,"p": [{"l": "en-US","t": "Rest/nap"}, {"l": "it-IT","t": "Riposo/Pennichella"}]}, {"id": 17,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Reading a book; listening to music"}, {"l": "it-IT","t": "Leggo un libro; ascolto musica"}]}, {"id": 18,"c": [],"c_id": 2206,"p": [{"l": "en-US","t": "Hobbies"}, {"l": "it-IT","t": "Altro Hobby/tempo libero"}]}, {"id": 19,"c": [],"c_id": 112289,"p": [{"l": "en-US","t": "Work"}, {"l": "it-IT","t": "Lavoro"}]}, {"id": 20,"c": [],"c_id": 93394,"p": [{"l": "en-US","t": "Other"}, {"l": "it-IT","t": "Altro"}]}]}
                visualizeText(rootView, contributionId, subQuestion);
            }
            if(subQuestionType.equals("m")) {
                //{"q": {"id": 1,"c": [],"t":"m","cnt": [{"or":1, "ty":"tiv", "p":[{"l":"en-US","t":"Welcome"},{"l":"it-IT","t":"Benvenuto"}]}, {"or":2, "ty":"tv", "p":[{"l":"en-US","t":"Hi, thank you for participating in the QROWDLab! The experiment starts on May 28 and ends on June 7. We might send you messages during this time. If you wish to reply, share your ideas or make any suggestion, please contact: trento.smart@comune.trento.it"},{"l":"it-IT","t":"Ciao, grazie per partecipare al QROWDLab! L'esperimento si svolge dal 28 maggio al 7 giugno. Durante questo periodo potremmo inviarti dei messaggi. Se desideri rispondere, condividere le tue impressioni o proporre dei suggerimenti scrivi a: trento.smart@comune.trento.it"}]}, {"or":3, "ty":"iv", "p":"http://qrowd-project.eu/wp-content/uploads/2017/12/1234-1.png"}]}}
                visualizeStaticMessage(subQuestion, contributionId);
            }
            if(subQuestionType.equals("l")) {
                //{"q": {"id": 1,"c": [],"t": "l","l": {"lat": 46.0661043,"lon": 11.121961},"p": [{"t": "We detected that on {trip.start_timestamp}, you stopped near the point on the map below, close to {trip.start_address}. Is this correct?","l": "en-US"},{"t": "We detected that on {trip.start_timestamp}, you stopped near the point on the map below, close to {trip.start_address}. Is this correct?","l": "it-IT"}]},"a": [{"id": 1,"c_id": -1,"p": [{"t": "Yes","l": "en-US"},{"t": "Si","l": "it-IT"}],"c": []},{"id": 2,"c_id": -1,"p": [{"t": "No, I was not there at that time","l": "en-US"},{"t": "No, I was not there at that time","l": "it-IT"}],"c": []},{"id": 3,"c_id": -1,"p": [{"t": "No, I was there but I did not start a trip at that time","l": "en-US"},{"t": "No, I was there but I did not start a trip at that time","l": "it-IT"}],"c": []},{"id": 4,"c_id": -1,"p": [{"t": "I don't remember","l": "en-US"},{"t": "Non mi ricordo","l": "it-IT"}],"c": []}]}
                visualizePointerMap(rootView, contributionId, subQuestion, savedInstanceState);
            }
            if(subQuestionType.equals("ms")) {
                //{"a": [{"c": [], "id": 1, "c_id": -1, "p": [{"t": "Yes", "l": "en-US"}, {"t": "S\u00ec", "l": "it-IT"}]}, {"c": [], "id": 2, "c_id": -1, "p": [{"t": "No", "l": "en-US"}, {"t": "No", "l": "it-IT"}]}, {"c": [], "id": 3, "c_id": -1, "p": [{"t": "I don't remember", "l": "en-US"}, {"t": "Non mi ricordo", "l": "it-IT"}]}], "q": {"c": [], "id": 1, "la": [23.43434, 43.54645], "t": "ms", "p": [{"t": "We detected that on 6/6 at around 9:09, you made a trip from Via Giovanni Battista Trener to Via Romano Guardini, arriving around 9:14. Is this correct?", "l": "en-US"}, {"t": "Abbiamo rilevato che il giorno 6/6 dalle 9:09 alle 9:14 hai effettuato uno spostamento da Via Giovanni Battista Trener a Via Romano Guardini. \u00c8 corretto?", "l": "it-IT"}], "lo": [23.43434, 43.54645]}}
                visualizePathMap(rootView, contributionId, subQuestion, savedInstanceState);
            }
            if(subQuestionType.equals("i")) {
                //{"a": [{"c": [],"id": 1,"c_id": -1,"p": [{"t": "Reached your destination","l": "en-US"}, {"t": "Hai raggiunto la tua destinazione","l": "it-IT"}]}, {"c": [],"id": 2,"c_id": -1,"p": [{"t": "Briefly stopped to change transportation mode","l": "en-US"}, {"t": "Ti sei fermato/a per cambiare mezzo di trasporto","l": "it-IT"}]}, {"c": [],"id": 3,"c_id": -1,"p": [{"t": "I don't remember","l": "en-US"}, {"t": "Non mi ricordo","l": "it-IT"}]}], "q": {"c": [], "id": 1, "t": "i", "l": "https://www.tesla.com/sites/default/files/images/software_update.jpg"}}
                visualizeImage(rootView, contributionId, subQuestion);
            }
            if(subQuestionType.equals("il")) {
                //{"a": [{"c": [],"id": 1,"c_id": -1,"p": [{"l": "en-US","t": "Type 1"}, {"l": "it-IT","t": "Type 1"}], "l": "https://www.tesla.com/sites/default/files/images/homepage/20180710/mx/homepage-modelx.jpg?20180712a"}, {"c": [],"id": 2,"c_id": -1,"p": [{"l": "en-US","t": "Type 2"}, {"l": "it-IT","t": "Type 2"}], "l": "https://insideevs.com/wp-content/uploads/2017/12/IMG_20171214_164551-e1529516907468.jpg"}, {"c": [],"id": 3,"c_id": -1,"p": [{"l": "en-US","t": "Type 3"}, {"l": "it-IT","t": "Type 3"}], "l": "https://insideevs.com/wp-content/uploads/2018/08/Tesla-Model-S-3-X-Semi.jpg"}], "q": {"id": 1,"c": [],"t": "il","p": [{"l": "en-US","t": "What kind of bike rack do you see?"}, {"l": "it-IT","t": "Che tipo di bike rack stai vedendo?"}]}}
                visualizeImageList(rootView, contributionId, subQuestion);
            }
            if(subQuestionType.equals("ap")) {
                //{"a": [], "q": {"c": [], "id": 1, "t": "ap", "p": []}}
                actionPicture();
            }
            if(subQuestionType.equals("al")) {
                //{"a": [{"c": [],"id": 1,"c_id": -1,"p": [{"t": "Reached your destination","l": "en-US"}, {"t": "Hai raggiunto la tua destinazione","l": "it-IT"}]}, {"c": [],"id": 2,"c_id": -1,"p": [{"t": "Briefly stopped to change transportation mode","l": "en-US"}, {"t": "Ti sei fermato/a per cambiare mezzo di trasporto","l": "it-IT"}]}, {"c": [],"id": 3,"c_id": -1,"p": [{"t": "I don't remember","l": "en-US"}, {"t": "Non mi ricordo","l": "it-IT"}]}],"q": {"c": [{"a": 1,"q": 1}],"id": 3,"t": "al","l": {"lon": 11.11938,"lat": 46.09143,"zoom": 17},"p": [{"t": "At Via Romano Guardini, you:","l": "en-US"}, {"t": "In Via Romano Guardini:","l": "it-IT"}]}}
                actionLocation(rootView, contributionId, subQuestion, savedInstanceState);
            }
            if(subQuestionType.equals("ac")) {
                //{"q": {"c": [], "id": 1, "t": "ac", "acc": 100.0, "p": []}
                actionCoordinates(rootView, contributionId, subQuestion, savedInstanceState);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get choices (list of answers) of the current subquestion based on the previous answers - TODO: check
     * @param choicesJSON Choices (list of answers) for the current subquestion
     * @param languageIndex Index of the language to be used, based on the locale of the device
     * @return {@link ArrayList} of {@link Choice} objects
     * @throws JSONException Exception thrown if the JSONArray is malformed
     */
    private ArrayList<Choice> getChoices(JSONArray choicesJSON, int languageIndex) throws JSONException {
        ArrayList<Choice> choices = new ArrayList<Choice>();

        for(int index=0; index<choicesJSON.length(); index++) {
            JSONObject choiceObject = (JSONObject) choicesJSON.get(index);
            if(constraintsSatisfied(choiceObject)) {
                choices.add(new Choice(choiceObject.getInt("id"), choiceObject.getInt("c_id"), ((JSONObject)choiceObject.getJSONArray("p").get(languageIndex)).getString("t")));
            }
        }
        return choices;
    }

    /**
     * Get choices (list of answers) of the current subquestion based on the previous answers - TODO: check
     * @param choicesJSON Choices (list of answers) for the current subquestion
     * @return {@link ArrayList} of {@link Choice} objects
     * @throws JSONException Exception thrown if the JSONArray is malformed
     */
    private ArrayList<Choice> getImageChoices(JSONArray choicesJSON) throws JSONException {
        ArrayList<Choice> choices = new ArrayList<Choice>();

        for(int index=0; index<choicesJSON.length(); index++) {
            JSONObject choiceObject = (JSONObject) choicesJSON.get(index);
            choices.add(new Choice(choiceObject.getInt("id"), choiceObject.getInt("c_id"), choiceObject.getString("l")));
        }
        return choices;
    }

    /**
     * Class used to download an image from a URL and visualize it in an {@link ImageView}
     */
    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        /**
         * Method that downloads the image from the URL
         * @param urls URL of the image to be downloaded
         * @return {@link Bitmap} of the downloaded image
         */
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        /**
         * Once the download is finished, it takes the {@link Bitmap} and sets it in the {@link ImageView}
         * @param result
         */
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    /**
     * Method used to take a picture and update the main button in the camera view
     * @param button {@link ImageButton} to be updated after the image has been taken
     */
    private void captureImage(ImageButton button) {
        camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        button.setImageResource(R.drawable.baseline_refresh_white_48);
    }

    /**
     * Method used to start the camer of the phone and show its content inside the View in the {@link #surfaceHolder}
     */
    public void startCamera() {
        try{
            camera = Camera.open();
        }catch(RuntimeException e){
            Log.e(tag, "init_camera: " + e);
            return;
        }

        try {
            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                camera.setDisplayOrientation(90);
            } else {
                camera.setDisplayOrientation(0);
            }

            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            Camera.Size optimalSize = getOptimalPreviewSize(sizes, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);

            //Get a median resolution for the phone
            List<Camera.Size> resolutions = parameters.getSupportedPictureSizes();
            Camera.Size mSize = parameters.getSupportedPictureSizes().get(resolutions.size() / 2);

            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            parameters.setJpegQuality(100);
            parameters.setPictureSize(mSize.width, mSize.height);
            camera.setParameters(parameters);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();

            isCameraPreviewStarted=true;
            ((ContributionActivity) getActivity()).buttonNextStatus(false);
        } catch (Exception e) {
            Log.e(tag, "init_camera: " + e);
            return;
        }
    }

    /**
     * Method used to stop the camera and release it. Once this is done, the method also enables the next button in the optionmenu.
     */
    private void stopCamera() {
        camera.stopPreview();
        camera.release();

        ((ContributionActivity) getActivity()).buttonNextStatus(true);
        isCameraPreviewStarted=false;
    }

    @Override
    public void onDestroy() {
        Log.d(this.getClass().getSimpleName(), "Destroy");
        super.onDestroy();
    }

    @Override
    public void onPause() {
        Log.d(this.getClass().getSimpleName(), "Pause");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(this.getClass().getSimpleName(), "Resume");
        super.onResume();
    }

    @Override
    public void onStop() {
        Log.d(this.getClass().getSimpleName(), "Stop");
        if(locationGPSListener!=null) {
            LocationManager locationManager = (LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(locationGPSListener);
        }
        super.onStop();
    }

    /**
     * Method that returns a boolean that defines if the constraints are satisfied for the current choice
     * @param choiceObject {@link JSONObject} containing the choices to be checked
     * @return True if the constraints are satisfied, false otherwise
     */
    private boolean constraintsSatisfied(JSONObject choiceObject) {
        boolean skip = true;
        try {
            HashMap<Integer, ArrayList<Integer>> constraints = ((ContributionActivity) getActivity()).convertConstraints(((ContributionActivity) getActivity()).convertJSONArrayToArrayList(choiceObject.getJSONArray("c")));

            Iterator it = constraints.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                ArrayList<Integer> orderedConstraints = (ArrayList<Integer>) pair.getValue();

                if (orderedConstraints.contains(((ContributionActivity) getActivity()).getAnsweridByQuestionid(((ContributionActivity) getActivity()).convertHasMapToArrayList(), (int) pair.getKey()))) {
                    skip = false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return skip;
    }

    /**
     * Method used to generate a {@link List} of {@link LatLng} objects from two {@link ArrayList} of coordinates, latitude and logitude
     * @param latitudes {@link ArrayList} of latitude points
     * @param longitudes {@link ArrayList} of longitude points
     * @return {@link List} of {@link LatLng} objects
     */
    private List<LatLng> generateLatLng(JSONArray latitudes, JSONArray longitudes) {
        List<LatLng> list = new ArrayList<LatLng>();
        for(int index=0; index<latitudes.length();index++) {
            LatLng latLng = null;
            try {
                latLng = new LatLng(latitudes.getDouble(index), longitudes.getDouble(index));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            list.add(latLng);
        }
        return list;
    }

    /**
     * Method called when the type of subquestion requires the user to place a pinpoint on the map
     * @param rootView {@link ViewGroup} element in which the elements of the view are
     * @param questionId Integer representing the id of the subquestion
     * @param subQuestion {@link JSONObject} element containing the subquestion and the elements to be visualized in the view
     * @param savedInstanceState {@link Bundle} element needed for the {@link MapView} in its {@link MapView#onCreate(Bundle)} method
     */
    private void actionLocation(ViewGroup rootView, final int questionId, JSONObject subQuestion, Bundle savedInstanceState) {
        try {
            /**
             * Create an object that allows to monitor the interactions of the user with the {@link GoogleMap} object
             */

            final MonitorableGoogleMap monitorableGoogleMap = new MonitorableGoogleMap(questionId, ((ContributionActivity) getActivity()));

            //Visualize the text of the subquestion above the map
            TextView myAwesomeTextView = (TextView) rootView.findViewById(R.id.subQuestion);
            myAwesomeTextView.setText(subQuestion.getJSONObject("q").getJSONArray("p").getJSONObject(((ContributionActivity)getActivity()).getLanguageIndex()).getString("t"));

            //Set the status of the next button to false since the user does not provide the input
            ((ContributionActivity) getActivity()).buttonNextStatus(false);

            final JSONObject subquestionCoordinates = subQuestion.getJSONObject("q").getJSONObject("l");

            //Initialize the {@link MapView}
            MapView mMapView = (MapView) rootView.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();

            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Asynchronous call to the map
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap mMap) {
                    /**
                     * Make the map monitorable. This class adds listeners to any action the user can perform on it:
                     * <ul>
                     *     <li>Zoom  in/out</li>
                     *     <li>Swipes</li>
                     *     <li>Clicks</li>
                     * </ul>
                     */
                    monitorableGoogleMap.setGoogleMap(mMap);

                    //When the map is loaded
                    mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            try {
                                LatLng location = new LatLng(subquestionCoordinates.getDouble("lat"), subquestionCoordinates.getDouble("lon"));
                                // For zooming automatically to the location of the marker
                                CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(subquestionCoordinates.getInt("zoom")).build();
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                /**
                                 * Initialize the {@link #monitorableGoogleMap}
                                 */
                                monitorableGoogleMap.setLastZoomValue((int)(mMap.getCameraPosition().zoom * 1000));
                                monitorableGoogleMap.setLastPosition(mMap.getCameraPosition().target);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {

                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(latLng));

                            try {
                                JSONObject position = new JSONObject();
                                position.put("lat", latLng.latitude);
                                position.put("long", latLng.longitude);

                                JSONObject payload = new JSONObject();
                                if(monitorableGoogleMap.getPayload().size()>0) {
                                    payload.put("mapstatistics", convertArrayListJSONArray(monitorableGoogleMap.getPayload()));
                                }
                                payload.put("data", position);

                                ((ContributionActivity) getActivity()).updateAnswer(questionId, -2, "s");
                                ((ContributionActivity) getActivity()).updatePayload(questionId, payload.toString());

                                ((ContributionActivity) getActivity()).buttonNextStatus(true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method called when the type of subquestion is a dynamic page with different text elements that are taken from the subquestion itself and appended to the main {@link LinearLayout}:
     * <ul>
     *     <li>tiv: {@link TextView} representing a title</li>
     *     <li>tv: {@link TextView} representing normal text</li>
     *     <li>iv: {@link ImageView}</li>
     * </ul>
     * Finally, since the user in this case is not required to perform any action, the next button is set to enabled (status = true)
     * @param subQuestion {@link JSONObject} element containing the subquestion and the elements to be visualized in the view
     * @param questionId Integer representing the id of the subquestion
     */
    private void visualizeStaticMessage(JSONObject subQuestion, int questionId) {

        try {
            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.linear);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            for(int index=0;index<subQuestion.getJSONObject("q").getJSONArray("cnt").length();index++) {
                //{"or":1, "ty":"tiv", "p":[{"l":"en-US","t":"This is a title TextView"},{"l":"it-IT","t":"Questa e una TextView per il titolo"}]}
                JSONObject singlePageContent = subQuestion.getJSONObject("q").getJSONArray("cnt").getJSONObject(index);
                String type = singlePageContent.getString("ty");
                if(type.equals("tiv")) {
                    linearLayout.addView(getDefaultTitle(iLogApplication.getLocalizedContent(singlePageContent.getJSONArray("p"))));
                }
                if(type.equals("tv")) {
                    linearLayout.addView(getDefaultTextview(iLogApplication.getLocalizedContent(singlePageContent.getJSONArray("p"))));
                }
                if(type.equals("iv")) {
                    ImageView imageView = getDefaultImage();
                    linearLayout.addView(imageView);
                    new DownloadImageTask(imageView).execute(singlePageContent.getString("p"));
                }
            }

            Log.d(this.toString(), "MENU STATUS");
            ((ContributionActivity) getActivity()).buttonNextStatus(true);

            ((ContributionActivity) getActivity()).updateAnswer(questionId, -2, "s");
            ((ContributionActivity) getActivity()).updatePayload(questionId, generateEmptyPayload());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to visualize a static text box at the top of the {@link ListView} containing the answers {@link Choice}
     * @param rootView {@link ViewGroup} element in which the elements of the view are
     * @param questionId Integer representing the id of the subquestion
     * @param subQuestion{@link JSONObject} element containing the subquestion and the elements to be visualized in the view
     */private void visualizeText(ViewGroup rootView, final int questionId, JSONObject subQuestion) {
        try {
            final String answerType = subQuestion.getJSONObject("q").getString("at");

            //Visualize the text of the subquestion above the map
            TextView myAwesomeTextView = (TextView) rootView.findViewById(R.id.subQuestion);
            myAwesomeTextView.setText(((JSONObject) subQuestion.getJSONObject("q").getJSONArray("p").get(((ContributionActivity)getActivity()).getLanguageIndex())).getString("t"));//+

            ((ContributionActivity) getActivity()).buttonNextStatus(false);

            /**
             * Populate the {@link ListView} with the choices (answers) for this specific subquestion
             */
            final ListView listView = (ListView) rootView.findViewById(R.id.answers);
            final ChoicesArrayAdapter adapter = new ChoicesArrayAdapter(getChoices(subQuestion.getJSONArray("a"), ((ContributionActivity)getActivity()).getLanguageIndex()));//from json
            listView.setAdapter(adapter);

            if(((ContributionActivity) getActivity()).isSubQuestionSingleSelection(answerType)) {
                //Single selection
                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            }
            else {
                //Multiple selection
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                listView.setItemsCanFocus(false);
            }

            ArrayList<Integer> answersIds = ((ContributionActivity) getActivity()).getAnswersBySubQuestionId(questionId);
            for(int index=0; index<answersIds.size(); index++) {
                if(answersIds.get(index) != -1) {
                    listView.setSelection(answersIds.get(index)-1);
                    listView.setItemChecked(answersIds.get(index)-1, true);
                    ((ContributionActivity) getActivity()).buttonNextStatus(true);
                }
            }

            /**
             * Method triggered when the user clicks one item in the {@link ListView}. Update the answer for this subquestion and ff this is the last question
             * (or any of the next don't have this constraint), set the text of the next button in the optionmenu to Finish, and enable the button.
             */
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                    view.setSelected(true);

                    Choice selectedChoice = (Choice) parent.getItemAtPosition(position);
                    ((ContributionActivity) getActivity()).updateAnswer(questionId, selectedChoice.getId(), answerType);
                    ((ContributionActivity) getActivity()).updatePayload(questionId, generateEmptyPayload());

                    //next have this answer as constraint
                    if(((ContributionActivity) getActivity()).anyNextHaveThisConstraint(questionId)) {
                        ((ContributionActivity) getActivity()).buttonNextSetText(R.string.next);
                    }
                    else {
                        ((ContributionActivity) getActivity()).buttonNextSetText(R.string.finish);
                    }

                    ((ContributionActivity) getActivity()).buttonNextStatus(true);
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to visualize a static text box at the top of the {@link ListView} containing the answers {@link Choice}
     * @param rootView {@link ViewGroup} element in which the elements of the view are
     * @param questionId Integer representing the id of the subquestion
     * @param subQuestion{@link JSONObject} element containing the subquestion and the elements to be visualized in the view
     */private void visualizeImageList(ViewGroup rootView, final int questionId, JSONObject subQuestion) {
        try {
            //Visualize the text of the subquestion above the map
            TextView myAwesomeTextView = (TextView) rootView.findViewById(R.id.subQuestion);
            myAwesomeTextView.setText(((JSONObject) subQuestion.getJSONObject("q").getJSONArray("p").get(((ContributionActivity)getActivity()).getLanguageIndex())).getString("t"));//+

            ((ContributionActivity) getActivity()).buttonNextStatus(false);

            /**
             * Populate the {@link ListView} with the choices (answers) for this specific subquestion
             */
            final ListView listView = (ListView) rootView.findViewById(R.id.answers);
            final ImageChoicesArrayAdapter adapter = new ImageChoicesArrayAdapter(getImageChoices(subQuestion.getJSONArray("a")));//from json
            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            //Load previously selected element
            ArrayList<Integer> answersIds = ((ContributionActivity) getActivity()).getAnswersBySubQuestionId(questionId);
            for(int index=0; index<answersIds.size(); index++) {
                if(answersIds.get(index) != -1) {
                    listView.setSelection(answersIds.get(index)-1);
                    listView.setItemChecked(answersIds.get(index)-1, true);
                    ((ContributionActivity) getActivity()).buttonNextStatus(true);
                }
            }

            /**
             * Method triggered when the user clicks one item in the {@link ListView}. Update the answer for this subquestion and ff this is the last question
             * (or any of the next don't have this constraint), set the text of the next button in the optionmenu to Finish, and enable the button.
             */
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                    view.setSelected(true);

                    Choice selectedChoice = (Choice) parent.getItemAtPosition(position);
                    ((ContributionActivity) getActivity()).updateAnswer(questionId, selectedChoice.getId(), "s");
                    ((ContributionActivity) getActivity()).updatePayload(questionId, generateEmptyPayload());

                    //next have this answer as constraint
                    if(((ContributionActivity) getActivity()).anyNextHaveThisConstraint(questionId)) {
                        ((ContributionActivity) getActivity()).buttonNextSetText(R.string.next);
                    }
                    else {
                        ((ContributionActivity) getActivity()).buttonNextSetText(R.string.finish);
                    }

                    ((ContributionActivity) getActivity()).buttonNextStatus(true);
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used when the user is asked to take a picture. It initializes the {@link ImageButton} and sets a listener on it. This listener allows to capture the image
     * if the preview is active or it allows to take another picture if one was already taken (and displayed).
     */
    public void actionPicture() {
        try {
            final ImageButton button = (ImageButton) rootView.findViewById(R.id.buttonShot);
            setHeigth(button, 200, 200);
            button.setImageResource(R.drawable.baseline_adjust_white_48);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isCameraPreviewStarted) {
                        captureImage(button);
                    }
                    else {
                        startCamera();
                        button.setImageResource(R.drawable.baseline_adjust_white_48);
                    }
                }
            });
            takePicture(rootView, contributionId, button);
        } catch(Exception e) {
            e.printStackTrace();
        }

        /**
         * If the {@link Manifest.permission.CAMERA} is not granted yet, ask to the user to grant it
         */
        if(!iLogApplication.hasSinglePermission(Manifest.permission.CAMERA)) {
            iLogApplication.requestSinglePermission(Manifest.permission.CAMERA, getActivity());
        }
    }

    /**
     * Method called when the type of subquestion requires the user to give an answer from a point on the map
     * @param rootView {@link ViewGroup} element in which the elements of the view are
     * @param questionId Integer representing the id of the subquestion
     * @param subQuestion {@link JSONObject} element containing the subquestion and the elements to be visualized in the view
     * @param savedInstanceState {@link Bundle} element needed for the {@link MapView} in its {@link MapView#onCreate(Bundle)} method
     */
    private void visualizePointerMap(ViewGroup rootView, final int questionId, JSONObject subQuestion, Bundle savedInstanceState) {
        try {
            final MonitorableGoogleMap monitorableGoogleMap = new MonitorableGoogleMap(questionId, ((ContributionActivity) getActivity()));

            //Visualize the text of the subquestion above the map
            TextView myAwesomeTextView = (TextView) rootView.findViewById(R.id.subQuestion);
            myAwesomeTextView.setText(((JSONObject) subQuestion.getJSONObject("q").getJSONArray("p").get(((ContributionActivity)getActivity()).getLanguageIndex())).getString("t"));

            final JSONObject subquestionCoordinates = subQuestion.getJSONObject("q").getJSONObject("l");

            //Initialize the {@link MapView}
            MapView mMapView = (MapView) rootView.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();

            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Asynchronous call to the map
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap mMap) {
                    /**
                     * Make the map monitorable. This class adds listeners to any action the user can perform on it:
                     * <ul>
                     *     <li>Zoom  in/out</li>
                     *     <li>Swipes</li>
                     *     <li>Clicks</li>
                     * </ul>
                     */
                    monitorableGoogleMap.setGoogleMap(mMap);

                    //When the map is loaded, load the pointer
                    mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            LatLng location = null;
                            try {
                                location = new LatLng(subquestionCoordinates.getDouble("lat"), subquestionCoordinates.getDouble("lon"));
                                mMap.addMarker(new MarkerOptions().position(location).title("Marker Title").snippet("Marker Description"));

                                //Update the camera on the current marker
                                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(location, 17f);
                                mMap.animateCamera(cu);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            /**
                             * Initialize the {@link #monitorableGoogleMap}
                             */
                            monitorableGoogleMap.setLastZoomValue((int)(mMap.getCameraPosition().zoom * 1000));
                            monitorableGoogleMap.setLastPosition(mMap.getCameraPosition().target);
                        }
                    });


                }
            });

            ((ContributionActivity) getActivity()).buttonNextStatus(false);

            /**
             * Populate the {@link ListView} with the choices (answers) for this specific subquestion
             */
            final ListView listView = (ListView) rootView.findViewById(R.id.answers);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            final ChoicesArrayAdapter adapter = new ChoicesArrayAdapter(getChoices(subQuestion.getJSONArray("a"), ((ContributionActivity)getActivity()).getLanguageIndex()));//from json
            listView.setAdapter(adapter);

            ArrayList<Integer> answersIds = ((ContributionActivity) getActivity()).getAnswersBySubQuestionId(questionId);
            for(int index=0; index<answersIds.size(); index++) {
                if(answersIds.get(index) != -1) {
                    listView.setSelection(answersIds.get(index)-1);
                    listView.setItemChecked(answersIds.get(index)-1, true);
                    ((ContributionActivity) getActivity()).buttonNextStatus(true);
                }
            }

            /**
             * Method triggered when the user clicks one item in the {@link ListView}. Update the answer for this subquestion and if this is the last question
             * (or any of the next don't have this constraint), set the text of the next button in the optionmenu to Finish, and enable the button. Add to the payload the
             * information collected by the {@link monitorableGoogleMap} obejct about how the user interacted with the {@link GoogleMap}
             */
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                    view.setSelected(true);

                    Choice selectedChoice = (Choice) parent.getItemAtPosition(position);
                    ((ContributionActivity) getActivity()).updateAnswer(questionId, selectedChoice.getId(),  "s");

                    if(((ContributionActivity) getActivity()).anyNextHaveThisConstraint(questionId)) {
                        ((ContributionActivity) getActivity()).buttonNextSetText(R.string.next);
                    }
                    else {
                        ((ContributionActivity) getActivity()).buttonNextSetText(R.string.finish);
                    }

                    ((ContributionActivity) getActivity()).buttonNextStatus(true);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method called when the type of subquestion requires the user to place a pinpoint on the map
     * @param rootView {@link ViewGroup} element in which the elements of the view are
     * @param questionId Integer representing the id of the subquestion
     * @param subQuestion {@link JSONObject} element containing the subquestion and the elements to be visualized in the view
     * @param savedInstanceState {@link Bundle} element needed for the {@link MapView} in its {@link MapView#onCreate(Bundle)} method
     */
    private void visualizePathMap(ViewGroup rootView, final int questionId, JSONObject subQuestion, Bundle savedInstanceState) {
        try {
            final MonitorableGoogleMap monitorableGoogleMap = new MonitorableGoogleMap(questionId, ((ContributionActivity) getActivity()));

            TextView myAwesomeTextView = (TextView) rootView.findViewById(R.id.subQuestion);
            myAwesomeTextView.setText(((JSONObject) subQuestion.getJSONObject("q").getJSONArray("p").get(((ContributionActivity)getActivity()).getLanguageIndex())).getString("t"));

            //{"q":{"id":1,"c":[],"t": "ms", "la":[], "lo": [], p":[{"l":"en-US","t":"Did you finish a trip at, or close, to this location at %s?"},{"l":"it-IT","t":"Hai terminato un trip a quella location alle %s?"}]},"a":[{"id":1,"c":[],"c_id":-1,"p":[{"l":"en-US","t":"Yes"},{"l":"it-IT","t":"Si"}]},{"id":2,"c":[],"c_id":-1,"p":[{"l":"en-US","t":"No"},{"l":"it-IT","t":"No"}]}]}
            final JSONArray subquestionCoordinatesLat = subQuestion.getJSONObject("q").getJSONArray("la");
            final JSONArray subquestionCoordinatesLong = subQuestion.getJSONObject("q").getJSONArray("lo");

            //Initialize the {@link MapView}
            MapView mMapView = (MapView) rootView.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();

            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Asynchronous call to the map
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap mMap) {
                    /**
                     * Make the map monitorable. This class adds listeners to any action the user can perform on it:
                     * <ul>
                     *     <li>Zoom  in/out</li>
                     *     <li>Swipes</li>
                     *     <li>Clicks</li>
                     * </ul>
                     */
                    monitorableGoogleMap.setGoogleMap(mMap);

                    //When the map is loaded, load the path
                    mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .clickable(true)
                                    .color(Color.RED)
                                    .width(5);
                            List<LatLng> list = generateLatLng(subquestionCoordinatesLat, subquestionCoordinatesLong);
                            for(int index=0; index<list.size();index++) {
                                polylineOptions.add(list.get(index));
                            }
                            Polyline polyline = mMap.addPolyline(polylineOptions);
                            polyline.setTag("A");

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            for (LatLng latLng : list) {
                                builder.include(latLng);
                            }

                            try {
                                final LatLngBounds bounds = builder.build();

                                //BOUND_PADDING is an int to specify padding of bound.. try 100.
                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                                mMap.animateCamera(cu);

                                /**
                                 * Initialize the {@link #monitorableGoogleMap}
                                 */
                                monitorableGoogleMap.setLastZoomValue((int)(mMap.getCameraPosition().zoom * 1000));
                                monitorableGoogleMap.setLastPosition(mMap.getCameraPosition().target);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            ((ContributionActivity) getActivity()).buttonNextStatus(false);

            /**
             * Populate the {@link ListView} with the choices (answers) for this specific subquestion
             */
            final ListView listView = (ListView) rootView.findViewById(R.id.answers);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            final ChoicesArrayAdapter adapter = new ChoicesArrayAdapter(getChoices(subQuestion.getJSONArray("a"), ((ContributionActivity)getActivity()).getLanguageIndex()));//from json
            listView.setAdapter(adapter);

            ArrayList<Integer> answersIds = ((ContributionActivity) getActivity()).getAnswersBySubQuestionId(questionId);
            for(int index=0; index<answersIds.size(); index++) {
                if(answersIds.get(index) != -1) {
                    listView.setSelection(answersIds.get(index)-1);
                    listView.setItemChecked(answersIds.get(index)-1, true);

                    if(((ContributionActivity) getActivity()).anyNextHaveThisConstraint(questionId)) {
                        ((ContributionActivity) getActivity()).buttonNextSetText(R.string.next);
                    }
                    else {
                        ((ContributionActivity) getActivity()).buttonNextSetText(R.string.finish);
                    }

                    ((ContributionActivity) getActivity()).buttonNextStatus(true);
                }
            }

            /**
             * Method triggered when the user clicks one item in the {@link ListView}. Update the answer for this subquestion and ff this is the last question
             * (or any of the next don't have this constraint), set the text of the next button in the optionmenu to Finish, and enable the button.
             */
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                    view.setSelected(true);

                    Choice selectedChoice = (Choice) parent.getItemAtPosition(position);
                    ((ContributionActivity) getActivity()).updateAnswer(questionId, selectedChoice.getId(), "s");

                    if(((ContributionActivity) getActivity()).anyNextHaveThisConstraint(questionId)) {
                        ((ContributionActivity) getActivity()).buttonNextSetText(R.string.next);
                    }
                    else {
                        ((ContributionActivity) getActivity()).buttonNextSetText(R.string.finish);
                    }

                    ((ContributionActivity) getActivity()).buttonNextStatus(true);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method called when the type of subquestion shows an image downloaded from an URL to the user
     * @param rootView {@link ViewGroup} element in which the elements of the view are
     * @param questionId Integer representing the id of the subquestion
     * @param subQuestion {@link JSONObject} element containing the subquestion and the elements to be visualized in the view
     */
    private void visualizeImage(ViewGroup rootView, final int questionId, JSONObject subQuestion) {
        try {

            /**
             * Download the image and populate the {@link ImageView}
             */
            new DownloadImageTask((ImageView) rootView.findViewById(R.id.imageView)).execute(subQuestion.getJSONObject("q").getString("l"));

            ((ContributionActivity) getActivity()).buttonNextStatus(false);

            /**
             * Populate the {@link ListView} with the choices (answers) for this specific subquestion
             */
            final ListView listView = (ListView) rootView.findViewById(R.id.answers);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            final ChoicesArrayAdapter adapter = new ChoicesArrayAdapter(getChoices(subQuestion.getJSONArray("a"), ((ContributionActivity)getActivity()).getLanguageIndex()));//from json
            listView.setAdapter(adapter);

            ArrayList<Integer> answersIds = ((ContributionActivity) getActivity()).getAnswersBySubQuestionId(questionId);
            for(int index=0; index<answersIds.size(); index++) {
                if(answersIds.get(index) != -1) {
                    listView.setSelection(answersIds.get(index)-1);
                    listView.setItemChecked(answersIds.get(index)-1, true);
                    ((ContributionActivity) getActivity()).buttonNextStatus(true);
                }
            }

            /**
             * Method triggered when the user clicks one item in the {@link ListView}. Update the answer for this subquestion and ff this is the last question
             * (or any of the next don't have this constraint), set the text of the next button in the optionmenu to Finish, and enable the button.
             */
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {

                    view.setSelected(true);

                    Choice selectedChoice = (Choice) parent.getItemAtPosition(position);
                    ((ContributionActivity) getActivity()).updateAnswer(questionId, selectedChoice.getId(), "s");
                    ((ContributionActivity) getActivity()).updatePayload(questionId, generateEmptyPayload());
                    ((ContributionActivity) getActivity()).buttonNextStatus(true);
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the height/width of the View
     * @param view {@link ImageButton} to be resized
     * @param height integer representing the pixels of the height
     * @param width integer representing the pixels of the width
     */
    private void setHeigth(ImageButton view, int height, int width) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        params.width = width;
        view.setLayoutParams(params);
    }

    /**
     * Method that sets up all the necessary elements to allow the user to take a picture. Among others, it initilizes the callbacks to detect the picture event and save it
     * in the payload of the answer.
     * @param rootView {@link ViewGroup} element in which the elements of the view are
     * @param questionId Integer representing the id of the subquestion
     * @param button {@link ImageButton} that the user presses to take the picture (or go back to shooting mode)
     */
    private void takePicture(View rootView, final int questionId, final ImageButton button) {
        /**
         * Initialize the {@link SurfaceView}
         */
        surfaceView = (SurfaceView) rootView.findViewById(R.id.surfaceView);
        /**
         * Set a OnTouchListener on it with {@link SurfaceView#setOnTouchListener(View.OnTouchListener)}. When the pinches in/out the camera zoomes in/out
         */
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if (event.getPointerCount() > 1) {
                    // handle multi-touch events
                    try {
                        Camera.Parameters params = camera.getParameters();
                        if (action == MotionEvent.ACTION_POINTER_DOWN) {
                            mDist = getFingerSpacing(event);
                        } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                            camera.cancelAutoFocus();
                            handleZoom(event, params);
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {}
            public void surfaceCreated(SurfaceHolder holder) {
                startCamera();
                button.setImageResource(R.drawable.baseline_adjust_white_48);
            }
            public void surfaceDestroyed(SurfaceHolder holder) {}
        });
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //Callback called when a raw image is captured, we ignore it
        rawCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d("Log", "onPictureTaken - raw");
            }
        };

        shutterCallback = new Camera.ShutterCallback() {
            public void onShutter() {
                Log.i("Log", "onShutter'd");
            }
        };

        /**
         * Handles data for jpeg picture. When a picture is taken, stop the camera, check if the user granted location permissions, if yes take the user location and put it with the
         * metadata of the image. Finally generate an answer and a payload with the image and the metadata.
         */
        jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(final byte[] data, Camera camera) {
                stopCamera();

                if (ActivityCompat.checkSelfPermission(((ContributionActivity) getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(((ContributionActivity) getActivity()), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(((ContributionActivity) getActivity()), new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        JSONObject answer = new JSONObject();

                                        //Save the picture to memory because cannot fit in the database, put uri in the db
                                        try {
                                            String filename = generateFilename(Utils.generateSalt());
                                            FileOutputStream fos = iLogApplication.getAppContext().openFileOutput(filename, 0);
                                            fos.write(data);
                                            fos.close();

                                            answer.put("picture", filename);
                                            answer.put("metadata", new JSONObject().put("lat", location.getLatitude()).put("long", location.getLongitude()).put("timestamp", Utils.longToStringFormat(location.getTime())));
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (NoSuchAlgorithmException e) {
                                            e.printStackTrace();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        ((ContributionActivity) getActivity()).updateAnswer(questionId, -2, "s");
                                        ((ContributionActivity) getActivity()).updatePayload(questionId, answer.toString());
                                    } else {
                                        JSONObject answer = new JSONObject();
                                        try {
                                            answer.put("picture", Base64.encodeToString(data, Base64.NO_WRAP));
                                            answer.put("metadata", "Location is null");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        ((ContributionActivity) getActivity()).updateAnswer(questionId, -2, "s");
                                        ((ContributionActivity) getActivity()).updatePayload(questionId, answer.toString());
                                    }
                                }
                            });
                }
                else {
                    JSONObject answer = new JSONObject();
                    try {
                        answer.put("picture", Base64.encodeToString(data, Base64.NO_WRAP));
                        answer.put("metadata", "No location permissions.");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ((ContributionActivity) getActivity()).updateAnswer(questionId, -2, "s");
                    ((ContributionActivity) getActivity()).updatePayload(questionId, answer.toString());
                }

                if(((ContributionActivity) getActivity()).anyNextHaveThisConstraint(questionId)) {
                    ((ContributionActivity) getActivity()).buttonNextSetText(R.string.next);
                }
                else {
                    ((ContributionActivity) getActivity()).buttonNextSetText(R.string.finish);
                }

                ((ContributionActivity) getActivity()).buttonNextStatus(true);
            }
        };
    }

    /**
     * Method called when the type of subquestion requires the user to take the current location
     * @param rootView {@link ViewGroup} element in which the elements of the view are
     * @param questionId Integer representing the id of the subquestion
     * @param subQuestion {@link JSONObject} element containing the subquestion and the elements to be visualized in the view
     * @param savedInstanceState {@link Bundle} element needed for the {@link MapView} in its {@link MapView#onCreate(Bundle)} method
     */
    private void actionCoordinates(final ViewGroup rootView, final int questionId, final JSONObject subQuestion, Bundle savedInstanceState) {
        //
        try {
            /**
             * Create an object that allows to monitor the interactions of the user with the {@link GoogleMap} object
             */
            final MonitorableGoogleMap monitorableGoogleMap = new MonitorableGoogleMap(questionId, ((ContributionActivity) getActivity()));

            //Initialize the {@link MapView} and resize it to half of the screen height
            MapView mMapView = (MapView) rootView.findViewById(R.id.mapView);
            ViewGroup.LayoutParams params = mMapView.getLayoutParams();
            params.height = getAvailableScreenHeight() / 2;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            mMapView.setLayoutParams(params);
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();

            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            final TextView loadingMessage = (TextView) rootView.findViewById(R.id.loadingTextView);
            final ProgressBar progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

            //Asynchronous call to the map
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap mMap) {
                    /**
                     * Make the map monitorable. This class adds listeners to any action the user can perform on it:
                     * <ul>
                     *     <li>Zoom  in/out</li>
                     *     <li>Swipes</li>
                     *     <li>Clicks</li>
                     * </ul>
                     */
                    monitorableGoogleMap.setGoogleMap(mMap);

                    //TODO - Check permissions

                    //Initialize the TextView used as button to reload the user location
                    final TextView reloadMessage = (TextView) rootView.findViewById(R.id.reloadTextView);
                    reloadMessage.setText(getResources().getString(R.string.reload_message));
                    reloadMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestLocationupdates(monitorableGoogleMap, getAccuracy(subQuestion), progressBar, loadingMessage, reloadMessage, questionId);
                        }
                    });

                    //TODO - Check permissions
                    requestLocationupdates(monitorableGoogleMap, getAccuracy(subQuestion), progressBar, loadingMessage, reloadMessage, questionId);

                    //Check for the initial position to center the camera on the position of the user in the map
                    if (ActivityCompat.checkSelfPermission(((ContributionActivity) getActivity()), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(((ContributionActivity) getActivity()), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(((ContributionActivity) getActivity()), new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            LatLng locationElement = new LatLng(location.getLatitude(), location.getLongitude());
                                            CameraPosition cameraPosition = new CameraPosition.Builder().target(locationElement).zoom(17).build();
                                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                        }
                                    }
                                });
                    }

                    //When the map is loaded
                    mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            try {
                                /**
                                 * Initialize the {@link #monitorableGoogleMap}
                                 */
                                monitorableGoogleMap.setLastZoomValue((int)(mMap.getCameraPosition().zoom * 1000));
                                monitorableGoogleMap.setLastPosition(mMap.getCameraPosition().target);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that handles the zoom of the camera when taking pictures
     * @param event {@Link MotionEvent} object
     * @param params Cemra parameters
     */
    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom=zoom+5;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom=zoom-5;
        }
        mDist = newDist;
        params.setZoom(zoom);
        camera.setParameters(params);
    }

    /**
     * Determine the space between the first two fingers
     * @param event {@link MotionEvent}
     * @return a float value representing the spacing between the fingers
     */
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Method that returns the optimal preview size for the camera
     * @param sizes {@link List} of {@link Camera.Size} objects
     * @param w integer representing the width in pixels
     * @param h integer representing the height in pixels
     * @return {@link Camera.Size} object
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w/h;

        if (sizes==null) return null;

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Find size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /**
     * Method that returns a default textview with a specific content
     * @param content The String to be put in the {@link TextView} using {@link TextView#setText(int)}
     * @return {@link TextView} object with the default characteristics, padding, size, color, among others
     */
    private TextView getDefaultTextview(String content) {
        TextView textView = new TextView(iLogApplication.getAppContext());
        textView.setId(View.generateViewId());
        textView.setText(content);
        textView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        textView.setPadding(20, 20, 20, 20);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        textView.setTextColor(Color.DKGRAY);
        textView.setLineSpacing(0, 1.0f);
        return textView;
    }

    /**
     * Method that returns a default title textview with a specific content
     * @param content The String to be put in the {@link TextView} using {@link TextView#setText(int)}
     * @return {@link TextView} object with the default characteristics, padding, size, color, among others
     */
    private TextView getDefaultTitle(String content) {
        TextView textView = new TextView(iLogApplication.getAppContext());
        textView.setId(View.generateViewId());
        textView.setText(content);
        textView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        textView.setPadding(20, 20, 20, 20);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        textView.setLineSpacing(0, 1.0f);
        textView.setTextColor(Color.DKGRAY);
        textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }

    /**
     * Method that returns a default imageview
     * @return {@link ImageView} object with the default characteristics, padding and size
     */
    private ImageView getDefaultImage() {
        ImageView imageView = new ImageView(iLogApplication.getAppContext());
        imageView.setPadding(20, 20, 20, 20);
        final ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        imageView.setLayoutParams(lp);
        return imageView;
    }

    /**
     * Method that returns the smartphone screen height
     * @param activity {@link Activity} class
     * @return integer representing the height, in pixels
     */
    public static int getScreenHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    /**
     * Method that returns the height of the actionbar
     * @param context {@link Context} class
     * @return Integer representing the height, in pixels, of the actionbar
     */
    public static int getActionBarHeight(Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return 0;
    }

    /**
     * Method that returns the height of the status bar
     * @param context {@link Context} class
     * @return Integer representing the height, in pixels, of the status bar
     */
    public int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Method that returns the height of the available screen, which is, the total height {@link #getScreenHeight(Activity)} minus {@link #getStatusBarHeight(Context)}
     * and {@link #getActionBarHeight(Context)}.
     * @return Integer representing the height of the available screen
     */
    public int getAvailableScreenHeight() {
        Activity activity = (ContributionActivity) getActivity();
        return (getScreenHeight(activity) - getStatusBarHeight(activity) - getActionBarHeight(activity));
    }

    /**
     * Method used to request location updates.
     * @param mMap {@link MonitorableGoogleMap} object used as the main object of the view
     * @param accuracyThreshold Double value representing the accuracy below which the location detection is considered over
     * @param progressBar {@link ProgressBar} object that spins when the application is looking for an accurate position
     * @param loadingTextView {@link TextView} showing the message when the position is loading
     * @param reloadMessage {@link TextView} used as a button to reload the user position, if needed
     * @param questionId Integer representing the id of the question
     */
    private void requestLocationupdates(MonitorableGoogleMap mMap, double accuracyThreshold, ProgressBar progressBar, TextView loadingTextView, TextView reloadMessage, int questionId) {
        /**
         * It sets some graphical elements {@link ProgressBar} and two {@link TextView}
         */
        progressBar.setVisibility(View.VISIBLE);
        loadingTextView.setText(getResources().getString(R.string.loading_map_challenge));
        reloadMessage.setVisibility(View.GONE);

        mMap.getGoogleMap().clear();
        mMap.getGoogleMap().setMyLocationEnabled(true);

        //Set the status of the next button to false till the user does not provide the input
        ((ContributionActivity) getActivity()).buttonNextStatus(false);

        //TODO - Check permissions
        locationGPSListener = new LocationGPSListener(mMap, accuracyThreshold, progressBar, loadingTextView, reloadMessage, questionId);
        LocationManager locationManager = (LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationGPSListener);
    }

    /**
     * Methos that returns the accuracy threshold from the original subquestion
     * @param subQuestion {@link JSONObject} representing the subquestion
     * @return Double representing the accuracy, expressed in meters
     */
    private double getAccuracy(JSONObject subQuestion) {
        try {
            System.out.println("WHAT: "+subQuestion.getJSONObject("q").getDouble("acc"));
            return subQuestion.getJSONObject("q").getDouble("acc");
        }
        catch (Exception e) {
            return 100.0; //TODO - change to 10.0
        }
    }

    /**
     * Method that generates an empty payload String
     * @return String containing an empty payload
     */
    public static String generateEmptyPayload() {
        return new JSONObject().toString();
    }

    /**
     * Methos that converts an {@link ArrayList} to a {@link JSONArray}
     * @param array {@link ArrayList} to be converted
     * @return {@link JSONArray to be returned}
     */
    private JSONArray convertArrayListJSONArray(ArrayList<JSONObject> array) {
        JSONArray jsonarray = new JSONArray();
        for(JSONObject object: array) {
            jsonarray.put(object);
        }
        return jsonarray;
    }

    private String generateFilename(String salt) {
        return salt+".jpeg";
    }

    /**
     * {@link LocationListener} used when a new location is generated by the operating system
     */
    public class LocationGPSListener implements LocationListener {

        MonitorableGoogleMap mMap;
        double accuracyThreshold;
        ProgressBar progressBar;
        TextView loadingTextView;
        TextView reloadMessage;
        int questionId;

        public LocationGPSListener(MonitorableGoogleMap map, double accuracyThreshold, ProgressBar progressBar, TextView loadingTextView, TextView reloadMessage, int questionId) {
            this.mMap = map;
            this.accuracyThreshold = accuracyThreshold;
            this.progressBar = progressBar;
            this.loadingTextView = loadingTextView;
            this.reloadMessage = reloadMessage;
            this.questionId = questionId;
        }
        /**
         * Method triggered when the location from the GPS provider is available.
         * @param location {@link Location} object
         */
        @Override
        public void onLocationChanged(Location location) {

            //When the accuracy is accurate enough, stop the spinner and enable the button so that the user can continue
            if(location.getAccuracy() < accuracyThreshold) {

                /**
                 * When the location is received we need to save it, but we need also to keep the data from the {@link #mMap} using {@link mMap#getPayload()}
                 */
                try {
                    JSONObject position = new JSONObject();
                    position.put("lat", location.getLatitude());
                    position.put("long", location.getLongitude());

                    JSONObject payload = new JSONObject();
                    if(mMap.getPayload().size()>0) {
                        payload.put("mapstatistics", convertArrayListJSONArray(mMap.getPayload()));
                    }
                    payload.put("data", position);

                    ((ContributionActivity) getActivity()).updateAnswer(questionId, -2, "s");
                    ((ContributionActivity) getActivity()).updatePayload(questionId, payload.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                LatLng locationElement = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder().target(locationElement).zoom(17).build();
                mMap.getGoogleMap().animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                mMap.getGoogleMap().setMyLocationEnabled(false);
                mMap.getGoogleMap().addMarker(new MarkerOptions().position(locationElement));

                //Update the status of the next button and make it available
                ((ContributionActivity) getActivity()).buttonNextStatus(true);

                //Hide spinner
                progressBar.setVisibility(View.GONE);
                //Visualize the reload button
                reloadMessage.setVisibility(View.VISIBLE);
                //Show text message
                loadingTextView.setText(getResources().getString(R.string.loaded_map_challenge));
                //Stop location updates
                LocationManager locationManager = (LocationManager) iLogApplication.getAppContext().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                locationManager.removeUpdates(locationGPSListener);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }
}

/*
t
[{"q": {"id": 1,"c": [],"at": "s","t": "t","p": [{"l": "en-US","t": "What are you doing?"}, {"l": "it-IT","t": "Cosa stai facendo?"}]},"a": [{"id": 1,"c": [],"c_id": 74549,"p": [{"l": "en-US","t": "Sleeping"}, {"l": "it-IT","t": "Dormire"}]}, {"id": 2,"c": [],"c_id": 31428,"p": [{"l": "en-US","t": "Study"}, {"l": "it-IT","t": "Studio"}]}, {"id": 3,"c": [],"c_id": 4581,"p": [{"l": "en-US","t": "Lesson"}, {"l": "it-IT","t": "Lezione"}]}, {"id": 4,"c": [],"c_id": 1501,"p": [{"l": "en-US","t": "En route"}, {"l": "it-IT","t": "In viaggio/spostamento da...a..."}]}, {"id": 5,"c": [],"c_id": 4317,"p": [{"l": "en-US","t": "Eating"}, {"l": "it-IT","t": "Mangiare"}]}, {"id": 6,"c": [],"c_id": 3405,"p": [{"l": "en-US","t": "Selfcare"}, {"l": "it-IT","t": "Cura della persona"}]}, {"id": 7,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Coffee break, cigarette, beer, etc."}, {"l": "it-IT","t": "Pausa caffè, sigaretta, birra ecc"}]}, {"id": 8,"c": [],"c_id": 25864,"p": [{"l": "en-US","t": "Social life"}, {"l": "it-IT","t": "Vita Sociale/divertimento"}]}, {"id": 9,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Al the phone; in chat WhatsApp"}, {"l": "it-IT","t": "Al telefono; in chat WhatsApp"}]}, {"id": 10,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Watcing Youtube, Tv-shows, etc."}, {"l": "it-IT","t": "Guardo Youtube, Serie-Tv, ecc."}]}, {"id": 11,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Social media (Facebook, Instagram, etc.)"}, {"l": "it-IT","t": "Social media (Facebook, Instagram, ecc.)"}]}, {"id": 12,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Movie Theater, Theater, Concert, Exhibit, ..."}, {"l": "it-IT","t": "Cinema, Teatro, Concerto, Mostra, ..."}]}, {"id": 13,"c": [],"c_id": 2681,"p": [{"l": "en-US","t": "Sport"}, {"l": "it-IT","t": "Sport/Attività fisica"}]}, {"id": 14,"c": [],"c_id": 387,"p": [{"l": "en-US","t": "Shopping"}, {"l": "it-IT","t": "Shopping/Fare la spesa"}]}, {"id": 15,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Housework"}, {"l": "it-IT","t": "Lavori domestici"}]}, {"id": 16,"c": [],"c_id": 4421,"p": [{"l": "en-US","t": "Rest/nap"}, {"l": "it-IT","t": "Riposo/Pennichella"}]}, {"id": 17,"c": [],"c_id": -1,"p": [{"l": "en-US","t": "Reading a book; listening to music"}, {"l": "it-IT","t": "Leggo un libro; ascolto musica"}]}, {"id": 18,"c": [],"c_id": 2206,"p": [{"l": "en-US","t": "Hobbies"}, {"l": "it-IT","t": "Altro Hobby/tempo libero"}]}, {"id": 19,"c": [],"c_id": 112289,"p": [{"l": "en-US","t": "Work"}, {"l": "it-IT","t": "Lavoro"}]}, {"id": 20,"c": [],"c_id": 93394,"p": [{"l": "en-US","t": "Other"}, {"l": "it-IT","t": "Altro"}]}]}]
m
[{"q": {"id": 1,"c": [],"t":"m","cnt": [{"or":1, "ty":"tiv", "p":[{"l":"en-US","t":"Welcome"},{"l":"it-IT","t":"Benvenuto"}]}, {"or":2, "ty":"tv", "p":[{"l":"en-US","t":"Hi, thank you for participating in the QROWDLab! The experiment starts on May 28 and ends on June 7. We might send you messages during this time. If you wish to reply, share your ideas or make any suggestion, please contact: trento.smart@comune.trento.it"},{"l":"it-IT","t":"Ciao, grazie per partecipare al QROWDLab! L'esperimento si svolge dal 28 maggio al 7 giugno. Durante questo periodo potremmo inviarti dei messaggi. Se desideri rispondere, condividere le tue impressioni o proporre dei suggerimenti scrivi a: trento.smart@comune.trento.it"}]}, {"or":3, "ty":"iv", "p":"http://qrowd-project.eu/wp-content/uploads/2017/12/1234-1.png"}]}}]
l
[{"q": {"id": 1,"c": [],"t": "l","l": {"lat": 46.0661043,"lon": 11.121961},"p": [{"t": "We detected that on {trip.start_timestamp}, you stopped near the point on the map below, close to {trip.start_address}. Is this correct?","l": "en-US"},{"t": "We detected that on {trip.start_timestamp}, you stopped near the point on the map below, close to {trip.start_address}. Is this correct?","l": "it-IT"}]},"a": [{"id": 1,"c_id": -1,"p": [{"t": "Yes","l": "en-US"},{"t": "Si","l": "it-IT"}],"c": []},{"id": 2,"c_id": -1,"p": [{"t": "No, I was not there at that time","l": "en-US"},{"t": "No, I was not there at that time","l": "it-IT"}],"c": []},{"id": 3,"c_id": -1,"p": [{"t": "No, I was there but I did not start a trip at that time","l": "en-US"},{"t": "No, I was there but I did not start a trip at that time","l": "it-IT"}],"c": []},{"id": 4,"c_id": -1,"p": [{"t": "I don't remember","l": "en-US"},{"t": "Non mi ricordo","l": "it-IT"}],"c": []}]}]
i
[{"a": [{"c": [],"id": 1,"c_id": -1,"p": [{"t": "Reached your destination","l": "en-US"}, {"t": "Hai raggiunto la tua destinazione","l": "it-IT"}]}, {"c": [],"id": 2,"c_id": -1,"p": [{"t": "Briefly stopped to change transportation mode","l": "en-US"}, {"t": "Ti sei fermato/a per cambiare mezzo di trasporto","l": "it-IT"}]}, {"c": [],"id": 3,"c_id": -1,"p": [{"t": "I don't remember","l": "en-US"}, {"t": "Non mi ricordo","l": "it-IT"}]}], "q": {"c": [], "id": 1, "t": "i", "l": "https://www.tesla.com/sites/default/files/images/software_update.jpg"}}]
ms
[{"a": [{"c": [], "id": 1, "c_id": -1, "p": [{"t": "Yes", "l": "en-US"}, {"t": "S\u00ec", "l": "it-IT"}]}, {"c": [], "id": 2, "c_id": -1, "p": [{"t": "No", "l": "en-US"}, {"t": "No", "l": "it-IT"}]}, {"c": [], "id": 3, "c_id": -1, "p": [{"t": "I don't remember", "l": "en-US"}, {"t": "Non mi ricordo", "l": "it-IT"}]}], "q": {"c": [], "id": 1, "la": [23.43434, 43.54645], "t": "ms", "p": [{"t": "We detected that on 6/6 at around 9:09, you made a trip from Via Giovanni Battista Trener to Via Romano Guardini, arriving around 9:14. Is this correct?", "l": "en-US"}, {"t": "Abbiamo rilevato che il giorno 6/6 dalle 9:09 alle 9:14 hai effettuato uno spostamento da Via Giovanni Battista Trener a Via Romano Guardini. \u00c8 corretto?", "l": "it-IT"}], "lo": [23.43434, 43.54645]}}]
al
[{"a": [{"c": [],"id": 1,"c_id": -1,"p": [{"t": "Reached your destination","l": "en-US"}, {"t": "Hai raggiunto la tua destinazione","l": "it-IT"}]}, {"c": [],"id": 2,"c_id": -1,"p": [{"t": "Briefly stopped to change transportation mode","l": "en-US"}, {"t": "Ti sei fermato/a per cambiare mezzo di trasporto","l": "it-IT"}]}, {"c": [],"id": 3,"c_id": -1,"p": [{"t": "I don't remember","l": "en-US"}, {"t": "Non mi ricordo","l": "it-IT"}]}],"q": {"c": [{"a": 1,"q": 1}],"id": 3,"t": "al","l": {"lon": 11.11938,"lat": 46.09143,"zoom": 17},"p": [{"t": "At Via Romano Guardini, you:","l": "en-US"}, {"t": "In Via Romano Guardini:","l": "it-IT"}]}}]
ap
[{"a": [], "q": {"c": [], "id": 1, "t": "ap", "p": []}}]
ac
[{"q": {"c": [], "id": 1, "t": "ac", "acc": 100.0, "p": []}]
XX
[{"a": [{"c": [],"id": 1,"c_id": -1,"p": [{"t": "Reached your destination","l": "en-US"}, {"t": "Hai raggiunto la tua destinazione","l": "it-IT"}]}, {"c": [],"id": 2,"c_id": -1,"p": [{"t": "Briefly stopped to change transportation mode","l": "en-US"}, {"t": "Ti sei fermato/a per cambiare mezzo di trasporto","l": "it-IT"}]}, {"c": [],"id": 3,"c_id": -1,"p": [{"t": "I don't remember","l": "en-US"}, {"t": "Non mi ricordo","l": "it-IT"}]}],"q": {"c": [{"a": 1,"q": 1}],"id": 3,"t": "al","l": {"lon": 11.11938,"lat": 46.09143,"zoom": 17},"p": [{"t": "Please pinpoint on the map where you changed transportation mode:","l": "en-US"}, {"t": "Please pinpoint on the map where you changed transportation mode:","l": "it-IT"}]}}]
 */