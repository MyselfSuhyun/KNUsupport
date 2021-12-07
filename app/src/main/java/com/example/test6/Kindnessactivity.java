package com.example.test6;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class Kindnessactivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kindnessactivity);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Kindnessactivity.this,MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}