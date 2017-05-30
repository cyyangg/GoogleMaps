package com.example.yangc9915.googlemaps;

import android.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private boolean isGPSenabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation=false;
    private static final long MIN_TIME_BW_UPDATES = 1000*15*1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;

    private static final CharSequence[] MAP_TYPE_ITEMS = {"Road Map", "Hybrid", "Satellite", "Terrain"};



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


        mMap.setMyLocationEnabled(true);
    }

    public void getLocation(){
        try{
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            isGPSenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(isGPSenabled) Log.d("MyMaps", "getLocation: GPS enabled");

            //get network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(isNetworkEnabled) Log.d("MyMaps", "getLocation: Network enabled");

            if(!isGPSenabled && !isNetworkEnabled){
                Log.d("MyMaps", "getLocation: No provider is enabled");


            } else {
                this.canGetLocation = true;

                if(isNetworkEnabled){
                    Log.d("MyMaps", "getLocation: Network enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerNetwork);

                    Log.d("MyMaps", "getLocation: NetworkLocation update request successful");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);
                }

                if(isGPSenabled){
                    Log.d("MyMaps", "getLocation: GPS enabled - requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            locationListenerGPS);

                    Log.d("MyMaps", "getLocation: GPSLocation update request successful");
                    Toast.makeText(this, "Using Network", Toast.LENGTH_SHORT);
                }
            }

        }catch (Exception e){
            Log.d("MyMaps", "Caught exception in getLocation");
            e.printStackTrace();
        }
    }

    public void onLocationChanged(Location location){

        Log.d("MyMaps", "onLocationChanged");

        //when location changes, zoom in on location
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
        this.mMap.moveCamera(center);

        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        this.mMap.animateCamera(zoom);

    }

    

    public void toggleView(GoogleMap googleMaps){
        //prepare dialog by setting up builder
        final String fDialogTitle = "Select Map Type";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fDialogTitle);
        Log.d("MyMaps", "toggleView: dialog builder success");

        //find current map type
        int checkItem = mMap.getMapType()-1;
        Log.d("MyMaps", "toggleView: found map type");

        //add onClickListener so selection will be handled
        builder.setSingleChoiceItems(MAP_TYPE_ITEMS, checkItem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item){
                Log.d("MyMaps", "toggleView: ???");
                //Perform action depending on which item was selected
                switch(item)
                {
                    case 1:
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case 2:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case 3:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    default:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                dialog.dismiss();
            }

        }
        );

        //build dialog and show
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();

    }





}
