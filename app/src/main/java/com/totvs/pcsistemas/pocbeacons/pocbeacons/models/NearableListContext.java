package com.totvs.pcsistemas.pocbeacons.pocbeacons.models;

/**
 * Created by rond.borges on 12/08/2015.
 */
public class NearableListContext {

    private String title;
    private String description;
    private String pictureUrl;

    private NearableListContext(){

    }

    NearableListContext(String title, String description, String pictureUrl){
        this.title = title;
        this.description = description;
        this.pictureUrl = pictureUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getPictureUrl(){return pictureUrl;}
}
