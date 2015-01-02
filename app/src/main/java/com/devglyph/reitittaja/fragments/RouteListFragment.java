package com.devglyph.reitittaja.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.adapters.RoutesAdapter;
import com.devglyph.reitittaja.models.Route;
import com.devglyph.reitittaja.models.RouteLeg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * A list fragment representing a list of Routes. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link RouteDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class RouteListFragment extends Fragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private final String LOG_TAG = RouteListFragment.class.getSimpleName();

    //the groupData map will hold the top level trip info
    private ArrayList<HashMap<String, String>> groupData = new ArrayList<HashMap<String, String>>();

    //the childData map will hold the leg info (icons, etc.)
    private ArrayList<ArrayList<HashMap<String, String>>> childData =
            new ArrayList<ArrayList<HashMap<String, String>>>();

    private ArrayList<Route> routes;
    private ExpandableListView eView;
    private TextView startPlace, endPlace;
    private Handler mHandler;
    private RoutesAdapter mRoutesAdapter;
    private String startPlaceName, endPlaceName;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RouteListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mHandler = new Handler();

        View v = inflater.inflate(R.layout.fragment_routes, null);
        eView = (ExpandableListView) v.findViewById(R.id.list_routes);

        startPlace = (TextView) v.findViewById(R.id.start_place);
        endPlace = (TextView) v.findViewById(R.id.end_place);

        mRoutesAdapter = new RoutesAdapter(childData, groupData, getActivity());
        eView.setAdapter(mRoutesAdapter);

        expandGroups();
        addOnGroupClickListeners();

        return v;
    }

    /**
     * Expand groups
     */
    private void expandGroups() {
        int groupCount = mRoutesAdapter.getGroupCount();

        for (int i = 0; i < groupCount; i++) {
            eView.expandGroup(i);
        }
    }

    /**
     * Add onGroupClickListeners to trips
     */
    private void addOnGroupClickListeners() {
        eView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            public boolean onGroupClick(ExpandableListView parent,
                                        View v, int groupPosition, long id) {

                Log.d(LOG_TAG, "onGroupClick, group "+ groupPosition);

                return true;
            }
        });
    }

    /**
     * Set the trip start and end details to the view
     */
    private void setTripStartAndEndDetailsToView() {
        final Runnable startPlaceRunnable = new Runnable() {
            public void run() {
                startPlace.setText(startPlaceName);
            }
        };
        mHandler.post(startPlaceRunnable);

        final Runnable endPlaceRunnable = new Runnable() {
            public void run() {
                endPlace.setText(endPlaceName);
            }
        };
        mHandler.post(endPlaceRunnable);
    }

    /**
     * Process the route information to fit the expandable list groups
     */
    private void processTripInfo() {
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);

            HashMap<String, String> currentGroupMap = new HashMap<String, String>();
            groupData.add(currentGroupMap);

            currentGroupMap.put("duration", calculateDurationInHHMM(route.getDuration()));
            currentGroupMap.put("st_time", parseTimeToHHMM(route.getStartLocation().getDepartureTime()));
            currentGroupMap.put("end_time", parseTimeToHHMM(route.getEndLocation().getArrivalTime()));

            childData.add(processTripLegInfo(route));
        }
    }

    private String parseTimeToHHMM(Date date) {
        SimpleDateFormat simpleDateFormat;

        simpleDateFormat = new SimpleDateFormat("HHmm");
        return simpleDateFormat.format(date);
    }

    /**
     * Calculate the length based on the distance value
     * @param distance
     * @return the rounded distance in meters with " m" appended to the value
     */
    public static String calculateTripLength(double distance) {
        long length = Math.round(distance);
        String stringLength = Long.toString(length);
        return stringLength + " m";
    }

    /**
     * Format the duration to X h Y min (Z sec) format.
     * @param duration in seconds
     * @return duration in string X h Y min format, if h and min > 0, else return Z sec
     */
    public static String calculateDurationInHHMM(double duration) {
        String hh = "";
        String mm = "";
        String ss = "";

        int h = 0;
        int m = 0;
        int s = 0;

        if (((int) duration / 3600) > 0) {
            h = ((int) duration / 3600);
            hh = h + " h ";
        }

        if (((int) duration % 3600) / 60 > 0) {
            m = (((int) duration % 3600) / 60);
            mm = m + " min ";
        }

        if (h == 0 && m == 0) {
            s = (int) duration; //the duration is already in seconds, so just cast it
            ss = s + " sec";
        }

        return hh + mm + ss;
    }


    /**
     * Go through the legs, format the line code/number to a readable format and add all the info
     * to the child map
     * @param route
     * @return list with map that holds the leg data
     */
    private ArrayList<HashMap<String, String>> processTripLegInfo(Route route) {
        ArrayList<HashMap<String, String>> children = new ArrayList<HashMap<String, String>>();
        String iconString;

        for (int j = 0; j < route.getLegs().size(); j++) {
            iconString = "";

            HashMap<String, String> currentChildMap = new HashMap<String, String>();
            children.add(currentChildMap);

            RouteLeg leg = route.getLegs().get(j);

            putLegDataToMap(currentChildMap, iconString, leg.getLineCode(), leg);
        }

        return children;
    }

    /**
     * Put leg data to the map
     * @param map
     * @param icon_str
     * @param cleanRouteId
     * @param leg
     */
    private void putLegDataToMap(HashMap<String, String> map, String icon_str,
                                 String cleanRouteId, RouteLeg leg) {

        map.put("icon", icon_str);
        map.put("routeId", cleanRouteId);
        map.put("leg_lenght", calculateTripLength(leg.getLength()));
        map.put("leg_duration", calculateDurationInHHMM(leg.getDuration()));
        map.put("leg_st_time", parseTimeToHHMM(leg.getStartLocation().getDepartureTime()));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }
    /*
        @Override
        public void onListItemClick(ListView listView, View view, int position, long id) {
            super.onListItemClick(listView, view, position, id);

            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
        }
    */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.

        /*getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
        */
    }

    private void setActivatedPosition(int position) {
        /*
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
        */
    }

    public void setRoutes(ArrayList<Route> routes) {
        this.routes = routes;
        updateAdapter();
    }

    private void updateAdapter() {
        processTripInfo();
        mRoutesAdapter.notifyDataSetChanged();
        setTripStartAndEndDetailsToView();
    }

    public void setStartPlaceName(String name) {
        this.startPlaceName = name;
    }

    public void setEndPlaceName(String name) {
        this.endPlaceName = name;
    }
}
