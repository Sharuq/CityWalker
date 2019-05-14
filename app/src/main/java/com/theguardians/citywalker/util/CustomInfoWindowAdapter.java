package com.theguardians.citywalker.util;


import android.app.Activity;
import android.graphics.Color;
import android.view.View;
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
                TextView time = (TextView) view.findViewById (R.id.time);
                TextView total = (TextView) view.findViewById (R.id.total);
                TextView predict_time = (TextView) view.findViewById (R.id.predict_time);
                TextView predict_total = (TextView) view.findViewById (R.id.predict_total);

                name.setText (marker.getTitle ());
                address.setText (hashMap.get ("address"));
                time.setText (hashMap.get ("time"));
                total.setText (hashMap.get ("total_of_directions")+" people");
                String pt = hashMap.get ("predict_time");
                if(pt!=null) {
                    predict_time.setText (hashMap.get ("predict_time"));
                    predict_total.setText (hashMap.get ("predict_total") + " people");

                }
                else {

                    predict_time.setText ("Not Available");
                    predict_total.setText ("Not Available");
                }
                return view;
            }
        }
        return null;
    }
}