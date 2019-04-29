package com.theguardians.citywalker.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.theguardians.citywalker.Model.ContactHandler;
import com.theguardians.citywalker.Model.UserContact;
import com.theguardians.citywalker.R;
import com.theguardians.citywalker.RouteActivity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    private boolean mLocationPermissionGranted = false;
    private boolean mSMSPermissionGranted = false;
    private boolean mCallPermissionGranted = false;
    private Context context;
    private List<UserContact> contacts;
    private LatLng userLocation;
    private String userLocationAddress;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        Toolbar toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);



        fusedLocationClient = LocationServices.getFusedLocationProviderClient (this);
        ContactHandler handler = new ContactHandler (this);
        // Reading all contacts
        contacts = handler.readAllContacts ();

        context = this;
        FloatingActionButton fab = findViewById (R.id.fab);
        fab.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {

                if (contacts.size () > 0) {
                    String phNo = contacts.get (0).getPhoneNumber ();
                    System.out.println (phNo);
                    if (mCallPermissionGranted) {
                        checkCallServices (phNo);
                    } else {
                        getCallPermission (phNo);
                    }
                } else {
                    Snackbar.make (view, "Please Add an Emergency Contact Number", Snackbar.LENGTH_LONG)
                            .setAction ("Action", null).show ();
                }
            }
        });



        FloatingActionButton fab1 = findViewById (R.id.fab2);
        fab1.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {

                if (contacts.size () > 0) {
                    String phNo = contacts.get (0).getPhoneNumber ();
                    System.out.println (phNo);
                    if(mLocationPermissionGranted){

                        getUserLocation ();
                    }
                    else {

                        getLocationPermission ();
                    }
                    try {

                        userLocationAddress = getUserLocationDetails (userLocation.latitude, userLocation.longitude);
                        System.out.println ("Hello " +userLocationAddress);
                        message = "HELP ME !!!! I am at Location: " + userLocationAddress + " Coordinates: " + userLocation + " **Emergency Distress Message sent from CityWalker App**";

                        if (mSMSPermissionGranted) {
                            sendSMSMessage (phNo, message);
                        } else {
                            getSMSPermission (phNo,message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace ();
                    }

                } else {
                    Snackbar.make (view, "Please Add an Emergency Contact Number", Snackbar.LENGTH_LONG)
                            .setAction ("Action", null).show ();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater ().inflate (R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId ();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent (this, AboutUsActivity.class);
            startActivity (intent);
            return true;
        }

        return super.onOptionsItemSelected (item);
    }

    /**
     * OnResume Override
     */
    @Override
    protected void onResume() {
        super.onResume ();
        new CheckInternetConnection ().execute ();

    }

    /**
     * Check if internet connection is available or not through async task
     */

    public class CheckInternetConnection extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                int timeoutMs = 1500;
                Socket sock = new Socket ();
                SocketAddress sockaddr = new InetSocketAddress ("8.8.8.8", 53);

                sock.connect (sockaddr, timeoutMs);
                sock.close ();

                return true;
            } catch (Exception e) {

                return false;


            }
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {

            if (aBoolean) {

                checkLocationServices ();
                checkSMSServices ();
                getUserLocation ();
            } else {

                //Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder (context)
                        .setIcon (android.R.drawable.ic_dialog_alert)
                        .setTitle ("Closing the App")
                        .setMessage ("No Internet Connection,check your settings")
                        .setPositiveButton ("Close", new DialogInterface.OnClickListener () {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish ();
                            }

                        })
                        .show ();
            }
        }
    }


    /**
     * This methid check if the lccation permisission is enabled or not and
     * will ask them to be granted it its not given
     */
    private void checkLocationServices() {

        if (checkMapServices ()) {

            if (mLocationPermissionGranted) {

                getNextActivity ();
            } else {
                getLocationPermission ();
            }

        }


    }


    private void checkCallServices(String phNo) {
        if (mCallPermissionGranted) {

            callNumber (phNo);
        } else {
            getCallPermission (phNo);
        }
    }

    private void checkSMSServices() {

        if (mSMSPermissionGranted) {

            getNextActivity ();
        } else {
            getNewSMSPermission ();
        }
    }

    public void callNumber(String phoneNumber) {
        Intent intent = new Intent (Intent.ACTION_CALL);
        intent.setData (Uri.parse ("tel:" + phoneNumber));
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission (this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions (this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);

            // MY_PERMISSIONS_REQUEST_CALL_PHONE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            //You already have permission
            try {
                startActivity (intent);
            } catch (SecurityException e) {
                e.printStackTrace ();
            }

        }
    }

    public void getNextActivity() {

        Button btnMap = (Button) findViewById (R.id.searchRouteBtn);
        Button btnMap2 = (Button) findViewById (R.id.emergencySupportBtn);
        btnMap.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (MainActivity.this, RouteActivity.class);
                startActivity (intent);
            }
        });

        btnMap2.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                if (contacts.size () > 0) {
                    Intent intent = new Intent (MainActivity.this, ContactEmergencyActivity.class);
                    startActivity (intent);
                } else {
                    Intent intent = new Intent (MainActivity.this, EmergencyActivity.class);
                    startActivity (intent);
                }
            }
        });

    }

    /**
     *  Check whether maps service is available
     */

    private boolean checkMapServices() {
        if (isServicesOK ()) {
            if (isMapsEnabled ()) {
                return true;
            }
        }
        return false;
    }


    /**
     * No GPS alert message
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder (this);
        builder.setMessage ("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable (false)
                .setPositiveButton ("Yes", new DialogInterface.OnClickListener () {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent (android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult (enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create ();
        alert.show ();
    }


    /**
     * Check if location is enabled
     * @return
     */
    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService (Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled (LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps ();
            return false;
        }
        return true;
    }


    /**
     * Ask for location permission
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission (this.getApplicationContext (),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getNextActivity ();
        } else {
            ActivityCompat.requestPermissions (this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void getCallPermission(String phNo) {
        if (ContextCompat.checkSelfPermission (this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale (this,
                    Manifest.permission.CALL_PHONE)) {
            } else {
                ActivityCompat.requestPermissions (this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_CALL_PHONE);

            }

            Intent intent = new Intent (Intent.ACTION_CALL);
            intent.setData (Uri.parse ("tel:" + phNo));
            startActivity (intent);
        } else {

            Intent intent = new Intent (Intent.ACTION_CALL);
            intent.setData (Uri.parse ("tel:" + phNo));
            startActivity (intent);
        }

    }
    protected void getNewSMSPermission() {

        if (ContextCompat.checkSelfPermission (this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale (this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions (this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);

            }
        }
    }

    protected void getSMSPermission(String phNo,String message) {

        if (ContextCompat.checkSelfPermission (this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale (this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions (this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);

            }
        }
        try{


            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(message);


            ArrayList<PendingIntent> sentPIarr = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> deliveredPIarr = new ArrayList<PendingIntent>();

            for (int i = 0; i < parts.size(); i++) {
                sentPIarr.add(PendingIntent.getBroadcast(this, 0,new Intent(SENT), 0));
                deliveredPIarr.add(PendingIntent.getBroadcast(this, 0,new Intent(DELIVERED), 0));
            }

            sms.sendMultipartTextMessage(phNo, null, parts, sentPIarr, deliveredPIarr);
            Toast.makeText(MainActivity.this, "Message Sent Successfully to Your Guardian", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(MainActivity.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Is all services okay and app ready for use
     * @return
     */
    public boolean isServicesOK() {


        int available = GoogleApiAvailability.getInstance ().isGooglePlayServicesAvailable (MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests

            return true;
        } else if (GoogleApiAvailability.getInstance ().isUserResolvableError (available)) {
            //an error occured but we can resolve it

            Dialog dialog = GoogleApiAvailability.getInstance ().getErrorDialog (MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show ();
        } else {
            Toast.makeText (this, "You can't make map requests", Toast.LENGTH_SHORT).show ();
        }
        return false;
    }

    public void getUserLocation(){


        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }

        fusedLocationClient.getLastLocation ()
                .addOnSuccessListener (this, new OnSuccessListener<Location> () {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            userLocation= new LatLng(location.getLatitude (),location.getLongitude ());

                            System.out.println ("NEw user location" +userLocation);
                        }
                    }
                });


    }

    /**
     * On Request permission callback override
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
                else {

                    Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
                }
            }
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mSMSPermissionGranted =true;
                }else {

                    Toast.makeText(this, "SMS permission not granted", Toast.LENGTH_SHORT).show();
                }
            }
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mCallPermissionGranted = true;

                } else {

                    Toast.makeText(this, "Call permission not granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void sendSMSMessage(String phoneNo,String message){
        try{


            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(message);


            ArrayList<PendingIntent> sentPIarr = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> deliveredPIarr = new ArrayList<PendingIntent>();

            for (int i = 0; i < parts.size(); i++) {
                sentPIarr.add(PendingIntent.getBroadcast(this, 0,new Intent(SENT), 0));
                deliveredPIarr.add(PendingIntent.getBroadcast(this, 0,new Intent(DELIVERED), 0));
            }

            sms.sendMultipartTextMessage(phoneNo, null, parts, sentPIarr, deliveredPIarr);
            Toast.makeText(MainActivity.this, "Message Sent Successfully to Your Guardian", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(MainActivity.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    private String getUserLocationDetails(Double latitude,Double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

        return address;
    }

    /**
     * Calling location granting activity for result
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                    getNextActivity();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }

}
