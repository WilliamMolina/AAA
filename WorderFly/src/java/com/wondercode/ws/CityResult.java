/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wondercode.ws;

import com.google.gson.annotations.SerializedName;
import com.wondercode.city.City;
import java.util.List;

/**
 *
 * @author wondercode
 */
public class CityResult extends Message{ 
    @SerializedName("cities")
    private List<City> cities;
    
    public List<City> getCities() {
        return cities;
    }

    public void setCities(List<City> cities) {
        this.cities = cities;
    }    
    
}
