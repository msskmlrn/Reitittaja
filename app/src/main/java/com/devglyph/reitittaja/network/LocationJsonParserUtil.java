package com.devglyph.reitittaja.network;

import android.util.Log;

import com.devglyph.reitittaja.models.Coordinates;
import com.devglyph.reitittaja.models.Location;
import com.devglyph.reitittaja.models.LocationDetails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LocationJsonParserUtil {

    private final String LOG_TAG = LocationJsonParserUtil.class.getSimpleName();

    public LocationJsonParserUtil() {
    }

    public ArrayList<Location> getPlacesFromJson(String jsonString) {
        ArrayList<Location> resultList = new ArrayList<>();

        final String TAG_LOCATION_TYPE = "locType";
        final String TAG_LOCATION_TYPE_ID = "locTypeId";
        final String TAG_LOCATION_NAME = "name";
        final String TAG_LOCATION_MATCHED_NAME = "matchedName";
        final String TAG_LOCATION_LANG = "lang";
        final String TAG_LOCATION_CITY = "city";

        final String TAG_LOCATION_COORDS = "coords";
        final String TAG_LOCATION_DETAILS = "details";

        try {
            JSONArray placesJson = new JSONArray(jsonString);
            JSONObject place;

            String locType;
            int locTypeId;
            String name;
            String matchedName;
            String lang;
            String city;
            String coords;
            Coordinates coordinates;

            JSONObject details;
            LocationDetails locationDetails;

            for (int i = 0; i < placesJson.length(); i++) {
                place = placesJson.getJSONObject(i);

                locType = place.getString(TAG_LOCATION_TYPE);
                locTypeId = place.getInt(TAG_LOCATION_TYPE_ID);
                name = place.getString(TAG_LOCATION_NAME);
                matchedName = place.getString(TAG_LOCATION_MATCHED_NAME);
                lang = place.getString(TAG_LOCATION_LANG);
                city = place.getString(TAG_LOCATION_CITY);
                coords = place.getString(TAG_LOCATION_COORDS);

                coordinates = readCoordinates(coords);

                details = place.getJSONObject(TAG_LOCATION_DETAILS);
                locationDetails = getPlaceDetailsFromJson(details);

                resultList.add(new Location(locType, locTypeId, name, matchedName,
                        lang, city, coordinates, locationDetails));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    private LocationDetails getPlaceDetailsFromJson(JSONObject detailsObject) {
        final String TAG_LOCATION_DETAILS_ADDRESS = "address";
        final String TAG_LOCATION_DETAILS_CODE = "code";
        final String TAG_LOCATION_DETAILS_SHORT_CODE= "shortCode";
        final String TAG_LOCATION_DETAILS_CHANGE_COST = "changeCost";
        final String TAG_LOCATION_DETAILS_LINES = "lines";
        final String TAG_LOCATION_DETAILS_TRANSPORT_TYPE_ID = "transport_type_id";
        final String TAG_LOCATION_DETAILS_HOUSE_NUMBER = "houseNumber";
        final String TAG_LOCATION_DETAILS_POI_TYPE = "poiType";

        //not all locations have the same type of detail information attached to them so
        //check if specific pieces of info are present
        try {
            String address;
            String code;
            String shortCode;
            double changeCost;
            JSONArray lines;
            int transportTypeId;
            int houseNumber;
            String poiType;

            LocationDetails details = new LocationDetails();

            if (detailsObject.has(TAG_LOCATION_DETAILS_ADDRESS)) {
                address = detailsObject.getString(TAG_LOCATION_DETAILS_ADDRESS);
                details.setAddress(address);
            }
            else if (detailsObject.has(TAG_LOCATION_DETAILS_CODE)) {
                code = detailsObject.getString(TAG_LOCATION_DETAILS_CODE);
                details.setCode(code);
            }
            else if (detailsObject.has(TAG_LOCATION_DETAILS_SHORT_CODE)) {
                shortCode = detailsObject.getString(TAG_LOCATION_DETAILS_SHORT_CODE);
                details.setShortCode(shortCode);
            }
            else if (detailsObject.has(TAG_LOCATION_DETAILS_CHANGE_COST)) {
                changeCost = detailsObject.getDouble(TAG_LOCATION_DETAILS_CHANGE_COST);
                details.setChangeCost(changeCost);
            }
            else if (detailsObject.has(TAG_LOCATION_DETAILS_LINES)) {
                lines = detailsObject.getJSONArray(TAG_LOCATION_DETAILS_LINES);
                //details.setLines(lines);
            }
            else if (detailsObject.has(TAG_LOCATION_DETAILS_TRANSPORT_TYPE_ID)) {
                transportTypeId = detailsObject.getInt(TAG_LOCATION_DETAILS_TRANSPORT_TYPE_ID);
                details.setTransportTypeId(transportTypeId);
            }
            else if (detailsObject.has(TAG_LOCATION_DETAILS_HOUSE_NUMBER)) {
                houseNumber = detailsObject.getInt(TAG_LOCATION_DETAILS_HOUSE_NUMBER);
                details.setHouseNumber(houseNumber);
            }
            else if (detailsObject.has(TAG_LOCATION_DETAILS_POI_TYPE)) {
                poiType = detailsObject.getString(TAG_LOCATION_DETAILS_POI_TYPE);
                details.setPoiType(poiType);
            }

            return  details;

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);

            return null;
        }
    }

    /**
     * Read coordinate information
     * @param coordinates
     * @return Location object
     */
    private Coordinates readCoordinates(String coordinates) {
        double latitude = 0; //y
        double longitude = 0; //x

        try {
            longitude = Double.parseDouble(coordinates.substring(0, coordinates.indexOf(",")));
            latitude = Double.parseDouble(coordinates.substring(coordinates.indexOf(",") + 1));
        }
        catch (Exception ex) {
            return new Coordinates(0, 0);
        }

        return new Coordinates(latitude, longitude);
    }
}
