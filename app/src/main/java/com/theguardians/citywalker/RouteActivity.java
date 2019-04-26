package com.theguardians.citywalker;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.theguardians.citywalker.Model.CCTVLocation;
import com.theguardians.citywalker.Model.PoliceStation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;

public class RouteActivity extends AppCompatActivity implements RoutingListener, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks {
    protected GoogleMap map;
    protected LatLng start;
    protected LatLng end;

    @BindView(R.id.send)
    ImageView send;
    private static final String LOG_TAG = "RouteActivity";
    protected GoogleApiClient mGoogleApiClient;
    private ProgressDialog progressDialog;
    private List<Polyline> polylines;

    private PoliceStation pInfo = new PoliceStation ();
    private CCTVLocation cInfo = new CCTVLocation ();
    private boolean firstTimeRun = false;

    private JSONArray policeStationArray = new JSONArray ();
    private JSONArray cctvLocationArray = new JSONArray ();

    //add Firebase Database stuff

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference policeStationRef;
    private DatabaseReference cctvRef;


    private HashMap<String,PoliceStation> selectedPoliceStation = new HashMap<> ();

    private static final int[] COLORS = new int[]{R.color.colorOrange, R.color.colorHomeBlockSix, R.color.colorPrimaryDark, R.color.colorHomeBlockFour, R.color.primary_dark_material_light};

    private Marker startingMarker;
    private Marker originMarker;
    private Marker destinationMarker;
    private Marker policeStationMarker;
    private List<Marker> selectedStationMarkers;

    /**
     * This activity loads a map and then displays the route and pushpins on it.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.route_activity);
        ButterKnife.bind (this);
       // getActionBar().setDisplayHomeAsUpEnabled(true);

        polylines = new ArrayList<> ();
        selectedStationMarkers =new ArrayList<> ();

        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        policeStationRef = mFirebaseDatabase.getReference("police_location");
        cctvRef =mFirebaseDatabase.getReference ("cctv_location");


        policeStationRef.addValueEventListener(new ValueEventListener () {

            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren ()) {

                    //System.out.println("This is ds " +ds);
                    pInfo = new PoliceStation ();
                    pInfo.setLatitude (Double.parseDouble (ds.child("latitude").getValue ().toString ()));
                    pInfo.setLongitude (Double.parseDouble (ds.child("longitude").getValue ().toString ()));
                    pInfo.setPolice_station (ds.child ("police_station").getValue ().toString ());
                    pInfo.setAddress (ds.child ("address").getValue ().toString ());
                    pInfo.setTel (ds.child ("tel").getValue ().toString ());


                    try {

                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("police_station", pInfo.getPolice_station ());
                        jsonObject.put("latitude", pInfo.getLatitude ());
                        jsonObject.put("longitude", pInfo.getLongitude ());
                        jsonObject.put("address", pInfo.getAddress ());
                        jsonObject.put("tel", pInfo.getTel ());

                        policeStationArray.put (jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                //Log.d (TAG, "$$$$ Array: " + policeStationArray);
            }
            @Override

            public void onCancelled(DatabaseError databaseError) {

            }

            });

             cctvRef.addValueEventListener(new ValueEventListener () {

                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {



                    for (DataSnapshot ds : dataSnapshot.getChildren ()) {


                        //System.out.println("This is cctv ds " +ds);
                        cInfo.setLatitude (Double.parseDouble (ds.child("latitude").getValue ().toString ()));
                        cInfo.setLongitude (Double.parseDouble (ds.child("longitude").getValue ().toString ()));
                        cInfo.setCctvNo (ds.child ("cctv").getValue ().toString ());
                        cInfo.setDetail (ds.child ("detail").getValue ().toString ());
                        //display all the information


                        try {

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("cctv", cInfo.getCctvNo ());
                            jsonObject.put("detail", cInfo.getDetail ());
                            jsonObject.put("latitude", cInfo.getLatitude ());
                            jsonObject.put("longitude", cInfo.getLongitude ());

                            cctvLocationArray.put (jsonObject);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    //Log.d (TAG, "$$$$ Array: " + cctvLocationArray);
                }



            @Override

            public void onCancelled(DatabaseError databaseError) {

            }

        });


        com.google.android.libraries.places.api.Places.initialize(getApplicationContext(), getString(R.string.google_maps_API_key));

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(getApplicationContext ());

        AutocompleteSupportFragment autocompleteFragment1 = (AutocompleteSupportFragment)
                getSupportFragmentManager ().findFragmentById(R.id.autocomplete_fragment1);

        AutocompleteSupportFragment autocompleteFragment2 = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment2);
        autocompleteFragment1.setHint("To");
        autocompleteFragment2.setHint("From");

        //Setting country to Australia
        autocompleteFragment1.setCountry ("AU");
        autocompleteFragment2.setCountry ("AU");

        autocompleteFragment1.setLocationRestriction(RectangularBounds.newInstance(
                new LatLng(-37.904116, 144.907608 ),
                new LatLng(-37.785368, 145.067425)));

        autocompleteFragment2.setLocationRestriction(RectangularBounds.newInstance(
                new LatLng(-37.904116, 144.907608 ),
                new LatLng(-37.785368, 145.067425)));

        ImageView searchIcon1 = (ImageView)((LinearLayout)autocompleteFragment1.getView()).getChildAt(0);
       ImageView searchIcon2 = (ImageView)((LinearLayout)autocompleteFragment2.getView()).getChildAt(0);

        // Set the desired icon
        searchIcon1.setImageDrawable(getResources().getDrawable(R.mipmap.from));
        searchIcon2.setImageDrawable(getResources().getDrawable(R.mipmap.to));


        // Specify the types of place data to return.
        autocompleteFragment1.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG));
        autocompleteFragment2.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG));


        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener () {
            @Override
            public void onPlaceSelected(com.google.android.libraries.places.api.model.Place originPlace) {
                // Get info about the selected place.
                //Log.i(LOG_TAG, "Place: " + originPlace.getName() + ", " + originPlace.getId());
                //System.out.println ("*****************Place1: " + originPlace.getName());

                start = originPlace.getLatLng();
            }

            @Override
            public void onError(Status status) {
                //Handle the error.
                Log.i(LOG_TAG, "An error occurred: " + status);
            }
        });

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place destinationPlace) {
                //Get info about the selected place.
                //Log.i(LOG_TAG, "Place: " + destinationPlace.getName() + ", " + destinationPlace.getId());
                //System.out.print ("**********************************Place2: " + destinationPlace.getName());

                end = destinationPlace.getLatLng();
            }

            @Override
            public void onError(Status status) {
                //Handle the error.
                Log.i(LOG_TAG, "An error occurred: " + status);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager ().findFragmentById (R.id.map);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance ();
            getSupportFragmentManager ().beginTransaction ().replace (R.id.map, mapFragment).commit ();
        }
        mapFragment.getMapAsync (this);
    }
        @Override
        public void onMapReady(GoogleMap googleMap) {
                                                         
        map=googleMap;


        startingMarker = map.addMarker (new MarkerOptions ()
        .position (new LatLng (-37.815018, 144.946014 )));

        map.animateCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (-37.815018, 144.946014),16 ));




    }

    @OnClick(R.id.send)
    public void sendRequest()
    {
        if(Util.Operations.isOnline(this))
        {
            route();
        }
        else
        {
            Toast.makeText(this,"No internet connectivity",Toast.LENGTH_SHORT).show();
        }
    }

    public void route()
    {
        if(start==null || end==null)
        {
            if(start==null)
            {

                    Toast.makeText(this,"Please choose a starting point.",Toast.LENGTH_SHORT).show();

            }
            if(end==null)
            {

                    Toast.makeText(this,"Please choose a destination.",Toast.LENGTH_SHORT).show();

            }
        }
        else
        {
            progressDialog = ProgressDialog.show(this, "Please wait.",
                    "Fetching route information.", true);

            startingMarker.remove ();
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.WALKING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(start, end)
                    .key (getString (R.string.google_maps_API_key))
                    .build();
            routing.execute();
        }
    }


    @Override
    public void onRoutingFailure(RouteException e) {
        // The Routing request failed
        progressDialog.dismiss();
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
        // The Routing Request starts
    }



    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex)
    {
        progressDialog.dismiss();
        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        map.moveCamera(center);

        map.animateCamera (zoom);

        if(firstTimeRun==true) {
            originMarker.remove ();
            destinationMarker.remove ();
        }
        else {
            firstTimeRun=true;
        }
        if (polylines.size () > 0) {
            for (Polyline poly : polylines) {
                    poly.remove ();
            }
        }

        if(selectedStationMarkers.size ()>0) {
            for (Marker m : selectedStationMarkers) {
                if(m!=null)
                {
                    m.remove();
                }
            }
            selectedStationMarkers.clear ();
        }
        polylines = new ArrayList<> ();
            //add route(s) to the map.

            //
        for (int i = 0; i < route.size (); i++) {

                //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

                PolylineOptions polyOptions = new PolylineOptions ();
                polyOptions.color (getResources ().getColor (COLORS[colorIndex]));

                polyOptions.width (12 + i * 3);
                polyOptions.addAll (route.get (i).getPoints ());
                Polyline polyline = map.addPolyline (polyOptions);
                polylines.add (polyline);




                Toast.makeText (getApplicationContext (), "Route " + (i + 1) + ": distance - " + route.get (i).getDistanceValue () + ": duration - " + route.get (i).getDurationValue (), Toast.LENGTH_SHORT).show ();
            }

        selectedPoliceStation = new HashMap<> ();
        for(Polyline polyline: polylines) {

            try {

                for (int j = 0; j < policeStationArray.length (); j++) {
                    JSONObject jsonobject = policeStationArray.getJSONObject (j);

                    String lat = jsonobject.getString ("latitude");
                    String lon = jsonobject.getString ("longitude");
                    String polSat = jsonobject.getString ("police_station");
                    String address = jsonobject.getString ("address");
                    String tel = jsonobject.getString ("tel");


                    pInfo=new PoliceStation ();
                    //System.out.println("This is ds " +ds);

                    pInfo.setLatitude (Double.parseDouble (lat));
                    pInfo.setLongitude (Double.parseDouble (lon));
                    pInfo.setPolice_station (polSat);
                    pInfo.setAddress (address);
                    pInfo.setTel (tel);


                    LatLng pt = new LatLng (pInfo.getLatitude (), pInfo.getLongitude ());

                    if (PolyUtil.isLocationOnPath (pt, polyline.getPoints (), true, 100)) {
                        System.out.println ("Yes is Location on path station name" + pInfo.getPolice_station ());
                        System.out.println ("pinfo" + pInfo.getAddress ());
                        selectedPoliceStation.put (pInfo.getPolice_station (), pInfo);

                    } else {
                        System.out.println ("No is  not Location on path station name " + polSat);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace ();
            }
        }

            // Start marker
        MarkerOptions options = new MarkerOptions ();
        options.position (start);
        options.icon (BitmapDescriptorFactory.fromResource (R.mipmap.to));
        originMarker = map.addMarker (options);

        // End marker
        options = new MarkerOptions ();
        options.position (end);
        options.icon (BitmapDescriptorFactory.fromResource (R.mipmap.to));
        destinationMarker = map.addMarker (options);


         System.out.println ("@@ Selected Stations  @@  " +selectedPoliceStation);

        selectedStationMarkers = new ArrayList ();

        for (PoliceStation policeStation : selectedPoliceStation.values ()) {



            policeStationMarker = null;
            System.out.println (policeStation.getAddress ());
            System.out.println (policeStation.getPolice_station ());
            MarkerOptions options1 = new MarkerOptions ();
            LatLng policeStationLatLng = new LatLng (policeStation.getLatitude (),policeStation.getLongitude ());
            options1.position (policeStationLatLng);
            options1.icon (BitmapDescriptorFactory.fromResource (R.mipmap.police_small));
            options1.title (policeStation.getPolice_station ());
            options1.snippet ("Address: "+policeStation.getAddress () + " Telephone: " +policeStation.getTel ());

            policeStationMarker=map.addMarker (options1);

            selectedStationMarkers.add (policeStationMarker);
        }

        System.out.println ("@@ Selected Stations Marker Size @@  " +selectedStationMarkers.size ());


    }

    @Override
    public void onRoutingCancelled() {
        Log.i(LOG_TAG, "Routing was cancelled.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.v(LOG_TAG,connectionResult.toString());
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }





}

