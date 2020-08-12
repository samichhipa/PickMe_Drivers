package com.example.pickmedrivers.Activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pickmedrivers.Common.Common;
import com.example.pickmedrivers.Model.Drivers;
import com.example.pickmedrivers.Notification.DriverTokens;
import com.example.pickmedrivers.R;
import com.example.pickmedrivers.Retrofit.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.pickmedrivers.Common.Common.lastLocation;

public class HomeNavActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback {


    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    NavigationView navigationView;
    DrawerLayout drawerLayout;

    private GoogleMap mMap;
    SwitchMaterial OnOffSwitch;

    //Presence System//
    DatabaseReference currentUserRef;
    DatabaseReference onlineRef;

    private static int MY_PERMISSION_REQ_CODE = 1000;
    private static int PLAY_SERVICES_RES_REQUEST = 10001;

    LocationRequest locationRequest;

    GoogleApiClient googleApiClient;

    Marker currentMarker;

    GeoFire geoFire;
    DatabaseReference driver_ref;

    private List<LatLng> polyLineList;
    private Marker carMarker;
    private float v;
    private double lat, lng;
    private Handler handler;
    private LatLng startPosition, endPosition, currentPosition;
    private int index, next;
    private Button btnGo;
    private PlaceAutocompleteFragment autocompleteFragment;
    private String destination;
    private String destination_lat,destination_lng;
    private PolylineOptions polylineOptions, blackPolyLineOptions;
    private Polyline blackPolyline, greyPolyLine;


    private IGoogleAPI mService;

    TextView txt_driver_name,txt_phone;

    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {

            if (index < polyLineList.size() - 1) {

                index++;
                next = index + 1;

            }
            if (index < polyLineList.size() - 1) {

                startPosition = polyLineList.get(index);
                endPosition = polyLineList.get(next);

            }

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                    lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                    LatLng newPos = new LatLng(lat, lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f, 0.5f);
                    carMarker.setRotation(getBearing(startPosition, newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(15.5f)
                                    .build()

                    ));
                }
            });

            valueAnimator.start();
            handler.postDelayed(this, 3000);
        }
    };

    private float getBearing(LatLng startPosition, LatLng endPosition) {

        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);

        if (startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        } else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude) {
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        } else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude) {
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        } else if (startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude) {

            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        } else {
            return -1;

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_nav);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        final SupportMapFragment mapFragment;
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.Open, R.string.Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(HomeNavActivity.this);
        navigationView.getMenu().getItem(0).setChecked(true);

        View headerView = navigationView.getHeaderView(0);
        txt_driver_name= headerView.findViewById(R.id.header_driver_name);
        txt_phone = headerView.findViewById(R.id.header_driver_phone);

        txt_driver_name.setText(Common.currentDrivers.getName());
        txt_phone.setText(Common.currentDrivers.getDriver_phone());




        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        OnOffSwitch = findViewById(R.id.switchBtn);

        FirebaseDatabase.getInstance().goOnline();
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef = FirebaseDatabase.getInstance().getReference().child("DriversLocation")
                .child(Common.currentDrivers.getCar_type()).child(FirebaseAuth.getInstance().getCurrentUser().getUid());


        onlineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                //we will remove value from driver table when driver disconnected.
                currentUserRef.onDisconnect().removeValue();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(HomeNavActivity.this);


        getPermission();


        mService = Common.getGoogleAPI();
        //  driver_ref = FirebaseDatabase.getInstance().getReference().child("DriversLocation").child(Common.SELECTED_CAR_TYPE);
        // geoFire = new GeoFire(driver_ref);

        // getPermission();
        //BuildGoogleApiClient();

        polyLineList = new ArrayList<>();

        Places.initialize(getApplicationContext(), "AIzaSyAiiW16B1_pzqgbhjN5MmP8ss6bedgOoh4");

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {


                destination = place.getName().toString();
                destination = destination.replace(" ", "");
                destination_lat=String.valueOf(place.getLatLng().latitude);
                destination_lng=String.valueOf(place.getLatLng().longitude);
                Toast.makeText(HomeNavActivity.this, destination, Toast.LENGTH_SHORT).show();

                getDirection();

            }

            @Override
            public void onError(@NonNull Status status) {

                Toast.makeText(HomeNavActivity.this, "" + status.getStatus().getStatusMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        OnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isOnline) {

                if (isOnline) {


                    if (ActivityCompat.checkSelfPermission(HomeNavActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeNavActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }

                    driver_ref = FirebaseDatabase.getInstance().getReference("DriversLocation").child(Common.currentDrivers.getCar_type());
                    geoFire = new GeoFire(driver_ref);

                    FirebaseDatabase.getInstance().goOnline();

                    BuildLocationCallback();
                    buildLocationRequest();
                    displayLocation();
                    mMap.setMyLocationEnabled(true);



                    Snackbar.make(mapFragment.getView(), "You are Online", Snackbar.LENGTH_SHORT).show();


                } else {

                    onlineRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {


                            //we will remove value from driver table when driver disconnected.
                            currentUserRef.onDisconnect().removeValue();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    FirebaseDatabase.getInstance().goOffline();
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    mMap.setMyLocationEnabled(false);
                   //stopLocation();
                    if (currentMarker != null) {
                        currentMarker.remove();
                    }
                    Snackbar.make(mapFragment.getView(), "You are Offline", Snackbar.LENGTH_SHORT).show();

                }

            }
        });


    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                lastLocation = location;
            }
        });


        if (lastLocation != null) {


            if (currentMarker != null) {
                currentMarker.remove();
            }

            if (OnOffSwitch.isChecked()) {


                final double lat = lastLocation.getLatitude();
                final double lng = lastLocation.getLongitude();


                //rotateMarker(currentMarker, -360, mMap);


                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {


                        if (currentMarker != null) {
                            currentMarker.remove();

                        }
                        currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("My Location")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.source_pin)));

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 13.0f));


                    }
                });


            }else{


            }
        }
        stopLocation();

    }


    private void stopLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }

    private void setUpLocation() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(HomeNavActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, MY_PERMISSION_REQ_CODE);
        } else {


            BuildLocationCallback();
            buildLocationRequest();

            if (OnOffSwitch.isChecked()) {
                displayLocation();
            }

        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_nav, menu);
        return true;
    }


    private void BuildLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {

                    lastLocation = location;
                }
                displayLocation();
            }
        };


    }


    @Override
    protected void onDestroy() {

        FirebaseDatabase.getInstance().goOffline();

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        if (handler != null) {

            handler.removeCallbacks(drawPathRunnable);
        }


        super.onDestroy();
    }

    private void buildLocationRequest() {


        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());


    }

/*
    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;

        setUpLocation();
        displayLocation();


    }


 */

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            poly.add(new LatLng((((double) lat / 1E5)), (((double) lng / 1E5))));
        }

        return poly;
    }


    /*  protected synchronized void BuildGoogleApiClient() {
          googleApiClient = new GoogleApiClient.Builder(this)
                  .addConnectionCallbacks(this)
                  .addOnConnectionFailedListener(this)
                  .addApi(LocationServices.API)
                  .build();
          googleApiClient.connect();

      }


     */
    private void getDirection() {

        currentPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference-less_driving&" +
                    "origin=" + Common.lastLocation.getLatitude() + "," + Common.lastLocation.getLongitude() + "&" +
                    "destination=" + destination_lat +","+ destination_lng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);

            Log.d("SAMI", requestApi);

            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polyLineList = decodePoly(polyline);

                        }

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng latLng : polyLineList) {

                            builder.include(latLng);

                        }
                        LatLngBounds bounds = builder.build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                        mMap.animateCamera(cameraUpdate);

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.GRAY);
                        polylineOptions.width(5);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.endCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polyLineList);
                        greyPolyLine = mMap.addPolyline(polylineOptions);

                        blackPolyLineOptions = new PolylineOptions();
                        blackPolyLineOptions.color(Color.BLACK);
                        blackPolyLineOptions.width(5);
                        blackPolyLineOptions.startCap(new SquareCap());
                        blackPolyLineOptions.endCap(new SquareCap());
                        blackPolyLineOptions.jointType(JointType.ROUND);
                        blackPolyline = mMap.addPolyline(blackPolyLineOptions);

                        mMap.addMarker(new MarkerOptions().position(polyLineList.get(polyLineList.size() - 1))
                                .title("PickUp Location"));


                        //Animation//

                        ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0, 100);
                        polyLineAnimator.setDuration(2000);
                        polyLineAnimator.setInterpolator(new LinearInterpolator());
                        polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {


                                List<LatLng> points = greyPolyLine.getPoints();
                                int percentValue = (int) valueAnimator.getAnimatedValue();
                                int size = points.size();
                                int newPoints = (int) (size * (percentValue / 100.0f));
                                List<LatLng> p = points.subList(0, newPoints);
                                blackPolyline.setPoints(p);
                            }
                        });

                        polyLineAnimator.start();
                        carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                .flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                        handler = new Handler();
                        index = -1;
                        next = 1;
                        handler.postDelayed(drawPathRunnable, 3000);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                    Toast.makeText(HomeNavActivity.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();

                }
            });

        } catch (Exception e) {


        }


    }

    private void getPermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                    , Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE}, 1000);
        }

    }


    private void rotateMarker(final Marker currentMarker, final float i, GoogleMap mMap) {

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = currentMarker.getRotation();
        final long duration = 1000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                long elpased = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elpased / duration);
                float rot = t * i + (1 - t) * startRotation;
                currentMarker.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {

                    handler.postDelayed(this, 16);
                }

            }
        }, 2000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case 1000:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    BuildLocationCallback();
                    buildLocationRequest();

                    if (OnOffSwitch.isChecked()) {

                        driver_ref = FirebaseDatabase.getInstance().getReference("DriversLocation").child(Common.currentDrivers.getCar_type());
                        geoFire = new GeoFire(driver_ref);


                        displayLocation();

                    }


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

    @Override
    public void onMapReady(GoogleMap googleMap) {


        try {

            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));

        } catch (Exception e) {
            e.printStackTrace();


        }
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        BuildLocationCallback();
        buildLocationRequest();
        displayLocation();
        // fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void setTokenId() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("DriverTokens");

        String tokenID = FirebaseInstanceId.getInstance().getToken();

        String driverid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DriverTokens token = new DriverTokens(tokenID, driverid);
        reference.child(driverid).setValue(token);
        Common.CURRENT_TOKEN = tokenID;

    }

    @Override
    protected void onStart() {
        super.onStart();

        setTokenId();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {


        } else if (id == R.id.nav_gallery) {


        } else if (id == R.id.nav_car_type) {

            UpdateCarTypeDialog();

        } else if (id == R.id.nav_slideshow) {


            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (firebaseUser != null) {


                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(HomeNavActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }


        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void UpdateCarTypeDialog() {


        final AlertDialog.Builder builder = new AlertDialog.Builder(HomeNavActivity.this);
        builder.setTitle("Update Car Type");

        final View view = LayoutInflater.from(HomeNavActivity.this).inflate(R.layout.car_type_layout, null);

        final RadioButton economical_car, business_car;

        economical_car = view.findViewById(R.id.economical_car);
        business_car = view.findViewById(R.id.business_car);

        if (Common.currentDrivers.getCar_type().equals("Economical")) {

            economical_car.setChecked(true);
        } else if (Common.currentDrivers.getCar_type().equals("Business")) {

            business_car.setChecked(true);
        }


        builder.setView(view);


        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                Map<String, Object> data = new HashMap<>();
                if (economical_car.isChecked()) {

                    data.put("car_type", "Economical");
                } else if (business_car.isChecked()) {


                    data.put("car_type", "Business");
                }

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Drivers");

                ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {


                            Toast.makeText(HomeNavActivity.this, "updated car type", Toast.LENGTH_LONG).show();


                        } else {

                            Toast.makeText(HomeNavActivity.this, task.getException().toString(), Toast.LENGTH_LONG).show();


                        }
                    }
                });

                ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Common.currentDrivers = snapshot.getValue(Drivers.class);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                dialogInterface.dismiss();


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });


        builder.show();

    }
}
