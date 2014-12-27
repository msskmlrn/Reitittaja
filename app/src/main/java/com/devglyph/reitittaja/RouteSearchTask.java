package com.devglyph.reitittaja;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RouteSearchTask extends AsyncTask<URL, Void, ArrayList<Route>> {

    private final String LOG_TAG = RouteSearchTask.class.getSimpleName();

    public  final static String SER_KEY = "com.devglyph.routes";

    private JourneyPlannerFragment journeyPlannerFragment;
    private ProgressDialog pDialog;

    public RouteSearchTask(JourneyPlannerFragment journeyPlannerFragment) {
        this.journeyPlannerFragment = journeyPlannerFragment;
    }

    @Override
    protected void onPreExecute() {
        pDialog = new ProgressDialog(journeyPlannerFragment.getActivity());
        pDialog.setMessage("Searching routes");
        pDialog.show();
    }

    @Override
    protected ArrayList<Route> doInBackground(URL... params) {

        if (params.length == 0) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String routesJsonStr = null;

        try {
            URL url = params[0];

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
            routesJsonStr = buffer.toString();
            Log.d(LOG_TAG, "routes " + routesJsonStr);
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

        return getRoutesFromJson(routesJsonStr);

    }

    @Override
    protected void onPostExecute(ArrayList<Route> routes) {
        super.onPostExecute(routes);

        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }

        if (routes != null && !routes.isEmpty()) {
            Intent intent = new Intent(journeyPlannerFragment.getActivity(), RouteListActivity.class);
            intent.putParcelableArrayListExtra(SER_KEY, routes);

            journeyPlannerFragment.startActivity(intent);
        }
        else {
            String message = "Please try again.";
            Toast.makeText(journeyPlannerFragment.getActivity(), message, Toast.LENGTH_SHORT).show();
        }

        Log.d(LOG_TAG, "onPostExecute");
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
            double duration;
            JSONArray routeLegs;

            Route route;
            ArrayList<RouteLeg> legs;
            for (int i = 0; i < routesJson.length(); i++) {
                routeArray = routesJson.getJSONArray(i);
                routeObject = routeArray.getJSONObject(0);

                length = routeObject.getDouble(TAG_ROUTE_LENGTH);
                duration = routeObject.getDouble(TAG_ROUTE_DURATION);
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
            double duration = legObject.getDouble(TAG_LEG_DURATION);
            String type = legObject.getString(TAG_LEG_TYPE);

            String lineCode = null;
            if (legObject.has(TAG_LEG_LINE_CODE)) {
                lineCode = legObject.getString(TAG_LEG_LINE_CODE);
            }

            JSONArray locations = legObject.getJSONArray(TAG_LEG_LOCATIONS);
            JSONArray shape = legObject.getJSONArray(TAG_LEG_SHAPE);

            ArrayList<RouteLocation> locationsList = getLegLocations(locations);
            ArrayList<Coordinates> shapeList = getLegShape(shape);

            leg = new RouteLeg(length, duration, type, lineCode, locationsList, shapeList);
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


                routeLocation = new RouteLocation(parseDateFromYYYYMMDDHHMM(arrivalTime),
                        parseDateFromYYYYMMDDHHMM(departureTime), coordinates);

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

    private Date parseDateFromYYYYMMDDHHMM(String time) {
        String DATE_FORMAT = "yyyyMMddHHmm";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

        try {
            return sdf.parse(time);
        }
        catch (ParseException e) {
            Log.e(LOG_TAG, "Cannot parse time", e);
            return null;
        }
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