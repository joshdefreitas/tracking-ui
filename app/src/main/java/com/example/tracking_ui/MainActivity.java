package com.example.tracking_ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

//plotting graph imports
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{


    private static final String TAG = "MainActivity";
    private Button startButton,btnONOFF,enableDiscoverable, btnDiscover, btnStartConnection, btnCalibrate, savebtn;

    BluetoothConnectionService mBluetoothConnection;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothDevice mBTDevice;

    BluetoothAdapter mBluetoothAD;
    public ArrayList<BluetoothDevice> BTDevices = new ArrayList<>();
    public DeviceListAdapter DeviceListAdapter;
    ListView lvNewDevices;

    //Plotting graph
    Point currentLocation = new Point(0, 0, 0);
    Point savedLocation = new Point(0,0,0);
    StringBuilder messages;
    WebView webView;
    ArrayList<Point> ptsList;

    /*
    *****************     BROADCAST RECEIVERS     ****************
     */

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAD.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAD.ERROR);

                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"onRecieve: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }

            }
        }
    };

    // Creater a BroadcastReceiver for ACTION SCAN
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, mBluetoothAD.ERROR);

                switch (mode){
                    //Device is in Discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG,"mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in Discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG,"mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG,"mBroadcastReceiver: Connecting.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG,"mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };

    // Creater a BroadcastReceiver for ACTION_FOUND
    private  BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                BTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                DeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, BTDevices);
                lvNewDevices.setAdapter(DeviceListAdapter);
            }
        }
    };

    // Broadcast receiver for inputStream message
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            messages.append(text);

            try {
                String [] splitString = text.split("\\s+");
                currentLocation = new Point(Integer.parseInt(splitString[0]),Integer.parseInt(splitString[0]),Integer.parseInt(splitString[0]));
                plotGraph();
            }catch (Exception e){
                Log.d(TAG, "Error parsing location: " + e.toString());
            }


        }
    };

    //
    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    mBTDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    /*
     ********************     ONCREATE & ONDESTROY METHODS    *****************
     */

    @Override
    protected void onDestroy(){

        Log.d(TAG, "onDestroy: called.");
        try{
            if(mBroadcastReceiver1 != null) {
                this.unregisterReceiver(mBroadcastReceiver1);

            }
        } catch (Exception e){
            // already unregistered
        }
        try{
            if(mBroadcastReceiver2 != null) {
                this.unregisterReceiver(mBroadcastReceiver1);

            }
        } catch (Exception e){
            // already unregistered
        }
        try{
            if(mBroadcastReceiver3 != null) {
                this.unregisterReceiver(mBroadcastReceiver1);

            }
        } catch (Exception e){
            // already unregistered
        }
        try{
            if(mBroadcastReceiver4 != null) {
                this.unregisterReceiver(mBroadcastReceiver1);

            }
        } catch (Exception e){
            // already unregistered
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove action bar
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_main);
        //change orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        messages = new StringBuilder();

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));


        mBluetoothAD = BluetoothAdapter.getDefaultAdapter();

        savebtn = findViewById(R.id.savebtn);
        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savedLocation = new Point(currentLocation.getX(),currentLocation.getY(),currentLocation.getYaw());
                plotGraph();
            }
        });

        btnStartConnection = findViewById(R.id.connectionBtn);
        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });

        btnONOFF = findViewById(R.id.btnONOFF);
        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: enable/disable bluetooth.");
                enableDisableBT();
            }
        });

        btnCalibrate = findViewById(R.id.btnCalibrate);
        btnCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calibrateCamera();
            }
        });

        startButton = findViewById(R.id.button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: start");
                startAlgorithm();
            }
        });

        enableDiscoverable = findViewById(R.id.button2);
        btnDiscover = findViewById(R.id.button4);
        lvNewDevices = findViewById(R.id.lvNewDevices);
        lvNewDevices.setOnItemClickListener(MainActivity.this);
//        lvNewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                onItemClick(adapterView,view,i,l);
//            }
//        });
        BTDevices = new ArrayList<>();

        //Broadcasts when bond state changes
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);


        //Plotting graph initializations
        plotGraph();
    }




    /*
    ********************     BLUETOOTH METHODS     *****************
     */
    //start service method
    public void  startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM BT connection and starting tracking fragment");
        mBluetoothConnection.startClient(device,uuid);
    }

    //Onclick method for start button
    public void startAlgorithm(){
        byte[] bytes = "start".getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
        plotGraph();
    }

    //Onclick method to calibrate camera
    public void calibrateCamera(){
        byte[] bytes = "camera 0".getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }
    //method for starting connection
    public void startConnection(){
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    //Onclick method for bluetooth button
    public void enableDisableBT(){
        if(mBluetoothAD == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities");
        }
        if(!mBluetoothAD.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if(mBluetoothAD.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAD.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }

    //Onclick method for bluetooth discoverability
    public void enableDisableDiscoverable(View view) {
        Log.d(TAG,"enableDisableDiscoverable: Making device discoverable for 300 seconds");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAD.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);
    }

    // Discover unpaired devices
    public void Discover(View view) {
        Log.d(TAG,"Discover: Looking for unpaired devices");

        if(mBluetoothAD.isDiscovering()){
            mBluetoothAD.cancelDiscovery();
            Log.d(TAG,"Discover: Cancelling discovery");

            // Check BT permissions in mainifest
            checkBTPermissions();

            mBluetoothAD.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAD.isDiscovering()){

            // Check BT permissions in manifest
            checkBTPermissions();

            mBluetoothAD.startDiscovery();
            mBluetoothAD.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }

    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    @Override
    @TargetApi(19)
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        mBluetoothAD.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = BTDevices.get(i).getName();
        String deviceAddress = BTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //create the bond.
        //NOTE: Requires API 17+? I think this is JellyBean
       if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            BTDevices.get(i).createBond();
            mBTDevice = BTDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
            Toast.makeText(MainActivity.this, "Pairing to: "+deviceName,
                    Toast.LENGTH_LONG).show();

        }
    }

    /*
     ********************     PLOTTING GRAPH METHODS    *****************
     */
    public void plotGraph(){
        webView = (WebView)findViewById(R.id.web2);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setPadding(0, 0, 0, 10);

        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/chart.html");
    }

    public class WebAppInterface {

        @JavascriptInterface
        public double getNum1() {
            return currentLocation.getX();
        }

        @JavascriptInterface
        public double getNum2() {
            return currentLocation.getY();
        }

        @JavascriptInterface
        public double getNum3() {
            return savedLocation.getX();
        }

        @JavascriptInterface
        public double getNum4() {
            return savedLocation.getY();
        }

    }




}
