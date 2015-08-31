package com.totvs.pcsistemas.pocbeacons.pocbeacons;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by rond.borges on 27/08/2015.
 */
public class ParsePushApplication extends Application {

    @Override
    public void onCreate(){
        Parse.initialize(this, getString(R.string.parseAppID), getString(R.string.parseClientID));
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}