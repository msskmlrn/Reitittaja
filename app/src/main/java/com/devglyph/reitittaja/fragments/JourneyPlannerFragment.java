package com.devglyph.reitittaja.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.activities.MainActivity;
import com.devglyph.reitittaja.activities.RouteListActivity;
import com.devglyph.reitittaja.adapters.PlacesAutoCompleteAdapter;
import com.devglyph.reitittaja.models.Location;
import com.devglyph.reitittaja.models.Route;
import com.devglyph.reitittaja.services.ReverseGeocodeService;
import com.devglyph.reitittaja.services.RouteSearchService;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link JourneyPlannerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link JourneyPlannerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JourneyPlannerFragment extends Fragment implements FavoriteDialogFragment.OnFavoriteChosenListener {

    private static final String SECTION_PARAM = "param1";

    public  final static String SER_KEY = "com.devglyph.routes";

    private OnFragmentInteractionListener mListener;

    private final String LOG_TAG = JourneyPlannerFragment.class.getSimpleName();

    private View mView;

    private Button mTimeButton;
    private Button mDateButton;

    private ImageButton mFromFavoriteButton;
    private ImageButton mToFavoriteButton;

    private AutoCompleteTextView mStartPlace;
    private AutoCompleteTextView mEndPlace;

    private RadioGroup departureGroup;
    private CheckBox busBox, trainBox, metroBox, tramBox, ulineBox, serviceBox, walkingBox, cyclingBox;

    //TODO handle progress dialog when the orientation changes
    private ProgressDialog progressDialog;

    private boolean mDeparture = true;

    private int mHours = -1;
    private int mMinutes = -1;
    private int mMonth = -1;
    private int mDay = -1;
    private int mYear = -1;

    private Location startLocation;
    private Location endLocation;

    private String startLocationName;
    private String endLocationName;

    private ArrayList<Route> routes;
    private ArrayList<Location> locationList = new ArrayList<>();

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
        args.putInt(SECTION_PARAM, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public JourneyPlannerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        //create intent filters and register receivers
        IntentFilter routeSearchFilter = new IntentFilter();
        routeSearchFilter.addAction(RouteSearchService.ROUTE_SEARCH_DONE);

        IntentFilter reverseGeocodeFilter = new IntentFilter();
        reverseGeocodeFilter.addAction(ReverseGeocodeService.REVERSE_GEOCODING_DONE);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(routeSearchReceiver,
                routeSearchFilter);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(reverseGeocodeReceiver,
                reverseGeocodeFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(LOG_TAG, "onStop");

        //unregister the receiver so that they will not leak
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(routeSearchReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(reverseGeocodeReceiver);

        //dismiss the dialog if is still being shown so that it will not leak
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_journey_planner, container, false);

        departureGroup = (RadioGroup) mView.findViewById(R.id.radioGroupArrivalDeparture);
        createRadioButtonChangeListener(departureGroup);

        initializeTransportationModeBoxes();
        initializeButtons();
        initializeTextFields();

        Log.d(LOG_TAG, "onCreateView");

        return mView;
    }

    /**
     * Initialize the transportation mode boxes
     */
    private void initializeTransportationModeBoxes() {
        busBox = (CheckBox) mView.findViewById(R.id.checkBoxBus);
        createCheckBoxChangeListener(busBox);
        busBox.setChecked(true);
        trainBox = (CheckBox) mView.findViewById(R.id.checkBoxTrain);
        createCheckBoxChangeListener(trainBox);
        trainBox.setChecked(true);
        metroBox = (CheckBox) mView.findViewById(R.id.checkBoxMetro);
        createCheckBoxChangeListener(metroBox);
        metroBox.setChecked(true);
        tramBox = (CheckBox) mView.findViewById(R.id.checkBoxTram);
        createCheckBoxChangeListener(tramBox);
        tramBox.setChecked(true);
        ulineBox = (CheckBox) mView.findViewById(R.id.checkBoxUline);
        createCheckBoxChangeListener(ulineBox);
        ulineBox.setChecked(true);
        serviceBox = (CheckBox) mView.findViewById(R.id.checkBoxService);
        createCheckBoxChangeListener(serviceBox);
        serviceBox.setChecked(true);
        walkingBox = (CheckBox) mView.findViewById(R.id.checkBoxOnlyWalking);
        createCheckBoxChangeListener(walkingBox);
        cyclingBox = (CheckBox) mView.findViewById(R.id.checkBoxOnlyCycling);
        createCheckBoxChangeListener(cyclingBox);
    }

    /**
     * Initialize the buttons
     */
    private void initializeButtons() {
        Button searchButton = (Button) mView.findViewById(R.id.button_search);
        createSearchButtonClickListener(searchButton);

        mTimeButton = (Button) mView.findViewById(R.id.whenTimeButton);
        createTimeButtonClickListener(mTimeButton);
        mTimeButton.setText(R.string.journey_planner_now_time);

        mDateButton = (Button) mView.findViewById(R.id.whenDateButton);
        createDateButtonClickListener(mDateButton);
        mDateButton.setText(R.string.journey_planner_now_date);

        mFromFavoriteButton = (ImageButton) mView.findViewById(R.id.from_favorites);
        createFavoriteButtonClickListener(mFromFavoriteButton);

        mToFavoriteButton = (ImageButton) mView.findViewById(R.id.to_favorites);
        createFavoriteButtonClickListener(mToFavoriteButton);
    }

    /**
     * Initialize the text fields
     */
    private void initializeTextFields() {
        mStartPlace = (AutoCompleteTextView) mView.findViewById(R.id.start_place);
        mStartPlace.setAdapter(new PlacesAutoCompleteAdapter(this, getActivity(), R.layout.list_item));
        createOnItemClickListener(mStartPlace);

        mEndPlace = (AutoCompleteTextView) mView.findViewById(R.id.end_place);
        mEndPlace.setAdapter(new PlacesAutoCompleteAdapter(this, getActivity(), R.layout.list_item));
        createOnItemClickListener(mEndPlace);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(SECTION_PARAM));

        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFavoriteChosenListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d(LOG_TAG, "onActivityCreated");

        if (savedInstanceState != null) {
            mHours = savedInstanceState.getInt("hours");
            mMinutes = savedInstanceState.getInt("minutes");
            mYear = savedInstanceState.getInt("years");
            mMonth = savedInstanceState.getInt("months");
            mDay = savedInstanceState.getInt("days");

            //check if the time and date values have to be reset
            if (compareCurrentTimeToGivenTime(mMinutes, mHours)) {
                setTimeButtonTime(mHours, mMinutes);
            }
            if (compareCurrentDateToGivenDate(mDay, mMonth, mYear)) {
                setDateButtonDate(mYear, mMonth, mDay);
            }

            startLocation = savedInstanceState.getParcelable("startLocation");
            endLocation = savedInstanceState.getParcelable("endLocation");
            startLocationName = savedInstanceState.getString("startLocationName");
            endLocationName = savedInstanceState.getString("endLocationName");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(LOG_TAG, "onSaveInstanceState");

        outState.putInt("minutes", mMinutes);
        outState.putInt("hours", mHours);
        outState.putInt("days", mDay);
        outState.putInt("months", mMonth - 1);
        outState.putInt("years", mYear);
        outState.putParcelable("startLocation", startLocation);
        outState.putParcelable("endLocation", endLocation);
        outState.putString("startLocationName", mStartPlace.getText().toString());
        outState.putString("endLocationName", mEndPlace.getText().toString());
    }

    /**
     * Create a listener for the autocomplete list item choices
     * @param autoCompleteTextView
     */
    private void createOnItemClickListener(final AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (autoCompleteTextView.getId() == R.id.start_place) {
                    startLocation = getLocationList().get(position);
                } else if (autoCompleteTextView.getId() == R.id.end_place) {
                    endLocation = getLocationList().get(position);
                }

                //close the soft keyboard
                InputMethodManager in = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(autoCompleteTextView.getWindowToken(), 0);
            }
        });
    }

    /**
     * Create a listener for the radio button choices
     * @param group
     */
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

    /**
     * Create a listener for the checkbox clicks
     * @param checkBox
     */
    private void createCheckBoxChangeListener(CheckBox checkBox) {
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //RelativeLayout layout = (RelativeLayout) buttonView.getParent();
                TableLayout layout = (TableLayout) buttonView.getParent().getParent();

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

    /**
     * Set the given value to all boxes except the untouchable box
     * @param layout
     * @param untouchable
     * @param value
     */
    private void checkOrUncheckOthers(TableLayout layout, int untouchable, boolean value) {
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

    /**
     * Set walking and cycling as checked
     * @param layout
     * @param value
     */
    private void setWalkingAndCycling(TableLayout layout, boolean value) {
        markItem(R.id.checkBoxOnlyCycling, value, layout);
        markItem(R.id.checkBoxOnlyWalking, value, layout);
    }

    /**
     * Set the chosen box as checked
     * @param item
     * @param value
     * @param layout
     */
    private void markItem(int item, boolean value, TableLayout layout) {
        CheckBox box = (CheckBox) layout.findViewById(item);
        box.setChecked(value);
    }

    /**
     * Create an onClickListener for the search button
     * @param button
     */
    private void createSearchButtonClickListener(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.button_search) {

                    //validate that the start and end location have been chosen
                    if (startLocation == null) {
                        String message = "Choose a start location";
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        return;
                    } else if (endLocation == null) {
                        String message = "Choose an end location";
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    prepareSearchQuery();
                }
            }
        });
    }

    /**
     * Create an onClickListener for the favorite buttons
     * @param button
     */
    private void createFavoriteButtonClickListener(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = "";
                String description = "";
                double lat = -1;
                double lon = -1;
                boolean start = true;

                if (v.getId() == R.id.from_favorites) {
                    Log.d(LOG_TAG, "from");
                    name = mStartPlace.getText().toString();

                    //check if the start location has been selected
                    if (startLocation != null) {
                        lat = startLocation.getCoords().getLatitude();
                        lon = startLocation.getCoords().getLongitude();
                        description = startLocation.getDescription();
                    }
                }
                else if (v.getId() == R.id.to_favorites) {
                    Log.d(LOG_TAG, "to");
                    name = mEndPlace.getText().toString();
                    start = false;

                    //check if the end location has been selected
                    if (endLocation != null) {
                        lat = endLocation.getCoords().getLatitude();
                        lon = endLocation.getCoords().getLongitude();
                        description = endLocation.getDescription();
                    }
                }

                FragmentManager manager = getFragmentManager();
                FavoriteDialogFragment dialog = FavoriteDialogFragment.newInstance(
                        name, description, lat, lon, start);
                dialog.show(manager, "dialog");
            }
        });
    }

    /**
     * Create an onClickListener for the time button
     * @param button
     */
    private void createTimeButtonClickListener(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.whenTimeButton) {
                    DialogFragment newFragment = new TimePickerFragment();
                    newFragment.setTargetFragment(JourneyPlannerFragment.this, 0);
                    newFragment.show(getFragmentManager(), "timePicker");
                }
            }
        });
    }

    /**
     * Create an onClickListener for the date button
     * @param button
     */
    private void createDateButtonClickListener(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.setTargetFragment(JourneyPlannerFragment.this, 0);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
    }

    @Override
    public void onFavoriteChosen(Location location, boolean startPlace) {
        Log.d(LOG_TAG, "onFavoriteSaved");
        Log.d(LOG_TAG, "onFavoriteSaved " + location.getName());
        Log.d(LOG_TAG, "onFavoriteSaved " + location.getDescription());
        Log.d(LOG_TAG, "onFavoriteSaved " + location.getCoords().getLatitude() + ", " + location.getCoords().getLongitude());
        Log.d(LOG_TAG, "onFavoriteSaved " + location.isFavorite());
    }

    /**
     * Show a dialog from which a time can be chosen
     */
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

    /**
     * Add a leading zero the to the number if it is < 10
     * @param value
     * @return formatted number
     */
    private String addLeadingZero(int value) {
        if (value < 10) {
            return "0" + value;
        }
        else {
            return "" + value;
        }
    }

    /**
     * Set the time on the time button
     * @param hour
     * @param minute
     */
    private void setTimeButtonTime(int hour, int minute) {
        String time = addLeadingZero(hour) + ":" + addLeadingZero(minute);
        mTimeButton.setText(time);
        mHours = hour;
        mMinutes = minute;
    }

    /**
     * Show a dialog from which a date can be chosen
     */
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
            JourneyPlannerFragment target = (JourneyPlannerFragment) getTargetFragment();
            target.setDateButtonDate(year, month, day);
        }
    }

    /**
     * Set the text on the date button
     * @param dYear
     * @param dMonth
     * @param dDay
     */
    private void setDateButtonDate(int dYear, int dMonth, int dDay) {
        dMonth = dMonth + 1; //months start from 0
        String date = addLeadingZero(dDay) + "." + addLeadingZero(dMonth) + ".";
        mDateButton.setText(date);
        mDay = dDay;
        mMonth = dMonth;
        mYear = dYear;
    }

    /**
     * Find out if the given time matches the current time
     * @param minutes
     * @param hours
     * @return true if the times match, else false
     */
    private boolean compareCurrentTimeToGivenTime(int minutes, int hours) {
        Calendar cal = Calendar.getInstance();
        int currentHours = cal.get(Calendar.HOUR_OF_DAY);
        int currentMinutes = cal.get(Calendar.MINUTE);

        if (hours == currentHours && minutes == currentMinutes) {
            return true;
        }

        return false;
    }

    /**
     * Find out if the given date matches the current date
     * @param days
     * @param months
     * @param years
     * @return true if the dates match, else false
     */
    private boolean compareCurrentDateToGivenDate(int days, int months, int years) {
        Calendar cal = Calendar.getInstance();
        int currentDays = cal.get(Calendar.DATE);
        int currentMonths = cal.get(Calendar.MONTH);
        int currentYears = cal.get(Calendar.YEAR);

        if (currentDays == days && currentMonths == months && currentYears == years) {
            return true;
        }

        return false;
    }

    /**
     * Prepare the route search query by formatting the query parameters
     */
    private void prepareSearchQuery() {

        final String QUERY_BASE_URL =
                "http://api.reittiopas.fi/hsl/prod/?";
        final String QUERY_PARAM = "request";
        final String FORMAT_PARAM = "format";
        final String USERNAME_PARAM = "user";
        final String PASSWORD_PARAM = "pass";
        final String COORDINATE_OUTPUT_PARAM = "epsg_out";
        final String COORDINATE_INPUT_PARAM = "epsg_in";
        final String FROM_PARAM = "from";
        final String TO_PARAM = "to";
        final String DATE_PARAM = "date";
        final String TIME_PARAM = "time";
        final String TIMETYPE_PARAM = "timetype";
        final String TRANSPORT_TYPES_PARAM = "transport_types";
        final String OPTIMIZE_PARAM = "optimize";
        final String CHANGE_MARGIN_PARAM = "change_margin";
        final String WALK_SPEED_PARAM = "walk_speed";
        final String SHOW_PARAM = "show";
        final String DETAIL_PARAM = "detail";

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(QUERY_PARAM, "route"));
        nameValuePairs.add(new BasicNameValuePair(FORMAT_PARAM, "json"));
        nameValuePairs.add(new BasicNameValuePair(USERNAME_PARAM, getString(R.string.reittiopas_username)));
        nameValuePairs.add(new BasicNameValuePair(PASSWORD_PARAM, getString(R.string.reittiopas_password)));
        nameValuePairs.add(new BasicNameValuePair(COORDINATE_OUTPUT_PARAM, "wgs84"));
        nameValuePairs.add(new BasicNameValuePair(COORDINATE_INPUT_PARAM, "wgs84"));

        String startCoords = startLocation.getCoords().getLongitude() + "," + startLocation.getCoords().getLatitude();
        String endCoords = endLocation.getCoords().getLongitude() + "," + endLocation.getCoords().getLatitude();
        nameValuePairs.add(new BasicNameValuePair(FROM_PARAM, startCoords));
        nameValuePairs.add(new BasicNameValuePair(TO_PARAM, endCoords));

        String modes = getTransportTypes();

        nameValuePairs.add(new BasicNameValuePair(TRANSPORT_TYPES_PARAM, modes));
        if (modes.equals("cycle")) {
            //TODO cycle route search
            String message = "Cycle route search not supported yet";
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return;
        }

        nameValuePairs.add(new BasicNameValuePair(DATE_PARAM, getDateParameter()));
        nameValuePairs.add(new BasicNameValuePair(TIME_PARAM, getTimeParameter()));
        nameValuePairs.add(new BasicNameValuePair(TIMETYPE_PARAM, getTimeType()));
        nameValuePairs.add(new BasicNameValuePair(SHOW_PARAM, "5"));
        nameValuePairs.add(new BasicNameValuePair(DETAIL_PARAM, "full"));

        //TODO implement walk speed, change margin and optimize choices

        String paramString = URLEncodedUtils.format(nameValuePairs, "utf-8");

        //launch the route search task
        startRouteSearchService(QUERY_BASE_URL + paramString);
    }

    /**
     * Try to start the route search service
     * @param query search parameters
     */
    private void startRouteSearchService(String query) {
        //try to start the route search task
        try {
            URL url = new URL(query);
            Log.d(LOG_TAG, "launching route search "+url);

            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Searching routes");
            progressDialog.show();

            RouteSearchService.startRouteSearch(getActivity(), url.toString());
        }
        catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Format the chosen query type for the search request
     * @return a string formatted withe the type information
     */
    private String getTimeType() {
        String timeType;

        if (mDeparture) {
            timeType = "" + R.string.journey_planner_start_time;
            timeType = timeType.toLowerCase(); //format the parameter for the query
        }
        else {
            timeType = "" + R.string.journey_planner_arrival_time;
            timeType.toLowerCase(); //format the parameter for the query
        }

        return timeType;
    }

    /**
     * Format the chosen date for the search query
     * @return a string formatted with the date information
     */
    private String getDateParameter() {
        String date;
        Calendar cal = Calendar.getInstance();
        Date dDate;

        //if the date parameter has not been changed from "today", then use the current date
        if (mDay == -1 || mMonth == -1 || mYear == -1) {
            dDate = cal.getTime();
        }
        else { //parse the chosen date
            cal.set(Calendar.YEAR, mYear);
            cal.set(Calendar.MONTH, mMonth);
            cal.set(Calendar.DATE, mDay);
            dDate = cal.getTime();
        }

        //format the date to yyyymmdd
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        date = simpleDateFormat.format(dDate);

        return date;
    }

    /**
     * Format the chosen time for the search query
     * @return a string formatted with the time information
     */
    private String getTimeParameter() {
        Calendar cal;

        //if the date parameter has not been changed from "now", then use the current time
        if (mMinutes == -1 || mHours == -1) {
            cal = Calendar.getInstance();
        }
        else { //parse the chosen time
            cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, mHours);
            cal.set(Calendar.MINUTE, mMinutes);
        }

        //format the time to hhmm
        Date dTime = cal.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmm");
        String time = simpleDateFormat.format(dTime);

        Log.d(LOG_TAG, "time set "+time);

        return time;
    }

    /**
     * Format the chosen transportation types for the search query
     * @return a string formatted with transportation types
     */
    private String getTransportTypes() {
        String modes = "";

        CheckBox box = (CheckBox) mView.findViewById(R.id.checkBoxBus);
        if (box.isChecked()) {
            modes = modes + R.string.bus;
        }

        box = (CheckBox) mView.findViewById(R.id.checkBoxTrain);
        if (box.isChecked()) {
            modes = modes + "|" + R.string.train;
        }

        box = (CheckBox) mView.findViewById(R.id.checkBoxMetro);
        if (box.isChecked()) {
            modes = modes + "|" + R.string.metro;
        }

        box = (CheckBox) mView.findViewById(R.id.checkBoxTram);
        if (box.isChecked()) {
            modes = modes + "|" + R.string.tram;
        }

        box = (CheckBox) mView.findViewById(R.id.checkBoxUline);
        if (box.isChecked()) {
            modes = modes + "|" + R.string.uline;
        }

        box = (CheckBox) mView.findViewById(R.id.checkBoxService);
        if (box.isChecked()) {
            modes = modes + "|" + R.string.service;
        }

        box = (CheckBox) mView.findViewById(R.id.checkBoxOnlyWalking);
        if (box.isChecked()) {
            return "" + R.string.walk;
        }

        box = (CheckBox) mView.findViewById(R.id.checkBoxOnlyCycling);
        if (box.isChecked()) {
            return "" + R.string.cycle;
        }

        return modes;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String string) {
        if (mListener != null) {
            mListener.onFragmentInteraction(string);
        }
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

    public void placeChosenFromFavorites(Location location, boolean clickForStartPlace) {
        Log.d(LOG_TAG, "placeChosenFromFavorites");
        if (clickForStartPlace) {
            startLocation = location;
            mStartPlace.setText(location.getName());
        }
        else {
            endLocation = location;
            mEndPlace.setText(location.getName());
        }
    }

    //getters and setters

    public ArrayList<Location> getLocationList() {
        return locationList;
    }

    public void setLocationList(ArrayList<Location> locationList) {
        this.locationList = locationList;
    }

    public String getStartLocationName() {
        return startLocationName;
    }

    public String getEndLocationName() {
        return endLocationName;
    }

    /**
     * Launch the route list activity only if the app is on the foreground.
     */
    private void launchRouteListActivity() {
        //check if the fragment is visible
        if (this.isVisible()) {
            //launch an activity to show the routes if any routes were found
            if (routes != null && !routes.isEmpty()) {
                Intent launchIntent = new Intent(getActivity(), RouteListActivity.class);

                //add the routes to the intent as extras
                launchIntent.putParcelableArrayListExtra(RouteSearchService.SER_KEY, routes);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
            }
            else {
                String message = "Please try again.";
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Receive broadcast from the route search service when the task finishes
     */
    private BroadcastReceiver routeSearchReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "routeSearchReceiver");

            if (intent != null && intent.hasExtra("routes")) {
                Log.d(LOG_TAG, "routeSearchReceiver, found routes");

                routes = intent.getParcelableArrayListExtra("routes");

                //start the reverse geocode service to fill in missing location information
                ReverseGeocodeService.startReverseGeocoding(getActivity(), routes);
            }
        }
    };

    /**
     * Receive broadcast from the reverse geocode service when the task finishes
     */
    private BroadcastReceiver reverseGeocodeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "reverseGeocodeReceiver");

            if (intent != null && intent.hasExtra(ReverseGeocodeService.REVERSE_GEOCODED_ROUTES_EXTRA)) {
                Log.d(LOG_TAG, "reverseGeocodeReceiver, found routes");

                //if the progress dialog is still being shown, dismiss it
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                routes = intent.getParcelableArrayListExtra(
                        ReverseGeocodeService.REVERSE_GEOCODED_ROUTES_EXTRA);

                launchRouteListActivity();
            }
        }
    };
}
