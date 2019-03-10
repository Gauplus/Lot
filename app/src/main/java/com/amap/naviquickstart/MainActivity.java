package com.amap.naviquickstart;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
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
import com.amap.naviquickstart.adapter.InputTipsAdapter;
import com.amap.naviquickstart.overlay.PoiOverlay;
import com.amap.naviquickstart.util.Constants;
import com.amap.naviquickstart.util.ToastUtil;
import com.amap.naviquickstart.util.Utils;


import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;


public class MainActivity extends AppCompatActivity implements Inputtips.InputtipsListener,SearchView.OnQueryTextListener,View.OnClickListener,NavigationView.OnNavigationItemSelectedListener ,AMapLocationListener, PoiSearch.OnPoiSearchListener, AMap.OnInfoWindowClickListener, AMap.OnMarkerClickListener, AMap.InfoWindowAdapter {
    private AMap mMap;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private Marker mLocationMarker;
    private Circle mLocationCircle;
    private PoiOverlay poiOverlay;
    private AMapLocation mCurrentLocation;
    public static int  available;
    public static int price;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

//        initSearchView();
        mCleanKeyWords = (ImageView)findViewById(R.id.clean_keywords);
        mCleanKeyWords.setOnClickListener(this);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mKeyWords = "";
        setUpMapIfNeeded();
        initLocation();
////        CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(mLocationClient.getLastKnownLocation().getLatitude(),mLocationClient.getLastKnownLocation().getLongitude()),18,30,0));
//        LatLng centerPoint= new LatLng(mLocationClient.getLastKnownLocation().getLatitude(),mLocationClient.getLastKnownLocation().getLongitude());
//// 定义了一个配置 AMap 对象的参数类
//        AMapOptions mapOptions = new AMapOptions();
//// 设置了一个可视范围的初始化位置
//// CameraPosition 第一个参数： 目标位置的屏幕中心点经纬度坐标。
//// CameraPosition 第二个参数： 目标可视区域的缩放级别
//// CameraPosition 第三个参数： 目标可视区域的倾斜度，以角度为单位。
//// CameraPosition 第四个参数： 可视区域指向的方向，以角度为单位，从正北向顺时针方向计算，从0度到360度
//        mapOptions.camera(new CameraPosition(centerPoint, 10f, 0, 0));
//// 定义一个 MapView 对象，构造方法中传入 mapOptions 参数类
//        MapView mapView = new MapView(this, mapOptions);
//// 调用 onCreate方法 对 MapView LayoutParams 设置
//        mapView.onCreate(savedInstanceState);

    }
/********            侧边栏选项             *******/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
          Intent intent = new Intent(MainActivity.this,Login.class);
          startActivity(intent);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        destroyLocation();
    }
/************** end  *******************/

/******************* 建立地图 ***********************/
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            mMap.setOnMarkerClickListener(this);
            mMap.setOnInfoWindowClickListener(this);
            mMap.setInfoWindowAdapter(this);
        }
        mKeywordsTextView = (TextView) findViewById(R.id.main_keywords);
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
        LatLng curLatLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
        if (mLocationMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(curLatLng);
            markerOptions.anchor(0.5f, 0.5f);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.navi_map_gps_locked));
            mLocationMarker = mMap.addMarker(markerOptions);
        }
        if (mLocationCircle == null) {
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(curLatLng);
            circleOptions.radius(aMapLocation.getAccuracy());
            circleOptions.strokeWidth(2);
            circleOptions.strokeColor(getResources().getColor(R.color.stroke));
            circleOptions.fillColor(getResources().getColor(R.color.fill));
            mLocationCircle = mMap.addCircle(circleOptions);
        }

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mLocationMarker == marker) {
            return false;
        }
        if(marker.getTitle().contains("停车场"))
            sendRequestWithHttpURLConnection();
        if(price==-1||available==-1)
        {
            ToastUtil.show(MainActivity.this,
                    "网络出现问题，请重试");
        }
        else
            marker.showInfoWindow();

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

        View view = getLayoutInflater().inflate(R.layout.poikeywordsearch_uri,
                null);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) view.findViewById(R.id.snippet);
      //  Log.d(TAG, "getInfoWindow: 1111111"+poiOverlay);
        int index = poiOverlay.getPoiIndex(marker);
        float distance = poiOverlay.getDistance(index);
        //Log.d(TAG, "getInfoWindow:123456 "+distance);
        String showDistance = Utils.getFriendlyDistance((int) distance);
        snippet.setText("距当前位置" + showDistance);
       if(marker.getTitle().contains("停车场"))          //如果是停车场，则从自己数据库调取数据
       {
           TextView available = (TextView) view.findViewById(R.id.available);
           available.setText("可用车位" + this.available);

           TextView price = (TextView) view.findViewById(R.id.price);
           price.setText("价格" + this.price + "/时");
       }

        ImageButton button = (ImageButton) view
                .findViewById(R.id.start_amap_app);
        // 调起导航
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAMapNavi(marker);
            }
        });
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
//                MainActivity.this.runOnUiThread(new Runnable() {
//
//                    ParkInfomation info = ParkInfomation.getLot(mLocationMarker.getPosition());
//
//                    @Override
//                    public void run() {
//                     available = info.getAvailable();
//                     price = info.getPrice();
//                    }
//                });
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
        showProgressDialog();// 显示进度框
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
        dissmissProgressDialog();// 隐藏对话框
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
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                        Log.d(TAG, "onPoiSearched: 111112"+poiOverlay.toString());
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtil.show(MainActivity.this,
                                R.string.no_result);
                    }
                }
            } else {
                ToastUtil.show(MainActivity.this,
                        R.string.no_result);
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
    }
/********************************* end ************************************************************/
    /**
     * 点击事件回调方法
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_keywords:
                Intent intent = new Intent(this, InputTipsActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.clean_keywords:
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
}

