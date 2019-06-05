package com.amap.njust.model;

import com.amap.api.location.AMapLocation;

import java.io.Serializable;

public class User implements Serializable{
    String teamID = null; //用户组队id
    String account; //用户账号
    int speed; //用户速度
    double latitude;  //
    double longitude;
    public void setPhone(String phone) {
        this.account = phone;
    }
    public User(String gid,String phone,int speed){
        this.teamID = gid;
        this.account = phone;
        this.speed = speed;
    }
    public User(){}
    public String getGid() {
        return teamID;
    }

    public void setGid(String gid) {
        this.teamID = gid;
    }

    public String getTeamID() {
        return teamID;
    }

    public void setTeamID(String teamID) {
        this.teamID = teamID;
    }

    public String getPhone() {
        return account;
    }


    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getAccount() {
        return account;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getSpeed() {
        return speed;
    }
    public static double GetDistance(double lat1, double lng1, double lat2, double lng2)
    {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 1000);
        return s;
    }
    private static double EARTH_RADIUS = 6371.393;
    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }

}
