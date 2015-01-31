package com.labs.okey.commonride;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.labs.okey.commonride.adapters.PassengersAdapter;
import com.labs.okey.commonride.model.Join;
import com.labs.okey.commonride.model.JoinAnnotated;
import com.labs.okey.commonride.model.Ride;
import com.labs.okey.commonride.model.User;
import com.labs.okey.commonride.utils.DrawableManager;
import com.microsoft.windowsazure.mobileservices.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SingleRideActivity extends ActionBarActivity {

    private static final String LOG_TAG = "CommonRide.SingleRide";

    private static final String WAMSTOKENPREF = "wamsToken";
    private static final String USERIDPREF = "userid";

    private MobileServiceClient mClient;
    private MobileServiceTable<Ride> mRidesTable;
    private MobileServiceTable<Join> mJoinsTable;
    private MobileServiceTable<JoinAnnotated> mJoinsAnnotatedTable;

    String myUserID;
    String mDriverID;

    String mRideId;
    String mDriverPhone;
    String mDriverEMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_ride);

        mRideId = getIntent().getStringExtra("rideId");

        try {
            mClient = new MobileServiceClient(
                    "https://commonride.azure-mobile.net/",
                    "RuDCJTbpVcpeCQPvrcYeHzpnLyikPo70",
                    this);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            myUserID = sharedPrefs.getString(USERIDPREF, "");
            MobileServiceUser wamsUser = new MobileServiceUser(myUserID);

            String token = sharedPrefs.getString(WAMSTOKENPREF, "");
            // According to this article (http://www.thejoyofcode.com/Setting_the_auth_token_in_the_Mobile_Services_client_and_caching_the_user_rsquo_s_identity_Day_10_.aspx)
            // this should be JWT token, so use WAMS_TOKEM
            wamsUser.setAuthenticationToken(token);

            mClient.setCurrentUser(wamsUser);

            final ProgressDialog progress = ProgressDialog.show(this,
                    "Downloading", "Ride");

            mRidesTable = mClient.getTable("commonrides", Ride.class);
            mRidesTable.lookUp(mRideId, new TableOperationCallback<Ride>() {

                        public void onCompleted(Ride ride,
                                                Exception e,
                                                ServiceFilterResponse serviceFilterResponse) {
                            if( e == null ){

                                if( ride != null ) {

                                   mDriverID = ride.getDriver();
                                    // 'Delete' menu item should be disabled for
                                    // any rides that published by not myUser.
                                    // Actual disabling is done in onPrepareOptionsMenu() method,
                                    // that is implied by invalidateOptionMenu() invocation.
                                   if( !myUserID.equals(mDriverID) )
                                        invalidateOptionsMenu();

                                   TextView v = (TextView) findViewById(R.id.ride_from);
                                   v.setText(ride.from);

                                   v = (TextView)findViewById(R.id.ride_to);
                                   v.setText(ride.to);

                                   v = (TextView)findViewById(R.id.ride_when);
                                   SimpleDateFormat df = new SimpleDateFormat("EEEE MMM dd, yyyy HH:mm");
                                   v.setText(df.format(ride.whenStarts));

                                   final TextView txtDriverName =
                                           (TextView) findViewById(R.id.txtDriverName);
                                   final TextView txtDriverEMail =
                                            (TextView) findViewById(R.id.txtDriverEMail);
                                   final TextView txtDriverPhone =
                                            (TextView) findViewById(R.id.txtDriverPhone);
                                   final ImageView imageDriver = (ImageView)findViewById(R.id.imgageViewDriver);

                                   String driverID = ride.getDriver();
                                   MobileServiceTable<User> usersTable =
                                            mClient.getTable("users", User.class);
                                   usersTable.where().field("registration_id").eq(driverID)
                                           .execute(new TableQueryCallback<User>() {
                                               @Override
                                               public void onCompleted(List<User> users,
                                                                       int count,
                                                                       Exception error,
                                                                       ServiceFilterResponse serviceFilterResponse) {

                                                   if (error == null) {

                                                       User user = users.get(0);
                                                       if (user != null) {
                                                           txtDriverName.setText(user.first_name +
                                                                   " " + user.last_name);
                                                           mDriverEMail = user.email;
                                                           txtDriverEMail.setText(mDriverEMail);
                                                           mDriverPhone = user.phone;
                                                           txtDriverPhone.setText(user.phone);

                                                           DrawableManager drawableManager = new DrawableManager();
                                                           drawableManager.fetchDrawableOnThread(user.picture_url,
                                                                   imageDriver);
                                                       }
                                                   }

                                               }
                                           });
                                }
                            }
                            progress.dismiss();
                        }
                    });

            //
            // Passengers
            //
            refreshPassengers();

        } catch(Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    private void refreshPassengers() {
        if( mJoinsAnnotatedTable == null ) {
            mJoinsAnnotatedTable = mClient.getTable("joins_annotated", JoinAnnotated.class);
        }

        mJoinsAnnotatedTable
                .parameter("rideid", mRideId)
                .execute(new TableQueryCallback<JoinAnnotated>() {
                    @Override
                    public void onCompleted(List<JoinAnnotated> joins,
                                            int count,
                                            Exception error,
                                            ServiceFilterResponse serviceFilterResponse) {
                        if (error != null) {
                            String err = error.toString();
                            Throwable t = error.getCause();

                            while (t != null) {
                                err = err + "\n Cause: " + t.toString();
                                t = t.getCause();
                            }
                        } else {
                            setupJoinsListView(joins);
                        }
                    }
                });

    }

    private void setupJoinsListView(List<JoinAnnotated> joins){
        final ListView listview = (ListView) findViewById(R.id.listViewPassengers);

        PassengersAdapter ridesAdapter = new PassengersAdapter(SingleRideActivity.this,
                R.layout.passenger_item_row, joins);
        listview.setAdapter(ridesAdapter);
    }

    public void deletePassenger(final View v){

        String joinId = (String)v.getTag();

        final Join join = new Join();
        join.Id = joinId;

        final ProgressDialog progress = ProgressDialog.show(this,
                "Uploading", "Unjoining a ride");

        mJoinsTable = mClient.getTable("joins", Join.class);
        mJoinsTable.delete(join, new TableDeleteCallback() {
            @Override
            public void onCompleted(Exception e,
                                    ServiceFilterResponse serviceFilterResponse) {

                progress.dismiss();

                if( e != null ) {
                    Toast.makeText(SingleRideActivity.this,
                            "Unable unjoin. Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {

                    final ListView listView = (ListView)v.getParent().getParent().getParent();
                    if( listView != null ) {
                        final PassengersAdapter adapter = (PassengersAdapter)listView.getAdapter();
                        if( adapter != null ) {
                            final List<JoinAnnotated> joins = adapter.getJoins();
                            if( joins != null ) {

                                View animView = (View)v.getParent();

                                animView.animate().setDuration(2000).alpha(0)
                                        .withEndAction(new Runnable() {
                                            @Override
                                            public void run() {
                                                joins.remove(join);
                                                adapter.notifyDataSetChanged();
                                                listView.setAlpha(1);
                                            }
                                        } );
                            }
                        }
                    }


                }
            }
        });
    }

    public void callDriver(View v){
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse("tel:" + mDriverPhone));

        try {
            startActivity(phoneIntent);
        } catch(android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,
                    "Call failed: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    public void emailDriver(View v) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{mDriverEMail});
        i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
        i.putExtra(Intent.EXTRA_TEXT   , "body of email");

        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void showRideMap(View v){
        Intent intent = new Intent(this, RideMapActivity.class);
        intent.putExtra("rideId", mRideId);
        startActivity(intent);
    }

    public void joinRide() {

        Join join = new Join();
        join.rideId = mRideId;
        join.whenJoined = new Date();

        final ProgressDialog progress = ProgressDialog.show(this,
                "Uploading", "Adding a ride");

        mJoinsTable = mClient.getTable("joins", Join.class);

        mJoinsTable.insert(join, new TableOperationCallback<Join>() {
                    public void onCompleted(Join entity,
                                            Exception exception,
                                            ServiceFilterResponse response) {
                        if (exception == null) {
                            refreshPassengers();
                        } else {
                            Log.e(LOG_TAG, exception.getMessage());
                        }

                        progress.dismiss();
                    }
                }
        );
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if( (myUserID != null && !myUserID.isEmpty() )
                &&
                (mDriverID != null && !mDriverID.isEmpty()) ) {
            if( !myUserID.equals( mDriverID) ) {
                menu.getItem(1).setEnabled(false);
            }
        }

        return true;
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

        switch ( id ) {
            case R.id.action_join_delete: {
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.dialog_confirm)
                        .setMessage(R.string.dilalog_confirm_join_delete)
                        .setCancelable(true)
                        .setNegativeButton(R.string.no, null)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Ride ride = new Ride();
                                ride.Id = mRideId;
                                mRidesTable.delete(ride, new TableDeleteCallback() {

                                    @Override
                                    public void onCompleted(Exception e,
                                                            ServiceFilterResponse serviceFilterResponse) {
                                        if (e != null) {
                                            Toast.makeText(SingleRideActivity.this,
                                                           e.getMessage(),
                                                           Toast.LENGTH_LONG).show();
                                        }
                                        else
                                            finish();
                                    }
                                });
                            }
                        })
                        .show();

            }
            return true;

            case R.id.action_join_ride: {
                joinRide();
            }
            break;
        }


        return super.onOptionsItemSelected(item);
    }
}
