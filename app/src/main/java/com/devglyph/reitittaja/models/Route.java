package com.devglyph.reitittaja.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Base class for Route objects. These objects represent the information returned by the
 * HSL route api call JSON response objects.
 * See http://developer.reittiopas.fi/pages/fi/http-get-interface-version-2.php?lang=EN#route
 */
public class Route implements Parcelable{

    private double length;
    private long duration;
    private ArrayList<RouteLeg> legs;

    public Route() {

    }

    public Route(double length, long duration, ArrayList<RouteLeg> legs) {
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
    public long getDuration() {
        return duration;
    }

    /**
     * Setter for route duration
     * @param duration
     */
    public void setDuration(long duration) {
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

    /**
     * Get the start location of this route
     * @return start location
     */
    public RouteLocation getStartLocation() {
        return this.getLegs().get(0).getStartLocation();
    }


    /**
     * Get the end location of this route
     * @return end location
     */
    public RouteLocation getEndLocation() {
        return this.getLegs().get(this.getLegs().size() - 1).getEndLocation();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(length);
        dest.writeLong(duration);
        dest.writeTypedList(legs);
    }

    public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    private Route(Parcel in) {
        length = in.readDouble();
        duration = in.readLong();
        legs = new ArrayList<RouteLeg>();
        in.readTypedList(legs, RouteLeg.CREATOR);
    }
}
