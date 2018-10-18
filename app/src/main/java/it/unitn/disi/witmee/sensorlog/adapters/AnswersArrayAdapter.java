package it.unitn.disi.witmee.sensorlog.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.Answer;
import it.unitn.disi.witmee.sensorlog.utils.Utils;

/**
 * Class that extends {@link ArrayAdapter} and is used to visualize {@link Answer} in the {@link android.widget.ListView} in the project
 */
public class AnswersArrayAdapter extends ArrayAdapter<Answer> {

    LayoutInflater lInflater;

    public AnswersArrayAdapter(String instanceid) {
        super(iLogApplication.getAppContext(), R.layout.single_answer, iLogApplication.db.getAllAnswersByInstanceId(instanceid));
        lInflater = (LayoutInflater) iLogApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Method that visualizes the element. In this case, we only visualize one {@link TextView} with the {@link Answer#answertime}
     * @param position default value
     * @param convertView default value
     * @param parent default value
     * @return default {@link View}
     */
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.single_answer, parent, false);
        }

        Answer question = getItem(position);

        ((TextView) view.findViewById(R.id.answerTime)).setText(String.valueOf(Utils.longToStringFormatTasks(question.getAnswertime())));

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
