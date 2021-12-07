package com.example.test6;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_SHORT;


public class RegisterActivity extends AppCompatActivity {

    private String userID;
    private String userPassword;
    private String userPassword2;
    private String userNAME;
    private String userGender;
    private String userDisable;
    private String userStudent;
    private String userPhone;
    private AlertDialog dialog;
    private boolean validate = false;
    ProgressDialog progressDialog;

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
    public InputFilter filterKor = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,

                                   Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$");

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
        setContentView(R.layout.activity_register);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()
                .permitDiskWrites()
                .permitNetwork().build());


        final EditText idText = (EditText) findViewById(R.id.idText);
        final EditText passText = (EditText) findViewById(R.id.passText);
        final EditText passTextcheck = (EditText) findViewById(R.id.passTextcheck);
        final EditText nameText = (EditText) findViewById(R.id.nameText);
        final EditText studentText = (EditText) findViewById(R.id.studentText);
        final EditText phonText = (EditText) findViewById(R.id.phonText);
        idText.setFilters(new InputFilter[] {filterAlphaNum});
        passText.setFilters(new InputFilter[] {filterpass});
        passTextcheck.setFilters(new InputFilter[] {filterpass});
        nameText.setFilters(new InputFilter[] {filterKor});

        phonText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {
                Log.d("TEST","beforeTextChanged: "+ s);
            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                Log.d("TEST","beforeTextChanged: "+ s);
                if (s.length() > 11) {
                    phonText.setText(phonText.getText().toString().substring(0,11));
                    phonText.setSelection(phonText.length());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("TEST","afterTextChanged: "+ s);
            }
        });

        RadioGroup genderGroup = (RadioGroup) findViewById(R.id.genderGroup);
        int genderGroupID = genderGroup.getCheckedRadioButtonId();
        userGender = ((RadioButton) findViewById(genderGroupID)).getText().toString();

        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int i) {
                RadioButton genderButton = (RadioButton) findViewById(i);
                userGender = genderButton.getText().toString();
            }
        });
        RadioGroup ableGroup = (RadioGroup) findViewById(R.id.disablegroup);
        int ableGroupID = ableGroup.getCheckedRadioButtonId();
        userDisable = ((RadioButton) findViewById(ableGroupID)).getText().toString();

        ableGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int i) {
                RadioButton disableButton = (RadioButton) findViewById(i);
                userDisable = disableButton.getText().toString();
            }
        });

        final Button validateButton = (Button) findViewById(R.id.validateButton);
        validateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String userID = idText.getText().toString();
                if (validate) {
                    return;
                }
                if (userID.equals(""))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("경북대 메일을 사용해주세요"+"\n"+"(EX. *@knu.ac.kr).")
                            .setPositiveButton("확인",null)
                            .create();
                    dialog.show();
                    return;
                }
                else if(!Pattern.matches("^[_a-zA-Z0-9-\\.]+@knu.ac.kr+$",userID)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("경북대 메일을 사용해 주세요."+"\n"+"(예: *@knu.ac.kr)")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }
                Response.Listener<String> responseListener = new Response.Listener<String>(){

                    @Override
                    public void onResponse(String response) {
                        try
                        {
                            Toast.makeText(RegisterActivity.this, response, Toast.LENGTH_LONG).show();
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if(success){
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                dialog = builder.setMessage("사용할 수 있는 아이디입니다.")
                                        .setPositiveButton("확인",null)
                                        .create();
                                dialog.show();
                                idText.setEnabled(false);
                                validate = true;
                                idText.setBackgroundColor(getResources().getColor(R.color.colorGray));
                                validateButton.setBackgroundColor(getResources().getColor(R.color.colorGray));
                            }
                            else{
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                dialog = builder.setMessage("사용할 수 없는 아이디입니다.")
                                        .setNegativeButton("확인",null)
                                        .create();
                                dialog.show();
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                LoginRequest loginRequest = new LoginRequest(userID, responseListener);
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(loginRequest);
            }
        });

        Button join = (Button) findViewById(R.id.join);
        join.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                final String userID = idText.getText().toString();
                final String userPassword = passText.getText().toString();
                final String userPasswordCheck = passTextcheck.getText().toString();
                final String userNAME = nameText.getText().toString();
                final String userStudent = studentText.getText().toString();
                final String userPhone = phonText.getText().toString();

                if (!validate) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("아이디 중복 체크를 해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }
                if (userPassword.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호를 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }
                if (!Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{8,15}$", userPassword)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호를 형식을 지켜주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }
                if (userPasswordCheck.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호를 한번 더 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }
                if (!userPassword.equals(userPasswordCheck)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호가 일치하지않습니다.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }
                if (!Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{8,15}$", userPasswordCheck)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("비밀번호를 형식을 지켜주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }
                if (userNAME.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("이름를 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }
                if (userStudent.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("학번을 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }
                if (userPhone.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("휴대폰 번호를 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create();
                    dialog.show();
                    return;
                }
                if (userGender.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    dialog = builder.setMessage("성별을 입력해주세요.")
                            .setPositiveButton("확인", null)
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

                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                dialog = builder.setMessage("회원 등록에 실패했습니다.")
                                        .setNegativeButton("확인", null)
                                        .create();
                                dialog.show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                RegisterRequest registerRequest = new RegisterRequest(userID, userPassword, userNAME, userGender, userStudent, userPhone, responseListener);
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(registerRequest);

                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(idText.getText().toString(), passText.getText().toString())
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                UserModel userModel = new UserModel();
                                userModel.userName = nameText.getText().toString();
                                userModel.Student = studentText.getText().toString();
                                userModel.Phone = phonText.getText().toString();
                                userModel.Gender = userGender;
                                userModel.Disable = userDisable;
                                userModel.alim ="허용";
                                userModel.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


                                final String uid = task.getResult().getUser().getUid();

                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(nameText.getText().toString()).build();

                                task.getResult().getUser().updateProfile(userProfileChangeRequest);
                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(), "회원가입을 축하드립니다.", LENGTH_SHORT).show();
                                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                        intent.putExtra("value","back");
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                            }
                        });
            }

        });
    }
    public void Ocrbutton(View v) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

        if (status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
            Toast.makeText(getApplicationContext(), "수료증 인식 화면으로 넘어갑니다.", LENGTH_SHORT).show();
            Intent i = new Intent(RegisterActivity.this, OcrActivity.class);
            startActivity(i);
        } else {
            Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해 주세요", LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "뒤로가기를 누르셨습니다.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
        intent.putExtra("value","back");
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
