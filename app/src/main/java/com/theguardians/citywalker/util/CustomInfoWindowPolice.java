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

public class CustomInfoWindowPolice implements GoogleMap.InfoWindowAdapter {

    private Activity context;

    Map<String, GoogleMap.InfoWindowAdapter> adapterMap;

    public CustomInfoWindowPolice(Activity context){
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

                View view = context.getLayoutInflater ().inflate (R.layout.customwindow, null);
                TextView name = (TextView) view.findViewById (R.id.name);
                TextView address = (TextView) view.findViewById (R.id.address);
                TextView tel = (TextView) view.findViewById (R.id.time);
                int walkunicode = 0x1F6B6;
                int addressUnicode =0x1F303;
                int clockUnicode = 0x1F55C;
                name.setText (marker.getTitle ());
                address.setText (new String (Character.toChars(addressUnicode))+ " " +hashMap.get ("address"));
                tel.setText (new String (Character.toChars(clockUnicode))+" " +hashMap.get ("tel"));

                return view;

        }
        return null;
    }
}