package com.labs.okey.commonride;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by Oleg Kleiman on 14-Feb-15.
 */
public abstract class BaseActivity extends ActionBarActivity {

    public Boolean isNetworkConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.registerReceiver(this.mConnReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy(){
        this.unregisterReceiver(this.mConnReceiver);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if( !isNetworkConnected ) {
            MenuItem menuItem = selectMenuItem(menu,
                    R.id.action_refresh, R.id.action_single_ride_refresh);
            if( menuItem != null ) {
                menuItem.setIcon(R.drawable.alarm2_48);
                menuItem.setEnabled(false);
            }

            menuItem = selectMenuItem(menu,
                        // TODO: return R.id.action_add,
                        R.id.action_join_ride);
            if( menuItem != null ) {
                menuItem.setEnabled(false);
                menuItem.setVisible(false);
            }

            menuItem = selectMenuItem(menu, R.id.action_ride_delete);
            if( menuItem != null ) {
                menuItem.setEnabled(false);
                menuItem.setVisible(false);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    private MenuItem selectMenuItem(Menu menu,
                                int ...ids) {
        MenuItem menuItem = null;
        for(int _id : ids) {
            menuItem = menu.findItem(_id);
            if( menuItem != null )
                break;
        }

        return menuItem;
    }

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connManager =
                    (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connManager.getActiveNetworkInfo();

            isNetworkConnected = ( info != null && info.isConnected() );
            invalidateOptionsMenu();
        }
    };

}
