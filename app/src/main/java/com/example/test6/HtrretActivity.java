package com.example.test6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.test6.model.GpsModel;
import com.example.test6.model.UserModel;
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
import java.util.List;
import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;

public class HtrretActivity extends AppCompatActivity implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private Marker currentMarker = null;
    private List<GpsModel> gpsModels = new ArrayList<>();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    /*DatabaseReference myRef = database.getReference().child("gpsmodel").child("윤수현");*/
    DatabaseReference myRef = database.getReference();

    private String uid;
    private int mcsum,mcf,mci2,mcf2,mci3,mcf3;

    private static final String TAG = "실시간 지도 표시";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 2000;  // 2초마다 업데이트
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000; // 1초마다 업데이트
    private static final int PERMISSIONS_REQUEST_CODE = 100; //0.1초마다 업데이트
    boolean needRequest = false;

    private static String[][] mapdata = {
            {"경북대학교 본관","내가 있는 곳에서 "}, {"중앙도서관","내가 있는 곳에서 "}, {"박물관","내가 있는 곳에서 "}, {"정보전산원","내가 있는 곳에서 "}, {"종합정보센터","내가 있는 곳에서 "},
            {"복지관","내가 있는 곳에서 "}, {"백호관(제1학생회관)","내가 있는곳에서 "},{"인문대학","내가 있는 곳에서"},{"사회과학대학","내가 있는 곳에서 "},{"제2과학관","내가 있는 곳에서 "},
            {"화학관","내가 있는 곳에서 "},{"생물학관","내가 있는 곳에서 "},{"약학대학(제2합동강의동)","내가 있는 곳에서"},{"조형관(제3합동강의동)","내가 있는 곳에서 "},{"제4합동강의동","내가 있는 곳에서 "},
            {"법과대학","내가 있는 곳에서 "},{"공대1호관","내가 있는 곳에서 "},{"공대2호관","내가 있는 곳에서 "},{"공대3호관","내가 있는 곳에서 "},{"공대6호관","내가 있는 곳에서 "},
            {"공대9호관","내가 있는 곳에서 "},{"IT대학3호관(공대11호관","내가 있는 곳에서 "},{"공대12호관","내가 있는 곳에서 "},{"농대2호관","내가 있는 곳에서 "},{"농대3호관","내가 있는 곳에서 "},
            {"예술대학","내가 있는 곳에서 "},{"조소동","내가 있는 곳에서 "},{"생명공학관(교양과정동)","내가 있는 곳에서 "},{"사범대학","내가 있는 곳에서 "},{"생활과학대학","내가 있는 곳에서 "},
            {"공동실험실습관","내가 있는 곳에서 "},{"학생종합서비스센터","내가 있는 곳에서 "},{"교수아파트","내가 있는 곳에서 "},{"외국어교육관","내가 있는 곳에서 "},{"국제경상관(경상정보교육관)","내가 있는 곳에서 "},
            {"국민체육센터","내가 있는 곳에서 "},{"DGB문화센터(평생교육원)","내가 있는 곳에서 "},{"학생생활관(첨성관)","내가 있는 곳에서 "},{"KNU글로벌프라자","내가 있는 곳에서 "},{"KNU 어린이집","내가 있는 곳에서 "},
            {"IT대학융복합공학관","내가 있는 곳에서 "},{"인문한국진흥관","내가 있는 곳에서 "},{"대강당","내가 있는 곳에서 "},{"복현회관","장애학생지원센터(102호)는 "},{"제1과학관","내가 있는 곳에서 "},
            {"경상대학","내가 있는 곳에서 "},{"공대7호관","내가 있는 곳에서 "},{"공대8호관","내가 있는 곳에서 "},{"농대1호관","내가 있는 곳에서 "},{"교육대학원","내가 있는 곳에서 "},
            {"동물병원","내가 있는 곳에서 "},{"학군단","내가 있는 곳에서 "},{"출판부","내가 있는 곳에서 "},{"창업보육센터","내가 있는 곳에서 "},{"공대2A호관","내가 있는 곳에서 "},
            {"자연과학대학","내가 있는 곳에서 "},{"향토생활관","내가 있는 곳에서 "},{"봉사관","내가 있는 곳에서 "},{"진리관","내가 있는 곳에서 "},{"화목관","내가 있는 곳에서 "},
            {"제1체육관","내가 있는 곳에서 "},{"제2체육관","내가 있는 곳에서 "},{"청룡관","내가 있는 곳에서 "},{"반도체연구동","내가 있는 곳에서 "},{"대학원동","내가 있는 곳에서 "},
            {"IT대학1호관(공대10호관)","내가 있는 곳에서 "},{"수의과대학","내가 있는 곳에서 "},{"성실관","내가 있는 곳에서 "},{"긍지관","내가 있는 곳에서 "},{"협동관","내가 있는 곳에서 "},
            {"면학관","내가 있는 곳에서 "},{"기숙사문화관","내가 있는 곳에서 "},{"공대4호관","내가 있는 곳에서 "},{"IT대학2호과(공대5호관)","내가 있는 곳에서 "},{"농대사과센터","내가 있는 곳에서 "},
            {"우당교육관","내가 있는 곳에서 "},{"P/P,공대구조실습실","내가 있는 곳에서 "},{"도서관휴게실","내가 있는 곳에서 "},{"취업정보센터","내가 있는 곳에서 "}
    };
    private static Double[][] corddata = {
            {35.890434, 128.612025},{35.891794, 128.612055},{35.888711, 128.613693},{35.891407, 128.613582},{35.892298, 128.613192},
            {35.888989, 128.614468},{35.888353, 128.604193},{35.891212, 128.610720},{35.888434, 128.615443},{35.889838, 128.606414},
            {35.886574, 128.608444},{35.886879, 128.606111},{35.892628, 128.612386},{35.893295, 128.612387},{35.889601, 128.614886},
            {35.887989, 128.614941},{35.887599, 128.608526},{35.887988, 128.608526},{35.887701, 128.609671},{35.887316, 128.609623},
            {35.886879, 128.608502},{35.888203, 128.610917},{35.888496, 128.610271},{35.891013, 128.608246},{35.891463, 128.608503},
            {35.893539, 128.611140},{35.893951, 128.612192},{35.889790, 128.609058},{35.890290, 128.613768},{35.889906, 128.615887},
            {35.886797, 128.607249},{35.890666, 128.611776},{35.885540, 128.609246},{35.890976, 128.614468},{35.889403, 128.615887},
            {35.890210, 128.605831},{35.892683, 128.609857},{35.891424, 128.614908},{35.891809, 128.611004},{35.890990, 128.607447},
            {35.888175, 128.611366},{35.889999, 128.610788},{35.892802, 128.610732},{35.890822, 128.607083},{35.889840, 128.607832},
            {35.889157, 128.615776},{35.887292, 128.610678},{35.886684, 128.611645},{35.891261, 128.609529},{35.889904, 128.613804},
            {35.886689, 128.613650},{35.887128, 128.604916},{35.890518, 128.608138},{35.886378, 128.612580},{35.888166, 128.607698},
            {35.890286, 128.606632},{35.890705, 128.615231},{35.886845, 128.609883},{35.886867, 128.609414},{35.886852, 128.610701},
            {35.889294, 128.604644},{35.889858, 128.605229},{35.888024, 128.605991},{35.887801, 128.612362},{35.889684, 128.610233},
            {35.887452, 128.612737},{35.886770, 128.613220},{35.885864, 128.608554},{35.886509, 128.609636},{35.886522, 128.610698},
            {35.886351, 128.611415},{35.886022, 128.609603},{35.887674, 128.610636},{35.887520, 128.611645},{35.890822, 128.609053},
            {35.890130, 128.615113},{35.887710, 128.607711},{35.891446, 128.612726},{35.890771, 128.612300}
    };

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소에 위치 퍼미션 허용

    Location mCurrentLocatiion;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    String distance;
    private AlertDialog dialog;
    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.

    String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_htrret);

        final CheckBox cb1 = (CheckBox)findViewById(R.id.check1);
        final CheckBox cb2 = (CheckBox)findViewById(R.id.check2);
        final CheckBox cb3 = (CheckBox)findViewById(R.id.check3);
        final CheckBox cb4 = (CheckBox)findViewById(R.id.check4);
        Button lc = (Button)findViewById(R.id.locationbutton);

        // end onClick
        lc.setOnClickListener(v -> {
            mcf = 0;
            mci2 = 0;
            mcf2 = 0;
            mci3 = 0;
            mcf3 =0;
            mcsum = 0;
            if(cb1.isChecked()) {
                mcf = 79;
                mcsum = mcf;
            }
            if(cb2.isChecked()) {
                mcf = 42;
                mci2 = 55;
                mcf2 = 57;
                mci3 = 64;
                mcf3 = 67;
                mcsum = mcf+mcf2+mcf3-mci2-mci3;
            }
            if(cb3.isChecked()) {
                mcf = 64;
                mcsum = mcf;
            }
            if(cb4.isChecked()) {
                mcf = 55;
                mcsum = mcf;
            }
            if(cb1.isChecked()&&cb2.isChecked()) {
                mcf = 42;
                mci2 = 55;
                mcf2 = 57;
                mci3 = 64;
                mcf3 = 67;
                mcsum = mcf+mcf2+mcf3-mci2-mci3;
            }
            if(cb1.isChecked()&&cb3.isChecked()) {
                mcf = 64;
                mcsum = mcf;
            }
            if(cb1.isChecked()&&cb4.isChecked()) {
                mcf = 55;
                mcsum = mcf;
            }
            if(cb1.isChecked()&&cb2.isChecked()&&cb3.isChecked()) {
                mcf = 42;
                mci2 = 55;
                mcf2 = 57;
                mcsum = mcf+mcf2-mci2;
            }
            if(cb1.isChecked()&&cb2.isChecked()&&cb4.isChecked()) {
                mcf = 42;
                mcsum = mcf;
            }
            if(cb1.isChecked()&&cb3.isChecked()&&cb4.isChecked()) {
                mcf = 55;
                mcsum = mcf;
            }
            if(cb2.isChecked()&&cb3.isChecked()&&cb4.isChecked()) {
                mcf = 42;
                mcsum = mcf;
            }
            if(cb1.isChecked()&&cb2.isChecked()&&cb3.isChecked()&&cb4.isChecked()){
                mcf = 42;
                mcsum = mcf;
            }
            Toast.makeText(getApplicationContext(), "다음 정보에 반영됩니다. 해당하는 건물은 "+mcsum+"곳입니다.", LENGTH_SHORT).show();


        });


        mLayout = findViewById(R.id.layout_main);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //채팅을 요구 하는 아아디 즉 단말기에 로그인된 UID

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
                        ActivityCompat.requestPermissions( HtrretActivity.this, REQUIRED_PERMISSIONS,
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


    final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);


                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.clear();


                for (int idx = 0; idx < mcf; idx++) {
                    distance = calcDistance(location.getLatitude(), location.getLongitude(), corddata[idx][0], corddata[idx][1]);
                    // 1. 마커 옵션 설정 (만드는 과정)
                    MarkerOptions makerOptions = new MarkerOptions();
                    makerOptions // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
                            .position(new LatLng(corddata[idx][0], corddata[idx][1]))
                            .title(mapdata[idx][0]) // 타이틀.
                            .snippet(mapdata[idx][1] + distance + "에 있습니다.")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.makercha10));
                    // 2. 마커 생성 (마커를 나타냄)
                    mMap.addMarker(makerOptions);
                }
                for (int idx = mci2; idx < mcf2; idx++) {
                    distance = calcDistance(location.getLatitude(), location.getLongitude(), corddata[idx][0], corddata[idx][1]);
                    // 1. 마커 옵션 설정 (만드는 과정)
                    MarkerOptions makerOptions = new MarkerOptions();
                    makerOptions // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
                            .position(new LatLng(corddata[idx][0], corddata[idx][1]))
                            .title(mapdata[idx][0]) // 타이틀.
                            .snippet(mapdata[idx][1] + distance + "에 있습니다.")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.makercha10));
                    // 2. 마커 생성 (마커를 나타냄)
                    mMap.addMarker(makerOptions);
                }
                for (int idx = mci3; idx < mcf3; idx++) {
                    distance = calcDistance(location.getLatitude(), location.getLongitude(), corddata[idx][0], corddata[idx][1]);
                    // 1. 마커 옵션 설정 (만드는 과정)
                    MarkerOptions makerOptions = new MarkerOptions();
                    makerOptions // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
                            .position(new LatLng(corddata[idx][0], corddata[idx][1]))
                            .title(mapdata[idx][0]) // 타이틀.
                            .snippet(mapdata[idx][1] + distance + "에 있습니다.")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.makercha10));
                    // 2. 마커 생성 (마커를 나타냄)
                    mMap.addMarker(makerOptions);
                }

                MarkerOptions makerOptions3 = new MarkerOptions();
                makerOptions3 // LatLng에 대한 어레이를 만들어서 이용할 수도 있다.
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("내 위치")
                        .snippet("위도:" + location.getLatitude() + " 경도:" + location.getLongitude());



                // 2. 마커 생성 (마커를 나타냄)
                mMap.addMarker(makerOptions3);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));


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

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 19);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(HtrretActivity.this);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(HtrretActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

}




