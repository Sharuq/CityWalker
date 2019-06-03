package com.theguardians.citywalker.Network;
/**
 * This class is utilised as an interface for rest framework api
 * @Author Sharuq
 * @Version 2.1
 */

import com.theguardians.citywalker.Model.PedestrianCount;
import com.theguardians.citywalker.Model.PedestrianSensor;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PedestrianSensorAPI {

    String BASE_URL = "http://pedestriancountwebapi.herokuapp.com/";

    @GET("combinedpedestriancount/{id}")
    Call <PedestrianCount> getPedestrianCount(@Path("id") String sensor_id);

    @GET("pedestriancountdetailscombined/")
    Call <List<PedestrianCount>> getPedestrianCount();
}
