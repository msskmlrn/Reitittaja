package com.devglyph.reitittaja.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.fragments.FavoriteDialogFragment;
import com.devglyph.reitittaja.fragments.FavoritesFragment;
import com.devglyph.reitittaja.fragments.JourneyPlannerFragment;
import com.devglyph.reitittaja.fragments.NavigationDrawerFragment;
import com.devglyph.reitittaja.fragments.SaveToFavoritesFragment;
import com.devglyph.reitittaja.models.Location;


public class MainActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        FavoritesFragment.OnFragmentInteractionListener,
        FavoriteDialogFragment.OnFavoriteChosenListener,
        SaveToFavoritesFragment.OnFavoriteSavedListener {


    private final String LOG_TAG = MainActivity.class.getSimpleName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, boolean fromSavedInstanceState) {
        // update the main content by replacing fragments
        if (!fromSavedInstanceState) {
            Fragment fragment = null;

            if (position == 0) {
                fragment = JourneyPlannerFragment.newInstance(position + 1);
            }
            else if (position == 1) {
                fragment = PlaceholderFragment.newInstance(position + 1);
            }
            else if (position == 2) {
                fragment = PlaceholderFragment.newInstance(position + 1);
            }
            else if (position == 3) {
                fragment = FavoritesFragment.newInstance(position + 1);
            }
            else {
                return;
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onFragmentInteraction(String string) {

    }

    @Override
    public void onSwapLocations() {
        Log.d(LOG_TAG, "onSwapLocations");
        //get a reference to the fragment and swap the start and end locations
        JourneyPlannerFragment fragment = (JourneyPlannerFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            Log.d(LOG_TAG, "onSwapLocations, swapping");
            fragment.swapLocations();
        }
    }

    @Override
    public void onFavoriteChosen(Location location, boolean startPlace) {
        Log.d(LOG_TAG, "onFavoriteChosen");
        Log.d(LOG_TAG, "onFavoriteChosen " + location.getName());
        Log.d(LOG_TAG, "onFavoriteChosen " + location.getDescription());
        Log.d(LOG_TAG, "onFavoriteChosen " + location.getCoords().getLatitude() + ", " + location.getCoords().getLongitude());
        Log.d(LOG_TAG, "onFavoriteChosen " + location.isFavorite());

        //get a reference to the fragment and pass the location to it
        JourneyPlannerFragment fragment = (JourneyPlannerFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            Log.d(LOG_TAG, "onFavoriteChosen, passing to journey planner fragment");
            fragment.placeChosenFromFavorites(location, startPlace);
        }
    }

    @Override
    public void onFavoriteSaved(Location location, boolean clickForStartPlace) {
        Log.d(LOG_TAG, "onFavoriteSaved");
        Log.d(LOG_TAG, "onFavoriteSaved " + location.getName());
        Log.d(LOG_TAG, "onFavoriteSaved " + location.getDescription());
        Log.d(LOG_TAG, "onFavoriteSaved " + location.getCoords().getLatitude() + ", " + location.getCoords().getLongitude());
        Log.d(LOG_TAG, "onFavoriteSaved " + location.isFavorite());

        //get a reference to the fragment and pass the location to it
        JourneyPlannerFragment fragment = (JourneyPlannerFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment != null) {
            Log.d(LOG_TAG, "onFavoriteSaved, passing to journey planner fragment");
            fragment.placeChosenFromFavorites(location, clickForStartPlace);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }
}