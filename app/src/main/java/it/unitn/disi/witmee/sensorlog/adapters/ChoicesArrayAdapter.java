package it.unitn.disi.witmee.sensorlog.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.application.iLogApplication;
import it.unitn.disi.witmee.sensorlog.model.Answer;
import it.unitn.disi.witmee.sensorlog.model.Choice;

/**
 * Class that extends {@link ArrayAdapter} and is used to visualize {@link Choice} in the {@link android.widget.ListView} in the project
 */
public class ChoicesArrayAdapter extends ArrayAdapter<Choice> {

    LayoutInflater lInflater;
    ArrayList<Choice> choices = new ArrayList<Choice>();

    public ChoicesArrayAdapter(ArrayList<Choice> choices) {
        super(iLogApplication.getAppContext(), R.layout.choice, choices);
        this.choices=choices;
        lInflater = (LayoutInflater) iLogApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    /**
     * Method that visualizes the element. In this case, we only visualize one {@link TextView} with the {@link Choice#name}
     * @param position default value
     * @param convertView default value
     * @param parent default value
     * @return default {@link View}
     */
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.choice, parent, false);
        }

        ((TextView) view.findViewById(R.id.choiceText)).setText(String.valueOf(getItem(position).getName()));

        return view;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
