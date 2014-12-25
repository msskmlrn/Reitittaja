package com.devglyph.reitittaja;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RouteSearchTask extends AsyncTask<URL, Void, String[]> {

    private final String LOG_TAG = RouteSearchTask.class.getSimpleName();

    private JourneyPlannerFragment journeyPlannerFragment;
    ProgressDialog pDialog;

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
    protected String[] doInBackground(URL... params) {

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
    protected void onPostExecute(String[] strings) {
        super.onPostExecute(strings);

        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }

        /*
        if (param) {
            Intent currentTrip = new Intent(JourneyPlannerActivity.this, MapActivity.class);
            currentTrip.putExtra("routeSearch", true);
            JourneyPlannerActivity.this.startActivity(currentTrip);
        }
        else {
            Intent sugTrips = new Intent(JourneyPlannerActivity.this,
                    JourneyPlannerTripsActivity.class);
            JourneyPlannerActivity.this.startActivityForResult(sugTrips, 0);
        }
        */
    }

    private String[] getRoutesFromJson(String json) {
        String[] routes = new String[5];

        Log.d(LOG_TAG, "getRoutesFromJson "+json);

        return routes;
    }
}