package com.google.firebase.udacity.mysmarthome;

/**
 * Created by XP011224 on 23/10/2017.
 */

public class TemperatureReading {
    private int temperature;
    private String address;
    private Long timestamp;
    private String room;

   public TemperatureReading(){};

    public TemperatureReading(int temperature, String address, Long timestamp, String room){
        this.temperature=temperature;
        this.address=address;
        this.timestamp=timestamp;
        this.room=room;
    }

    public void AddRoom(String room){
        this.room=room;
    }

    public int getTemperature() {
        return temperature;
    }

    public String getAddress() {
        return address;
    }

    public String getRoom(){
        return room;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
