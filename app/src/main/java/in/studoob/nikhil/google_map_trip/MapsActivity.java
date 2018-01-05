package in.studoob.nikhil.google_map_trip;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener, LocationListener ,GoogleMap.OnInfoWindowClickListener {

    private Location mLocation;
    private GoogleMap mMap;
    String destination;
    String provider;
    private LocationManager locationManager;
    private List<Polyline> polylines;
    Location getLastLocation;
    LatLng searchlocation;
    private static final int[] COLORS = new int[]{R.color.blue, R.color.yello, R.color.black, R.color.green, R.color.blue};
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 150;
    private final static int PERMISSIONS_REQUEST_LOCATION = 220;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        checkLocationPermission();

        polylines = new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        startLocation();
        getLocation();
        final TextView ride = findViewById(R.id.sheet_ride);
        LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setPeekHeight(50);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                  if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                      ride.setText("HIDE");
                  } else {
                    ride.setText("START RIDE");
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        FloatingActionButton fab = findViewById(R.id.mylocation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location location = locationManager.getLastKnownLocation(provider);
                    mMap.clear();
                    CircleOptions circleOptions = new CircleOptions()
                            .center(new LatLng(location.getLatitude(),location.getLongitude()))
                            .strokeWidth(10)
                            .strokeColor(Color.TRANSPARENT)
                            .fillColor(Color.argb(50, 0, 150, 250))
                            .radius(1000);
                    getLastLocation = location;
                    if (location != null) {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(getLastLocation.getLatitude(), getLastLocation.getLongitude())).title("Your Location"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(getLastLocation.getLatitude(), getLastLocation.getLongitude()), 14));
                        Circle circle = mMap.addCircle(circleOptions);
                        updateLocation(location);
                    }
                }
            }
        });

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                 destination = place.getName().toString();
                searchlocation = place.getLatLng();

                getRoute(searchlocation);

            }

            @Override
            public void onError(Status status) {
                Log.i("lol", "An error occurred: " + status);
            }
        });


    }
    public void startLocation(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider, 1000, 10, this);
        }
    }

    public  void  getLocation(){

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastLocation = locationManager.getLastKnownLocation(provider);
        }
    }

    public  void  updateLocation(Location location) {
        if (location != null) {
            getLastLocation = location;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(MapsActivity.this, location.getAccuracy() + "", Toast.LENGTH_SHORT).show();
     //   mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));
       // mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location"));
        updateLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));
                updateLocation(location);

            }
        }

    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Needed location permission ")
                        .setMessage("accepect permission")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        locationManager.requestLocationUpdates(provider, 400, 1, this);
                    }

                } else {


                }
                return;
            }

        }
    }


    @Override
    public void onRoutingFailure(RouteException e) {
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {
        LatLng start = new LatLng(getLastLocation.getLatitude(), getLastLocation.getLongitude());
        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);

        mMap.moveCamera(center);

        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }
        mMap.clear();
        polylines = new ArrayList<>();
        float distance = 0;
        int duration = 0;
        for (int i = 0; i < routes.size(); i++) {
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(routes.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
            distance = (routes.get(i).getDistanceValue()) / 1000;
            duration=((routes.get(i).getDurationValue()) / 60);
            //"Route "+ (i+1) +
            Toast.makeText(getApplicationContext(), "distance KM : " + distance + ": duration mins : " +duration, Toast.LENGTH_LONG).show();
        }

        MarkerOptions source = new MarkerOptions();
        source.position(start);
        source.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
        mMap.addMarker(source);

        MarkerOptions destin = new MarkerOptions();
        destin.position(searchlocation);
        destin.title(destination);
        destin.snippet("distance KM : "+distance+" duration MINS : "+duration);
       Marker desti = mMap.addMarker(destin);
       desti.showInfoWindow();


    }

    @Override
    public void onRoutingCancelled() {

    }

    private void getRoute(LatLng pickuplatlang) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(getLastLocation.getLatitude(), getLastLocation.getLongitude()), pickuplatlang)
                .build();
        routing.execute();
    }

    private void erasePoly() {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Info window clicked",
                Toast.LENGTH_SHORT).show();
    }
}