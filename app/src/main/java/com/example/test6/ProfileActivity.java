package com.example.test6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.os.Vibrator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;


import static android.widget.Toast.LENGTH_SHORT;

public class ProfileActivity extends AppCompatActivity {
    Integer mTvBluetoothStatus;
    private Vibrator vibrator;
    private TextView textview;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private AlertDialog dialog;
    private FirebaseAuth firebaseAuth;


    private String Alimgroup;
    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //????????? ?????? ?????? ????????? ??? ???????????? ???????????? UID
    private String Email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    private String ableok, ableokk, matchok = "0";
    private Button btn_help;

    private ListView lstvw;
    private ArrayAdapter aAdapter;
    String gender,phone,studentid,name,alim;

    //editText.setClickable(false);
    //editText.setFocusable(false);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();


        TextView Emailtext = (TextView) findViewById(R.id.emailchange);
        TextView nametext = (TextView) findViewById(R.id.namechange);
        TextView phonetext = (TextView) findViewById(R.id.phonechange);
        TextView studenttext = (TextView) findViewById(R.id.studentchange);
        TextView gendertext = (TextView) findViewById(R.id.genderchange);
        TextView alimtext = (TextView) findViewById(R.id.Helpannounce);
        Emailtext.setText(Email);


        RadioGroup genderGroup = (RadioGroup) findViewById(R.id.Alimgroup);
        int genderGroupID = genderGroup.getCheckedRadioButtonId();
        Alimgroup = ((RadioButton) findViewById(genderGroupID)).getText().toString();

        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int i) {
                RadioButton genderButton = (RadioButton) findViewById(i);
                Alimgroup = genderButton.getText().toString();
            }
        });

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("users").child(uid).getValue()!=null){
                    name = dataSnapshot.child("users").child(uid).child("userName").getValue().toString();
                    phone = dataSnapshot.child("users").child(uid).child("Phone").getValue().toString();
                    gender =  dataSnapshot.child("users").child(uid).child("Gender").getValue().toString();
                    studentid = dataSnapshot.child("users").child(uid).child("Student").getValue().toString();
                    alim = dataSnapshot.child("users").child(uid).child("Disable").getValue().toString();
                    if(alim.equals("?????????")){
                        alimtext.setVisibility(View.GONE);
                        genderGroup.setVisibility(View.GONE);

                    }
                    nametext.setText(name);
                    phonetext.setText(phone);
                    gendertext.setText(gender);
                    studenttext.setText(studentid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void changebutton(View v){

        final EditText emailchange = (EditText) findViewById(R.id.emailchange);
        final EditText passchange = (EditText) findViewById(R.id.passchange);
        final EditText phonechange = (EditText) findViewById(R.id.phonechange);


        final String userPhone = phonechange.getText().toString();

        final String userPassword = passchange.getText().toString();

        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

        if(status == NetworkStatus.TYPE_MOBILE||status == NetworkStatus.TYPE_WIFI){
            if(passchange.getText().length() == 0){
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                dialog = builder.setMessage("??????????????? ??????????????????.")
                        .setPositiveButton("??????", null)
                        .create();
                dialog.show();
                return;
            } else {
                firebaseAuth.signInWithEmailAndPassword(emailchange.getText().toString(), passchange.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    //????????? ???????????????
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                                    dialog = builder.setMessage("??????????????? ???????????????.")
                                            .setPositiveButton("??????", null)
                                            .create();
                                    dialog.show();
                                    return;
                                }else{
                                    if (userPhone.equals("")) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                                        dialog = builder.setMessage("????????? ????????? ??????????????????.")
                                                .setPositiveButton("??????", null)
                                                .create();
                                        dialog.show();
                                        return;
                                    }
                                    if(!Pattern.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$",userPhone))
                                    {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                                        dialog = builder.setMessage("????????? ????????? ????????? ????????????.")
                                                .setPositiveButton("??????", null)
                                                .create();
                                        dialog.show();
                                        return;
                                    }
                                    FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("Phone").setValue(userPhone).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("alim").setValue(Alimgroup).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getApplicationContext(), "????????? ?????????????????????.", LENGTH_SHORT).show();
                                                    Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });

                                        }
                                    });

                                }
                            }
                        });
            }
        } else {
            Toast.makeText(getApplicationContext(), "????????? ????????? ????????? ?????????", LENGTH_SHORT).show();
        }
    }
    public void deletebutton(View v){
        new AlertDialog.Builder(this)
                .setTitle("????????????").setMessage("????????? ????????????????????????? \n?????? ?????? ????????? ???????????????.")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNegativeButton("????????????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Response.Listener<String> responseListener = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    boolean success = jsonResponse.getBoolean("success");
                                    //????????? ?????? success??? ??????????????? ??????????????? ?????? ?????? ?????? ?????????
                                    if (success) {
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        //volley ?????????
                        //1. RequestObject??? ????????????. ?????? ??????????????? ???????????? ?????? responseListener??? ????????? ????????????.
                        //????????? userID??? final??? ???????????? ?????? ?????? ?????????
                        DeleteRequest deleteRequest = new DeleteRequest(Email, responseListener);
                        //2. RequestQueue??? ????????????.
                        //????????? UserListAdapter??? Activity?????? ???????????? ???????????? ???????????? Activity?????? ???????????? ????????? ????????????
                        RequestQueue queue = Volley.newRequestQueue(ProfileActivity.this);
                        //3. RequestQueue??? RequestObject??? ????????????.
                        queue.add(deleteRequest);

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseDatabase.getInstance().getReference().child("users").child(uid).removeValue();
                                            Intent intent = new Intent(ProfileActivity.this,LoginActivity.class);
                                            intent.putExtra("value","delete");
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });

                    }
                })
                .show();

    }
    public void passchangebutton(View v){
        Toast.makeText(getApplicationContext(), "???????????? ?????? ???????????? ???????????????.", LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, PasswordchangeActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuthListener != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }
}