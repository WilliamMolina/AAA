/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wondercode.ws;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author wondercode
 */
public class Message {
    public Message(){}
    public Message(String message){
        this.message=message;
        this.type="simple";
    }
    public Message(String message,String type){
        this.message=message;
        this.type=type;
    }
    @SerializedName("message")
    private String message;
    @SerializedName("type")
    private String type;
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
}
