package com.theguardians.citywalker.util;


import android.app.Activity;
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

                name.setText (marker.getTitle ());
                address.setText (hashMap.get ("address"));
                time.setText (hashMap.get ("time"));
                total.setText (hashMap.get ("total_of_directions"));


                return view;
            }
        }
        return null;
    }
}