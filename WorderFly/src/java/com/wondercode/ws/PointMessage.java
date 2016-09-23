/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wondercode.ws;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 *
 * @author wondercode
 */
public class PointMessage extends Message{
    @SerializedName("points")
    private List<Integer> points;

    public List<Integer> getPoints() {
        return points;
    }

    public void setPoints(List<Integer> points) {
        this.points = points;
    }
    
}
