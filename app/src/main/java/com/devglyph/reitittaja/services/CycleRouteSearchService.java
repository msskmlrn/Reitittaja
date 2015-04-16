package com.devglyph.reitittaja.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.devglyph.reitittaja.models.Coordinates;
import com.devglyph.reitittaja.models.Route;
import com.devglyph.reitittaja.models.RouteLeg;
import com.devglyph.reitittaja.models.RouteLocation;

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
import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class CycleRouteSearchService extends IntentService {
    private static final String ACTION_CYCLE_ROUTE_SEARCH = "com.devglyph.reitittaja.services.action.CYCLE_ROUTE_SEARCH";
    private static final String URL_EXTRA = "com.devglyph.reitittaja.services.extra.URL";
    private static final String SEARCH_TIME_EXTRA = "com.devglyph.reitittaja.services.extra.SEARCH_TIME";

    private final String LOG_TAG = CycleRouteSearchService.class.getSimpleName();

    private long searchTime;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startCycleRouteSearch(Context context, String urlParam, long searchTime) {
        Log.d("com.devglyph.reitittaja.CYCLE_ROUTE_SEARCH", "startCycleRouteSearch");
        Intent intent = new Intent(context, CycleRouteSearchService.class);
        intent.setAction(ACTION_CYCLE_ROUTE_SEARCH);
        intent.putExtra(URL_EXTRA, urlParam);
        intent.putExtra(SEARCH_TIME_EXTRA, searchTime);
        context.startService(intent);
    }

    public CycleRouteSearchService() {
        super("CycleRouteSearchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent");
        if (intent != null) {
            //get the time that was used as the start time of the route
            if (intent.hasExtra(SEARCH_TIME_EXTRA)) {
                searchTime = intent.getLongExtra(SEARCH_TIME_EXTRA, -1);
            }

            final String action = intent.getAction();
            if (ACTION_CYCLE_ROUTE_SEARCH.equals(action)) {
                Log.d(LOG_TAG, "if (ACTION_CYCLE_ROUTE_SEARCH.equals(action)) {");

                final String url = intent.getStringExtra(URL_EXTRA);
                handleCycleRouteSearchAction(url);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleCycleRouteSearchAction(String urlParam) {
        if (urlParam == null) {
            return;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String cycleRoutesJson = null;

        try {
            URL url = new URL(urlParam);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                //add new line for debugging
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            cycleRoutesJson = buffer.toString();
            Log.d(LOG_TAG, "routes " + cycleRoutesJson);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return;
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

        ArrayList<Route> routes = getCycleRoutesFromJson(cycleRoutesJson);
        notifyFinished(routes);
    }

    private void notifyFinished(ArrayList<Route> routes) {
        Intent intent = new Intent(RouteSearchService.ROUTE_SEARCH_DONE);
        intent.putParcelableArrayListExtra("routes", routes);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(LOG_TAG, "notifyFinished");
    }

    private ArrayList<Route> getCycleRoutesFromJson(String json) {
        ArrayList<Route> routes = new ArrayList<>();

        Log.d(LOG_TAG, "getCycleRoutesFromJson "+json);

        final String TAG_ROUTE_LENGTH = "length";
        final String TAG_ROUTE_LEGS = "path";

        try {
            JSONObject routesJson = new JSONObject(json);

            double length;
            JSONArray routeLegs;

            Route route;
            ArrayList<RouteLeg> legs;
            for (int i = 0; i < routesJson.length(); i++) {
                length = routesJson.getDouble(TAG_ROUTE_LENGTH);

                routeLegs = routesJson.getJSONArray(TAG_ROUTE_LEGS);

                legs = new ArrayList<>();
                for (int j = 0; j < routeLegs.length(); j++) {
                    legs.add(getRouteLeg(routeLegs.getJSONObject(j)));
                }

                route = new Route(length, legs);
                routes.add(route);
            }
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return routes;
    }

    private RouteLeg getRouteLeg(JSONObject legObject) {
        final String TAG_LEG_LENGTH = "length";
        final String TAG_LEG_NAME = "name";
        final String TAG_LEG_SURFACE_TYPE = "type";
        final String TAG_LEG_POINTS = "points";

        RouteLeg leg = null;

        try {
            double length = legObject.getDouble(TAG_LEG_LENGTH);
            String name = legObject.getString(TAG_LEG_NAME);
            String surfaceType = legObject.getString(TAG_LEG_SURFACE_TYPE);

            JSONArray points = legObject.getJSONArray(TAG_LEG_POINTS);
            ArrayList<Coordinates> coordinatesList = getLegShape(points);

            //get the first coordinates from the path and use them as the coordinates for the location
            ArrayList<RouteLocation> locationsList = new ArrayList<>();
            RouteLocation routeLocation = new RouteLocation(name, coordinatesList.get(0));

            //set the departure and arrival times temporarily to the start time of the queried trip
            routeLocation.setDepartureTime(new Date(searchTime));
            routeLocation.setArrivalTime(new Date(searchTime));

            locationsList.add(routeLocation);

            leg = new RouteLeg(length, surfaceType, locationsList, coordinatesList);
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return leg;
    }

    private ArrayList<RouteLocation> getLegLocations(JSONArray locationsArray, String name) {
        final String TAG_LOCATION_X_COORDINATE = "x";
        final String TAG_LOCATION_Y_COORDINATE = "y";
        final String TAG_LOCATION_Z_COORDINATE = "z";

        ArrayList<RouteLocation> locations = new ArrayList<>();

        try {
            JSONObject location;
            Coordinates coordinates;

            RouteLocation routeLocation;

            for (int i = 0; i < locationsArray.length(); i++) {
                location = locationsArray.getJSONObject(i);

                coordinates = new Coordinates(location.getDouble(TAG_LOCATION_Y_COORDINATE),
                        location.getDouble(TAG_LOCATION_X_COORDINATE));

                routeLocation = new RouteLocation(name, coordinates);

                //set the departure and arrival times temporarily to the start time of the queried trip
                routeLocation.setDepartureTime(new Date(searchTime));
                routeLocation.setArrivalTime(new Date(searchTime));

                locations.add(routeLocation);
            }
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return locations;
    }

    private ArrayList<Coordinates> getLegShape(JSONArray shapeArray) {
        ArrayList<Coordinates> shape = new ArrayList<>();

        final String TAG_LOCATION_X_COORDINATE = "x";
        final String TAG_LOCATION_Y_COORDINATE = "y";

        try {
            JSONObject locationObject;
            Coordinates coordinates;
            for (int i = 0; i < shapeArray.length(); i++) {
                locationObject = shapeArray.getJSONObject(i);
                coordinates = new Coordinates(locationObject.getDouble(TAG_LOCATION_Y_COORDINATE),
                        locationObject.getDouble(TAG_LOCATION_X_COORDINATE));

                shape.add(coordinates);

            }
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return shape;
    }
}
