package it.unitn.disi.witmee.sensorlog.model;

/**
 * Created with IntelliJ IDEA.
 ** User: Ilya * Modified by: Mattia
 * Date: 22/05/13
 * Time: 18.03
 */

public class Choice {

    int id = -1;
    int conceptId = -1;
    String name = "null";

    public Choice(int id, int conceptId, String name) {
        this.id = id;
        this.conceptId = conceptId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getConceptId() {
        return conceptId;
    }

    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getConceptId()+" "+this.getId()+" "+this.getName();
    }
}
