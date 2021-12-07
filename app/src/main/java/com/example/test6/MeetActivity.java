package com.example.test6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Rating;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.test6.helpchat.MessageActivity;
import com.example.test6.model.GreatModel;
import com.example.test6.model.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Objects;

import static android.widget.Toast.LENGTH_SHORT;

public class MeetActivity extends AppCompatActivity {
    private AlertDialog dialog;
    private TextView timer;
    private long baseTime=0,pauseTime;
    private Button btn_exam;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    private String uid,matchok="0",Remote="0";
    private String destinationuid,Timestamp;
    private Context mContext,uidcontext;
    public float Meetrating= (float) 2.5;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet);

        RatingBar meetrating = (RatingBar)findViewById(R.id.MeetRating);
        btn_exam= findViewById(R.id.exambutton);
        //btn_exam.setEnabled(false);
        mContext = this;


        long mt = PreferenceManager.getLong(mContext, "Meettime");



        timer = (TextView)findViewById(R.id.timeView);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //채팅을 요구 하는 아아디 즉 단말기에 로그인된 UID
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();


        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(Color.parseColor("#4ea1d3"));
        }

        meetrating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener(){
            @Override
            public void onRatingChanged(RatingBar ratingBar1, float rating, boolean fromUser){
                Meetrating = rating;
            }
        });


        if (mt!=0) {
            baseTime = mt;
        }else{
            baseTime = SystemClock.elapsedRealtime();
            PreferenceManager.setLong(mContext, "Meettime",baseTime );
        }
        handler.sendEmptyMessage(0);

        myRef.child("helpcall").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Timestamp = dataSnapshot.child("helptime").getValue().toString();
                destinationuid = dataSnapshot.child("destinationuid").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        myRef.child("helpcall").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Timestamp = dataSnapshot.child("helptime").getValue().toString();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        myRef.child("RemoteController").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    Remote = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                }
                if (Remote.equals("yes4")) {
                    Calendar cal = Calendar.getInstance();
                    int year = cal.get(Calendar.YEAR);
                    GreatModel greatModel = new GreatModel();
                    greatModel.Meettime = year + "." + Timestamp;
                    greatModel.MeetContext = "감사합니다";
                    greatModel.MeetScore = 5;
                    greatModel.MeetVisible = "Visible";
                    myRef.child("RemoteController").child(uid).setValue("no").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            myRef.child("helpcall").child(uid).child("matching").setValue("완료").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    FirebaseDatabase.getInstance().getReference().child("Scores").child(destinationuid).push().setValue(greatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getApplicationContext(), "평가가 자동 완료되었습니다.", LENGTH_SHORT).show();
                                            //PreferenceManager.setString(uidcontext,"uidkey","0");
                                            PreferenceManager.setLong(mContext, "Meettime", 0);
                                            Intent intent = new Intent(MeetActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void exambutton(View view){

        new AlertDialog.Builder(this)
                .setTitle("평가").setMessage("등록하시겠습니까? 수정할 수 없습니다.")
                .setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNegativeButton("등록하기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final EditText meetText = (EditText) findViewById(R.id.contextmeet);
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get(Calendar.YEAR);
                        String Meet;

                        CheckBox checkBox = (CheckBox) findViewById(R.id.evlcheck) ;
                        if (checkBox.isChecked()) {
                            Meet="Unvisible";
                        } else {
                            Meet="Visible";
                        }

                        GreatModel greatModel = new GreatModel();
                        greatModel.MeetContext = meetText.getText().toString();
                        greatModel.MeetScore = Meetrating;
                        greatModel.Meettime = year+"."+Timestamp;
                        greatModel.MeetVisible = Meet;
                        FirebaseDatabase.getInstance().getReference().child("Scores").child(destinationuid).push().setValue(greatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(getApplicationContext(), "평가가 완료되었습니다.", LENGTH_SHORT).show();
                                //PreferenceManager.setString(uidcontext,"uidkey","0");
                                myRef.child("helpcall").child(uid).child("matching").setValue("완료");
                                PreferenceManager.setLong(mContext, "Meettime",0 );
                                Intent intent = new Intent(MeetActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
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
        Intent intent = new Intent(MeetActivity.this,MainActivity.class);
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