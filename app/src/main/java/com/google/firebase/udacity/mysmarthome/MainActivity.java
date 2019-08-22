/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.udacity.mysmarthome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.udacity.mysmarthome.BTDevice;
import com.google.firebase.udacity.mysmarthome.DeviceAdapter;
import com.google.firebase.udacity.mysmarthome.FriendlyMessage;
import com.google.firebase.udacity.mysmarthome.MessageAdapter;
import com.google.firebase.udacity.mysmarthome.R;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    private static final String LOG_TAG="MainActivity Log";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final int RC_SIGN_IN =1002;
    public static final int RC_TEMP_READING=1003;

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private DeviceAdapter mDeviceAdapter;
    private TemperatureAdapter mTempAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private List<BTDevice> listDevices;
    private List<TemperatureReading> listTemperatures;

    private String mUsername;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mUserDBReference;
    private DatabaseReference mTempDBReference;
    private DatabaseReference mMessageDBReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private FirebaseUser mFirebaseUser=null;
    private User mUser;
    private Query mTempQuery;

    private ChildEventListener mChildEventListener;
    private ChildEventListener mTempChildEventListener;
    private ValueEventListener mValueEventListener;
    private Boolean userHasWriteAccess=false;


    private Menu mMenu=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsername = ANONYMOUS;

        //intantiate mFirebase and mDatabaaseREference
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("btdevices");
        mMessageDBReference = mFirebaseDatabase.getReference().child("messages");
        mUserDBReference=mFirebaseDatabase.getReference().child("users");
        mTempDBReference=mFirebaseDatabase.getReference().child("tempdb/treading");



       // mUser=new User();

        //initialize mFirebaseAuth
        mFirebaseAuth=FirebaseAuth.getInstance();

        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
       // List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        //List<BTDevice> list_btdevices= new ArrayList<>();
        listDevices= new ArrayList<>();
        listTemperatures= new ArrayList<>();
        mDeviceAdapter = new DeviceAdapter(this, R.layout.item_device, listDevices);
        mTempAdapter = new TemperatureAdapter(this,R.layout.item_temperature,listTemperatures);

        //mMessageListView.setAdapter(mDeviceAdapter);
        mMessageListView.setAdapter(mTempAdapter);
        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click
                FriendlyMessage fm = new FriendlyMessage(mMessageEditText.getText().toString(),mUsername,null);
                mMessageDBReference.push().setValue(fm);
                //BTDevice mBTDevice = new BTDevice(4,"Device4","AA:HH:LL",mMessageEditText.getText().toString());
                //mDatabaseReference.push().setValue(mBTDevice);
                //mUserDBReference.push().setValue(mUser);


                // Clear input box
                mMessageEditText.setText("");
            }
        });



        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
               final FirebaseUser user = firebaseAuth.getCurrentUser();
                //mFirebaseUser = firebaseAuth.getCurrentUser();
                if (user == null){
                    //Need to Sign in
                    onSignOutCleanUp();
                    Toast.makeText(getApplicationContext(),"Need to Sign in",Toast.LENGTH_SHORT).show();
                    Intent intentSignIn= AuthUI.getInstance().createSignInIntentBuilder().
                            setProviders(AuthUI.EMAIL_PROVIDER,AuthUI.GOOGLE_PROVIDER).build();
                    startActivityForResult(intentSignIn,RC_SIGN_IN);

                }
                if (user !=null){
                    // user is authenticated - need to iniitialize
                    Toast.makeText(getApplicationContext(),"Welcome "+user.getDisplayName(),Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG,"User is not null!!!!");
                    userHasWriteAccess=false;
                   mUserDBReference.addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot dataSnapshot) {
                           Log.d(LOG_TAG,"onDataChange for User");
                           for (DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                               User user_from_db = userSnapshot.getValue(User.class);
                               Log.d(LOG_TAG,"user_from_db:"+user_from_db.getName() +" uid:" + user_from_db.getUid() +" user authenticated:"+user.getDisplayName()+" uid:"+user.getUid());
                               if (user.getUid().equals(user_from_db.getUid())){
                                   //the user authenticated is present on the db
                                   // check if isWriter
                                   Log.d(LOG_TAG,"This user is the authenticated");
                                   userHasWriteAccess=user_from_db.getIsWriter();
                                   Log.d(LOG_TAG,"And has Write Acces:"+userHasWriteAccess);
                               }
                               Logging(user_from_db);
                           }
                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {

                       }
                   });


                    Log.d(LOG_TAG,"user:"+user.getDisplayName()+ " has WriteAccess:"+userHasWriteAccess);

                    onSignInInitialize(user.getDisplayName());


                }

            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==RC_SIGN_IN){
            // it was a SIGN IN activity
            if (resultCode==RESULT_OK){
                Toast.makeText(getApplicationContext(),"Sign In success",Toast.LENGTH_LONG).show();
            }
            else if (resultCode==RESULT_CANCELED){
                Toast.makeText(getApplicationContext(),"Sign In was not successfull",Toast.LENGTH_LONG).show();
                finish();
            }
        }
        else if (requestCode==RC_TEMP_READING){
            if (resultCode==RESULT_OK){


            String receivedAddress=data.getStringExtra("address");
            Integer receivedTemp=data.getIntExtra("temperature",0);
            //Toast.makeText(this,"Back From Temp:"+receivedAddress +" "+receivedTemp,Toast.LENGTH_SHORT).show();

                // Remove the items in the database that are older than 1h
                Long cutoff = new Date().getTime()- TimeUnit.MILLISECONDS.convert(1,TimeUnit.HOURS);
            mTempQuery=mTempDBReference.orderByChild("timestamp").endAt(cutoff);
                    mTempQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot itemSnapshot: snapshot.getChildren()) {
                        itemSnapshot.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    throw databaseError.toException();
                }
            });
            TemperatureReading tr = new TemperatureReading(receivedTemp,receivedAddress,System.currentTimeMillis(),"room");
            mTempDBReference.push().setValue(tr);
            Log.d(LOG_TAG,"Got Data from Reader:"+receivedAddress+" temp:"+receivedTemp);
            int idDevice=GetDeviceIDfromAddress(receivedAddress);
            if (idDevice>0){
                Log.d(LOG_TAG,"ID Device:"+idDevice);
            }
            } //end if RESULT_OK
        } //end if requestCode RC_TEMP
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mMenu=menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
            case R.id.reader:
                Intent scannerIntent = new Intent(this,TemperatureScannerActivity.class);
                startActivityForResult(scannerIntent,RC_TEMP_READING);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        Log.d(LOG_TAG,"OnResume PrintDevices");
        PrintDevices();
    }

    @Override
    public void onPause(){
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        Log.d(LOG_TAG,"onPause");
        PrintDevices();
        detachDatabaseListener();
        mDeviceAdapter.clear();
        mTempAdapter.clear();
        listTemperatures.clear();
    }

    private void onSignInInitialize(String username){
        mUsername=username;
        attachDatabaseListener();
        attachTempDatabaseListener();


    }

    private void onSignOutCleanUp(){
        //On Sign Out: clear the username, clear the list and detach the db listener
        //Toast.makeText(getApplicationContext(),"Good Bye",Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG,"OnSignOutCleanUp");

        mUsername = "ANONYMOUS";
        mDeviceAdapter.clear();
        mTempAdapter.clear();
        listTemperatures.clear();


    }
    private void attachUserListener(){
        if (mValueEventListener == null){
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //read data
                    User user = dataSnapshot.getValue(User.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }
        mUserDBReference.addValueEventListener(mValueEventListener);
    }
    private void attachDatabaseListener(){
        if (mChildEventListener == null){
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                BTDevice mBTDevice = dataSnapshot.getValue(BTDevice.class);
                mDeviceAdapter.add(mBTDevice);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }
    }

    private void attachTempDatabaseListener(){
        if (mTempChildEventListener == null){
            mTempChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    TemperatureReading mTR = dataSnapshot.getValue(TemperatureReading.class);
                    //add to the list of temperatures
                    // check if the timestamp
                    mTempAdapter.add(mTR);
                    mTempAdapter.notifyDataSetChanged();
                   // listTemperatures.add(mTR);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mTempDBReference.addChildEventListener(mTempChildEventListener);
        }

    }


    private void detachDatabaseListener(){
       if (mChildEventListener != null){
           mDatabaseReference.removeEventListener(mChildEventListener);
           mChildEventListener=null;
       }
        if (mTempChildEventListener != null){
            mTempDBReference.removeEventListener(mTempChildEventListener);
            mTempChildEventListener=null;
        }
   }

    private void Logging (User user){
        Log.d(LOG_TAG,"User:"+user.getName()+ " "+user.getUid()+ " isWriter:"+user.getIsWriter());
    }

    private void PrintDevices(){
        int i=0;
        Log.d(LOG_TAG,"Devices DB");
        while (i<listDevices.size()) {
            Log.d(LOG_TAG,"Device " + i + " Address:" + listDevices.get(i).getAddress() + " Room:" + listDevices.get(i).getRoom());
            i++;
        }
        i=0;
        Log.d(LOG_TAG,"TEMP DB");
        while (i<listTemperatures.size()) {
            Log.d(LOG_TAG,"Record " + i + " Address:" + listTemperatures.get(i).getAddress() + listTemperatures.get(i).getRoom()+ " " + listTemperatures.get(i).getTemperature()+ " Temp:"+listTemperatures.get(i).getTimestamp());
            i++;
        }
    }

    private int GetDeviceIDfromAddress(String address){
        Log.d(LOG_TAG,"GetDeviceIDfromAddress"+" listDeviceSize:"+listDevices.size());
        int id=0; // not found
        int i=0;
        while (i<listDevices.size()|| (id>0)) {
            Log.d(LOG_TAG,"address received:"+address+ "address in db pos "+i+":"+listDevices.get(i).getAddress());
            if(address.equals(listDevices.get(i).getAddress())){
                id=listDevices.get(i).getId();
            }
            i++;
        }
        return id;

    }

    private String GetRoomfromAddress(String address){
        Log.d(LOG_TAG,"GetRoomfromAddress"+" listDeviceSize:"+listDevices.size());
        Boolean roomFound=false; // not found
        String room_from_address=null;
        int i=0;
        int num_devices=listDevices.size();
           if (num_devices>0){
            while (i<num_devices && !roomFound) {
            Log.d(LOG_TAG,"address received:"+address+ " address in db pos "+i+":"+listDevices.get(i).getAddress());
            if(address.equals(listDevices.get(i).getAddress())){
                Log.d(LOG_TAG,"Match Found");
                roomFound=true;
                room_from_address=listDevices.get(i).getRoom();
                }
             i++;
            }
           }

        return room_from_address;

    }







}
