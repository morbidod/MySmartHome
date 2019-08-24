package com.google.firebase.udacity.mysmarthome;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    public static final int RC_SIGN_IN =1002;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mTempdbReference;
    private FirebaseAuth mFirebaseAuth;
    private static final String LOG_TAG="LoginDEBUG";
    private boolean user_at_home=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseDatabase=FirebaseDatabase.getInstance();
        mTempdbReference=mFirebaseDatabase.getReference().child("tempdb").child("users");



        Button sign_in_button=(Button) findViewById(R.id.sign_in_button);
        Button exit_button=(Button) findViewById(R.id.exitbutton);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"onClick");
                //Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                Intent signInIntent= AuthUI.getInstance().createSignInIntentBuilder()
                                     .setLogo(R.drawable.hotcold)
                                     .setProviders(AuthUI.EMAIL_PROVIDER, AuthUI.GOOGLE_PROVIDER)
                                     .setIsSmartLockEnabled(true)
                                     .build();
                startActivityForResult(signInIntent,RC_SIGN_IN);
            }
        });
        exit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        mAuthStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(LOG_TAG,"Notification onAuthStateChanged");
                FirebaseUser user=firebaseAuth.getCurrentUser();
                if (user==null){
                    Log.d(LOG_TAG,"User is null");
                }
                else {
                    Log.d(LOG_TAG,"User:"+user.getUid()+user.getDisplayName()+" Logged IN ..checking user permission");
                    checkUser(user);
                }

            }
        };
    }

    //if the user belongs to home start MainActity
    //otherwise start UserNo
    private void checkUser(FirebaseUser user){
        Log.d(LOG_TAG,"checkUser started");
        final FirebaseUser loggedUser=user;
        mTempdbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG,"snapshot:"+dataSnapshot.toString()+" looking for child:"+loggedUser.getUid());
                if (dataSnapshot.hasChild(loggedUser.getUid())){
                    Intent i=new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(i);
                }
                else {
                    Log.d(LOG_TAG,"User Not Part of Home");
                    //TODO: Handle it
                }
            }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

    }






    @Override
    public void onResume(){
        super.onResume();
       if(mFirebaseAuth!=null){
           mFirebaseAuth.addAuthStateListener(mAuthStateListener);
       }

    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(LOG_TAG,"onPause");
        if(mFirebaseAuth!=null){
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==RC_SIGN_IN){
            // it was a SIGN IN activity
            if (resultCode==RESULT_OK){
                Toast.makeText(getApplicationContext(),"Sign In success",Toast.LENGTH_LONG).show();
                mFirebaseAuth=FirebaseAuth.getInstance();
            }
            else if (resultCode==RESULT_CANCELED){
                Toast.makeText(getApplicationContext(),"Sign In was not successfull",Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
