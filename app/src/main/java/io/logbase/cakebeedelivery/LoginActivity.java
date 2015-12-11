package io.logbase.cakebeedelivery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;

/**
 * Created by logbase on 30/11/15.
 */

public class LoginActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {

    Context context;
    String deviceID;
    String accountID;
    String currentDate;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        context = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        Firebase.setAndroidContext(this);

        getConfig();

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        deviceID = sharedPref.getString("deviceID", null);
        accountID = sharedPref.getString("accountID", null);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        currentDate = sdf.format(new java.util.Date());

        Button loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.getBackground().setColorFilter(0xFF00b5ad, PorterDuff.Mode.MULTIPLY);
        loginButton.setVisibility(View.GONE);

        isLoggedIn();
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    public void login(View view) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String currentdateandtime = sdf.format(new java.util.Date());

        Firebase myFirebaseRef = new Firebase(getString(R.string.friebaseurl)+"accounts/"+accountID+"/orders/"+deviceID+"/"+currentDate+"/LoggedOn");
        myFirebaseRef.setValue(currentdateandtime);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("loggedinDate", currentDate);
        editor.commit();

        //((MyApp) this.getApplication()).startDefaultTracking();

        getlocation();
    }

    public void stopTracking() {
        //((MyApp)this.getApplication()).stopTracking();
    }

    public void startDefaultTracking() {
        //((MyApp)this.getApplication()).startDefaultTracking();
    }

    private void GoToOrders() {
        Intent intent = new Intent(this, OrdersActivity.class);
        startActivity(intent);
    }

    private void isLoggedIn() {
        Firebase myFirebaseRef =  new Firebase(getString(R.string.friebaseurl)+"accounts/"+accountID+"/orders/"+deviceID+"/"+currentDate+"/LoggedOn");
        myFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Object obj = snapshot.getValue();
                if (obj != null && obj != "") {
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("loggedinDate", currentDate);
                    editor.commit();

                    startDefaultTracking();

                    GoToOrders();
                } else {
                    Button loginButton = (Button) findViewById(R.id.loginButton);
                    loginButton.setVisibility(View.VISIBLE);
                    setGoogleApiClient();

                    stopTracking();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        //ShowToast("onConnected: ");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
        //ShowToast("onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // This callback is important for handling errors that
        // may occur while attempting to connect with Google.
        //
        // More about this in the next section.
        //ShowToast("onConnectionFailed: ");
    }


    private void setGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void getlocation() {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Firebase myFirebaseRef =  new Firebase(getString(R.string.friebaseurl)+"accounts/"+accountID+"/orders/"+deviceID+"/"+currentDate+"/Loggedat");
            myFirebaseRef.setValue(mLastLocation.getLatitude() +" " +mLastLocation.getLongitude());
        }

        GoToOrders();
    }

    private void getConfig() {
        Firebase myFirebaseRef = new Firebase(getString(R.string.friebaseurl) + "Config");
        myFirebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Config config = snapshot.getValue(Config.class);

                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("TrackDefault", config.TrackDefault);
                editor.putInt("TrackDefaultFreq", config.TrackDefaultFreq);
                editor.putInt("TrackOrderFreq", config.TrackOrderFreq);
                editor.commit();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }
}