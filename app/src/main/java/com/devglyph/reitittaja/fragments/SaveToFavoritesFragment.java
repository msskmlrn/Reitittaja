package com.devglyph.reitittaja.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.data.LocationContract;
import com.devglyph.reitittaja.models.Location;

/**
 * Activities that contain this fragment must implement the
 * {@link SaveToFavoritesFragment.OnFavoriteSavedListener} interface
 * to handle interaction events.
 */
public class SaveToFavoritesFragment extends DialogFragment {

    private final String LOG_TAG = FavoritesFragment.class.getSimpleName();

    private OnFavoriteSavedListener mListener;
    private EditText mEditText;

    private static final String NAME_PARAM = "name";
    private static final String DESCRIPTION_PARAM = "description";
    private static final String LAT_PARAM = "lat";
    private static final String LON_PARAM = "lon";
    private static final String START_PLACE_PARAM = "startPlace";

    private String mName;
    private String mDescription;
    private double mLat;
    private double mLon;
    private boolean mStartPlace;

    public SaveToFavoritesFragment() {
    }

    public static SaveToFavoritesFragment newInstance(String name, String description, double lat, double lon, boolean startPlace) {
        SaveToFavoritesFragment fragment = new SaveToFavoritesFragment();
        Bundle args = new Bundle();
        args.putString(NAME_PARAM, name);
        args.putString(DESCRIPTION_PARAM, description);
        args.putDouble(LAT_PARAM, lat);
        args.putDouble(LON_PARAM, lon);
        args.putBoolean(START_PLACE_PARAM, startPlace);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mName = getArguments().getString(NAME_PARAM);
            mDescription = getArguments().getString(DESCRIPTION_PARAM);
            mLat = getArguments().getDouble(LAT_PARAM);
            mLon = getArguments().getDouble(LON_PARAM);
            mStartPlace = getArguments().getBoolean(START_PLACE_PARAM);

            Log.d(LOG_TAG, "in SaveToFavoritesFragment");
            Log.d(LOG_TAG, mName + " " + mDescription + " " + mLat + " " + mLon + " " + mStartPlace);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_save_to_favorites, container);
        TextView mNameText = (TextView) view.findViewById(R.id.favorite_name);
        mEditText = (EditText) view.findViewById(R.id.favorite_description);
        Button mCancelButton = (Button) view.findViewById(R.id.favorite_cancel);
        createOnClickListener(mCancelButton);
        Button mSaveButton = (Button) view.findViewById(R.id.favorite_save);
        createOnClickListener(mSaveButton);

        //set the name of the location
        if (mName != null && !mName.isEmpty()) {
            mNameText.setText(mName);
        }

        //set the description to the previously saved description
        if (mDescription != null && !mDescription.isEmpty()) {
            mEditText.setText(mDescription);
        }
        getDialog().setTitle(R.string.new_favorite_dialog_header);

        // Show soft keyboard automatically
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return view;
    }

    /**
     * Create a listener for cancel and save button clicks
     * @param button
     */
    private void createOnClickListener(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.favorite_cancel) {
                    //just dismiss the dialog
                    dismiss();
                }
                else if (v.getId() == R.id.favorite_save) {
                    //First check if the given location has already been saved to the database.
                    //If it has, then alter the description, if that was changed and save the changes.
                    //Otherwise, this is a new favorite, so save it to the database.

                    //get the description from the text field
                    if (!mEditText.getText().toString().isEmpty()) {
                        mDescription = mEditText.getText().toString();
                    }

                    addToFavorites(mName, mDescription, mLat, mLon);
                    //finally dismiss the dialog
                    dismiss();
                }
            }
        });
    }

    /**
     * Update the description of an existing favorite or add a new favorite to the database.
     * @param name
     * @param description
     * @param lat
     * @param lon
     */
    private void addToFavorites(String name, String description, double lat, double lon) {
        Log.d(LOG_TAG, "addToFavorites");
        Log.d(LOG_TAG, name + " " + description + " " + lat + " "+ lon);

        // First, check if the location with given name, latitude and longitude exists in the db
        Cursor cursor = getActivity().getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI,
                null,
                LocationContract.LocationEntry.COLUMN_LOCATION_NAME + " = ? AND " +
                        LocationContract.LocationEntry.COLUMN_COORD_LAT + " = ? AND " +
                        LocationContract.LocationEntry.COLUMN_COORD_LONG + " = ?",
                new String[]{name, ""+lat, ""+lon},
                null,
                null);

        if (cursor.moveToFirst()) {
            Log.d(LOG_TAG, "location already present");
            updateFavorite(cursor, name, description, lat, lon);
        } else {
            Log.d(LOG_TAG, "location not found, saving this one");
            insertLocation(name, description, lat, lon);
        }
    }

    /**
     * Update the description of the location, if it was changed.
     * @param cursor
     * @param name
     * @param description
     * @param lat
     * @param lon
     */
    private void updateFavorite(Cursor cursor, String name, String description, double lat, double lon) {
        //print out the location information for testing
        String entryName = cursor.getString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME_INDEX);
        String entryDescription = cursor.getString(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION_INDEX);
        double entryLat = cursor.getDouble(LocationContract.LocationEntry.COLUMN_COORD_LAT_INDEX);
        double entryLon = cursor.getDouble(LocationContract.LocationEntry.COLUMN_COORD_LONG_INDEX);
        boolean entryFavorite = cursor.getInt(LocationContract.LocationEntry.COLUMN_FAVORITE_INDEX) == 1;

        Log.d(LOG_TAG, entryName + " " + entryDescription + " " + entryLat + " " + entryLon + " " + entryFavorite);
        Log.d(LOG_TAG, "old description "+entryDescription + " vs new "+description);

        //if the description needs to be updated
        if (!entryDescription.equals(description)) {
            Log.d(LOG_TAG, "changing description");
            long id = cursor.getLong(LocationContract.LocationEntry.COLUMN_ID_INDEX);

            ContentValues updatedValues = new ContentValues();
            updatedValues.put(LocationContract.LocationEntry._ID, id);
            updatedValues.put(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION, description);

            int count = getActivity().getContentResolver().update(
                    LocationContract.LocationEntry.CONTENT_URI, updatedValues, LocationContract.LocationEntry._ID + "= ?",
                    new String[] { Long.toString(id)});

            Log.d(LOG_TAG, "number of rows updated "+count);
        }

        //create a location object and call interface method
        Location location = new Location(name, description, lat, lon, true);
        mListener.onFavoriteSaved(location, mStartPlace);
    }

    /**
     * Insert the location in to the database
     * @param name
     * @param description
     * @param lat
     * @param lon
     */
    private void insertLocation(String name, String description, double lat, double lon) {
        int favoriteValue = 1;

        if (description == null || description.isEmpty()) {
            description = "-";
        }

        ContentValues locationValues = new ContentValues();
        locationValues.put(LocationContract.LocationEntry.COLUMN_FAVORITE, favoriteValue);
        locationValues.put(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION, description);
        locationValues.put(LocationContract.LocationEntry.COLUMN_LOCATION_NAME, name);
        locationValues.put(LocationContract.LocationEntry.COLUMN_COORD_LAT, lat);
        locationValues.put(LocationContract.LocationEntry.COLUMN_COORD_LONG, lon);

        Uri locationInsertUri = getActivity().getContentResolver()
                .insert(LocationContract.LocationEntry.CONTENT_URI, locationValues);

        Log.d(LOG_TAG, "inserting location");
        Log.d(LOG_TAG, "favorite value "+favoriteValue);

        //create a location object and call interface method
        Location location = new Location(name, description, lat, lon, true);
        mListener.onFavoriteSaved(location, mStartPlace);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFavoriteSavedListener) activity;
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
     * Interface for passing the created/updated location information to the journeyplannerfragment.
     * if startPlace == true, then the original click came from the choosing the start place favorite,
     * else from end place favorite.
     */
    public interface OnFavoriteSavedListener{
        void onFavoriteSaved(Location location, boolean startPlace);
    }

}
