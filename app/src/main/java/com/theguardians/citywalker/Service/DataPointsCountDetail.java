package com.theguardians.citywalker.Service;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.PolyUtil;
import com.theguardians.citywalker.Model.CCTVLocation;
import com.theguardians.citywalker.Model.OpenShop;
import com.theguardians.citywalker.Model.PedestrianSensor;
import com.theguardians.citywalker.Model.PoliceStation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class DataPointsCountDetail {


    private static PoliceStation pInfo = new PoliceStation ();
    private static CCTVLocation cInfo = new CCTVLocation ();
    private static PedestrianSensor sInfo = new PedestrianSensor ();
    private static OpenShop oInfo = new OpenShop ();

    public  static JSONArray getpolylineCountDetailsArray(List<Polyline> polylines, JSONArray policeStationArray, JSONArray cctvLocationArray, JSONArray pedestrianSensorArray, JSONArray openShopArray, HashMap<String,PoliceStation> selectedPoliceStation, HashMap<String,CCTVLocation> selectedCCTVLocation, HashMap<String,PedestrianSensor> selectedPedestrianSensor, HashMap<String, OpenShop> selectedOpenShop, JSONArray polylineCountDetailsArray) {

        for (Polyline polyline : polylines) {

            int cctvCount = 0;
            int stationCount = 0;
            int pedestrianSensorCount = 0;
            int openShopCount =0;

            try {

                for (int j = 0; j < policeStationArray.length (); j++) {
                    JSONObject jsonobject = policeStationArray.getJSONObject (j);

                    String lat = jsonobject.getString ("latitude");
                    String lon = jsonobject.getString ("longitude");
                    String polSat = jsonobject.getString ("police_station");
                    String address = jsonobject.getString ("address");
                    String tel = jsonobject.getString ("tel");


                    pInfo = new PoliceStation ();
                    //System.out.println("This is ds " +ds);

                    pInfo.setLatitude (Double.parseDouble (lat));
                    pInfo.setLongitude (Double.parseDouble (lon));
                    pInfo.setPolice_station (polSat);
                    pInfo.setAddress (address);
                    pInfo.setTel (tel);


                    LatLng pt = new LatLng (pInfo.getLatitude (), pInfo.getLongitude ());

                    if (PolyUtil.isLocationOnPath (pt, polyline.getPoints (), true, 100)) {
                        // System.out.println ("Yes is Location on path station name" + pInfo.getPolice_station ());
                        // System.out.println ("pinfo" + pInfo.getAddress ());

                        stationCount = stationCount + 1;
                        selectedPoliceStation.put (pInfo.getPolice_station (), pInfo);

                    } else {
                        // System.out.println ("No is  not Location on path station name " + polSat);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace ();
            }

            try {

                for (int j = 0; j < cctvLocationArray.length (); j++) {
                    JSONObject jsonobject = cctvLocationArray.getJSONObject (j);

                    String lat = jsonobject.getString ("latitude");
                    String lon = jsonobject.getString ("longitude");
                    String cctvLocNo = jsonobject.getString ("cctv");
                    String detail = jsonobject.getString ("detail");


                    cInfo = new CCTVLocation ();
                    //System.out.println("This is ds " +ds);

                    cInfo.setLatitude (Double.parseDouble (lat));
                    cInfo.setLongitude (Double.parseDouble (lon));
                    cInfo.setCctvNo (cctvLocNo);
                    cInfo.setDetail (detail);


                    LatLng pt = new LatLng (cInfo.getLatitude (), cInfo.getLongitude ());

                    if (PolyUtil.isLocationOnPath (pt, polyline.getPoints (), true, 50)) {
                        // System.out.println ("Yes is Location on path CCTV name" + cInfo.getDetail ());
                        cctvCount = cctvCount + 1;
                        selectedCCTVLocation.put (cInfo.getCctvNo (), cInfo);

                    } else {
                        //System.out.println ("No is  not Location on path CCTV name " + cInfo.getDetail ());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace ();
            }

            try {

                for (int j = 0; j < pedestrianSensorArray.length (); j++) {
                    JSONObject jsonobject = pedestrianSensorArray.getJSONObject (j);

                    String lat = jsonobject.getString ("latitude");
                    String lon = jsonobject.getString ("longitude");
                    String sensor_id = jsonobject.getString ("sensor_id");
                    String sensor_description = jsonobject.getString ("sensor_description");


                    sInfo = new PedestrianSensor ();
                    //System.out.println("This is ds " +ds);

                    sInfo.setLatitude (Double.parseDouble (lat));
                    sInfo.setLongitude (Double.parseDouble (lon));
                    sInfo.setSensor_id (sensor_id);
                    sInfo.setSensor_description (sensor_description);


                    LatLng pt = new LatLng (sInfo.getLatitude (), sInfo.getLongitude ());

                    if (PolyUtil.isLocationOnPath (pt, polyline.getPoints (), true, 50)) {
                        // System.out.println ("Yes is Location on path CCTV name" + cInfo.getDetail ());
                        pedestrianSensorCount = pedestrianSensorCount + 1;
                        selectedPedestrianSensor.put (sInfo.getSensor_id (), sInfo);

                    } else {
                        //System.out.println ("No is  not Location on path CCTV name " + cInfo.getDetail ());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace ();
            }

            try {

                for (int j = 0; j < openShopArray.length (); j++) {
                    JSONObject jsonobject = openShopArray.getJSONObject (j);

                    String lat = jsonobject.getString ("latitude");
                    String lon = jsonobject.getString ("longitude");
                    String name = jsonobject.getString ("name");
                    String address = jsonobject.getString ("address");


                    oInfo = new OpenShop ();
                    //System.out.println("This is ds " +ds);

                    oInfo.setLatitude (Double.parseDouble (lat));
                    oInfo.setLongitude (Double.parseDouble (lon));
                    oInfo.setName (name);
                    oInfo.setAddress (address);


                    LatLng pt = new LatLng (oInfo.getLatitude (), oInfo.getLongitude ());

                    if (PolyUtil.isLocationOnPath (pt, polyline.getPoints (), true, 50)) {
                        // System.out.println ("Yes is Location on path CCTV name" + cInfo.getDetail ());
                        openShopCount = openShopCount + 1;
                        selectedOpenShop.put (oInfo.getName (), oInfo);

                    } else {
                        //System.out.println ("No is  not Location on path CCTV name " + cInfo.getDetail ());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace ();
            }
            try {

                JSONObject jsonObject = new JSONObject ();
                jsonObject.put ("polylineId", polyline.getId ());
                jsonObject.put ("selectedStationCount", stationCount);
                jsonObject.put ("selectedCCTVCount", cctvCount);
                jsonObject.put ("pedestrianSensorCount", pedestrianSensorCount);
                jsonObject.put ("openShopCount", openShopCount);

                polylineCountDetailsArray.put (jsonObject);

            } catch (JSONException e) {
                e.printStackTrace ();
            }
        }


        return polylineCountDetailsArray;
    }


    public  static HashMap<String,PoliceStation> getSelectedPoliceStation(List<Polyline> polylines,JSONArray policeStationArray,HashMap<String,PoliceStation> selectedPoliceStation) {

        for (Polyline polyline : polylines) {

            int cctvCount = 0;
            int stationCount = 0;
            try {

                for (int j = 0; j < policeStationArray.length (); j++) {
                    JSONObject jsonobject = policeStationArray.getJSONObject (j);

                    String lat = jsonobject.getString ("latitude");
                    String lon = jsonobject.getString ("longitude");
                    String polSat = jsonobject.getString ("police_station");
                    String address = jsonobject.getString ("address");
                    String tel = jsonobject.getString ("tel");


                    pInfo = new PoliceStation ();
                    //System.out.println("This is ds " +ds);

                    pInfo.setLatitude (Double.parseDouble (lat));
                    pInfo.setLongitude (Double.parseDouble (lon));
                    pInfo.setPolice_station (polSat);
                    pInfo.setAddress (address);
                    pInfo.setTel (tel);


                    LatLng pt = new LatLng (pInfo.getLatitude (), pInfo.getLongitude ());

                    if (PolyUtil.isLocationOnPath (pt, polyline.getPoints (), true, 100)) {
                        // System.out.println ("Yes is Location on path station name" + pInfo.getPolice_station ());
                        // System.out.println ("pinfo" + pInfo.getAddress ());

                        stationCount = stationCount + 1;
                        selectedPoliceStation.put (pInfo.getPolice_station (), pInfo);

                    } else {
                        // System.out.println ("No is  not Location on path station name " + polSat);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace ();
            }
        }

        return selectedPoliceStation;
    }


    public  static HashMap<String,CCTVLocation> getSelectedCCTVLocation(List<Polyline> polylines,JSONArray cctvLocationArray,HashMap<String,CCTVLocation> selectedCCTVLocation) {

        for (Polyline polyline : polylines) {

            int cctvCount = 0;

            try {

                for (int j = 0; j < cctvLocationArray.length (); j++) {
                    JSONObject jsonobject = cctvLocationArray.getJSONObject (j);

                    String lat = jsonobject.getString ("latitude");
                    String lon = jsonobject.getString ("longitude");
                    String cctvLocNo = jsonobject.getString ("cctv");
                    String detail = jsonobject.getString ("detail");


                    cInfo = new CCTVLocation ();
                    //System.out.println("This is ds " +ds);

                    cInfo.setLatitude (Double.parseDouble (lat));
                    cInfo.setLongitude (Double.parseDouble (lon));
                    cInfo.setCctvNo (cctvLocNo);
                    cInfo.setDetail (detail);


                    LatLng pt = new LatLng (cInfo.getLatitude (), cInfo.getLongitude ());

                    if (PolyUtil.isLocationOnPath (pt, polyline.getPoints (), true, 50)) {
                        // System.out.println ("Yes is Location on path CCTV name" + cInfo.getDetail ());
                        cctvCount = cctvCount + 1;
                        selectedCCTVLocation.put (cInfo.getCctvNo (), cInfo);

                    } else {
                        //System.out.println ("No is  not Location on path CCTV name " + cInfo.getDetail ());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace ();
            }

        }
        return selectedCCTVLocation;
    }


    public  static HashMap<String,PedestrianSensor> getSelectedPedestrianSensor(List<Polyline> polylines,JSONArray pedestrianSensorArray,HashMap<String,PedestrianSensor> selectedPedestrianSensor) {

        for (Polyline polyline : polylines) {

            int pedSensorCount = 0;

            try {

                for (int j = 0; j < pedestrianSensorArray.length (); j++) {
                    JSONObject jsonobject = pedestrianSensorArray.getJSONObject (j);

                    String lat = jsonobject.getString ("latitude");
                    String lon = jsonobject.getString ("longitude");
                    String sensor_id = jsonobject.getString ("sensor_id");
                    String sensor_decription = jsonobject.getString ("sensor_description");


                    sInfo = new PedestrianSensor ();
                    //System.out.println("This is ds " +ds);

                    sInfo.setLatitude (Double.parseDouble (lat));
                    sInfo.setLongitude (Double.parseDouble (lon));
                    sInfo.setSensor_id (sensor_id);
                    sInfo.setSensor_description (sensor_decription);


                    LatLng pt = new LatLng (sInfo.getLatitude (), sInfo.getLongitude ());

                    if (PolyUtil.isLocationOnPath (pt, polyline.getPoints (), true, 50)) {
                        // System.out.println ("Yes is Location on path CCTV name" + cInfo.getDetail ());
                        pedSensorCount = pedSensorCount + 1;
                        selectedPedestrianSensor.put (sInfo.getSensor_id (), sInfo);

                    } else {
                        //System.out.println ("No is  not Location on path CCTV name " + cInfo.getDetail ());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace ();
            }

        }
        return selectedPedestrianSensor;
    }

    public  static HashMap<String,OpenShop> getSelectedOpenShop(List<Polyline> polylines,JSONArray openShopArray,HashMap<String,OpenShop> selectedOpenShop) {

        for (Polyline polyline : polylines) {

            int openShopCount = 0;

            try {

                for (int j = 0; j < openShopArray.length (); j++) {
                    JSONObject jsonobject = openShopArray.getJSONObject (j);

                    String lat = jsonobject.getString ("latitude");
                    String lon = jsonobject.getString ("longitude");
                    String name = jsonobject.getString ("name");
                    String address = jsonobject.getString ("address");


                    oInfo = new OpenShop ();
                    //System.out.println("This is ds " +ds);

                    oInfo.setLatitude (Double.parseDouble (lat));
                    oInfo.setLongitude (Double.parseDouble (lon));
                    oInfo.setName (name);
                    oInfo.setAddress (address);


                    LatLng pt = new LatLng (oInfo.getLatitude (), oInfo.getLongitude ());

                    if (PolyUtil.isLocationOnPath (pt, polyline.getPoints (), true, 50)) {
                        // System.out.println ("Yes is Location on path CCTV name" + cInfo.getDetail ());
                        openShopCount = openShopCount + 1;
                        selectedOpenShop.put (oInfo.getName (), oInfo);

                    } else {
                        //System.out.println ("No is  not Location on path CCTV name " + cInfo.getDetail ());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace ();
            }

        }
        return selectedOpenShop;
    }
}
