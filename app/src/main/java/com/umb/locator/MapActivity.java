package com.umb.locator;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ZoomButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private EditText editLocation = null;
    private ProgressBar pb =null;
    private List<Marker> professors;

    private LocationManager locationMangaer = null;
    private LocationListener locationListener = null;

    private Button btnGetLocation = null;

    private static final String TAG = "Debug";
    private Boolean flag = false;

    private double latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        professors = new ArrayList<>();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, 34992, this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        locationChecker(mGoogleApiClient, MapActivity.this);

        locationMangaer = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        locationListener = new MyLocationListener();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }

    /**
     * Prompt user to enable GPS and Location Services
     * @param mGoogleApiClient
     * @param activity
     */
    public static void locationChecker(GoogleApiClient mGoogleApiClient, final Activity activity) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2 * 1000);
        locationRequest.setFastestInterval(2 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    activity, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.263189, 20.176604), 8.0f));


        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false));
//        double latitude = location.getLatitude();
//        double longitude = location.getLongitude();
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 20.0f));


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationMangaer.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                5, locationListener);
        locationMangaer.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);


//        LatLng sydney = new LatLng(latitude, longitude);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 20.0f));

        LatLng umd1 = new LatLng(41.313099, 19.814720);
        LatLng umd2 = new LatLng(41.313049, 19.814889);
        LatLng umd3 = new LatLng(41.312904, 19.814927);
        LatLng umd4 = new LatLng(41.329013, 19.451704);
        LatLng umd5 = new LatLng(41.328959, 19.451334);


        Marker marker = mMap.addMarker(new MarkerOptions().position(umd1).title("Zeqo").icon(BitmapDescriptorFactory.fromResource(R.drawable.prof1_s)));
        Marker marker1 = mMap.addMarker(new MarkerOptions().position(umd2).title("Zeqo1").icon(BitmapDescriptorFactory.fromResource(R.drawable.prof1_s)));
        Marker marker2 = mMap.addMarker(new MarkerOptions().position(umd3).title("Zeqo2").icon(BitmapDescriptorFactory.fromResource(R.drawable.prof1_s)));
        Marker marker3 = mMap.addMarker(new MarkerOptions().position(umd4).title("Zeqo3").icon(BitmapDescriptorFactory.fromResource(R.drawable.prof1_s)));
        Marker marker4 = mMap.addMarker(new MarkerOptions().position(umd5).title("Zeqo4").icon(BitmapDescriptorFactory.fromResource(R.drawable.prof1_s)));

        professors.add(marker);
        professors.add(marker1);
        professors.add(marker2);
        professors.add(marker3);
        professors.add(marker4);




    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            Log.v(TAG, "locChangedCalled");
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();


            Toast.makeText(getBaseContext(),"Location changed : Lat: " +
                            loc.getLatitude()+ " Lng: " + loc.getLongitude(),
                    Toast.LENGTH_SHORT).show();
            //String longitude = "Longitude: " +loc.getLongitude();
            //Log.v(TAG, longitude);
            //String latitude = "Latitude: " +loc.getLatitude();
            //Log.v(TAG, latitude);

            LatLng sydney = new LatLng(loc.getLatitude(), loc.getLongitude());
            DistanceCalculator dist = new DistanceCalculator();

            for (Marker marker : professors) {
                if (dist.distance(marker.getPosition().latitude, marker.getPosition().longitude, latitude, longitude, "K") < 0.002){
//                if(marker.getPosition().latitude == latitude && marker.getPosition().longitude == longitude){
//
//                }
                    marker.remove();
                }
            }

            //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney").icon(BitmapDescriptorFactory.fromResource(R.drawable.prof1_s)));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 20.0f));



    /*----------to get City-Name from coordinates ------------- */
//            String cityName=null;
//            Geocoder gcd = new Geocoder(getBaseContext(),
//                    Locale.getDefault());
//            List<Address>  addresses;
//            try {
//                addresses = gcd.getFromLocation(loc.getLatitude(), loc
//                        .getLongitude(), 1);
//                if (addresses.size() > 0)
//                    System.out.println(addresses.get(0).getLocality());
//                cityName=addresses.get(0).getLocality();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            String s = longitude+"\n"+latitude +
//                    "\n\nMy Currrent City is: "+cityName;
//            editLocation.setText(s);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }


}
