package com.theguardians.citywalker.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theguardians.citywalker.Model.ContactHandler;
import com.theguardians.citywalker.Model.OpenShop;
import com.theguardians.citywalker.Model.PoliceStation;
import com.theguardians.citywalker.Model.UserContact;
import com.theguardians.citywalker.R;

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

    private boolean mCallPermissionGranted = false;
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final int  PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;

    private String phoneNo;
    private String message;
    private String userLocationAddress;
    private FloatingActionButton sendLocation;
    private FloatingActionButton shareLocation;
    private FloatingActionButton emergencycall;
    private  Button editContact;
    private Button navigateToPolice;
    private Button navigateToShop;
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
    private PoliceStation nearestStation = new PoliceStation ();
    private  OpenShop nearestShop = new OpenShop ();

    private static PoliceStation pInfo = new PoliceStation ();
    private static OpenShop oInfo = new OpenShop ();

    private JSONArray policeStationArray = new JSONArray ();
    private JSONArray openShopArray = new JSONArray ();
    private JSONArray result =new JSONArray ();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate (savedInstanceState);
        setContentView (R.layout.contact_emergency_support);
        Toolbar toolbar = findViewById (R.id.toolbar);
        toolbar.setTitle ("Emergency Support");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar (toolbar);
        sendLocation = findViewById(R.id.sendLocation);
        editContact =findViewById (R.id.editContact);
        userPhNo = findViewById (R.id.phoneNumberValue);
        name = findViewById (R.id.nameValue);
        navigateToPolice =findViewById (R.id.navigatetopolice);
        navigateToShop =findViewById (R.id.navigatetoshop);
        shareLocation =findViewById (R.id.sharelocation);
        emergencycall =findViewById (R.id.emergencycall);

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

        policeStationArray = getPoliceStationArray (policeStationRef);
        openShopArray = getOpenShopArray (openShopRef);

        navigateToPolice.setEnabled (false);
        navigateToShop.setEnabled (false);
        extras = getIntent().getExtras();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient (this);

        ContactHandler handler =new ContactHandler (this);
        // Reading all contacts
        List<UserContact> contacts = handler.readAllContacts();
        Id=contacts.get (0).getId ();
        userName = contacts.get (0).getName ();
        userPhoneNumber = contacts.get (0).getPhoneNumber ();

        userPhNo.setText (userPhoneNumber);
        name.setText (userName);


        if (mLocationPermissionGranted) {
            userLocation=getUserLocation();
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
                    userLocation=getUserLocation ();
                    userLocationAddress = getUserLocationDetails (userLocation.latitude, userLocation.longitude);
                    phoneNo = contacts.get (0).getPhoneNumber ();;
                    message = "HELP ME !!!! I am at Location: " + userLocationAddress + " Coordinates: " + userLocation + " **Emergency Distress Message sent from CityWalker App**";

                    sendSMSMessage ();
                } catch (IOException e) {
                    e.printStackTrace ();
                }


            }
        });


        emergencycall.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {

                if(mCallPermissionGranted){
                        phoneNo = contacts.get (0).getPhoneNumber ();
                        callNumber (phoneNo);
                 }
                 else{

                    phoneNo = contacts.get (0).getPhoneNumber ();
                     getCallPermission (phoneNo);
                }
           }
        });

        shareLocation.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                //userLocationValue.setText (userLocation.toString ());

                try {
                    userLocation=getUserLocation ();
                    userLocationAddress = getUserLocationDetails (userLocation.latitude, userLocation.longitude);
                    phoneNo = userPhoneNumber;
                    message = "I am at Location: " + userLocationAddress + " Coordinates: " + userLocation + " **Message sent from CityWalker App**";

                    sendSMSMessage ();
                } catch (IOException e) {
                    e.printStackTrace ();
                }


            }
        });
        navigateToPolice.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                 userLocation=getUserLocation ();
                 if(userLocation!=null){

                     if(policeStationArray.length ()!=0) {
                         nearestStation = findNearestPoliceStation (policeStationArray);
                         System.out.println ("nearest police station " + nearestStation.getPolice_station ());
                         Intent intent = new Intent(ContactEmergencyActivity.this, PoliceMapActivity.class);
                         Bundle bundle = new Bundle ();
                         bundle.putParcelable ("originPoint", new com.mapbox.mapboxsdk.geometry.LatLng (userLocation.latitude,userLocation.longitude));
                         bundle.putParcelable ("destinationPoint", new com.mapbox.mapboxsdk.geometry.LatLng (nearestStation.getLatitude (),nearestStation.getLongitude ()));
                         bundle.putSerializable ("policestation",nearestStation);
                         intent.putExtras (bundle);
                         startActivity(intent);
                     }
                 }
            }
        });

        navigateToShop.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                userLocation=getUserLocation ();
                if(userLocation!=null){

                    if(openShopArray.length ()!=0) {
                        nearestShop = findNearestOpenShop (openShopArray);
                        System.out.println ("nearest open shop " + nearestShop.getName ());
                        Intent intent = new Intent(ContactEmergencyActivity.this, ShopMapActivity.class);
                        Bundle bundle = new Bundle ();
                        bundle.putParcelable ("originPoint", new com.mapbox.mapboxsdk.geometry.LatLng (userLocation.latitude,userLocation.longitude));
                        bundle.putParcelable ("destinationPoint", new com.mapbox.mapboxsdk.geometry.LatLng (nearestShop.getLatitude (),nearestShop.getLongitude ()));
                        bundle.putSerializable ("openshop",nearestShop);
                        intent.putExtras (bundle);
                        startActivity(intent);
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ContactEmergencyActivity.this, MainActivity.class);
        startActivity(intent);
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

        } else {

            Intent intent = new Intent (Intent.ACTION_CALL);
            intent.setData (Uri.parse ("tel:" + phNo));
            startActivity (intent);
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
        }
        else {
            //You already have permission
            try {
                startActivity (intent);
            } catch (SecurityException e) {
                e.printStackTrace ();
            }

        }
    }

    private PoliceStation findNearestPoliceStation(JSONArray policeStationArray ) {

        userLocation = getUserLocation ();
        List<Float> pathDistances = new ArrayList<> ();
        PoliceStation nearStation = new PoliceStation ();
        float minDistance=0;
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


        if(pathDistances.size ()<=0){
            System.out.println ("Zero Path Distance: " +pathDistances);
        }
        else {
            minDistance = getMinValue (pathDistances);
        }


        try{
            for(int j=0; j<result.length ();j++){

                JSONObject jsonObject = result.getJSONObject (j);
                float dis = Float.parseFloat (jsonObject.getString ("distance"));
                PoliceStation policeStation = (PoliceStation) jsonObject.get ("policeStation");
                if(Float.compare(minDistance, dis) == 0){
                    nearStation = policeStation;
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace ();
        }

        return  nearStation;

    }


    private OpenShop findNearestOpenShop(JSONArray openShopArray ) {

        userLocation = getUserLocation ();
        List<Float> pathDistances = new ArrayList<> ();
        OpenShop nearShop = new OpenShop ();
        float minDistance=0;
        for (int i = 0; i < openShopArray.length (); i++) {
            try {
                float distance = 0;
                JSONObject jsonobject = openShopArray.getJSONObject (i);

                String lat = jsonobject.getString ("latitude");
                String lon = jsonobject.getString ("longitude");
                String name = jsonobject.getString ("name");
                String address = jsonobject.getString ("address");
                oInfo = new OpenShop ();
                oInfo.setLatitude (Double.parseDouble (lat));
                oInfo.setLongitude (Double.parseDouble (lon));
                oInfo.setName (name);
                oInfo.setAddress (address);

                Location crntLocation = new Location (LocationManager.GPS_PROVIDER);
                crntLocation.setLatitude (userLocation.latitude);
                crntLocation.setLongitude (userLocation.longitude);


                Location newLocation = new Location (LocationManager.GPS_PROVIDER);
                newLocation.setLatitude (oInfo.getLatitude ());
                newLocation.setLongitude (oInfo.getLongitude ());


                distance = crntLocation.distanceTo(newLocation);

                pathDistances.add (distance);
                try {

                    JSONObject jsonObject1 = new JSONObject ();
                    jsonObject1.put ("distance", distance);
                    jsonObject1.put ("openShop", oInfo);

                    result.put (jsonObject1);

                } catch (JSONException e) {
                    e.printStackTrace ();
                }
            } catch (JSONException e) {
                e.printStackTrace ();
            }
        }


        if(pathDistances.size ()<=0){
            System.out.println ("Zero Path Distance: " +pathDistances);
        }
        else {
            minDistance = getMinValue (pathDistances);
        }


        try{
            for(int j=0; j<result.length ();j++){

                JSONObject jsonObject = result.getJSONObject (j);
                float dis = Float.parseFloat (jsonObject.getString ("distance"));
                OpenShop openShop = (OpenShop) jsonObject.get ("openShop");
                if(Float.compare(minDistance, dis) == 0){
                    nearShop = openShop;
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace ();
        }

        return  nearShop;

    }

    public JSONArray getPoliceStationArray(DatabaseReference stationReference) {

        stationReference.addValueEventListener (new ValueEventListener () {

            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren ()) {

                    //System.out.println("This is ds " +ds);
                    pInfo = new PoliceStation ();
                    pInfo.setLatitude (Double.parseDouble (ds.child ("latitude").getValue ().toString ()));
                    pInfo.setLongitude (Double.parseDouble (ds.child ("longitude").getValue ().toString ()));
                    pInfo.setPolice_station (ds.child ("police_station").getValue ().toString ());
                    pInfo.setAddress (ds.child ("address").getValue ().toString ());
                    pInfo.setTel (ds.child ("tel").getValue ().toString ());


                    try {

                        JSONObject jsonObject = new JSONObject ();
                        jsonObject.put ("police_station", pInfo.getPolice_station ());
                        jsonObject.put ("latitude", pInfo.getLatitude ());
                        jsonObject.put ("longitude", pInfo.getLongitude ());
                        jsonObject.put ("address", pInfo.getAddress ());
                        jsonObject.put ("tel", pInfo.getTel ());

                        policeStationArray.put (jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace ();
                    }

                }

                navigateToPolice.setEnabled (true);
            }

            @Override

            public void onCancelled(DatabaseError databaseError) {

            }

        });
        return policeStationArray;
    }



    public JSONArray getOpenShopArray(DatabaseReference openShopReference) {
        openShopReference.addValueEventListener (new ValueEventListener () {

            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot ds : dataSnapshot.getChildren ()) {


                    //System.out.println("This is cctv ds " +ds);
                    oInfo.setLatitude (Double.parseDouble (ds.child ("latitude").getValue ().toString ()));
                    oInfo.setLongitude (Double.parseDouble (ds.child ("longitude").getValue ().toString ()));
                    oInfo.setName (ds.child ("name").getValue ().toString ());
                    oInfo.setAddress (ds.child ("address").getValue ().toString ());
                    //display all the information


                    try {

                        JSONObject jsonObject = new JSONObject ();
                        jsonObject.put ("name", oInfo.getName ());
                        jsonObject.put ("address", oInfo.getAddress ());
                        jsonObject.put ("latitude", oInfo.getLatitude ());
                        jsonObject.put ("longitude", oInfo.getLongitude ());

                        openShopArray.put (jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace ();
                    }

                }

                navigateToShop.setEnabled (true);


                //Log.d (TAG, "$$$$ Array: " + cctvLocationArray);
            }


            @Override

            public void onCancelled(DatabaseError databaseError) {

            }

        });
        return openShopArray;
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
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mCallPermissionGranted = true;

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
