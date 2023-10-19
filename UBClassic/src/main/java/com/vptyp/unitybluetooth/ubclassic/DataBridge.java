package com.vptyp.unitybluetooth.ubclassic;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Bridge class to transport data between Unity and Android via Json
 */
public class DataBridge {
    public String command;
    public String device;
    public String name;
    public String data;

    public boolean hasError = false;
    public String errMsg;

    public DataBridge(String command) {
        this.command = command;
    }

    public void setError(String errMsg) {
        this.hasError = true;
        this.errMsg = errMsg;
    }

    public String toJson(){
        JSONObject json = new JSONObject();
        try {
            json.put("command", command);
            json.put("device", device);
            json.put("name", name);
            json.put("data", data);
            json.put("hasError", hasError);
            json.put("errMsg", errMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
