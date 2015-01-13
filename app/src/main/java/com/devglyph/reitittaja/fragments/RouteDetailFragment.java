package com.devglyph.reitittaja.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.Util;
import com.devglyph.reitittaja.activities.MapsActivity;
import com.devglyph.reitittaja.models.Route;
import com.devglyph.reitittaja.models.RouteLeg;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A fragment representing a single Route detail screen.
 * This fragment is either contained in a {@link com.devglyph.reitittaja.activities.RouteListActivity}
 * in two-pane mode (on tablets) or a {@link com.devglyph.reitittaja.activities.RouteDetailActivity}
 * on handsets.
 */
public class RouteDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public  final static String ROUTE_DETAIL_KEY = "com.devglyph.route_details";
    private final String LOG_TAG = RouteDetailFragment.class.getSimpleName();

    //the route whose details are to be shown
    private Route mRoute;

    private ArrayList<HashMap<String,String>> list =  new ArrayList<HashMap<String,String>>();

    private TextView startTime, startPlace, endTime, endPlace, totalTime, tripDistance;
    private SimpleAdapter mAdapter;
    private Handler mHandler;
    private ListView mView;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RouteDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        if (getArguments().containsKey(ROUTE_DETAIL_KEY)) {
            mRoute = getArguments().getParcelable(ROUTE_DETAIL_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_route_detail, container, false);

        // Setting up the view
        startTime = (TextView) rootView.findViewById(R.id.start_time);
        startPlace = (TextView) rootView.findViewById(R.id.start_place);
        endTime = (TextView) rootView.findViewById(R.id.end_time);
        endPlace = (TextView) rootView.findViewById(R.id.end_place);
        totalTime = (TextView) rootView.findViewById(R.id.total_time);
        tripDistance = (TextView) rootView.findViewById(R.id.trip_distance);

        /*
        // Show the dummy content as text in a TextView.
        if (mRoute != null) {
            ((TextView) rootView.findViewById(R.id.route_detail)).setText
                    (mRoute.getStartLocation().getName() + " - " + mRoute.getEndLocation().getName());
        }
        */

        if (mRoute != null) {
            processRoute(mRoute);
            setTripStartAndEndDetailsToView();
        }

        // Setting up the Adapter for ListView
        mAdapter = new SimpleAdapter(getActivity(), list, R.layout.leg_item,
                new String[] {"stop","time","icon","mode", "legDistance", "legDuration"},
                new int[] {R.id.text_stop, R.id.text_time, R.id.icon, R.id.text_mode,
                        R.id.text_leg_distance, R.id.text_leg_duration});


        mView = (ListView) rootView.findViewById(R.id.listview);
        mView.setAdapter(mAdapter);

        addOnItemClickListeners();

        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //updateAdapter();
    }

    /**
     * Add onGroupClickListeners to trips
     */
    private void addOnItemClickListeners() {
        mView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "onItemClick, position "+position);
                startMaps(position);
            }
        });
    }

    private void startMaps(int position) {
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        intent.putExtra("route", mRoute);
        intent.putExtra("leg", position);
        startActivity(intent);
    }

    /**
     * Set the trip information to the fields
     */
    private void setTripStartAndEndDetailsToView() {
        final Runnable startTimeRunnable = new Runnable() {
            public void run() {
                startTime.setText(Util.parseDate(Util.DATE_FORMAT_TIME, mRoute.getStartLocation().getDepartureTime().getTime()));
            }
        };
        mHandler.post(startTimeRunnable);
        final Runnable startPlaceRunnable = new Runnable() {
            public void run() {
                startPlace.setText(mRoute.getStartLocation().getName());
            }
        };
        mHandler.post(startPlaceRunnable);
        final Runnable endTimeRunnable = new Runnable() {
            public void run() {
                endTime.setText(Util.parseDate(Util.DATE_FORMAT_TIME, mRoute.getEndLocation().getArrivalTime().getTime()));
            }
        };
        mHandler.post(endTimeRunnable);
        final Runnable endPlaceRunnable = new Runnable() {
            public void run() {
                endPlace.setText(mRoute.getEndLocation().getName());
            }
        };
        mHandler.post(endPlaceRunnable);
        final Runnable totalTimeRunnable = new Runnable() {
            public void run() {
                totalTime.setText(Util.convertSecondsToHHmmss(mRoute.getDuration()));
            }
        };
        mHandler.post(totalTimeRunnable);
    }

    private void processRoute(Route route) {

        //Setting Leg details to the list
        HashMap<String,String> temp;
        ArrayList<RouteLeg> legs = route.getLegs();
        String iconString;
        String lineCode;
        final String totalTripDistanceString;
        double distance = 0;
        double legDistance = 0;
        for (int i = 0; i < legs.size(); i++) {
            iconString = "";
            lineCode = "";

            RouteLeg leg = route.getLegs().get(i);

            legDistance = leg.getLength();
            distance = distance + legDistance;

            temp = new HashMap<String,String>();
            temp.put("stop", leg.getStartLocation().getName());
            temp.put("legDistance", Util.roundDistance(legDistance));
            temp.put("time", Util.parseDate(Util.DATE_FORMAT_TIME, leg.getStartLocation().getDepartureTime().getTime()));
            temp.put("legDuration", Util.convertSecondsToHHmmss(leg.getDuration()));

            //iconString = Library.getLegIcon(Integer.parseInt(legs.get(i).getMode().trim()), true);
            iconString = "";

            if (leg.getLineCode() != null) {
                lineCode = leg.getLineCode();
            }
            temp.put("icon", iconString);
            temp.put("mode", lineCode);
            list.add(temp);
        }

        //add the trip distance to the end of the page
        totalTripDistanceString = Util.roundDistance(distance);

        final Runnable tripDistanceRunnable = new Runnable() {
            public void run() {
                tripDistance.setText(totalTripDistanceString);
            }
        };
        mHandler.post(tripDistanceRunnable);
    }
}
