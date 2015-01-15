package com.devglyph.reitittaja.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.fragments.JourneyPlannerFragment;
import com.devglyph.reitittaja.fragments.RouteDetailFragment;
import com.devglyph.reitittaja.fragments.RouteListFragment;
import com.devglyph.reitittaja.models.Route;

import java.util.ArrayList;



/**
 * An activity representing a list of Routes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link RouteDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link com.devglyph.reitittaja.fragments.RouteListFragment} and the item details
 * (if present) is a {@link com.devglyph.reitittaja.fragments.RouteDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link com.devglyph.reitittaja.fragments.RouteListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class RouteListActivity extends FragmentActivity
        implements RouteListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private ArrayList<Route> routes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);

        Intent intent = getIntent();

        //get the route info from the extras
        routes = intent.getParcelableArrayListExtra(JourneyPlannerFragment.SER_KEY);

        if (findViewById(R.id.route_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((RouteListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.route_list))
                    .setActivateOnItemClick(true);
        }
    }

    /**
     * Callback method from {@link RouteListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int index) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(RouteDetailFragment.ROUTE_DETAIL_KEY, routes.get(index));
            RouteDetailFragment fragment = new RouteDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.route_detail_container, fragment)
                    .commit();


        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, RouteDetailActivity.class);
            detailIntent.putExtra(RouteDetailFragment.ROUTE_DETAIL_KEY, routes.get(index));

            startActivity(detailIntent);
        }
    }
}
