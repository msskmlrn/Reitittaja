package com.devglyph.reitittaja;

import java.util.ArrayList;

public class LocationDetails {

    //Detailed information about the location such as houseNumber for addresses, poiClass for POIs and codes for stops.
    private String address;
    private String code;
    private String shortCode;
    private double changeCost;
    private ArrayList<String> lines;
    private int transportTypeId;
    private int houseNumber;
    private String poiType;

    public LocationDetails() {

    }


    /**
     * Getter for location address
     * @return location address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Setter for location address
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Getter for location code
     * @return location code
     */
    public String getCode() {
        return code;
    }

    /**
     * Setter for location code
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Getter for location short code
     * @return location short code
     */
    public String getShortCode() {
        return shortCode;
    }

    /**
     * Setter for location short code
     * @param shortCode
     */
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    /**
     * Getter for change cost
     * @return change cost
     */
    public double getChangeCost() {
        return changeCost;
    }

    /**
     * Setter for change cost
     * @param changeCost
     */
    public void setChangeCost(double changeCost) {
        this.changeCost = changeCost;
    }

    /**
     * Getter for lines
     * @return list of lines
     */
    public ArrayList<String> getLines() {
        return lines;
    }

    /**
     * Setter for lines
     * @param lines
     */
    public void setLines(ArrayList<String> lines) {
        this.lines = lines;
    }

    /**
     * Getter for transportation type id
     * @return transportation type id
     */
    public int getTransportTypeId() {
        return transportTypeId;
    }

    /**
     * Setter for transportation type id
     * @param transportTypeId
     */
    public void setTransportTypeId(int transportTypeId) {
        this.transportTypeId = transportTypeId;
    }

    /**
     * Getter for house number
     * @return house number
     */
    public int getHouseNumber() {
        return houseNumber;
    }

    /**
     * Setter for house number
     * @param houseNumber
     */
    public void setHouseNumber(int houseNumber) {
        this.houseNumber = houseNumber;
    }

    /**
     * Getter for poi type
     * @return
     */
    public String getPoiType() {
        return poiType;
    }

    /**
     * Setter for poi type
     * @param poiType
     */
    public void setPoiType(String poiType) {
        this.poiType = poiType;
    }
}