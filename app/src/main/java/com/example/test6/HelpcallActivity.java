package com.example.test6;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.test6.fragment.HelpcallFragment;

public class HelpcallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpcall);

        getFragmentManager().beginTransaction().replace(R.id.helpcallactivity_framelayout, new HelpcallFragment()).commit();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(HelpcallActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}