package com.labs.okey.commonride;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.widget.FacebookDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.labs.okey.commonride.adapters.PassengersAdapter;
import com.labs.okey.commonride.model.Join;
import com.labs.okey.commonride.model.JoinAnnotated;
import com.labs.okey.commonride.model.Ride;
import com.labs.okey.commonride.model.User;
import com.labs.okey.commonride.utils.GMapV2Direction;
import com.labs.okey.commonride.utils.Globals;
import com.labs.okey.commonride.utils.RoundedDrawable;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableDeleteCallback;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SingleRideActivity extends BaseActivity {

    private static final String LOG_TAG = "Annex.SingleRide";

    private static final String WAMSTOKENPREF = "wamsToken";
    private static final String USERIDPREF = "userid";

    private MobileServiceClient mClient;
    private MobileServiceTable<Ride> mRidesTable;
    private MobileServiceTable<Join> mJoinsTable;
    private MobileServiceTable<JoinAnnotated> mJoinsAnnotatedTable;

    String myUserID;
    String mDriverID;
    int mFreePlaces;

    String mRideId;
    String mDriverPhone;
    String mDriverEMail;

    Toolbar mToolbar;

    private GoogleMap gMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_ride);

        mRideId = getIntent().getStringExtra("rideId");

        // enable ActionBar app icon to behave as action to toggle nav drawer
        try {
            mToolbar = (Toolbar) findViewById(R.id.annex_toolbar);
            setSupportActionBar(mToolbar);

            mToolbar.setTitle(R.string.title_activity_single_ride);
            mToolbar.setNavigationIcon(R.mipmap.icon_toolbal_arrow_white);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });


        }catch(Exception ex) {
            Log.e(LOG_TAG, ex.getMessage());
        }

        setupFloatingActionMenu();

        try {
            mClient = new MobileServiceClient(
                    Globals.WAMS_URL,
                    Globals.WAMS_API_KEY,
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
                    "Ride", "Downloading");

            mRidesTable = mClient.getTable("commonrides", Ride.class);
            mRidesTable.lookUp(mRideId, new TableOperationCallback<Ride>() {

                        public void onCompleted(Ride ride,
                                                Exception e,
                                                ServiceFilterResponse serviceFilterResponse) {
                            if( e == null ){

                                if( ride != null ) {

                                    mFreePlaces = ride.freePlaces;
                                    mDriverID = ride.getDriver();

                                    // 'Delete' menu item should be disabled for
                                    // any rides that published by not myUser.
                                    // Actual disabling is done in onPrepareOptionsMenu() method,
                                    // that is implied by invalidateOptionMenu() invocation.
                                    //if( !myUserID.equals(mDriverID) )
                                        invalidateOptionsMenu();

                                    TextView v = (TextView) findViewById(R.id.ride_from);
                                    v.setText(ride.ride_from);

                                    v = (TextView)findViewById(R.id.ride_to);
                                    v.setText(ride.ride_to);

                                    v = (TextView)findViewById(R.id.ride_when);
                                    SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy HH:mm");
                                    v.setText(df.format(ride.whenStarts));

                                    final TextView txtDriverName =
                                           (TextView) findViewById(R.id.txtDriverName);
                                    final TextView txtDriverEMail =
                                            (TextView) findViewById(R.id.txtDriverEMail);
                                    final TextView txtDriverPhone =
                                            (TextView) findViewById(R.id.txtDriverPhone);
                                    final ImageView imageDriver = (ImageView)findViewById(R.id.imgageViewDriver);
                                    final ImageView imageCallDriver = (ImageView)findViewById(R.id.imgCallDriver);

                                    if( ensureRideLocation(ride) ) {
                                        ensureMap();

                                        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                        gMap.getUiSettings().setMyLocationButtonEnabled(true);
                                        gMap.getUiSettings().setZoomControlsEnabled(false);

                                        gMap.getUiSettings().setZoomGesturesEnabled(false);
                                        gMap.getUiSettings().setScrollGesturesEnabled(false);
                                        gMap.getUiSettings().setTiltGesturesEnabled(false);
                                        gMap.getUiSettings().setRotateGesturesEnabled(false);

                                        gMap.setBuildingsEnabled(false);
                                        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                            @Override
                                            public void onMapClick(LatLng latLng) {
                                                showRideMap(null);
                                            }
                                        });

                                        Double _latCenter = (Double.parseDouble(ride.from_lat) +
                                                Double.parseDouble(ride.to_lat)) / 2;
                                        Double _lonCenter = (Double.parseDouble(ride.from_lon) +
                                                Double.parseDouble(ride.to_lon)) / 2;
                                        LatLng _rideCenter = new LatLng(_latCenter, _lonCenter);
                                        PositionMap(_rideCenter);

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
                                                           txtDriverName.setText(user.getFirstName() +
                                                                   " " + user.getLastName());
                                                           mDriverEMail = user.getEmail();
                                                           txtDriverEMail.setText(mDriverEMail);
                                                           if( user.getUsePhone() ) {
                                                               mDriverPhone = user.getPhone();
                                                               txtDriverPhone.setText(user.getPhone());
                                                           } else {
                                                               imageCallDriver.setVisibility(View.INVISIBLE);
                                                           }

                                                           try {
                                                                Drawable drawable = (Globals.drawMan.userDrawable(SingleRideActivity.this,
                                                                          user.getRegistrationId(),
                                                                          user.getPictureURL()))
                                                                          .get();
                                                               drawable = RoundedDrawable.fromDrawable(drawable);
                                                               ((RoundedDrawable) drawable)
                                                                       .setCornerRadius(Globals.PICTURE_CORNER_RADIUS)
                                                                       .setBorderColor(Color.LTGRAY)
                                                                       .setBorderWidth(Globals.PICTURE_BORDER_WIDTH)
                                                                       .setOval(true);

                                                               imageDriver.setImageDrawable(drawable);

                                                           } catch (Exception ex) {
                                                               Log.e(LOG_TAG, ex.getCause().toString());
                                                           }
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

    private void setupFloatingActionMenu(){
        // Floating button in Activity Context
        Button actionButton = (Button)findViewById(R.id.btnCommunicatePassengers);

        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);

        ImageView itemIconFacebook = new ImageView(this);
        itemIconFacebook.setImageDrawable(getResources().getDrawable(R.drawable.facebook32));
        // Check if the Facebook app is installed and we can present the share dialog
        final FacebookDialog.MessageDialogBuilder builder
                = new FacebookDialog.MessageDialogBuilder(this)
                .setLink("https://developers.facebook.com/docs/android/share/")
                .setName("Message Dialog Tutorial")
                .setCaption("Build great social apps that engage your friends.")
                .setPicture("http://i.imgur.com/g3Qc1HN.png")
                .setDescription("Allow your users to message links from your app using the Android SDK.");
        //.setFragment(this);

//        FacebookDialog.OpenGraphMessageDialogBuilder builder2 =
//                new FacebookDialog.OpenGraphMessageDialogBuilder(getActivity(), action, previewPropertyName);


        if ( !builder.canPresent()) {
            // TODO: Disable button within circle menu for FB
        }

        SubActionButton button1 = itemBuilder.setContentView(itemIconFacebook).build();
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    FacebookDialog dialog = builder.build();
                    dialog.present();
                } catch(Exception ex) {
                    Log.e(LOG_TAG, ex.getCause().toString());
                }
            }
        });

        ImageView itemIconWhatsup = new ImageView(this);
        itemIconWhatsup.setImageDrawable(getResources().getDrawable(R.drawable.whatsapp32));
        SubActionButton button2 = itemBuilder.setContentView(itemIconWhatsup).build();

        ImageView itemIconEmail = new ImageView(this);
        itemIconEmail.setImageDrawable(getResources().getDrawable(R.drawable.email32));
        SubActionButton button3 = itemBuilder.setContentView(itemIconEmail).build();

        ImageView itemIconShare = new ImageView(this);
        itemIconShare.setImageDrawable(getResources().getDrawable(R.drawable.sharethis32));
        SubActionButton button4 = itemBuilder.setContentView(itemIconShare).build();
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SingleRideActivity.this, "Make a phone call", Toast.LENGTH_LONG).show();
            }
        });

        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .setStartAngle(0)
                .setEndAngle(90)
                .setRadius(getResources().getDimensionPixelSize(R.dimen.radius_medium))
                .addSubActionView(button1)
                .addSubActionView(button2)
                .addSubActionView(button3)
                .addSubActionView(button4)
                        // ...
                .attachTo(actionButton)
                .build();

        // End Floating button

    }

    private boolean ensureRideLocation(Ride ride) {
        if( ride.from_lat == null || ride.from_lat.isEmpty()
                || ride.from_lon == null || ride.from_lon.isEmpty()
                || ride.to_lat == null || ride.to_lat.isEmpty()
                || ride.to_lon == null || ride.to_lon.isEmpty() ) {
            return false;
        } else
            return true;
    }

    private void refreshRide() {
        mRidesTable = mClient.getTable("commonrides", Ride.class);
        mRidesTable.lookUp(mRideId, new TableOperationCallback<Ride>() {

            public void onCompleted(Ride ride,
                                    Exception e,
                                    ServiceFilterResponse serviceFilterResponse) {
                if (e == null) {

                    if (ride != null) {

                        mFreePlaces = ride.freePlaces;
                        invalidateOptionsMenu();
                    }
                }
            }
        });
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
                                            Exception ex,
                                            ServiceFilterResponse serviceFilterResponse) {
                        // Ensure ProgressBar becomes original 'Refresh' menu item
                        invalidateOptionsMenu();

                        if (ex != null) {
                            Toast.makeText(SingleRideActivity.this,
                                        ex.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            setupJoinsListView(joins);
                        }
                    }
                });

    }

    private void setupJoinsListView(List<JoinAnnotated> joins){
        final ListView listview = (ListView) findViewById(R.id.listViewPassengers);

        PassengersAdapter ridesAdapter = new PassengersAdapter(SingleRideActivity.this,
                R.layout.passenger_item_row, joins,
                mDriverID.equals(myUserID));
        listview.setAdapter(ridesAdapter);
    }


    private ProgressDialog mProgress;

    public void updateJoin(JoinAnnotated join, String status) {

        mProgress = ProgressDialog.show(this, "Updating a ride", "Uploading...");

        if( join.Id.isEmpty() )
            return;

        final Join _join = new Join();
        _join.Id = join.Id;
        _join.rideId = join.ride_id;
        _join.setPassengerId( join.passengerId );
        _join.whenJoined = join.whenJoined;
        _join.status = status;

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected void onPostExecute(Void result) {
                refreshPassengers();

                mProgress.dismiss();
            }

            @Override
            protected Void doInBackground(Void... voids) {

                if( mJoinsTable == null ) {
                    mJoinsTable = mClient.getTable("joins", Join.class);
                }

                try {
                    mJoinsTable.update(_join).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();

    }

    public void callPassenger(View v) {

        try {
            String phoneNumber = (String)v.getTag();
            callPhoneNumber(phoneNumber);
        } catch(android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,
                    "Call failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void deletePassenger(final View v){

        String joinId = (String)v.getTag();

        final Join join = new Join();
        join.Id = joinId;

        final ProgressDialog progress = ProgressDialog.show(this,
                "Un-joining a ride", "Uploading...");

        mJoinsTable = mClient.getTable("joins", Join.class);
        mJoinsTable.delete(join, new TableDeleteCallback() {
            @Override
            public void onCompleted(Exception e,
                                    ServiceFilterResponse serviceFilterResponse) {

                progress.dismiss();

                if( e != null ) {
                    Toast.makeText(SingleRideActivity.this,
                            "Unable un-join. Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {

                    final ListView listView = (ListView)v.getParent().getParent().getParent();
                    if( listView != null ) {
                        final PassengersAdapter adapter = (PassengersAdapter)listView.getAdapter();
                        if( adapter != null ) {
                            final List<JoinAnnotated> joins = adapter.getJoins();
                            if( joins != null ) {

                                View animView = (View)v.getParent();

                                animView.animate().setDuration(1000).alpha(0)
                                        .withEndAction(new Runnable() {
                                            @Override
                                            public void run() {
                                                joins.remove(join);
                                                adapter.notifyDataSetChanged();
                                                listView.setAlpha(1);
                                            }
                                        } );

                                refreshRide();
                            }
                        }
                    }


                }
            }
        });
    }

    public void callPhoneNumber(String phoneNumber)
            throws android.content.ActivityNotFoundException {
        Intent phoneIntent = new Intent(Intent.ACTION_CALL);
        phoneIntent.setData(Uri.parse("tel:" + phoneNumber));

        startActivity(phoneIntent);
    }

    public void callDriver(View v){

        try {
            callPhoneNumber(mDriverPhone);
        } catch(android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,
                    "Call failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
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

    private void PositionMap(LatLng location){

        final int zoomLevel = 12;
        // Move the camera instantly to the current location with a zoom.
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));

        // Zoom in, animating the camera.
        gMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to specified zoom level, animating with a duration of 2 seconds.
        gMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel), 2000, null);
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
        join.status = Globals.JOIN_STATUS_INIT;

        final ProgressDialog progress = ProgressDialog.show(this,
                "Uploading", "Adding a ride");

        mJoinsTable = mClient.getTable("joins", Join.class);

        mJoinsTable.insert(join, new TableOperationCallback<Join>() {
                    public void onCompleted(Join entity,
                                            Exception exception,
                                            ServiceFilterResponse response) {
                        if (exception == null) {
                            refreshPassengers();
                            refreshRide();
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
            // Disable 'Delete' if ride was published by not owner of the ride
            if( !myUserID.equals( mDriverID) ) {
                MenuItem menuItem = menu.findItem(R.id.action_ride_delete);
                if( menuItem != null )
                    menuItem.setEnabled(false);
            }

            // Disable 'Join' if there is no free places
            // or this ride was published by the driver
            if( mFreePlaces == 0
                    || myUserID.equals( mDriverID)) {
                MenuItem menuItem = menu.findItem(R.id.action_join_ride);
                if( menuItem != null ) {
                    menuItem.setEnabled(false);
                    menuItem.setVisible(false);
                }
            }
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_single_ride, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch ( id ) {
            case R.id.action_ride_delete: {
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.dialog_title_confirm_join_delete)
                        .setMessage(R.string.dialog_message_confirm_join_delete)
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Join from default start location?")
                        .setTitle("Where you're joining?")
                        .setPositiveButton(getResources().getString(R.string.yes),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        joinRide();
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.no),
                                new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                } );
                builder.create().show();


            }
            break;

            case R.id.action_single_ride_refresh: {
                item.setActionView(R.layout.action_progress);
                refreshPassengers();

            }
            break;
        }


        return super.onOptionsItemSelected(item);
    }
}
