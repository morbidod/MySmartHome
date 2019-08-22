package com.google.firebase.udacity.mysmarthome;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TemperatureScannerActivity extends AppCompatActivity {
    final static String BLE_DEBUG_TAG = "TemperatureScannerDebug";
    final int REQUEST_ENABLE_BT = 1001;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2001;
    private static final int SCAN_PERIOD = 10000; //10sec
    private static final String TEMP_SENSOR_TAG="0c166e3531";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapater;
    private BluetoothLeScanner mBLEScanner;
    private BleScannerAdapter mBleScannerAdapter=null;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private boolean mScanning = false;
    private Handler mHandler = new Handler();

    private Button sendButton;
    private String deviceAddress;
    private Integer deviceTemperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_scanner);
        //getApplicationContext().getActionBar().setDisplayHomeAsUpEnabled(true);
        TextView blesupported_tv = (TextView) findViewById(R.id.ble_textview);
        TextView enableble_tv = (TextView) findViewById(R.id.isenabled_textview);
        boolean isBleEnabled = false;
       sendButton = (Button) findViewById(R.id.sendButton);



        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Sorry BLE is not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        blesupported_tv.setText("BLE Supported");

        // check if Bluetooth is enabled: if not ask to enable
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapater = mBluetoothManager.getAdapter();
        checkpermission();

    }

    private boolean isBtEnabled() {
        if (mBluetoothAdapater == null) {
            return false;
        } else { //the BluetoothAdapter is not null
            if (mBluetoothAdapater.isEnabled()) {
                return true;
            } else { // bluetooth is not enabled - need to ask to enable it
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }


        }
        return true;
    }

    @Override
    protected void onPause(){
        Log.d(BLE_DEBUG_TAG,"TemperatureScanner-OnPause");
        super.onPause();
        mScanning = false;
        mBleScannerAdapter.clearList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(BLE_DEBUG_TAG, "OnResume");
        if (isBtEnabled()) {
            Toast.makeText(this, "BT is enabled", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Sorry BT Adapert is null", Toast.LENGTH_SHORT).show();
            finish();

        }
        mBLEScanner = mBluetoothAdapater.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        filters = new ArrayList<ScanFilter>();
        scanLeDevice(true);
        //set the adapter
        mBleScannerAdapter=new BleScannerAdapter(this);
        ListView mListView = (ListView) findViewById(R.id.listDevices);
        mListView.setAdapter(mBleScannerAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent();

                i.putExtra("address",mBleScannerAdapter.getDevice(position).getAddress());
                i.putExtra("temperature",mBleScannerAdapter.getTemperature(position));
                setResult(Activity.RESULT_OK,i);
                finish();
            }
        });
        if (mBleScannerAdapter.getCount()>0){
            //updateButton.setEnabled(true);
            Log.d("TemperatureScannerActivity","Adapter finishing....");
            Intent i = new Intent();
            int index=0;
            i.putExtra("num_devices",mBleScannerAdapter.getCount());
            String addressString=null;
            String tempString=null;
            while (index < mBleScannerAdapter.getCount()){
                addressString+=mBleScannerAdapter.getDevice(index).getAddress() +";";
                tempString+=mBleScannerAdapter.getTemperature(index) +";";


            }
            i.putExtra("address",addressString);
            i.putExtra("temperature",tempString);
            setResult(Activity.RESULT_OK,i);
            finish();

        }
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra("address",deviceAddress);
                i.putExtra("temperature",deviceTemperature);
                setResult(Activity.RESULT_OK,i);
                finish();
            }
        });
    }

 @Override
 public void onBackPressed(){
     //super.onBackPressed();
     Toast.makeText(this,"Going Back with no data",Toast.LENGTH_SHORT).show();
     Intent i = new Intent();
     i.putExtra("address","");
     i.putExtra("temperature",0);
     setResult(Activity.RESULT_CANCELED,i);
     finish();
 }


    private void scanLeDevice(boolean enable) {
        if (enable) {
            // we need to start the scanning
            mScanning = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    mBLEScanner.stopScan(mScanCallback);
                    mScanning = false;
                    Log.d(BLE_DEBUG_TAG, "Runnable is in execution");
                }
            }, SCAN_PERIOD);
            Log.d(BLE_DEBUG_TAG, "Start Scanning for BLE devices");

            mBLEScanner.startScan(filters, settings, mScanCallback);
        } else { // enable is false
            mBLEScanner.stopScan(mScanCallback);
            mScanning = false;
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(BLE_DEBUG_TAG, "Found Device onScanResult is called");
            Log.d("BLE CallBack","Type:"+callbackType + " Result:"+result.toString());
            byte[] scanrec_bytes= result.getScanRecord().getBytes();
            String scanRecord = byteArrayToHexString(result.getScanRecord().getBytes());
            Log.d("BLE CallBack","Scan Record:"+scanRecord);
            String tempSensorString = scanRecord.substring(60,70);
            int temperature_device=0;
            int battery_level=0;

            if (tempSensorString.equals((String) TEMP_SENSOR_TAG)) {
                Log.d("BLE Scan CallBack", "Yes it's a chinese temp sensor!!!");
                String accelerometer = scanRecord.substring(71,77);
                //byte[] tempbyte= {scanrec_bytes[156],scanrec_bytes[157],scanrec_bytes[158],scanrec_bytes[159],scanrec_bytes[160],scanrec_bytes[161],scanrec_bytes[162],scanrec_bytes[163]};
                //Log.d("ScanCallBack","Temp byte array:"+tempbyte.toString());
                String temperature = scanRecord.substring(76,80);
                String temperature_hex="0x" + temperature;
                String battery= scanRecord.substring(80,82);
                deviceTemperature=Integer.decode(temperature_hex);
                deviceAddress=result.getDevice().getAddress();
                battery_level = Integer.parseInt(battery,16);

                Log.d("ScanCallBack","SensorData from Device:"+deviceAddress + " temperature:"+temperature + " temperature_hex:"+temperature_hex + " which is:"+ deviceTemperature+"degrees C Battery:"+battery);
                mBleScannerAdapter.addDevice(result.getDevice(),result.getRssi(),scanrec_bytes,deviceTemperature);
                mBleScannerAdapter.notifyDataSetChanged();


           /*     updateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "Update Button Pressed", Toast.LENGTH_LONG).show();


                        // we must start the scanning process
                    }
                });
                */
            }
        }
    };

    /* add device to the current list of devices */
    private void handleFoundDevice(final BluetoothDevice device,
                                   final int rssi,
                                   final byte[] scanRecord,
                                   final int temperature)
    {
        // adding to the UI have to happen in UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBleScannerAdapter.addDevice(device, rssi, scanRecord, temperature);
                mBleScannerAdapter.notifyDataSetChanged();
            }
        });
    }

    public static String byteArrayToHexString(byte[] array) {
        StringBuffer hexString = new StringBuffer();
        for (byte b : array) {
            int intVal = b & 0xff;
            if (intVal < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(intVal));
        }
        return hexString.toString();
    }


    private void checkpermission() {
        boolean permission_granted = false;
        // Android M Permission check 
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(TemperatureScannerActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(BLE_DEBUG_TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to scan ble");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }
}








