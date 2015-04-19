package com.devglyph.reitittaja;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.devglyph.reitittaja.data.LocationContract;

public class TestProvider extends AndroidTestCase {

    private static final String LOG_TAG = TestProvider.class.getSimpleName();

    // empty the database
    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                LocationContract.LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    // deleteAllRecords in setUp meaning before each test
    public void setUp() {
        deleteAllRecords();
    }

    long locationRowId;

    // Inserts test values to the database.
    public void insertRautatientoriTestData() {
        ContentValues testValues = TestDb.createTestLocationValuesRautatientori();
        Uri locationInsertUri = mContext.getContentResolver()
                .insert(LocationContract.LocationEntry.CONTENT_URI, testValues);
        assertTrue(locationInsertUri != null);

        locationRowId = ContentUris.parseId(locationInsertUri);
    }

    public void insertKumpulaTestData() {
        ContentValues testValues = TestDb.createTestLocationValuesRautatientori();
        Uri locationInsertUri = mContext.getContentResolver()
                .insert(LocationContract.LocationEntry.CONTENT_URI, testValues);
        assertTrue(locationInsertUri != null);

        locationRowId = ContentUris.parseId(locationInsertUri);
    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecordsAtEnd() {
        Log.d(LOG_TAG, "testDeleteRecordsAtEnd");
        deleteAllRecords();
    }

    public void testGetType() {
        Log.d(LOG_TAG, "testGetType");
        // content://com.devglyph.reitittaja/location/
        String type = mContext.getContentResolver().getType(LocationContract.LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.devglyph.reitittaja/location/
        assertEquals(LocationContract.LocationEntry.CONTENT_TYPE, type);

        // content://com.devglyph.reitittaja/location//location/1
        type = mContext.getContentResolver().getType(LocationContract.LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.devglyph.reitittaja/location/
        assertEquals(LocationContract.LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testInsertReadProvider() {
        Log.d(LOG_TAG, "testInsertReadProvider");
        ContentValues testValues = TestDb.createTestLocationValuesKumpula();

        Uri locationUri = mContext.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify that a row was returned
        assertTrue(locationRowId != -1);

        //query the database and make sure the data was really inserted
        Cursor cursor = mContext.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testValues);
    }

    public void testQueryByRowId() {
        Log.d(LOG_TAG, "testQueryByRowId");
        ContentValues testValues = TestDb.createTestLocationValuesKumpula();

        Uri locationUri = mContext.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify that a row was returned
        assertTrue(locationRowId != -1);

        // query by row id
        Cursor cursor = mContext.getContentResolver().query(
                LocationContract.LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testValues);
    }

    public void testFailQueryByRowId() {
        Log.d("TEST22", "testQueryByRowId");
        ContentValues testValues = TestDb.createTestLocationValuesKumpula();

        Uri locationUri = mContext.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, testValues);

        //create a bogus rowid
        long locationRowId = ContentUris.parseId(Uri.parse(locationUri.toString() + "11"));

        Log.d("TEST22", "id: " + locationRowId);

        // query by the bogus row id
        Cursor cursor = mContext.getContentResolver().query(
                LocationContract.LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        //test that the cursor is empty
        assertFalse(cursor.moveToFirst());
    }

    public void testQueryByName() {
        Log.d(LOG_TAG, "testQueryByName");
        ContentValues testValues = TestDb.createTestLocationValuesKumpula();

        Uri locationUri = mContext.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify that a row was returned
        assertTrue(locationRowId != -1);

        // query by name
        Cursor cursor = mContext.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                LocationContract.LocationEntry.COLUMN_LOCATION_NAME + " = ?", // cols for "where" clause
                new String[]{testValues.getAsString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME)}, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testValues);
    }

    public void testQueryFailByName() {
        Log.d(LOG_TAG, "testQueryFailByName");
        ContentValues testValues = TestDb.createTestLocationValuesKumpula();
        ContentValues failValues = TestDb.createTestLocationValuesRautatientori();

        Uri locationUri = mContext.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, testValues);
        long locationRowIdTestValues = ContentUris.parseId(locationUri);

        locationUri = mContext.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, failValues);
        long locationRowIdFailValues = ContentUris.parseId(locationUri);

        // Verify that a row was returned
        assertTrue(locationRowIdTestValues != -1);
        assertTrue(locationRowIdFailValues != -1);

        // query by wrong name
        Cursor cursor = mContext.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                LocationContract.LocationEntry.COLUMN_LOCATION_NAME + " = ?", // cols for "where" clause
                new String[]{failValues.getAsString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME)}, // values for "where" clause
                null  // sort order
        );

        //assert that some values were returned
        assertTrue(cursor.moveToFirst());

        //assert that the cursor and test values do not have the same name information
        assertFalse(cursor.getString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME_INDEX).
                equals(testValues.getAsString(LocationContract.LocationEntry.COLUMN_LOCATION_NAME)));
    }

    public void testQueryByCoordinates() {
        Log.d(LOG_TAG, "testQueryByCoordinates");
        ContentValues testValues = TestDb.createTestLocationValuesKumpula();

        Uri locationUri = mContext.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify that a row was returned
        assertTrue(locationRowId != -1);

        // query by coordinates
        Cursor cursor = mContext.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                LocationContract.LocationEntry.COLUMN_COORD_LAT + " = ? AND " +
                        LocationContract.LocationEntry.COLUMN_COORD_LONG + " = ?", // cols for "where" clause
                new String[]{""+testValues.getAsDouble(LocationContract.LocationEntry.COLUMN_COORD_LAT),
                        ""+testValues.getAsDouble(LocationContract.LocationEntry.COLUMN_COORD_LONG)}, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, testValues);
    }

    public void testQueryFailByCoordinates() {
        Log.d(LOG_TAG, "testQueryFailByCoordinates");
        ContentValues testValues = TestDb.createTestLocationValuesKumpula();
        ContentValues failValues = TestDb.createTestLocationValuesRautatientori();

        Uri locationUri = mContext.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, testValues);
        long locationRowIdTestValues = ContentUris.parseId(locationUri);

        locationUri = mContext.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, failValues);
        long locationRowIdFailValues = ContentUris.parseId(locationUri);

        // Verify that a row was returned
        assertTrue(locationRowIdTestValues != -1);
        assertTrue(locationRowIdFailValues != -1);

        // query by the wrong coordinates
        Cursor cursor = mContext.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                LocationContract.LocationEntry.COLUMN_COORD_LAT + " = ? AND " +
                        LocationContract.LocationEntry.COLUMN_COORD_LONG + " = ?", // cols for "where" clause
                new String[]{""+failValues.getAsDouble(LocationContract.LocationEntry.COLUMN_COORD_LAT),
                        ""+failValues.getAsDouble(LocationContract.LocationEntry.COLUMN_COORD_LONG)}, // values for "where" clause
                null  // sort order
        );

        //test that the cursor values do not match the testvalues
        TestDb.validateCursorMatchingFails(cursor, testValues);
    }

    public void testQueryFavorites() {
        Log.d(LOG_TAG, "testQueryFavorites");
        ContentValues testValues = TestDb.createTestLocationValuesKumpula();
        ContentValues testValues2 = TestDb.createTestLocationValuesRautatientori();

        Uri locationUri = mContext.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, testValues);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify that a row was returned
        assertTrue(locationRowId != -1);

        Uri locationUri2 = mContext.getContentResolver().insert(LocationContract.LocationEntry.CONTENT_URI, testValues2);
        long locationRowId2 = ContentUris.parseId(locationUri2);

        // Verify that a row was returned
        assertTrue(locationRowId2 != -1);

        // query by row id
        Cursor cursor = mContext.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                LocationContract.LocationEntry.COLUMN_FAVORITE + " = ?", // cols for "where" clause
                new String[]{"0"}, // values for "where" clause
                null  // sort order
        );

        //test that only one row was returned
        assertEquals(1, cursor.getCount());

        //test that the results match with the expected values
        TestDb.validateCursor(cursor, testValues2);
    }

    public void testUpdateLocation() {
        Log.d(LOG_TAG, "testUpdateLocation");
        ContentValues values = TestDb.createTestLocationValuesKumpula();

        Uri locationUri = mContext.getContentResolver().
                insert(LocationContract.LocationEntry.CONTENT_URI, values);
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify that a row was returned
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "row id: " + locationRowId);

        //update create a new map of values and update the name
        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationContract.LocationEntry._ID, locationRowId);
        updatedValues.put(LocationContract.LocationEntry.COLUMN_LOCATION_NAME, "Kumpula");

        int count = mContext.getContentResolver().update(
                LocationContract.LocationEntry.CONTENT_URI, updatedValues, LocationContract.LocationEntry._ID + "= ?",
                new String[] { Long.toString(locationRowId)});

        assertEquals(count, 1);

        Cursor cursor = mContext.getContentResolver().query(
                LocationContract.LocationEntry.buildLocationUri(locationRowId),
                null,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        //make sure that the values were updated in the database
        TestDb.validateCursor(cursor, updatedValues);
    }

    public void testUpdateAndReadLocation() {
        Log.d(LOG_TAG, "testUpdateAndReadLocation");
        insertRautatientoriTestData();
        long tempLocationRowId = locationRowId;

        String newDescription = "Tourist info";

        // Make an update to one value.
        ContentValues testUpdate = new ContentValues();
        testUpdate.put(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION, newDescription);

        mContext.getContentResolver().update(
                LocationContract.LocationEntry.CONTENT_URI, testUpdate, null, null);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                LocationContract.LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make the same update to the full ContentValues for comparison.
        ContentValues testAltered = TestDb.createTestLocationValuesRautatientori();
        testAltered.put(LocationContract.LocationEntry._ID, tempLocationRowId);
        testAltered.put(LocationContract.LocationEntry.COLUMN_LOCATION_DESCRIPTION, newDescription);

        TestDb.validateCursor(cursor, testAltered);
    }
}