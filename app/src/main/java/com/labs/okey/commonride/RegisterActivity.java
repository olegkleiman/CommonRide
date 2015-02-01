package com.labs.okey.commonride;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.labs.okey.commonride.model.Ride;
import com.labs.okey.commonride.model.User;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;


public class RegisterActivity extends FragmentActivity {

    private static final String LOG_TAG = "CommonRide.RegisterActivity";
    private final String fbProvider = "fb";
    private final String PENDING_ACTION_BUNDLE_KEY = "com.labs.okey.commomride:PendingAction";

    private static final String TOKENPREF = "accessToken";
    String mAccessToken;

    private UiLifecycleHelper uiHelper;
    private LoginButton loginButton;

    private GraphUser fbUser;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }

    private PendingAction pendingAction = PendingAction.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }

        setContentView(R.layout.activity_register);

        loginButton = (LoginButton) findViewById(R.id.loginButton);
        loginButton.setReadPermissions("email");

        loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                if (user != null) {
                    RegisterActivity.this.fbUser = user;

                    saveFBUser(user);

                    showRegistrationForm();
                }
            }
        });

        loginButton.setOnErrorListener(new LoginButton.OnErrorListener() {

            @Override
            public void onError(FacebookException error) {
                String msg = error.getMessage();
                msg.trim();

            }
        });
    }

    private void showRegistrationForm() {
        LinearLayout form = (LinearLayout)findViewById(R.id.register_form);
        form.setVisibility(View.VISIBLE);
    }

    public void registerUser(View v) {

        AutoCompleteTextView txtUser = (AutoCompleteTextView)findViewById(R.id.phone);
        if( txtUser.getText().toString().isEmpty() ) {

            String noPhoneNumber = getResources().getString(R.string.no_phone_number);
            txtUser.setError(noPhoneNumber);
            return;
        }

        final ProgressDialog progress = ProgressDialog.show(this, "Adding", "New user");

        try {
            Switch switchView = (Switch)findViewById(R.id.switchUsePhone);

            MobileServiceClient wamsClient =
                    new MobileServiceClient(
                            "https://commonride.azure-mobile.net/",
                            "RuDCJTbpVcpeCQPvrcYeHzpnLyikPo70",
                            this);

            // 'Users' table is defined with 'Anybody with the Applicatin Key'
            // permissions for READ and INSERT operations, so no authentication is
            // required for adding new user to it
            MobileServiceTable<User> usersTable =
                    wamsClient.getTable("users", User.class);
            User newUser = new User();
            newUser.registration_id = "Facebook:" + fbUser.getId();
            newUser.setFirstName( fbUser.getFirstName() );
            newUser.setLastName( fbUser.getLastName() );
            String pictureURL = "http://graph.facebook.com/" + fbUser.getId() + "/picture?type=large";
            newUser.picture_url = pictureURL;
            newUser.email = (String)fbUser.getProperty("email");
            newUser.phone = txtUser.getText().toString();
            newUser.usePhone = switchView.isChecked();

            usersTable.insert(newUser, new TableOperationCallback<User>(){

                @Override
                public void onCompleted(User user,
                                        Exception e,
                                        ServiceFilterResponse serviceFilterResponse) {
                    progress.dismiss();

                    if( e != null ) {
                        Toast.makeText(RegisterActivity.this,
                                e.getMessage(), Toast.LENGTH_LONG).show();
                    } else {

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(TOKENPREF, mAccessToken);
                        setResult(RESULT_OK, returnIntent);
                        finish();

                    }

                }
            });
        } catch(Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            progress.dismiss();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    private void saveFBUser(GraphUser fbUser) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("username", fbUser.getFirstName());
        editor.putString("registrationProvider", fbProvider);
        editor.putString("lastUsername", fbUser.getLastName());
        editor.putString(TOKENPREF, mAccessToken);

        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    private void handlePendingAction() {
        pendingAction = PendingAction.NONE;
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                        exception instanceof FacebookAuthorizationException)) {
//                new AlertDialog.Builder(RegisterActivity.this)
//                    .setTitle(R.string.cancelled)
//                    .setMessage(R.string.permission_not_granted)
//                    .setPositiveButton(R.string.ok, null)
//                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        } else if( state == SessionState.OPENED ) {
            mAccessToken = session.getAccessToken();
        }

    }
}
