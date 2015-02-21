package com.labs.okey.commonride;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.labs.okey.commonride.model.*;
import com.labs.okey.commonride.adapters.PlaceAutoCompleteAdapter;
import com.labs.okey.commonride.model.Ride;
import com.labs.okey.commonride.pickers.DatePickerFragment;
import com.labs.okey.commonride.pickers.TimePickerFragment;
import com.labs.okey.commonride.utils.Globals;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;


import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;


public class AddRideActivity extends BaseActivity {

    private static final String LOG_TAG = "Annex.AddRide";

    private MobileServiceClient wamsClient;
    private MobileServiceTable<Ride> mRidesTable;

    private static final String WAMSTOKENPREF = "wamsToken";
    private static final String USERIDPREF = "userid";

    private Calendar mWhenStarts;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place/details";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyBJryLCLoWeBUnSTabBxwDL4dWO4tExR1c";

    private String from_lat;
    private String from_lon;
    private String to_lat;
    private String to_lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ride);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        AutoCompleteTextView autoCompViewFrom =
                (AutoCompleteTextView)findViewById(R.id.autocompleteFrom);
        autoCompViewFrom.setAdapter(new PlaceAutoCompleteAdapter(this, R.layout.search_map_list_item));
        autoCompViewFrom.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position,
                                    long id) {
                FoundPlace place = (FoundPlace)adapterView.getItemAtPosition(position);

                StringBuilder sb = new StringBuilder(PLACES_API_BASE + OUT_JSON);
                sb.append("?placeid=" + place.getPlaceID());
                sb.append("&key=" + API_KEY);

                CallPlaceDetails task = new CallPlaceDetails();
                task.setPlaceDescription(place.getDescription(), "from");
                task.execute(sb.toString());
            }
        });

        AutoCompleteTextView autoCompViewTo =
                (AutoCompleteTextView)findViewById(R.id.autocompleteTo);
        autoCompViewTo.setAdapter(new PlaceAutoCompleteAdapter(this, R.layout.search_map_list_item));
        autoCompViewTo.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position,
                                    long id) {
                FoundPlace place = (FoundPlace)adapterView.getItemAtPosition(position);

                StringBuilder sb = new StringBuilder(PLACES_API_BASE + OUT_JSON);
                sb.append("?placeid=" + place.getPlaceID());
                sb.append("&key=" + API_KEY);

                CallPlaceDetails task = new CallPlaceDetails();
                task.setPlaceDescription(place.getDescription(), "to");
                task.execute(sb.toString());
            }
        });

        TextView txtView = (TextView)findViewById(R.id.txtWhenDate);
        // See the samples of SimpleDateFormat here: http://developer.android.com/reference/java/text/SimpleDateFormat.html
        SimpleDateFormat df = new SimpleDateFormat("EEEE MMM dd, yyyy");
        txtView.setText(df.format(new Date()));

        txtView = (TextView)findViewById(R.id.txtWhenTime);
        SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
        txtView.setText(df2.format(new Date()));

        mWhenStarts = Calendar.getInstance();

        try {
            wamsClient = new MobileServiceClient(
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

            wamsClient.setCurrentUser(wamsUser);

            mRidesTable = wamsClient.getTable("commonrides", Ride.class);
        } catch(Exception e) {
            Log.i(LOG_TAG, e.getMessage());
        }
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment(new DatePickerDialog.OnDateSetListener(){


            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                TextView txtViewDate = (TextView)findViewById(R.id.txtWhenDate);

                Calendar c = Calendar.getInstance();
                c.set(year, month, day);
                Date dt = c.getTime();

                SimpleDateFormat df = new SimpleDateFormat("EEEE MMM dd, yyyy");
                txtViewDate.setText(df.format(dt));

                mWhenStarts.set(year, month, day);
            }
        });
        newFragment.show(getSupportFragmentManager(), "datePicker");

    }

    public void showTimePickerDialog(View v) {
        TimePickerFragment newFragment = new TimePickerFragment(new TimePickerDialog.OnTimeSetListener(){

            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                TextView txtView = (TextView)findViewById(R.id.txtWhenTime);

                Calendar c= Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                Date dt = c.getTime();

                SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                txtView.setText(df.format(dt));

                mWhenStarts.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mWhenStarts.set(Calendar.MINUTE, minute);
            }
        });
        newFragment.show(getSupportFragmentManager(), "timePicker");

    }

    public void btnAddRideClick(View v) {

        AutoCompleteTextView autoText = (AutoCompleteTextView)findViewById(R.id.autocompleteFrom);
        String from = autoText.getText().toString();
        if( from.isEmpty() ) {
            String error = getResources().getString(R.string.no_from_location);
            autoText.setError(error);
            return;
        }

        autoText = (AutoCompleteTextView)findViewById(R.id.autocompleteTo);
        String to = autoText.getText().toString();
        if( to.isEmpty() ) {
            String error = getResources().getString(R.string.no_to_location);
            autoText.setError(error);
            return;
        }

        EditText txtPassengers = (EditText)findViewById(R.id.txtNumberPassengers);
        int freePlaces = 0;
        String errorNoFreePlaces = getResources().getString(R.string.no_free_places);
        try {

            freePlaces = Integer.parseInt(txtPassengers.getText().toString());
            if( freePlaces == 0 ) {
                txtPassengers.setError(errorNoFreePlaces);
                return;
            }
        }
        catch(Exception ex) {
            txtPassengers.setError(errorNoFreePlaces);
            return;
        }

        final ProgressDialog progress = ProgressDialog.show(this, "Adding", "New ride");

        Ride ride = new Ride();
        ride.whenPublished = new Date();
        ride.whenStarts = this.mWhenStarts.getTime();

        ride.ride_from = from;
        ride.from_lat = from_lat;
        ride.from_lon = from_lon;

        ride.ride_to = to;
        ride.to_lat = to_lat;
        ride.to_lon = to_lon;

        ride.freePlaces = freePlaces;

        EditText txtNotes = (EditText)findViewById(R.id.txtRideNotes);
        ride.notes = txtNotes.getText().toString();

        //
        // Prepare sync context before use offline operation
        //
//        MobileServiceSyncTable<Ride> ridesTable =
//                wamsClient.getSyncTable("commonrides", Ride.class);
//        try {
//            ridesTable.insert(ride).get();
//            progress.dismiss();
//            finish();
//        } catch(InterruptedException ex) {
//            Log.e(LOG_TAG, ex.getMessage());
//        } catch(ExecutionException ex) {
//            Log.e(LOG_TAG, ex.getMessage());
//        }

        mRidesTable.insert(ride, new TableOperationCallback<Ride>() {
            public void onCompleted(Ride entity,
                                    Exception exception,
                                    ServiceFilterResponse response) {
                if (exception == null) {
                    finish();
                } else {
                    String err = exception.toString();
                    Throwable t = exception.getCause();

                    while (t != null) {
                        err = err + "\n Cause: " + t.toString();
                        t = t.getCause();
                    }

                    Toast.makeText(AddRideActivity.this,
                                   t.getMessage(),
                                   Toast.LENGTH_LONG).show();
                }
                progress.dismiss();
            }
        });
    }

    private class CallPlaceDetails extends AsyncTask<String,Object,String> {
        private String PlaceDescription;
        private String Tag;

        public void setPlaceDescription(String desc, String tag) {
            PlaceDescription = desc;
            Tag = tag;
        }

        // This method is called on main thread UI
        @Override
        protected void onPostExecute(String result) {
            try {
                // Create a JSON object hierarchy from the results
                JSONObject jsonObj = new JSONObject(result.toString());
                JSONObject jsonObjResult = jsonObj.getJSONObject("result");
                JSONObject jsonObjGeometry = jsonObjResult.getJSONObject("geometry");
                JSONObject jsonObjLocation = jsonObjGeometry.getJSONObject("location");

                String strLat = jsonObjLocation.getString("lat");
                String strLon = jsonObjLocation.getString("lng");

                if( Tag == "from") {
                    from_lat = strLat;
                    from_lon = strLon;
                } else if( Tag == "to" ) {
                    to_lat = strLat;
                    to_lon = strLon;
                }

            } catch (Exception ex) {

                Log.e(LOG_TAG, ex.getMessage());
                ex.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection conn = null;

            try {

                String strURL = params[0];
                URL url = new URL(strURL);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                StringBuilder jsonResults = new StringBuilder();

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }

                return jsonResults.toString();

            } catch (Exception ex) {
                Log.e(LOG_TAG, ex.getMessage());
                ex.printStackTrace();

                return "";
            }
        }
    }
}
