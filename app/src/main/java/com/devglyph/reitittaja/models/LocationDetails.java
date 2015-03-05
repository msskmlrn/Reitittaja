package com.devglyph.reitittaja.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class LocationDetails implements Parcelable {

    //Detailed information about the location such as houseNumber for addresses, poiClass for POIs and codes for stops.
    private String address;
    private String code;
    private String shortCode;
    private double changeCost;
    private ArrayList<String> lines;
    private int transportTypeId = 0; //default value
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(code);
        dest.writeString(shortCode);
        dest.writeDouble(changeCost);
        dest.writeStringList(lines);
        dest.writeInt(transportTypeId);
        dest.writeInt(houseNumber);
        dest.writeString(poiType);
    }

    public static final Parcelable.Creator<LocationDetails> CREATOR = new Parcelable.Creator<LocationDetails>() {
        public LocationDetails createFromParcel(Parcel in) {
            return new LocationDetails(in);
        }

        public LocationDetails[] newArray(int size) {
            return new LocationDetails[size];
        }
    };

    private LocationDetails(Parcel in) {
        address = in.readString();
        code = in.readString();
        shortCode = in.readString();
        changeCost = in.readDouble();
        lines = in.createStringArrayList();
        transportTypeId = in.readInt();
        houseNumber = in.readInt();
        poiType = in.readString();
    }
}