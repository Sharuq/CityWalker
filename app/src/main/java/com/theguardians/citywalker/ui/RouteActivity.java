package com.theguardians.citywalker.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theguardians.citywalker.Model.CCTVLocation;
import com.theguardians.citywalker.Model.OpenShop;
import com.theguardians.citywalker.Model.PedestrianCount;
import com.theguardians.citywalker.Model.PedestrianSensor;
import com.theguardians.citywalker.Model.PoliceStation;
import com.theguardians.citywalker.Network.PedestrianSensorAPI;
import com.theguardians.citywalker.R;
import com.theguardians.citywalker.Service.DataFromFirebase;
import com.theguardians.citywalker.Service.DataPointsCountDetail;
import com.theguardians.citywalker.util.CustomInfoWindowAdapter;
import com.theguardians.citywalker.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RouteActivity extends AppCompatActivity implements RoutingListener,GoogleMap.OnPolylineClickListener, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks {

    protected GoogleMap map;
    protected LatLng start;
    protected LatLng end;

    private FusedLocationProviderClient fusedLocationClient;
    private boolean mLocationPermissionGranted = false;
    private boolean mSMSPermissionGranted =false;
    private LatLng userLocation;
    private static final int  PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;

    private View nestedScrollView;
    private TextView routeDuration;
    private TextView routeDistance;
    private TextView stationCount;
    private TextView cctvCount;
    private TextView sensorCount;
    private TextView openShopCount;

    private static final String LOG_TAG = "RouteActivity";
    private ProgressDialog progressDialog;
    private List<Polyline> polylines;

    private PoliceStation pInfo = new PoliceStation ();
    private CCTVLocation cInfo = new CCTVLocation ();
    private boolean firstTimeRun = false;

    private JSONArray policeStationArray = new JSONArray ();
    private JSONArray cctvLocationArray = new JSONArray ();
    private JSONArray pedestrianSensorArray = new JSONArray ();
    private JSONArray openShopArray = new JSONArray ();

    private JSONArray polylineRouteDetailsArray = new JSONArray ();
    private JSONArray polylineCountDetailsArray =new JSONArray ();
    private JSONArray pedestrianCountFinalArray =new JSONArray ();

    //add Firebase Database stuff

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference policeStationRef;
    private DatabaseReference cctvRef;
    private DatabaseReference pedestrianSensorRef;
    private DatabaseReference openShopRef;


    private HashMap<String,PoliceStation> selectedPoliceStation = new HashMap<> ();
    private HashMap<String,CCTVLocation> selectedCCTVLocation =new HashMap<> ();
    private HashMap<String, PedestrianSensor> selectedPedestrianSensor = new HashMap<> ();
    private HashMap<String, OpenShop> selectedOpenShop = new HashMap<> ();



    private Marker startingMarker;
    private Marker originMarker;
    private Marker destinationMarker;
    private Marker policeStationMarker;
    private Marker cctvMarker;
    private Marker pedestrianSensorMarker;
    private Marker openShopMarker;
    private List<Marker> selectedStationMarkers;
    private List<Marker> selectedCCTVMarkers;
    private List<Marker> selectedPedestrianSensorMarkers;
    private List<Marker> selectedOpenShopMarkers;

    private DataFromFirebase dataFromFirebase = new DataFromFirebase ();

    private BottomSheetBehavior mBottomSheetBehaviour;
    private AutocompleteSupportFragment autocompleteFragment1;
    private AutocompleteSupportFragment autocompleteFragment2;
    private boolean frag1 =false;
    private boolean frag2=false;
    private boolean clickedMap =false;

    private CardView cardView ;
    private Button info2;
    private Button currentLocation;

    private Context context;
    //private List<PedestrianCount> pedestrianCount;
    private List<PedestrianCount> pedestrianCountDetails = new ArrayList<> ();
    private ArrayList<String> markerPlaces = new ArrayList<>();
    /**
     * This activity loads a map and then displays the route and pushpins on it.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.route_activity);

        nestedScrollView = findViewById (R.id.nestedScrollView);
        routeDuration =findViewById (R.id.timeValue);
        info2 =findViewById (R.id.info2);
        info2.setVisibility (View.INVISIBLE);
        currentLocation =findViewById (R.id.currentlocation);
        nestedScrollView.setVisibility (View.INVISIBLE);
        cardView = findViewById (R.id.cardview);
        routeDistance =findViewById(R.id.distanceValue);
        stationCount =findViewById (R.id.stationCount);
        cctvCount =findViewById (R.id.cctvCount);
        sensorCount =findViewById (R.id.sensorCount);
        openShopCount =findViewById (R.id.openShopCount);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (mLocationPermissionGranted) {
            getUserLocation();
        }
        else {
            getLocationPermission();
        }

        polylines = new ArrayList<> ();
        selectedStationMarkers =new ArrayList<> ();
        selectedCCTVMarkers =new ArrayList<> ();
        selectedPedestrianSensorMarkers =new ArrayList<> ();
        selectedOpenShopMarkers =new ArrayList<> ();
        pedestrianCountDetails =new ArrayList<> ();

        context =this;

        /**
        Collecting data from Firebase and storing to JSONArray
         */
        FirebaseApp.initializeApp(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        policeStationRef = mFirebaseDatabase.getReference("police_location");
        cctvRef = mFirebaseDatabase.getReference ("cctv_location");
        pedestrianSensorRef = mFirebaseDatabase.getReference ("ped_sensor_location");
        openShopRef = mFirebaseDatabase.getReference ("24hr_stores");

        policeStationArray = dataFromFirebase.getPoliceStationArray (policeStationRef);
        cctvLocationArray = dataFromFirebase.getCctvLocationArray (cctvRef);
        pedestrianSensorArray =dataFromFirebase.getPedestrianSensorArray (pedestrianSensorRef);
        openShopArray =dataFromFirebase.getOpenShopArray (openShopRef);


        /**
         Getting and setting values on Places auto complete fragment
         */
        com.google.android.libraries.places.api.Places.initialize(getApplicationContext(), getString(R.string.google_maps_API_key));
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(getApplicationContext ());
        autocompleteFragment1 = (AutocompleteSupportFragment)
                getSupportFragmentManager ().findFragmentById(R.id.autocomplete_fragment1);
        autocompleteFragment2 = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment2);
        autocompleteFragment1.setHint("From");
        autocompleteFragment2.setHint("To");
        //Set image icon
        ImageView searchIcon1 = (ImageView)((LinearLayout)autocompleteFragment1.getView()).getChildAt(0);
        ImageView searchIcon2 = (ImageView)((LinearLayout)autocompleteFragment2.getView()).getChildAt(0);
        searchIcon1.setMaxHeight (30);
        searchIcon2.setMaxHeight (30);
        searchIcon1.setMaxWidth (30);
        searchIcon2.setMaxWidth (30);
        searchIcon1.setImageDrawable(getResources().getDrawable(R.drawable.from));
        searchIcon2.setImageDrawable(getResources().getDrawable(R.drawable.to));


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
                frag1 = true;
                if(frag2==true){
                    sendRequest ();
                }
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
                frag2=true;
                if (frag1 == true && frag2==true ){
                    sendRequest ();
                }

            }
            @Override
            public void onError(Status status) {
                //Handle the error.
                Log.i(LOG_TAG, "An error occurred: " + status);
            }
        });

        /**
         On current location button clicked
         */
        currentLocation.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                getUserLocation();
                autocompleteFragment1.setText ("Current Location");
                start =userLocation;
                frag1 = true;
                if(frag2==true){
                    sendRequest ();
                }
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

        map=googleMap;
        map.setOnPolylineClickListener(this);
        map.animateCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (-37.814593, 144.966520),14 ));



    }
    /**
     Function to call route plotting with details
     */
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
                }
                if(end==null)
                {
                    Snackbar.make (autocompleteFragment2.getView (), "Destination place can't be empty", Snackbar.LENGTH_LONG)
                            .setAction ("Action", null).show ();
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
    /**
     Generating route on map
     */
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
       // progressDialog.dismiss();
        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        map.moveCamera(center);
        map.animateCamera (zoom);

        /**
        Removing  Polylines and Markers initially
        */
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

        if(selectedPedestrianSensorMarkers.size ()>0) {
            for (Marker m : selectedPedestrianSensorMarkers) {
                if(m!=null)
                {
                    m.remove();
                }
            }
            selectedPedestrianSensorMarkers.clear ();
        }

        if(selectedOpenShopMarkers.size ()>0) {
            for (Marker m : selectedOpenShopMarkers) {
                if(m!=null)
                {
                    m.remove();
                }
            }
            selectedOpenShopMarkers.clear ();
        }

        polylines = new ArrayList<> ();
        /**
        Finding the minimum distance route
         */
        List<Integer> pathDistances = new ArrayList<> ();
        for(Route path: route)
        {
            int x = path.getDistanceValue ();
            pathDistances.add (x);

        }
        int minDistance = getMinValue (pathDistances);


        /**
         Plotting origin and destination marker
         */
        LatLng origin = new LatLng (route.get (0).getPoints ().get (0).latitude,route.get (0).getPoints ().get (0).longitude);
        LatLng destination = new LatLng (route.get (0).getPoints ().get ((route.get (0).getPoints ().size ())-1).latitude,route.get (0).getPoints ().get ((route.get (0).getPoints ().size ())-1).longitude);

        // Start marker
        MarkerOptions options = new MarkerOptions ();
        options.position (origin);
       // options.icon (BitmapDescriptorFactory.fromResource (R.drawable.startingpoint));
        options.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("startingpoint",150,150)));
        originMarker = map.addMarker (options);

        // End marker
        options = new MarkerOptions ();
        options.position (destination);
        //options.icon (BitmapDescriptorFactory.fromResource (R.drawable.destinationicon2));
        options.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("destinationpoint",150,150)));
        destinationMarker = map.addMarker (options);


        polylineRouteDetailsArray =new JSONArray ();
        polylineCountDetailsArray =new JSONArray ();
        pedestrianCountFinalArray = new JSONArray ();

        nestedScrollView.setVisibility (View.VISIBLE);
        mBottomSheetBehaviour = BottomSheetBehavior.from (nestedScrollView);
        mBottomSheetBehaviour.setState (BottomSheetBehavior.STATE_COLLAPSED);

        java.util.List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dot());

        /**
         Getting polylines from routes and plotting
         */
        for (int i = 0; i < route.size (); i++) {


                if(route.get (i).getDistanceValue () == minDistance) {

                    PolylineOptions polyOptions = new PolylineOptions ();
                    //polyOptions.color (getResources ().getColor (COLORS[colorIndex]));
                    polyOptions.pattern (pattern);
                    //polyOptions.color (colorIndex);
                    //polyOptions.color (R.color.colorBlue);
                    polyOptions.width (28);
                    polyOptions.zIndex (1000);
                    polyOptions.color (Color.parseColor ("#03a8f3"));
                    polyOptions.addAll (route.get (i).getPoints ());
                    polyOptions.clickable (true);
                    Polyline polyline = map.addPolyline (polyOptions);
                    polylines.add (polyline);
                }
                else {

                    PolylineOptions polyOptions = new PolylineOptions ();
                    //polyOptions.color (getResources ().getColor (COLORS[colorIndex]));
                    polyOptions.pattern (pattern);
                    //polyOptions.color (colorIndex);
                    polyOptions.color (Color.parseColor ("#757575"));
                    polyOptions.width (28);
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
        /**
         Setting Bottom sheet values
         */
        routeDistance.setText ("(" +route.get (0).getDistanceText () +")");
        routeDuration.setText (route.get (0).getDurationText ());

        //System.out.println ("polylineRouteDetailsArray  "+polylineRouteDetailsArray);


        selectedPoliceStation = new HashMap<> ();
        selectedCCTVLocation = new HashMap<> ();
        selectedPedestrianSensor = new HashMap<> ();
        selectedOpenShop = new HashMap<> ();

        /**
        Getting values from {@link DataPointsCountDetail}
         */

        polylineCountDetailsArray= DataPointsCountDetail.getpolylineCountDetailsArray(polylines,policeStationArray, cctvLocationArray,pedestrianSensorArray,openShopArray,selectedPoliceStation,selectedCCTVLocation,selectedPedestrianSensor,selectedOpenShop,polylineCountDetailsArray) ;
        selectedPoliceStation=DataPointsCountDetail.getSelectedPoliceStation (polylines,policeStationArray,selectedPoliceStation);
        selectedCCTVLocation=DataPointsCountDetail.getSelectedCCTVLocation (polylines,cctvLocationArray,selectedCCTVLocation);
        selectedPedestrianSensor =DataPointsCountDetail.getSelectedPedestrianSensor (polylines,pedestrianSensorArray,selectedPedestrianSensor);
        selectedOpenShop = DataPointsCountDetail.getSelectedOpenShop(polylines,openShopArray,selectedOpenShop);

        //System.out.println ("polylineCountDetailsArray  "+polylineCountDetailsArray);

        if(selectedPedestrianSensor.size ()>0) {

            try {
               new PopulateDataFromServer ().execute (selectedPedestrianSensor).get ();

            } catch (ExecutionException e) {
                e.printStackTrace ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
        else
        {
            progressDialog.dismiss();
        }



        selectedStationMarkers = new ArrayList ();
        selectedCCTVMarkers =new ArrayList<> ();
        selectedPedestrianSensorMarkers =new ArrayList<> ();
        selectedOpenShopMarkers = new ArrayList<> ();




        System.out.println("Final Sensor Array:" +pedestrianCountFinalArray);


        /**
             Displaying police station markers
        */

        for (PoliceStation policeStation : selectedPoliceStation.values ()) {

            policeStationMarker = null;
            MarkerOptions options1 = new MarkerOptions ();
            LatLng policeStationLatLng = new LatLng (policeStation.getLatitude (),policeStation.getLongitude ());
            options1.position (policeStationLatLng);
            //options1.icon (BitmapDescriptorFactory.fromResource (R.drawable.mappolicestation2));
            options1.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("policenew2",140,150)));
            options1.title (policeStation.getPolice_station ());
            options1.zIndex (2);
            options1.snippet ("Address: "+policeStation.getAddress () + "Telephone: " +policeStation.getTel ());

            policeStationMarker=map.addMarker (options1);

            selectedStationMarkers.add (policeStationMarker);
        }
        /**
         Displaying cctv  markers
         */
        for (CCTVLocation cctvLocation : selectedCCTVLocation.values ()) {

            cctvMarker = null;
            MarkerOptions options1 = new MarkerOptions ();
            LatLng policeStationLatLng = new LatLng (cctvLocation.getLatitude (),cctvLocation.getLongitude ());
            options1.position (policeStationLatLng);
            //options1.icon (BitmapDescriptorFactory.fromResource (R.drawable.mapcctv));
            options1.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("cctvnew3",140,150)));
            options1.title ("Safe City Camera");
            options1.snippet ("Detail: "+cctvLocation.getDetail ());

            cctvMarker=map.addMarker (options1);

            selectedCCTVMarkers.add (cctvMarker);
        }
        /**
         Displaying sensor  markers
         */
        for(int i =0; i< pedestrianCountFinalArray.length ();i++){

            HashMap<String, String> markerdetails = new HashMap<String, String>();

            JSONObject jsonobject = null;
            try {
                jsonobject = pedestrianCountFinalArray.getJSONObject (i);
                String lat = jsonobject.getString ("latitude");
                String lon = jsonobject.getString ("longitude");
                String sensor_id = jsonobject.getString ("sensor_id");
                String sensor_description = jsonobject.getString ("sensor_description");
                String time = jsonobject.getString ("time");
                String total_of_directions = jsonobject.getString ("total_of_directions");
                String predict_time = jsonobject.getString ("prediction_time");
                String predict_total = jsonobject.getString ("prediction_counts");



                pedestrianSensorMarker = null;
                MarkerOptions options2 = new MarkerOptions ();
                LatLng sensorLatLng = new LatLng (Double.parseDouble (lat),Double.parseDouble (lon));
                options2.position (sensorLatLng);
                //options2.icon (BitmapDescriptorFactory.fromResource (R.drawable.peoplesensor));
                options2.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("sensornew3",140,150)));
                options2.title ("Sensor " +sensor_id);
                //options2.snippet ("Address "+sensor_description);


                CustomInfoWindowAdapter customInfoWindow = new CustomInfoWindowAdapter(this);
                map.setInfoWindowAdapter(customInfoWindow);
                //String time_count = time + total_of_directions;
                pedestrianSensorMarker=map.addMarker (options2);
                pedestrianSensorMarker.showInfoWindow();
                markerdetails.put ("address",sensor_description);
                markerdetails.put ("time",time);
                markerdetails.put ("total_of_directions",total_of_directions);
                markerdetails.put ("predict_time",predict_time);
                markerdetails.put ("predict_total",predict_total);
                markerdetails.put ("Id",pedestrianSensorMarker.getId ());

                pedestrianSensorMarker.setTag (markerdetails);


                selectedPedestrianSensorMarkers.add (pedestrianSensorMarker);

            } catch (JSONException e) {
                e.printStackTrace ();
            }


        }


        System.out.println("Final Sensor Array:" +pedestrianCountFinalArray);

            /**
            Displaying open shop markers
            */
        for (OpenShop openShop : selectedOpenShop.values ()) {

            openShopMarker = null;
            MarkerOptions options3 = new MarkerOptions ();
            LatLng sensorLatLng = new LatLng (openShop.getLatitude (),openShop.getLongitude ());
            options3.position (sensorLatLng);
            //options2.icon (BitmapDescriptorFactory.fromResource (R.drawable.peoplesensor));
            options3.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("shopnew2",140,150)));
            options3.title (openShop.getName ());
            options3.snippet ("Address: "+openShop.getAddress ());

            openShopMarker=map.addMarker (options3);
            selectedOpenShopMarkers.add (openShopMarker);
        }

       // System.out.println ("@@ Selected Stations Marker Size @@  " +selectedStationMarkers.size ());
       // System.out.println ("@@ Selected CCTV Marker Size @@  " +selectedCCTVMarkers.size ());

        /**
        Setting value in bottom sheet
         */

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
        try {
            sensorCount.setText (polylineCountDetailsArray.getJSONObject (0).getString ("pedestrianSensorCount"));
        } catch (JSONException e) {
            e.printStackTrace ();
        }

        try {
            openShopCount.setText (polylineCountDetailsArray.getJSONObject (0).getString ("openShopCount"));
        } catch (JSONException e) {
            e.printStackTrace ();
        }

        /**
         Hiding and showing search box
         */
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener ()
        {
            @Override
            public void onMapClick(LatLng arg0)
            {
                if(clickedMap==true){
                    cardView.setVisibility (View.VISIBLE);
                    clickedMap=false;
                }
                else {
                    cardView.setVisibility (View.INVISIBLE);
                    clickedMap=true;
                }
            }
        });

    }

    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
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

    /**
     On Polyline click
     */


    @Override
    public void onPolylineClick(Polyline polyline) {

        System.out.println ("highlight polyline id" + polyline.getId ());
        for (Polyline polyline1 : polylines) {
            //Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if (polyline.getId ().equals (polyline1.getId ())) {
                polyline1.setColor (ContextCompat.getColor (this, R.color.colorBlue));
                polyline1.setZIndex (1000);
                try {
                    for (int i = 0; i < polylineRouteDetailsArray.length (); i++) {
                        JSONObject jsonObject = polylineRouteDetailsArray.getJSONObject (i);
                        String polylineId = jsonObject.getString ("polylineId");
                        String routeDis = jsonObject.getString ("routeDistance");
                        String routeDur = jsonObject.getString ("routeDuration");

                        if (polylineId.equals (polyline.getId ())) {
                            routeDistance.setText (routeDis);
                            routeDuration.setText (routeDur);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace ();
                }
                try {
                    for (int i = 0; i < polylineCountDetailsArray.length (); i++) {
                        JSONObject jsonObject = polylineCountDetailsArray.getJSONObject (i);
                        String polylineId = jsonObject.getString ("polylineId");
                        String satCount = jsonObject.getString ("selectedStationCount");
                        String ccCount = jsonObject.getString ("selectedCCTVCount");
                        String psCount = jsonObject.getString ("pedestrianSensorCount");
                        String osCount = jsonObject.getString ("openShopCount");
                        if (polylineId.equals (polyline.getId ())) {
                            stationCount.setText (satCount);
                            cctvCount.setText (ccCount);
                            sensorCount.setText (psCount);
                            openShopCount.setText (osCount);
                    }
                    }

                } catch (JSONException e) {
                    e.printStackTrace ();
                }

            } else {
                polyline1.setColor (ContextCompat.getColor (this, R.color.colorSecondaryText));
                polyline1.setZIndex (1);
            }
        }


    }




    private class PopulateDataFromServer extends AsyncTask<HashMap<String, PedestrianSensor> ,Void,Void>

    {

        @Override
        protected Void doInBackground(HashMap... voids) {

            HashMap<String, PedestrianSensor> selectedPedestrianSensors = voids[0];
            pedestrianCountDetails =new ArrayList<> ();

            //Creating a retrofit object
            Retrofit retrofit = new Retrofit.Builder ()
                    .baseUrl (PedestrianSensorAPI.BASE_URL)
                    .addConverterFactory (GsonConverterFactory.create ()) //Here we are using the GsonConverterFactory to directly convert json data to object
                    .build ();
            //creating the api interface
            PedestrianSensorAPI api = retrofit.create (PedestrianSensorAPI.class);
            Call<List<PedestrianCount>> call = api.getPedestrianCount ();
            try {
                pedestrianCountDetails = call.execute ().body ();

                for (PedestrianSensor pedestrianSensor : selectedPedestrianSensors.values ()) {

                    for (PedestrianCount pedestrianCount : pedestrianCountDetails) {


                        if (pedestrianCount.getSensor_id ().equals (pedestrianSensor.getSensor_id ())) {


                                try {

                                    JSONObject jsonObject = new JSONObject ();
                                    jsonObject.put ("sensor_id", pedestrianCount.getSensor_id ());
                                    jsonObject.put ("time", pedestrianCount.getTime ());
                                    jsonObject.put ("total_of_directions", pedestrianCount.getTotal_of_directions ());
                                    jsonObject.put ("prediction_counts", pedestrianCount.getPrediction_counts ());
                                    jsonObject.put ("prediction_time", pedestrianCount.getPrediction_time ());
                                    jsonObject.put ("sensor_description", pedestrianSensor.getSensor_description ());
                                    jsonObject.put ("latitude", pedestrianSensor.getLatitude ());
                                    jsonObject.put ("longitude", pedestrianSensor.getLongitude ());

                                    pedestrianCountFinalArray.put (jsonObject);

                                } catch (JSONException e) {
                                    e.printStackTrace ();
                                }


                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace ();
            }

        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
           progressDialog.dismiss();
        //if (dialog.isShowing()) {
         //   dialog.dismiss();
        //}
    }

    @Override
    protected void onPreExecute() {
            //progressDialog = ProgressDialog.show(context, "Please wait.",
                   // "Processing route safety situation information.", true);

        //dialog.setMessage("Please wait, Processing route safety situation information.");
        //dialog.show();

    }
  }

    /**
     * Return minimum value
     */


    public static int getMinValue(List<Integer> numbers){
        int minValue = numbers.get (0);
        for(int i=1;i<numbers.size ();i++){
            if(numbers.get (i) < minValue){
                minValue = numbers.get (i);
            }
        }
        return minValue;
    }
    /**
    Get user Location
     */

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
            return ;
        }

        fusedLocationClient.getLastLocation ()
                .addOnSuccessListener (this, new OnSuccessListener<Location> () {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            userLocation = new LatLng(location.getLatitude (),location.getLongitude ());

                            System.out.println ("user location found" +userLocation);
                        }
                    }
                });


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
}

