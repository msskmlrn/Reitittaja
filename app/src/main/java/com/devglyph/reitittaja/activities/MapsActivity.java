package com.devglyph.reitittaja.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.devglyph.reitittaja.R;
import com.devglyph.reitittaja.Util;
import com.devglyph.reitittaja.models.Coordinates;
import com.devglyph.reitittaja.models.Route;
import com.devglyph.reitittaja.models.RouteLeg;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity {

    private final String LOG_TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private Route mRoute;
    private int mLegPosition = -1;
    private LatLng startLocation;
    private static int counter = 1;
    private LatLng zoomPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("route")) {
                mRoute = intent.getParcelableExtra("route");
            }
            if (intent.hasExtra("leg")) {
                mLegPosition = intent.getIntExtra("leg", -1);
            }
        }

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }

        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if (mRoute != null) {
            createRoutePolyline();
            zoomPoint = startLocation;
        }

        if (mLegPosition != -1) {
            Coordinates point = mRoute.getLegs().get(mLegPosition).getLocations().get(0).getCoordinates();
            zoomPoint = new LatLng(point.getLatitude(), point.getLongitude());
        }

        //zoom the camera to either the start of the route or start of the clicked leg
        zoomCamera(zoomPoint);


        //adjust map options
        mMap.setMyLocationEnabled(true);

        UiSettings settings = mMap.getUiSettings();

        settings.setCompassEnabled(true);
        settings.setMyLocationButtonEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setRotateGesturesEnabled(false);
        settings.setScrollGesturesEnabled(true);
        settings.setTiltGesturesEnabled(false);
        settings.setZoomGesturesEnabled(true);
    }

    private void createRoutePolyline() {
        for (int i = 0; i < mRoute.getLegs().size(); i++) {
            PolylineOptions options = new PolylineOptions();
            RouteLeg leg = mRoute.getLegs().get(i);

            markPoints(leg.getStartLocation().getCoordinates().getLatitude(),
                    leg.getStartLocation().getCoordinates().getLongitude(),
                    leg.getEndLocation().getCoordinates().getLatitude(),
                    leg.getEndLocation().getCoordinates().getLongitude(),
                    leg.getStartLocation().getName(),
                    leg.getEndLocation().getName());


            addPointsToPolylineOptions(leg, options);
            Polyline polyline = mMap.addPolyline(options);
        }
    }

    //Add the points the polyline should follow to the options
    private void addPointsToPolylineOptions(RouteLeg leg, PolylineOptions options) {
        //gather the points the route should follow
        for (int i = 0; i < leg.getShape().size(); i++) {
            Coordinates coords = leg.getShape().get(i);
            LatLng point = new LatLng(coords.getLatitude(), coords.getLongitude());
            options.add(point);
        }

        //color each leg based on the transportation mode used
        options.color(getModeColor(leg.getType()));
    }

    private void markPoints(double startLati, double startLongi, double endLati, double endLongi,
                            String startName, String endName) {

        startLocation = new LatLng(startLati, startLongi);
        LatLng endLocation = new LatLng(endLati, endLongi);

        Marker marker = mMap.addMarker(new MarkerOptions().
                position(startLocation).title(""+counter + ", " + startName));
        counter++;

        marker = mMap.addMarker(new MarkerOptions().
                position(endLocation).title(""+counter + ", " + endName));
        counter++;
    }


    private void zoomCamera(LatLng point) {
        if (point != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
        }
    }

    private int getModeColor(int mode) {
        if (mode == RouteLeg.TRAIN) {
            return Color.GREEN;
        }
        else if (mode == RouteLeg.TRAM) {
            return Color.YELLOW;
        }
        else if (mode == RouteLeg.METRO) {
            return Color.RED;
        }
        else if (mode == RouteLeg.WALK) {
            return Color.GRAY;
        }
        else if (mode == RouteLeg.CYCLE) {
            return Color.CYAN;
        }
        else if (Util.isBusMode(mode)) {
            return Color.MAGENTA;
        }
        else {
            return Color.BLACK;
        }
    }
}
