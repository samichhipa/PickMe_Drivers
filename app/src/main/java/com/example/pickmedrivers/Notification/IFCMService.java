package com.example.pickmedrivers.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAATggyeSY:APA91bFavkvHnAr-fXZjm5DKm136EO8h-HWKVOWyXA0-o3VMD5jtXGAupJFdhM2Lqi_9Pdcd9t9fq7IaO_6pLeIm7MSl85yE0DGafQbcziONupPHX1NfyKKe6QwPxIvfWa2if_Jj6do3"


    })

    @POST("fcm/send")
    Call<MyResponse> sendMessage(@Body Sender body);
}
