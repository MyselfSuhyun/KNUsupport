package com.example.test6;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.test6.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.widget.Toast.LENGTH_SHORT;

public class WritingsActivity extends AppCompatActivity {

    private FirebaseAuth firebaseauth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private String uid,ableok;

    private AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writings);


        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //채팅을 요구 하는 아아디 즉 단말기에 로그인된 UID


    }
    public void writingokbutton(View v){

        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String lockpost = "0";
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //채팅을 요구 하는 아아디 즉 단말기에 로그인된 UID
        final EditText writitle = (EditText) findViewById(R.id.writingtitle);
        final EditText wricontent = (EditText) findViewById(R.id.writingcontent);

        final String wriTitle = writitle.getText().toString();
        final String wriContent = wricontent.getText().toString();

        if(wriTitle.equals("")){
            AlertDialog.Builder builder = new AlertDialog.Builder(WritingsActivity.this);
            dialog = builder.setMessage("제목을 입력해주세요.")
                    .setPositiveButton("확인",null)
                    .create();
            dialog.show();
            return;
        }
        if(wriContent.equals("")){
            AlertDialog.Builder builder = new AlertDialog.Builder(WritingsActivity.this);
            dialog = builder.setMessage("내용을 적어주세요.")
                    .setPositiveButton("확인",null)
                    .create();
            dialog.show();
            return;
        }

        Handler mHandler = new Handler();
        Response.Listener<String> responseListener = new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        Toast.makeText(getApplicationContext(), "게시글을 작성하였습니다.", LENGTH_SHORT).show();
                        Intent intent = new Intent(WritingsActivity.this,MainActivity.class);
                        intent.putExtra("value","back");
                        startActivity(intent);
                        finish();

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WritingsActivity.this);
                        dialog = builder.setMessage("게시글 작성에 실패했습니다.")
                                .setNegativeButton("확인", null)
                                .create();
                        dialog.show();
                    }
                } catch (Exception e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(WritingsActivity.this);
                    dialog = builder.setMessage("게시글 작성에 실패했습니다.")
                            .setNegativeButton("확인", null)
                            .create();
                    dialog.show();
                    e.printStackTrace();
                }
            }
        };
        WritingsRequest writingsRequest = new WritingsRequest(userName, uid, wriTitle, wriContent, responseListener);
        RequestQueue queue = Volley.newRequestQueue(WritingsActivity.this);
        queue.add(writingsRequest);


    }

    /*
        @Override
        public void onBackPressed() {
            Toast.makeText(this, "뒤로가기를 누르셨습니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(WritingsActivity.this,MainActivity.class);
            intent.putExtra("value","back");
            startActivity(intent);
            finish();
            super.onBackPressed();
        }*/
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