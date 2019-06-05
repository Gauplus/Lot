package com.amap.njust.Group;
/*
   组队界面，用于获取组队的每个人的信息
 */
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amap.njust.MainActivity;
import com.amap.njust.R;

import com.amap.njust.adapter.userAdapter;
import com.amap.njust.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Group extends AppCompatActivity {
    final String TAG = "Group";
    private List<User> userlist = new ArrayList<>();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
   private  ArrayList<User> userResult;
    private userAdapter adapter;
    private ProgressBar pb ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        adapter = new userAdapter(Group.this,R.layout.user_item,userlist);
        initStartButton();
        initAddButton();
        ListView listView = findViewById(R.id.userlist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String user = userlist.get(position);
//                PopupMenu popupMenu = new PopupMenu(Group.this,view);
//                MenuInflater inflater = popupMenu.getMenuInflater();
//                inflater.inflate(R.menu.group,popupMenu.getMenu());
//                popupMenu.setOnMenuItemClickListener(this);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.group,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.delete:
                /*todo*/
                break;
            case R.id.cancel:
                /*todo*/
                break;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }
    /*************init start group **********/
    public void initStartButton(){
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               pb = findViewById(R.id.groupPB);
               pb.setVisibility(View.VISIBLE);
               sentGroup();
            }
        });
    }
    /*************初始化增加按钮**************/
    public void initAddButton(){
        Button addButton = findViewById(R.id.addUser);
        final EditText newuser = findViewById(R.id.auser);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(newuser.getText().toString().equals(""))
                {
                    Toast.makeText(Group.this,"请输入队友手机号",Toast.LENGTH_SHORT ).show();
                }
                else{
                     userlist.add(new User("",newuser.getText().toString(),0));
//                    userlist.add(newuser.getText().toString());
                      adapter.notifyDataSetChanged();
                      newuser.setText("");

//                    Toast.makeText(Group.this,newuser.getText().toString(),Toast.LENGTH_SHORT ).show();
//                    for(User  u:userlist){
//                        Log.d(TAG, "onClick: "+u.getPhone());
//                    }
                }
            }
        });
    }
    /********发起组队请求********/
    public void sentGroup() {

        new Thread() {
            @Override
            public void run() {

                String url = "http://47.102.149.164:30000/buildTeam";
                JSONObject json = new JSONObject();

                ArrayList<String> users = new ArrayList<>();
                for(User u :userlist)
                {
                    users.add(u.getPhone());
                }
                try{
                    json.put("members",users);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                try {
                    Log.d(TAG, "run: "+json);
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    if(response.isSuccessful()){
                      //  Log.d(TAG, "run: "+response.body().string()+"22222222222222");
                       String  teamID = response.body().string();
                        OkHttpClient client1 = new OkHttpClient();
                        Request request1 = new Request.Builder()
                                .url("http://47.102.149.164:30000/getUserInf?teamID="+teamID)
                                .get()
                                .build();

                        Response response1 = client1.newCall(request1).execute();

                        if(response1.isSuccessful()){
                            String responseData = response1.body().string();
                            Log.d(TAG, "run:hhhhhhhhhhhhh "+responseData);
                            userResult = parseJSONWithGSON(responseData);
                            Log.d(TAG, "run:hhhhhhhhhhhhh "+userResult.get(0).getLatitude()+"tid:"+userResult.get(0).getTeamID());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                   pb.setVisibility(View.GONE);
                                   Intent intent = new Intent(Group.this, MainActivity.class);
//                                   Bundle bundle = new Bundle();
//                                   bundle.putBoolean("Group",true);
//                                   bundle.putExtra("userResults", userResult);
//                                   intent.putExtras(bundle);

                                   intent.putExtra("Group",true);
                                   intent.putExtra("userResults",(Serializable)userResult);
//                                    final Intent intent1 = intent.putExtra("userResult", (Serializable) userResult);
                                    startActivity(intent);
                                }
                            });

                        }
                        
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    Log.d(TAG, "run: errrrrrrrrrrrrrrrrrr");
                }
            }
        }.start();
    }
    private static ArrayList<User> parseJSONWithGSON(String jsonData) {

        Gson gson = new Gson();
       ArrayList<User> userResult = gson.fromJson(jsonData,new TypeToken< ArrayList<User>>(){}.getType());
        for(User u : userResult){
            Log.d("Group", "parseJSONWithGSON: "+u.getLatitude());
        }
       return userResult;
    }
}

