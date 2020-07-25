package com.example.pickmedrivers.Common;

import android.location.Location;

import com.example.pickmedrivers.Model.Drivers;
import com.example.pickmedrivers.Notification.FCMClient;
import com.example.pickmedrivers.Notification.IFCMService;
import com.example.pickmedrivers.Retrofit.IGoogleAPI;
import com.example.pickmedrivers.Retrofit.RetrofitClient;

public class Common {

    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmbaseURL = "https://fcm.googleapis.com/";
    public static String CURRENT_TOKEN = "";
    public static Location lastLocation=null;
    public static Drivers currentDrivers;
    public static String SELECTED_CAR_TYPE;

    public static double base_fare=2.55;
    public static double time_rate=0.35;
    public static double distance_rate=1.75;

    public static double getPrice(double km, int min){

        return (base_fare+(time_rate*min)+(distance_rate*km));

    }



    public static IGoogleAPI getGoogleAPI() {

        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);


    }

    public static IFCMService getFCMService() {

        return FCMClient.getFCMClient(fcmbaseURL).create(IFCMService.class);

    }
}
