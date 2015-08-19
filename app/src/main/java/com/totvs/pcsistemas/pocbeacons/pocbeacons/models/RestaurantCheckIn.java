package com.totvs.pcsistemas.pocbeacons.pocbeacons.models;

/**
 * Created by rond.borges on 18/08/2015.
 */
public class RestaurantCheckIn {

    private String table;
    private String beaconIdentifier;
    private String pictureUrl;
    private String status;
    private String customerName;
    private String transaction;

    public RestaurantCheckIn(String table, String beaconIdentifier, String pictureUrl, String status, String customerName, String transaction){
        this.table = table;
        this.beaconIdentifier = beaconIdentifier;
        this.pictureUrl = pictureUrl;
        this.status = status;
        this.customerName = customerName;
        this.transaction = transaction;
    }

    public String getTable() {
        return table;
    }

    public String getBeaconIdentifier() {
        return beaconIdentifier;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public String getStatus() {
        return status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getTransaction(){
        return transaction;
    }

    private RestaurantCheckIn(){
    }
}
