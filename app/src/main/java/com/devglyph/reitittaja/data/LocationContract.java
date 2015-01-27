package com.devglyph.reitittaja.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class LocationContract {

    public static final String CONTENT_AUTHORITY = "com.devglyph.reitittaja";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_LOCATION = "location";

    /* Inner class that defines the table contents of the location table */
    public static final class LocationEntry implements BaseColumns {

        // These indices are tied to LOCATION_COLUMNS.  If Location changes, these
        // must change.
        public static final int COLUMN_ID_INDEX = 0;
        public static final int COLUMN_LOCATION_NAME_INDEX = 1;
        public static final int COLUMN_LOCATION_DESCRIPTION_INDEX = 2;
        public static final int COLUMN_COORD_LAT_INDEX = 3;
        public static final int COLUMN_COORD_LONG_INDEX = 4;
        public static final int COLUMN_FAVORITE_INDEX = 5;

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        // Table name
        public static final String TABLE_NAME = "location";

        //has the location been added to the favorites list
        public static final String COLUMN_FAVORITE = "favorite";

        //human readable name for the location parsed from the API information
        public static final String COLUMN_LOCATION_NAME = "location_name";

        //possible description given for the location
        public static final String COLUMN_LOCATION_DESCRIPTION = "location_description";

        //coordinates
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
