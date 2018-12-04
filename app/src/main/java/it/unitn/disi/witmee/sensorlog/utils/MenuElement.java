package it.unitn.disi.witmee.sensorlog.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public class    MenuElement {

    private String code, description;
    private Drawable icon;
    private boolean isToShow;
    private Intent activity;

    public MenuElement (String code, String description, Drawable icon, Intent activity, boolean isToShow) {
        this.code = code;
        this.description = description;
        this.icon = icon;
        this.isToShow = isToShow;
        this.activity = activity;
    }

    public Drawable getIcon() {
        return icon;
    }
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsToShow() {
        return isToShow;
    }
    public void setIsToShow(boolean isToShow) {
        this.isToShow = isToShow;
    }
    //endregion

    public String toString() {
        return "[" + this.code + "," + this.description + "," + this.icon + "," + this.isToShow + "]";
    }

    public Intent getIntent() {
        return activity;
    }

    public void setIntent(Intent activity) {
        this.activity = activity;
    }
}
