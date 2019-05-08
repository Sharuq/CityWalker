package com.theguardians.citywalker.Service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theguardians.citywalker.Model.CCTVLocation;
import com.theguardians.citywalker.Model.OpenShop;
import com.theguardians.citywalker.Model.PedestrianSensor;
import com.theguardians.citywalker.Model.PoliceStation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataFromFirebase {
    //add Firebase Database stuff

    private PoliceStation pInfo = new PoliceStation ();
    private CCTVLocation cInfo = new CCTVLocation ();
    private PedestrianSensor sInfo = new PedestrianSensor ();
    private OpenShop oInfo = new OpenShop ();

    private JSONArray policeStationArray = new JSONArray ();
    private JSONArray cctvLocationArray = new JSONArray ();
    private JSONArray pedestrianSensorArray = new JSONArray ();
    private JSONArray openShopArray = new JSONArray ();

   // private DatabaseReference policeStationRef;
   // private DatabaseReference cctvRef;



    public JSONArray getPoliceStationArray(DatabaseReference stationReference) {

        stationReference.addValueEventListener (new ValueEventListener () {

            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren ()) {

                    //System.out.println("This is ds " +ds);
                    pInfo = new PoliceStation ();
                    pInfo.setLatitude (Double.parseDouble (ds.child ("latitude").getValue ().toString ()));
                    pInfo.setLongitude (Double.parseDouble (ds.child ("longitude").getValue ().toString ()));
                    pInfo.setPolice_station (ds.child ("police_station").getValue ().toString ());
                    pInfo.setAddress (ds.child ("address").getValue ().toString ());
                    pInfo.setTel (ds.child ("tel").getValue ().toString ());


                    try {

                        JSONObject jsonObject = new JSONObject ();
                        jsonObject.put ("police_station", pInfo.getPolice_station ());
                        jsonObject.put ("latitude", pInfo.getLatitude ());
                        jsonObject.put ("longitude", pInfo.getLongitude ());
                        jsonObject.put ("address", pInfo.getAddress ());
                        jsonObject.put ("tel", pInfo.getTel ());

                        policeStationArray.put (jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace ();
                    }

                }

                //Log.d (TAG, "$$$$ Array: " + policeStationArray);
            }

            @Override

            public void onCancelled(DatabaseError databaseError) {

            }

        });
        return policeStationArray;
    }


    public JSONArray getCctvLocationArray(DatabaseReference cctvReference) {
        cctvReference.addValueEventListener (new ValueEventListener () {

            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot ds : dataSnapshot.getChildren ()) {


                    //System.out.println("This is cctv ds " +ds);
                    cInfo.setLatitude (Double.parseDouble (ds.child ("latitude").getValue ().toString ()));
                    cInfo.setLongitude (Double.parseDouble (ds.child ("longitude").getValue ().toString ()));
                    cInfo.setCctvNo (ds.child ("cctv").getValue ().toString ());
                    cInfo.setDetail (ds.child ("detail").getValue ().toString ());
                    //display all the information


                    try {

                        JSONObject jsonObject = new JSONObject ();
                        jsonObject.put ("cctv", cInfo.getCctvNo ());
                        jsonObject.put ("detail", cInfo.getDetail ());
                        jsonObject.put ("latitude", cInfo.getLatitude ());
                        jsonObject.put ("longitude", cInfo.getLongitude ());

                        cctvLocationArray.put (jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace ();
                    }

                }

                //Log.d (TAG, "$$$$ Array: " + cctvLocationArray);
            }


            @Override

            public void onCancelled(DatabaseError databaseError) {

            }

        });
        return cctvLocationArray;
    }

    public JSONArray getPedestrianSensorArray(DatabaseReference pedtrianSensorReference) {
        pedtrianSensorReference.addValueEventListener (new ValueEventListener () {

            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot ds : dataSnapshot.getChildren ()) {


                    //System.out.println("This is cctv ds " +ds);
                    sInfo.setLatitude (Double.parseDouble (ds.child ("latitude").getValue ().toString ()));
                    sInfo.setLongitude (Double.parseDouble (ds.child ("longitude").getValue ().toString ()));
                    sInfo.setSensor_id (ds.child ("sensor_id").getValue ().toString ());
                    sInfo.setSensor_description (ds.child ("sensor_description").getValue ().toString ());
                    //display all the information


                    try {

                        JSONObject jsonObject = new JSONObject ();
                        jsonObject.put ("sensor_id", sInfo.getSensor_id ());
                        jsonObject.put ("sensor_description", sInfo.getSensor_description ());
                        jsonObject.put ("latitude", sInfo.getLatitude ());
                        jsonObject.put ("longitude", sInfo.getLongitude ());

                        pedestrianSensorArray.put (jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace ();
                    }

                }

                //Log.d (TAG, "$$$$ Array: " + cctvLocationArray);
            }


            @Override

            public void onCancelled(DatabaseError databaseError) {

            }

        });
        return pedestrianSensorArray;
    }


    public JSONArray getOpenShopArray(DatabaseReference openShopReference) {
        openShopReference.addValueEventListener (new ValueEventListener () {

            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot ds : dataSnapshot.getChildren ()) {


                    //System.out.println("This is cctv ds " +ds);
                    oInfo.setLatitude (Double.parseDouble (ds.child ("latitude").getValue ().toString ()));
                    oInfo.setLongitude (Double.parseDouble (ds.child ("longitude").getValue ().toString ()));
                    oInfo.setName (ds.child ("name").getValue ().toString ());
                    oInfo.setAddress (ds.child ("address").getValue ().toString ());
                    //display all the information


                    try {

                        JSONObject jsonObject = new JSONObject ();
                        jsonObject.put ("name", oInfo.getName ());
                        jsonObject.put ("address", oInfo.getAddress ());
                        jsonObject.put ("latitude", oInfo.getLatitude ());
                        jsonObject.put ("longitude", oInfo.getLongitude ());

                        openShopArray.put (jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace ();
                    }

                }

                //Log.d (TAG, "$$$$ Array: " + cctvLocationArray);
            }


            @Override

            public void onCancelled(DatabaseError databaseError) {

            }

        });
        return openShopArray;
    }
}
