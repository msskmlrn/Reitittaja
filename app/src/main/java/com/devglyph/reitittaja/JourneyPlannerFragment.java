package com.devglyph.reitittaja;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link JourneyPlannerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link JourneyPlannerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JourneyPlannerFragment extends Fragment implements AdapterView.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mParam1;

    private OnFragmentInteractionListener mListener;

    private final String LOG_TAG = JourneyPlannerFragment.class.getSimpleName();

    private boolean mDeparture = true;
    private boolean[] mTransportationModeStates = new boolean[8];
    private static final int MODE_BUS = 0;
    private static final int MODE_TRAIN = 1;
    private static final int MODE_METRO = 2;
    private static final int MODE_TRAM = 3;
    private static final int MODE_ULINE = 4;
    private static final int MODE_SERVICE = 5;
    private static final int MODE_WALKING = 6;
    private static final int MODE_CYCLING = 7;

    private Button mTimeButton;
    private Button mDateButton;

    private AutoCompleteTextView mStartPlace;
    private AutoCompleteTextView mEndPlace;

    private int mHours = -1;
    private int mMinutes = -1;
    private int mMonth = -1;
    private int mDay = -1;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment JourneyPlannerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static JourneyPlannerFragment newInstance(int param1) {
        JourneyPlannerFragment fragment = new JourneyPlannerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public JourneyPlannerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_journey_planner, container, false);

        RadioGroup group = (RadioGroup) view.findViewById(R.id.radioGroupArrivalDeparture);
        createRadioButtonChangeListener(group);

        CheckBox box = (CheckBox) view.findViewById(R.id.checkBoxBus);
        createCheckBoxChangeListener(box);
        box.setChecked(true);
        box = (CheckBox) view.findViewById(R.id.checkBoxTrain);
        createCheckBoxChangeListener(box);
        box.setChecked(true);
        box = (CheckBox) view.findViewById(R.id.checkBoxMetro);
        createCheckBoxChangeListener(box);
        box.setChecked(true);
        box = (CheckBox) view.findViewById(R.id.checkBoxTram);
        createCheckBoxChangeListener(box);
        box.setChecked(true);
        box = (CheckBox) view.findViewById(R.id.checkBoxUline);
        createCheckBoxChangeListener(box);
        box.setChecked(true);
        box = (CheckBox) view.findViewById(R.id.checkBoxService);
        createCheckBoxChangeListener(box);
        box.setChecked(true);
        box = (CheckBox) view.findViewById(R.id.checkBoxOnlyWalking);
        createCheckBoxChangeListener(box);
        box = (CheckBox) view.findViewById(R.id.checkBoxOnlyCycling);
        createCheckBoxChangeListener(box);

        Button searchButton = (Button) view.findViewById(R.id.button_search);
        createSearchButtonClickListener(searchButton);

        mTimeButton = (Button) view.findViewById(R.id.whenTimeButton);
        createTimeButtonClickListener(mTimeButton);
        mTimeButton.setText(R.string.journey_planner_now_time);

        mDateButton = (Button) view.findViewById(R.id.whenDateButton);
        createDateButtonClickListener(mDateButton);
        mDateButton.setText(R.string.journey_planner_now_date);

        mStartPlace = (AutoCompleteTextView) view.findViewById(R.id.start_place);
        mStartPlace.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.list_item));
        //createOnItemClickListener(this,);
        //createStartPlaceListener(mStartPlace);
        //mStartPlace.setOnClickListener();

        mEndPlace = (AutoCompleteTextView) view.findViewById(R.id.end_place);
        //createEndPlaceListener(mEndPlace);
        mEndPlace.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.list_item));
        //mEndPlace.setOnClickListener();

        return view;
    }

    private void createOnItemClickListener(AdapterView<?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private void createRadioButtonChangeListener(RadioGroup group) {
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                // Check which radio button was clicked
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.radioButtonDepartureTime:
                        mDeparture = true;
                        break;
                    case R.id.radioButtonArrivalTime:
                        mDeparture = false;
                        break;
                }
            }
        });
    }

    private void createCheckBoxChangeListener(CheckBox checkBox) {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //RelativeLayout layout = (RelativeLayout) buttonView.getParent().getParent();
                RelativeLayout layout = (RelativeLayout) buttonView.getParent();

                int id = buttonView.getId();
                if (id == R.id.checkBoxBus || id == R.id.checkBoxTrain || id == R.id.checkBoxMetro
                        || id == R.id.checkBoxTram || id == R.id.checkBoxUline
                        || id == R.id.checkBoxService) {

                    //if one of the above transportation modes was just checked then uncheck
                    // only walking and only cycling
                    if (isChecked) {
                        setWalkingAndCycling(layout, false);
                    }
                }
                else if (id == R.id.checkBoxOnlyWalking) {
                    //if only walking was just checked then uncheck all others
                    if (isChecked) {
                        checkOrUncheckOthers(layout, R.id.checkBoxOnlyWalking, false);
                        markItem(R.id.checkBoxOnlyCycling, false, layout);
                    }
                    //only walking was just unchecked so check all others except only cycling
                    else {
                        checkOrUncheckOthers(layout, R.id.checkBoxOnlyWalking, true);
                    }
                }
                else if (id == R.id.checkBoxOnlyCycling) {
                    //if only cycling was just checked then uncheck all others
                    if (isChecked) {
                        checkOrUncheckOthers(layout, R.id.checkBoxOnlyCycling, false);
                        markItem(R.id.checkBoxOnlyWalking, false, layout);
                    }
                    //only walking was just unchecked so check all others except only walking
                    else {
                        checkOrUncheckOthers(layout, R.id.checkBoxOnlyCycling, true);
                    }
                }
            }
        });
    }

    private void getTransportationModeChoices(RelativeLayout view) {
        CheckBox box = (CheckBox) view.findViewById(R.id.checkBoxBus);
        setTransportationModeParameter(MODE_BUS, box.isChecked());

        box = (CheckBox) view.findViewById(R.id.checkBoxTrain);
        setTransportationModeParameter(MODE_TRAIN, box.isChecked());

        box = (CheckBox) view.findViewById(R.id.checkBoxMetro);
        setTransportationModeParameter(MODE_METRO, box.isChecked());

        box = (CheckBox) view.findViewById(R.id.checkBoxTram);
        setTransportationModeParameter(MODE_TRAM, box.isChecked());

        box = (CheckBox) view.findViewById(R.id.checkBoxUline);
        setTransportationModeParameter(MODE_ULINE, box.isChecked());

        box = (CheckBox) view.findViewById(R.id.checkBoxService);
        setTransportationModeParameter(MODE_SERVICE, box.isChecked());

        box = (CheckBox) view.findViewById(R.id.checkBoxOnlyWalking);
        setTransportationModeParameter(MODE_WALKING, box.isChecked());

        box = (CheckBox) view.findViewById(R.id.checkBoxOnlyCycling);
        setTransportationModeParameter(MODE_CYCLING, box.isChecked());
    }

    private void checkOrUncheckOthers(RelativeLayout layout, int untouchable, boolean value) {
        if (R.id.checkBoxBus != untouchable) {
            markItem(R.id.checkBoxBus, value, layout);
        }
        if (R.id.checkBoxTrain != untouchable) {
            markItem(R.id.checkBoxTrain, value, layout);
        }
        if (R.id.checkBoxMetro != untouchable) {
            markItem(R.id.checkBoxMetro, value, layout);
        }
        if (R.id.checkBoxTram != untouchable) {
            markItem(R.id.checkBoxTram, value, layout);
        }
        if (R.id.checkBoxUline != untouchable) {
            markItem(R.id.checkBoxUline, value, layout);
        }
        if (R.id.checkBoxService != untouchable) {
            markItem(R.id.checkBoxService, value, layout);
        }
    }

    private void setWalkingAndCycling(RelativeLayout layout, boolean value) {
        markItem(R.id.checkBoxOnlyCycling, value, layout);
        markItem(R.id.checkBoxOnlyWalking, value, layout);
    }

    private void markItem(int item, boolean value, RelativeLayout layout) {
        CheckBox box = (CheckBox) layout.findViewById(item);
        box.setChecked(value);
    }

    private void setTransportationModeParameter(int mode, boolean value) {
        mTransportationModeStates[mode] = value;
    }

    private void createSearchButtonClickListener(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.button_search) {
                    Log.d(LOG_TAG, "search");
                }
            }
        });
    }

    private void createTimeButtonClickListener(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.whenTimeButton) {
                    Log.d(LOG_TAG, "time");
                    DialogFragment newFragment = new TimePickerFragment();
                    newFragment.setTargetFragment(JourneyPlannerFragment.this, 0);
                    newFragment.show(getFragmentManager(), "timePicker");
                }
            }
        });
    }

    private void createDateButtonClickListener(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "date");
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.setTargetFragment(JourneyPlannerFragment.this, 0);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            JourneyPlannerFragment target = (JourneyPlannerFragment) getTargetFragment();
            target.setTimeButtonTime(hourOfDay, minute);
        }
    }

    private String addLeadingZero(int value) {
        Log.d(LOG_TAG, "value " + value);
        if (value < 10) {
            return "0" + value;
        }
        else {
            return "" + value;
        }
    }

    private void setTimeButtonTime(int hour, int minute) {
        String time = addLeadingZero(hour) + ":" + addLeadingZero(minute);
        mTimeButton.setText(time);
        mHours = hour;
        mMinutes = minute;
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            JourneyPlannerFragment target = (JourneyPlannerFragment) getTargetFragment();
            target.setDateButtonDate(year, month, day);
        }
    }

    private void setDateButtonDate(int dYear, int dMonth, int dDay) {
        dMonth = dMonth + 1; //months start from 0
        String date = addLeadingZero(dDay) + "." + addLeadingZero(dMonth) + ".";
        mDateButton.setText(date);
        mDay = dDay;
        mMonth = dMonth;
    }

    private void resetTimeAndDate() {
        Log.d(LOG_TAG, "reset time and date");
        mDateButton.setText(R.string.journey_planner_now_date);
        mTimeButton.setText(R.string.journey_planner_now_time);
        mHours = -1;
        mMinutes = -1;
        mDay = -1;
        mMonth = -1;
    }

    private void createStartPlaceListener(EditText text) {
        text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.d(LOG_TAG, "start place");
                    onButtonPressed("start");
                }

            }
        });
    }

    private void createEndPlaceListener(EditText text) {
        text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.d(LOG_TAG, "end place");
                    onButtonPressed("end");
                }
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String string) {
        if (mListener != null) {
            mListener.onFragmentInteraction(string);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String string);
    }



    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
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
                        Log.d(LOG_TAG, "Charconstraintsequence "+constraint);

                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();

                        /*
                        ArrayList<String> list = autocomplete(constraint.toString());

                        if (list != null && !resultList.isEmpty()) {
                            resultList = list;

                            // Assign the data to the FilterResults
                            filterResults.values = resultList;
                            filterResults.count = resultList.size();
                        }
                        */
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }};
            return filter;
        }
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
            nameValuePairs.add(new BasicNameValuePair(USERNAME_PARAM, getString(R.string.reittiopas_username)));
            nameValuePairs.add(new BasicNameValuePair(PASSWORD_PARAM, getString(R.string.reittiopas_password)));
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
     * @return CustomLocaiton object
     */
    private Coordinates readCoordinates(String coordinates) {
        double latitude = 0; //y
        double longitude = 0; //x

        try {
            longitude = Double.parseDouble(coordinates.substring(0, coordinates.indexOf(",")));
            latitude = Double.parseDouble(coordinates.substring(coordinates.indexOf(",") + 1));
        }
        catch (Exception e) {
        }

        return new Coordinates(latitude, longitude);
    }

}