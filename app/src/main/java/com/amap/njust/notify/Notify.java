package com.amap.njust.notify;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amap.njust.MainActivity;
import com.amap.njust.R;

import io.socket.client.Socket;


public class Notify extends AppCompatActivity {
    private Button submit;
    private EditText message;
    private String msg;
    private Socket msocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
      submit = findViewById(R.id.messageSubmit);
      message = findViewById(R.id.notifyMessage);
      msocket = MainActivity.getmSocket();
      submit.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
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
                  msocket.emit("newMessage", msg);
                  //msocket.emit("location")

              }
//                  new Thread() {
//                  @Override
//                  public void run()
//                      {
//                          sendMessage();
//                      }
//                  }.start();
          }
      });
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

}
