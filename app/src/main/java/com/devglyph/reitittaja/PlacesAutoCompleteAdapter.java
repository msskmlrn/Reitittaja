package com.devglyph.reitittaja;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private final String LOG_TAG = PlacesAutoCompleteAdapter.class.getSimpleName();

    private JourneyPlannerFragment journeyPlannerFragment;
    private ArrayList<String> resultList;
    private Context mContext;

    public PlacesAutoCompleteAdapter(JourneyPlannerFragment journeyPlannerFragment, Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.journeyPlannerFragment = journeyPlannerFragment;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    Log.d(LOG_TAG, "Charconstraintsequence " + constraint);

                    resultList = autocomplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();

                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    private ArrayList<String> autocomplete(String input) {
        if (input == null || input.length() < 3) {
            return null;
        }

        ArrayList<String> autoCompleteResultsList = new ArrayList<>();

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        String resultString;
        BufferedReader reader = null;
        try {
            final String QUERY_BASE_URL =
                    "http://api.reittiopas.fi/hsl/prod/?";
            final String QUERY_PARAM = "request";
            final String FORMAT_PARAM = "format";
            final String USERNAME_PARAM = "user";
            final String PASSWORD_PARAM = "pass";
            final String COORDINATE_OUTPUT_PARAM = "epsg_out";

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair(QUERY_PARAM, "geocode"));
            nameValuePairs.add(new BasicNameValuePair(FORMAT_PARAM, "json"));
            nameValuePairs.add(new BasicNameValuePair(USERNAME_PARAM, mContext.getString(R.string.reittiopas_username)));
            nameValuePairs.add(new BasicNameValuePair(PASSWORD_PARAM, mContext.getString(R.string.reittiopas_password)));
            nameValuePairs.add(new BasicNameValuePair(COORDINATE_OUTPUT_PARAM, "wgs84"));
            nameValuePairs.add(new BasicNameValuePair("key", input));
            String paramString = URLEncodedUtils.format(nameValuePairs, "utf-8");

            URL url = new URL(QUERY_BASE_URL + paramString);
            Log.d(LOG_TAG, ""+url);

            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Read the input stream into a String
            InputStream inputStream = conn.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            resultString = buffer.toString();
            Log.d(LOG_TAG, resultString);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        ArrayList<Location> locationList = getPlacesFromJson(resultString);

        //pass the location list to the fragment
        journeyPlannerFragment.setLocationList(locationList);

        //create a list of the locations in string form
        if (locationList != null && !locationList.isEmpty()) {
            String result;
            for (int i = 0; i < locationList.size(); i++) {
                result = locationList.get(i).toString();
                if (result != null && !result.isEmpty()) {
                    Log.d(LOG_TAG, "adding result "+result);
                    autoCompleteResultsList.add(result);
                }
            }
        }

        return autoCompleteResultsList;
    }

    private ArrayList<Location> getPlacesFromJson(String jsonString) {
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

        Log.d(LOG_TAG, "END OF PARSING");

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
        catch (NumberFormatException ex) {

        }
        catch (IndexOutOfBoundsException ex) {

        }
        catch (NullPointerException ex) {

        }

        return new Coordinates(latitude, longitude);
    }
}