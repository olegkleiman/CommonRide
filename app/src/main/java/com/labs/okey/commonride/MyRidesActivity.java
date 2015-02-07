package com.labs.okey.commonride;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
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
import com.labs.okey.commonride.model.Ride;
import com.labs.okey.commonride.utils.Globals;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableQueryCallback;

import java.util.List;


public class MyRidesActivity extends ActionBarActivity {

    static MobileServiceClient mClient;
    private MobileServiceTable<Ride> mRidesTable;
    ActionBar.Tab tab1, tab2;

    private static final String LOG_TAG = "CommonRide.MyRides";

    private static final String WAMSTOKENPREF = "wamsToken";
    private static final String USERIDPREF = "userid";

    Fragment fragmentTab1 = new FragmentTabOffers();
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

        tab1.setTabListener(new MyTabListener(fragmentTab1));
        tab2.setTabListener(new MyTabListener(fragmentTab2));

        actionBar.addTab(tab1);
        actionBar.addTab(tab2);

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

            mRidesTable = mClient.getTable("commonrides", Ride.class);
            mRidesTable//.where().field("")
                    .execute(new TableQueryCallback<Ride>() {
                        @Override
                        public void onCompleted(List<Ride> rides,
                                                int i,
                                                Exception e,
                                                ServiceFilterResponse serviceFilterResponse) {
                            if (e != null) {
                                Toast.makeText(MyRidesActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }else {
                            }
                        }
                    });



        } catch(Exception e) {
            Log.i(LOG_TAG, e.getMessage());
         }

     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_rides, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ValidFragment")
    public class FragmentTabOffers extends android.support.v4.app.Fragment{
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState){
            View view = inflater.inflate(R.layout.my_rides_driver, container, false);

            ListView myRideslistView = (ListView)view.findViewById(R.id.listViewMyDriver);
            //myRideslistView.setAdapter();

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
