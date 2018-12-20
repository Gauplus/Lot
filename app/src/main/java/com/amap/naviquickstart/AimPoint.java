package com.amap.naviquickstart;
//目的地
public class AimPoint {
    private String  name;  //地点名
    private String  location;//所在地区
    private double distance;
    public AimPoint(String name,String location,double distance ){

    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }
}
