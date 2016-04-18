package com.electrisole.team17.soleble;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DeviceControlActivity extends Activity {
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    private TextView mConnectionState;
    private TextView mMilesData;
    private TextView mCaloriesData;
    private TextView mDataField;
    private TextView mHangTime;
    private String mDeviceName;
    private String mDeviceAddress;
    //private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothLeService mBluetoothLeService2;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic writeChar;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private ExpandableListView mGattServicesList;
    //UUID chara = UUID.fromString("c97433f0-be8f-4dc8-b6f0-5343e6100eb4");
    private List<BluetoothGattService> servs=null;
    int step1 =0;
    int step2 =0;
    private Button hangTime;
    final Context context = this;
    private Button weight;
    int wght = 1;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int REQUEST_ENABLE_BT = 1;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);


        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mBluetoothLeService.connect("C6:39:87:17:90:CD");
                mConnected = true;
                updateConnectionState(R.string.connected);
                mConnectionState.setTextColor(Color.parseColor("#FF17AA00"));
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                mConnectionState.setTextColor(Color.parseColor("#ff0000"));
                invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                /*servs = mBluetoothLeService.getSupportedGattServices();
                mBluetoothLeService.readCharacteristic(servs.get(2).getCharacteristics().get(0));*/

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                if(intent.getStringExtra("Left")!=null){


                    step1 =Integer.valueOf(intent.getStringExtra("Left"));


                }

                if(intent.getStringExtra("Right")!=null){
                    //if(Integer.parseInt(intent.getStringExtra("Right"))>0){

                        step2 =Integer.valueOf(intent.getStringExtra("Right"));

                }
                //Log.d(TAG, "Sum " + step1 + " " + step2);
                mDataField.setText(String.valueOf(step1+step2));
                mMilesData.setText((Integer.parseInt(mDataField.getText().toString()) / 2250) + " miles");
                int stp = Integer.parseInt(mDataField.getText().toString());
                mCaloriesData.setText(stp/20 + " calories");
                mHangTime.setText(intent.getStringExtra("H"));
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        mHandler = new Handler();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        final Intent intent = getIntent();
        //mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        //mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceAddress = "E8:FF:34:49:A9:5B";
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data);
        mMilesData = (TextView) findViewById(R.id.miles);
        mCaloriesData= (TextView) findViewById(R.id.kcal);
        mHangTime = (TextView) findViewById(R.id.hang);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        /*hangTime = (Button) findViewById(R.id.ht);
        hangTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Button press");
                mBluetoothLeService.writeCharacteristic(
                        mBluetoothLeService.getSupportedGattServices().get(2).getCharacteristics().get(1));
                mHangTime.setText(intent.getStringExtra("H"));
            }
        });*/

        weight = (Button) findViewById(R.id.sw);
        weight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.weight_prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        if(!userInput.getText().toString().isEmpty()){
                                        wght = Integer.parseInt(userInput.getText().toString());

                                        }


                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }


    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);

        }
    }

    private void clearUI() {
       // mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }




    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void onClickWrite(View v) {
        Log.d(TAG, "Button press");
        if(mBluetoothLeService != null){
        //byte pepe = (byte) Integer.parseInt("1");
        //byte[] charLetra = new byte[1];
        //charLetra[0] = pepe;
        //mBluetoothLeService.getSupportedGattServices().get(2).getCharacteristics().get(1).setValue(charLetra);
        //mBluetoothLeService.writeCharacteristic(mBluetoothLeService.getSupportedGattServices().get(2).getCharacteristics().get(1));
        mBluetoothLeService.writeCharacteristic();
        if (mBluetoothLeService.writeCharacteristic() == false) {
            Log.w(TAG, "Failed to write characteristic");
        }
    }

    }


}
