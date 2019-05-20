package com.theguardians.citywalker.ui;

import android.Manifest;
import android.annotation.SuppressLint;
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
        userLocation=getUserLocation ();
        context = this;
        FloatingActionButton fab = findViewById (R.id.fab);
        fab.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {

                if (contacts.size () > 0) {
                    String phNo = contacts.get (0).getPhoneNumber ();
                    callNumber (phNo);
                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setMessage("Please add an emergency guardian contact first. ");
                    builder1.setCancelable(true);
                    builder1.setNegativeButton(
                            "Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        });


        FloatingActionButton fab1 = findViewById (R.id.fab2);
        fab1.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {

                if (contacts.size () > 0) {
                    String phNo = contacts.get (0).getPhoneNumber ();

                    try {
                        userLocation = getUserLocation ();
                        if(userLocation!=null) {
                            userLocationAddress = getUserLocationDetails (userLocation.latitude, userLocation.longitude);
                            // System.out.println ("Hello " +userLocationAddress);
                            message = "HELP ME !!!! I am at Location: " + userLocationAddress + " Coordinates: " + userLocation + " **Emergency Distress Message sent from CityWalker App**";
                            sendSMSMessage (phNo, message);
                        }
                        else {
                            userLocation = getUserLocation ();
                            userLocationAddress = getUserLocationDetails (userLocation.latitude, userLocation.longitude);
                            // System.out.println ("Hello " +userLocationAddress);
                            message = "HELP ME !!!! I am at Location: " + userLocationAddress + " Coordinates: " + userLocation + " **Emergency Distress Message sent from CityWalker App**";
                            sendSMSMessage (phNo, message);
                        }
                        } catch (IOException e) {
                        e.printStackTrace ();
                    }

                } else {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setMessage("Please add an emergency guardian contact first. ");
                    builder1.setCancelable(true);
                    builder1.setNegativeButton(
                            "Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        });


        Button btnMap = (Button) findViewById (R.id.searchRouteBtn);
        Button btnMap2 = (Button) findViewById (R.id.emergencySupportBtn);
        Button btnMap3 = (Button) findViewById (R.id.aboutUs);
        Button btnMap4 = (Button) findViewById (R.id.safetyTips);
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
                    Intent intent = new Intent (MainActivity.this, NewEmergency.class);
                    startActivity (intent);
                } else {
                    Intent intent = new Intent (MainActivity.this, EmergencyActivity.class);
                    startActivity (intent);
                }
            }
        });

        btnMap3.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (MainActivity.this, AboutUsActivity.class);
                startActivity (intent);
            }
        });

        btnMap4.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (MainActivity.this, TipsActivity.class);
                startActivity (intent);
            }
        });
    }





    public void callNumber(String phoneNumber) {
        Intent intent = new Intent (Intent.ACTION_CALL);
        intent.setData (Uri.parse ("tel:" + phoneNumber));
        startActivity (intent);

    }


    @SuppressLint("MissingPermission")
    public LatLng getUserLocation(){


        fusedLocationClient.getLastLocation ()
                .addOnSuccessListener (this, new OnSuccessListener<Location> () {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            userLocation= new LatLng(location.getLatitude (),location.getLongitude ());

                            //System.out.println ("NEw user location" +userLocation);
                        }
                    }
                });

        return userLocation;
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



}
