package com.example.pickmedrivers.Activity;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pickmedrivers.Common.Common;
import com.example.pickmedrivers.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;

public class TripDetailsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    TextView txt_total_price,txt_distance,txt_fare,txt_time,txt_start_address,txt_end_address,txt_date;

    ImageView CancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();

    CancelBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    });

    }

    private void init() {


        txt_total_price=findViewById(R.id.trip_price);
        txt_distance=findViewById(R.id.trip_distance);
        txt_fare=findViewById(R.id.trip_base_fare);
        txt_time=findViewById(R.id.trip_time);
        txt_start_address=findViewById(R.id.trip_source_address);
        txt_end_address=findViewById(R.id.trip_destination_address);
        txt_date=findViewById(R.id.trip_date);
        CancelBtn=findViewById(R.id.cancelBtn);



    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (getIntent()!=null){

            Calendar calendar=Calendar.getInstance();
            String date=String.format("%s,%d/%d",convertToDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH));
            txt_date.setText(date);

            txt_total_price.setText(String.format("$ %.2f",Double.parseDouble(getIntent().getStringExtra("price"))));
            txt_fare.setText(String.format("$ %.2f", Common.base_fare));
            txt_time.setText(String.format("%s Min",getIntent().getStringExtra("time")));
            txt_distance.setText(String.format("%s km",getIntent().getStringExtra("distance")));
            txt_start_address.setText(String.format(getIntent().getStringExtra("source_address")));
            txt_end_address.setText(String.format(getIntent().getStringExtra("destination_address")));


            String[] loc=getIntent().getStringExtra("location_end").split(",");

            LatLng latLng=new LatLng(Double.parseDouble(loc[0]),Double.parseDouble(loc[1]));

            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_pin))
                    .position(latLng)
            .title("Drop Off Location"));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,16.0f));

        }


    }

    private Object convertToDayOfWeek(int day) {
        switch (day){

            case Calendar.SUNDAY:
                return "SUNDAY";
            case Calendar.MONDAY:
                return "MONDAY";
            case Calendar.TUESDAY:
                return "TUESDAY";
            case Calendar.WEDNESDAY:
                return "WEDNESDAY";
            case Calendar.THURSDAY:
                return "THURSDAY";
            case Calendar.FRIDAY:
                return "FRIDAY";
            case Calendar.SATURDAY:
                return "SATURDAY";
            default:
                return "ERROR";




        }



    }
}
