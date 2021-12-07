package com.example.test6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.test6.model.GpsModel;
import com.example.test6.model.GreatModel;
import com.example.test6.model.HelpModel;
import com.example.test6.model.NotificationModel;
import com.example.test6.model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.widget.Toast.LENGTH_SHORT;

public class AskhelpActivity extends AppCompatActivity {

    private GpsTracker gpsTracker;

    private Context Waitcontext;

    private List<GpsModel> gpsModels = new ArrayList<>();

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private String uid,helpname;
    private AlertDialog dialog;
    long now = System.currentTimeMillis();
    Date date = new Date(now);
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd HH:mm");

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private String destinationuid;
    String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    private String remoteok,Remote="Melon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_askhelp);

        Waitcontext = this;

        long WT = PreferenceManager.getLong(Waitcontext, "Waittime");

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //채팅을 요구 하는 아아디 즉 단말기에 로그인된 UID
        helpname = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        myRef.child("RemoteController").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    Remote = dataSnapshot.getValue().toString();
                }
                if(Remote.equals("yes")){
                    connect();
                }else{

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }
    }



    public void askhelpbutton(View v){
        final EditText askhelp = (EditText) findViewById(R.id.askehelp);
        final EditText contexthelp = (EditText) findViewById(R.id.contexthelp);

        gpsTracker = new GpsTracker(AskhelpActivity.this);


        final String askHelp = askhelp.getText().toString();
        final String conHelp = contexthelp.getText().toString();

        if(askHelp.equals("")){
            AlertDialog.Builder builder = new AlertDialog.Builder(AskhelpActivity.this);
            dialog = builder.setMessage("도움 제목을 입력해주세요.")
                    .setPositiveButton("확인",null)
                    .create();
            dialog.show();
            return;
        }
        if(conHelp.equals("")){
            AlertDialog.Builder builder = new AlertDialog.Builder(AskhelpActivity.this);
            dialog = builder.setMessage("도움 내용을 입력해주세요.")
                    .setPositiveButton("확인",null)
                    .create();
            dialog.show();
            return;
        }
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String time = simpleDateFormat.format(date);

        gpsTracker = new GpsTracker(AskhelpActivity.this);

        double latitude = gpsTracker.getLatitude();
        double longtitude = gpsTracker.getLongitude();

        GpsModel gpsmodel = new GpsModel();
        gpsmodel.latitude = latitude;
        gpsmodel.longtitude = longtitude;

        FirebaseDatabase.getInstance().getReference().child("gpsmodel").child(uid).setValue(gpsmodel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });



        HelpModel helpModel = new HelpModel();
        helpModel.destinationuid = "X";
        helpModel.helptitle = askhelp.getText().toString();
        helpModel.helpcontent = contexthelp.getText().toString();
        helpModel.myuid = uid;
        helpModel.helpanswer = helpname;
        helpModel.helptime = time;
        helpModel.matching = "매칭대기";

        List<UserModel> userModels;

        userModels = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userModels.clear();

                for(DataSnapshot snapshot :dataSnapshot.getChildren()){

                    UserModel userModel = snapshot.getValue(UserModel.class);

                    if(userModel.alim!=null){
                        if(userModel.alim.equals("비허용")) {
                            continue;
                        }
                    }
                    if (userModel.Disable != null) {
                        if (userModel.Disable.equals("장애인")) {
                            continue;

                        }
                    }
                    Gson gson = new Gson();

                    NotificationModel notificationModel = new NotificationModel();
                    notificationModel.to = userModel.pushToken;
                    notificationModel.notification.title = askhelp.getText().toString();
                    notificationModel.notification.text = contexthelp.getText().toString();
                    notificationModel.data.title = askhelp.getText().toString();
                    notificationModel.data.text = contexthelp.getText().toString();


                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel));

                    Request request = new Request.Builder()
                            .header("Content-Type", "application/json")
                            .addHeader("Authorization", "key=AAAAUmoQ2Pc:APA91bGqYYa6vm8s5lpx1qAJL9SS9y3BYCCLhWJQR7IBxWNr3SOyQSJc6MhpgNp3g3-Ta9o7vAfS4R9uzlTr3l_wslY5Rkx_8lh9DuihIFgORSPW-mtWOFBNEwJhGr63EUmouWnmpMs2")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(requestBody)
                            .build();
                    OkHttpClient okHttpClient = new OkHttpClient();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                        }
                    });




                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("helpcall").child(uid).setValue(helpModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                PreferenceManager.setLong(Waitcontext, "Waittime",0 );
                Toast.makeText(getApplicationContext(), "도움 요청을 보내겠습니다.", LENGTH_SHORT).show();
                Intent i = new Intent(AskhelpActivity.this, HelpwaitActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                //위치 값을 가져올 수 있음
                ;
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(AskhelpActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(AskhelpActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(AskhelpActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(AskhelpActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(AskhelpActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(AskhelpActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(AskhelpActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(AskhelpActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(AskhelpActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }
    void connect(){
        String timk = new SimpleDateFormat("MM.dd HH:mm").format(new Date(System.currentTimeMillis()));

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("helpcall").child(uid).child("matching").getValue() != null) {
                    remoteok = dataSnapshot.child("helpcall").child(uid).child("matching").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
        if (status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {

            final String askHelp = "시각장애인 호출";
            final String conHelp = "리모컨 이용";

            HelpModel helpModel = new HelpModel();
            helpModel.destinationuid = "X";
            helpModel.helptitle = askHelp;
            helpModel.helpcontent = conHelp;
            helpModel.myuid = uid;
            helpModel.helpanswer = userName;
            helpModel.helptime = timk;
            helpModel.matching = "매칭대기";
            FirebaseDatabase.getInstance().getReference().child("helpcall").child(uid).setValue(helpModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    List<UserModel> userModels;

                    userModels = new ArrayList<>();
                    FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            userModels.clear();

                            for(DataSnapshot snapshot :dataSnapshot.getChildren()){



                                UserModel userModel = snapshot.getValue(UserModel.class);

                                if(userModel.alim!=null){
                                    if(userModel.alim.equals("비허용")) {
                                        continue;
                                    }
                                }
                                if (userModel.Disable != null) {
                                    if (userModel.Disable.equals("장애인")) {
                                        continue;

                                    }
                                }
                                                Gson gson = new Gson();
                                                NotificationModel notificationModel = new NotificationModel();
                                                notificationModel.to = userModel.pushToken;
                                                notificationModel.notification.title = "시각장애인 호출";
                                                notificationModel.notification.text = "리모컨 이용";
                                                notificationModel.data.title = "시각장애인 호출";
                                                notificationModel.data.text = "리모컨 이용";


                                                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"),gson.toJson(notificationModel));

                                                Request request = new Request.Builder()
                                                        .header("Content-Type","application/json")
                                                        .addHeader("Authorization","key=AAAAUmoQ2Pc:APA91bGqYYa6vm8s5lpx1qAJL9SS9y3BYCCLhWJQR7IBxWNr3SOyQSJc6MhpgNp3g3-Ta9o7vAfS4R9uzlTr3l_wslY5Rkx_8lh9DuihIFgORSPW-mtWOFBNEwJhGr63EUmouWnmpMs2")
                                                        .url("https://fcm.googleapis.com/fcm/send")
                                                        .post(requestBody)
                                                        .build();
                                                OkHttpClient okHttpClient = new OkHttpClient();
                                                okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {

                                                    }

                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {

                                                    }
                                                });

                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    FirebaseDatabase.getInstance().getReference().child("RemoteController").child(uid).setValue("yes2").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            PreferenceManager.setLong(Waitcontext, "Waittime",0 );
                            Intent i = new Intent(AskhelpActivity.this, HelpwaitActivity.class);
                            startActivity(i);
                            ActivityCompat.finishAffinity(AskhelpActivity.this);
                            finish();
                        }
                    });
                }
            });





        } else {
            Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해 주세요", LENGTH_SHORT).show();
        }

    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AskhelpActivity.this,MainActivity.class);
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