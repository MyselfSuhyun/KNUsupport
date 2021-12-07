package com.example.test6;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.test6.helpchat.CallActivity;
import com.example.test6.model.GpsModel;
import com.example.test6.model.HelpModel;
import com.example.test6.model.NotificationModel;
import com.example.test6.model.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.widget.Toast.LENGTH_SHORT;

public class HelpwaitActivity extends AppCompatActivity {
    private AlertDialog dialog;
    private TextView timer;
    private long baseTime=0,pauseTime;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    private String uid,matchok="0";
    private String Remote="";
    private String destinationuid;
    private Context Waitcontext,mContext;
    private String remoteok;
    private String bluename="0",time;
    String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpwait);

        Waitcontext = this;
        mContext=this;
        long WT = PreferenceManager.getLong(Waitcontext, "Waittime");



        timer = (TextView)findViewById(R.id.timeView);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //채팅을 요구 하는 아아디 즉 단말기에 로그인된 UID
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();




        ImageView helpwait = (ImageView) findViewById(R.id.gif_image);
        Glide.with(this).load(R.drawable.helpwait).into(helpwait);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor("#4ea1d3"));
        }
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("RemoteController").child(uid).getValue()!=null){
                    Remote = Objects.requireNonNull(dataSnapshot.child("RemoteController").child(uid).getValue()).toString();
                }
                if(Remote.equals("yes2")){
                    final Button btn_str = (Button) findViewById(R.id.streetbutton);
                    if (dataSnapshot.child("helpcall").child(uid).child("matching").getValue() != null) {
                        matchok = dataSnapshot.child("helpcall").child(uid).child("matching").getValue().toString();
                        if (matchok.equals("수락")) {
                            btn_str.setText("매칭 수락");
                            if (dataSnapshot.child("helpcall").child(uid).child("destinationuid").getValue() != null) {
                                destinationuid = dataSnapshot.child("helpcall").child(uid).child("destinationuid").getValue().toString();
                                FirebaseDatabase.getInstance().getReference().child("RemoteController").child(uid).setValue("yes3").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        PreferenceManager.setLong(Waitcontext, "Waittime",0 );
                                        //Intent i = new Intent(MainActivity.this, WritingsActivity.class);
                                        Intent intent = new Intent(HelpwaitActivity.this, StreetActivity.class);
                                        intent.putExtra("destinationUid",destinationuid);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                            btn_str.setEnabled(true);
                        }
                    }
                }else{

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Button btn_str = (Button) findViewById(R.id.streetbutton);
                if (dataSnapshot.child("helpcall").child(uid).child("matching").getValue() != null) {
                    matchok = dataSnapshot.child("helpcall").child(uid).child("matching").getValue().toString();
                }
                if (matchok.equals("매칭대기")) {
                    btn_str.setText("매칭 대기");
                    btn_str.setEnabled(false);
                }else if (matchok.equals("수락")) {
                    btn_str.setText("매칭 수락");
                    if(dataSnapshot.child("helpcall").child(uid).child("destinationuid").getValue()!=null){
                        destinationuid = dataSnapshot.child("helpcall").child(uid).child("destinationuid").getValue().toString();
                    }
                    btn_str.setEnabled(true);
                }else{
                    btn_str.setText("매칭 오류");
                    btn_str.setEnabled(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (WT!=0) {
            baseTime = WT;
        }else{
            baseTime = SystemClock.elapsedRealtime();
            PreferenceManager.setLong(Waitcontext, "Waittime",baseTime );
        }
        handler.sendEmptyMessage(0);
    }

    public void streetbutton(View view){

        Toast.makeText(getApplicationContext(), "실시간 매칭화면으로 넘어갑니다.", LENGTH_SHORT).show();
        PreferenceManager.setLong(Waitcontext, "Waittime",0 );
        //Intent i = new Intent(MainActivity.this, WritingsActivity.class);
        Intent intent = new Intent(view.getContext(), StreetActivity.class);
        intent.putExtra("destinationUid",destinationuid);
        startActivity(intent);
        finish();
    }
    public void cancelbutton(View v){
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("취소하기").setMessage("취소하시겠습니까? \n취소시 메인화면으로 돌아갑니다.")
                .setNegativeButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        FirebaseDatabase.getInstance().getReference().child("helpcall").child(uid).child("matching").setValue("취소");
                        FirebaseDatabase.getInstance().getReference().child("RemoteController").child(uid).setValue("no").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Intent i = new Intent(HelpwaitActivity.this, MainActivity.class);
                                startActivity(i);
                                finish();
                            }
                        });
                    }
                })
                .setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .show();
    }
    private String getTime(){
        //경과된 시간 체크

        long nowTime = SystemClock.elapsedRealtime();

        //시스템이 부팅된 이후의 시간?
        long overTime = nowTime - baseTime;

        long m = overTime/1000/60;
        long s = (overTime/1000)%60;
        long ms = overTime % 1000;

        String recTime = String.format("%02d:%02d:%03d",m,s,ms);

        return recTime;
    }
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){

        @Override
        public void handleMessage(@NonNull Message msg) {

            timer.setText(getTime());


            handler.sendEmptyMessage(0);
        }
    };

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "뒤로가기를 누르셨습니다.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HelpwaitActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super .onStop();
        if(dialog !=null)
        {
            dialog.dismiss();
            dialog = null;
        }
    }

}