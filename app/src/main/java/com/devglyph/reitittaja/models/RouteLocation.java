package com.devglyph.reitittaja.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Base class for CustomRouteLocation objects. These objects represent the information returned by the
 * HSL api call JSON response objects. See http://developer.reittiopas.fi/pages/fi/http-get-interface-version-2.php?lang=EN#route
 */
public class RouteLocation implements Parcelable {

    private Date arrivalTime;
    private Date departureTime;
    private String name;
    private double code;
    private String shortCode;
    private String address;
    private Coordinates coordinates;

    public RouteLocation() {

    }

    public RouteLocation(Date arrivalTime, Date departureTime, Coordinates coordinates) {
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.coordinates = coordinates;
    }

    /**
     * Getter for arrival time
     * @return arrival time
     */
    public Date getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Setter for arrival time
     * @param arrivalTime
     */
    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    /**
     * Getter for departure time
     * @return departure time
     */
    public Date getDepartureTime() {
        return departureTime;
    }

    /**
     * Setter for departure time
     * @param departurelTime
     */
    public void setDepartureTime(Date departurelTime) {
        this.departureTime = departurelTime;
    }

    /**
     * Getter for location name
     * @return location name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for location name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for location code
     * @return location code
     */
    public double getCode() {
        return code;
    }

    /**
     * Setter for location code
     * @param code
     */
    public void setCode(double code) {
        this.code = code;
    }

    /**
     * Getter for short location code
     * @return short location code
     */
    public String getShortCode() {
        return shortCode;
    }

    /**
     * Setter for short location code
     * @param shortCode
     */
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
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
     * Getter for coordinates
     * @return coordinates
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Setter for coordinates
     * @param coordinates
     */
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(arrivalTime.getTime());
        dest.writeLong(departureTime.getTime());
        dest.writeString(name);
        dest.writeDouble(code);
        dest.writeString(shortCode);
        dest.writeString(address);
        dest.writeParcelable(coordinates, flags);
    }

    public static final Parcelable.Creator<RouteLocation> CREATOR = new Parcelable.Creator<RouteLocation>() {
        public RouteLocation createFromParcel(Parcel in) {
            return new RouteLocation(in);
        }

        public RouteLocation[] newArray(int size) {
            return new RouteLocation[size];
        }
    };

    private RouteLocation(Parcel in) {
        arrivalTime = new Date(in.readLong());
        departureTime = new Date(in.readLong());
        name = in.readString();
        code = in.readDouble();
        shortCode = in.readString();
        address = in.readString();
        coordinates = in.readParcelable(Coordinates.class.getClassLoader());
    }


}