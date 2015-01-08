package com.devglyph.reitittaja.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.models.Coordinates;
import com.devglyph.reitittaja.models.Location;
import com.devglyph.reitittaja.models.Route;
import com.devglyph.reitittaja.models.RouteLeg;
import com.devglyph.reitittaja.models.RouteLocation;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LocationSearchTask extends AsyncTask<ArrayList<Route>, Void, ArrayList<Route>> {

    private final String LOG_TAG = LocationSearchTask.class.getSimpleName();

    private ArrayList<Route> routes;
    private Context mContext;
    private OnLocationSearchCompleted onLocationSearchCompleted;

    public interface OnLocationSearchCompleted{
        void onLocationSearchTaskCompleted(ArrayList<Route> routes);
    }

    public LocationSearchTask(Context context, OnLocationSearchCompleted onLocationSearchCompleted) {
        this.onLocationSearchCompleted = onLocationSearchCompleted;
        this.mContext = context;
    }

    /**
     * Fill in missing location information
     * @param params
     * @return list of routes with location information
     */
    @Override
    protected ArrayList<Route> doInBackground(ArrayList<Route>... params) {
        routes = params[0];

        //go through the routes
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            for (int j = 0; j < route.getLegs().size(); j++) {
                RouteLeg leg = route.getLegs().get(j);
                for (int k = 0; k < leg.getLocations().size(); k++) {
                    RouteLocation location = leg.getLocations().get(k);
                    //if the location name is missing, then query the API for info
                    if (location.getName() == null || location.getName().isEmpty() || location.getName().equals("null")) {
                        Location loc = getLocation(location.getCoordinates());
                        if (loc != null && loc.getName() != null) {
                            location.setName(loc.getName());
                        }
                    }
                }
            }
        }


        return routes;
    }

    private Location getLocation(Coordinates coordinates) {
        if (coordinates == null) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String locationJsonStr = null;

        try {
            URL url = prepareUrl(coordinates);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
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
            locationJsonStr = buffer.toString();
            Log.d(LOG_TAG, "locations " + locationJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        //parse the json to location objects
        ArrayList<Location> locationList = new LocationJsonParserUtil().getPlacesFromJson(locationJsonStr);
        if (locationList != null && !locationList.isEmpty()) {
            return locationList.get(0);
        }

        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Route> routes) {
        super.onPostExecute(routes);

        onLocationSearchCompleted.onLocationSearchTaskCompleted(routes);
    }

    private URL prepareUrl(Coordinates coordinates) {
        final String QUERY_BASE_URL =
                "http://api.reittiopas.fi/hsl/prod/?";
        final String QUERY_PARAM = "request";
        final String FORMAT_PARAM = "format";
        final String USERNAME_PARAM = "user";
        final String PASSWORD_PARAM = "pass";
        final String COORDINATE_OUTPUT_PARAM = "epsg_out";
        final String COORDINATE_INPUT_PARAM = "epsg_in";
        final String COORDINATE_PARAM = "coordinate";
        final String RADIUS_PARAM = "radius";

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(QUERY_PARAM, "reverse_geocode"));
        nameValuePairs.add(new BasicNameValuePair(FORMAT_PARAM, "json"));
        nameValuePairs.add(new BasicNameValuePair(USERNAME_PARAM, mContext.getString(R.string.reittiopas_username)));
        nameValuePairs.add(new BasicNameValuePair(PASSWORD_PARAM, mContext.getString(R.string.reittiopas_password)));
        nameValuePairs.add(new BasicNameValuePair(COORDINATE_OUTPUT_PARAM, "wgs84"));
        nameValuePairs.add(new BasicNameValuePair(COORDINATE_INPUT_PARAM, "wgs84"));

        String coords = coordinates.getLongitude() + "," + coordinates.getLatitude();
        nameValuePairs.add(new BasicNameValuePair(COORDINATE_PARAM, coords));
        nameValuePairs.add(new BasicNameValuePair(RADIUS_PARAM, "500"));

        String paramString = URLEncodedUtils.format(nameValuePairs, "utf-8");

        try {
            URL url = new URL(QUERY_BASE_URL + paramString);
            return url;
        }
        catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
