package com.theguardians.citywalker.ui;
/**
 * This class is utilised for  functions of emergency support page
 * @Author Sharuq
 * @Version 3.1
 */
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
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
import com.theguardians.citywalker.Model.OpenShop;
import com.theguardians.citywalker.Model.PoliceStation;
import com.theguardians.citywalker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EmergencyActivity extends AppCompatActivity {


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
    private Button addContact;
    private LinearLayout navigateToPolice;
    private LinearLayout navigateToShop;
    private LinearLayout emergencyMessage;
    private LinearLayout shareLocation;
    private LinearLayout emergencyCall;
    private Context context;


    private PoliceStation nearestStation = new PoliceStation ();
    private OpenShop nearestShop = new OpenShop ();

    private static PoliceStation pInfo = new PoliceStation ();
    private static OpenShop oInfo = new OpenShop ();

    private JSONArray policeStationArray = new JSONArray ();
    private JSONArray openShopArray = new JSONArray ();
    private JSONArray result =new JSONArray ();
    private JSONArray resultShop =new JSONArray ();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ...
        super.onCreate (savedInstanceState);
        setContentView (R.layout.emergency_support);
        Toolbar toolbar = findViewById (R.id.toolbar);
        toolbar.setTitle ("Emergency Support");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar (toolbar);

        context =this;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient (this);

        emergencyMessage = findViewById(R.id.emergencymessage);
        addContact =findViewById (R.id.addContact);
        navigateToPolice =findViewById (R.id.navigatetopolice);
        navigateToShop =findViewById (R.id.navigatetoshop);
        shareLocation =findViewById (R.id.sharelocation);
        emergencyCall =findViewById (R.id.emergencycall);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EmergencyActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });




        userLocation = getUserLocation ();

        //when add contact button clicked
        addContact.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmergencyActivity.this, AddContactActivity.class);
                startActivity(intent);
            }
        });
        //when emergency message button clikced
        emergencyMessage.setOnClickListener (new View.OnClickListener () {
                @Override
                public void onClick(View v) {
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
            });
        // when emergency call button clicked
        emergencyCall.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setMessage("Please add an emergency guardian contact first");
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
        });
        //when share location button clicked
        shareLocation.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                //userLocationValue.setText (userLocation.toString ());
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setMessage("Please add an emergency guardian contact first");
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
        });
        // when navigate to police station button clicked
        navigateToPolice.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                userLocation=getUserLocation ();
                if(userLocation!=null){

                    nearestStation = findNearestPoliceStation ();
                    System.out.println ("nearest police station " + nearestStation.getPolice_station ());
                    Intent intent = new Intent(EmergencyActivity.this, PoliceMapActivity.class);
                    Bundle bundle = new Bundle ();
                    bundle.putParcelable ("originPoint", new com.mapbox.mapboxsdk.geometry.LatLng (userLocation.latitude,userLocation.longitude));
                    bundle.putParcelable ("destinationPoint", new com.mapbox.mapboxsdk.geometry.LatLng (nearestStation.getLatitude (),nearestStation.getLongitude ()));
                    bundle.putSerializable ("policestation",nearestStation);
                    intent.putExtras (bundle);
                    startActivity(intent);


                }

            }
        });
        //when navigate to open shop button clicked
        navigateToShop.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                userLocation=getUserLocation ();
                if(userLocation!=null){

                    nearestShop = findNearestOpenShop ();
                    System.out.println ("nearest open shop " + nearestShop.getName ());
                    Intent intent = new Intent(EmergencyActivity.this, ShopMapActivity.class);
                    Bundle bundle = new Bundle ();
                    bundle.putParcelable ("originPoint", new com.mapbox.mapboxsdk.geometry.LatLng (userLocation.latitude,userLocation.longitude));
                    bundle.putParcelable ("destinationPoint", new com.mapbox.mapboxsdk.geometry.LatLng (nearestShop.getLatitude (),nearestShop.getLongitude ()));
                    bundle.putSerializable ("openshop",nearestShop);
                    intent.putExtras (bundle);
                    startActivity(intent);


                }

            }
        });

    }



    @Override
    public void onBackPressed() {
        Intent intent = new Intent(EmergencyActivity.this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Function find the nearest police station
     */
    private PoliceStation findNearestPoliceStation() {

        userLocation = getUserLocation ();
        result = new JSONArray ();
        SharedPreferences sharedPreferences = getSharedPreferences("new_firebase_data", Context.MODE_PRIVATE);
        String satValue = sharedPreferences.getString("police_station_data",null);
        try {
            policeStationArray= new JSONArray(satValue);
        } catch (JSONException e) {
            e.printStackTrace ();
        }
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
    /**
     * Function to find the nearest open shop
     */

    private OpenShop findNearestOpenShop() {

        userLocation = getUserLocation ();
        resultShop = new JSONArray ();
        SharedPreferences sharedPreferences = getSharedPreferences("new_firebase_data", Context.MODE_PRIVATE);
        String shopValue = sharedPreferences.getString("open_shop_data",null);
        try {
            openShopArray = new JSONArray(shopValue);
        } catch (JSONException e) {
            e.printStackTrace ();
        }

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

                    resultShop.put (jsonObject1);

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
            for(int j=0; j<resultShop.length ();j++){

                JSONObject jsonObject = resultShop.getJSONObject (j);
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient (this);

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
