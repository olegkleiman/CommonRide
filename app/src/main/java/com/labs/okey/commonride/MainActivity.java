package com.labs.okey.commonride;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
import com.labs.okey.commonride.model.Ride;
import com.microsoft.windowsazure.mobileservices.*;

import com.facebook.*;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import org.apache.http.StatusLine;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
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

    MobileServiceClient wamsClient;

    public static final String SENDER_ID = "<PROJECT_NUMBER>";

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

        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                wams_GetRides(accessToken);
        }

        NotificationsManager.handleNotifications(this, SENDER_ID, GCMHandler.class);
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

                                        MobileServiceTable<Ride> ridesTable = wamsClient.getTable("commonrides", Ride.class);
                                        ridesTable//.where().field("when_starts").gt(new Date())
                                                .execute(new TableQueryCallback<Ride>() {
                                                             @Override
                                                             public void onCompleted(List<Ride> rides,
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
                                                                     setupRidesListView(rides);
                                                                 }
                                                                 progress.dismiss();

                                                             }
                                                         }
                                                );

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

    private void setupRidesListView(List<Ride> rides){
        final ListView listview = (ListView) findViewById(R.id.listview);

        RidesAdapter ridesAdapter = new RidesAdapter(MainActivity.this,
                R.layout.ride_item_row, rides, null);
        listview.setAdapter(ridesAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final Ride ride = (Ride) parent.getItemAtPosition(position);

                Intent intent = new Intent(MainActivity.this,
                        SingleRideActivity.class);
                Bundle b = new Bundle();
                b.putString("rideId", ride.Id);
                intent.putExtras(b);
                startActivity(intent);

//                        view.animate().setDuration(2000).alpha(0)
//                                .withEndAction(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        rides.remove(ride);
//                                        ridesAdapter.notifyDataSetChanged();
//                                        view.setAlpha(1);
//                                    }
//                                });

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                                // TODO:
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
}
