package com.example.workplan.Model;

import com.google.android.gms.maps.model.LatLng;


import java.util.List;

public class AOIModel {


    private String areaname;
    private List<LatLng> Polyganlist;
    boolean Isvisit=false;
    boolean notification_status=false;


    public AOIModel(String areaname, List<LatLng> polyganlist) {
        this.areaname = areaname;
        Polyganlist = polyganlist;
    }

    public AOIModel() {
    }

    public String getAreaname() {
        return areaname;
    }

    public void setAreaname(String areaname) {
        this.areaname = areaname;
    }

    public List<LatLng> getPolyganlist() {
        return Polyganlist;
    }

    public void setPolyganlist(List<LatLng> polyganlist) {
        Polyganlist = polyganlist;
    }

    public boolean isIsvisit() {
        return Isvisit;
    }

    public void setIsvisit(boolean isvisit) {
        Isvisit = isvisit;
    }

    public boolean isNotification_status() {
        return notification_status;
    }

    public void setNotification_status(boolean notification_status) {
        this.notification_status = notification_status;
    }

}
