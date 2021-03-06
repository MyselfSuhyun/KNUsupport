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

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //????????? ?????? ?????? ????????? ??? ???????????? ???????????? UID
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
            dialog = builder.setMessage("?????? ????????? ??????????????????.")
                    .setPositiveButton("??????",null)
                    .create();
            dialog.show();
            return;
        }
        if(conHelp.equals("")){
            AlertDialog.Builder builder = new AlertDialog.Builder(AskhelpActivity.this);
            dialog = builder.setMessage("?????? ????????? ??????????????????.")
                    .setPositiveButton("??????",null)
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
        helpModel.matching = "????????????";

        List<UserModel> userModels;

        userModels = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userModels.clear();

                for(DataSnapshot snapshot :dataSnapshot.getChildren()){

                    UserModel userModel = snapshot.getValue(UserModel.class);

                    if(userModel.alim!=null){
                        if(userModel.alim.equals("?????????")) {
                            continue;
                        }
                    }
                    if (userModel.Disable != null) {
                        if (userModel.Disable.equals("?????????")) {
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
                Toast.makeText(getApplicationContext(), "?????? ????????? ??????????????????.", LENGTH_SHORT).show();
                Intent i = new Intent(AskhelpActivity.this, HelpwaitActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
    /*
     * ActivityCompat.requestPermissions??? ????????? ????????? ????????? ????????? ???????????? ??????????????????.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????

            boolean check_result = true;


            // ?????? ???????????? ??????????????? ???????????????.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                //?????? ?????? ????????? ??? ??????
                ;
            }
            else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.2 ?????? ????????? ????????????.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(AskhelpActivity.this, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????.", Toast.LENGTH_LONG).show();
                    finish();


                }else {

                    Toast.makeText(AskhelpActivity.this, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission(){

        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(AskhelpActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(AskhelpActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)


            // 3.  ?????? ?????? ????????? ??? ??????



        } else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.

            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(AskhelpActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Toast.makeText(AskhelpActivity.this, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(AskhelpActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(AskhelpActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress( double latitude, double longitude) {

        //????????????... GPS??? ????????? ??????
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //???????????? ??????
            Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
            return "???????????? ????????? ????????????";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //??????????????? GPS ???????????? ?????? ????????????
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(AskhelpActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
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

                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS ????????? ?????????");
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

            final String askHelp = "??????????????? ??????";
            final String conHelp = "????????? ??????";

            HelpModel helpModel = new HelpModel();
            helpModel.destinationuid = "X";
            helpModel.helptitle = askHelp;
            helpModel.helpcontent = conHelp;
            helpModel.myuid = uid;
            helpModel.helpanswer = userName;
            helpModel.helptime = timk;
            helpModel.matching = "????????????";
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
                                    if(userModel.alim.equals("?????????")) {
                                        continue;
                                    }
                                }
                                if (userModel.Disable != null) {
                                    if (userModel.Disable.equals("?????????")) {
                                        continue;

                                    }
                                }
                                                Gson gson = new Gson();
                                                NotificationModel notificationModel = new NotificationModel();
                                                notificationModel.to = userModel.pushToken;
                                                notificationModel.notification.title = "??????????????? ??????";
                                                notificationModel.notification.text = "????????? ??????";
                                                notificationModel.data.title = "??????????????? ??????";
                                                notificationModel.data.text = "????????? ??????";


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
            Toast.makeText(getApplicationContext(), "????????? ????????? ????????? ?????????", LENGTH_SHORT).show();
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