package com.amap.njust.LoginandSignin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amap.njust.MainActivity;
import com.amap.njust.R;

import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginOrSignin extends AppCompatActivity {
    EditText account;
    EditText password;
    String phone;
    String pwd;
    final static String TAG = "LoginOrSignin";
    int flag = 99;//用于表示验证是否成功 成功1，失败0，网络错误4
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_signin);
        final Button loginButton   =  findViewById(R.id.btn_login);
         account = findViewById(R.id.account);
         password = findViewById(R.id.password);
         Intent intent = getIntent();
         account.setText(intent.getStringExtra("phone"));
        Log.d(TAG, "onCreate: 111111111111111111");
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 2");

                ProgressDialog progressDialog = new ProgressDialog(LoginOrSignin.this);
                progressDialog.setTitle("正在登陆请稍等...");
                progressDialog.setMessage("登陆中");
                progressDialog.show();
                sendToVerify();
                while(flag ==99){  //线程发起请求尚未返回时阻塞
                    try{
                       TimeUnit.SECONDS.sleep( 5);

                        break;
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    }

                Log.d(TAG, "onClick: 5555555555555555555555flag"+flag);
               if(flag==1){
                   progressDialog.dismiss();

                   SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();  //保存当前账号密码
                   editor.putString("username",account.getText().toString());
                   editor.putString("password",password.getText().toString());
                   editor.apply();
                   Intent intent1 = new Intent(LoginOrSignin.this, MainActivity.class);
                   intent1.putExtra("username",account.getText().toString());
                   startActivity(intent1);
               }
               else {
                   progressDialog.dismiss();
                   verifyErro(flag);
               }

            }
        });
    }
     public boolean verify(String username1,String password1){
        phone = account.getText().toString();
        pwd = password.getText().toString();
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("username", username1 )
                .add("password", password1)
                .build();
        Request request = new Request.Builder()
                .post(body)
                .url("http://47.102.149.164:30000/login")
                .build();
        Log.d(TAG, "verify: 444444444444444444444");
        try {

            Response response = client.newCall(request).execute();
            String responseData = response.body().string();
            Log.d(TAG, "verify: 555555555555555555"+responseData);
            if(responseData.equals("1")) {
                Log.d(TAG, "verify: 9999999999999999"+true);
                return true;
            }
            else
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("ParkInfomation", "getLot: erro6666666666");
            return false;
        }

    }
    public void verifyErro(int flag){   //账号密码错误的时候出现提示


        AlertDialog.Builder dialog = new AlertDialog.Builder(LoginOrSignin.this);
        if(flag==0) {
            dialog.setMessage("账号或密码错误");
        }
        else
        {
            dialog.setMessage("网络问题，请重试");
        }
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                account.setText(phone);
                password.setText("");
//                Intent intent = new Intent(LoginOrSignin.this, LoginOrSignin.class);
//                intent.putExtra("phone",phone);
//                startActivity(intent);
            }
        });
       dialog.show();
    }
    public ProgressDialog.Builder showProgressDialog(){
        ProgressDialog.Builder progressDialog = new ProgressDialog.Builder(LoginOrSignin.this);
        progressDialog.setTitle("正在登陆请稍等...");
        progressDialog.setMessage("登陆中");
        progressDialog.show();
        return progressDialog;
    }
    public void sendToVerify(){
        new Thread() {
            @Override
            public void run() {
                try {
                    boolean v = verify(phone,pwd);
                    Log.d(TAG, "run: "+v);
                    if(v==true){
                        flag =1;
                    }
                    else flag = 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    flag = 4;
                }
            }
        }.start();

        }
}
