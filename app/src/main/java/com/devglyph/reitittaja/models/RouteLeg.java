package com.devglyph.reitittaja.models;

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
    private int type;
    private String lineCode;
    private ArrayList<RouteLocation> locations;
    private ArrayList<Coordinates> shape;

    //ids for the transportation modes
    public static final int HELSINKI_BUS = 1;
    public static final int ESPOO_BUS = 3;
    public static final int VANTAA_BUS = 4;
    public static final int REGION_BUS = 5;
    public static final int U_LINE_BUS = 8;
    public static final int HELSINKI_SERVICE_LINE_BUS = 21;
    public static final int HELSINKI_NIGHT_BUS = 22;
    public static final int ESPOO_SERVICE_LINE_BUS = 23;
    public static final int VANTAA_SERVICE_LINE_BUS = 24;
    public static final int REGION_NIGHT_BUS = 25;
    public static final int KIRKKONUMMI_BUS = 36;
    public static final int SIPOO_INTERNAL = 38;
    public static final int KERAVA_BUS = 39;

    public static final int TRAM = 2;
    public static final int METRO = 6;
    public static final int FERRY = 7;
    public static final int TRAIN = 12;
    public static final int WALK = 90;
    public static final int CYCLE = 91;

    public static final int OTHER_LOCAL_TRAFFIC = 9;
    public static final int LONG_DISTANCE_TRAFFIC = 10;
    public static final int EXPRESS = 11;
    public static final int VR_LONG_DISTANCE_TRAFFIC = 13;
    public static final int ALL = 14;

    public RouteLeg() {

    }

    public RouteLeg(double length, double duration, int type,
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
    public int getType() {
        return type;
    }

    /**
     * Setter for route leg type
     * @param type
     */
    public void setType(int type) {
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

    /**
     * Get the start location of the leg
     * @return start location
     */
    public RouteLocation getStartLocation() {
        return this.getLocations().get(0);
    }

    /**
     * Get the end location of the leg
     * @return end location
     */
    public RouteLocation getEndLocation() {
        return this.getLocations().get(this.getLocations().size() - 1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(length);
        dest.writeDouble(duration);
        dest.writeInt(type);
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
        type = in.readInt();
        lineCode = in.readString();
        locations = new ArrayList<RouteLocation>();
        in.readTypedList(locations, RouteLocation.CREATOR);
        shape = new ArrayList<Coordinates>();
        in.readTypedList(shape, Coordinates.CREATOR);
    }
}