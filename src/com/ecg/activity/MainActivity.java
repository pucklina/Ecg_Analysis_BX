package com.ecg.activity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.cengalabs.flatui.views.FlatToggleButton;
import com.ecg.MyApplication;
import com.ecg_analysis.R;

import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import info.hoang8f.widget.FButton;

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {

    private String LOG_HEAD = "Activity_Main";

    protected static final int REQUEST_ENABLE_BT = 1;
    private static final int SCAN_PERIOD = 1000;

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;


    boolean x;
    @Background
    void ConnectDevice(){
        if(!flagForBLEConnecting&&mBluetoothLeService!=null&&flagForIfConnectAuto) {
            x = mBluetoothLeService.connect(MyApplication.BLEDeviceName,true);
            System.out.println(x);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ConnectDevice();
            }
        };
        timer.schedule(task,0, 2*1000);

        SharedPreferences sharedPreferences = getSharedPreferences("DeviceAddress",MODE_PRIVATE);
        if(sharedPreferences!=null){
            MyApplication.BLEDeviceName = sharedPreferences.getString("DeviceAddress","");

        }

        Log.e("MainActivity","onCreat");
        System.out.println("MyApplication.BLEDeviceName" + MyApplication.BLEDeviceName);
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "设备不支持低功耗蓝牙",Toast.LENGTH_SHORT).show();
            finish();
        }

        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "设备不支持低功耗蓝牙",Toast.LENGTH_SHORT).show();
            finish();
        }
    }



    @AfterViews
    public void init(){
        Log.e("FirstToLast","init");
        Log.e("FirstToLast","finished");
    }

    @ViewById
    ListView service_list;

    @ViewById
    TextView MainActivity_TextView_ShowConnectState;

    @ItemClick
    void service_list(int position){
        try {
            final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
            if (device == null) return;
            final Intent intent = new Intent(this, ShowActivity_.class);

            intent.putExtra(ShowActivity_.EXTRAS_DEVICE_ADDRESS, device.getAddress());
            intent.putExtra(ShowActivity_.EXTRAS_ENTER_METHOD,"EXTRAS_ENTER_METHOD_1");
            //Log.w("test", device.getName() + device.getAddress());

            if (mScanning) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mScanning = false;
            }
            startActivity(intent);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @ViewById
    FButton main_connect;

    @ViewById
    FButton main_cancel;

    @ViewById
    LinearLayout linerlayout_main_connect;

    @ViewById
    RelativeLayout linerlayout_main;
    @ViewById
    LinearLayout linerlayout_main_text;
    @ViewById
    LinearLayout  linerlayout_main_connect_button;

    @Click
    void main_connect(){
        System.out.println(MyApplication.BlEServiceStart);
        linerlayout_main_connect.setVisibility(View.VISIBLE);
        linerlayout_main.setVisibility(View.GONE);
        linerlayout_main_text.setVisibility(View.VISIBLE);
        linerlayout_main_connect_button.setVisibility(View.VISIBLE);
        main_connect.setEnabled(false);
        main_cancel.setEnabled(true);
        scanLeDevice(true);
    }

    @Click
    void MainActivity_Button_ConnectAtNow(){
        if(flagForBLEConnecting){
            final Intent intent = new Intent(this, ShowActivity_.class);
            intent.putExtra(ShowActivity_.EXTRAS_DEVICE_ADDRESS, MyApplication.BLEDeviceName);
            intent.putExtra(ShowActivity_.EXTRAS_ENTER_METHOD,"EXTRAS_ENTER_METHOD_2");
            startActivity(intent);
        }
        else Toast.makeText(MainActivity.this, "您的默认设备并没有连接成功，请扫描并选择设备连接",Toast.LENGTH_LONG).show();
    }

    @Click
    void main_cancel(){
        linerlayout_main_connect.setVisibility(View.GONE);
        linerlayout_main.setVisibility(View.VISIBLE);
        linerlayout_main_text.setVisibility(View.GONE);
        linerlayout_main_connect_button.setVisibility(View.GONE);
        main_cancel.setEnabled(false);
        main_connect.setEnabled(true);
        //scanLeDevice(false);
    }

    @Background(delay=SCAN_PERIOD)
    void scanLeDevice(final boolean enable) {

        if (enable) {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                            Log.w(LOG_HEAD, "Device has been scanned"+device.getName()+device.getAddress());
                        }
                    });
                }
            };



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private BluetoothLeService mBluetoothLeService;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Log.i("information", "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(MyApplication.BLEDeviceName,true);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private boolean flagForBLEConnecting = false;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                MainActivity_TextView_ShowConnectState.setText(getResources().getString(R.string.MainActivity_String_DeviceAddressWhichOnConnect)+" "+MyApplication.BLEDeviceName);
                flagForBLEConnecting = true;
            }
            if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                MainActivity_TextView_ShowConnectState.setText(R.string.main_bt_unconnect);
                flagForBLEConnecting = false;
            }
        }
    };
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        //2015 4 12 add to detect bluetooth is disconnect
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.EXTRA_DATA_BLUETOOTH);
        return intentFilter;
    }


    private boolean flagForBindService = false;

    protected void onResume() {
        super.onResume();

        Log.e("MainActivity", "onResume");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        linerlayout_main_connect.setVisibility(View.GONE);
        linerlayout_main.setVisibility(View.VISIBLE);
        linerlayout_main_text.setVisibility(View.GONE);
        linerlayout_main_connect_button.setVisibility(View.GONE);
        main_cancel.setEnabled(false);
        main_connect.setEnabled(true);
        System.out.println(MyApplication.BlEServiceStart);


            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            startService(gattServiceIntent);
        flagForBindService=bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        }
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        service_list.setAdapter(mLeDeviceListAdapter);
    }

    protected void onPause() {
        super.onPause();
        Log.e("MainActivity","onPause");
        mLeDeviceListAdapter.clear();


    }
    public void onDestroy() {
        super.onDestroy();
        Log.e("MainActivity","onDestory");

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        stopService(gattServiceIntent);
    }

    public void onStop() {
        super.onStop();
        Log.e("MainActivity","onStop");
        unregisterReceiver(mGattUpdateReceiver);
        if(flagForBindService)  {

            unbindService(mServiceConnection);
            flagForBindService = false;
        }

    }

    public void onStart() {
        super.onStart();
        Log.e("MainActivity","onStart");
    }

    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
            mInflator = MainActivity.this.getLayoutInflater();
        }


        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }


        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }


        public void clear() {
            mLeDevices.clear();
        }

        public int getCount() {
            return mLeDevices.size();
        }

        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        public long getItemId(int i) {
            return i;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText("Unknown device");
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    @ViewById
    ToggleButton MainActivity_ToggleButton_IfConnectAuto;


    boolean flagForIfConnectAuto = true;
    @CheckedChange
    void MainActivity_ToggleButton_IfConnectAuto(){
        if(MainActivity_ToggleButton_IfConnectAuto.isChecked()){
            Toast.makeText(MainActivity.this, "已经关闭", Toast.LENGTH_SHORT).show();
           // unregisterReceiver(mGattUpdateReceiver);

            mBluetoothLeService.disconnect();
            mBluetoothLeService.connect(MyApplication.BLEDeviceName,false);
            mBluetoothLeService.disconnect();
            flagForIfConnectAuto = false;
        }else{
            Toast.makeText(MainActivity.this, "已经开启", Toast.LENGTH_SHORT).show();
          //  registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            mBluetoothLeService.connect(MyApplication.BLEDeviceName,true);
            flagForIfConnectAuto = true;
        }

    }

}
