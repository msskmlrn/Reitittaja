package com.devglyph.reitittaja.adapters;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.fragments.JourneyPlannerFragment;
import com.devglyph.reitittaja.models.Location;
import com.devglyph.reitittaja.network.ApiCalls;
import com.devglyph.reitittaja.network.LocationJsonParserUtil;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private final String LOG_TAG = PlacesAutoCompleteAdapter.class.getSimpleName();

    private JourneyPlannerFragment journeyPlannerFragment;
    private ArrayList<String> resultList;
    private Context mContext;

    public PlacesAutoCompleteAdapter(JourneyPlannerFragment journeyPlannerFragment, Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);

        Log.d(LOG_TAG, "constructor");

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
        Log.d(LOG_TAG, "getFilter");
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Log.d(LOG_TAG, "performFiltering");

                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results
                    Log.d(LOG_TAG, "Charconstraintsequence " + constraint);

                    //if the text in the search field matches the previously selected value, then there
                    //is no need to perform the search again, For example, if the start location
                    //has been chosen from the results and then the screen is rotated that
                    //causes the adapter to called again with the chosen value which is unneeded.
                    if (journeyPlannerFragment.getStartLocation() != null &&
                            journeyPlannerFragment.getStartLocation().toString() != null &&
                            constraint.toString().equals(journeyPlannerFragment.getStartLocation().toString())) {
                        Log.d(LOG_TAG, "returning, no search for start");
                        return filterResults;
                    }
                    else if (journeyPlannerFragment.getEndLocation() != null &&
                            journeyPlannerFragment.getEndLocation().toString() != null &&
                            constraint.toString().equals(journeyPlannerFragment.getEndLocation().toString())) {
                        Log.d(LOG_TAG, "returning, no search for end");
                        return filterResults;
                    }
                    //perform the search
                    else {
                        ArrayList<String> tempList = autocomplete(constraint.toString());
                        if (tempList != null && !tempList.isEmpty()) {
                            resultList = tempList;

                            // Assign the data to the FilterResults
                            filterResults.values = resultList;
                            filterResults.count = resultList.size();
                        }
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.d(LOG_TAG, "publishResults");
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

        String searchString = prepareSearchQuery(input);
        ApiCalls apiCalls = new ApiCalls();
        String resultString = apiCalls.performApiCall(searchString);
        ArrayList<Location> locationList = new LocationJsonParserUtil().getPlacesFromJson(resultString);

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

    private String prepareSearchQuery(String input) {
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

        return QUERY_BASE_URL + paramString;
    }
}