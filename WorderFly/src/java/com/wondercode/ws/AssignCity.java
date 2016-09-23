/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wondercode.ws;

import com.google.gson.annotations.SerializedName;
import com.wondercode.city.City;

/**
 *
 * @author wondercode
 */
public class AssignCity extends Message{
    @SerializedName("city")
    private City city;

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }
    
}
