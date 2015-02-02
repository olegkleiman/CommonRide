package com.labs.okey.commonride;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.JsonObject;
import com.labs.okey.commonride.adapters.RidesAdapter;
import com.labs.okey.commonride.model.Ride;
import com.labs.okey.commonride.model.RideAnnotated;
import com.microsoft.windowsazure.mobileservices.*;

import com.facebook.*;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import org.apache.http.StatusLine;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = "CommonRide.Main";
    static final int REGISTER_USER_REQUEST = 1;

    final boolean DEVELOPER_MODE = true;
    private static final String TOKENPREF = "accessToken";
    private static final String WAMSTOKENPREF = "wamsToken";
    private static final String USERIDPREF = "userid";

    public final Object mAuthenticationLock = new Object();

    static MobileServiceClient wamsClient;

    public static final String SENDER_ID = "574878603809";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private String[] mDrawerTitles;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

         try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("KeyHash:", hash);
            }
        }
        catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        }
        catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        }

//        if (DEVELOPER_MODE) {
//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectNetwork()
//                    .penaltyLog()
//                    .build());
//
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectActivityLeaks()
//                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects()
//                    .penaltyLog()
//                    .build());
//        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerTitles = getResources().getStringArray(R.array.drawers_array);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

//        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);



        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if( sharedPrefs.getString(USERIDPREF, "").isEmpty() ) {

            try {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivityForResult(intent, REGISTER_USER_REQUEST);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
        } else {
                String accessToken = sharedPrefs.getString(TOKENPREF, "");
                Intent intent = getIntent();
                if( Intent.ACTION_SEARCH.equals(intent.getAction())) {
                    String query = intent.getStringExtra(SearchManager.QUERY);
                    wams_GetSearch(accessToken, query);
                } else {
                    wams_GetRides(accessToken);
                }

        }

        NotificationsManager.handleNotifications(this, SENDER_ID, GCMHandler.class);
    }

    private void wams_GetSearch(String accessToken, String query) {

        final ProgressDialog progress = ProgressDialog.show(this, "Downloading", "Search results");

        if( wamsClient == null ) {

        } else {
            MobileServiceTable<RideAnnotated> annotatedRidesTable =
                    wamsClient.getTable("rides_annotated", RideAnnotated.class);
            annotatedRidesTable
                    .parameter("searchquery", query)
                    .execute(new TableQueryCallback<RideAnnotated>() {

                        @Override
                        public void onCompleted(List<RideAnnotated> rides,
                                                int count,
                                                Exception error,
                                                ServiceFilterResponse serviceFilterResponse) {
                            progress.dismiss();

                            if( error != null) {
//                                String err = error.toString();
//                                Throwable t = error.getCause();
//
//                                while (t != null) {
//                                    err = err + "\n Cause: " + t.toString();
//                                    t = t.getCause();
//                                }

                                Toast.makeText(MainActivity.this,
                                                error.getMessage(),
                                                Toast.LENGTH_LONG).show();

                            } else {
                                setupRidesListView(rides);
                            }

                        }
                    });
        }

    }

    private void wams_GetRides(String accessToken){

        final ProgressDialog progress = ProgressDialog.show(this, "Downloading", "List of rides");

        try {
            wamsClient = new MobileServiceClient(
                    "https://commonride.azure-mobile.net/",
                    "RuDCJTbpVcpeCQPvrcYeHzpnLyikPo70",
                    this)
                    //.withFilter(new ProgressFilter());
                    .withFilter(new RefreshTokenCacheFilter());

            JsonObject body = new JsonObject();
            body.addProperty("access_token", accessToken);

            wamsClient.login(MobileServiceAuthenticationProvider.Facebook,
                        body,
                        new UserAuthenticationCallback() {
                            @Override
                            public void onCompleted(MobileServiceUser mobileServiceUser,
                                                    Exception exception,
                                                    ServiceFilterResponse serviceFilterResponse) {

                                synchronized(mAuthenticationLock) {

                                    if (exception == null) {

                                        saveUser(mobileServiceUser);

                                        MobileServiceTable<RideAnnotated> annotatedRidesTable =
                                                wamsClient.getTable("rides_annotated", RideAnnotated.class);
                                        annotatedRidesTable//.where().field("when_starts").le(new Date())
                                                .execute(new TableQueryCallback<RideAnnotated>(){

                                            @Override
                                            public void onCompleted(List<RideAnnotated> rides,
                                                                    int count,
                                                                    Exception error,
                                                                    ServiceFilterResponse serviceFilterResponse) {
                                                progress.dismiss();

                                                if( error != null) {
//                                                    String err = error.toString();
//                                                    Throwable t = error.getCause();
//
//                                                    while (t != null) {
//                                                        err = err + "\n Cause: " + t.toString();
//                                                        t = t.getCause();
//                                                    }

                                                    Toast.makeText(MainActivity.this,
                                                            error.getMessage(),
                                                            Toast.LENGTH_LONG).show();

                                                } else {
                                                    setupRidesListView(rides);
                                                }


                                            }
                                        });
                                    } else {
                                        progress.dismiss();
                                        Log.e(LOG_TAG, exception.getMessage());
                                    }

                                    mAuthenticationLock.notifyAll();
                                }
                            }
                        });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    private void saveUser(MobileServiceUser mobileServiceUser) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(WAMSTOKENPREF, mobileServiceUser.getAuthenticationToken());
        editor.putString(USERIDPREF, mobileServiceUser.getUserId());
        editor.commit();
    }

    private void setupRidesListView(List<RideAnnotated> rides){
        final ListView listview = (ListView) findViewById(R.id.listview);

        RidesAdapter ridesAdapter = new RidesAdapter(MainActivity.this,
                R.layout.ride_item_row, rides, null);
        listview.setAdapter(ridesAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final RideAnnotated ride = (RideAnnotated) parent.getItemAtPosition(position);

                Intent intent = new Intent(MainActivity.this,
                        SingleRideActivity.class);
                Bundle b = new Bundle();
                b.putString("rideId", ride.Id);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REGISTER_USER_REQUEST && resultCode == RESULT_OK) {

            Bundle bundle = data.getExtras();
            String accessToken = bundle.getString("accessToken");

            wams_GetRides(accessToken);
        }
    }

    private void logout(boolean shouldRedirectToLogin){

        //Clear the cookies so they won't auto login to a provider again
        //CookieSyncManager.createInstance(mContext);
        //CookieManager cookieManager = CookieManager.getInstance();
        //cookieManager.removeAllCookie();

        //Clear the user id and token from the shared preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor preferencesEditor = sharedPrefs.edit();
        preferencesEditor.clear();
        preferencesEditor.commit();

        wamsClient.logout();

        //Take the user back to the auth activity to relogin if requested
        if (shouldRedirectToLogin) {
            Intent logoutIntent = new Intent(this, RegisterActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(logoutIntent, REGISTER_USER_REQUEST);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if( mDrawerToggle != null )
            mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        if( mDrawerToggle != null )
            mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Rides to share");

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        if( searchView != null ) {
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    // returns true if the query has been handled by the listener,
                    // false to let the SearchView perform the default action.
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    // returns false if the SearchView should perform the default action of showing any suggestions if available,
                    // true if the action was handled by the listener.
                    return false;
                }
            });
            searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionSelect(int i) {
                    return false;
                }

                @Override
                public boolean onSuggestionClick(int i) {
                    return false;
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if( mDrawerToggle != null
            && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch ( item.getItemId() ) {
            case R.id.action_add: {
                onBtnAddRideClick(null);
            }
            return true;

            case R.id.action_refresh: {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String accessToken = sharedPrefs.getString(TOKENPREF, "");
                wams_GetRides(accessToken);
            }

            case R.id.action_search:
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBtnAddRideClick(View v) {
        Intent intent = new Intent(this, AddRideActivity.class);
        startActivity(intent);
    }

    /**
     * The RefreshTokenCacheFilter class filters responses for HTTP status code 401.
     * When 401 is encountered, the filter calls the authenticate method on the
     * UI thread. Out going requests and retries are blocked during authentication.
     * Once authentication is complete, the token cache is updated and
     * any blocked request will receive the X-ZUMO-AUTH header added or updated to
     * that request.
     */
    public class RefreshTokenCacheFilter implements ServiceFilter {

        @Override
        public void handleRequest(final ServiceFilterRequest request,
                                  NextServiceFilterCallback nextServiceFilterCallback,
                                  final ServiceFilterResponseCallback responseCallback) {
            String logStr = request.getUrl();
            Log.i(LOG_TAG, logStr);
            logStr = request.getContent();
            if( logStr != null && !logStr.isEmpty() )
                Log.i(LOG_TAG, logStr);

            //nextServiceFilterCallback.onNext(request, responseCallback);
            nextServiceFilterCallback.onNext(request,
                    new ServiceFilterResponseCallback() {

                        @Override
                        public void onResponse(ServiceFilterResponse response, Exception e) {
                            StatusLine status = response.getStatus();
                            int statusCode = status.getStatusCode();
                            if( statusCode == 401 ){
                                // TODO: Refresh authorization token
                                // see here: http://chrisrisner.com/Authentication-with-Android-and-Windows-Azure-Mobile-Services
                                // here: http://blogs.msdn.com/b/carlosfigueira/archive/2014/02/24/using-service-filters-with-the-mobile-services-javascript-sdk.aspx
                                // here: http://www.thejoyofcode.com/Handling_expired_tokens_in_your_application_Day_11_.aspx
                                // and here: http://blogs.msdn.com/b/carlosfigueira/archive/2014/03/13/caching-and-handling-expired-tokens-in-azure-mobile-services-managed-sdk.aspx

                                final CountDownLatch latch = new CountDownLatch(1);

                                logout(true);

                                MainActivity.this.runOnUiThread( new Runnable() {
                                    @Override
                                    public void run() {
                                        wamsClient.login(MobileServiceAuthenticationProvider.Facebook,
                                                new UserAuthenticationCallback() {
                                                    @Override
                                                    public void onCompleted(MobileServiceUser mobileServiceUser,
                                                                            Exception exception,
                                                                            ServiceFilterResponse serviceFilterResponse) {
                                                        if( exception == null ) {
                                                            //Update the requests X-ZUMO-AUTH header
                                                            request.removeHeader("X-ZUMO-AUTH");
                                                            //request.addHeader("X-ZUMO-AUTH", mClient.getCurrentUser().getAuthenticationToken());

                                                            //Add our BYPASS querystring parameter to the URL
                                                            Uri.Builder uriBuilder = Uri.parse(request.getUrl()).buildUpon();
                                                            uriBuilder.appendQueryParameter("bypass", "true");
                                                            try {
                                                                request.setUrl(uriBuilder.build().toString());
                                                            } catch (URISyntaxException e) {
                                                                Log.e(LOG_TAG, "Couldn't set request's new url: " + e.getMessage());
                                                                e.printStackTrace();
                                                            }

                                                            latch.countDown();
                                                        }
                                                    }
                                                });
                                    }
                                });

                                try {
                                    latch.await();
                                } catch (InterruptedException ex) {
                                    Log.e(LOG_TAG, "Interrupted exception: " + ex.getMessage());
                                    return;
                                }
                            }

                            //Return a response to the caller (otherwise returning from this method to
                            // RequestAsyncTask will cause a crash).
                            responseCallback.onResponse(response, e);
                        }
                    });

        }
    }

    /**
     * The RefreshTokenCacheFilter class filters responses for HTTP status code 401.
     * When 401 is encountered, the filter calls the authenticate method on the
     * UI thread. Out going requests and retries are blocked during authentication.
     * Once authentication is complete, the token cache is updated and
     * any blocked request will receive the X-ZUMO-AUTH header added or updated to
     * that request.
     */
    private class RefreshTokenCacheFilter_ implements ServiceFilter {

        @Override
        public void handleRequest(final ServiceFilterRequest request,
                                  final NextServiceFilterCallback nextServiceFilterCallback,
                                  final ServiceFilterResponseCallback responseCallback) {

            nextServiceFilterCallback.onNext(request, new ServiceFilterResponseCallback() {
                @Override
                public void onResponse(ServiceFilterResponse response, Exception exception) {
//                    if( exception != null ){
//                        Log.e(LOG_TAG, "RefreshTokenFilter onResponse exception: " + exception.getMessage());
//                    }
//                    StatusLine status = response.getStatus();
//                    int statusCode = status.getStatusCode();
//                    if( statusCode == 401 ) {
//
//                        //Return a response to the caller (otherwise returning from this method to
//                        //RequestAsyncTask will cause a crash).
//                        responseCallback.onResponse(response, exception);
//
//                        MainActivity.this.runOnUiThread( new Runnable() {
//                            @Override
//                            public void run() {
//
//                            }
//                        });
//                    }
                }
            });
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent,
                                View view,
                                int position,
                                long id) {
            switch ( position ){
                case 0: { // Settings
                    Intent intent = new Intent(MainActivity.this, TabsActivity.class);
                    startActivity(intent);
                }
                break;

                case 1: { // My Rides
                    Intent intent = new Intent(MainActivity.this, MyRidesActivity.class);
                    startActivity(intent);
                }
                break;

                case 4: { // About
                    Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(intent);
                }
                break;
            }

        }
    }

//    public static class PlaceholderFragment extends Fragment {
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_my, container, false);
//            return rootView;
//        }
//    }

    /**
     * This class makes the ad request and loads the ad.
     */
    public static class AdFragment extends Fragment {

        private AdView mAdView;

        public AdFragment() {
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
            // values/strings.xml.
            mAdView = (AdView) getView().findViewById(R.id.adView);

            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("4F039640448C4A8959EA044F68179499")
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();

            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_ad, container, false);
        }

        /** Called when leaving the activity */
        @Override
        public void onPause() {
            if (mAdView != null) {
                mAdView.pause();
            }
            super.onPause();
        }

        /** Called when returning to the activity */
        @Override
        public void onResume() {
            super.onResume();
            if (mAdView != null) {
                mAdView.resume();
            }
        }

        /** Called before the activity is destroyed */
        @Override
        public void onDestroy() {
            if (mAdView != null) {
                mAdView.destroy();
            }
            super.onDestroy();
        }

    }

}
