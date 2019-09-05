package com.google.firebase.udacity.mysmarthome;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RequestActivity extends AppCompatActivity {
    private static final String DEFAULT_MESSAGE="New Permission Requst";
    private static final String LOG_TAG=RequestActivity.class.getSimpleName();
    private FirebaseDatabase mFirebaseDatabse=FirebaseDatabase.getInstance();
    private DatabaseReference mFirebaseDbRef=mFirebaseDatabse.getReference().child("tempdb/pending");
    private boolean requestSent;

    public class UserRequest{
        public String name;
        public String request;
        public UserRequest(String name, String requestMessage){
            this.name=name;
            this.request=requestMessage;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request);


     Button sendButton=(Button) findViewById(R.id.sendButton);
     sendButton.setEnabled(false);
     final String userId=getIntent().getStringExtra("uid");
     final String userName=getIntent().getStringExtra("name");
     if (userId!=null && !requestSent) {
         sendButton.setEnabled(true);
     }
     else {
         if (requestSent){
             Toast.makeText(getApplicationContext(),"You have already submitted a request! Please be patient. You will be notified as soon as possibl e",Toast.LENGTH_LONG).show();
         }
     }
     sendButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             UserRequest ur= new UserRequest(userName,DEFAULT_MESSAGE);
             mFirebaseDbRef.child(userId).setValue(ur).
                     addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void aVoid) {
                             Log.d(LOG_TAG,"Pending db updated");
                             showRequestConfirmDialog();
                             //  Toast.makeText(getApplicationContext(),"Your Request has been sent to the administrator. You will be notified once accepted",Toast.LENGTH_LONG).show();
                         }
                     })
                     .addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             Log.d(LOG_TAG,"Fail to add user to pending location"+e.getMessage());
                             Toast.makeText(getApplicationContext(),"Cannot process this request, perhaps you have already requested a permssion!",Toast.LENGTH_LONG).show();
                             showRequestConfirmDialog();
                         }
                     });
         }
     });


    }

    //handling onBack - user will be signed out and Login Activity displayed
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        handleBack();
    }

    private void showRequestConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this, R.style.Theme_AppCompat_Dialog);
// 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_message)
                .setTitle(R.string.dialog_db_updated)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        handleBack();
                    }
                });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    private void handleBack(){
            Log.d(LOG_TAG,"OnBackPressed");
            AuthUI.getInstance().signOut(this);
            Intent loginIntent = new Intent(this,LoginActivity.class);
            startActivity(loginIntent);
        }

    //requestSent
    // check if there is already a pendingRequest for this user looking at the child a tempdb/pending/
    private void requestSent(String uid ){
        requestSent=false;
        final String userId=uid;
        mFirebaseDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG,"Check Request:dataSnapshot:"+dataSnapshot.toString()+" Looking for child:"+userId);
                if (dataSnapshot.hasChild(userId)){
                   requestSent=true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Log.d(LOG_TAG,"requestSent returning:"+requestSent);
        return;
    }
}


