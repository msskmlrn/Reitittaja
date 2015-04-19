package com.devglyph.reitittaja.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.adapters.RoutesAdapter;
import com.devglyph.reitittaja.models.Route;

import java.util.ArrayList;

/**
 * A list fragment representing a list of Routes. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link RouteDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link com.devglyph.reitittaja.fragments.RouteListFragment.OnItemSelected}
 * interface.
 */
public class RouteListFragment extends Fragment {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    private final String LOG_TAG = RouteListFragment.class.getSimpleName();

    private ArrayList<Route> mRoutes = new ArrayList<>();
    private ListView listView;

    private RoutesAdapter routeArrayAdapter;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private OnItemSelected mCallback;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface OnItemSelected {
        void onItemSelected(int index);
    }

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

        Intent i = getActivity().getIntent();
        mRoutes = i.getParcelableArrayListExtra(JourneyPlannerFragment.SER_KEY);

        View v = inflater.inflate(R.layout.fragment_routes, null);
        listView = (ListView) v.findViewById(R.id.list_routes);

        TextView startPlace = (TextView) v.findViewById(R.id.start_place);
        TextView endPlace = (TextView) v.findViewById(R.id.end_place);

        startPlace.setText(mRoutes.get(0).getStartLocation().getName());
        endPlace.setText(mRoutes.get(0).getEndLocation().getName());

        routeArrayAdapter = new RoutesAdapter(getActivity(), mRoutes);
        listView.setAdapter(routeArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "onGroupClick, group " + position);
                mCallback.onItemSelected(position);
            }
        });

        return v;
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

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnItemSelected) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnItemSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

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
        listView.setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);

    }

    private void setActivatedPosition(int position) {

        if (position == ListView.INVALID_POSITION) {
            listView.setItemChecked(mActivatedPosition, false);
        } else {
            listView.setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
}
