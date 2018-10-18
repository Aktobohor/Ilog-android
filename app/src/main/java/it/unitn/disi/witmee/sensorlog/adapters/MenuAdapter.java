package it.unitn.disi.witmee.sensorlog.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.utils.MenuElement;

public class MenuAdapter extends ArrayAdapter<MenuElement> {

    private final Context context;
    private final int layoutResourceId;
    private MenuElement data[] = null;
    private ImageView images; //////////////// var per la posizione delle immagini.


    public MenuAdapter(Context context, int layoutResourceId, MenuElement [] data)
    {
        super(context, layoutResourceId, data);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        View v = inflater.inflate(layoutResourceId, parent, false);

        TextView textView = (TextView) v.findViewById(R.id.text);

        //var in cui salvo l'id dell layout dell'immagine.
        images = (ImageView) v.findViewById(R.id.Icon);
        Drawable imageDrawable = null; //variabile in cui ci vado a disegnare la icona.

        MenuElement choice = data[position];


        textView.setText(choice.getDescription());
        switch (choice.getCode()) {
            case("Profile"): {
                imageDrawable = context.getResources().getDrawable(R.drawable.image_menu_profile);
                break;
            }
            case ("Survey"): {
                imageDrawable = context.getResources().getDrawable(R.drawable.image_menu_survey);
                break;
            }
            case ("Graph"): {
                imageDrawable = context.getResources().getDrawable(R.drawable.image_menu_graph);
                break;
            }
            case ("Information"):{
                imageDrawable = context.getResources().getDrawable(R.drawable.image_menu_information);
                break;
            }
            case ("Quit"): {
                imageDrawable = context.getResources().getDrawable(R.drawable.image_menu_quit);
                break;
            }

            default:{

                break;
            }
        }

        if(imageDrawable!=null) {
            // collego la variabile che è stata disegnata al layout dell'immagine.
            images.setImageDrawable(imageDrawable);
        }
        return v;
    }
}
