package com.devglyph.reitittaja.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Base class for geocodedLocation objects. These objects represent the information returned by the
 * HSL api call JSON response objects. See http://developer.reittiopas.fi/pages/fi/http-get-interface-version-2.php?lang=EN#geocode
 */
public class Location implements Parcelable {
    private String locType; //Type of the location: street, address, poi (point of interest) or stop
    private int locTypeId; //Location type id of the location (1-9 and 1008 = poi, 10 = stop, 900 = address)
    private String name; //Name of the location.
    private String matchedName; //Name of the location in that was matched with the search key.
    private String lang; //Language of the matched name of the location.
    private String city; //Name of the city the location is in.
    private Coordinates coords; //Coordinates of the location (<x,y>, e.g. 2551217,6681725).
    private LocationDetails details; //Detailed information about the location such as houseNumber for addresses, poiClass for POIs and codes for stops.

    private boolean favorite; //if the location has been saved to the favorites
    private String description; //description given to the favorite location

    public Location(String locType, int locTypeId, String name,
                            String matchedName, String lang, String city,
                            Coordinates coords, LocationDetails details) {

        this.locType = locType;
        this.locTypeId = locTypeId;
        this.name = name;
        this.matchedName = matchedName;
        this.lang = lang;
        this.city = city;
        this.coords = coords;
        this.details = details;
    }

    public Location(String name, String description, double latitude, double longitude, boolean favorite) {
        this.name = name;
        this.coords = new Coordinates(latitude, longitude);
        this.details = new LocationDetails();
        this.description = description;
        this.favorite = favorite;
    }

    /**
     * Getter for location type
     * @return location type
     */
    public String getLocType() {
        return locType;
    }

    /**
     * Setter for location type
     * @param locType
     */
    public void setLocType(String locType) {
        this.locType = locType;
    }

    /**
     * Getter for location type id
     * @return location type id
     */
    public int getLocTypeId() {
        return locTypeId;
    }

    /**
     * Setter for location type id
     * @param locTypeId
     */
    public void setLocTypeId(int locTypeId) {
        this.locTypeId = locTypeId;
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
     * Getter for matched name
     * @return matched name
     */
    public String getMatchedName() {
        return matchedName;
    }

    /**
     * Setter for matched name
     * @param matchedName
     */
    public void setMatchedName(String matchedName) {
        this.matchedName = matchedName;
    }

    /**
     * Getter for location language
     * @return location language
     */
    public String getLang() {
        return lang;
    }

    /**
     * Setter for location language
     * @param lang
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * Getter for the city of the location
     * @return city of the location
     */
    public String getCity() {
        return city;
    }

    /**
     * Setter for city of the location
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Getter for coordinates
     * @return coordinates
     */
    public Coordinates getCoords() {
        return coords;
    }

    /**
     * Setter for coordinates
     * @param coords
     */
    public void setCoords(Coordinates coords) {
        this.coords = coords;
    }

    /**
     * Getter for location details
     * @return location details
     */
    public LocationDetails getDetails() {
        return details;
    }

    /**
     * Setter for location details
     * @param details
     */
    public void setDetails(LocationDetails details) {
        this.details = details;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public String getDescription() {
        return description;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        String result = "";
        if (getName() != null) {
            result =  getName();
        }
        if (getCity() != null) {
            result = result + " - " + getCity();
        }

        if (details != null) {
            if (details.getPoiType() != null && !details.getPoiType().isEmpty()) {
                result = result + " - " + details.getPoiType();
            }
            else if (details.getTransportTypeId() != 0) {
                result = result + " - " + details.getTransportTypeId();
            }
        }

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(locType);
        dest.writeInt(locTypeId);
        dest.writeString(name);
        dest.writeString(matchedName);
        dest.writeString(lang);
        dest.writeString(city);
        dest.writeParcelable(coords, flags);
        dest.writeParcelable(details, flags);
        int favoriteValue = favorite == true ? 1 : 0;
        dest.writeInt(favoriteValue);
        dest.writeString(description);
    }

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    private Location(Parcel in) {
        locType = in.readString();
        locTypeId = in.readInt();
        name = in.readString();
        matchedName = in.readString();
        lang = in.readString();
        city = in.readString();
        coords = in.readParcelable(Coordinates.class.getClassLoader());
        details = in.readParcelable(LocationDetails.class.getClassLoader());

        int favoriteValue = in.readInt();
        favorite = favoriteValue == 1 ? true : false;

        description = in.readString();
    }
}