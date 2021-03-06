package edu.und.seau.lib.UAV.objects;

import androidx.annotation.NonNull;

public class UAV {

    private String id = "";
    private String Name = "";

    public String getId() {
        return id;
    }

    public String getName(){
        return Name;
    }

    public void setName(String value) { Name = value;}

    public void setId(String id){ this.id = id; }


    public UAV()
    {

    }

    public UAV(String id)
    {
        this.id = id;
    }

    public Boolean isValid()
    {
        return ((!id.isEmpty()) && (!Name.isEmpty()));
    }

    @NonNull
    @Override
    public String toString() {
        return "Name: "+Name+ "ID: "+ id;
    }
}
