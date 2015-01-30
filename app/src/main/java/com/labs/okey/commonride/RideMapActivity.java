package com.labs.okey.commonride;

import android.app.ProgressDialog;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.labs.okey.commonride.model.Ride;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;


public class RideMapActivity extends FragmentActivity {

    private static final String LOG_TAG = "CommonRide.RideMapActivity";

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
                    "https://commonride.azure-mobile.net/",
                    "RuDCJTbpVcpeCQPvrcYeHzpnLyikPo70",
                    this);
            mRidesTable = mClient.getTable("commonrides", Ride.class);

            final ProgressDialog progress = ProgressDialog.show(this,
                    "Preparing", "Ride's map");

            mRidesTable.lookUp(mRideId, new TableOperationCallback() {
                @Override
                public void onCompleted(Object o, Exception e, ServiceFilterResponse serviceFilterResponse) {
                    if( e == null ){

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ride_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
