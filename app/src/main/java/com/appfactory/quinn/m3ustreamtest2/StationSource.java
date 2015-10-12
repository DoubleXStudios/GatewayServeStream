package com.appfactory.quinn.m3ustreamtest2;

/**
 * Created by Quinn on 4/13/15.
 */
public class StationSource {
    private final String source;
    private final String name;
    private final int resourceID;

    public StationSource(String name,int resourceID, String source){
        this.name = name;
        this.source = source;
        this.resourceID = resourceID;

    }

    public String getName(){
        return this.name;
    }
    public String getSource(){
        return this.source;
    }
    public int getResourceID(){
        return this.resourceID;
    }


}
