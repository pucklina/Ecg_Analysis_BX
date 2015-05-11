package com.ecg.activity;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import com.ecg.MyApplication;
import com.ecg_analysis.R;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.view.Window;
import android.view.WindowManager;
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
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import info.hoang8f.widget.FButton;

@EActivity(R.layout.acticity_main)
public class MainActivity extends Activity {

    private String Tag = "Activity_Main";

     /*--------------------- Part 1 FindVeiewByID Start----------------------------
     ---------------------------------------------------------------------------*/
    @ViewById
    ListView MainActivity_ListView_BLEDevice;

   // @ViewById
   // TextView MainActivity_TextView_ShowConnectState;

    @ViewById
    FButton MainActivity_Button_ScanBLE;

    @ViewById
    FButton MainActivity_Button_CancelScan;

    @ViewById
    LinearLayout MainActivity_LinearLayout_BLEDeviceList;

    @ViewById
    RelativeLayout MainActivity_RelativeLayout_ButtonSeries;

    @ViewById
    LinearLayout MainActivity_LinearLayout_Text;

    @ViewById
    LinearLayout  MainActivity_LinearLayout_Cancel;

    @ViewById
    ToggleButton MainActivity_ToggleButton_IfConnectAuto;

     /*--------------------- Part 1 FindVeiewByID End----------------------------
     ---------------------------------------------------------------------------*/

     /*--------------------- Part 2 ButtonClick Start----------------------------
     ---------------------------------------------------------------------------*/
     @Click //ScanBLE
     void MainActivity_Button_ScanBLE(){

         MainActivity_RelativeLayout_ButtonSeries.setVisibility(View.GONE);
         MainActivity_LinearLayout_BLEDeviceList.setVisibility(View.VISIBLE);
         MainActivity_LinearLayout_Text.setVisibility(View.VISIBLE);
         MainActivity_LinearLayout_Cancel.setVisibility(View.VISIBLE);
         MainActivity_Button_ScanBLE.setEnabled(false);
         MainActivity_Button_CancelScan.setEnabled(true);
         scanLeDevice(true);
     }

    @Click //If ToggleButton(ConnectAuto) is true,You can press this Button to ShowActivity
    void MainActivity_Button_ConnectAtNow(){
        if(flagForBLEConnecting){
            final Intent intent = new Intent(this, ShowActivity_.class);
            intent.putExtra(ShowActivity_.EXTRAS_DEVICE_ADDRESS, ((MyApplication)getApplication()).getBLEConnectNowDeviceAddress());
            intent.putExtra(ShowActivity_.EXTRAS_ENTER_METHOD,"EXTRAS_ENTER_METHOD_2");
            startActivity(intent);
        }
        else Toast.makeText(MainActivity.this, "您的默认设备并没有连接成功，请扫描并选择设备连接",Toast.LENGTH_LONG).show();
    }

    @Click //cancle Blescan
    void MainActivity_Button_CancelScan(){
        MainActivity_LinearLayout_BLEDeviceList.setVisibility(View.GONE);
        MainActivity_RelativeLayout_ButtonSeries.setVisibility(View.VISIBLE);
        MainActivity_LinearLayout_Text.setVisibility(View.GONE);
        MainActivity_LinearLayout_Cancel.setVisibility(View.GONE);
        MainActivity_Button_CancelScan.setEnabled(false);
        MainActivity_Button_ScanBLE.setEnabled(true);
        //scanLeDevice(false);
    }
    /*--------------------- Part 2 ButtonClick Start----------------------------
     ---------------------------------------------------------------------------*/
   /*--------------------- Part 3 ScanBLEDevice Start----------------------------
     ---------------------------------------------------------------------------*/

    protected static final int REQUEST_ENABLE_BT = 1;

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;


    private boolean mScanning;
    private static final int SCAN_PERIOD = 1000;
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

    //callback functions
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                            Log.w(Tag, "Device has been scanned"+device.getName()+device.getAddress());
                        }
                    });
                }
            };



    //ListViewClickActivity
    @ItemClick
    void MainActivity_ListView_BLEDevice(int position){
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

    //ListAdapter
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

    /*--------------------- Part 3 ScanBLEDevice Start----------------------------
        ---------------------------------------------------------------------------*/
    /*--------------------- Part 4 AndriodActivity LifeCycle Start----------------------------
     ---------------------------------------------------------------------------*/
    Intent gattServiceIntent;
    Timer timer;
    @Override
     protected void onCreate(Bundle savedInstanceState) {

         super.onCreate(savedInstanceState);

         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//Forbid SCREEN off




         //get DeviceAddress if it store in Andriod
         SharedPreferences sharedPreferences = getSharedPreferences("DeviceAddress",MODE_PRIVATE);
         if(sharedPreferences!=null){
             ((MyApplication)getApplication()).setBLEAutoConnectDeviceAddress(sharedPreferences.getString("DeviceAddress","")); ;

         }

         Log.e(Tag,"onCreat");

         final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

         // If this Android device has BLE
         if (!getPackageManager().hasSystemFeature(
                 PackageManager.FEATURE_BLUETOOTH_LE)) {
             Toast.makeText(this, "设备不支持低功耗蓝牙",Toast.LENGTH_SHORT).show();
             finish();
         }

         //get BLE 适配器
         mBluetoothAdapter = bluetoothManager.getAdapter();
         if (mBluetoothAdapter == null) {
             Toast.makeText(this, "设备不支持低功耗蓝牙",Toast.LENGTH_SHORT).show();
             finish();
         }

        //if Bluetooth is not open，open it
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
     }


    @AfterViews
    public void init(){

    }

    @Background
    void ConnectDevice(){
        if(!flagForBLEConnecting&&mBluetoothLeService!=null&&flagForIfConnectAuto) {

            mBluetoothLeService.connect(((MyApplication) getApplication()).getBLEAutoConnectDeviceAddress(), true, false);


        }
    }



    protected void onResume() {
        super.onResume();

        Log.e(Tag, "onResume");

        //ConnectDevice per 2000ms
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ConnectDevice();
            }
        };
        timer.schedule(task,0, 2*1000);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        //ViewChange
        MainActivity_LinearLayout_BLEDeviceList.setVisibility(View.GONE);
        MainActivity_RelativeLayout_ButtonSeries.setVisibility(View.VISIBLE);
        MainActivity_LinearLayout_Text.setVisibility(View.GONE);
        MainActivity_LinearLayout_Cancel.setVisibility(View.GONE);
        MainActivity_Button_CancelScan.setEnabled(false);
        MainActivity_Button_ScanBLE.setEnabled(true);

        gattServiceIntent = new Intent(this, BluetoothLeService.class);
        startService(gattServiceIntent);
        flagForBindService=bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mLeDeviceListAdapter = new LeDeviceListAdapter();
        MainActivity_ListView_BLEDevice.setAdapter(mLeDeviceListAdapter);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void onPause() {
        super.onPause();
        Log.e(Tag,"onPause");
        mLeDeviceListAdapter.clear();


    }
    public void onDestroy() {
        super.onDestroy();
        Log.e(Tag,"onDestory");

        if(flagForBindService)  {

            unbindService(mServiceConnection);
            flagForBindService = false;
        }
        stopService(gattServiceIntent);
    }

    public void onStop() {
        super.onStop();
        Log.e(Tag, "onStop");
        timer.cancel();
        unregisterReceiver(mGattUpdateReceiver);
    }

    public void onStart() {
        super.onStart();
        Log.e(Tag, "onStart");
    }


 /*--------------------- Part 4 AndriodActivity LifeCycle End----------------------------
     ---------------------------------------------------------------------------*/



 /*--------------------- Part 5 BluetoothLeService Start----------------------------
 ---------------------------------------------------------------------------*/
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
            mBluetoothLeService.disconnect();
            mBluetoothLeService.connect(((MyApplication)getApplication()).getBLEAutoConnectDeviceAddress(),true,true);
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

                //MainActivity_TextView_ShowConnectState.setText(getResources().getString(R.string.MainActivity_String_DeviceAddressWhichOnConnect)+" "
                //        +((MyApplication)getApplication()).getBLEConnectNowDeviceAddress());



                flagForBLEConnecting = true;
            }
            if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {


                //MainActivity_TextView_ShowConnectState.setText(getResources().getString(R.string.main_bt_unconnect));
                flagForBLEConnecting = false;
            }
        }
    };
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        return intentFilter;
    }

    private boolean flagForBindService = false;
 /*--------------------- Part 5 BluetoothLeService End----------------------------
 ---------------------------------------------------------------------------*/

 /*--------------------- Part 6 ConnectAuto Start----------------------------
 ---------------------------------------------------------------------------*/

    boolean flagForIfConnectAuto = true;
    @CheckedChange
    void MainActivity_ToggleButton_IfConnectAuto(){
        if(MainActivity_ToggleButton_IfConnectAuto.isChecked()){
            Toast.makeText(MainActivity.this, "已经关闭自动连接", Toast.LENGTH_SHORT).show();
            //unregisterReceiver(mGattUpdateReceiver);

                flagForIfConnectAuto = false;
                mBluetoothLeService.disconnect();
                mBluetoothLeService.connect(((MyApplication) getApplication()).getBLEAutoConnectDeviceAddress(), false, true);
                mBluetoothLeService.disconnect();



        }else{
            Toast.makeText(MainActivity.this, "已经开启自动连接", Toast.LENGTH_SHORT).show();


            flagForBindService = false;
            mBluetoothLeService.connect(((MyApplication)getApplication()).getBLEAutoConnectDeviceAddress(),true,true);
            flagForIfConnectAuto = true;
        }

    }
    /*--------------------- Part 6 ConnectAuto End----------------------------
 ---------------------------------------------------------------------------*/
}
