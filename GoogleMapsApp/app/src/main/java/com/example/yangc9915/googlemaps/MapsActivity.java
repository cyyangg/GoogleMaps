package com.example.yangc9915.googlemaps;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private boolean isGPSenabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 15 * 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
    private Location myLocation;
    private static final int MY_LOC_ZOOM_FACTOR = 18;
    private boolean trackerOn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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


        //Add a marker in birth place and move the camera
        LatLng boise = new LatLng(43, -116);
        mMap.addMarker(new MarkerOptions().position(boise).title("born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(boise));

        //current location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("GoogleMaps", "Failed Permission check 1");
            Log.d("GoogleMaps", Integer.toString(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)));
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("GoogleMaps", "Failed Permission check 2");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }


        //mMap.setMyLocationEnabled(true);
    }

    /**
     * --------------------------------------------------------
     * CHANGE VIEW
     * --------------------------------------------------------
     */
    public void changeView(View view) {
        if (mMap.getMapType() != GoogleMap.MAP_TYPE_SATELLITE) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }


    /**
     * --------------------------------------------------------
     * TRACKER ON/OFF
     * --------------------------------------------------------
     */
    public void trackerToggle(View view){
        if(trackerOn==false){
            getLocation(view);
            isGPSenabled=true;
            isNetworkEnabled=true;
            Log.d("MyMaps", "trackerToggle: tracker ON");
        }

        else{
            canGetLocation=false;
            isGPSenabled=false;
            isNetworkEnabled=false;
            trackerOn=false;
            Log.d("MyMaps", "trackerToggle: tracker OFF");
        }
    }




    /**
     * --------------------------------------------------------
     * GET LOCATION
     * --------------------------------------------------------
     */
    public void getLocation(View view) {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSenabled) Log.d("MyMaps", "getLocation: GPS enabled");

            //get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMaps", "getLocation: Network enabled");

            if (!isGPSenabled && !isNetworkEnabled) {
                Log.d("MyMaps", "getLocation: No provider is enabled");


            } else {
                this.canGetLocation = true;

                if (isNetworkEnabled) {
                    Log.d("MyMaps", "getLocation: Network enabled - requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

                    Log.d("MyMaps", "getLocation: NetworkLocation update request successful");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);
                }

                if (isGPSenabled) {
                    Log.d("MyMaps", "getLocation: GPS enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGPS);

                    Log.d("MyMaps", "getLocation: GPSLocation update request successful");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);
                }
            }

        } catch (Exception e) {
            Log.d("MyMaps", "Caught exception in getLocation");
            e.printStackTrace();
        }

        trackerOn = true;
    }


    /**
     * --------------------------------------------------------
     * request updates from NETWORK_PROVIDER
     * --------------------------------------------------------
     */
    public void requestUpdatesNetworkProvider() {
        //permission
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //request
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                locationListenerNetwork);
    }

    /**
     * --------------------------------------------------------
     * LOCATION LISTENER GPS
     * --------------------------------------------------------
     */
    android.location.LocationListener locationListenerGPS = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output is Log.d and Toast that GPS is enabled and working
            Log.d("MyMaps", "onLocationChanged: GPS enabled and working");
            Toast.makeText(MapsActivity.this, "GPS enabled and working", Toast.LENGTH_SHORT);

            //drop a marker on map - create method called dropMarker
            dropMarker(LocationManager.GPS_PROVIDER);
            Log.d("MyMaps", "OnLocationChanged: Dropping markers-network");


            //remove network location updates. hint: see LocationManager for update removal method
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            locationManager.removeUpdates(locationListenerNetwork);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output is Log.d and Toast that GPS is enabled and working
            Log.d("MyMaps", "onStatusChanged: Network switched to GPS");
            Toast.makeText(MapsActivity.this, "Network switched to GPS", Toast.LENGTH_SHORT);


            //setup a switch statement to check the status input parameter
            switch (status) {
                //case LocationProvider.AVAILABLE --> output message to Log.d and Toast
                case LocationProvider.AVAILABLE:
                    Log.d("MyMaps", "onStatusChanged: Network Provider available");
                    Toast.makeText(MapsActivity.this, "Network Provider available", Toast.LENGTH_SHORT);

                    break;

                //case LocationProvider.OUT_OF_SERVICE --> output messages and request updates from NETWORK_PROVIDER
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "onStatusChanged: Network Provider Out of Service");
                    Toast.makeText(MapsActivity.this, "Network Provider Out of Service", Toast.LENGTH_SHORT);

                    requestUpdatesNetworkProvider();

                    break;

                //case LocationProvider.TEMPORARILY_UNAVAILABLE --> request updates from NETWORK_PROVIDER
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMaps", "onStatusChanged: Network Provider Temporarily Unavailable");
                    Toast.makeText(MapsActivity.this, "Network Provider Temporarily Unavailable", Toast.LENGTH_SHORT);

                    requestUpdatesNetworkProvider();

                    break;

                //case default --> request updates from NETWORK_PROVIDER
                default:
                    requestUpdatesNetworkProvider();

            }


        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

    };


    /**
     * --------------------------------------------------------
     * LOCATION LISTENER NETWORK
     * --------------------------------------------------------
     */
    android.location.LocationListener locationListenerNetwork = new android.location.LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //output is Log.d and Toast that GPS is enabled and working
            Log.d("MyMaps", "OnLocationChanged: GPS enabled and working");
            Toast.makeText(MapsActivity.this, "GPS enabled and working", Toast.LENGTH_SHORT);

            //drop a marker on map - create method called dropMarker
            dropMarker(LocationManager.NETWORK_PROVIDER);
            Log.d("MyMaps", "OnLocationChanged: Dropping markers-network");

            //relaunch the network provider request (requestLocationUpdates (NETWORK_PROVIDER))
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListenerNetwork);

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //output message in Log.d and Toast
            Log.d("MyMaps", "onStatusChanged: Network status changed");
            Toast.makeText(MapsActivity.this, "Network status changed", Toast.LENGTH_SHORT);

        }

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };


    /**
     * --------------------------------------------------------
     * DROP MARKER
     * --------------------------------------------------------
     */
    public void dropMarker(String provider) {
        LatLng userLocation = null;

        if(locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);
        }

        if(myLocation == null){
            //display message via log.d and/or toast
            Log.d("MyMaps", "dropMarker: myLocation is null");
            Toast.makeText(MapsActivity.this, "myLocation is null", Toast.LENGTH_SHORT);
        }

        else {
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            //display message with lat/long
            Toast.makeText(MapsActivity.this, ""+userLocation, Toast.LENGTH_SHORT);

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            //drop actual marker on map
            //if using circles, reference Android Circle class
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(userLocation)
                    .radius(3)
                    .strokeColor(Color.BLACK)
                    .strokeWidth(2)
                    .fillColor(Color.RED));

            mMap.animateCamera(update);
        }
    }


}
