package com.ahmet.orderfoodserver.UIMain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ahmet.orderfoodserver.Common.Common;
import com.ahmet.orderfoodserver.Common.DirectionJSONParser;
import com.ahmet.orderfoodserver.R;
import com.ahmet.orderfoodserver.Remote.IGeoCoordinates;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrakingOrderActivity extends FragmentActivity implements OnMapReadyCallback
                , GoogleApiClient.ConnectionCallbacks
                , GoogleApiClient.OnConnectionFailedListener
                , LocationListener {

    private GoogleMap mMap;

    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;


    private static int UPDATE_INTERVAL = 1000;
    private static int FATEST_INTERVAL = 1001;
    private static int DISPLACEMENT = 1002;

    private IGeoCoordinates mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traking_order);

        mService = Common.getIGeoCodeService();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestRunTimePermission();

        } else {
            // permission has been granted, continue as usual

            if (checkPlayServices()){

                buildGoogleApiClient();
                createLocationRequest();
            }

        }

        displayLocation();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void displayLocation() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            requestRunTimePermission();
        }else {

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null){
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();

                // Add marker in your location and move camera
                LatLng userLocation = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomBy(17.0f));

                // After add Marker for your location,And Marker for this Order and draw route
                drawRoute(userLocation, Common.mCurrentRequest.getAddress());

            }else {
                Toast.makeText(this, "Cannot get the location", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void drawRoute(final LatLng userLocation, String address) {

        mService.getGeoCode(address).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                try {

                    JSONObject jsonObject = new JSONObject(response.body().toString());

                    String lat = ((JSONArray)jsonObject.get("result"))
                                    .getJSONObject(0)
                                    .getJSONObject("geometry")
                                    .getJSONObject("location")
                                    .get("lat").toString();

                    String lng = ((JSONArray)jsonObject.get("result"))
                            .getJSONObject(0)
                            .getJSONObject("geometry")
                            .getJSONObject("location")
                            .get("lng").toString();

                    LatLng orderLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.box);
                    bitmap = Common.scaleBitmap(bitmap, 70, 70);

                    MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                            .title("Order of " + Common.mCurrentRequest.getPhone())
                                            .position(orderLocation);

                    mMap.addMarker(markerOptions);

                    // Draw route
                    mService.getDirections(userLocation.latitude + "," + userLocation.longitude,
                                            orderLocation.latitude + "," + orderLocation.longitude)
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    new ParserTask().execute(response.body().toString());
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {

                                }
                            });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }


    private void createLocationRequest() {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

    }

    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS){

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, Common.CODE_PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }else {
                Toast.makeText(this, "This device is not support", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;

    }

    private void requestRunTimePermission() {

        // Check Permissions Now
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                Common.CODE_LOCATION_PERMISSION_REQUEST);
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

       // mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Add a marker in Sydney and move the camera
        // Default Location
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();

        startLocationUpdates();
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            return;

        }else {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest,
                    this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){

            case Common.CODE_LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (checkPlayServices()){

                        buildGoogleApiClient();
                        createLocationRequest();

                        displayLocation();
                    }
                }
                break;
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        ProgressDialog dialog = new ProgressDialog(TrakingOrderActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait....");
            dialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {

            JSONObject jsonObject;

            List<List<HashMap<String, String>>> mListRoutes = null;

            try {

                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                mListRoutes = parser.parse(jsonObject);


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return mListRoutes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {

            dialog.dismiss();

            ArrayList mListPoints = null;
            PolylineOptions polylineOptions = null;

            for (int i = 0; i < lists.size(); i++){

                mListPoints = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String, String>> mListPath = lists.get(i);

                for (int x = 0; x < mListPath.size(); x++){

                    Map<String, String> mMappoint = mListPath.get(x);
                    double lat = Double.parseDouble(mMappoint.get("lat"));
                    double lng = Double.parseDouble(mMappoint.get("lng"));
                    LatLng posiotn = new LatLng(lat, lng);

                    mListPoints.add(posiotn);
                }
                polylineOptions.addAll(mListPoints);
                polylineOptions.width(12);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }
            mMap.addPolyline(polylineOptions);
        }
    }
}
