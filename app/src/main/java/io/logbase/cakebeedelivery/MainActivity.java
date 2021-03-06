package io.logbase.cakebeedelivery;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.content.SharedPreferences;
import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import android.app.PendingIntent;
import java.util.Date;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.app.AlarmManager;
import java.util.Map;
import java.util.Arrays;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.content.ActivityNotFoundException;

public class MainActivity extends Activity implements OnItemSelectedListener {

    public Context context;
    LBProcessDialog mDialog = null;
    String versionName = "";
    String alerttype = "Ring tone";
    SharedPreferences sharedPref;
    Integer trackDefaultFreq;
    Integer trackOrderFreq;
    String[] idleTrackitems = new String[]{"3", "5", "10", "30"};
    String[] deliveryTrackitems = new String[]{"3", "5", "10"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        mDialog = new LBProcessDialog(this);
        sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        Firebase.setAndroidContext(this);
        initialize();
        registerAlarm(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Utility.checkLocationPermission()) {
            initializeSettings();
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void initializeSettings() {
        ((MyApp) context.getApplicationContext()).setCurrentActivity(this);
        CheckUpdates();

        if(sharedPref.getString("alerttype", null) != null)
            alerttype = sharedPref.getString("alerttype", null);
        else
            alerttype = "Ring tone";

        Spinner alerttypespinner = (Spinner)findViewById(R.id.alerttype);
        if(alerttype.contains("Notification")) {
            alerttypespinner.setSelection(1);
        }
        else {
            alerttypespinner.setSelection(0);
        }

        Spinner idleTrackTime = (Spinner)findViewById(R.id.idleTrackTime);
        trackDefaultFreq = sharedPref.getInt("TrackDefaultFreq", 0);
        trackDefaultFreq = trackDefaultFreq > 0 ? trackDefaultFreq : 30;
        int indexOfidleTrackTime = Arrays.asList(idleTrackitems).indexOf(trackDefaultFreq.toString());
        idleTrackTime.setSelection(indexOfidleTrackTime);

        setDeliveryTrackTimeArray(trackDefaultFreq);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeSettings();

                } else {
                    // permission denied
                }
                return;
            }
        }
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

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using

        switch(parent.getId()) {
            case R.id.alerttype:
                alerttype = (String) parent.getItemAtPosition(pos);
                break;
            case R.id.idleTrackTime:
                String idleTime = (String)parent.getItemAtPosition(pos);
                setDeliveryTrackTimeArray(Integer.parseInt(idleTime));
                break;
            case R.id.deliveryTrackTime:
                break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void saveDeviceID(View view) {
        if(Utility.checkLocationPermission()) {
            mDialog.StartProcessDialog();

            LinearLayout editlayout = (LinearLayout) findViewById(R.id.editlayout);
            String accountname = null;
            String username = null;

            if (editlayout.getVisibility() == View.GONE) {
                EditText accountnameText = (EditText) findViewById(R.id.accountname);
                accountname = accountnameText.getText().toString();

                EditText usernameText = (EditText) findViewById(R.id.username);
                username = usernameText.getText().toString();
            } else {
                EditText accountnameText = (EditText) findViewById(R.id.editaccountname);
                accountname = accountnameText.getText().toString();

                EditText usernameText = (EditText) findViewById(R.id.editusername);
                username = usernameText.getText().toString();
            }

            if ((accountname != null) && (!accountname.equals("")) && (username != null) && (!username.equals(""))) {
                CheckDeviceId(accountname, username);
            } else {
                ShowToast("Unable to save blank ID");
            }
        }
        else {
            ShowToast("Allow app to use location service.");
        }
    }

    private void initialize() {
        Spinner alerttypespinner = (Spinner)findViewById(R.id.alerttype);
        String[] items = new String[]{"Ring tone", "Notification"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        alerttypespinner.setAdapter(adapter);
        alerttypespinner.setOnItemSelectedListener(this);

        Spinner idleTrackTime = (Spinner)findViewById(R.id.idleTrackTime);
        ArrayAdapter<String> idleTrackadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, idleTrackitems);
        idleTrackTime.setAdapter(idleTrackadapter);
        idleTrackTime.setOnItemSelectedListener(this);

        Button savebutton = (Button)findViewById(R.id.savebutton);
        savebutton.getBackground().setColorFilter(0xFF00b5ad, PorterDuff.Mode.MULTIPLY);

        Button savebutton1 = (Button)findViewById(R.id.savebutton1);
        savebutton1.getBackground().setColorFilter(0xFF00b5ad, PorterDuff.Mode.MULTIPLY);

        String deviceID = sharedPref.getString("deviceID", null);
        String accountID = sharedPref.getString("accountID", null);

        LinearLayout editlayout = (LinearLayout)findViewById(R.id.editlayout);
        LinearLayout newlayout = (LinearLayout)findViewById(R.id.newlayout);

        if(accountID != null && deviceID != null) {
            newlayout.setVisibility(View.GONE);
            editlayout.setVisibility(View.VISIBLE);

            String username = sharedPref.getString("username", null);
            String accountname = sharedPref.getString("accountname", null);

            EditText usernametext = (EditText)findViewById(R.id.editusername);
            EditText usernametext1 = (EditText)findViewById(R.id.username);
            usernametext.setText(username);
            usernametext1.setText(username);

            EditText accountnametext = (EditText)findViewById(R.id.editaccountname);
            EditText accountnametext1 = (EditText)findViewById(R.id.accountname);
            accountnametext.setText(accountname);
            accountnametext1.setText(accountname);

            OrderAdded(accountID, deviceID);

            Bundle b = getIntent().getExtras();
            if(b == null || (b.getString("ChangeDevice") == null)) {
                Intent intent = new Intent(this, OrdersActivity.class);
                startActivity(intent);
            }
        } else {
            newlayout.setVisibility(View.VISIBLE);
            editlayout.setVisibility(View.GONE);
        }
    }


    private void setDeliveryTrackTimeArray(int trackDefaultFreq) {
        deliveryTrackitems = new String[]{"3", "5", "10"};
        if(trackDefaultFreq == 3) {
            deliveryTrackitems = new String[]{"3"};
        }
        else if(trackDefaultFreq == 5) {
            deliveryTrackitems = new String[]{"3", "5"};
        }

        Spinner deliveryTrackTime = (Spinner)findViewById(R.id.deliveryTrackTime);
        ArrayAdapter<String> deliveryTrackadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, deliveryTrackitems);
        deliveryTrackTime.setAdapter(deliveryTrackadapter);
        deliveryTrackTime.setOnItemSelectedListener(this);

        trackOrderFreq = sharedPref.getInt("TrackOrderFreq", 0);
        trackOrderFreq = trackOrderFreq > 0 ? trackOrderFreq : 10;
        int indexOfdeiveryTrackTime = Arrays.asList(deliveryTrackitems).indexOf(trackOrderFreq.toString());
        deliveryTrackTime.setSelection(indexOfdeiveryTrackTime >=0 ? indexOfdeiveryTrackTime : (deliveryTrackitems.length -1));
    }

    private void CheckDeviceId(String actname, String username) {
        final String accountname = actname.toLowerCase().trim();
        username = username.toLowerCase().trim();

        final Button savebutton = (Button)findViewById(R.id.savebutton);
        savebutton.setEnabled(false);

        Firebase myFirebaseRef = new Firebase(getString(R.string.friebaseurl)+"/accountusers/"+accountname+"/users/"+username);
        myFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                savebutton.setEnabled(true);
                if (snapshot.getValue() != null) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("deviceID", snapshot.getValue().toString());
                    editor.commit();
                    ((MyApp) context.getApplicationContext()).stopAndStartTracking();
                    GetAccountId(accountname, snapshot.getValue().toString());
                } else {
                    ShowToast("Account name or User name is incorrect");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void GetAccountId(String accountname, String devID) {
        final String deviceId = devID;
        Firebase myFirebaseRef = new Firebase(getString(R.string.friebaseurl)+"/accountusers/"+accountname+"/accountid");
        myFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mDialog.StopProcessDialog();
                GetAppSettings(snapshot.getValue().toString(), deviceId);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                mDialog.StopProcessDialog();
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void GetAppSettings(String accountid, String devid) {
        final String deviceId = devid;
        final String accountId = accountid;

        Firebase myFirebaseRef = new Firebase(getString(R.string.friebaseurl)+"/accounts/"+accountid+"/settings/agentapp");
        myFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                SharedPreferences.Editor editor = sharedPref.edit();
                if(snapshot.getValue() != null) {
                    editor.putBoolean("acceptEnabled", snapshot.child("accept").getValue() != null ? Boolean.parseBoolean(snapshot.child("accept").getValue().toString()) : false);
                    editor.putBoolean("startEnabled",  snapshot.child("start").getValue() != null ? Boolean.parseBoolean(snapshot.child("start").getValue().toString()) : false);
                    editor.putBoolean("pickupEnabled", snapshot.child("pickup").getValue() != null ? Boolean.parseBoolean(snapshot.child("pickup").getValue().toString()) : false);
                    editor.putBoolean("deliverEnabled", true);
                }
                else {
                    editor.putBoolean("acceptEnabled", false);
                    editor.putBoolean("startEnabled", false);
                    editor.putBoolean("pickupEnabled", false);
                    editor.putBoolean("deliverEnabled", true);
                }
                editor.commit();
                GoToOrders(accountId, deviceId);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                mDialog.StopProcessDialog();
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }


    private void ShowToast(String message) {
        mDialog.StopProcessDialog();
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void GoToOrders(String accountID, String deviceID) {
        String accountname = null;
        String username = null;

        LinearLayout editlayout = (LinearLayout)findViewById(R.id.editlayout);
        if(editlayout.getVisibility() == View.GONE) {
            EditText accountnameText = (EditText) findViewById(R.id.accountname);
            accountname = accountnameText.getText().toString();

            EditText usernameText = (EditText) findViewById(R.id.username);
            username = usernameText.getText().toString();
        }
        else {
            EditText accountnameText = (EditText) findViewById(R.id.editaccountname);
            accountname = accountnameText.getText().toString();

            EditText usernameText = (EditText) findViewById(R.id.editusername);
            username = usernameText.getText().toString();
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("deviceID", deviceID);
        editor.putString("accountID", accountID);
        editor.putString("accountname", accountname);
        editor.putString("username", username);
        editor.putString("alerttype", alerttype);

        Spinner idleTrackTime = (Spinner)findViewById(R.id.idleTrackTime);
        String idleTime = idleTrackTime.getSelectedItem().toString();
        editor.putInt("TrackDefaultFreq", Integer.parseInt(idleTime));

        Spinner deliveryTrackTime = (Spinner)findViewById(R.id.deliveryTrackTime);
        String deliveryTime = deliveryTrackTime.getSelectedItem().toString();
        editor.putInt("TrackOrderFreq", Integer.parseInt(deliveryTime));

        editor.commit();

        OrderAdded(accountID, deviceID);

        Intent intent = new Intent(this, OrdersActivity.class);
        startActivity(intent);
    }

    private void OrderAdded(String accountID, String deviceID){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String currentDate = sdf.format(new java.util.Date());

        Firebase myFirebaseRef = new Firebase(getString(R.string.friebaseurl)+"accounts/"+accountID+"/orders/"+deviceID+"/"+currentDate);
        myFirebaseRef.addChildEventListener(new ChildEventListener() {
            // Retrieve new posts as they are added to the database
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                if (snapshot.getKey().toString().contains("Logged") == false &&
                    snapshot.getKey().toString().contains("Activity") == false) {
                    ArrayList<String> orderlist;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    Set orders = sharedPref.getStringSet("OrderIds", null);
                    if (orders != null) {
                        orderlist = new ArrayList<String>(orders);
                        if (orderlist.indexOf(snapshot.getKey()) < 0) {
                            orders.add(snapshot.getKey());
                            editor.putStringSet("OrderIds", orders);
                            editor.commit();

                            NotifyNewOrder();
                        }
                    } else {
                        orders = new HashSet<String>();
                        orders.add(snapshot.getKey());
                        editor.putStringSet("OrderIds", orders);
                        editor.commit();
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot var1) {

            }

            @Override
            public void onChildChanged(DataSnapshot var1, String var2) {

            }

            @Override
            public void onChildMoved(DataSnapshot var1, String var2) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }

    private void NotifyNewOrder(){
        ((MyApp) context.getApplicationContext()).Notify("New order has been assigned");
    }

    private static void registerAlarm(Context context) {
        AlarmManager alarmManager=(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (60000 * 20), pendingIntent); //20 mins
    }

    private void CheckUpdates() {
        int code = 0;
        PackageInfo info = null;
        try {
            info = this.getPackageManager().getPackageInfo("io.logbase.cakebeedelivery", 0);
            versionName = info.versionName;
            code = info.versionCode;
            TextView versionnumber = (TextView)findViewById(R.id.versionnumber);
            versionnumber.setText("Version number: " +versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        final int versioncode = code;
        Firebase myFirebaseRef = new Firebase(getString(R.string.friebaseurl)+"/Config/StickAgent");
        myFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() != null && snapshot.getValue() != "") {
                    int latestcode = Integer.parseInt(snapshot.getValue().toString());
                    if (latestcode > versioncode) {
                        Uri uri = Uri.parse("market://details?id=io.logbase.cakebeedelivery");
                        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);

                        try {
                            startActivity(myAppLinkToMarket);

                        } catch (ActivityNotFoundException e) {

                            //the device hasn't installed Google Play
                            Toast.makeText(context, "You don't have Google Play installed", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The config failed: " + firebaseError.getMessage());
            }
        });
    }

    public static class AlarmReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            MyApp app = ((MyApp) context.getApplicationContext());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm a");
            String notifcationmessage = "";

            Map<String, String> yettopickuporders = app.getOrders(true);
            Iterator it = yettopickuporders.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                try {
                    Date date1 = dateFormat.parse((String) pair.getValue());
                    Date today = new Date();

                    long diffMins = (date1.getTime() - today.getTime()) / 60000;
                    // Difference less than 10 mins
                    if (diffMins <= 10) {
                        notifcationmessage = notifcationmessage + ("Order #" + pair.getKey() + " is due for pickup") + "\n";
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            Map<String, String> yetdeliverorders = app.getOrders(false);
            it = yetdeliverorders.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                try {
                    Date date1 = dateFormat.parse((String) pair.getValue());
                    Date today = new Date();

                    long diffMins = (date1.getTime() - today.getTime()) / 60000;
                    // Difference less than 10 mins
                    if (diffMins <= 10) {
                        notifcationmessage = notifcationmessage + ("Order #" + pair.getKey() + " is due for delivery") + "\n";
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if(notifcationmessage != "") {
                app.Notify(notifcationmessage);
            }
        }
    }
}
