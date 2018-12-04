package it.unitn.disi.witmee.sensorlog.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.unitn.disi.witmee.sensorlog.R;
import it.unitn.disi.witmee.sensorlog.adapters.MenuAdapter;
import it.unitn.disi.witmee.sensorlog.utils.MenuElement;

public class ILog_CommonMethod {
    private static ILog_CommonMethod INSTANCE = new ILog_CommonMethod();
    private Context currentContext;
    private String[] osArray = {"Profilo", "Questionari", "Grafici", "Informazioni","Settaggi", "Chiudi"};
    private List menuElements = new ArrayList<MenuElement>();
    private MenuAdapter mAdapter;
    private ListView mDrawerList;

    // other instance variables can be here

    private ILog_CommonMethod() {};

    public static ILog_CommonMethod getInstance() {
        return(INSTANCE);
    }
    public void CreateMenu() {

        Intent intent;
        MenuElement m;
        String code;
        Drawable imageDrawable;

        for (String menuElement : osArray) {

            switch (menuElement) {
                case ("Profilo"): {
                    code = "Profile";
                    intent = new Intent(currentContext, ProfileActivity.class);
                    imageDrawable = currentContext.getResources().getDrawable(R.drawable.image_menu_profile);
                    break;
                }
                case ("Questionari"): {
                    code = "Survey";
                    intent = new Intent(currentContext, QuestionActivity.class);
                    imageDrawable = currentContext.getResources().getDrawable(R.drawable.image_menu_survey);
                    break;
                }
                case ("Grafici"): {
                    code = "Graph";
                    intent = null;
                    imageDrawable = currentContext.getResources().getDrawable(R.drawable.image_menu_graph);
                    break;
                }
                case ("Informazioni"): {
                    code = "Information";
                    intent = null;
                    imageDrawable = currentContext.getResources().getDrawable(R.drawable.image_menu_information);
                    break;
                }
                case ("Settaggi"): {
                    code = "Settings";
                    intent = new Intent(currentContext, SettingActivity.class);
                    imageDrawable = currentContext.getResources().getDrawable(R.drawable.image_menu_settings);
                    break;
                }
                case ("Chiudi"): {
                    code = "Quit";
                    intent = null;
                    imageDrawable = currentContext.getResources().getDrawable(R.drawable.image_menu_quit);
                    break;
                }
                default: {
                    code = "";
                    intent = null;
                    imageDrawable = currentContext.getResources().getDrawable(R.drawable.ic_missing_icon);
                    break;
                }
            }
            m = new MenuElement(code, menuElement, imageDrawable, intent, true);
            try {
                menuElements.add(m);
            } catch (Exception e) {
            }

        }

    }

    public Context getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(Context currentContext) {
        this.currentContext = currentContext;
    }

    public List<MenuElement> getMenuElement(){
        return menuElements;
    }

    public void setmDrawerList(ListView mDrawerList) {
        this.mDrawerList = mDrawerList;
    }
    public ListView getmDrowerList(){
        return mDrawerList;
    }
}
