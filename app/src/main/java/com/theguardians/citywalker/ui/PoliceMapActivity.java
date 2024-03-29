package com.theguardians.citywalker.ui;
/**
 * This class is utilised for navigation screen to nearest police station
 * @Author Sharuq
 * @Version 2.1
 */
import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.util.List;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.widget.TextView;
import android.widget.Toast;

// classes needed to initialize map
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

// classes needed to add the location component
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;

// classes needed to add a marker
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

// classes to calculate a route
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;

// classes needed to launch navigation UI
import android.view.View;
import android.widget.Button;

import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.theguardians.citywalker.Model.PoliceStation;
import com.theguardians.citywalker.R;


public class PoliceMapActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {
    // variables for adding location layer
    private MapView mapView;
    private MapboxMap mapboxMap;
    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private static final String TAG = "PoliceMapActivity";
    private NavigationMapRoute navigationMapRoute;
    // variables needed to initialize navigation
    private Button button;
    private LatLng origin;
    private LatLng destination;
    private PoliceStation nearPol =new PoliceStation ();
    private Point originPlace;
    private Point destinationPlace;
    private TextView stationname;
    private TextView timeValue;
    private TextView distanceValue;
    private CardView maincard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        Mapbox.getInstance (this, getString (R.string.access_token));
        setContentView (R.layout.activity_police_map);
        mapView = findViewById (R.id.mapView);
        mapView.onCreate (savedInstanceState);
        stationname =findViewById (R.id.stationname);
        timeValue =findViewById (R.id.timeValue);
        distanceValue =findViewById (R.id.distanceValue);
        maincard =findViewById (R.id.maincard);
        //maincard.setVisibility (View.INVISIBLE);
        button = findViewById(R.id.startButton);
        //originPlace = getCurrentLocation ();
        Bundle bundle = getIntent().getExtras();
        origin = bundle.getParcelable ("originPoint");
        destination = bundle.getParcelable ("destinationPoint");
        nearPol = (PoliceStation) bundle.getSerializable ("policestation");
        originPlace = Point.fromLngLat (origin.getLongitude (), origin.getLatitude ());
        destinationPlace = Point.fromLngLat (destination.getLongitude (), destination.getLatitude ());

        stationname.setText (nearPol.getPolice_station ());
        mapView.getMapAsync (this);

    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;


        IconFactory iconFactory = IconFactory.getInstance(PoliceMapActivity.this);
        Icon icon = iconFactory.fromResource(R.drawable.mapbox_marker_icon_default);

        mapboxMap.addMarker (new MarkerOptions ()
                .position (new com.mapbox.mapboxsdk.geometry.LatLng (destinationPlace.latitude (), destinationPlace.longitude ()))
                .title (nearPol.getPolice_station ())
                .snippet (" Address: "  +nearPol.getAddress ()+ "   Telephone: " +nearPol.getTel ())
                .icon (icon));

        mapboxMap.setStyle (Style.LIGHT, new Style.OnStyleLoaded () {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
               enableLocationComponent (style);

                //addDestinationIconSymbolLayer (style);

                GeoJsonSource source = mapboxMap.getStyle ().getSourceAs ("destination-source-id");
                if (source != null) {
                    source.setGeoJson (Feature.fromGeometry (destinationPlace));
                }



                System.out.println ("Origin place: "+originPlace);

                System.out.println ("destination place: "+destinationPlace);

                getRoute (originPlace, destinationPlace);

                //mapboxMap.addOnMapClickListener(PoliceMapActivity.this);

                button.setEnabled(true);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean simulateRoute = true;
                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                .shouldSimulateRoute(false)
                                .waynameChipEnabled (true)
                                .build();

                        // Call this method with Context from within an Activity
                        NavigationLauncher.startNavigation(PoliceMapActivity.this, options);
                       // maincard.setVisibility (View.VISIBLE);
                    }
                });
            }
        });
    }


    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    @SuppressLint("MissingPermission")
    private Point getCurrentLocation(){

        Point originPoint = Point.fromLngLat (locationComponent.getLastKnownLocation ().getLongitude (),
                locationComponent.getLastKnownLocation ().getLatitude ());
        System.out.println ("Origin: "+originPoint);
        return originPoint;

    }
    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .profile(DirectionsCriteria.PROFILE_WALKING)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        System.out.println ("Origin"+origin);
                        System.out.println ("Destination"+destination);

                        currentRoute = response.body().routes().get(0);


                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                        int time = (int) (currentRoute.duration ()/60);
                        timeValue.setText (time +" min");

                        distanceValue.setText ("("+currentRoute.distance ().toString () + " m"+")");

                       // button.callOnClick ();

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}