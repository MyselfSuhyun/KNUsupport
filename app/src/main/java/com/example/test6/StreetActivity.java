package com.example.test6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.test6.helpchat.MessageActivity;
import com.example.test6.model.GpsModel;
import com.example.test6.model.GreatModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.widget.Toast.LENGTH_SHORT;

public class StreetActivity extends AppCompatActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private Marker currentMarker = null;
    private List<GpsModel> gpsModels = new ArrayList<>();

    private String destinatonUid;
    private String uid;
    private String Timestamp;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();


    private static final String TAG = "실시간 지도 표시";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 4000;  // 2초마다 업데이트
    private static final int FASTEST_UPDATE_INTERVAL_MS = 2000; // 1초마다 업데이트
    private static final int PERMISSIONS_REQUEST_CODE = 200; //0.1초마다 업데이트
    boolean needRequest = false;
    private Button btn_meet,btn_chat;
    private Context mContext;

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소에 위치 퍼미션 허용

    Location mCurrentLocatiion;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    String distance, ableok;
    private String Remote="0";
    private double mlat=35.890434,mlng= 128.612025,mlat2=35.890434,mlng2= 128.612025;

    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.

    String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_street);

        mLayout = findViewById(R.id.layout_main);

        btn_meet= findViewById(R.id.meetbutton);
        btn_chat = findViewById(R.id.chatbutton);

        mContext = this;

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //채팅을 요구 하는 아아디 즉 단말기에 로그인된 UID
        if( getIntent().getStringExtra("destinationUid")!=null){
            destinatonUid = getIntent().getStringExtra("destinationUid"); // 채팅을 당하는 아이디
        }else{
        }

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        myRef.child("gpsmodel").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMap.clear();
                if(dataSnapshot.child(uid).child("latitude").getValue()!=null){
                    mlat = dataSnapshot.child(uid).child("latitude").getValue(Double.class);
                }
                if(dataSnapshot.child(uid).child("longtitude").getValue()!=null){
                    mlng = dataSnapshot.child(uid).child("longtitude").getValue(Double.class);
                }
                if(dataSnapshot.child(destinatonUid).child("latitude").getValue()!=null){
                    mlat2 = dataSnapshot.child(destinatonUid).child("latitude").getValue(Double.class);
                }
                if(dataSnapshot.child(destinatonUid).child("longtitude").getValue()!=null) {
                    mlng2 = dataSnapshot.child(destinatonUid).child("longtitude").getValue(Double.class);
                }
                distance = calcDistance(mlat, mlng, mlat2, mlng2);

                MarkerOptions makerOptions1 = new MarkerOptions();
                makerOptions1 // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
                        .position(new LatLng(mlat,mlng))
                        .title("내 위치")
                        .snippet("위도:" + mlat + " 경도:" + mlng);


                // 2. 마커 생성 (마커를 나타냄)
                mMap.addMarker(makerOptions1);

                MarkerOptions makerOptions2 = new MarkerOptions();
                makerOptions2 // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
                        .position(new LatLng(mlat2, mlng2))
                        .title("상대위치")
                        .snippet("안녕하세요. 저는"+distance+"에 위치해 있습니다.")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.yourmaker15)); ;
                mMap.addMarker(makerOptions2);
                String intStr;
                double disnum;
                if(distance.contains("km"))
                {
                    intStr = distance.replaceAll("[^0-9.]", "");
                    disnum = Double.parseDouble(intStr)*1000;
                }else{
                    intStr = distance.replaceAll("[^0-9]", "");
                    disnum = Double.parseDouble(intStr);
                }

                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mlat,mlng)));

                if(disnum<15.0){
                        btn_meet.setEnabled(true);
                        btn_meet.setText("매칭 성사! 버튼을 눌러주세요.");
                    myRef.child("RemoteController").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue()!=null){
                                Remote = dataSnapshot.getValue().toString();
                            }
                            if(Remote.equals("yes3")){
                                FirebaseDatabase.getInstance().getReference().child("RemoteController").child(uid).setValue("yes4").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FirebaseDatabase.getInstance().getReference().child("helpcall").child(uid).child("matching").setValue("매칭완료");
                                        Intent i = new Intent(StreetActivity.this,MeetActivity.class);
                                        startActivity(i);
                                        ActivityCompat.finishAffinity(StreetActivity.this);
                                        finish();
                                    }
                                });
                            }else{
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }else{
                    btn_meet.setEnabled(false);
                    btn_meet.setText("상대방과 "+distance+"만큼 떨어져있습니다.");
                }





            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef.child("users").child(uid).child("Disable").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ableok = dataSnapshot.getValue().toString();
                if (ableok.equals("장애인")) {
                    btn_meet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                FirebaseDatabase.getInstance().getReference().child("helpcall").child(uid).child("matching").setValue("매칭완료");
                                PreferenceManager.setLong(mContext, "Meettime",0 );
                                Toast.makeText(getApplicationContext(), "만남이 성사되었습니다!", LENGTH_SHORT).show();
                                Intent i = new Intent(StreetActivity.this, MeetActivity.class);
                                i.putExtra("destinationUid",destinatonUid);
                                startActivity(i);
                                finish();}
                    });
                    myRef.child("helpcall").child(uid).child("matching").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String chatok = null;
                            if(dataSnapshot.getValue()!=null){
                                chatok=dataSnapshot.getValue().toString();
                            }
                            if(chatok.equals("매칭완료")||chatok.equals(("완료"))){
                                btn_chat.setEnabled(false);
                            }else{
                                btn_chat.setEnabled(true);
                                btn_chat.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(view.getContext(), MessageActivity.class);
                                        intent.putExtra("destinationUid",destinatonUid);
                                        ActivityOptions activityOptions = null;
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                            activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright,R.anim.toleft);
                                            startActivity(intent,activityOptions.toBundle());
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    btn_meet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(getApplicationContext(), "만남이 성사되었습니다! \n좋은 봉사 되시기 바랍니다.", LENGTH_SHORT).show();
                            Intent i = new Intent(StreetActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                    });
                    myRef.child("helpcall").child(destinatonUid).child("matching").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String chatok = null;
                            if(dataSnapshot.getValue()!=null){
                                chatok=dataSnapshot.getValue().toString();
                            }
                            if(chatok.equals("매칭완료")||chatok.equals(("완료"))){
                                btn_chat.setEnabled(false);
                            }else{
                                btn_chat.setEnabled(true);
                                btn_chat.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(view.getContext(), MessageActivity.class);
                                        intent.putExtra("destinationUid",destinatonUid);
                                        ActivityOptions activityOptions = null;
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                            activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright,R.anim.toleft);
                                            startActivity(intent,activityOptions.toBundle());
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");

        mMap = googleMap;



        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 경북대학교로 이동
        setDefaultLocation();

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {

            // 2. 퍼미션이 이미 있는경우

            startLocationUpdates(); // 3. 위치 업데이트 시작

        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부한 경우
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신
                        ActivityCompat.requestPermissions( StreetActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                Log.d( TAG, "onMapClick :");
            }
        });
    }


    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);


                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.clear();

                GpsModel gpsmodel = new GpsModel();
                gpsmodel.latitude = location.getLatitude();
                gpsmodel.longtitude = location.getLongitude();

                FirebaseDatabase.getInstance().getReference().child("gpsmodel").child(uid).setValue(gpsmodel);


                MarkerOptions makerOptions3 = new MarkerOptions();
                makerOptions3 // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
                        .position(new LatLng(location.getLatitude(),location.getLongitude()))
                        .title("내 위치")
                        .snippet("위도:" + location.getLatitude() + " 경도:" + location.getLongitude());


                // 2. 마커 생성 (마커를 나타냄)
                mMap.addMarker(makerOptions3);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));



            }
        }
    };

    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {

                Log.d(TAG, "startLocationUpdates : GPS 사용이 꺼져 있습니다.");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getCurrentAddress(LatLng latlng) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
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

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (currentMarker != null) currentMarker.remove();
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);

    }


    public void setDefaultLocation() {


        //디폴트 위치, 경북대학교
        LatLng DEFAULT_LOCATION = new LatLng(35.89, 128.61);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 여부를 확인하세요";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);

    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }

        return false;

    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신확인

            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {
                // 퍼미션을 허용했다면 위치 업데이트를 시작
                startLocationUpdates();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }
    //여기부터는 GPS 활성화를 위한 메소드
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StreetActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하시겠습니까?");
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
                        Log.d(TAG, "onActivityResult : GPS 활성화 되어있습니다.");
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }

    //위도 경도를 이용한 최단거리계산.
    public static String calcDistance(double lat1, double lon1, double lat2, double lon2){
        double EARTH_R, Rad, radLat1, radLat2, radDist;
        double distance, ret;

        EARTH_R = 6371000.0;
        Rad = Math.PI/180;
        radLat1 = Rad * lat1;
        radLat2 = Rad * lat2;
        radDist = Rad * (lon1 - lon2);

        distance = Math.sin(radLat1) * Math.sin(radLat2);
        distance = distance + Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radDist);
        ret = EARTH_R * Math.acos(distance);

        double rslt = Math.round(Math.round(ret*10)/1000.0)/10.0;
        String result = rslt + " km";
        if(rslt <= 1) result = Math.round(ret) +" m";

        return result;
    }
    public void onBackPressed() {
        Toast.makeText(this, "뒤로가기를 누르셨습니다.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(StreetActivity.this,MainActivity.class);
        intent.putExtra("destinationUid",destinatonUid);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}

