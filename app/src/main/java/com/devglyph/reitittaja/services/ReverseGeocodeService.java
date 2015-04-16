package com.devglyph.reitittaja.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.data.LocationContract;
import com.devglyph.reitittaja.models.Coordinates;
import com.devglyph.reitittaja.models.Location;
import com.devglyph.reitittaja.models.Route;
import com.devglyph.reitittaja.models.RouteLeg;
import com.devglyph.reitittaja.models.RouteLocation;
import com.devglyph.reitittaja.network.LocationJsonParserUtil;

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

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class ReverseGeocodeService extends IntentService {
    private static final String ACTION_REVERSE_GEOCODE = "com.devglyph.reitittaja.services.action.REVERSE_GEOCODE_SEARCH";
    public static final String REVERSE_GEOCODED_ROUTES_EXTRA = "com.devglyph.reitittaja.services.extra.reverse_geocoded_routes";
    public static final String REVERSE_GEOCODING_DONE = "com.devglyph.reitittaja.REVERSE_GEOCODING_DONE";

    private final String LOG_TAG = ReverseGeocodeService.class.getSimpleName();

    private static final String TAG = ReverseGeocodeService.class.getSimpleName();

    /**
     * Starts this service to perform reverse geocode search with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startReverseGeocoding(Context context, ArrayList<Route> routes) {
        Log.d(TAG, "startReverseGeocoding");
        Intent intent = new Intent(context, ReverseGeocodeService.class);
        intent.setAction(ACTION_REVERSE_GEOCODE);
        intent.putParcelableArrayListExtra(REVERSE_GEOCODED_ROUTES_EXTRA, routes);
        context.startService(intent);
    }

    public ReverseGeocodeService() {
        super("ReverseGeocodeService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REVERSE_GEOCODE.equals(action)) {
                final ArrayList<Route> routes = intent.getParcelableArrayListExtra(REVERSE_GEOCODED_ROUTES_EXTRA);
                handleReverseGeocodeAction(routes);
            }
        }
    }

    /**
     * Handle the reverse geocode action in the provided background thread with the provided
     * parameters.
     */
    private void handleReverseGeocodeAction(ArrayList<Route> routes) {
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
                    //the location has all the needed info, so save it to the database for later use
                    else {
                        addLocation(false, location.getName(), null, location.getCoordinates().getLatitude(), location.getCoordinates().getLongitude());
                    }
                }
            }
        }

        notifyFinished(routes);
    }

    /**
     * Helper method to handle insertion of a new location in the database.
     *
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private long addLocation(boolean favorite, String name, String description, double lat, double lon) {
        Log.d(LOG_TAG, "add location "+favorite + " "+name + " "+ description + " " + lat + " " + lon);

        // First, check if the location with this name exists in the db
        Cursor cursor = this.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI,
                new String[]{LocationContract.LocationEntry._ID},
                LocationContract.LocationEntry.COLUMN_LOCATION_NAME + " = ?",
                new String[]{name},
                null,
                null);

        if (cursor.moveToFirst()) {
            int locationIdIndex = cursor.getColumnIndex(LocationContract.LocationEntry._ID);
            Log.d(LOG_TAG, "location already present");

            long result = cursor.getLong(locationIdIndex);
            cursor.close();
            return result;
        } else {
            int favoriteValue = favorite ? 1 : 0;

            if (description == null || description.isEmpty()) {
                description = "-";
            }

            ContentValues locationValues = new ContentValues();
            locationValues.put(LocationContract.LocationEntry.COLUMN_FAVORITE, favoriteValue);
            locationValues.put(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION, description);
            locationValues.put(LocationContract.LocationEntry.COLUMN_LOCATION_NAME, name);
            locationValues.put(LocationContract.LocationEntry.COLUMN_COORD_LAT, lat);
            locationValues.put(LocationContract.LocationEntry.COLUMN_COORD_LONG, lon);

            Uri locationInsertUri = this.getContentResolver()
                    .insert(LocationContract.LocationEntry.CONTENT_URI, locationValues);

            Log.d(LOG_TAG, "inserting location");
            Log.d(LOG_TAG, "favorite value "+favoriteValue);

            cursor.close();
            return ContentUris.parseId(locationInsertUri);
        }
    }


    private void notifyFinished(ArrayList<Route> routes) {
        Intent intent = new Intent(REVERSE_GEOCODING_DONE);
        intent.putParcelableArrayListExtra(REVERSE_GEOCODED_ROUTES_EXTRA, routes);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
                //add new line for debugging
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            locationJsonStr = buffer.toString();
            Log.d(LOG_TAG, "locations " + locationJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
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
        nameValuePairs.add(new BasicNameValuePair(USERNAME_PARAM, getString(R.string.reittiopas_username)));
        nameValuePairs.add(new BasicNameValuePair(PASSWORD_PARAM, getString(R.string.reittiopas_password)));
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
