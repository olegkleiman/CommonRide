package com.labs.okey.commonride;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.labs.okey.commonride.model.User;
import com.labs.okey.commonride.utils.Globals;
import com.labs.okey.commonride.utils.RoundedDrawable;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;


public class SettingsTabsActivity extends ActionBarActivity {

    private static final String LOG_TAG = "Annex.SettingsActivity";

    ActionBar.Tab tab1, tab2;
    Fragment fragmentTab1, fragmentTab2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        String caption = getResources().getString(R.string.settings_user);
        tab1 = actionBar.newTab().setText(caption);

        caption = getResources().getString(R.string.settings_prefs);
        tab2 = actionBar.newTab().setText(caption);

        fragmentTab1 = new FragmentTabProfile(this);
        fragmentTab2 = new FragmentTabPreferences(this);

        tab1.setTabListener(new MyTabListener(fragmentTab1));
        tab2.setTabListener(new MyTabListener(fragmentTab2));

        actionBar.addTab(tab1);
        actionBar.addTab(tab2);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_tabs, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressLint("ValidFragment")
    public static class FragmentTabProfile extends android.support.v4.app.Fragment{

        Context mContext;

        public FragmentTabProfile(Context context){
            this.mContext = context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_user, container, false);

            final User user = User.load(this.mContext);

            final ImageView imgUserPic = (ImageView)rootView.findViewById(R.id.imgViewUserSettings);
            try{

                Drawable drawable = (Globals.drawMan.userDrawable(mContext,
                        user.getRegistrationId(),
                        user.getPictureURL()))
                        .get();
                if( drawable != null ) {
                    drawable = RoundedDrawable.fromDrawable(drawable);
                    ((RoundedDrawable) drawable)
                            .setCornerRadius(Globals.PICTURE_CORNER_RADIUS)
                            .setBorderColor(Color.LTGRAY)
                            .setBorderWidth(Globals.PICTURE_BORDER_WIDTH)
                            .setOval(true);
                    imgUserPic.setImageDrawable(drawable);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getCause().toString());
            }

            EditText txtViewMail = (EditText)rootView.findViewById(R.id.txtUserEMailSettings);
            txtViewMail.setText(user.getEmail());

            final EditText txtViewPhone = (EditText)rootView.findViewById(R.id.txtUserPhoneSettings);
            txtViewPhone.setText(user.getPhone());

            final Switch switchView = (Switch)rootView.findViewById(R.id.switchUsePhoneSettings);
            switchView.setChecked(user.getUsePhone());

            ImageButton saveBtn = (ImageButton)rootView.findViewById(R.id.btnSaveSettings);
            saveBtn.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if( txtViewPhone.getText().toString().isEmpty() ) {
                        txtViewPhone.setError(getResources().getString(R.string.no_phone_number));
                        return;
                    }

                    user.setPhone(txtViewPhone.getText().toString());
                    user.setUsePhone(switchView.isChecked());

                    user.save(mContext);

                    try {
                        final ProgressDialog progress =
                                ProgressDialog.show(mContext, "Saving...", "User's profile");

                        // 'users' table is defined with 'Anybody with the Application Key' permissions
                        // for read and update operations,
                        // so no login is required for this instance of MobileServiceClient
                        MobileServiceClient wamsClient = new MobileServiceClient(
                                Globals.WAMS_URL,
                                Globals.WAMS_API_KEY,
                                mContext);
                        // We are going to update Azure table,
                        // so no need for Sync table in this case
                        final MobileServiceTable<User> usersTable =
                                wamsClient.getTable("users", User.class);

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... voids) {
                                try {

                                    // In order to update we need a user ID
                                    // as known in 'users' table
                                    MobileServiceList<User> _users =
                                            usersTable
                                                    .where()
                                                    .field("registration_id")
                                                    .eq(user.getRegistrationId())
                                                    .execute().get();
                                    if( _users.getTotalCount() > 0 ) {
                                        // Assign obtained ID to changed user object
                                        user.Id = _users.get(0).Id;

                                        // Perform actual update
                                        usersTable.update(user).get();
                                        //Toast.makeText(mContext, "Saved", Toast.LENGTH_LONG).show();
                                    }

                                } catch(Exception ex) {
                                    Log.e(LOG_TAG, ex.getMessage());
                                    Toast.makeText(mContext, "Exception: " + ex.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }finally {
                                    progress.dismiss();
                                }

                                return null;
                            }
                        }.execute();

                    } catch(Exception ex) {
                        Toast.makeText(mContext, "Exception: " + ex.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }


                }
            });

            return rootView;
        }

    }

    @SuppressLint("ValidFragment")
    public static class FragmentTabPreferences extends android.support.v4.app.Fragment {

        Context context;

        public FragmentTabPreferences(Context context) {
            this.context = context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_prefs, container, false);
            ImageButton btnSave = (ImageButton)rootView.findViewById(R.id.btnSettingsPrefsSave);
            btnSave.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            return rootView;
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
            ft.replace(R.id.settings_fragment_container, fragment);
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
