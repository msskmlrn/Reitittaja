package com.devglyph.reitittaja.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.models.Route;

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

    //the route whose details are to be shown
    private Route mRoute;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RouteDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ROUTE_DETAIL_KEY)) {
            mRoute = getArguments().getParcelable(ROUTE_DETAIL_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_route_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mRoute != null) {
            ((TextView) rootView.findViewById(R.id.route_detail)).setText
                    (mRoute.getStartLocation().getName() + " - " + mRoute.getEndLocation().getName());
        }

        return rootView;
    }
}
