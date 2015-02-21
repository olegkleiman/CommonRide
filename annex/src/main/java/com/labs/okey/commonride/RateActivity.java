package com.labs.okey.commonride;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.labs.okey.commonride.adapters.RatingAdapter;
import com.labs.okey.commonride.adapters.RidesAdapter;
import com.labs.okey.commonride.model.RideAnnotated;
import com.labs.okey.commonride.utils.Globals;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOrder;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;

import java.net.MalformedURLException;


public class RateActivity extends ActionBarActivity {

    private static final String LOG_TAG = "Annex.Rating";

    static MobileServiceClient wamsClient;
    private MobileServiceSyncTable<RideAnnotated> mRidesTable;
    private Query mPullQuery;

    RatingAdapter mRatingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        ListView listView = (ListView)findViewById(R.id.listViewRating);
        mRatingAdapter = new RatingAdapter(this, R.layout.rating_item_row);
        listView.setAdapter(mRatingAdapter);

        refreshRides();
    }

    private void refreshRides() {

        try {
            wamsClient = new MobileServiceClient(
                            Globals.WAMS_URL,
                            Globals.WAMS_API_KEY,
                            this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... params) {

                try {

                    mRidesTable = wamsClient.getSyncTable("rides_annotated",
                            RideAnnotated.class);
                    mPullQuery = wamsClient.getTable(RideAnnotated.class).orderBy("when_started", QueryOrder.Ascending);
                    final MobileServiceList<RideAnnotated> rides =
                            mRidesTable.read(mPullQuery).get();

                    mRatingAdapter.clear();

                    for (RideAnnotated _ride : rides) {
                        mRatingAdapter.add(_ride);
                    }

                } catch (Exception ex) {
                    Log.e(LOG_TAG, ex.getMessage());
                }

                return null;
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rate, menu);
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
