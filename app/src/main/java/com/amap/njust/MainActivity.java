package com.amap.njust;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;


import com.amap.api.maps.CameraUpdate;

import com.amap.api.maps.Projection;

import com.amap.api.maps.model.CameraPosition;
import com.amap.njust.Group.BigGroup;
import com.amap.njust.notify.NotifyHistory;
import com.amap.njust.util.DistanceUtil;
import com.amap.njust.util.TTSUtil;

import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;

import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import com.amap.njust.LoginandSignin.LoginOrSignin;
import com.amap.njust.adapter.InputTipsAdapter;
import com.amap.njust.model.ParkInfomation;
import com.amap.njust.model.User;
import com.amap.njust.notify.Notify;
import com.amap.njust.overlay.PoiOverlay;
import com.amap.njust.util.Constants;
import com.amap.njust.util.ToastUtil;
import com.amap.njust.util.WakeUpUtil;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;


import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URISyntaxException;


import java.util.ArrayList;
import java.util.List;



import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;





public class MainActivity extends AppCompatActivity implements Inputtips.InputtipsListener,SearchView.OnQueryTextListener,View.OnClickListener,NavigationView.OnNavigationItemSelectedListener ,AMapLocationListener, PoiSearch.OnPoiSearchListener, AMap.OnInfoWindowClickListener, AMap.OnMarkerClickListener, AMap.InfoWindowAdapter {
    private AMap mMap;
    private int  loginstate = 0;  //0代表未登陆
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private Marker mLocationMarker;
    private Circle mLocationCircle;
    private PoiOverlay poiOverlay;
    private static AMapLocation mCurrentLocation;
    public static int  available;
    public static int price;
    private int LoginState = 0;
    private String TAG = "MainActivity";
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private String mKeyWords = "";// 要输入的poi搜索关键字
    private ProgressDialog progDialog = null;// 搜索时进度条
    private Marker mPoiMarker;
    private ImageView mCleanKeyWords;
    private PoiResult poiResult; // poi返回的结果
    private TextView mKeywordsTextView;
    private int currentPage = 1;
    public static final int REQUEST_CODE = 100;
    public static final int RESULT_CODE_INPUTTIPS = 101;
    public static final int RESULT_CODE_KEYWORDS = 102;
    private InputTipsAdapter mIntipAdapter;
    private List<Tip> mCurrentTipList;//test
    private WakeUpUtil wakeup;//语音唤醒
   private static Context context;
    private Handler loginHandler ;
    private ArrayList<User> userResult;
   private FloatingActionButton floatingActionButton;

    private ArrayList<String> historyMsgs = new ArrayList<>() ;//存储历史通知
    private  static int initTime=0; //地图中心点初始化的次数
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(com.amap.njust.R.layout.activity_main);
        context = getApplicationContext();
//        initSearchView();
//        TTSUtil.getInstance().init();
        mCleanKeyWords = (ImageView)findViewById(com.amap.njust.R.id.clean_keywords);
        mCleanKeyWords.setOnClickListener(this);
        NavigationView navigationView = (NavigationView) findViewById(com.amap.njust.R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mKeyWords = "";
        setUpMapIfNeeded();

        initLocation();
        mMap.showIndoorMap(true);
//        initLogin();
//        initMyaccount();
        /**************/
        if(mSocket!=null){}
        else
            initSocket();
      //  addNewMarker();
        /*************/
        floatingActionButton = findViewById(R.id.notifyButton);
        initNotifyButton();
        SpeechUtility.createUtility(context, SpeechConstant.APPID +"=5cfb6988");

/************************************根据缩放级别显示用户*************************************/
        mMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {      //地图滑动监听
                if(cameraPosition.zoom<17){
                    clearUserMarkers();
                }
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                float getZoomB = 17;
                if(cameraPosition.zoom>=getZoomB)
                {
                    try{
                        addGroupMarker();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
        });
        /*******************************语音唤醒****************/
        wakeup = new WakeUpUtil(getContext()) {
            @Override
            public void wakeUp() {
                Toast.makeText(MainActivity.this, "唤醒成功", Toast.LENGTH_SHORT).show();
                // 开启唤醒
                wakeup.wake();
            }
        };

    }
/***************************************end***************************************************/

    public static Context getContext() {
        return context;
    }

    private void clearUserMarkers() {  //移除用户标记
        //获取地图上所有Marker
        List<Marker> mapScreenMarkers = mMap.getMapScreenMarkers();
        for (int i = 0; i < mapScreenMarkers.size(); i++) {
            Marker marker = mapScreenMarkers.get(i);
            if (marker.getObject() instanceof User) {
                marker.remove();//移除当前Marker
            }
        }

    }

/*************************悬浮语音按钮初始化******************************************/
public void initNotifyButton(){
    floatingActionButton.setOnClickListener(new View.OnClickListener() {
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

               sendResult(recognizerResult);

            }

            @Override

            public void onError(SpeechError speechError) {

            }

        });

        dialog.show();

        Toast.makeText(this, "请开始说话", Toast.LENGTH_SHORT).show();

    }
    private void  sendResult(RecognizerResult results) {

       Intent intent = new Intent(MainActivity.this,Notify.class);
        String voiceMsg = parseIatResult(results.getResultString());
       intent.putExtra("voiceMsg",voiceMsg);
       intent.putExtra("Latitude",mCurrentLocation.getLatitude());
       intent.putExtra("Longitude",mCurrentLocation.getLongitude());
       startActivity(intent);

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
    /**************悬浮按钮语音输入结束************/
    /**************通知功能的socket******************************/
  private static Socket mSocket;

  public static Socket getmSocket(){
      return mSocket;
  }
  private void initSocket(){
              try {
                  mSocket = IO.socket("http://47.102.149.164:30000");
                  mSocket.on("notify",notifyMessage);
                  mSocket.connect();
              } catch (URISyntaxException e) {}
  }
/*****************显示通知*********************/
    private void showMessage(String msg){

        TTSUtil.getInstance().speak(msg);   //语音
        Toast toast = Toast.makeText(this, "上部", Toast.LENGTH_LONG);
        Display display = getWindowManager().getDefaultDisplay();
        int height = display.getHeight();
        toast.setGravity(Gravity.TOP, 0, height / 6);
       toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
       historyMsgs.add(msg);

    }
    private Emitter.Listener notifyMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          String[] msgs ;
                          double latitude=0;
                          double longitude=0;
                          try {
                              msgs= args[0].toString().split("`");//将字符串切割 1.通知消息 2.精度 3.纬度

                              latitude= Double.parseDouble(msgs[1]);
                              longitude= Double.parseDouble(msgs[2]);
                              if(DistanceUtil.getDistance(longitude,latitude,mCurrentLocation.getLongitude(),mCurrentLocation.getLatitude())<3)
                                       showMessage(msgs[0]);


                          } catch (Exception e) {
                              e.printStackTrace();

                          }
                      }
                  });




        }
    };
//    /************自动登陆***********/
//    public void initLogin() {   //自动读取本地账号信息进行登陆
//        SharedPreferences pre = getSharedPreferences("data", MODE_PRIVATE);
//        String username = pre.getString("username", null);
//        String password = pre.getString("password", null);//获取上次登陆的账号密码，如果首次登陆默认为空
//        if (username == null || password == null) {
//
//        } else {
//            sendToLogin();
//            while (LoginState != 99) {  //线程发起请求尚未返回时阻塞
//                try {
//
//
//                    break;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                if(LoginState==1){
//                    initMyaccount();
//                }
//                else {
//                    verifyErro(LoginState);
//                }
//            }
//        }
//    }
//    public boolean verify(){
//        SharedPreferences pre = getSharedPreferences("data",MODE_PRIVATE);
//        String username = pre.getString("username",null);
//        String password = pre.getString("password",null);//获取上次登陆的账号密码，如果首次登陆默认为空
//            OkHttpClient client = new OkHttpClient();
//            RequestBody body = new FormBody.Builder()
//                    .add("username", username )
//                    .add("password", password)
//                    .build();
//            Request request = new Request.Builder()
//                    .post(body)
//                    .url("http://47.102.149.164:30000/login")
//                    .build();
//            Log.d(TAG, "verify: 444444444444444444444");
//            try {
//
//                Response response = client.newCall(request).execute();
//                String responseData = response.body().string();
//
//                if(responseData.equals("1")) {
//
//                    return true;
//                }
//                else
//                    return false;
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//
//        }
//    public void verifyErro(int flag){   //账号密码错误的时候出现提示
//        if(flag==1) {
//
//        }
//        else
//        {
//            Toast.makeText(MainActivity.this,"登陆失败，如要使用更多功能，请前往登陆",Toast.LENGTH_SHORT).show();
//        }
//
//    }
//    public void sendToLogin(){
//
//        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    boolean v = verify();
//                    Log.d(TAG, "run: "+v);
//                    if(v==true){
//                        LoginState =1;
//                    }
//                    else LoginState = 0;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    LoginState = 0;
//                }
//            }
//        }.start();
//
//    }
    /************自动登陆结束*********************/
    /************ 登陆时初始化侧边栏的用户信息************/
//    public void initMyaccount(){
//        try {
//            SharedPreferences pre = getSharedPreferences("data",MODE_PRIVATE);
//            final String username = pre.getString("username","");
//
//            LayoutInflater inflater = this.getLayoutInflater();                             //先获取当前布局的填充器
//            View view = inflater.inflate(com.amap.njust.R.layout.nav_header_main, null);   //通过填充器获取另外一个布局的对象
//            final TextView myName = view.findViewById(com.amap.njust.R.id.myName);
//           loginHandler = new Handler() {
//                public void handleMessage(Message msg) {
//                        myName.setText(username);
//                }
//            };
//
//            Log.d(TAG, "initMyaccount:1111111111111111111111111111 "+username);
//        }catch (Exception e){
//            e.printStackTrace();
//            Log.d(TAG, "initMyaccount: hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhffffff");
//        }
//    }
//    class loginThread extends Thread{
//        public void run() {
//            Message message = new Message();
//            loginHandler.sendMessage(message);
//        }
//    }
/********            侧边栏选项             *******/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == com.amap.njust.R.id.nav_camera) {
          Intent intent = new Intent(MainActivity.this,LoginOrSignin.class);
          startActivity(intent);
        } else if (id == com.amap.njust.R.id.nav_gallery) {
//            Toast.makeText(MainActivity.this,"敬请期待",Toast.LENGTH_SHORT).show();
            SharedPreferences pre = getSharedPreferences("data",MODE_PRIVATE);
            String username = pre.getString("username",null);
            String password = pre.getString("password",null);//获取上次登陆的账号密码，如果首次登陆默认为空
            if(username==null||password==null){//如果没有登陆就跳转到登陆
                Intent login = new Intent(MainActivity.this,LoginOrSignin.class);
                startActivity(login);
            }else{  //否则直接进入到组队界面
//                Intent group = new Intent(MainActivity.this,Group.class);
//                startActivity(group);
            }
        } else if (id == com.amap.njust.R.id.nav_share) {
//             Intent notify = new Intent(MainActivity.this,Notify.class);
//             notify.putExtra("Latitude",mCurrentLocation.getLatitude());
//             notify.putExtra("Longitude",mCurrentLocation.getLongitude());
//            Log.d(TAG, "onNavigationItemSelected: " +mCurrentLocation.getLongitude());
//             startActivity(notify);
        } else if (id == com.amap.njust.R.id.nav_send) {
            /**
             * 本次打开收到的历史通知
             */
            Intent notifyHistory = new Intent(MainActivity.this, NotifyHistory.class);
            if(historyMsgs!=null) {
                String[] msgs = new String[historyMsgs.size()];
                int i;
                for(i=0;i<historyMsgs.size();i++)
                {
                    msgs[i] = historyMsgs.get(i)+"";
                }
                notifyHistory.putExtra("historyMsgs", msgs);
            }
            startActivity(notifyHistory);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(com.amap.njust.R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
/*********      end        *********/

/************* 生命周期*************/
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
      //  mSocket.disconnect();// mSocket.off("notify",notifyMessage);
        initTime=0;
        destroyLocation();
    }
/************** end  *******************/
/****************新增组队标记点**********************/
//private void clearUserMarkers() {
//    /**
//     * describe 删除上次的用户标记点
//     */
//    //获取地图上所有Marker
//    List<Marker> mapScreenMarkers = mMap.getMapScreenMarkers();
//    for (int i = 0; i < mapScreenMarkers.size(); i++) {
//        Marker marker = mapScreenMarkers.get(i);
//        if (marker.getObject() instanceof User) {
//            marker.remove();//移除当前Marker
//        }
//    }
//
//}
private ArrayList<Marker> lastMarkers = new ArrayList<>(); //用于保存上次的marker
public void addGroupMarker(){
/**
 * describe 生成周围的其他用户的标记
 */
    BigGroup group = new BigGroup();
    try {
        if(lastMarkers!=null){          //将上次的标记点清除
              for(Marker marker :lastMarkers)
              {

                  marker.remove();
              }
        }
//        clearUserMarkers();

        ArrayList<User> userResults = group.getUserResult(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 10, "15651982927");

        ArrayList<LatLng> latLngs = new ArrayList<>();
        MarkerOptions options = new MarkerOptions();
        if (userResults != null) {
            for (User user : userResults) {
//                for(Marker marker)
                latLngs.add(new LatLng(user.getLatitude(), user.getLongitude()));
                Log.d(TAG, "addNewMarker: " + user.getLatitude());
                options.position(new LatLng(user.getLatitude(), user.getLongitude()));
                options.title(user.getAccount()).snippet(user.getSpeed() + "");
//            options.
                Marker marker = mMap.addMarker(options);
                marker.setObject(user);
                lastMarkers.add(marker);
//                Animation markerAnimation = new ScaleAnimation(0, 1, 0, 1); //初始化生长效果动画
//                markerAnimation.setDuration(1000);  //设置动画时间 单位毫秒
//                marker.setAnimation(markerAnimation);
//                marker.startAnimation();
                 mMap.addMarker(new MarkerOptions().title(user.getAccount()).snippet(user.getSpeed() + ""));
//                TimeUnit.SECONDS.sleep(2);
                userResult = userResults;
            }

        }
    }catch (Exception e)
    {
        e.printStackTrace();
        ToastUtil.show(this,"服务器异常");
    }
}
//小组队，暂时废弃
//public void addNewMarker(){
//    Intent intent = getIntent();
//        if(intent.getBooleanExtra("Group",true)){
//             userResult = (ArrayList<User>)intent.getSerializableExtra("userResults");
//            Log.d(TAG, "addNewMarker: group1"+(ArrayList<User>)intent.getSerializableExtra("userResults"));
//        }
//    ArrayList<LatLng> latLngs = new ArrayList<>();
//    MarkerOptions options = new MarkerOptions();
//
//    if(userResult!=null) {
//        for (User user : userResult) {
//            latLngs.add(new LatLng(user.getLatitude(), user.getLongitude()));
//            Log.d(TAG, "addNewMarker: " + user.getLatitude());
//            options.position(new LatLng(user.getLatitude(),user.getLongitude()));
//            options.title(user.getAccount()).snippet(user.getSpeed()+"");
////            options.
//            Marker marker = mMap.addMarker(options);
//
//            Animation markerAnimation = new ScaleAnimation(0, 1, 0, 1); //初始化生长效果动画
//            markerAnimation.setDuration(1000);  //设置动画时间 单位毫秒
//            marker.setAnimation(markerAnimation);
//            marker.startAnimation();
//        }
////        for(LatLng latlng:latLngs)
////            mMap.addMarker(new MarkerOptions().title(user.getAccount()).snippet(user.getSpeed() + ""));
////    }
//    }
//    else{
//        Log.d(TAG, "addNewMarker: 空的");
//    }
//}
/***************end*************************************/
/******************* 建立地图 ***********************/
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(com.amap.njust.R.id.map)).getMap();
            mMap.setOnMarkerClickListener(this);
            mMap.setOnInfoWindowClickListener(this);
            mMap.setInfoWindowAdapter(this);
        }
        mKeywordsTextView = (TextView) findViewById(com.amap.njust.R.id.main_keywords);
        mKeywordsTextView.setOnClickListener(this);

    }

    private void destroyLocation() {
        if (mLocationClient != null) {
            mLocationClient.unRegisterLocationListener(this);
            mLocationClient.onDestroy();
        }
    }


    /**
     * 初始化定位
     */
    private void initLocation() {
        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocation(true);
        mLocationClient = new AMapLocationClient(this.getApplicationContext());
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();

    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

        if (aMapLocation == null || aMapLocation.getErrorCode() != AMapLocation.LOCATION_SUCCESS) {
            Toast.makeText(this, aMapLocation.getErrorInfo() + "  " + aMapLocation.getErrorCode(), Toast.LENGTH_LONG).show();
            return;
        }
        mCurrentLocation = aMapLocation;
        if(initTime==0) {        //如果是还未进行地图中心初始化
            CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 18, 30, 0));
            mMap.moveCamera(mCameraUpdate);
            initTime++;
        }
        LatLng curLatLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
        if (mLocationMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(curLatLng);
            markerOptions.anchor(0.5f, 0.5f);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(com.amap.njust.R.mipmap.navi_map_gps_locked));
            mLocationMarker = mMap.addMarker(markerOptions);
        }
        if (mLocationCircle == null) {
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(curLatLng);
            circleOptions.radius(aMapLocation.getAccuracy());
            circleOptions.strokeWidth(2);
            circleOptions.strokeColor(getResources().getColor(com.amap.njust.R.color.stroke));
            circleOptions.fillColor(getResources().getColor(com.amap.njust.R.color.fill));
            mLocationCircle = mMap.addCircle(circleOptions);
        }

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
          marker.hideInfoWindow();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mLocationMarker == marker) {
            return false;
        }
//        if(marker.getTitle().length()==11){
//
//        }
        if(marker.getTitle().contains("停车场"))

            sendRequestWithHttpURLConnection();
        if(price==-1||available==-1)
        {
            ToastUtil.show(MainActivity.this,
                    "网络出现问题，请重试");
        }
        else  {
//            if(available>10){
//                marker.set
//            }
            if(marker.isInfoWindowShown()){
                marker.hideInfoWindow();
            }else
                   marker.showInfoWindow();
        }
        return false;
    }

    /**
     * 自定义marker点击弹窗内容
     *
     * @param marker
     * @return
     */

    /******************** 点击标记点时显示的窗口信息 ************************/
    @Override
    public View getInfoWindow(final Marker marker) {

        View view = getLayoutInflater().inflate(com.amap.njust.R.layout.poikeywordsearch_uri,
                null);

        TextView title = (TextView) view.findViewById(com.amap.njust.R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) view.findViewById(com.amap.njust.R.id.snippet);

        if(marker.getTitle().length()==11){
            String speed ="";
            float distance = 0;

            for(User user:userResult)

            {
                if(user.getAccount().equals(marker.getTitle())){
                    speed += user.getSpeed();
                    distance = AMapUtils.calculateLineDistance(new LatLng(user.getLatitude(),user.getLongitude()),new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()));
                }
            }
            snippet.setText("当前车速" +speed);
            TextView available = (TextView) view.findViewById(com.amap.njust.R.id.available);

            available.setText("距离您" +distance+"米");
        }
        else {

            int distance =(int) AMapUtils.calculateLineDistance(new LatLng(marker.getPosition().latitude,marker.getPosition().longitude),new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()));

           // String showDistance = Utils.getFriendlyDistance((int) distance);
            snippet.setText("距当前位置" + distance+"米");
            if (marker.getTitle().contains("停车场"))          //如果是停车场，则从自己数据库调取数据
            {

                TextView available = (TextView) view.findViewById(com.amap.njust.R.id.available);
                available.setText("可用车位" + this.available);
                BitmapDescriptor bitmapDescriptor;
//                if(this.available<5){
//                    bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.marker_red));
//                }
//                else if(this.available>5&&this.available<10){
//                    bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.marker_yellow));
//                }
//                else {
//                    bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.marker_green));
//                }
//                marker.setIcon(bitmapDescriptor);
                TextView price = (TextView) view.findViewById(com.amap.njust.R.id.price);
                price.setText("价格" + this.price + "/时");
            }


            ImageButton button = (ImageButton) view
                    .findViewById(com.amap.njust.R.id.start_amap_app);
            // 调起导航
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startAMapNavi(marker);
//                Intent intent = new Intent(MainActivity.this,intimeNavi.class);
//                intent.putExtra("gps", false);
//                intent.putExtra("start", new NaviLatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
//                intent.putExtra("end", new NaviLatLng(marker.getPosition().latitude, marker.getPosition().longitude));
//                startActivity(intent);
                }
            });
        }
        return view;
    }
/******************************** end *************************************/

    /**  模拟导航
     * 点击一键导航按钮跳转到导航页面
     *
     * @param marker
     */
    private void startAMapNavi(Marker marker) {
        if (mCurrentLocation == null) {
            return;
        }
        Intent intent = new Intent(this, RouteNaviActivity.class);
        intent.putExtra("gps", false);
        intent.putExtra("start", new NaviLatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        intent.putExtra("end", new NaviLatLng(marker.getPosition().latitude, marker.getPosition().longitude));
        startActivity(intent);
    }
/************************************ emd *********************************************/
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

/************************ 从数据库获取停车场的相关信息 ************************************/
    private void sendRequestWithHttpURLConnection() {
        //开启线程来发起网络请求
        new Thread() {
            @Override
            public void run() {
                try {
                    ParkInfomation info = ParkInfomation.getLot(mLocationMarker.getPosition());

                available = info.getAvailable();
                price = info.getPrice();
                }catch (Exception e){
                    available = -1;
                    price=-1 ;

                }
            }
        }.start();
    }
    /*************************************** end *************************************************/

    /************************************ 显示进度条 *********************************************************/
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在搜索:\n" + mKeyWords);
        progDialog.show();
    }
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }
    /******************************** end *************************************************************/
    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery(String keywords) {
//        showProgressDialog();// 显示进度框
        currentPage = 1;
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query = new PoiSearch.Query(keywords, "", mLocationClient.getLastKnownLocation().getCity());
        // 设置每页最多返回多少条poiitem
        query.setPageSize(10);
        // 设置查第一页
        query.setPageNum(currentPage);

        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    /*********************************************************************************************/
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(MainActivity.this, infomation);

    }

    /*********************************************************************************************/

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
//        dissmissProgressDialog();// 隐藏对话框
        if (rCode == 1000) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        mMap.clear();// 清理之前的图标
                        poiOverlay = new PoiOverlay(mMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap ();
                        poiOverlay.zoomToSpan();
                        Log.d(TAG, "onPoiSearched: 111112"+poiOverlay.toString());
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtil.show(MainActivity.this,
                                com.amap.njust.R.string.no_result);
                    }
                }
            } else {
                ToastUtil.show(MainActivity.this,
                        com.amap.njust.R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }

    }

    /*************************************** 通过此方法与上面方法对结果进行解析和处理 *****************************************************/


    /*********************************************************************************************/

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_CODE_INPUTTIPS && data
                    != null) {
                mMap.clear();
                Tip tip = data.getParcelableExtra(Constants.EXTRA_TIP);
               // Log.d(TAG, "onActivityResult: "+tip.toString());
                if (tip.getPoiID() == null || tip.getPoiID().equals("")) {
                    doSearchQuery(tip.getName());
                } else {
                    addTipMarker(tip);
                }
                mKeywordsTextView.setText(tip.getName());
                if(!tip.getName().equals("")){
                    mCleanKeyWords.setVisibility(View.VISIBLE);
                }
            } else if (resultCode == RESULT_CODE_KEYWORDS && data != null) {
                mMap.clear();
                String keywords = data.getStringExtra(Constants.KEY_WORDS_NAME);
                if(keywords != null && !keywords.equals("")){
                    doSearchQuery(keywords);
                }
                mKeywordsTextView.setText(keywords);
                if(!keywords.equals("")){
                    mCleanKeyWords.setVisibility(View.VISIBLE);
                }
            }
        }

    /********************************** 将搜索到的结果在地图上标记 ***********************************************************/
    private void addTipMarker(Tip tip) {
        if (tip == null) {
            return;
        }
        mPoiMarker = mMap.addMarker(new MarkerOptions());
        LatLonPoint point = tip.getPoint();
        if (point != null) {
            LatLng markerPosition = new LatLng(point.getLatitude(), point.getLongitude());
            mPoiMarker.setPosition(markerPosition);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 17));
        }
        mPoiMarker.setTitle(tip.getName());
        mPoiMarker.setSnippet(tip.getAddress());
        if(mPoiMarker.getTitle().contains("停车场"))
        {

        }
    }
/********************************* end ************************************************************/
    /**
     * 点击事件回调方法
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case com.amap.njust.R.id.main_keywords:
                Intent intent = new Intent(this, InputTipsActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case com.amap.njust.R.id.clean_keywords:
                mKeywordsTextView.setText("");
                mMap.clear();
                mCleanKeyWords.setVisibility(View.GONE);
            default:
                break;
        }
    }

    @Override
    public boolean onQueryTextChange(String newText){
        if (TextUtils.isEmpty(newText)) {
            // Clear the text filter.
//            listView1.clearTextFilter();
            InputtipsQuery inputquery = new InputtipsQuery(newText, Constants.DEFAULT_CITY);
            Inputtips inputTips = new Inputtips(MainActivity.this.getApplicationContext(), inputquery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
        } else {
            // Sets the initial value for the text filter.
//            listView1.setFilterText(newText.toString());
            if (mIntipAdapter != null && mCurrentTipList != null) {
                mCurrentTipList.clear();
                mIntipAdapter.notifyDataSetChanged();
            }
        }
        return false;
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        // TODO Auto-generated method stub
        return false;
    }
    /**
     * 输入提示回调
     *
     * @param tipList
     * @param rCode
     */
    @Override
    public void onGetInputtips(List<Tip> tipList, int rCode) {
        Log.d(TAG,"haha");
        if (rCode == 1000) {// 正确返回
            mCurrentTipList = tipList;
            List<String> listString = new ArrayList<String>();
            for (int i = 0; i < tipList.size(); i++) {
                listString.add(tipList.get(i).getName());

            }
            mIntipAdapter = new InputTipsAdapter(
                    getApplicationContext(),
                    mCurrentTipList);
//            listView1.setAdapter(mIntipAdapter);
            mIntipAdapter.notifyDataSetChanged();
        } else {
            ToastUtil.showerror(this, rCode);
        }

    }
    public LatLng getMapCenterPoint() {          //获取屏幕中心点经纬度坐标
       View mMap1 = findViewById(com.amap.njust.R.id.map);
        int left = mMap1.getLeft();
        int top = mMap1.getTop();
        int right = mMap1.getRight();
        int bottom = mMap1.getBottom();
        // 获得屏幕点击的位置
        int x = (int) (mMap1.getX() + (right - left) / 2);
        int y = (int) (mMap1.getY() + (bottom - top) / 2);
        Projection projection = mMap.getProjection();
        LatLng pt = projection.fromScreenLocation(new Point(x, y));

        return pt;
    }
}

