package com.theguardians.citywalker.ui;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theguardians.citywalker.Model.ContactHandler;
import com.theguardians.citywalker.Model.PoliceStation;
import com.theguardians.citywalker.Model.UserContact;
import com.theguardians.citywalker.R;
import com.theguardians.citywalker.Service.DataFromFirebase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContactEmergencyActivity extends AppCompatActivity {


    private FusedLocationProviderClient fusedLocationClient;
    private boolean mLocationPermissionGranted = false;
    private boolean mSMSPermissionGranted =false;
    private LatLng userLocation;
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final int  PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;

    private String phoneNo;
    private String message;
    private String userLocationAddress;
    private FloatingActionButton sendLocation;
    private  Button editContact;
    private Button navigateToPolice;
    private String userName;
    private String userPhoneNumber;
    private TextView name;
    private TextView userPhNo;
    private int Id;
    Bundle extras = new Bundle ();
    //add Firebase Database stuff

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference policeStationRef;
    private DatabaseReference openShopRef;
    private DataFromFirebase dataFromFirebase = new DataFromFirebase ();


    private static PoliceStation pInfo = new PoliceStation ();

    private JSONArray policeStationArray = new JSONArray ();
    private JSONArray openShopArray = new JSONArray ();
    private JSONArray result =new JSONArray ();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ...
        super.onCreate (savedInstanceState);
        setContentView (R.layout.contact_emergency_support);
        Toolbar toolbar = findViewById (R.id.toolbar);
        toolbar.setTitle ("Emergency Support");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar (toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ContactEmergencyActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        /**
         Collecting data from Firebase and storing to JSONArray
         */
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        policeStationRef = mFirebaseDatabase.getReference("police_location");
        openShopRef = mFirebaseDatabase.getReference ("24hr_stores");

        policeStationArray = dataFromFirebase.getPoliceStationArray (policeStationRef);
        openShopArray =dataFromFirebase.getOpenShopArray (openShopRef);

        extras = getIntent().getExtras();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient (this);
        sendLocation = findViewById(R.id.sendLocation);
        editContact =findViewById (R.id.editContact);
        userPhNo = findViewById (R.id.phoneNumberValue);
        name = findViewById (R.id.nameValue);
        navigateToPolice =findViewById (R.id.navigatetopolice);
        ContactHandler handler =new ContactHandler (this);
        // Reading all contacts
        List<UserContact> contacts = handler.readAllContacts();
        Id=contacts.get (0).getId ();
        userName = contacts.get (0).getName ();
        userPhoneNumber = contacts.get (0).getPhoneNumber ();

        userPhNo.setText (userPhoneNumber);
        name.setText (userName);


        if (mLocationPermissionGranted) {
            getUserLocation();
        }
        else {
            getLocationPermission();
        }

        if (mSMSPermissionGranted){

            System.out.print ("SMS is there");
        }
        else
        {
            getSMSPermission ();
        }


        editContact.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactEmergencyActivity.this, UpdateContactActivity.class);
                //extras.putExtra ("Id",Id);
                intent.putExtra("Id",Id);
                startActivity(intent);
            }
        });

        sendLocation.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                //userLocationValue.setText (userLocation.toString ());

                try {
                    getUserLocation ();
                    userLocationAddress = getUserLocationDetails (userLocation.latitude, userLocation.longitude);
                    phoneNo = userPhoneNumber;
                    message = "HELP ME !!!! I am at Location: " + userLocationAddress + " Coordinates: " + userLocation + " **Emergency Distress Message sent from CityWalker App**";

                    sendSMSMessage ();
                } catch (IOException e) {
                    e.printStackTrace ();
                }


            }
        });

        navigateToPolice.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                 getUserLocation ();
                 if(userLocation!=null){
                 calculateDistances(userLocation);}
                 else {
                     getUserLocation ();
                     calculateDistances(userLocation);
                 }
            }
        });


    }

    private void calculateDistances(LatLng userLocation) {


        if(userLocation==null){
            System.out.println ("Yes");
        }
        else{
            System.out.println ("No" +userLocation);
        }
        List<Float> pathDistances = new ArrayList<> ();
        PoliceStation nearestStation = new PoliceStation ();

            for (int i = 0; i < policeStationArray.length (); i++) {
                try {
                    float distance = 0;
                    JSONObject jsonobject = policeStationArray.getJSONObject (i);

                    String lat = jsonobject.getString ("latitude");
                    String lon = jsonobject.getString ("longitude");
                    String polSat = jsonobject.getString ("police_station");
                    String address = jsonobject.getString ("address");
                    String tel = jsonobject.getString ("tel");
                    pInfo = new PoliceStation ();
                    pInfo.setLatitude (Double.parseDouble (lat));
                    pInfo.setLongitude (Double.parseDouble (lon));
                    pInfo.setPolice_station (polSat);
                    pInfo.setAddress (address);
                    pInfo.setTel (tel);

                    Location crntLocation = new Location (LocationManager.GPS_PROVIDER);
                    crntLocation.setLatitude (userLocation.latitude);
                    crntLocation.setLongitude (userLocation.longitude);

                    Location newLocation = new Location (LocationManager.GPS_PROVIDER);
                    newLocation.setLatitude (pInfo.getLatitude ());
                    newLocation.setLongitude (pInfo.getLongitude ());


                     distance = crntLocation.distanceTo(newLocation);

                    System.out.println ("Distance: " + distance);
                    System.out.println ("Station: " + pInfo.getPolice_station ());
                    pathDistances.add (distance);
                    try {

                        JSONObject jsonObject1 = new JSONObject ();
                        jsonObject1.put ("distance", distance);
                        jsonObject1.put ("policeStation", pInfo);

                        result.put (jsonObject1);

                    } catch (JSONException e) {
                        e.printStackTrace ();
                    }
                } catch (JSONException e) {
                    e.printStackTrace ();
                }
            }


        System.out.println ("Path Distance: " +pathDistances);


        //float minDistance = getMinValue (pathDistances);
        //System.out.println ("MinDistance: " +minDistance);
        /*
        PoliceStation policeStation =new PoliceStation ();
        try{
            for(int j=0; j<result.length ();j++){

                JSONObject jsonObject = result.getJSONObject (j);
                float dis = Float.parseFloat (jsonObject.getString ("distance"));
                policeStation = (PoliceStation) jsonObject.get ("policeStation");
                if(Float.compare(minDistance, dis) == 0){
                    nearestStation = policeStation;
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace ();
        }

        System.out.println ("Station: "+nearestStation.getPolice_station ());
        System.out.println ("Address" +nearestStation.getAddress ());

       */
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
     * Ask for location permission
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getUserLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
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

                            //System.out.println ("NEw user location" +userLocation);
                        }
                    }
                });


    }

    public void sendSMSMessage(){
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
            Toast.makeText(ContactEmergencyActivity.this, "Message Sent Successfully to Your Guardian", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(ContactEmergencyActivity.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    public void getSMSPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);

            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mSMSPermissionGranted = true;
                }
            }

        }
    }

    /**
     * Return minimum value
     */


    public static float getMinValue(List<Float> numbers){
        float minValue = numbers.get (0);
        for(int i=1;i<numbers.size ();i++){
            if(numbers.get (i) < minValue){
                minValue = numbers.get (i);
            }
        }
        return minValue;
    }


}
