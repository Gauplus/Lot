package com.amap.njust.notify;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.amap.njust.MainActivity;
import com.amap.njust.R;
import com.amap.njust.util.ToastUtil;



public class NotifyHistory extends AppCompatActivity {

    String[]  msgs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_history);
        Intent intent  = getIntent();

          msgs = intent.getStringArrayExtra("historyMsgs");
        Log.d("this", "onCreate:nnnnnnnnnnnnnn "+msgs.length);
          if(msgs.length>0){
              initListView();
          }
          else if(msgs.length==0){
              AlertDialog.Builder dialog = new AlertDialog.Builder(NotifyHistory.this);
              dialog.setTitle("温馨提示");
              dialog.setMessage("暂无收到任何通知");
              dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {
                      Intent intent1 = new Intent(NotifyHistory.this, MainActivity.class);
                      startActivity(intent1);
                  }
              });
              dialog.show();
          }

    }
    public void initListView(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(NotifyHistory.this,android.R.layout.simple_list_item_1,msgs);
        final ListView listView = findViewById(R.id.msgList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                ToastUtil.show(NotifyHistory.this,msgs[position]);
            }
        });
    }
}
