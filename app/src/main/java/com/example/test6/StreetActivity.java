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


    private static final String TAG = "????????? ?????? ??????";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 4000;  // 2????????? ????????????
    private static final int FASTEST_UPDATE_INTERVAL_MS = 2000; // 1????????? ????????????
    private static final int PERMISSIONS_REQUEST_CODE = 200; //0.1????????? ????????????
    boolean needRequest = false;
    private Button btn_meet,btn_chat;
    private Context mContext;

    // ?????? ???????????? ?????? ????????? ???????????? ???????????????.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // ?????? ???????????? ?????? ????????? ??????

    Location mCurrentLocatiion;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    String distance, ableok;
    private String Remote="0";
    private double mlat=35.890434,mlng= 128.612025,mlat2=35.890434,mlng2= 128.612025;

    private View mLayout;  // Snackbar ???????????? ???????????? View??? ???????????????.

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

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //????????? ?????? ?????? ????????? ??? ???????????? ???????????? UID
        if( getIntent().getStringExtra("destinationUid")!=null){
            destinatonUid = getIntent().getStringExtra("destinationUid"); // ????????? ????????? ?????????
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
                makerOptions1 // LatLng??? ?????? ???????????? ???????????? ????????? ?????? ??????.
                        .position(new LatLng(mlat,mlng))
                        .title("??? ??????")
                        .snippet("??????:" + mlat + " ??????:" + mlng);


                // 2. ?????? ?????? (????????? ?????????)
                mMap.addMarker(makerOptions1);

                MarkerOptions makerOptions2 = new MarkerOptions();
                makerOptions2 // LatLng??? ?????? ???????????? ???????????? ????????? ?????? ??????.
                        .position(new LatLng(mlat2, mlng2))
                        .title("????????????")
                        .snippet("???????????????. ??????"+distance+"??? ????????? ????????????.")
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
                        btn_meet.setText("?????? ??????! ????????? ???????????????.");
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
                                        FirebaseDatabase.getInstance().getReference().child("helpcall").child(uid).child("matching").setValue("????????????");
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
                    btn_meet.setText("???????????? "+distance+"?????? ?????????????????????.");
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
                if (ableok.equals("?????????")) {
                    btn_meet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                FirebaseDatabase.getInstance().getReference().child("helpcall").child(uid).child("matching").setValue("????????????");
                                PreferenceManager.setLong(mContext, "Meettime",0 );
                                Toast.makeText(getApplicationContext(), "????????? ?????????????????????!", LENGTH_SHORT).show();
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
                            if(chatok.equals("????????????")||chatok.equals(("??????"))){
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
                            Toast.makeText(getApplicationContext(), "????????? ?????????????????????! \n?????? ?????? ????????? ????????????.", LENGTH_SHORT).show();
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
                            if(chatok.equals("????????????")||chatok.equals(("??????"))){
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



        //????????? ????????? ?????? ??????????????? GPS ?????? ?????? ???????????? ???????????????
        //????????? ??????????????? ?????????????????? ??????
        setDefaultLocation();

        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ??????
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {

            // 2. ???????????? ?????? ????????????

            startLocationUpdates(); // 3. ?????? ???????????? ??????

        }else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.

            // 3-1. ???????????? ????????? ????????? ??????
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ??????
                Snackbar.make(mLayout, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.",
                        Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ??????
                        ActivityCompat.requestPermissions( StreetActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();

            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ??????
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
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
                makerOptions3 // LatLng??? ?????? ???????????? ???????????? ????????? ?????? ??????.
                        .position(new LatLng(location.getLatitude(),location.getLongitude()))
                        .title("??? ??????")
                        .snippet("??????:" + location.getLatitude() + " ??????:" + location.getLongitude());


                // 2. ?????? ?????? (????????? ?????????)
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

                Log.d(TAG, "startLocationUpdates : GPS ????????? ?????? ????????????.");
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
        //????????????... GPS??? ????????? ??????
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
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


        //????????? ??????, ???????????????
        LatLng DEFAULT_LOCATION = new LatLng(35.89, 128.61);
        String markerTitle = "???????????? ????????? ??? ??????";
        String markerSnippet = "?????? ???????????? GPS ?????? ????????? ???????????????";

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

    //??????????????? ????????? ????????? ????????? ?????? ????????????
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
     * ActivityCompat.requestPermissions??? ????????? ????????? ????????? ????????? ???????????? ??????????????????.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ????????????

            boolean check_result = true;

            // ?????? ???????????? ??????????????? ??????

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {
                // ???????????? ??????????????? ?????? ??????????????? ??????
                startLocationUpdates();
            }
            else {
                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // ???????????? ????????? ????????? ???????????? ?????? ?????? ???????????? ????????? ???????????? ?????? ????????? ??? ????????????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }else {
                    // "?????? ?????? ??????"??? ???????????? ???????????? ????????? ????????? ???????????? ??????(??? ??????)?????? ???????????? ???????????? ?????? ????????? ??? ????????????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }
    //??????????????? GPS ???????????? ?????? ?????????
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StreetActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ?????????????????????????");
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
                        Log.d(TAG, "onActivityResult : GPS ????????? ??????????????????.");
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }

    //?????? ????????? ????????? ??????????????????.
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
        Toast.makeText(this, "??????????????? ??????????????????.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(StreetActivity.this,MainActivity.class);
        intent.putExtra("destinationUid",destinatonUid);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}


