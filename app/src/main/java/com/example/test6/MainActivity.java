package com.example.test6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test6.model.GpsModel;
import com.example.test6.model.HelpModel;
import com.example.test6.model.NotificationModel;
import com.example.test6.model.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.os.Vibrator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity {
    private GpsTracker gpsTracker;
    Integer mTvBluetoothStatus;
    private Vibrator vibrator;
    private TextView textview;
    private FirebaseAuth firebaseauth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    private String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();  //????????? ?????? ?????? ????????? ??? ???????????? ???????????? UID
    private String ableok,ableokk, matchok = "0",remoteok ="0";
    private String destinatonUid, destest,destinationuid;
    private Button btn_help;
    private String bluename="0",time;
    private ArrayAdapter<String> mBTArrayAdapter;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ListView lstvw;
    private ArrayAdapter aAdapter;
    private Context Waitcontext,mContext;
    Button mBtnConnect;
    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;
    BluetoothAdapter mBluetoothAdapter;
    List<String> mListPairedDevices;;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd HH:mm");



    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Integer k=1,y=0;
    private String remote;
    String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();


    private static final int REQUEST_ENABLE_BT = 10; // ???????????? ????????? ??????
    private BluetoothAdapter bluetoothAdapter; // ???????????? ?????????
    private Set<BluetoothDevice> devices; // ???????????? ???????????? ????????? ???
    private BluetoothDevice bluetoothDevice; // ???????????? ????????????
    String readMessage = null;

    LocationManager locationManager;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        passPushTokenToServer();
        btn_help = findViewById((R.id.helpbutton));
        btn_help.setEnabled(false);

        Waitcontext = this;
        mContext = this;
        readMessage = null;

        long WT = PreferenceManager.getLong(Waitcontext, "Waittime");


        TextView textview = (TextView) findViewById(R.id.Welcomemessage);


        Log.d(this.getClass().getName(), (String) textview.getText());
        textview.setText(userName+"??? ???????????????.");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mBluetoothHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == BT_MESSAGE_READ) {
                    readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if (readMessage != null) {
                        FirebaseDatabase.getInstance().getReference().child("RemoteController").child(uid).setValue("yes").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                connectSelectedDevice("yena");
                                Intent i = new Intent(MainActivity.this,AskhelpActivity.class);
                                startActivity(i);
                                ActivityCompat.finishAffinity(MainActivity.this);
                                finish();
                            }
                        });
                    }
                }

            }
        };

        if (getIntent().getStringExtra("destinationUid") != null) {
            destinatonUid = getIntent().getStringExtra("destinationUid"); // ????????? ????????? ?????????
        } else {
            destinatonUid = "0";
        }

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("users").child(uid).child("Disable").getValue() != null){
                    ableok = dataSnapshot.child("users").child(uid).child("Disable").getValue().toString();
                }
                if (dataSnapshot.child("helpcall").child(uid).child("matching").getValue() != null) {
                    matchok = dataSnapshot.child("helpcall").child(uid).child("matching").getValue().toString();
                }

                if (ableok.equals("?????????")) {



                    if(dataSnapshot.child("helpcall").child(uid).child("destinationuid").getValue() != null) {
                        destinatonUid = dataSnapshot.child("helpcall").child(uid).child("destinationuid").getValue().toString();
                    }
                    final Button helpButton = (Button) findViewById(R.id.helpbutton);
                    if(matchok.equals("????????????")) {
                        helpButton.setText("?????? ?????? ??????");
                        helpButton.setEnabled(true);
                        helpButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

                                if (status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
                                    Toast.makeText(getApplicationContext(), "????????? ?????? ?????????????????? ???????????????.", LENGTH_SHORT).show();
                                    Intent i = new Intent(MainActivity.this, HelpwaitActivity.class);
                                    startActivity(i);
                                } else {
                                    Toast.makeText(getApplicationContext(), "????????? ????????? ????????? ?????????", LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else if (matchok.equals("??????")||matchok.equals("????????????")) {
                        helpButton.setText("????????? ?????? ??????");
                        helpButton.setEnabled(true);
                        helpButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

                                if (status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
                                    Toast.makeText(getApplicationContext(), "????????? ????????? ???????????? ???????????????.", LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, StreetActivity.class);
                                    intent.putExtra("destinationUid", destinatonUid);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "????????? ????????? ????????? ?????????", LENGTH_SHORT).show();
                                }

                            }
                        });
                    } else if (matchok.equals("????????????")) {
                        readMessage = null;

                        helpButton.setText("?????? ??? ??????");
                        helpButton.setEnabled(true);
                        helpButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

                                if (status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {

                                    Toast.makeText(getApplicationContext(), "?????? ??? ?????????????????? ???????????????.", LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, MeetActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "????????? ????????? ????????? ?????????", LENGTH_SHORT).show();
                                }
                            }
                        });

                    }

                    else {
                        helpButton.setText("?????? ??????");
                        helpButton.setEnabled(true);
                        helpButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

                                if (status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
                                        Toast.makeText(getApplicationContext(), "?????? ????????? ?????????????????????.", LENGTH_SHORT).show();
                                        Intent i = new Intent(MainActivity.this, AskhelpActivity.class);
                                        startActivity(i);
                                        finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "????????? ????????? ????????? ?????????", LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                } else {
                    final Button helpButton = (Button) findViewById(R.id.helpbutton);
                    if (dataSnapshot.child("answercall").child(uid).child("answer").getValue() != null) {
                        destinatonUid = dataSnapshot.child("answercall").child(uid).child("answer").getValue().toString();
                    }
                    if (dataSnapshot.child("helpcall").child(destinatonUid).child("matching").getValue() != null) {
                        matchok = dataSnapshot.child("helpcall").child(destinatonUid).child("matching").getValue().toString();
                    }

                    if (matchok.equals("??????")||matchok.equals("????????????")) {
                        helpButton.setText("????????? ?????? ??????");
                        helpButton.setEnabled(true);
                        helpButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

                                if (status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
                                    Toast.makeText(getApplicationContext(), "????????? ????????? ???????????? ???????????????..", LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, StreetActivity.class);
                                    intent.putExtra("destinationUid", destinatonUid);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "????????? ????????? ????????? ?????????", LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        helpButton.setText("?????? ?????? ??????");
                        helpButton.setEnabled(true);
                        helpButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

                                if (status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
                                    Toast.makeText(getApplicationContext(), "?????? ?????? ????????? ?????????????????????.", LENGTH_SHORT).show();
                                    Intent i = new Intent(MainActivity.this, HelpcallActivity.class);
                                    startActivity(i);
                                } else {
                                    Toast.makeText(getApplicationContext(), "????????? ????????? ????????? ?????????", LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void bluetoothbutton(View v){
        myRef.child("users").child(uid).child("Disable").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String blue = dataSnapshot.getValue().toString();
                if(blue.equals("?????????")){
                    listPairedDevices();
                }else{

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void communitybutton(View v) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

        if (status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
            Toast.makeText(getApplicationContext(), "?????? ???????????? ???????????? ?????????????????????.", LENGTH_SHORT).show();
            Intent i = new Intent(MainActivity.this, WritingsActivity.class);
            startActivity(i);
        } else {
            Toast.makeText(getApplicationContext(), "????????? ????????? ????????? ?????????", LENGTH_SHORT).show();
        }
    }

    public void announcebutton(View v) {
        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());

        if (status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
            Toast.makeText(getApplicationContext(), "?????????????????? ????????? ?????????????????????.", LENGTH_SHORT).show();
            Intent i = new Intent(MainActivity.this, HtrretActivity.class);
            startActivity(i);
        } else {
            Toast.makeText(getApplicationContext(), "????????? ????????? ????????? ?????????", LENGTH_SHORT).show();
        }
    }

    public void btn_logout(View v) {
        PopupMenu popup = new PopupMenu(getApplicationContext(), v);//v??? ????????? ?????? ??????

        getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.myaccount:
                        Toast.makeText(getApplication(), "?????? ??????????????? ???????????????.", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(MainActivity.this,ProfileActivity.class);
                        startActivity(i);
                        finish();
                        break;
                    case R.id.kindnes:
                        if (ableok.equals("?????????")) {
                            Toast.makeText(getApplication(), "?????? ????????? ?????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplication(), "?????? ??????????????? ???????????????.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this,GreatActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        break;
                    case R.id.logout:
                        Logout(v);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        popup.show();//Popup Menu ?????????
    }
    public void Logout(View v) {
        new AlertDialog.Builder(this)
                .setTitle("????????????").setMessage("???????????? ???????????????????")
                .setPositiveButton("????????????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                        intent.putExtra("value","logout");
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("pushToken").setValue("");
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })
                .show();
    }


    void passPushTokenToServer(){

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String,Object> map = new HashMap<>();
        map.put("pushToken",token);

        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);


    }
    @Override
// 1. ???????????? ????????? ????????????.
    public void onBackPressed() {
        // 2. ?????????????????? ????????????.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");
        builder.setMessage("?????? ?????????????????????????");
        builder.setNegativeButton("??????", null);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 3. ?????????????????? ?????? ???????????? ?????? ????????????.
                moveTaskToBack(true);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case BT_REQUEST_ENABLE:
                    if (resultCode == RESULT_OK) { // ???????????? ???????????? ????????? ??????????????????
                        Toast.makeText(getApplicationContext(), "???????????? ?????????", Toast.LENGTH_LONG).show();
                        listPairedDevices();
                    } else if (resultCode == RESULT_CANCELED) { // ???????????? ???????????? ????????? ??????????????????
                        Toast.makeText(getApplicationContext(), "??????", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        super.onActivityResult(requestCode, resultCode, data);
    }
    void listPairedDevices() {
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("?????? ????????? ????????????");
            builder.setMessage("???????????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                    + "?????? ????????? ???????????????????");
            builder.setCancelable(true);
            builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //GPS ?????????????????? ??????
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            builder.create().show();
            return;
        }
        gpsTracker = new GpsTracker(MainActivity.this);


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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //????????? ???????????? ????????? ??????????????? ???????????? ?????????
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "??????????????? ???????????? ?????? ???????????????.", Toast.LENGTH_LONG).show();
        }
        else {
            if (mBluetoothAdapter.isEnabled()) {
            } else {
                Toast.makeText(getApplicationContext(), "??????????????? ????????? ?????? ?????? ????????????.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();
            int k=0;

            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //builder.setTitle("?????? ??????");
                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                    if(device.getName().equals("yena")){
                        k=1;
                    }
                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                /*builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();*/
                if(k==1){
                    connectSelectedDevice("yena");

                }else{
                    Toast.makeText(getApplicationContext(), "???????????? ???????????? ?????????????????????.", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(getApplicationContext(), "???????????? ????????? ????????????.", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "??????????????? ???????????? ?????? ????????????.", Toast.LENGTH_SHORT).show();
        }
    }
    void connectSelectedDevice(String selectedDeviceName) {
        for(BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
        }
    }

    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Toast.makeText(getApplicationContext(), "?????? ????????? ??????????????????.", Toast.LENGTH_LONG).show();

            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "?????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();

            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "????????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "?????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
            }
        }
    }
    //???????????? ??????

}