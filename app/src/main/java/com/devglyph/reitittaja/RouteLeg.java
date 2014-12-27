package com.devglyph.reitittaja;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Base class for RouteLeg objects. These objects represent the information returned by the
 * HSL route api call JSON response objects.
 * See http://developer.reittiopas.fi/pages/fi/http-get-interface-version-2.php?lang=EN#route
 */
public class RouteLeg implements Parcelable {

    private double length;
    private double duration;
    private String type;
    private String lineCode;
    private ArrayList<RouteLocation> locations;
    private ArrayList<Coordinates> shape;

    public RouteLeg() {

    }

    public RouteLeg(double length, double duration, String type,
                    String lineCode, ArrayList<RouteLocation> locations,
                    ArrayList<Coordinates> shape) {

        this.length = length;
        this.duration = duration;
        this.type = type;
        this.lineCode = lineCode;
        this.locations = locations;
        this.shape = shape;
    }

    /**
     * Getter for route leg length
     * @return route leg length
     */
    public double getLength() {
        return length;
    }

    /**
     * Setter for route leg length
     * @param length
     */
    public void setLength(double length) {
        this.length = length;
    }

    /**
     * Getter for route leg duration
     * @return route leg duration
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Setter for route leg duration
     * @param duration
     */
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * Getter for route leg type
     * @return route leg type
     */
    public String getType() {
        return type;
    }

    /**
     * Setter for route leg type
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter for route leg linecode
     * @return route leg linecode
     */
    public String getLineCode() {
        return lineCode;
    }

    /**
     * Setter for route leg linecode
     * @param lineCode
     */
    public void setLineCode(String lineCode) {
        this.lineCode = lineCode;
    }

    /**
     * Getter for route leg locations
     * @return list of RouteLocation objects
     */
    public ArrayList<RouteLocation> getLocations() {
        return locations;
    }

    /**
     * Setter for route leg locations
     * @param locations
     */
    public void setLocations(ArrayList<RouteLocation> locations) {
        this.locations = locations;
    }

    /**
     * Getter for route leg shape
     * @return list of Location objects
     */
    public ArrayList<Coordinates> getShape() {
        return shape;
    }

    /**
     * Setter for route leg shape
     * @param shape
     */
    public void setShape(ArrayList<Coordinates> shape) {
        this.shape = shape;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(length);
        dest.writeDouble(duration);
        dest.writeString(type);
        dest.writeString(lineCode);
        dest.writeTypedList(locations);
        dest.writeTypedList(shape);
    }

    public static final Parcelable.Creator<RouteLeg> CREATOR = new Parcelable.Creator<RouteLeg>() {
        public RouteLeg createFromParcel(Parcel in) {
            return new RouteLeg(in);
        }

        public RouteLeg[] newArray(int size) {
            return new RouteLeg[size];
        }
    };

    private RouteLeg(Parcel in) {
        length = in.readDouble();
        duration = in.readDouble();
        type = in.readString();
        lineCode = in.readString();
        locations = new ArrayList<RouteLocation>();
        in.readTypedList(locations, RouteLocation.CREATOR);
        shape = new ArrayList<Coordinates>();
        in.readTypedList(shape, Coordinates.CREATOR);
    }
}