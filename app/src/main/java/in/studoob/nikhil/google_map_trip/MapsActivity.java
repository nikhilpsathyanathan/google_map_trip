package in.studoob.nikhil.google_map_trip;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionEventListener, LocationListener {

    private GoogleMap mMap;
    private final String TAG = "123";
    private EditText editText;
    private GoogleApiClient mGoogleApiclient;
    private LocationManager locationManager;
    LocationListener locationListener;
    private double lat, lon;
    Location getLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationManager = (LocationManager)getSystemService
                (Context.LOCATION_SERVICE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();
                LatLng x = new LatLng(lat, lon);

                Geocoder geo = new Geocoder(MapsActivity.this);
                try {
                    List<Address> addresses = geo.getFromLocation(lat, lon, 10);
                    String adreess = addresses.get(0).getAddressLine(0);
                    String country = addresses.get(0).getCountryName();
                    String permises = addresses.get(0).getPremises();
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(x).title("Country:" + country + "\nAdress" + adreess + "\nPermises" + permises));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(x));
                    mMap.moveCamera(CameraUpdateFactory.zoomBy(10));

                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(x).title("My location").icon(BitmapDescriptorFactory.fromResource(R.drawable.house)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(x));
                mMap.moveCamera(CameraUpdateFactory.zoomBy(10));
                Toast.makeText(MapsActivity.this, "Location Changed", Toast.LENGTH_LONG).show();
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
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 500, locationListener);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.mylocation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                getLastLocation = locationManager.getLastKnownLocation
                        (LocationManager.PASSIVE_PROVIDER);
               double currentLongitude = getLastLocation.getLongitude();
              double  currentLatitude = getLastLocation.getLatitude();

              LatLng  currentLocation = new LatLng(currentLatitude, currentLongitude);

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("My location").icon(BitmapDescriptorFactory.fromResource(R.drawable.house)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mMap.moveCamera(CameraUpdateFactory.zoomBy(10));
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(MapsActivity.this,location+"",Toast.LENGTH_LONG).show();

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
    public void connectionClosed(ConnectionEvent connectionEvent) {

    }

    @Override
    public void connectionErrorOccurred(ConnectionEvent connectionEvent) {

    }
}
