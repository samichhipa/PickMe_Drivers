package com.example.pickmedrivers.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pickmedrivers.Common.Common;
import com.example.pickmedrivers.Notification.CustomerTokens;
import com.example.pickmedrivers.Notification.Data;
import com.example.pickmedrivers.Notification.DriverTokens;
import com.example.pickmedrivers.Notification.IFCMService;
import com.example.pickmedrivers.Notification.MyResponse;
import com.example.pickmedrivers.Notification.Sender;
import com.example.pickmedrivers.R;
import com.example.pickmedrivers.Retrofit.IGoogleAPI;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.pickmedrivers.Common.Common.lastLocation;

public class CustomerCallActivity extends AppCompatActivity {

    TextView txt_time, txt_distance, txt_address;

    MediaPlayer mediaPlayer;

    IGoogleAPI iGoogleAPI;
    IFCMService ifcmService;

    Button btnAccept, btnDecline;
    String customer_id;

    DatabaseReference reference;

    String lat,lng;

    TextView txt_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        reference = FirebaseDatabase.getInstance().getReference().child("CustomerTokens");

        iGoogleAPI = Common.getGoogleAPI();
        ifcmService = Common.getFCMService();
        init();

        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                DeclineRide(customer_id);

            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(CustomerCallActivity.this,DriverTrackingActivity.class);

                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("customer_id",customer_id);

                startActivity(intent);
                finish();

            }
        });


    }

    private void DeclineRide(final String customer_id) {

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {


                    CustomerTokens tokens = snap.getValue(CustomerTokens.class);

                    if (tokens.getCustomer_id().equals(customer_id)) {

                        String customer_token=tokens.getToken_id();

                       Data data=new Data("Cancel","Driver Cancel Your Booking...");

                       Sender sender=new Sender(customer_token,data);
                        ifcmService.sendMessage(sender).enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                if (response.body().success==1){

                                    Toast.makeText(CustomerCallActivity.this,"Cancelled",Toast.LENGTH_SHORT).show();
                                finish();
                                }else{
                                    Toast.makeText(CustomerCallActivity.this,response.errorBody().toString(),Toast.LENGTH_SHORT).show();


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

    private void init() {

        txt_address = findViewById(R.id.txt_address);
        txt_distance = findViewById(R.id.txt_distance);
        txt_time = findViewById(R.id.txt_time);
        btnAccept = findViewById(R.id.acceptBtn);
        btnDecline = findViewById(R.id.declineBtn);
        txt_count=findViewById(R.id.timer_count);
        mediaPlayer = MediaPlayer.create(CustomerCallActivity.this, R.raw.calling);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();


        if (getIntent() != null) {

           lat = getIntent().getStringExtra("lat");
           lng = getIntent().getStringExtra("lng");
            customer_id = getIntent().getStringExtra("customer");

            getDirection(lat, lng);


        }

        startTimer();
    }

    private void startTimer() {

        CountDownTimer countDownTimer=new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long l) {

                txt_count.setText(String.valueOf(l/1000));

            }

            @Override
            public void onFinish() {
                if (!TextUtils.isEmpty(customer_id))
                {
                    DeclineRide(customer_id);

                }else{

                    Toast.makeText(CustomerCallActivity.this,"Customer Id is Null",Toast.LENGTH_LONG).show();


                }
            }
        }.start();


    }

    private void getDirection(String lat, String lng) {

        // currentPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference-less_driving&" +
                    "origin=" + lastLocation.getLatitude() + "," + lastLocation.getLongitude() + "&" +
                    "destination=" + lat + "," + lng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);

            Log.d("SAMI", requestApi);

            iGoogleAPI.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");

                        //after get routes, just get first element of routes

                        JSONObject object = routes.getJSONObject(0);

                        //after get first element, we need get array with name "legs"

                        JSONArray legs = object.getJSONArray("legs");

                        //after get first element of legs array
                        JSONObject legObject = legs.getJSONObject(0);

                        //Now, get Distance
                        JSONObject distance = legObject.getJSONObject("distance");
                        txt_distance.setText(distance.getString("text"));

                        //Now, get Time
                        JSONObject time = legObject.getJSONObject("duration");
                        txt_time.setText(time.getString("text"));

                        //Now, get address
                        String address = legObject.getString("end_address");
                        txt_address.setText(address);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                    Toast.makeText(CustomerCallActivity.this, t.getMessage().toString(), Toast.LENGTH_SHORT).show();

                }
            });

        } catch (Exception e) {


        }


    }

    @Override
    protected void onStop() {

        mediaPlayer.release();
        super.onStop();

    }

    @Override
    protected void onResume() {
        mediaPlayer.start();
        super.onResume();


    }

    @Override
    protected void onPause() {

        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }

        super.onPause();
    }
}
