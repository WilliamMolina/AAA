/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wondercode.city;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author wdario.molina
 */
public class City {
    @SerializedName("name")
    private String name;
    @SerializedName("img")
    private String img;

    public City(String name, String img) {
        this.name = name;
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
    
}
