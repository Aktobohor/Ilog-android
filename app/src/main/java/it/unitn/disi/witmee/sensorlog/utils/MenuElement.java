package it.unitn.disi.witmee.sensorlog.utils;

public class MenuElement {
    public MenuElement (String code, String description, int icon, boolean isToShow) {
        this.code = code;
        this.description = description;
        this.icon = icon;
        this.isToShow = isToShow;
    }

    //#region getters/setters
    private String code,
            description;
    private int icon;
    private boolean isToShow;

    public int getIcon() {
        return icon;
    }
    public void setIcon(int icon) {
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
}
