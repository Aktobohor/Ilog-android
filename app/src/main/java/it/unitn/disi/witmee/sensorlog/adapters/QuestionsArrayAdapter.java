package it.unitn.disi.witmee.sensorlog.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.Answer;
import it.unitn.disi.witmee.sensorlog.model.Question;

/**
 * Class that extends {@link ArrayAdapter} and is used to visualize {@link Question} in the {@link android.widget.ListView} in the project
 */
public class QuestionsArrayAdapter extends ArrayAdapter<Question> {

    LayoutInflater lInflater;

    public QuestionsArrayAdapter() {
        super(iLogApplication.getAppContext(), R.layout.question, new ArrayList<Question>()); //iLogApplication.questionnaire.getQuestions()
        lInflater = (LayoutInflater) iLogApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Method that visualizes the element. In this case, we visualize two {@link TextView}, one with the {@link Question#content} and one with the {@link Question#instanceTime}
     * @param position default value
     * @param convertView default value
     * @param parent default value
     * @return default {@link View}
     */
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.question, parent, false);
        }

        Question question = getItem(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
        String endingTime = dateFormat.format(new Date(question.getInstanceTime()));

        ((TextView) view.findViewById(R.id.questionTime)).setText(iLogApplication.getAppContext().getResources().getString(R.string.question)+" "+(position+1));
        ((TextView) view.findViewById(R.id.questionText)).setText(String.valueOf(endingTime));

        return view;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void update() {
        this.notifyDataSetChanged();
    }
}
