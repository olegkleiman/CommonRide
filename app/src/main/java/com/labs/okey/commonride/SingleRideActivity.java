package com.labs.okey.commonride;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.labs.okey.commonride.model.Join;
import com.labs.okey.commonride.model.Ride;
import com.microsoft.windowsazure.mobileservices.*;

import java.util.Date;
import java.util.List;

public class SingleRideActivity extends ActionBarActivity {

    private static final String LOG_TAG = "CommonRide.SingleRide";

    private MobileServiceClient mClient;
    private MobileServiceTable<Ride> mRidesTable;
    private MobileServiceTable<Join> mJoinsTable;

    String mRideId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_ride);

        Bundle b = getIntent().getExtras();
        mRideId = b.getString("rideId");

        try {
            mClient = new MobileServiceClient(
                    "https://commonride.azure-mobile.net/",
                    "RuDCJTbpVcpeCQPvrcYeHzpnLyikPo70",
                    this);
            mRidesTable = mClient.getTable("commonrides", Ride.class);
            mJoinsTable = mClient.getTable("joins", Join.class);

            mRidesTable.lookUp(mRideId, new TableOperationCallback<Ride>() {

                        public void onCompleted(Ride ride,
                                                Exception e,
                                                ServiceFilterResponse serviceFilterResponse) {
                            if( e == null ){

                                if( ride != null ) {
                                   TextView v = (TextView) findViewById(R.id.ride_from);
                                   v.setText(ride.from);
                                }
                            }
                        }
                    });
        } catch(Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    public void btnJoinRideClick(View v) {

        Join join = new Join();
        join.rideId = mRideId;

        mJoinsTable.insert(join, new TableOperationCallback<Join>() {
                    public void onCompleted(Join entity,
                                            Exception exception,
                                            ServiceFilterResponse response) {
                        if (exception == null) {
                            Log.i(LOG_TAG, "Inserted to JOINS object with ID " + entity.Id);
                        }
                    }
                }
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_single_ride, menu);
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
