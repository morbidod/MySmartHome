package com.google.firebase.udacity.mysmarthome;

/**
 * Created by XP011224 on 19/10/2017.
 */

public class BleScanner {
    private String device_name;
    private String address;
    private int rssi;
    private int temperature;

    public BleScanner() {

    }

    public BleScanner(String device_name, String address, int rssi, int temperature){
        this.device_name=device_name;
        this.address=address;
        this.rssi=rssi;
        this.temperature=temperature;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }
}
