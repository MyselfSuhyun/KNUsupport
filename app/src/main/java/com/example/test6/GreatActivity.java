package com.example.test6;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.test6.fragment.HelpcallFragment;
import com.example.test6.fragment.KindnessFragment;
import com.example.test6.model.GreatModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GreatActivity extends AppCompatActivity {

    float sum=0;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_great);

        getFragmentManager().beginTransaction().replace(R.id.greatactivity_framelayout, new KindnessFragment()).commit();
        RatingBar greatrating = (RatingBar)findViewById(R.id.greatRating);

        List<GreatModel> greatModels;

        greatModels = new ArrayList<>();
        final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("Scores").child(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                greatModels.clear();

                for(DataSnapshot snapshot :dataSnapshot.getChildren()){


                    GreatModel greatModel = snapshot.getValue(GreatModel.class);

                    sum += greatModel.MeetScore;

                    greatModels.add(greatModel);

                    greatrating.setRating(sum/greatModels.size());
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    @Override
    public void onBackPressed() {
        Toast.makeText(this, "뒤로가기를 누르셨습니다.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(GreatActivity.this,MainActivity.class);
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