package com.totvs.pcsistemas.pocbeacons.pocbeacons.models;

/**
 * Created by rond.borges on 18/08/2015.
 */
public class RestaurantCheckIn {

    private Integer table;
    private String beaconIdentifier;
    private String pictureUrl;
    private String status;
    private String customerName;
    private String transaction;
    private Double bill;

    public RestaurantCheckIn(Integer table, String beaconIdentifier, String pictureUrl, String status, String customerName, String transaction, Double bill){
        this.table = table;
        this.beaconIdentifier = beaconIdentifier;
        this.pictureUrl = (pictureUrl == "" ? "http://lorempixel.com/75/75/people/" : pictureUrl);
        this.status = status;
        this.customerName = customerName;
        this.transaction = transaction;
        this.bill = bill;
    }

    public Integer getTable() {
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

    public Double getBill(){
        return bill;
    }

    public void setTable(Integer table){
        this.table = table;
    }

    public void setBill(Double bill){
        this.bill = bill;
    }

    public void Copy(RestaurantCheckIn source){
        this.table = source.table;
        this.beaconIdentifier = source.beaconIdentifier;
        this.pictureUrl = source.pictureUrl;
        this.status = source.status;
        this.customerName = source.customerName;
        this.transaction = source.transaction;
        this.bill = source.bill;
    }

    private RestaurantCheckIn(){
    }
}
