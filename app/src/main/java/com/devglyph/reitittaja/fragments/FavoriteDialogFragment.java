package com.devglyph.reitittaja.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.data.LocationContract;
import com.devglyph.reitittaja.models.Location;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link com.devglyph.reitittaja.fragments.FavoriteDialogFragment.OnFavoriteChosenListener}
 * interface.
 */
public class FavoriteDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String NAME_PARAM = "name";
    private static final String DESCRIPTION_PARAM = "description";
    private static final String LAT_PARAM = "lat";
    private static final String LON_PARAM = "lon";
    private static final String CLICK_FOR_START_PLACE_PARAM = "mClickForStartPlace";

    private String mName;
    private String mDescription;
    private double mLat = -1;
    private double mLon = -1;
    private boolean mClickForStartPlace;

    private OnFavoriteChosenListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    private final String LOG_TAG = FavoritesFragment.class.getSimpleName();

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private SimpleCursorAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static FavoriteDialogFragment newInstance(String name, String description, double lat, double lon, boolean clickForStartPlace) {
        FavoriteDialogFragment fragment = new FavoriteDialogFragment();
        Bundle args = new Bundle();
        args.putString(NAME_PARAM, name);
        args.putString(DESCRIPTION_PARAM, description);
        args.putDouble(LAT_PARAM, lat);
        args.putDouble(LON_PARAM, lon);
        args.putBoolean(CLICK_FOR_START_PLACE_PARAM, clickForStartPlace);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FavoriteDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mName = getArguments().getString(NAME_PARAM);
            mDescription = getArguments().getString(DESCRIPTION_PARAM);
            mLat = getArguments().getDouble(LAT_PARAM);
            mLon = getArguments().getDouble(LON_PARAM);
            mClickForStartPlace = getArguments().getBoolean(CLICK_FOR_START_PLACE_PARAM);

            Log.d(LOG_TAG, "in FavoriteDialogFragment");
            Log.d(LOG_TAG, mName + " " + mDescription + " " + mLat + " " + mLon + " " + mClickForStartPlace);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_favoritedialog, container, false);

        // Set the adapter
        mListView = (ListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        Button saveButton = (Button) view.findViewById(R.id.button_save_location);
        createOnClickListener(saveButton);

        // Fields from the database (projection)
        // Must include the _id column for the adapter to work
        String[] from = new String[] { LocationContract.LocationEntry._ID,
                LocationContract.LocationEntry.COLUMN_FAVORITE,
                LocationContract.LocationEntry.COLUMN_LOCATION_NAME,
                LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION,
                LocationContract.LocationEntry.COLUMN_COORD_LAT,
                LocationContract.LocationEntry.COLUMN_COORD_LONG,
                };
        // Fields on the UI to which we map
        int[] to = new int[] { android.R.id.text1, android.R.id.text1, android.R.id.text1 };

        getLoaderManager().initLoader(0, null, this);
        mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, from,
                to, 0);

        mListView.setAdapter(mAdapter);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return view;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFavoriteChosenListener) activity;
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

    /**
     * Create a listener for save button presses
     * @param button
     */
    private void createOnClickListener(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.button_save_location) {
                    dismiss();

                    //check that a location was chosen, since nonexistent location cannot be added to the favorites
                    if (mName != null && mLat != -1 && mLon != -1) {
                        FragmentManager manager = getFragmentManager();
                        //pass the location parameters to the dialog
                        SaveToFavoritesFragment dialog = SaveToFavoritesFragment.newInstance(mName, mDescription, mLat, mLon, mClickForStartPlace);
                        dialog.show(manager, "dialog");
                    }
                    else { //give a toast message for nonexistent location
                        String message = "Enter a location before trying to add it to the favorites";
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(LOG_TAG, "onItemClick " + position);

        if (null != mListener) {
            Cursor cursor = ((SimpleCursorAdapter) parent.getAdapter()).getCursor();

            if (cursor != null && cursor.moveToPosition(position)) {
                //get the location information from the chosen location item
                String name = cursor.getString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME_INDEX);
                String description = cursor.getString(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION_INDEX);
                double lat = cursor.getDouble(LocationContract.LocationEntry.COLUMN_COORD_LAT_INDEX);
                double lon = cursor.getDouble(LocationContract.LocationEntry.COLUMN_COORD_LONG_INDEX);
                boolean favorite = cursor.getInt(LocationContract.LocationEntry.COLUMN_FAVORITE_INDEX) == 1 ? true : false;

                Log.d(LOG_TAG, "location info from database, clicked item");
                Log.d(LOG_TAG, name + " " + description + " " + lat + " " + lon + " " + favorite);

                //create a location object from the info
                Location location = new Location(name, description, lat, lon, favorite);
                Log.d(LOG_TAG, "onItemClick passing location");

                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onFavoriteChosen(location, mClickForStartPlace);

                //finally dismiss the dialog
                dismiss();
            }
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * Interface for passing the chosen favorite location information to the journeyplannerfragment.
     * if startPlace == true, then the original click came from the choosing the start place favorite,
     * else from end place favorite.
     */
    public interface OnFavoriteChosenListener {
        public void onFavoriteChosen(Location location, boolean startPlace);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader");
        String[] projection = { LocationContract.LocationEntry._ID,
                LocationContract.LocationEntry.COLUMN_FAVORITE,
                LocationContract.LocationEntry.COLUMN_LOCATION_NAME,
                LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION,
                LocationContract.LocationEntry.COLUMN_COORD_LAT,
                LocationContract.LocationEntry.COLUMN_COORD_LONG };

        //get only favorite locations
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                LocationContract.LocationEntry.CONTENT_URI,
                projection,
                LocationContract.LocationEntry.COLUMN_FAVORITE + " = ?", // cols for "where" clause
                new String[]{"1"}, // values for "where" clause
                null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished");
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "onLoaderReset");
        // data is not available anymore, delete reference
        mAdapter.swapCursor(null);
    }

}
