package com.amap.njust.Group;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.amap.njust.MainActivity;
import com.amap.njust.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BigGroup {

    private String TAG = "BigGroup";
    private  ArrayList<User> userResult;
    /********发起组队请求********/
    public Runnable sentGroup(final double latitude, final double longitude, final int speed, final String account) {

        Runnable runnable = new Runnable()  {
            @Override
            public void run() {

                String url = "http://47.102.149.164:30000/getNearUser";

                try {
//                    Thread.sleep(2000);
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = new FormBody.Builder()
                            .add("latitude",latitude+"")
                            .add("longitude", longitude+"")
                            .add("speed", speed+"")
                            .add("account",account+"")
                            .build();
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    if(response.isSuccessful()) {
                        String responseData = response.body().string();
                        Log.d(TAG, "run:hhhhhhhhhhhhh " + responseData);
                        userResult = parseJSONWithGSON(responseData);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.d(TAG, "run: errrrrrrrrrrrrrrrrrr");
                }
            }
        };
        return runnable;
    }
    private static ArrayList<User> parseJSONWithGSON(String jsonData) {

        Gson gson = new Gson();
        ArrayList<User> userResult = gson.fromJson(jsonData,new TypeToken< ArrayList<User>>(){}.getType());

//        for(User u : userResult){
//////            Log.d("Group", "parseJSONWithGSON: "+u.getLatitude());
//////        }
        return userResult;
    }

    public ArrayList<User> getUserResult(double latitude,double longitude,int speed,String account) throws Exception{
        Thread thread = new Thread(sentGroup(latitude,longitude,speed,account)); //新建请求线程
        thread.start();

        thread.join();//等待线程执行完毕

        return userResult;
    }
}
