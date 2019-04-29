package com.theguardians.citywalker;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;
import com.theguardians.citywalker.Model.CCTVLocation;
import com.theguardians.citywalker.Model.PoliceStation;
import com.theguardians.citywalker.Service.DataFromFirebase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;

public class RouteActivity extends AppCompatActivity implements RoutingListener,GoogleMap.OnPolylineClickListener, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks {
    protected GoogleMap map;
    protected LatLng start;
    protected LatLng end;

    @BindView(R.id.send)
    ImageView send;
    @BindView (R.id.nestedScrollView)
    View nestedScrollView;
    @BindView (R.id.timeValue)
    TextView routeDuration;
    @BindView (R.id.distanceValue)
    TextView routeDistance;
    @BindView (R.id.stationCount)
    TextView stationCount;
    @BindView (R.id.cctvCount)
    TextView cctvCount;

    private static final String LOG_TAG = "RouteActivity";
    private ProgressDialog progressDialog;
    private List<Polyline> polylines;

    private PoliceStation pInfo = new PoliceStation ();
    private CCTVLocation cInfo = new CCTVLocation ();
    private boolean firstTimeRun = false;

    private JSONArray policeStationArray = new JSONArray ();
    private JSONArray cctvLocationArray = new JSONArray ();

    private JSONArray polylineRouteDetailsArray = new JSONArray ();
    private JSONArray polylineCountDetailsArray =new JSONArray ();

    //add Firebase Database stuff

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference policeStationRef;
    private DatabaseReference cctvRef;


    private HashMap<String,PoliceStation> selectedPoliceStation = new HashMap<> ();
    private HashMap<String,CCTVLocation> selectedCCTVLocation =new HashMap<> ();

    private static final int[] COLORS = new int[]{R.color.colorOrange, R.color.colorHomeBlockSix, R.color.colorPrimaryDark, R.color.colorHomeBlockFour, R.color.primary_dark_material_light};

    private Marker startingMarker;
    private Marker originMarker;
    private Marker destinationMarker;
    private Marker policeStationMarker;
    private Marker cctvMarker;
    private List<Marker> selectedStationMarkers;
    private List<Marker> selectedCCTVMarkers;
    private DataFromFirebase dataFromFirebase = new DataFromFirebase ();

    private BottomSheetBehavior mBottomSheetBehaviour;

    private AutocompleteSupportFragment autocompleteFragment1;
    private AutocompleteSupportFragment autocompleteFragment2;
    /**
     * This activity loads a map and then displays the route and pushpins on it.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.route_activity);
        ButterKnife.bind (this);

        nestedScrollView.setVisibility (View.INVISIBLE);

        //View nestedScrollView = (View) findViewById(R.id.nestedScrollView);

        //mBottomSheetBehaviour.setPeekHeight(200);



        polylines = new ArrayList<> ();
        selectedStationMarkers =new ArrayList<> ();
        selectedCCTVMarkers =new ArrayList<> ();

        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        policeStationRef = mFirebaseDatabase.getReference("police_location");
        cctvRef =mFirebaseDatabase.getReference ("cctv_location");


        policeStationArray = dataFromFirebase.getPoliceStationArray (policeStationRef);
        cctvLocationArray = dataFromFirebase.getCctvLocationArray (cctvRef);

        com.google.android.libraries.places.api.Places.initialize(getApplicationContext(), getString(R.string.google_maps_API_key));

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(getApplicationContext ());

        autocompleteFragment1 = (AutocompleteSupportFragment)
                getSupportFragmentManager ().findFragmentById(R.id.autocomplete_fragment1);

        autocompleteFragment2 = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment2);
        autocompleteFragment1.setHint("To");
        autocompleteFragment2.setHint("From");
        //Set image icon
        ImageView searchIcon1 = (ImageView)((LinearLayout)autocompleteFragment1.getView()).getChildAt(0);
        ImageView searchIcon2 = (ImageView)((LinearLayout)autocompleteFragment2.getView()).getChildAt(0);
        searchIcon1.setImageDrawable(getResources().getDrawable(R.mipmap.to));
        searchIcon2.setImageDrawable(getResources().getDrawable(R.mipmap.from));

        //Setting country to Australia
        autocompleteFragment1.setCountry ("AU");
        autocompleteFragment2.setCountry ("AU");

        autocompleteFragment1.setLocationRestriction(RectangularBounds.newInstance(
                new LatLng(-37.904116, 144.907608 ),
                new LatLng(-37.785368, 145.067425)));

        autocompleteFragment2.setLocationRestriction(RectangularBounds.newInstance(
                new LatLng(-37.904116, 144.907608 ),
                new LatLng(-37.785368, 145.067425)));

        // Specify the types of place data to return.
        autocompleteFragment1.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG));
        autocompleteFragment2.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, com.google.android.libraries.places.api.model.Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener () {
            @Override
            public void onPlaceSelected(com.google.android.libraries.places.api.model.Place originPlace) {
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
                                                         
        /*

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.custom_style_map));

            if (!success) {
                Log.e(LOG_TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(LOG_TAG, "Can't find style. Error: ", e);
        }
        */
        map=googleMap;
        map.setOnPolylineClickListener(this);

        map.animateCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (-37.814593, 144.966520),14 ));

    }

    @OnClick(R.id.send)
    public void sendRequest()
    {

        if(Util.Operations.isOnline(this))
        {
            if(start==null || end==null)
            {
                if(start==null)

                {
                    Snackbar.make (autocompleteFragment1.getView (), "Origin place can't be empty", Snackbar.LENGTH_LONG)
                            .setAction ("Action", null).show ();

                    //Toast.makeText(this,"Please choose a starting point.",Toast.LENGTH_SHORT).show();

                }
                if(end==null)
                {


                    Snackbar.make (autocompleteFragment2.getView (), "Destination place can't be empty", Snackbar.LENGTH_LONG)
                            .setAction ("Action", null).show ();
                    //Toast.makeText(this,"Please choose a destination.",Toast.LENGTH_SHORT).show();

                }
            }
            else {
                route ();

            }
        }
        else
        {
            Toast.makeText(this,"No internet connectivity",Toast.LENGTH_SHORT).show();
        }
    }

    public void route()
    {


            progressDialog = ProgressDialog.show(this, "Please wait.",
                    "Fetching route information.", true);

            //startingMarker.remove ();
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.WALKING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(start, end)
                    .key (getString (R.string.google_maps_API_key))
                    .build();
            routing.execute();

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
        if(selectedCCTVMarkers.size ()>0) {
            for (Marker m : selectedCCTVMarkers) {
                if(m!=null)
                {
                    m.remove();
                }
            }
            selectedCCTVMarkers.clear ();
        }
        polylines = new ArrayList<> ();
            //add route(s) to the map.

        List<Integer> pathDistances = new ArrayList<> ();
        for(Route path: route)
        {
            int x = path.getDistanceValue ();
            pathDistances.add (x);

        }
        System.out.println("Distances: " +pathDistances);
        java.util.List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dot());

        LatLng origin = new LatLng (route.get (0).getPoints ().get (0).latitude,route.get (0).getPoints ().get (0).longitude);
        LatLng destination = new LatLng (route.get (0).getPoints ().get ((route.get (0).getPoints ().size ())-1).latitude,route.get (0).getPoints ().get ((route.get (0).getPoints ().size ())-1).longitude);
        // Start marker
        MarkerOptions options = new MarkerOptions ();
        options.position (origin);
        options.icon (BitmapDescriptorFactory.fromResource (R.mipmap.walking));

        originMarker = map.addMarker (options);

        // End marker
        options = new MarkerOptions ();
        options.position (destination);
        options.icon (BitmapDescriptorFactory.fromResource (R.mipmap.to));
        destinationMarker = map.addMarker (options);


        polylineRouteDetailsArray =new JSONArray ();

        polylineCountDetailsArray =new JSONArray ();


        nestedScrollView.setVisibility (View.VISIBLE);
        mBottomSheetBehaviour = BottomSheetBehavior.from (nestedScrollView);
        mBottomSheetBehaviour.setState (BottomSheetBehavior.STATE_COLLAPSED);

        int minDistance = getMinValue (pathDistances);
        System.out.println("Minimum Distance" +minDistance);

        for (int i = 0; i < route.size (); i++) {

                //In case of more than 5 alternative routes
                //int colorIndex = i % COLORS.length;

                if(route.get (i).getDistanceValue () == minDistance) {

                    PolylineOptions polyOptions = new PolylineOptions ();
                    //polyOptions.color (getResources ().getColor (COLORS[colorIndex]));
                    polyOptions.pattern (pattern);
                    polyOptions.color (R.color.colorBlue);
                    polyOptions.width (20);
                    polyOptions.zIndex (1000);
                    polyOptions.addAll (route.get (i).getPoints ());
                    polyOptions.clickable (true);
                    Polyline polyline = map.addPolyline (polyOptions);
                    polylines.add (polyline);
                }
                else {

                    PolylineOptions polyOptions = new PolylineOptions ();
                    //polyOptions.color (getResources ().getColor (COLORS[colorIndex]));
                    polyOptions.pattern (pattern);
                    polyOptions.color (R.color.colorRed);
                    polyOptions.width (20);
                    polyOptions.zIndex (1);
                    polyOptions.addAll (route.get (i).getPoints ());
                    polyOptions.clickable (true);
                    Polyline polyline = map.addPolyline (polyOptions);
                    polylines.add (polyline);
                }

                 try {

                JSONObject jsonObject = new JSONObject ();
                jsonObject.put ("polylineId", polylines.get (i).getId ());
                jsonObject.put ("routeDistance", route.get (i).getDistanceText ());
                jsonObject.put ("routeDuration", route.get (i).getDurationText ());

                     polylineRouteDetailsArray.put (jsonObject);

                } catch (JSONException e) {
                e.printStackTrace ();
                }



                Toast.makeText (getApplicationContext (), "Route " + (i + 1) + ": distance - " + route.get (i).getDistanceText () + ": duration - " + route.get (i).getDurationText (), Toast.LENGTH_SHORT).show ();
            }

        routeDistance.setText (route.get (0).getDistanceText ());
        routeDuration.setText (route.get (0).getDurationText ());


        System.out.println ("polylineRouteDetailsArray  "+polylineRouteDetailsArray);

        selectedPoliceStation = new HashMap<> ();
        selectedCCTVLocation = new HashMap<> ();

        for(Polyline polyline: polylines) {

            int cctvCount =0;
            int stationCount =0;
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
                       // System.out.println ("Yes is Location on path station name" + pInfo.getPolice_station ());
                        //System.out.println ("pinfo" + pInfo.getAddress ());
                        stationCount=stationCount+1;
                        selectedPoliceStation.put (pInfo.getPolice_station (), pInfo);

                    } else {
                       // System.out.println ("No is  not Location on path station name " + polSat);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace ();
            }

            try {

                for (int j = 0; j < cctvLocationArray.length (); j++) {
                    JSONObject jsonobject = cctvLocationArray.getJSONObject (j);

                    String lat = jsonobject.getString("latitude");
                    String lon = jsonobject.getString("longitude");
                    String cctvLocNo = jsonobject.getString("cctv");
                    String detail = jsonobject.getString("detail");


                    cInfo=new CCTVLocation ();
                    //System.out.println("This is ds " +ds);

                    cInfo.setLatitude (Double.parseDouble (lat));
                    cInfo.setLongitude (Double.parseDouble (lon));
                    cInfo.setCctvNo (cctvLocNo);
                    cInfo.setDetail (detail);


                    LatLng pt = new LatLng (cInfo.getLatitude (), cInfo.getLongitude ());

                    if (PolyUtil.isLocationOnPath (pt, polyline.getPoints (), true, 50)) {
                       // System.out.println ("Yes is Location on path CCTV name" + cInfo.getDetail ());
                        cctvCount=cctvCount+1;
                        selectedCCTVLocation.put (cInfo.getCctvNo (), cInfo);

                    } else {
                        //System.out.println ("No is  not Location on path CCTV name " + cInfo.getDetail ());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace ();
            }

            try {

                JSONObject jsonObject = new JSONObject ();
                jsonObject.put ("polylineId", polyline.getId ());
                jsonObject.put ("selectedStationCount",stationCount );
                jsonObject.put ("selectedCCTVCount",cctvCount);

                polylineCountDetailsArray.put (jsonObject);

            } catch (JSONException e) {
                e.printStackTrace ();
            }


        }




        System.out.println ("polylineCountDetailsArray  "+polylineCountDetailsArray);


        //System.out.println ("@@ Selected Stations  @@  " +selectedPoliceStation);

        selectedStationMarkers = new ArrayList ();
        selectedCCTVMarkers =new ArrayList<> ();

        for (PoliceStation policeStation : selectedPoliceStation.values ()) {

            policeStationMarker = null;
            MarkerOptions options1 = new MarkerOptions ();
            LatLng policeStationLatLng = new LatLng (policeStation.getLatitude (),policeStation.getLongitude ());
            options1.position (policeStationLatLng);
            options1.icon (BitmapDescriptorFactory.fromResource (R.mipmap.police_small));
            options1.title (policeStation.getPolice_station ());
            options1.zIndex (2);
            options1.snippet ("Address: "+policeStation.getAddress () + " Telephone: " +policeStation.getTel ());

            policeStationMarker=map.addMarker (options1);

            selectedStationMarkers.add (policeStationMarker);
        }
        for (CCTVLocation cctvLocation : selectedCCTVLocation.values ()) {

            cctvMarker = null;
            MarkerOptions options1 = new MarkerOptions ();
            LatLng policeStationLatLng = new LatLng (cctvLocation.getLatitude (),cctvLocation.getLongitude ());
            options1.position (policeStationLatLng);
            options1.icon (BitmapDescriptorFactory.fromResource (R.mipmap.cctv_small));
            options1.title ("Safe City Camera");
            options1.snippet ("Detail: "+cctvLocation.getDetail ());

            cctvMarker=map.addMarker (options1);

            selectedCCTVMarkers.add (cctvMarker);
        }

        System.out.println ("@@ Selected Stations Marker Size @@  " +selectedStationMarkers.size ());

        System.out.println ("@@ Selected CCTV Marker Size @@  " +selectedCCTVMarkers.size ());

        try {
            stationCount.setText (polylineCountDetailsArray.getJSONObject (0).getString ("selectedStationCount"));
        } catch (JSONException e) {
            e.printStackTrace ();
        }
        try {
            cctvCount.setText (polylineCountDetailsArray.getJSONObject (0).getString ("selectedCCTVCount"));
        } catch (JSONException e) {
            e.printStackTrace ();
        }

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


    @Override
    public void onPolylineClick(Polyline polyline) {

        System.out.println ("highlight polyline id" +polyline.getId ());
        for(Polyline polyline1:polylines){
            //Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if(polyline.getId().equals(polyline1.getId())){
                polyline1.setColor(ContextCompat.getColor(this, R.color.colorBlue));
                polyline1.setZIndex(1000);
                try {
                    for (int i = 0; i < polylineRouteDetailsArray.length (); i++) {
                        JSONObject jsonObject  = polylineRouteDetailsArray.getJSONObject (i);
                        String polylineId = jsonObject.getString ("polylineId");
                        String routeDis =jsonObject.getString ("routeDistance");
                        String routeDur = jsonObject.getString ("routeDuration");

                        if(polylineId.equals (polyline.getId ())) {
                            routeDistance.setText (routeDis);
                            routeDuration.setText (routeDur);
                        }
                    }

                }
                catch (JSONException e) {
                    e.printStackTrace ();
                }
                try {
                    for (int i = 0; i < polylineCountDetailsArray.length (); i++) {
                        JSONObject jsonObject  = polylineCountDetailsArray.getJSONObject (i);
                        String polylineId = jsonObject.getString ("polylineId");
                        String satCount =jsonObject.getString ("selectedStationCount");
                        String ccCount = jsonObject.getString ("selectedCCTVCount");

                        if(polylineId.equals (polyline.getId ())) {
                            stationCount.setText (satCount);
                            cctvCount.setText (ccCount);
                        }
                    }

                }
                catch (JSONException e) {
                    e.printStackTrace ();
                }

            }
            else{
                polyline1.setColor(ContextCompat.getColor(this, R.color.colorSecondaryText));
                polyline1.setZIndex(1);
            }
        }


    }

    public static int getMinValue(List<Integer> numbers){
        int minValue = numbers.get (0);
        for(int i=1;i<numbers.size ();i++){
            if(numbers.get (i) < minValue){
                minValue = numbers.get (i);
            }
        }
        return minValue;
    }
}

