package com.google.firebase.udacity.mysmarthome;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class DeviceAdapter extends ArrayAdapter<BTDevice> {
    public DeviceAdapter(Context context, int resource, List<BTDevice> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_device, parent, false);
        }
        TextView idTextView = (TextView) convertView.findViewById(R.id.idTextView);
        TextView nameTextView = (TextView) convertView.findViewById(R.id.deviceNameTextView);
        TextView addressTextView = (TextView) convertView.findViewById(R.id.addressTextView);
        TextView roomTextView = (TextView) convertView.findViewById(R.id.roomTextView);

        BTDevice device = getItem(position);

        //idTextView.setText(device.getId().toString());
        nameTextView.setText(device.getName());
        addressTextView.setText(device.getAddress());
        roomTextView.setText(device.getRoom());

        return convertView;
    }
}
