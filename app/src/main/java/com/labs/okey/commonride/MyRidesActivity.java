package com.labs.okey.commonride;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.UiLifecycleHelper;
import com.labs.okey.commonride.adapters.MyridesDriverAdapter;
import com.labs.okey.commonride.model.Ride;
import com.labs.okey.commonride.model.RideAnnotated;
import com.labs.okey.commonride.model.User;
import com.labs.okey.commonride.utils.ConflictResolvingSyncHandler;
import com.labs.okey.commonride.utils.Globals;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.MobileServiceSyncHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class MyRidesActivity extends ActionBarActivity {

    private static MobileServiceClient mClient;
    private MobileServiceSyncTable<Ride> mRidesTable;
    private Query mPullDriverQuery;
    private SQLiteLocalStore mLocalStore;

    ActionBar.Tab tab1, tab2;

    private static final String LOG_TAG = "CommonRide.MyRides";

    private static final String WAMSTOKENPREF = "wamsToken";
    private static final String USERIDPREF = "userid";

    Fragment fragmentTab1 = new FragmentTabOffers();
    MyridesDriverAdapter mDriverRidesAdapter;
    Fragment fragmentTab2 = new FragmentTabParticipation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rides);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        tab1 = actionBar.newTab().setText("Offers");
        tab2 = actionBar.newTab().setText("Participation");

        wamsInit();

        fragmentTab1 = new FragmentTabOffers();
        fragmentTab2 = new FragmentTabParticipation();

        tab1.setTabListener(new MyTabListener(fragmentTab1));
        tab2.setTabListener(new MyTabListener(fragmentTab2));

        actionBar.addTab(tab1);
        actionBar.addTab(tab2);


        refreshRides();
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

            mPullDriverQuery = mClient.getTable(Ride.class)
                    .where()
                    .field("user_driver")
                    .eq(myUser.getRegistrationId())
                    .and().field("when_starts").le(new Date());
            mLocalStore = new SQLiteLocalStore(mClient.getContext(),
                    "myrides", null, 1);
            MobileServiceSyncHandler handler = new ConflictResolvingSyncHandler();
            MobileServiceSyncContext syncContext = mClient.getSyncContext();
            if (!syncContext.isInitialized()) {
                Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                tableDefinition.put("id", ColumnDataType.String);
                tableDefinition.put("user_driver", ColumnDataType.String);
                tableDefinition.put("when_published", ColumnDataType.Date);
                tableDefinition.put("free_places", ColumnDataType.Number);
                tableDefinition.put("when_starts", ColumnDataType.Date);
                tableDefinition.put("ride_from", ColumnDataType.String);
                tableDefinition.put("ride_to", ColumnDataType.String);
                tableDefinition.put("from_lat", ColumnDataType.String);
                tableDefinition.put("from_lon", ColumnDataType.String);
                tableDefinition.put("to_lat", ColumnDataType.String);
                tableDefinition.put("to_lon", ColumnDataType.String);
                tableDefinition.put("notes", ColumnDataType.String);

                mLocalStore.defineTable("commonrides", tableDefinition);

                syncContext.initialize(mLocalStore, handler).get();
            }

            mRidesTable = mClient.getSyncTable("commonrides", Ride.class);

        } catch(Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }

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

                } catch(Exception ex) {
                    Log.e(LOG_TAG, ex.getCause().toString());
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
            case R.id.myrides_action_refresh: {

                final ProgressDialog progress =
                        ProgressDialog.show(this, "My rides", "Synchronizing...");

                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected void onPostExecute(Void result) {
                        progress.dismiss();
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            mRidesTable.purge(mPullDriverQuery);
                            mRidesTable.pull(mPullDriverQuery).get();

                            refreshRides();
                        }
                        catch (Exception ex) {
                            Log.e(LOG_TAG, ex.getCause().toString());
                        }

                        return null;
                    }
                }.execute();

            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ValidFragment")
    public class FragmentTabOffers extends android.support.v4.app.Fragment{

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState){

            View view = inflater.inflate(R.layout.my_rides_driver, container, false);

            User myUser = User.load(MyRidesActivity.this);
            TextView txtView = (TextView)view.findViewById(R.id.txtMyDriver);
            txtView.setText(myUser.getFirstName() + " " + myUser.getLastName());

            ListView myRidesListView = (ListView)view.findViewById(R.id.listViewMyDriver);
            mDriverRidesAdapter = new MyridesDriverAdapter(MyRidesActivity.this,
                    R.layout.my_rides_driver_row );

            myRidesListView.setAdapter(mDriverRidesAdapter);

            return view;
        }
    }

    @SuppressLint("ValidFragment")
    public class FragmentTabParticipation extends android.support.v4.app.Fragment{

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View view = inflater.inflate(R.layout.my_rides_passenger, container, false);

            ListView myRideslistView = (ListView)view.findViewById(R.id.listViewMyPassenger);

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
