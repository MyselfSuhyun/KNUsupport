package com.example.test6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_SHORT;

public class PasswordchangeActivity extends AppCompatActivity {

    EditText Password,Pchange,Pchangecheck;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private AlertDialog dialog;
    private FirebaseAuth firebaseAuth;

    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //채팅을 요구 하는 아아디 즉 단말기에 로그인된 UID
    private String Email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwordchange);
        final EditText Password = (EditText) findViewById(R.id.passchange);
        final EditText Pchange = (EditText) findViewById(R.id.passchange2);
        final EditText Pchangecheck = (EditText) findViewById(R.id.passchange3);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        Button passchange = (Button) findViewById(R.id.passchangebutton);
        passchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String password = Password.getText().toString();
                final String pchange = Pchange.getText().toString();
                final String pchangecheck = Pchange.getText().toString();

                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

                if(status == NetworkStatus.TYPE_MOBILE||status == NetworkStatus.TYPE_WIFI){
                    if(Password.getText().length() == 0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(PasswordchangeActivity.this);
                        dialog = builder.setMessage("비밀번호를 입력해주세요.")
                                .setPositiveButton("확인", null)
                                .create();
                        dialog.show();
                        return;
                    } else {
                        firebaseAuth.signInWithEmailAndPassword(Email, password)
                                .addOnCompleteListener(PasswordchangeActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (!task.isSuccessful()) {
                                            //로그인 실패한부분
                                            AlertDialog.Builder builder = new AlertDialog.Builder(PasswordchangeActivity.this);
                                            dialog = builder.setMessage("비밀번호가 틀렸습니다.")
                                                    .setPositiveButton("확인", null)
                                                    .create();
                                            dialog.show();
                                            return;
                                        }else{
                                            if (pchange.equals("")) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(PasswordchangeActivity.this);
                                                dialog = builder.setMessage("변경할 비밀번호를 입력해주세요.")
                                                        .setPositiveButton("확인", null)
                                                        .create();
                                                dialog.show();
                                                return;
                                            }
                                            if (!Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{8,15}$", pchange)) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(PasswordchangeActivity.this);
                                                dialog = builder.setMessage("비밀번호를 형식을 지켜주세요.")
                                                        .setPositiveButton("확인", null)
                                                        .create();
                                                dialog.show();
                                                return;
                                            }
                                            if (pchangecheck.equals("")) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(PasswordchangeActivity.this);
                                                dialog = builder.setMessage("비밀번호를 한번 더 입력해주세요.")
                                                        .setPositiveButton("확인", null)
                                                        .create();
                                                dialog.show();
                                                return;
                                            }
                                            if (!pchange.equals(pchangecheck)) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(PasswordchangeActivity.this);
                                                dialog = builder.setMessage("비밀번호가 일치하지않습니다.")
                                                        .setPositiveButton("확인", null)
                                                        .create();
                                                dialog.show();
                                                return;
                                            }
                                            if (!Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{8,15}$", pchangecheck)) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(PasswordchangeActivity.this);
                                                dialog = builder.setMessage("비밀번호를 형식을 지켜주세요.")
                                                        .setPositiveButton("확인", null)
                                                        .create();
                                                dialog.show();
                                                return;
                                            }
                                            user.updatePassword(pchange)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getApplicationContext(), "비밀번호가 변경되었습니다.\n바뀐 비밀번호로 접속해주세요.", LENGTH_SHORT).show();
                                                                Intent intent = new Intent(PasswordchangeActivity.this,LoginActivity.class);
                                                                intent.putExtra("value","change");
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }
                                                    });

                                        }
                                    }
                                });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해 주세요", LENGTH_SHORT).show();
                }


            }
        });


    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PasswordchangeActivity.this,ProfileActivity.class);
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