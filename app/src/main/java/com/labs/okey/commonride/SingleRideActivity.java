package com.labs.okey.commonride;

import android.app.ProgressDialog;
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

    String mRideId;
    String mDriverPhone;
    String mDriverEMail;

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

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String userID = sharedPrefs.getString(USERIDPREF, "");
            MobileServiceUser wamsUser = new MobileServiceUser(userID);

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

                                                   progress.dismiss();
                                               }
                                           });
                                }
                            }
                        }
                    });

            //
            // Passengers
            //
            mJoinsAnnotatedTable = mClient.getTable("joins_annotated", JoinAnnotated.class);
            mJoinsAnnotatedTable
                    .parameter("rideid", mRideId)
                    .execute(new TableQueryCallback<JoinAnnotated>() {
                @Override
                public void onCompleted(List<JoinAnnotated> joins,
                                        int count,
                                        Exception error,
                                        ServiceFilterResponse serviceFilterResponse) {
                    if( error != null) {
                        String err = error.toString();
                        Throwable t = error.getCause();

                        while (t != null) {
                            err = err + "\n Cause: " + t.toString();
                            t = t.getCause();
                        }
                    }
                    else{
                        setupJoinsListView(joins);
                    }
                }
            });
        } catch(Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    private void setupJoinsListView(List<JoinAnnotated> joins){
        final ListView listview = (ListView) findViewById(R.id.listViewPassengers);

        PassengersAdapter ridesAdapter = new PassengersAdapter(SingleRideActivity.this,
                R.layout.passenger_item_row, joins);
        listview.setAdapter(ridesAdapter);
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

    public void btnJoinRideClick(View v) {

        Join join = new Join();
        join.rideId = mRideId;
        join.whenJoined = new Date();

        mJoinsTable = mClient.getTable("joins", Join.class);

        mJoinsTable.insert(join, new TableOperationCallback<Join>() {
                    public void onCompleted(Join entity,
                                            Exception exception,
                                            ServiceFilterResponse response) {
                        if (exception == null) {
                            Log.i(LOG_TAG, "Inserted to JOINS object with ID " + entity.Id);
                        } else {
                            Log.e(LOG_TAG, exception.getMessage());
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
