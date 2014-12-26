package com.devglyph.reitittaja;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Base class for CustomRouteLocation objects. These objects represent the information returned by the
 * HSL api call JSON response objects. See http://developer.reittiopas.fi/pages/fi/http-get-interface-version-2.php?lang=EN#route
 */
public class RouteLocation {

    private Date arrivalTime;
    private Date departureTime;
    private String name;
    private double code;
    private String shortCode;
    private String address;
    private Location coordinates;

    public RouteLocation() {

    }

    public RouteLocation(Location coordinates, String arrivalTime, String departureTime,
                               String name, double code, String shortcode, String address) {

        String DATE_FORMAT = "yyyyMMddHHmm";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

        try {
            this.setArrivalTime(sdf.parse(arrivalTime));
        }
        catch (ParseException e) {
            this.setArrivalTime(Calendar.getInstance().getTime());
        }

        try {
            this.setDepartureTime(sdf.parse(departureTime));
        }
        catch (ParseException e) {
            this.setDepartureTime(Calendar.getInstance().getTime());
        }

        this.name = name;
        this.code = code;
        this.shortCode = shortcode;
        this.address = address;
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
    public Location getCoordinates() {
        return coordinates;
    }

    /**
     * Setter for coordinates
     * @param coordinates
     */
    public void setCoordinates(Location coordinates) {
        this.coordinates = coordinates;
    }
}