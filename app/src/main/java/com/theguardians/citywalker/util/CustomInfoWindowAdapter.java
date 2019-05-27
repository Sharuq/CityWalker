package com.theguardians.citywalker.util;


import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.theguardians.citywalker.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    Map<String, GoogleMap.InfoWindowAdapter> adapterMap;

    private Activity context;

    public CustomInfoWindowAdapter(Activity context){
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {



        HashMap<String,String> hashMap = (HashMap<String, String>) marker.getTag ();

        if(hashMap!=null) {
            if (hashMap.get ("Id").equals (marker.getId ())) {

                View view = context.getLayoutInflater ().inflate (R.layout.customwindow, null);
                TextView name = (TextView) view.findViewById (R.id.name);
                TextView address = (TextView) view.findViewById (R.id.address);
                TextView tel = (TextView) view.findViewById (R.id.tel);
                TextView time = (TextView) view.findViewById (R.id.time);
                TextView total = (TextView) view.findViewById (R.id.total);
                TextView predict_time = (TextView) view.findViewById (R.id.predict_time);
                TextView predict_total = (TextView) view.findViewById (R.id.predict_total);
                TextView busyness = (TextView) view.findViewById (R.id.busyness);
                LinearLayout layout2 = view.findViewById (R.id.lay2_1);
                LinearLayout layout3 = view.findViewById (R.id.lay3);
                LinearLayout layout4 = view.findViewById (R.id.lay4);
                LinearLayout layout5 = view.findViewById (R.id.lay5);
                LinearLayout layout6 = view.findViewById (R.id.lay6);
                //layout.setVisibility (View.GONE);
                int walkunicode = 0x1F6B6;
                int addressUnicode =0x1F303;
                int policeaddessUnicode =0x1F3DB;
                int openShop = 0x1F3EA;
                int clockUnicode = 0x1F55C;
                int phone =0x260E;
                name.setText (marker.getTitle ());
                if(hashMap.get ("this_is").equals ("police_station")){
                    tel.setText (new String (Character.toChars(phone))+ " "+hashMap.get ("tel"));
                    address.setText (new String (Character.toChars(policeaddessUnicode))+ " " +hashMap.get ("address"));
                    layout3.setVisibility (View.GONE);
                    layout4.setVisibility (View.GONE);
                    layout5.setVisibility (View.GONE);
                    layout6.setVisibility (View.GONE);
                }
                else if(hashMap.get ("this_is").equals ("open_shop")){
                    address.setText (new String (Character.toChars(openShop))+ " " +hashMap.get ("address"));
                    layout2.setVisibility (View.GONE);
                    layout3.setVisibility (View.GONE);
                    layout4.setVisibility (View.GONE);
                    layout5.setVisibility (View.GONE);
                    layout6.setVisibility (View.GONE);
                }
                else {
                    layout2.setVisibility (View.GONE);
                    address.setText (new String (Character.toChars (addressUnicode)) + " " + hashMap.get ("address"));
                    time.setText (new String (Character.toChars (clockUnicode)) + " " + hashMap.get ("time"));
                    total.setText (new String (Character.toChars (walkunicode)) + " " + hashMap.get ("total_of_directions") + " people");
                    String pt = hashMap.get ("predict_time");
                    if (pt != null) {
                        predict_time.setText (new String (Character.toChars (clockUnicode)) + " " + hashMap.get ("predict_time"));
                        predict_total.setText (new String (Character.toChars (walkunicode)) + " " + hashMap.get ("predict_total") + " people");
                        busyness.setText (hashMap.get ("busyness"));
                    } else {

                        predict_time.setText ("Not Available");
                        predict_total.setText ("Not Available");
                        busyness.setText ("Not Available");
                    }
                }
                return view;
            }
        }
        return null;
    }
}