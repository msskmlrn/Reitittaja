package com.devglyph.reitittaja.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.devglyph.reitittaja.Util;
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

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class RouteSearchService extends IntentService {
    private static final String ACTION_ROUTE_SEARCH = "com.devglyph.reitittaja.services.action.ROUTE_SEARCH";
    private static final String URL_EXTRA = "com.devglyph.reitittaja.services.extra.URL";
    public static final String ROUTE_SEARCH_DONE = "com.devglyph.reitittaja.ROUTE_SEARCH_DONE";

    private final String LOG_TAG = RouteSearchService.class.getSimpleName();

    public  final static String SER_KEY = "com.devglyph.routes";

    private Context mContext;

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startRouteSearch(Context context, String urlParam) {
        Log.d("com.devglyph.reitittaja.ROUTE_SEARCH", "startRouteSearch");
        Intent intent = new Intent(context, RouteSearchService.class);
        intent.setAction(ACTION_ROUTE_SEARCH);
        intent.putExtra(URL_EXTRA, urlParam);
        context.startService(intent);
    }

    public RouteSearchService() {
        super("RouteSearchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ROUTE_SEARCH.equals(action)) {
                Log.d(LOG_TAG, "if (ACTION_ROUTE_SEARCH.equals(action)) {");

                final String url = intent.getStringExtra(URL_EXTRA);
                handleRouteSearchAction(url);
            }
        }
    }

    /**
     * Handle the route search action in the provided background thread with the provided
     * parameter.
     */
    private void handleRouteSearchAction(String urlParam) {
        if (urlParam == null) {
            return;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String routesJsonStr = null;

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
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            routesJsonStr = buffer.toString();
            Log.d(LOG_TAG, "routes " + routesJsonStr);
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

        ArrayList<Route> routes = getRoutesFromJson(routesJsonStr);
        notifyFinished(routes);
    }

    private void notifyFinished(ArrayList<Route> routes) {
        Log.d(LOG_TAG, "notifyFinished");
        Intent intent = new Intent(ROUTE_SEARCH_DONE);
        intent.putParcelableArrayListExtra("routes", routes);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private ArrayList<Route> getRoutesFromJson(String json) {
        ArrayList<Route> routes = new ArrayList<>();

        Log.d(LOG_TAG, "getRoutesFromJson "+json);

        final String TAG_ROUTE_LENGTH = "length";
        final String TAG_ROUTE_DURATION = "duration";
        final String TAG_ROUTE_LEGS = "legs";

        try {
            JSONArray routesJson = new JSONArray(json);

            JSONArray routeArray;
            JSONObject routeObject;

            double length;
            long duration;
            JSONArray routeLegs;

            Route route;
            ArrayList<RouteLeg> legs;
            for (int i = 0; i < routesJson.length(); i++) {
                routeArray = routesJson.getJSONArray(i);
                routeObject = routeArray.getJSONObject(0);

                length = routeObject.getDouble(TAG_ROUTE_LENGTH);
                duration = routeObject.getLong(TAG_ROUTE_DURATION);
                routeLegs = routeObject.getJSONArray(TAG_ROUTE_LEGS);

                legs = new ArrayList<>();
                for (int j = 0; j < routeLegs.length(); j++) {
                    legs.add(getRouteLeg(routeLegs.getJSONObject(j)));
                }

                route = new Route(length, duration, legs);
                routes.add(route);
            }
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        Log.d(LOG_TAG, "end of getRoutesFromJson");
        return routes;
    }

    private RouteLeg getRouteLeg(JSONObject legObject) {
        final String TAG_LEG_LENGTH = "length";
        final String TAG_LEG_DURATION = "duration";
        final String TAG_LEG_TYPE = "type";
        final String TAG_LEG_LINE_CODE = "code";
        final String TAG_LEG_LOCATIONS = "locs";
        final String TAG_LEG_SHAPE = "shape";

        RouteLeg leg = null;

        try {
            double length = legObject.getDouble(TAG_LEG_LENGTH);
            long duration = legObject.getLong(TAG_LEG_DURATION);
            String type = legObject.getString(TAG_LEG_TYPE);
            int typeInt = Util.parseType(type);

            String lineCode = null;
            if (legObject.has(TAG_LEG_LINE_CODE)) {
                lineCode = legObject.getString(TAG_LEG_LINE_CODE);
            }
            String original = lineCode;
            lineCode = Util.parseJoreCode(typeInt, lineCode);

            Log.d(LOG_TAG, "lineCode " + lineCode + ", original "+original);

            JSONArray locations = legObject.getJSONArray(TAG_LEG_LOCATIONS);
            JSONArray shape = legObject.getJSONArray(TAG_LEG_SHAPE);

            ArrayList<RouteLocation> locationsList = getLegLocations(locations);
            ArrayList<Coordinates> shapeList = getLegShape(shape);

            leg = new RouteLeg(length, duration, typeInt, lineCode, locationsList, shapeList);
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return leg;
    }

    private ArrayList<RouteLocation> getLegLocations(JSONArray locationsArray) {
        final String TAG_LOCATION_ARRIVAL_TIME = "arrTime";
        final String TAG_LOCATION_DEPARTURE_TIME = "depTime";
        final String TAG_LOCATION_COORDINATES = "coord";

        final String TAG_LOCATION_NAME = "name";
        final String TAG_LOCATION_CODE = "code";
        final String TAG_LOCATION_SHORT_CODE = "shortCode";
        final String TAG_LOCATION_ADDRESS = "stopAddress";
        final String TAG_LOCATION_X_COORDINATE = "x";
        final String TAG_LOCATION_Y_COORDINATE = "y";

        ArrayList<RouteLocation> locations = new ArrayList<>();

        try {

            JSONObject location;
            String arrivalTime;
            String departureTime;
            String name;
            String code;
            String shortCode;
            String stopAddress;
            JSONObject coordinatesObject;
            Coordinates coordinates;

            RouteLocation routeLocation;

            for (int i = 0; i < locationsArray.length(); i++) {
                location = locationsArray.getJSONObject(i);

                //all locations have the three following parameters
                arrivalTime = location.getString(TAG_LOCATION_ARRIVAL_TIME);
                departureTime = location.getString(TAG_LOCATION_DEPARTURE_TIME);
                coordinatesObject = location.getJSONObject(TAG_LOCATION_COORDINATES);

                coordinates = new Coordinates(coordinatesObject.getDouble(TAG_LOCATION_Y_COORDINATE),
                        coordinatesObject.getDouble(TAG_LOCATION_X_COORDINATE));


                routeLocation = new RouteLocation(Util.parseDate(Util.DATE_FORMAT_FULL, arrivalTime),
                        Util.parseDate(Util.DATE_FORMAT_FULL, departureTime), coordinates);

                //check if the following parameters are present
                if (location.has(TAG_LOCATION_NAME)) {
                    name = location.getString(TAG_LOCATION_NAME);
                    routeLocation.setName(name);
                }
                else if (location.has(TAG_LOCATION_CODE)) {
                    code = location.getString(TAG_LOCATION_CODE);
                    routeLocation.setCode(tryParsingStringToInt(code));
                }
                else if (location.has(TAG_LOCATION_SHORT_CODE)) {
                    shortCode = location.getString(TAG_LOCATION_SHORT_CODE);
                    routeLocation.setShortCode(shortCode);
                }
                else if (location.has(TAG_LOCATION_ADDRESS)) {
                    stopAddress = location.getString(TAG_LOCATION_ADDRESS);
                    routeLocation.setAddress(stopAddress);
                }

                locations.add(routeLocation);
            }
        }
        catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return locations;
    }

    private int tryParsingStringToInt(String string) {
        try {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Cannot parse int", e);
            return -1;
        }
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
