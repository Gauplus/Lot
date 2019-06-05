package com.amap.njust.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.amap.njust.R;
import com.amap.njust.model.User;



import java.util.ArrayList;
import java.util.List;

public class userAdapter extends ArrayAdapter<User> {
    private int resourceId;
    private List<User> userList = new ArrayList<>();

    public userAdapter(Context context,int textViewResourceId,List<User>objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;
    }
    @Override
    public View getView(int position,View converView,ViewGroup parent){
        View view;
        User user = getItem(position);

             view  = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);

        TextView username = view.findViewById(R.id.userphone);
        username.setText(user.getPhone());
        Log.d("Group", "getView: "+user.getPhone()+"0909090");
        return view;
    }
//   public void adddUser(String text){
//        userList.add(text);
//   }
//   public void deleteUser(String text){
//        if(uer)
//   }
}
