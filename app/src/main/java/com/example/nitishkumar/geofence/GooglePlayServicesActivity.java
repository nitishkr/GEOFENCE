package com.example.nitishkumar.geofence;


import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;


public class GooglePlayServicesActivity extends FragmentActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener,  ResultCallback<Status> {
    protected static final String TAG = "GEOFENCING";
    protected ArrayList<Geofence> mGeofenceList;
    private boolean mGeofencesAdded;
    private SharedPreferences mSharedPreferences;
    private GoogleMap mMap;
    private LatLng mGeofenceLatLng = new LatLng(28.5444498,77.2726199);
    private int mRadius = 1000;
    private Geofence mGeofence;
    private Location mCurrentLocation;
    private String email;
    private GoogleApiClient mLocationClient;
    private LocationRequest mLocationRequest;
    private CircleOptions mCircleOptions;
    private Circle mCircle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        email =  this.getIntent().getExtras().getString("Email");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.disconnect();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mLocationClient, mLocationRequest, this);
    }
    protected void onResume() {
        super.onResume();
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            setUpMapIfNeeded();

        } else {
            GooglePlayServicesUtil.getErrorDialog(
                    GooglePlayServicesUtil.isGooglePlayServicesAvailable(this),
                    this, 0);
        }
    }

    private void setUpMapIfNeeded() {

        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }


    private void setUpMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mGeofenceLatLng.latitude, mGeofenceLatLng.longitude), 14));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setIndoorEnabled(false);
        mMap.setMyLocationEnabled(true);

        mCircleOptions = new CircleOptions()
                .center(mGeofenceLatLng).radius(mRadius).fillColor(0x40ff0000)
                .strokeColor(Color.TRANSPARENT).strokeWidth(2);
        mCircle = mMap.addCircle(mCircleOptions);
    }

    @Override

    public void onConnected(Bundle arg0) {

        Log.v("GEOFENCE", "Connected to location services.");

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mLocationClient, mLocationRequest, this);

        ArrayList<Geofence> geofences = new ArrayList<Geofence>();

        mGeofence = new Geofence.Builder()
                .setRequestId("Geofence")
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setCircularRegion(mGeofenceLatLng.latitude, mGeofenceLatLng.longitude, mRadius)
                .setExpirationDuration((1000 * 60) * 60)
                .setLoiteringDelay(1000)
                .build();

        geofences.add(mGeofence);

        Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);
        intent.putExtra("Email", email);

        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mLocationClient,
                    geofences,
                    pendingIntent
            ).setResultCallback(this);
        } catch (SecurityException securityException) {

        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v(TAG, location.toString());
    }

    @Override
    public void onResult(Status result) {
        if (result.isSuccess()) {
            Log.v(TAG, "Success!");
        } else if (result.hasResolution()) {
            // TODO Handle resolution
        } else if (result.isCanceled()) {
            Log.v(TAG, "Canceled");
        } else if (result.isInterrupted()) {
            Log.v(TAG, "Interrupted");
        } else {

        }

    }
}