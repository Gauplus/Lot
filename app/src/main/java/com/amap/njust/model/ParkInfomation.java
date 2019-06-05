package com.amap.njust.model;

import android.util.Log;

import com.amap.api.maps.model.LatLng;
import com.google.gson.Gson;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ParkInfomation {           //停车场信息
    private int available;
    private int  price;

    public int getPrice() {
        return price;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public void setPrice(int  price) {
        this.price = price;
    }

    public static ParkInfomation getLot(LatLng latLng) {
        /*
        @param 当前经纬度
         */
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
//                .add("longitude", latLng.longitude + "")
//                .add("latitude", latLng.latitude + "")  //到时改回来
                .add("parkID","park1")
                .build();
        Log.d("ParkInfomation","whatthe");
        Request request = new Request.Builder()
//                .post(body)
                .url("http://47.102.149.164:30000/getPark?parkID=park1")
                .build();
        try {

            Response response = client.newCall(request).execute();
            String responseData = response.body().string();
            Log.d("ParkInfomation",responseData+"1111111");
            ParkInfomation info =  parseJSONWithGSON(responseData);
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ParkInfomation", "getLot: erro");
            return null;
        }

    }

    private static ParkInfomation parseJSONWithGSON(String jsonData) {

        Gson gson = new Gson();
        ParkInfomation info = gson.fromJson(jsonData, ParkInfomation.class);
        return info;
    }
}
