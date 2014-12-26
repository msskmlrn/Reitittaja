package com.devglyph.reitittaja;

import java.util.ArrayList;

/**
 * Base class for Route objects. These objects represent the information returned by the
 * HSL route api call JSON response objects.
 * See http://developer.reittiopas.fi/pages/fi/http-get-interface-version-2.php?lang=EN#route
 */
public class Route {

    private double length;
    private double duration;
    private ArrayList<RouteLeg> legs;

    public Route() {

    }

    public Route(double length, double duration, ArrayList<RouteLeg> legs) {
        this.length = length;
        this.duration = duration;
        this.legs = legs;
    }

    public Route(double length, ArrayList<RouteLeg> legs) {
        this.length = length;
        this.legs = legs;
    }

    /**
     * Getter for route length
     * @return route length
     */
    public double getLength() {
        return length;
    }

    /**
     * Setter for route length
     * @param length
     */
    public void setLength(double length) {
        this.length = length;
    }

    /**
     * Getter for route duration
     * @return route duration
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Setter for route duration
     * @param duration
     */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * Getter for route legs
     * @return list of RouteLeg objects
     */
    public ArrayList<RouteLeg> getLegs() {
        return legs;
    }

    /**
     * Setter for route legs
     * @param legs
     */
    public void setLegs(ArrayList<RouteLeg> legs) {
        this.legs = legs;
    }
}
