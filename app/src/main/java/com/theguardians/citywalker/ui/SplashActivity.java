//Class splash screen activity
package com.theguardians.citywalker.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.theguardians.citywalker.R;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


public class SplashActivity extends AppCompatActivity {

    private ImageView logo;
    private static int splashTimeOut=4000;
    private Context context;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    private static final int  PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private boolean mLocationPermissionGranted = false;
    private boolean mSMSPermissionGranted = false;
    private boolean mCallPermissionGranted = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        logo=(ImageView)findViewById(R.id.logo);

        context=this;



    }

    /**
     * OnResume Override
     */
    @Override
    protected void onResume() {
        super.onResume();
        new CheckInternetConnection().execute();

    }

    public void launchActivity(){

        if(mCallPermissionGranted&&mLocationPermissionGranted&&mSMSPermissionGranted){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(SplashActivity.this, IntroActivity.class);
                    startActivity(i);
                    finish();
                }
            },splashTimeOut);

            Animation myanim = AnimationUtils.loadAnimation(this,R.anim.mysplashanimation);
            logo.startAnimation(myanim);
        }
        else if(!mLocationPermissionGranted){
            getLocationPermission ();
        }
        else if(!mCallPermissionGranted){
            getCallPermission ();
        }
        else{
            getSMSPermission ();
        }
    }



    /**
     * This methid check if the lccation permisission is enabled or not and
     * will ask them to be granted it its not given
     */
    private void checkLocationServices() {

        if (checkMapServices ()) {

            if (!mLocationPermissionGranted) {
                getLocationPermission ();
            }

        }


    }

    /**
     *  Check whether maps service is available
     */

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void checkSMSServices() {

        if (!mSMSPermissionGranted) {
             getSMSPermission ();
        }
    }
    private void checkCallPermission() {

        if (!mCallPermissionGranted) {
            getCallPermission ();
        }
    }

    /**
     * Is all services okay and app ready for use
     * @return
     */
    public boolean isServicesOK(){


        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(SplashActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests

            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it

            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(SplashActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * Check if location is enabled
     * @return
     */
    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }


    /**
     * No GPS alert message
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
        //alert.dismiss ();
    }

    /**
     * Ask for location permission
     */
    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        } else {
            mLocationPermissionGranted = true;
            //launchActivity ();
        }
    }
    private void getSMSPermission() {


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);

        } else {
            // Permission has already been granted
            mSMSPermissionGranted =true;
            //launchActivity ();
        }
    }

    private void getCallPermission() {
        if (ContextCompat.checkSelfPermission (this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions (this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_CALL_PHONE);

            }
        else {
            // Permission has already been granted
            mCallPermissionGranted =true;
        }

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
                checkLocationServices();
                checkSMSServices ();
                checkCallPermission ();
                launchActivity ();
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
                    launchActivity();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }
}