package com.devglyph.reitittaja;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.devglyph.reitittaja.data.DbHelper;
import com.devglyph.reitittaja.data.LocationContract;

public class TestDb extends AndroidTestCase {
    private static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
        SQLiteDatabase db = new DbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = createTestLocationValuesKumpula();

        long locationRowId;
        locationRowId = db.insert(LocationContract.LocationEntry.TABLE_NAME, null, testValues);

        // Verify that a row was returned
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "row id: " + locationRowId);

        //query the database and make sure the data was really inserted
        Cursor cursor = db.query(
                LocationContract.LocationEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, testValues);

        dbHelper.close();
    }

    static ContentValues createTestLocationValuesRautatientori() {
        ContentValues testValues = new ContentValues();
        testValues.put(LocationContract.LocationEntry.COLUMN_COORD_LAT, 60.171111);
        testValues.put(LocationContract.LocationEntry.COLUMN_COORD_LONG, 24.944028);
        testValues.put(LocationContract.LocationEntry.COLUMN_FAVORITE, 0);
        testValues.put(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION,
                "Rautatientori (ruots. Järnvägstorget) " +
                "on Helsingin ydinkeskustassa, Helsingin rautatieaseman vieressä sijaitseva aukio");
        testValues.put(LocationContract.LocationEntry.COLUMN_LOCATION_NAME, "Rautatientori");

        return testValues;
    }

    static ContentValues createTestLocationValuesKumpula() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(LocationContract.LocationEntry.COLUMN_COORD_LAT, 60.204722);
        testValues.put(LocationContract.LocationEntry.COLUMN_COORD_LONG, 24.962778);
        testValues.put(LocationContract.LocationEntry.COLUMN_FAVORITE, 1);
        testValues.put(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION,
                "Kumpulan kampus on yksi Helsingin yliopiston neljästä kampusalueesta.");
        testValues.put(LocationContract.LocationEntry.COLUMN_LOCATION_NAME, "Kumpulan kampus");

        return testValues;
    }

    /**
     * Compare the cursor values with the expected contentvalues to make sure they match
     * @param valueCursor
     * @param expectedValues
     */
    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {
        assertTrue(valueCursor.moveToFirst());

        Log.d(LOG_TAG, "expected: "+valueCursor.getDouble(LocationContract.LocationEntry.COLUMN_COORD_LAT_INDEX));
        Log.d(LOG_TAG, "received: " + expectedValues.getAsDouble(LocationContract.LocationEntry.COLUMN_COORD_LAT));

        Log.d(LOG_TAG, "expected: " + valueCursor.getDouble(LocationContract.LocationEntry.COLUMN_COORD_LONG_INDEX));
        Log.d(LOG_TAG, "received: "+expectedValues.getAsDouble(LocationContract.LocationEntry.COLUMN_COORD_LONG));

        Log.d(LOG_TAG, "expected: "+valueCursor.getInt(LocationContract.LocationEntry.COLUMN_FAVORITE_INDEX));
        Log.d(LOG_TAG, "received: "+expectedValues.getAsInteger(LocationContract.LocationEntry.COLUMN_FAVORITE).intValue());

        Log.d(LOG_TAG, "expected: "+valueCursor.getString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME_INDEX));
        Log.d(LOG_TAG, "received: "+expectedValues.getAsString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME));

        Log.d(LOG_TAG, "expected: "+valueCursor.getString(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION_INDEX));
        Log.d(LOG_TAG, "received: "+expectedValues.getAsString(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION));

        assertEquals(valueCursor.getDouble(LocationContract.LocationEntry.COLUMN_COORD_LAT_INDEX),
                expectedValues.getAsDouble(LocationContract.LocationEntry.COLUMN_COORD_LAT));
        assertEquals(valueCursor.getDouble(LocationContract.LocationEntry.COLUMN_COORD_LONG_INDEX),
                expectedValues.getAsDouble(LocationContract.LocationEntry.COLUMN_COORD_LONG));
        assertEquals(valueCursor.getInt(LocationContract.LocationEntry.COLUMN_FAVORITE_INDEX),
                expectedValues.getAsInteger(LocationContract.LocationEntry.COLUMN_FAVORITE).intValue());
        assertEquals(valueCursor.getString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME_INDEX),
                expectedValues.getAsString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME));
        assertEquals(valueCursor.getString(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION_INDEX),
                expectedValues.getAsString(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION));
    }

    /**
     * Compare the cursor values with the expected contentvalues to make sure they do not match
     * @param valueCursor
     * @param expectedValues
     */
    static void validateCursorMatchingFails(Cursor valueCursor, ContentValues expectedValues) {
        assertTrue(valueCursor.moveToFirst());

        Log.d(LOG_TAG, "expected: "+valueCursor.getDouble(LocationContract.LocationEntry.COLUMN_COORD_LAT_INDEX));
        Log.d(LOG_TAG, "received: " + expectedValues.getAsDouble(LocationContract.LocationEntry.COLUMN_COORD_LAT));

        Log.d(LOG_TAG, "expected: " + valueCursor.getDouble(LocationContract.LocationEntry.COLUMN_COORD_LONG_INDEX));
        Log.d(LOG_TAG, "received: "+expectedValues.getAsDouble(LocationContract.LocationEntry.COLUMN_COORD_LONG));

        Log.d(LOG_TAG, "expected: "+valueCursor.getInt(LocationContract.LocationEntry.COLUMN_FAVORITE_INDEX));
        Log.d(LOG_TAG, "received: "+expectedValues.getAsInteger(LocationContract.LocationEntry.COLUMN_FAVORITE).intValue());

        Log.d(LOG_TAG, "expected: "+valueCursor.getString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME_INDEX));
        Log.d(LOG_TAG, "received: "+expectedValues.getAsString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME));

        Log.d(LOG_TAG, "expected: "+valueCursor.getString(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION_INDEX));
        Log.d(LOG_TAG, "received: "+expectedValues.getAsString(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION));

        assertFalse(valueCursor.getDouble(LocationContract.LocationEntry.COLUMN_COORD_LAT_INDEX) ==
                expectedValues.getAsDouble(LocationContract.LocationEntry.COLUMN_COORD_LAT));
        assertFalse(valueCursor.getDouble(LocationContract.LocationEntry.COLUMN_COORD_LONG_INDEX) ==
                expectedValues.getAsDouble(LocationContract.LocationEntry.COLUMN_COORD_LONG));
        assertFalse(valueCursor.getInt(LocationContract.LocationEntry.COLUMN_FAVORITE_INDEX) ==
                expectedValues.getAsInteger(LocationContract.LocationEntry.COLUMN_FAVORITE).intValue());
        assertFalse(valueCursor.getString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME_INDEX).
                equals(expectedValues.getAsString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME)));
        assertFalse(valueCursor.getString(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION_INDEX).
                equals(expectedValues.getAsString(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION)));
    }

}

