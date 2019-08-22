package com.google.firebase.udacity.mysmarthome;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TemperatureAdapter extends ArrayAdapter<TemperatureReading> {
    public TemperatureAdapter(Context context, int resource, List<TemperatureReading> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_temperature, parent, false);
        }
        TextView roomTextView = (TextView) convertView.findViewById(R.id.roomTextView);
        TextView temperatureTextView = (TextView) convertView.findViewById(R.id.temperatureTextView);
        TextView timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);

        TemperatureReading mtemp = getItem(position);
        Date date = new Date(mtemp.getTimestamp()) ;;
        SimpleDateFormat dateFormat = new SimpleDateFormat("E dd  HH.mm");
        String dateString= dateFormat.format(date);

        //idTextView.setText(device.getId().toString());
        roomTextView.setText(mtemp.getRoom());
        temperatureTextView.setText(mtemp.getTemperature()+"C   ");
        timeTextView.setText(dateString);

        return convertView;
    }
}
