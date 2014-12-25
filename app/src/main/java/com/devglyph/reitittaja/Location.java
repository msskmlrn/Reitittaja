package com.devglyph.reitittaja;

/**
 * Base class for geocodedLocation objects. These objects represent the information returned by the
 * HSL api call JSON response objects. See http://developer.reittiopas.fi/pages/fi/http-get-interface-version-2.php?lang=EN#geocode
 */
public class Location {
    private String locType; //Type of the location: street, address, poi (point of interest) or stop
    private int locTypeId; //Location type id of the location (1-9 and 1008 = poi, 10 = stop, 900 = address)
    private String name; //Name of the location.
    private String matchedName; //Name of the location in that was matched with the search key.
    private String lang; //Language of the matched name of the location.
    private String city; //Name of the city the location is in.
    private Coordinates coords; //Coordinates of the location (<x,y>, e.g. 2551217,6681725).
    private LocationDetails details; //Detailed information about the location such as houseNumber for addresses, poiClass for POIs and codes for stops.

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

    @Override
    public String toString() {
        String result = getName() + " - " + getCity();
        if (details.getPoiType() != null) {
            result = result + " - " + details.getPoiType();
        }
        else if (details.getTransportTypeId() != 0) {
            result = result + " - " + details.getTransportTypeId();
        }

        return result;
    }
}