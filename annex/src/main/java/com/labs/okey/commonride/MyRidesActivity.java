package com.labs.okey.commonride;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.labs.okey.commonride.adapters.MyRidesDriverAdapter;
import com.labs.okey.commonride.adapters.MyRidesPassengerAdapter;
import com.labs.okey.commonride.model.JoinedRide;
import com.labs.okey.commonride.model.Ride;
import com.labs.okey.commonride.model.User;
import com.labs.okey.commonride.utils.ConflictResolvingSyncHandler;
import com.labs.okey.commonride.utils.Globals;
import com.labs.okey.commonride.utils.RoundedDrawable;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.MobileServiceSyncHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class MyRidesActivity extends BaseActivity {

    private static MobileServiceClient mClient;
    private MobileServiceSyncTable<Ride> mRidesTable;
    private MobileServiceSyncTable<JoinedRide> mJoinsTable;
    private Query mPullDriverQuery;
    private Query mPullPassengerQuery;

    //ActionBar.Tab tab1, tab2;

    private static final String LOG_TAG = "Annex.MyRides";

    private static final String WAMSTOKENPREF = "wamsToken";
    private static final String USERIDPREF = "userid";

    //Fragment fragmentTab1, fragmentTab2;
    MyRidesDriverAdapter mDriverRidesAdapter;
    MyRidesPassengerAdapter mPassengerRidesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rides);

        wamsInit();

        ActionBar actionBar = getSupportActionBar();

        // enable ActionBar app icon to behave as action to toggle nav drawer
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab()
                        .setText(getResources().getString(R.string.driverTabCaption))
                        .setTabListener(new MyTabListener(new FragmentTabOffers(this))));

        actionBar.addTab(actionBar.newTab()
                .setText(getResources().getString(R.string.passengerTabCaption))
                .setTabListener(new MyTabListener(new FragmentTabParticipation(this))));

    }

    private void wamsInit() {
        try {
            mClient = new MobileServiceClient(
                    Globals.WAMS_URL,
                    Globals.WAMS_API_KEY,
                    this);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String userID = sharedPrefs.getString(USERIDPREF, "");
            MobileServiceUser wamsUser = new MobileServiceUser(userID);

            String token = sharedPrefs.getString(WAMSTOKENPREF, "");
            // According to this article (http://www.thejoyofcode.com/Setting_the_auth_token_in_the_Mobile_Services_client_and_caching_the_user_rsquo_s_identity_Day_10_.aspx)
            // this should be JWT token, so use WAMS_TOKEM
            wamsUser.setAuthenticationToken(token);

            mClient.setCurrentUser(wamsUser);

            User myUser = User.load(this);

            mPullDriverQuery = mClient.getTable("commonrides", Ride.class)
                    .where()
                    .field("user_driver")
                    .eq(myUser.getRegistrationId())
                    .and().field("when_starts").le(new Date());

            mPullPassengerQuery = mClient.getTable("joined_rides", JoinedRide.class)
                    .where();
//                    .field("passenger_id")
//                    .eq(myUser.getRegistrationId())
//                    .and().field("when_joined").le(new Date());
            mPullPassengerQuery.parameter("passenger_id", myUser.getRegistrationId());

            SQLiteLocalStore mLocalStore =
                    new SQLiteLocalStore(mClient.getContext(),
                                        "myrides", null, 1);
            MobileServiceSyncHandler handler = new ConflictResolvingSyncHandler();
            MobileServiceSyncContext syncContext = mClient.getSyncContext();
            if (!syncContext.isInitialized()) {
                Map<String, ColumnDataType> tableDefinition = new HashMap<>();
                tableDefinition.put("id", ColumnDataType.String);
                tableDefinition.put("user_driver", ColumnDataType.String);
                tableDefinition.put("when_published", ColumnDataType.Date);
                tableDefinition.put("free_places", ColumnDataType.Number);
                tableDefinition.put("when_starts", ColumnDataType.Date);
                tableDefinition.put("ride_from", ColumnDataType.String);
                tableDefinition.put("ride_to", ColumnDataType.String);
                tableDefinition.put("isanonymous", ColumnDataType.Boolean);
                tableDefinition.put("from_lat", ColumnDataType.String);
                tableDefinition.put("from_lon", ColumnDataType.String);
                tableDefinition.put("to_lat", ColumnDataType.String);
                tableDefinition.put("to_lon", ColumnDataType.String);
                tableDefinition.put("notes", ColumnDataType.String);
                tableDefinition.put("__deleted", ColumnDataType.Boolean);
                tableDefinition.put("__version", ColumnDataType.String);
                mLocalStore.defineTable("commonrides", tableDefinition);

                Map<String, ColumnDataType> joinsTableDefinition = new HashMap<>();
                joinsTableDefinition.put("id", ColumnDataType.String);
                joinsTableDefinition.put("ride_id", ColumnDataType.String);
                joinsTableDefinition.put("when_joined", ColumnDataType.Date);
                joinsTableDefinition.put("status", ColumnDataType.String);
                joinsTableDefinition.put("ride_to", ColumnDataType.String);
                joinsTableDefinition.put("ride_from", ColumnDataType.String);
                joinsTableDefinition.put("__deleted", ColumnDataType.Boolean);
                joinsTableDefinition.put("__version", ColumnDataType.String);
                mLocalStore.defineTable("joined_rides", joinsTableDefinition);

                syncContext.initialize(mLocalStore, handler).get();
            }

            mRidesTable = mClient.getSyncTable("commonrides", Ride.class);
            mJoinsTable = mClient.getSyncTable("joined_rides", JoinedRide.class);

        } catch(Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }

    }

    private void refreshJoins() {

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    final MobileServiceList<JoinedRide> joins = mJoinsTable
                        .read(mPullPassengerQuery)
                        .get();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if( mPassengerRidesAdapter == null )
                                return;

                            mPassengerRidesAdapter.clear();

                            for (JoinedRide _join : joins) {
                                Log.i(LOG_TAG, _join.whenJoined.toString());
                                mPassengerRidesAdapter.add(_join);
                            }
                        }
                    });
                } catch(final Exception ex) {
                    Log.e(LOG_TAG, ex.getCause().toString());

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MyRidesActivity.this,
                                    ex.getCause().toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                return null;
            }
        }.execute();
    }

    private void refreshRides() {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    final MobileServiceList<Ride> rides = mRidesTable
                            .read(mPullDriverQuery)
                            .get();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if( mDriverRidesAdapter == null )
                                return;

                            mDriverRidesAdapter.clear();

                            for (Ride _ride : rides) {
                                Log.i(LOG_TAG, _ride.whenStarts.toString());
                                mDriverRidesAdapter.add(_ride);
                            }
                        }
                    });

                } catch(final Exception ex) {
                    Log.e(LOG_TAG, ex.getCause().toString());
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(MyRidesActivity.this,
                                    ex.getCause().toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                return null;
            }
        }.execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_rides, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch ( item.getItemId() ) {
            case R.id.action_refresh: {

                ActionBar actionBar = getSupportActionBar();
                ActionBar.Tab selectedTab = actionBar.getSelectedTab();
                int position = selectedTab.getPosition();

//                final ProgressDialog progress =
//                        ProgressDialog.show(this, "My rides", "Synchronizing...");
                item.setActionView(R.layout.action_progress);

                if( position == 0) {
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected void onPostExecute(Void result) {
                            // Ensure ProgressBar becomes original 'Refresh' menu item
                            invalidateOptionsMenu();
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                mRidesTable.purge(mPullDriverQuery);
                                mRidesTable.pull(mPullDriverQuery).get();

                                refreshRides();
                            } catch (Exception ex) {
                                Log.e(LOG_TAG, ex.getCause().toString());
                            }

                            return null;
                        }
                    }.execute();
                } else if( position == 1 ) { // Passenger tab
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected void onPostExecute(Void result) {
                            // Ensure ProgressBar becomes original 'Refresh' menu item
                            invalidateOptionsMenu();
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                mJoinsTable.purge(mPullPassengerQuery);
                                mJoinsTable.pull(mPullPassengerQuery).get();

                                refreshJoins();
                            } catch (Exception ex) {
                                Log.e(LOG_TAG, ex.getCause().toString());
                            }

                            return null;
                        }
                    }.execute();

                }

            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ValidFragment")
    public class FragmentTabOffers extends android.support.v4.app.Fragment{

        Context context;

        public FragmentTabOffers(Context context){
            this.context = context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState){

            View view = inflater.inflate(R.layout.my_rides_driver, container, false);

            User myUser = User.load(MyRidesActivity.this);
            TextView txtView = (TextView)view.findViewById(R.id.txtMyDriver);
            txtView.setText(myUser.getFirstName() + " " + myUser.getLastName());

            ImageView imageMe = (ImageView)view.findViewById(R.id.imageViewMe);
            try{

                Drawable drawable = (Globals.drawMan.userDrawable(context,
                        myUser.getRegistrationId(),
                        myUser.getPictureURL()))
                        .get();
                if( drawable != null ) {
                    drawable = RoundedDrawable.fromDrawable(drawable);
                    ((RoundedDrawable) drawable)
                            .setCornerRadius(Globals.PICTURE_CORNER_RADIUS)
                            .setBorderColor(Color.LTGRAY)
                            .setBorderWidth(Globals.PICTURE_BORDER_WIDTH)
                            .setOval(true);
                    imageMe.setImageDrawable(drawable);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getCause().toString());
            }

            ListView myRidesListView = (ListView)view.findViewById(R.id.listViewMyDriver);
            mDriverRidesAdapter = new MyRidesDriverAdapter(MyRidesActivity.this,
                    R.layout.my_rides_driver_row );
            myRidesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    Ride ride = (Ride) parent.getItemAtPosition(position);

                    Intent intent = new Intent(MyRidesActivity.this,
                                                SingleRideActivity.class);
                    Bundle b = new Bundle();
                    b.putString("rideId", ride.Id);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });

            myRidesListView.setAdapter(mDriverRidesAdapter);

            refreshRides();

            return view;
        }
    }

    @SuppressLint("ValidFragment")
    public class FragmentTabParticipation extends android.support.v4.app.Fragment{

        Context context;

        public FragmentTabParticipation(Context context){
            this.context = context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View view = inflater.inflate(R.layout.my_rides_passenger, container, false);
            User myUser = User.load(MyRidesActivity.this);
            TextView txtView = (TextView)view.findViewById(R.id.txtMyDriver);
            txtView.setText(myUser.getFirstName() + " " + myUser.getLastName());

            ImageView imageMe = (ImageView)view.findViewById(R.id.imageViewMe);
            try{

                Drawable drawable = (Globals.drawMan.userDrawable(context,
                        myUser.getRegistrationId(),
                        myUser.getPictureURL()))
                        .get();
                if( drawable != null ) {
                    drawable = RoundedDrawable.fromDrawable(drawable);
                    ((RoundedDrawable) drawable)
                            .setCornerRadius(Globals.PICTURE_CORNER_RADIUS)
                            .setBorderColor(Color.LTGRAY)
                            .setBorderWidth(Globals.PICTURE_BORDER_WIDTH)
                            .setOval(true);
                    imageMe.setImageDrawable(drawable);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getCause().toString());
            }

            ListView myJoinsListView = (ListView)view.findViewById(R.id.listViewMyPassenger);
            mPassengerRidesAdapter = new MyRidesPassengerAdapter(MyRidesActivity.this,
                        R.layout.my_rides_passenger_row);
            myJoinsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    JoinedRide ride = (JoinedRide) parent.getItemAtPosition(position);

                    Intent intent = new Intent(MyRidesActivity.this,
                            SingleRideActivity.class);
                    Bundle b = new Bundle();
                    b.putString("rideId", ride.rideId);
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });
            myJoinsListView.setAdapter(mPassengerRidesAdapter);

            refreshJoins();

            return view;
        }

    }

    public class MyTabListener implements ActionBar.TabListener {

        Fragment fragment;

        public MyTabListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab,
                                  FragmentTransaction ft) {
            ft.replace(R.id.fragment_container, fragment);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.remove(fragment);
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }
    }

}
