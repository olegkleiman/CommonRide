package com.labs.okey.commonride;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.labs.okey.commonride.model.Ride;
import com.labs.okey.commonride.utils.GMapV2Direction;
import com.labs.okey.commonride.utils.Globals;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;


public class RideMapActivity extends FragmentActivity {

    private static final String LOG_TAG = "Annex.RideMapActivity";

    private MobileServiceClient mClient;
    private MobileServiceTable<Ride> mRidesTable;

    private GoogleMap gMap;
    String mRideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_map);

        mRideId = getIntent().getStringExtra("rideId");
        try {
            mClient = new MobileServiceClient(
                    Globals.WAMS_URL,
                    Globals.WAMS_API_KEY,
                    this);
            mRidesTable = mClient.getTable("commonrides", Ride.class);

            final ProgressDialog progress = ProgressDialog.show(this,
                    "Preparing", "Ride's map");

            mRidesTable.lookUp(mRideId, new TableOperationCallback<Ride>() {
                @Override
                public void onCompleted(Ride ride,
                                        Exception e,
                                        ServiceFilterResponse serviceFilterResponse) {
                    if( e == null ){

                        if( ride == null
                                || ride.from_lat == null || ride.from_lat.isEmpty()
                                || ride.from_lon == null || ride.from_lon.isEmpty()
                                || ride.to_lat == null || ride.to_lat.isEmpty()
                                || ride.to_lon == null || ride.to_lon.isEmpty() ) {

                            progress.dismiss();

                            Toast.makeText(RideMapActivity.this,
                                    "The ride is not well-defined",
                                    Toast.LENGTH_LONG).show();
                        } else {

                            LatLng start = new LatLng(Double.parseDouble(ride.from_lat),
                                    Double.parseDouble(ride.from_lon));
                            LatLng end = new LatLng(Double.parseDouble(ride.to_lat),
                                    Double.parseDouble(ride.to_lon));

                            GMapV2Direction md = new GMapV2Direction();
                            md.drawDirectitions(gMap, start, end,
                                    GMapV2Direction.MODE_DRIVING,
                                    // TODO : detect used language
                                    // List of supported languages : https://spreadsheets.google.com/pub?key=p9pdwsai2hDMsLkXsoM05KQ&gid=1);
                                    "iw");
                        }
                    }

                    progress.dismiss();
                }
            });
        } catch(Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }

        ensureMap();

        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        gMap.getUiSettings().setMyLocationButtonEnabled(true);
        gMap.getUiSettings().setZoomControlsEnabled(false);
        gMap.setBuildingsEnabled(true);

        gMap.setTrafficEnabled(true);

        LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location lastKnownLocation = locationManager.getLastKnownLocation(bestProvider);

        PositionMap(lastKnownLocation);
    }

    private void ensureMap() {
        try{
            if( gMap == null ) {
                gMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.ride_map))
                        .getMap();
            }
        } catch(Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void PositionMap(Location location){

        final LatLng ME = new LatLng(location.getLatitude(),
                location.getLongitude());

        final int zoomLevel = 12;
        // Move the camera instantly to the current location with a zoom.
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ME, zoomLevel));

        // Zoom in, animating the camera.
        gMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to specified zoom level, animating with a duration of 2 seconds.
        gMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel), 2000, null);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_ride_map, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
