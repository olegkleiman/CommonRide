package com.labs.okey.commonride;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.labs.okey.commonride.model.RideAnnotated;
import com.labs.okey.commonride.model.User;
import com.labs.okey.commonride.utils.Globals;
import com.labs.okey.commonride.utils.RoundedDrawable;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;


public class SettingsTabsActivity extends ActionBarActivity {

    private static final String LOG_TAG = "Annex.SettingsActivity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    ScreenSlidePagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(),
                                                            this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }

    public void gotoUser(View v) {
        mViewPager.setCurrentItem(0);
    }

    public void gotoPrefs(View v) {
        mViewPager.setCurrentItem(1);
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


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        private Context context;

        public ScreenSlidePagerAdapter(FragmentManager fm, Activity context) {
            super(fm);

            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            return PlaceholderFragment.newInstance(position + 1, context);// getApplicationContext());
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, Context context) {
            PlaceholderFragment fragment = new PlaceholderFragment(context);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        Context mContext;

        public PlaceholderFragment() {

        }

        public PlaceholderFragment(Context context) {
            mContext = context;
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {

            Bundle bundle = this.getArguments();
            int nSection =  bundle.getInt(ARG_SECTION_NUMBER);

            View rootView = null;
            switch( nSection) {
                case 1: {

                    if( mContext == null )
                        return null;

                    final User user = User.load(mContext);

                    rootView = inflater.inflate(R.layout.fragment_user, container, false);
                    final ImageView imgUserPic = (ImageView)rootView.findViewById(R.id.imgViewUserSettings);

                    try {
                        Drawable drawable = Globals.drawMan.userDrawable(mContext,
                                                          user.getRegistrationId(),
                                                          user.getPictureURL()).get();
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

//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                    final File myPhotoFile = getFilePath(Globals.MY_PICTURE_FILE_NAME);
//
//                    Bitmap myPictureBitmap;
//                    if(myPhotoFile.exists()) {
//
//                        try {
//
//                            myPictureBitmap =
//                                BitmapFactory.decodeFile(myPhotoFile.toString(), options);
//
//                            Drawable drawable = new BitmapDrawable(myPictureBitmap);
//                            drawable = RoundedDrawable.fromDrawable(drawable);
//                            ((RoundedDrawable) drawable)
//                                    .setCornerRadius(Globals.PICTURE_CORNER_RADIUS)
//                                    .setBorderColor(Color.LTGRAY)
//                                    .setBorderWidth(Globals.PICTURE_BORDER_WIDTH)
//                                    .setOval(true);
//                            imgUserPic.setImageDrawable(drawable);
//
//                        } catch (Exception ex) {
//                            Log.e(LOG_TAG, ex.getCause().toString());
//                        }
//
//                        myPhotoFile.delete();
//
//                    } else {
//                        // If the picture was not there, download it from Web
//
//                        new AsyncTask<Void, Void, Void>(){
//
//                            Drawable drawable = null;
//
//                            @Override
//                            protected void onPostExecute(Void result){
//                                if( drawable != null ) {
//                                    drawable = RoundedDrawable.fromDrawable(drawable);
//                                    ((RoundedDrawable) drawable)
//                                            .setCornerRadius(20)
//                                            .setBorderColor(Color.LTGRAY)
//                                            .setBorderWidth(4)
//                                            .setOval(true);
//
//                                    imgUserPic.setImageDrawable(drawable);
//                                }
//                            }
//
//                            @Override
//                            protected Void doInBackground(Void... voids) {
//
//                                try {
//                                    InputStream is = fetch(user.getPictureURL());
//                                    drawable = Drawable.createFromStream(is, "src");
//
//                                    Bitmap bmp = ((BitmapDrawable)drawable).getBitmap();
//                                    if( bmp != null ) {
//                                        myPhotoFile.createNewFile();
//                                        OutputStream fileOut = new FileOutputStream(myPhotoFile);
//                                        bmp.compress(Bitmap.CompressFormat.PNG, 100, fileOut);
//                                        fileOut.flush();
//                                        fileOut.close();
//                                    }
//                                } catch(Exception ex) {
//                                    Log.e(LOG_TAG, ex.getCause().toString());
//                                }
//
//                                return null;
//                            }
//                        }.execute();
//
//                    }

//                    DrawableManager drawableManager = new DrawableManager();
//                    drawableManager.setRounded()
//                            .setCornerRadius(20)
//                            .setBorderColor(Color.LTGRAY)
//                            .setBorderWidth(4);
//                    drawableManager.fetchDrawableOnThread(user.getPictureURL(),
//                                                          imgUserPic);

                    TextView txtView = (TextView)rootView.findViewById(R.id.textViewFirstNameSettings);
                    txtView.setText(user.getFirstName() + " " + user.getLastName());


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
                                        ProgressDialog.show(mContext, "Saving...", "User's registration");

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

                }
                break;

                case 2: {
                    rootView = inflater.inflate(R.layout.fragment_prefs, container, false);

                    Button btnSave = (Button)rootView.findViewById(R.id.btnSettingsPrefsSave);
                    btnSave.setOnClickListener( new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {

                                                    }
                                                }
                    );
                }
                break;

            }

            return rootView;
        }
    }

}
