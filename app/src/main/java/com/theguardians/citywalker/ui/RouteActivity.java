package com.theguardians.citywalker.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.fangxu.allangleexpandablebutton.AllAngleExpandableButton;
import com.fangxu.allangleexpandablebutton.ButtonData;
import com.fangxu.allangleexpandablebutton.ButtonEventListener;
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
import com.theguardians.citywalker.Service.DataPointsCountDetail;
import com.theguardians.citywalker.util.CustomInfoWindowAdapter;
import com.theguardians.citywalker.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    private TextView safetyScoreText;

    private static final String LOG_TAG = "RouteActivity";
    private ProgressDialog progressDialog;
    private List<Polyline> polylines;

    private boolean firstTimeRun = false;

    private JSONArray policeStationArray;
    private JSONArray cctvLocationArray;
    private JSONArray pedestrianSensorArray;
    private JSONArray openShopArray;

    private JSONArray polylineRouteDetailsArray;
    private JSONArray polylineCountDetailsArray;
    private JSONArray totalValues;
    private JSONArray finalTotalValues;

    private HashMap<String,PoliceStation> selectedPoliceStation;
    private HashMap<String,CCTVLocation> selectedCCTVLocation;
    private HashMap<String, PedestrianSensor> selectedPedestrianSensor;
    private HashMap<String, OpenShop> selectedOpenShop;

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
    private List<MarkerOptions> selectedStationMarkerOptions;
    private List<MarkerOptions> selectedCCTVMarkerOptions;
    private List<MarkerOptions> selectedOpenShopMarkerOptions;
    private List<MarkerOptions> selectedPedstrianSensorMarkerOptions;

    private BottomSheetBehavior mBottomSheetBehaviour;
    private AutocompleteSupportFragment autocompleteFragment1;
    private AutocompleteSupportFragment autocompleteFragment2;
    private boolean frag1 =false;
    private boolean frag2=false;
    private boolean clickedMap =false;

    private CardView cardView ;
    private LinearLayout layout;
    private LinearLayout mainLayout;
    private Button info2;
    private Button currentLocation;
    private AllAngleExpandableButton button;
    private AllAngleExpandableButton button2;
    private  ImageView safestRoute;
    private Context context;
    private int maxSafetyScore = 0;
    private int cctv= 0;
    private int shop =0;
    private int station =0;
    private int dist =0;
    private int safetyscore =0;
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
        safetyScoreText =findViewById (R.id.safetyScore);
        safestRoute = findViewById (R.id.safestRoute);
        layout =findViewById (R.id.saflay);
        mainLayout =findViewById (R.id.mainlayout);
        button = (AllAngleExpandableButton) findViewById(R.id.button_expandable);
        //button2 = (AllAngleExpandableButton) findViewById(R.id.button_expandable2);
        button.setVisibility (View.INVISIBLE);


       // installButton110to250();
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
        selectedStationMarkerOptions =new ArrayList<> ();
        selectedCCTVMarkerOptions = new ArrayList<> ();
        selectedOpenShopMarkerOptions =new ArrayList<> ();
        selectedPedstrianSensorMarkerOptions =new ArrayList<> ();

        policeStationArray = new JSONArray ();
        cctvLocationArray = new JSONArray ();
        pedestrianSensorArray = new JSONArray ();
        openShopArray = new JSONArray ();
        polylineRouteDetailsArray = new JSONArray ();
        polylineCountDetailsArray =new JSONArray ();
        totalValues = new JSONArray ();
        finalTotalValues = new JSONArray ();

        selectedPoliceStation = new HashMap<> ();
        selectedCCTVLocation =new HashMap<> ();
        selectedPedestrianSensor = new HashMap<> ();
        selectedOpenShop = new HashMap<> ();

        context =this;


        SharedPreferences sharedPreferences = getSharedPreferences("new_firebase_data", Context.MODE_PRIVATE);
        String satValue = sharedPreferences.getString("police_station_data",null);
        try {
            policeStationArray= new JSONArray(satValue);
        } catch (JSONException e) {
            e.printStackTrace ();
        }

        String shopValue = sharedPreferences.getString("open_shop_data",null);
        try {
            openShopArray = new JSONArray(shopValue);
        } catch (JSONException e) {
            e.printStackTrace ();
        }

        String sensorValue = sharedPreferences.getString("pedestrian_sensor_data",null);
        try {
            pedestrianSensorArray = new JSONArray(sensorValue);
        } catch (JSONException e) {
            e.printStackTrace ();
        }

        String cctvValue = sharedPreferences.getString("cctv_data",null);
        try {
            cctvLocationArray = new JSONArray(cctvValue);
        } catch (JSONException e) {
            e.printStackTrace ();
        }

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
/*
    private void installButton110to250() {
        final List<ButtonData> buttonDatas = new ArrayList<>();
        int[] drawable = {R.drawable.emergency, R.drawable.emergencycall, R.drawable.emergencymessage,  R.drawable.ic_navigation};
        int[] color = {R.color.colorOrange, R.color.colorRed, R.color.colorRed, R.color.colorRed};
        for (int i = 0; i < 4; i++) {
            ButtonData buttonData;
            if (i == 0) {
                buttonData = ButtonData.buildIconButton(this, drawable[i], 15);
            } else {
                buttonData = ButtonData.buildIconButton(this, drawable[i], 0);
            }
            buttonData.setBackgroundColorId(this, color[i]);
            buttonDatas.add(buttonData);
        }
        button2.setButtonDatas(buttonDatas);
        setListener(button2);
    }

    private void setListener(AllAngleExpandableButton button) {
        button.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                if(index==1)
                showToast("Emergency Call Clicked");
                if(index==2)
                    showToast("Emergency Message  Clicked");
                if(index==3)
                    showToast("Emergency Navigation Clicked");
            }

            @Override
            public void onExpand() {
//                showToast("onExpand");
            }

            @Override
            public void onCollapse() {
//                showToast("onCollapse");
            }
        });


    }
*/
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
        getUserLocation ();
        if(userLocation!=null) {
            startingMarker = map.addMarker (new MarkerOptions ()
                    .position (new LatLng (userLocation.latitude, userLocation.longitude)));

            map.animateCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (userLocation.latitude, userLocation.longitude), 14));
        }
        else{
            map.animateCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (-37.814593, 144.966520),14 ));

        }


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
        progressDialog.dismiss();

        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        map.moveCamera(center);
        map.animateCamera (zoom);

        /**
         Removing  Polylines and Markers initially
         */
        if(startingMarker!=null)
        {startingMarker.remove ();}
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
        finalTotalValues = new JSONArray ();

        nestedScrollView.setVisibility (View.VISIBLE);
        mBottomSheetBehaviour = BottomSheetBehavior.from (nestedScrollView);
        mBottomSheetBehaviour.setState (BottomSheetBehavior.STATE_COLLAPSED);

        java.util.List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dot());


        mainLayout.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                builder1.setTitle ("Safety Score Analysis");
                builder1.setMessage("The safety scores is calculated based on the distance of the route and the quantities of safety factors (CCTV cameras, 24-hour open stores and police stations) along the routes");
                builder1.setCancelable(true);
                builder1.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

    /*
                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });


    */
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });


        /**
         Getting polylines from routes and plotting
         */
        for (int i = 0; i < route.size (); i++) {


            if(route.get (i).getDistanceValue () == minDistance) {

                PolylineOptions polyOptions = new PolylineOptions ();
                polyOptions.pattern (pattern);
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
                polyOptions.pattern (pattern);
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
                jsonObject.put ("routeDistanceValue",route.get (i).getDistanceValue ());

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

        totalValues = new JSONArray ();
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



        System.out.println ("Polyline counts:" +polylineCountDetailsArray);

        for(int i =0; i<polylineRouteDetailsArray.length ();i++){
                try {
                    JSONObject jsonobject = polylineRouteDetailsArray.getJSONObject (i);
                    String polID = jsonobject.getString ("polylineId");
                    String routeDistance = jsonobject.getString ("routeDistance");
                    String routeDuration = jsonobject.getString ("routeDuration");
                    String routeDistanceValue = jsonobject.getString ("routeDistanceValue");

                    for(int k=0;k<polylineCountDetailsArray.length ();k++){

                        JSONObject jsonObject1 = polylineCountDetailsArray.getJSONObject (k);
                        String polylineId = jsonObject1.getString ("polylineId");
                        String satCount = jsonObject1.getString ("selectedStationCount");
                        String ccCount = jsonObject1.getString ("selectedCCTVCount");
                        String psCount = jsonObject1.getString ("pedestrianSensorCount");
                        String osCount = jsonObject1.getString ("openShopCount");

                        if(polID.equals (polylineId)){

                            try {

                                JSONObject newjsonObject = new JSONObject ();
                                newjsonObject.put ("polylineId", polID);
                                newjsonObject.put ("routeDistance", routeDistance);
                                newjsonObject.put ("routeDuration", routeDuration);
                                newjsonObject.put ("routeDistanceValue",routeDistanceValue);
                                newjsonObject.put ("selectedStationCount", satCount);
                                newjsonObject.put ("selectedCCTVCount", ccCount);
                                newjsonObject.put ("pedestrianSensorCount", psCount);
                                newjsonObject.put ("openShopCount", osCount);

                                totalValues.put (newjsonObject);

                            } catch (JSONException e) {
                                e.printStackTrace ();
                            }
                        }
                    }


                }
                catch (JSONException e) {
                    e.printStackTrace ();
                }
            }



        System.out.println ("total Values  "+totalValues);




        selectedStationMarkers = new ArrayList ();
        selectedCCTVMarkers =new ArrayList<> ();
        selectedPedestrianSensorMarkers =new ArrayList<> ();
        selectedOpenShopMarkers = new ArrayList<> ();
        selectedStationMarkerOptions =new ArrayList<> ();
        selectedCCTVMarkerOptions = new ArrayList<> ();
        selectedOpenShopMarkerOptions =new ArrayList<> ();
        selectedPedstrianSensorMarkerOptions =new ArrayList<> ();



        /**
         Displaying police station markers
         */

        for (PoliceStation policeStation : selectedPoliceStation.values ()) {

            policeStationMarker = null;
            HashMap<String, String> markerdetails = new HashMap<String, String>();
            CustomInfoWindowAdapter customInfoWindow = new CustomInfoWindowAdapter(this);
            map.setInfoWindowAdapter(customInfoWindow);

            String title = policeStation.getPolice_station ();
            String address = "Address: " + policeStation.getAddress();
            String tel = "Phone Number: " + policeStation.getTel ();

            System.out.println (address+tel);
            MarkerOptions poloptions = new MarkerOptions ();
            LatLng policeStationLatLng = new LatLng (policeStation.getLatitude (),policeStation.getLongitude ());
            poloptions.position (policeStationLatLng);
            poloptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("policenew2",140,150)));
            poloptions.title (title);
            poloptions.snippet(address+"\n"+tel);

            policeStationMarker=map.addMarker (poloptions);
            selectedStationMarkerOptions.add (poloptions);
            selectedStationMarkers.add (policeStationMarker);


            policeStationMarker.showInfoWindow ();


            markerdetails.put ("address", policeStation.getAddress ());
            markerdetails.put ("tel", policeStation.getTel ());
            markerdetails.put("this_is","police_station");
            markerdetails.put ("Id", policeStationMarker.getId ());

            policeStationMarker.setTag (markerdetails);


        }
        /**
         Displaying cctv  markers
         */
        for (CCTVLocation cctvLocation : selectedCCTVLocation.values ()) {

            cctvMarker = null;
            MarkerOptions cctvoptions = new MarkerOptions ();
            LatLng policeStationLatLng = new LatLng (cctvLocation.getLatitude (),cctvLocation.getLongitude ());
            cctvoptions.position (policeStationLatLng);
            cctvoptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("cctvnew3",140,150)));
            cctvoptions.title (cctvLocation.getCctvNo ());
            cctvoptions.snippet ("Detail: "+cctvLocation.getDetail ());

            cctvMarker=map.addMarker (cctvoptions);
            selectedCCTVMarkerOptions.add (cctvoptions);
            selectedCCTVMarkers.add (cctvMarker);
        }


        displaySensors();

        if(selectedPedestrianSensor.size ()>0){
            if(selectedPedestrianSensorMarkers.size ()<=0){
                displaySensors ();
            }
        }

        /**
         Displaying open shop markers
         */
        for (OpenShop openShop : selectedOpenShop.values ()) {

            openShopMarker = null;

            HashMap<String, String> markerdetails = new HashMap<String, String>();
            CustomInfoWindowAdapter customInfoWindow = new CustomInfoWindowAdapter(this);
            map.setInfoWindowAdapter(customInfoWindow);
            MarkerOptions openshopoptions = new MarkerOptions ();
            LatLng sensorLatLng = new LatLng (openShop.getLatitude (),openShop.getLongitude ());
            openshopoptions.position (sensorLatLng);
            openshopoptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("shopnew2",140,150)));
            openshopoptions.title (openShop.getName ());
            openshopoptions.snippet (openShop.getAddress ());

            openShopMarker=map.addMarker (openshopoptions);
            selectedOpenShopMarkerOptions.add (openshopoptions);
            selectedOpenShopMarkers.add (openShopMarker);

            markerdetails.put ("address", openShop.getAddress ());
            markerdetails.put("this_is","open_shop");
            markerdetails.put ("Id", openShopMarker.getId ());

            openShopMarker.setTag (markerdetails);
        }

        // System.out.println ("@@ Selected Stations Marker Size @@  " +selectedStationMarkers.size ());
        // System.out.println ("@@ Selected CCTV Marker Size @@  " +selectedCCTVMarkers.size ());

        /**
         Finding safety score
         */


        List<Integer> safetyScores = new ArrayList<> ();
        try {
             for(int k =0;k<totalValues.length ();k++){

                    JSONObject jsonObject1 = totalValues.getJSONObject (k);

                    String polylineId = jsonObject1.getString ("polylineId");
                    String routeDistance = jsonObject1.getString ("routeDistance");
                    String routeDuration = jsonObject1.getString ("routeDuration");
                    String routeDistanceValue = jsonObject1.getString ("routeDistanceValue");
                    String satCount = jsonObject1.getString ("selectedStationCount");
                    String ccCount = jsonObject1.getString ("selectedCCTVCount");
                    String psCount = jsonObject1.getString ("pedestrianSensorCount");
                    String osCount = jsonObject1.getString ("openShopCount");

                    cctv = Integer.parseInt (ccCount);
                    station =Integer.parseInt (satCount);
                    shop= Integer.parseInt (osCount);
                    dist =Integer.parseInt (routeDistanceValue);
                    safetyscore = calculateSafetyScore (cctv,shop,station,dist);
                    safetyScores.add (safetyscore);

                     try {

                     JSONObject newjsonObject = new JSONObject ();
                     newjsonObject.put ("polylineId", polylineId);
                     newjsonObject.put ("routeDistance", routeDistance);
                     newjsonObject.put ("routeDuration", routeDuration);
                     newjsonObject.put ("routeDistanceValue",routeDistanceValue);
                     newjsonObject.put ("selectedStationCount", satCount);
                     newjsonObject.put ("selectedCCTVCount", ccCount);
                     newjsonObject.put ("pedestrianSensorCount", psCount);
                     newjsonObject.put ("openShopCount", osCount);
                     newjsonObject.put ("safetyScore",safetyscore);

                     finalTotalValues.put (newjsonObject);

                     } catch (JSONException e) {
                     e.printStackTrace ();
                 }



                }

        } catch (JSONException e) {
            e.printStackTrace ();
        }


        maxSafetyScore = getMaxValue (safetyScores);

        for(Polyline polyline : polylines){
            for(int i =0;i<finalTotalValues.length ();i++) {
                try {
                    JSONObject jsonObject1 = finalTotalValues.getJSONObject (i);
                    String polylineId = jsonObject1.getString ("polylineId");
                    String maxScore = jsonObject1.getString ("safetyScore");

                    //Polyline p
                    if (maxSafetyScore == Integer.parseInt (maxScore)) {
                        if(polyline.getId ().equals (polylineId))
                         {onPolylineClick (polyline);}
                    }
                } catch (JSONException e) {
                    e.printStackTrace ();
                }
        }
    }






        System.out.println ("finalTotalValues  "+finalTotalValues);



        /**
         Hiding and showing search box
         */
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener () {
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

        /**
       Filtering safety factors
         */

        button.setVisibility (View.VISIBLE);

        final List<ButtonData> buttonDatas = new ArrayList<>();
        int[] drawable = {R.drawable.select_all, R.drawable.shopicon, R.drawable.policeicon, R.drawable.cctvicon,R.drawable.sensoricon};
        int[] color = {R.color.quantum_cyan900, R.color.colorRed, R.color.colorOrange, R.color.colorButton,R.color.sensorcolor};
        //String[] texts ={"Filter","shops","police","cctv","sensor"};
        for (int i = 0; i < 5; i++) {
            ButtonData buttonData;
            if (i == 0) {
                buttonData = ButtonData.buildIconButton(this, drawable[i], 9);
                // buttonData.setText (texts[i]);
            } else {
                buttonData = ButtonData.buildIconButton(this, drawable[i], 8);
                // buttonData.setTexts (texts);
            }
            buttonData.setBackgroundColorId(this, color[i]);

            buttonDatas.add(buttonData);
        }
        button.setButtonDatas(buttonDatas);
        //setListener(button);


        button.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                if(index==1) {
                    showToast ("All safety factors filter option selected");

                    for (Marker m : selectedStationMarkers) {
                        if(m!=null)
                        { m.setVisible (true);}
                    }
                    for (Marker m : selectedCCTVMarkers) {
                        if(m!=null)
                        { m.setVisible (true);}
                    }
                    for (Marker m : selectedPedestrianSensorMarkers) {
                        if(m!=null)
                        { m.setVisible (true);}
                    }
                    for (Marker m : selectedOpenShopMarkers) {
                        if(m!=null)
                        { m.setVisible (true);}
                    }

                }
                if(index==2)
                {
                    showToast("24 hours open shops filter option selected");
                    for (Marker m : selectedStationMarkers) {
                            if(m!=null)
                            { m.setVisible (false);}
                        }
                    for (Marker m : selectedCCTVMarkers) {
                            if(m!=null)
                            { m.setVisible (false);}
                        }
                    for (Marker m : selectedPedestrianSensorMarkers) {
                            if(m!=null)
                            { m.setVisible (false);}
                        }
                    for (Marker m : selectedOpenShopMarkers) {
                        if(m!=null)
                        { m.setVisible (true);}
                    }
                }
                if(index==3) {
                    showToast ("Police stations filter option selected");
                    for (Marker m : selectedStationMarkers) {
                        if(m!=null)
                        { m.setVisible (true);}
                    }
                    for (Marker m : selectedCCTVMarkers) {
                        if(m!=null)
                        { m.setVisible (false);}
                    }
                    for (Marker m : selectedPedestrianSensorMarkers) {
                        if(m!=null)
                        { m.setVisible (false);}
                    }
                    for (Marker m : selectedOpenShopMarkers) {
                        if(m!=null)
                        { m.setVisible (false);}
                    }
                }
                if(index==4){
                    showToast("Safe city cameras filter option selected");
                    for (Marker m : selectedStationMarkers) {
                        if(m!=null)
                        { m.setVisible (false);}
                    }
                    for (Marker m : selectedCCTVMarkers) {
                        if(m!=null)
                        { m.setVisible (true);}
                    }
                    for (Marker m : selectedPedestrianSensorMarkers) {
                        if(m!=null)
                        { m.setVisible (false);}
                    }
                    for (Marker m : selectedOpenShopMarkers) {
                        if(m!=null)
                        { m.setVisible (false);}
                    }
                }
                if(index==5){
                    showToast("Pedestrian sensors filter option selected");
                    for (Marker m : selectedStationMarkers) {
                        if(m!=null)
                        { m.setVisible (false);}
                    }
                    for (Marker m : selectedCCTVMarkers) {
                        if(m!=null)
                        { m.setVisible (false);}
                    }
                    for (Marker m : selectedPedestrianSensorMarkers) {
                        if(m!=null)
                        { m.setVisible (true);}
                    }
                    for (Marker m : selectedOpenShopMarkers) {
                        if(m!=null)
                        { m.setVisible (false);}
                    }
                }
            }

            @Override
            public void onExpand() {
//                showToast("onExpand");
            }

            @Override
            public void onCollapse() {
//                showToast("onCollapse");
            }
        });
    }

    private void displaySensors(){
        /**
         Displaying sensor  markers
         */
        for (PedestrianSensor pedestrianSensor : selectedPedestrianSensor.values ()) {
            HashMap<String, String> markerdetails = new HashMap<String, String>();

            pedestrianSensorMarker = null;
            MarkerOptions pedoptions = new MarkerOptions ();
            LatLng sensorLatLng = new LatLng (pedestrianSensor.getLatitude (),pedestrianSensor.getLongitude ());
            pedoptions.position (sensorLatLng);
            pedoptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("sensornew3",150,150)));
            pedoptions.title ("Sensor " + pedestrianSensor.getSensor_id ());
            // options2.snippet ("Detail: "+pedestrianSensor.getSensor_description ());


            //Creating a retrofit object
            Retrofit retrofit = new Retrofit.Builder ()
                    .baseUrl (PedestrianSensorAPI.BASE_URL)
                    .addConverterFactory (GsonConverterFactory.create ()) //Here we are using the GsonConverterFactory to directly convert json data to object
                    .build ();
            //creating the api interface
            PedestrianSensorAPI api = retrofit.create (PedestrianSensorAPI.class);
            Call<PedestrianCount> call = api.getPedestrianCount (pedestrianSensor.getSensor_id ());
            CustomInfoWindowAdapter customInfoWindow = new CustomInfoWindowAdapter(this);
            map.setInfoWindowAdapter(customInfoWindow);

            call.enqueue(new Callback<PedestrianCount>() {
                @Override
                public void onResponse(Call<PedestrianCount> call, Response<PedestrianCount> response) {
                    PedestrianCount pedestrianCount = response.body();

                    if(pedestrianCount!=null) {
                        //String time_count = time + total_of_directions;
                        //pedestrianSensorMarker=map.addMarker (options2);
                        pedestrianSensorMarker=map.addMarker (pedoptions);

                        pedestrianSensorMarker.showInfoWindow ();
                        markerdetails.put ("address", pedestrianSensor.getSensor_description ());
                        markerdetails.put ("time", pedestrianCount.getTime ());
                        markerdetails.put ("total_of_directions", pedestrianCount.getTotal_of_directions ());
                        markerdetails.put ("predict_time", pedestrianCount.getPrediction_time ());
                        markerdetails.put ("predict_total", pedestrianCount.getPrediction_counts ());
                        markerdetails.put ("busyness", pedestrianCount.getBusyness ());
                        markerdetails.put("this_is","pedestrian_count");
                        markerdetails.put ("Id", pedestrianSensorMarker.getId ());

                        pedestrianSensorMarker.setTag (markerdetails);

                        selectedPedstrianSensorMarkerOptions.add (pedoptions);
                        selectedPedestrianSensorMarkers.add (pedestrianSensorMarker);
                    }
                }

                //selectedPedestrianSensorMarkers.add (pedestrianSensorMarker);
                @Override
                public void onFailure(Call<PedestrianCount> call, Throwable throwable) {
                    Log.e(LOG_TAG, throwable.toString());
                }
            });

        }

    }


    private void showToast(String Msg) {
        //Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        LayoutInflater inflater = getLayoutInflater();

        View layout = inflater.inflate(R.layout.toast,
                (ViewGroup) findViewById(R.id.custom_toast_layout));

        TextView text = (TextView) layout.findViewById(R.id.textToShow);

        text.setText(Msg);

        Toast toast = new Toast(getApplicationContext());

        toast.setGravity(Gravity.BOTTOM | 0, 0, 550);

        toast.setDuration(Toast.LENGTH_SHORT);

        toast.setView(layout);

        toast.show();
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

        //System.out.println ("highlight polyline id" + polyline.getId ());
        for (Polyline polyline1 : polylines) {
            //Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if (polyline.getId ().equals (polyline1.getId ())) {
                polyline1.setColor (ContextCompat.getColor (this, R.color.colorBlue));
                polyline1.setZIndex (1000);
                try{

                    System.out.println ("finalTotalValues length"+finalTotalValues.length ());
                    for(int k =0;k<finalTotalValues.length ();k++){
                        JSONObject jsonObject = finalTotalValues.getJSONObject (k);
                        String polylineId = jsonObject.getString ("polylineId");
                        String routeDis = jsonObject.getString ("routeDistance");
                        String routeDur = jsonObject.getString ("routeDuration");
                        String satCount = jsonObject.getString ("selectedStationCount");
                        String ccCount = jsonObject.getString ("selectedCCTVCount");
                        String psCount = jsonObject.getString ("pedestrianSensorCount");
                        String osCount = jsonObject.getString ("openShopCount");
                        String safetyScore = jsonObject.getString ("safetyScore");

                        if (polylineId.equals (polyline.getId ())) {
                            routeDistance.setText (routeDis);
                            routeDuration.setText (routeDur);
                            stationCount.setText (satCount);
                            cctvCount.setText (ccCount);
                            sensorCount.setText (psCount);
                            openShopCount.setText (osCount);
                            safetyScoreText.setText (safetyScore);
                            if(Integer.parseInt (safetyScore)==maxSafetyScore){
                                layout.setVisibility (View.VISIBLE);
                            }
                            else{
                                layout.setVisibility (View.INVISIBLE);
                            }
                        }
                    }
                }catch (JSONException e) {
                    e.printStackTrace ();
                }

            } else {
                polyline1.setColor (ContextCompat.getColor (this, R.color.colorSecondaryText));
                polyline1.setZIndex (1);
            }
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

    public static int getMaxValue(List<Integer> numbers){
        int maxValue = numbers.get (0);
        for(int i=1;i<numbers.size ();i++){
            if(numbers.get (i) > maxValue){
                maxValue = numbers.get (i);
            }
        }
        return maxValue;
    }


    public int calculateSafetyScore(int cctv,int shop,int station,int dist){

        double cs = 0;
        double im = 0;
        int ss = 0;

        cs = ((0.88 * cctv + 4.4 * shop + 8.8 * station) / dist) * 1000;
        im =  (0.0004 * Math.pow((cs/2 - 50), 3));


        ss = (int) Math.round( ( Math.sqrt (60 + im ) ) * 9.8 );


        return ss;
    }


    /**
     Get user Location
     */

    public void getUserLocation(){


        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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

