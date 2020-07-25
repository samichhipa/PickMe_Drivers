package com.example.pickmedrivers.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.example.pickmedrivers.Common.Common;
import com.example.pickmedrivers.Model.DirectionJSONParser;
import com.example.pickmedrivers.Notification.CustomerTokens;
import com.example.pickmedrivers.Notification.Data;
import com.example.pickmedrivers.Notification.IFCMService;
import com.example.pickmedrivers.Notification.MyResponse;
import com.example.pickmedrivers.Notification.Sender;
import com.example.pickmedrivers.R;
import com.example.pickmedrivers.Retrofit.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.pickmedrivers.Common.Common.lastLocation;

public class DriverTrackingActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;

    String lat, lng;

    String customer_id;

    //Presence System//
    DatabaseReference currentUserRef, onlineRef;

    private static int MY_PERMISSION_REQ_CODE = 1000;
    private static int PLAY_SERVICES_RES_REQUEST = 10001;

    LocationRequest locationRequest;

    GoogleApiClient googleApiClient;

    Marker currentMarker, driver_marker;


    DatabaseReference reference;

    private Circle riderMarker;


    private Polyline direction;
    private IGoogleAPI mService;
    private IFCMService ifcmService;

    GeoFire geoFire;

    Button StartTripBtn;

    Location pickUpLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (getIntent() != null) {

            lat = getIntent().getStringExtra("lat");
            lng = getIntent().getStringExtra("lng");
            customer_id = getIntent().getStringExtra("customer_id");

        }

        StartTripBtn = findViewById(R.id.startTripBtn);
        mService = Common.getGoogleAPI();
        ifcmService = Common.getFCMService();
        reference = FirebaseDatabase.getInstance().getReference().child("CustomerTokens");


        getPermission();


        StartTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (StartTripBtn.getText().equals("Start Trip")) {

                    pickUpLocation = lastLocation;
                    StartTripBtn.setText("Drop Off");

                } else if (StartTripBtn.getText().equals("Drop Off")) {


                    calculateRideFee(pickUpLocation, lastLocation);
                }

            }
        });
        //setUpLocation();
        //displayLocation();


    }

    private void calculateRideFee(final Location pickUpLocation, final Location lastLocation) {

        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference-less_driving&" +
                    "origin=" + pickUpLocation.getLatitude() + "," + pickUpLocation.getLongitude() + "&" +
                    "destination=" + lastLocation.getLatitude() + "," + lastLocation.getLongitude() + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);

            Log.d("SAMI", requestApi);

            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try {

                        JSONObject jsonObject = new JSONObject(response.body().toString());

                        JSONArray routes = jsonObject.getJSONArray("routes");

                        JSONObject object = routes.getJSONObject(0);

                        JSONArray legs = object.getJSONArray("legs");

                        JSONObject legsObject = legs.getJSONObject(0);

                        JSONObject distance = legsObject.getJSONObject("distance");
                        String distance_txt = distance.getString("text");
                        Double distance_val = Double.parseDouble(distance_txt.replaceAll("[^0-9\\\\.]+", ""));

                        JSONObject time = legsObject.getJSONObject("duration");
                        String time_txt = time.getString("text");
                        Integer time_val = Integer.parseInt(time_txt.replaceAll("[^0-9\\\\.]+", ""));

                        //Now, get address

                        String start_address = legsObject.getString("start_address");
                        String end_address = legsObject.getString("end_address");



                        sendNotificationForRating();
                        Intent intent=new Intent(DriverTrackingActivity.this, TripDetailsActivity.class);
                        intent.putExtra("source_address",start_address);
                        intent.putExtra("destination_address",end_address);
                        intent.putExtra("time",String.valueOf(time_val));
                        intent.putExtra("distance",String.valueOf(distance_val));
                        intent.putExtra("location_start",String.format("%f,%f",pickUpLocation.getLatitude(),pickUpLocation.getLongitude()));
                        intent.putExtra("location_end",String.format("%f,%f",lastLocation.getLatitude(),lastLocation.getLongitude()));
                        intent.putExtra("price",String.valueOf(Common.getPrice(distance_val,time_val)));
                        startActivity(intent);
                        finish();

                    } catch (Exception e) {


                    }

                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                    Toast.makeText(DriverTrackingActivity.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();

                }
            });

        } catch (Exception e) {


        }

    }

    private void sendNotificationForRating() {

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {


                    CustomerTokens tokens = snap.getValue(CustomerTokens.class);

                    if (tokens.getCustomer_id().equals(customer_id)) {

                        String customer_token = tokens.getToken_id();

                        Data data = new Data("DropOff", FirebaseAuth.getInstance().getCurrentUser().getUid());

                        Sender sender = new Sender(customer_token, data);
                        ifcmService.sendMessage(sender).enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                if (response.body().success == 1) {

                                //    Toast.makeText(DriverTrackingActivity.this, "Arrived..", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(DriverTrackingActivity.this, response.errorBody().toString(), Toast.LENGTH_SHORT).show();


                                }
                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {

                            }
                        });

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

    private void stopLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    private void setUpLocation() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, DriverTrackingActivity.this);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        riderMarker = mMap.addCircle(new CircleOptions()
                .center(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)))
                .radius(50) //radius is 50m
                .strokeColor(Color.BLUE)
                .fillColor(R.color.colorAccent)
                .strokeWidth(5.0f));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        BuildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference().child("DriversLocation").child(Common.currentDrivers.getCar_type()));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(Double.parseDouble(lat), Double.parseDouble(lng)), 0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {


                sendNotificationToArrivedDriver(customer_id);
                StartTripBtn.setEnabled(true);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    private void sendNotificationToArrivedDriver(final String customer_id) {

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {


                    CustomerTokens tokens = snap.getValue(CustomerTokens.class);

                    if (tokens.getCustomer_id().equals(customer_id)) {

                        String customer_token = tokens.getToken_id();

                        Data data = new Data("Arrived", "Driver has arrived at your place..");

                        Sender sender = new Sender(customer_token, data);
                        ifcmService.sendMessage(sender).enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                if (response.body().success == 1) {

                                    Toast.makeText(DriverTrackingActivity.this, "Arrived..", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(DriverTrackingActivity.this, response.errorBody().toString(), Toast.LENGTH_SHORT).show();


                                }
                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {

                            }
                        });

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    protected synchronized void BuildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {


        lastLocation = location;

        //  setUpLocation();
        displayLocation();
    }

    private void getPermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                    , Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case 1000:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    BuildGoogleApiClient();
                    lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);


                  /*  if (currentMarker != null) {
                        currentMarker.remove();
                    }


                    final double lat = lastLocation.getLatitude();
                    final double lng = lastLocation.getLongitude();






                   */
                    //  currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("My Location"));

                    //  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 13.0f));

                    //  rotateMarker(currentMarker, -360, mMap);
                }

        }

    }

    private void displayLocation() {

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        if (lastLocation != null) {


            if (currentMarker != null) {
                currentMarker.remove();
            }

            final double latitude = lastLocation.getLatitude();
            final double longitude = lastLocation.getLongitude();

            if (driver_marker != null) {

                driver_marker.remove();
            }

            driver_marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                    .title("You")
                    .icon(BitmapDescriptorFactory.defaultMarker()));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));

            if (direction != null) {

                direction.remove();

            }

            getDirection();


            //rotateMarker(currentMarker, -360, mMap);


         /*   geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {


                    if (currentMarker != null) {
                        currentMarker.remove();

                    }
                    currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("My Location"));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 13.0f));




                }
            });


          */

            if (googleApiClient != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, DriverTrackingActivity.this);
            }
        }

    }

    private void getDirection() {

        LatLng currentPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference-less_driving&" +
                    "origin=" + Common.lastLocation.getLatitude() + "," + Common.lastLocation.getLongitude() + "&" +
                    "destination=" + lat + "," + lng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);

            Log.d("SAMI", requestApi);

            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    new ParserTask().execute(response.body().toString());

                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                    Toast.makeText(DriverTrackingActivity.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();

                }
            });

        } catch (Exception e) {


        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {


        ProgressDialog progressDialog = new ProgressDialog(DriverTrackingActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;
            try {

                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();

                routes = parser.parse(jsonObject);
            } catch (Exception e) {

                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            progressDialog.dismiss();

            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for (int i = 0; i < lists.size(); i++) {

                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = lists.get(i);

                for (int j = 0; j < path.size(); j++) {

                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    LatLng position = new LatLng(lat, lng);

                    points.add(position);

                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);
            }

            direction = mMap.addPolyline(polylineOptions);

        }
    }
}
