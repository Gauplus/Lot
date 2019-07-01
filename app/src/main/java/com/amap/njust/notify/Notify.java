package com.amap.njust.notify;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.njust.MainActivity;
import com.amap.njust.R;

import com.amap.njust.util.ToastUtil;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;

import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


import java.util.HashMap;
import java.util.LinkedHashMap;

import io.socket.client.Socket;


public class Notify extends AppCompatActivity {
    private Button submit;
    private Button voiceInput;
    private EditText message;
    private String msg;
    private Socket msocket;
   private String TAG = "Notify";
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String , String>();
    private double latitude;
    private double longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        StringBuffer param = new StringBuffer();//语音唤醒
        //获取主页面传过来的经纬度
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("Latitude",0);
        longitude = intent.getDoubleExtra("Longitude",0);
        String voiceMsgmsg = intent.getStringExtra("voiceMsg");  //获取主界面悬浮按钮的语音信息
      submit = findViewById(R.id.messageSubmit);
      message = findViewById(R.id.notifyMessage);
      message.append(voiceMsgmsg);
      msocket = MainActivity.getmSocket();
      voiceInput = findViewById(R.id.voideInput);

        submit.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
//              ToastUtil.show(Notify.this,message.getText().toString());
              msg = message.getText().toString();
              if(msg.length()<=1)
              {
                  final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Notify.this);

                  alertDialog.setTitle("内容为空");
                  alertDialog.setMessage("请输入您的消息");
                  alertDialog.setCancelable(false);
                  alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          dialog.dismiss();
                      }
                  });
                  alertDialog.show();
              }else {
                  msocket.emit("newMessage", msg+"`"+latitude+"`"+longitude);
                  //msocket.emit("location")
//                  ToastUtil.show(Notify.this,message.getText().toString()+"1");

              }
          }
      });

        voiceInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnVoice();

            }

        });
    }
    public  void btnVoice() {
        RecognizerDialog dialog = new RecognizerDialog(this,null);

        dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");

        dialog.setParameter(SpeechConstant.ACCENT, "mandarin");

        dialog.setListener(new RecognizerDialogListener() {

            @Override

            public void onResult(RecognizerResult recognizerResult, boolean b) {

                printResult(recognizerResult);

            }

            @Override

            public void onError(SpeechError speechError) {

            }

        });

        dialog.show();

        Toast.makeText(this, "请开始说话", Toast.LENGTH_SHORT).show();

    }

    //回调结果：

    private void  printResult(RecognizerResult results) {

        String text = parseIatResult(results.getResultString());

        // 自动填写地址

        message.append(text);

    }

    public static String parseIatResult(String json) {

        StringBuffer ret = new StringBuffer();

        try {

            JSONTokener tokener = new JSONTokener(json);

            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");

            for (int i = 0; i < words.length(); i++) {

                // 转写结果词，默认使用第一个结果

                JSONArray items = words.getJSONObject(i).getJSONArray("cw");

                JSONObject obj = items.getJSONObject(0);

                ret.append(obj.getString("w"));

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return ret.toString();

    }
}



//    public void sendMessage(){
//                 msocket.emit("newMessage",msg);
//                OkHttpClient okHttpClient = new OkHttpClient();
//
//                RequestBody body = new FormBody.Builder()
//                        .add("message",msg )
//                        .build();
//                //创建一个请求对象
//                Request request = new Request.Builder()
//                        .url("http://192.168.0.102:8080/TestProject/JsonServlet")
//                        .post(body)
//                        .build();
//                //发送请求获取响应
//            try {
//
//                Response response = okHttpClient.newCall(request).execute();
//                String responseData = response.body().string();
//            }catch (IOException e){
//                e.printStackTrace();
//            }

//        }


