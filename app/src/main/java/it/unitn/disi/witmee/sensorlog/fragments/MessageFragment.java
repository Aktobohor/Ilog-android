package it.unitn.disi.witmee.sensorlog.fragments;

/**
 * Created by mattiazeni on 5/23/17.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.activities.MessageActivity;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Class that extends {@link Fragment} and is intended to display the content to be presented to the user and to collect his feedback with {@link it.unitn.disi.witmee.sensorlog.model.Message}.
 */
public class MessageFragment extends Fragment {

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

        ViewGroup rootView = null;

        try {
            final JSONObject subQuestion = new JSONObject(String.valueOf(getArguments().getString("selectedfragment")));

            final int questionId = subQuestion.getInt("id");
            String subQuestionType = subQuestion.getString("ty");
            JSONArray content =  subQuestion.getJSONArray("cnt");
            System.out.println("SIZE: "+ questionId + " " +((MessageActivity) getActivity()).fragments.size());

            if(questionId == 1 && ((MessageActivity) getActivity()).fragments.size() == 1) {
                System.out.println("1 1");
                ((MessageActivity) getActivity()).buttonPreviousStatus(false);
                ((MessageActivity) getActivity()).buttonNextStatus(false);

            }
            else if (questionId == 1 && ((MessageActivity) getActivity()).fragments.size() != 1) {
                System.out.println("1 !1");
                ((MessageActivity) getActivity()).buttonPreviousStatus(false);
                ((MessageActivity) getActivity()).buttonNextStatus(true);
            }
            else if (questionId == ((MessageActivity) getActivity()).fragments.size() && questionId != 1) {
                System.out.println("= !1");
                ((MessageActivity) getActivity()).buttonPreviousStatus(true);
                ((MessageActivity) getActivity()).buttonNextStatus(false);
            }
            else if (questionId != ((MessageActivity) getActivity()).fragments.size() && questionId != 1) {
                System.out.println("!1 !1");
                ((MessageActivity) getActivity()).buttonPreviousStatus(true);
                ((MessageActivity) getActivity()).buttonNextStatus(true);
            }

            //Dynamic text message
            if(subQuestionType.equals("t")) {
                rootView = (ViewGroup) inflater.inflate(R.layout.linear_layout, container, false);
                LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.linear);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                for(int index=0;index<content.length();index++) {
                    //{"or":1, "ty":"tiv", "p":[{"l":"en-US","t":"This is a title TextView"},{"l":"it-IT","t":"Questa e una TextView per il titolo"}]}
                    JSONObject singlePageContent = content.getJSONObject(index);
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
            }
            if(subQuestionType.equals("ht")) {

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    /**
     * Class used to download an image from a URL and visualize it in an {@link ImageView}
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

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

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
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
        super.onStop();
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
}

