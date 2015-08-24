package com.totvs.pcsistemas.pocbeacons.pocbeacons.drivers;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.core.Path;
import com.firebase.client.core.Repo;
import com.totvs.pcsistemas.pocbeacons.pocbeacons.R;

/**
 * Created by rond.borges on 24/08/2015.
 */
public class FirebaseConn extends Firebase {
    static private String firebase_url = "https://pocbeacons.firebaseio.com";

    public static String getFirebase_url() {
        return firebase_url;
    }

    public static String getFirebase_token() {
        return firebase_token;
    }

    static private String firebase_token = "LSKmIxD1cPDLdThePlMeiJaUqTJJzm6MexMLwRWN";

    public FirebaseConn(String url, String token) {
        super(url);
        this.authWithCustomToken(token, new AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {

            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {

            }
        });
    }

    public FirebaseConn() {
        super(FirebaseConn.firebase_url);
        this.authWithCustomToken(FirebaseConn.firebase_token, new AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {

            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {

            }
        });
    }
}
