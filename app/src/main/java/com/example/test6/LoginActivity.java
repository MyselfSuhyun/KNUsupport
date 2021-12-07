package com.example.test6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;

import android.content.Intent;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_SHORT;


public class LoginActivity extends AppCompatActivity {

    private AlertDialog dialog;
    private FirebaseAuth mAuth;

    private EditText id;
    private EditText password;

    private Button login;
    private Button signup;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    CheckBox checkauto;

    SharedPreferences setting;
    SharedPreferences.Editor editor;


    protected InputFilter filterAlphaNum = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9@.]+$");

            if (!ps.matcher(source).matches()) {

                return "";

            }
            return null;
        }
    };
    protected InputFilter filterpass = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9!@#$%^&*]+$");

            if (!ps.matcher(source).matches()) {

                return "";

            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        String data = intent.getStringExtra("value");


        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signOut();
        id = (EditText) findViewById(R.id.idText);
        password = (EditText) findViewById(R.id.passText);
        id.setFilters(new InputFilter[] {filterAlphaNum});
        password.setFilters(new InputFilter[] {filterpass});

        login = (Button) findViewById(R.id.login);
        signup = (Button) findViewById(R.id.register);

        //자동로그인 체크 되있는지 확인 코드.

        checkauto=(CheckBox) findViewById(R.id.checkauto);

        setting = getSharedPreferences("setting",0);
        editor = setting.edit();

        if(setting.getBoolean("checkauto", false)){
            id.setText(setting.getString("ID", ""));
            password.setText(setting.getString("PW", ""));
            checkauto.setChecked(true);
        }
        //자동로그인 로그아웃으로 화면 이동 시 체크박스 해제코드.
        if(data.equals("logout")) {
            checkauto.setChecked(false);
        }else if(data.equals("login")){
            if(id.getText().length() != 0 || password.getText().length() != 0){
                firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }else if(data.equals("back")){
            }
        }else if(data.equals("delete")) {
            checkauto.setChecked(false);
            id.setText("");
            password.setText("");
        }else if(data.equals("change")) {
            checkauto.setChecked(true);
            password.setText("");
        } else if(data.equals("back")){
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

                if(status == NetworkStatus.TYPE_MOBILE||status == NetworkStatus.TYPE_WIFI){
                    if(id.getText().length() == 0 || password.getText().length() == 0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        dialog = builder.setMessage("아이디와 비밀번호를 입력해주세요.")
                                .setPositiveButton("확인", null)
                                .create();
                        dialog.show();
                        return;
                    } else {
                        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (!task.isSuccessful()) {
                                            //로그인 실패한부분
                                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                            dialog = builder.setMessage("아이디와 비밀번호가 틀렸습니다.")
                                                    .setPositiveButton("확인", null)
                                                    .create();
                                            dialog.show();
                                            return;
                                        }
                                    }
                                });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해 주세요", LENGTH_SHORT).show();
                }
            }

        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

                if(status == NetworkStatus.TYPE_MOBILE||status == NetworkStatus.TYPE_WIFI){
                    Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해 주세요", LENGTH_SHORT).show();
                }
            }
        });
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //로그인
                    if(checkauto.isChecked()) {
                        String ID = id.getText().toString();
                        String PW = password.getText().toString();

                        editor.putString("ID", ID);
                        editor.putString("PW", PW);
                        editor.putBoolean("checkauto", true);
                        editor.commit();
                    }else{
                        editor.clear();
                        editor.commit();
                    }
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    //로그아웃
                }

            }
        };
        //로그인 인터페이스 리스너
    }
    public void search_password(View v){
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

        if(status == NetworkStatus.TYPE_MOBILE||status == NetworkStatus.TYPE_WIFI){
            Toast.makeText(getApplicationContext(), "비밀번호 찾기 화면으로 넘어갑니다.", LENGTH_SHORT).show();
            Intent i = new Intent(LoginActivity.this, FindActivity.class);
            startActivity(i);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해 주세요", LENGTH_SHORT).show();
        }
    }
    @Override
    // 1. 뒤로가지 입력을 감지한다.
    public void onBackPressed() {
        // 2. 다이얼로그를 생성한다.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");
        builder.setMessage("앱을 종료하시겠습니까?");
        builder.setNegativeButton("취소", null);
        builder.setPositiveButton("종료", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(checkauto.isChecked()) {
                    editor.clear();
                    editor.commit();
                }else{
                    editor.clear();
                    editor.commit();
                }
                // 3. 다이얼로그의 긍정 이벤트일 경우 종료한다.
                moveTaskToBack(true);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        builder.show();
    }
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}