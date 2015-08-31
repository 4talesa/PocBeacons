package com.totvs.pcsistemas.pocbeacons.pocbeacons;

import android.app.Application;
import android.content.Intent;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.services.NearablePushNotification;

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