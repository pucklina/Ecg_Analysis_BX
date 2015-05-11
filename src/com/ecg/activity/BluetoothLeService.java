/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ecg.activity;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ecg.MyApplication;
import com.ecg_analysis.R;

import java.util.List;
import java.util.UUID;


public class BluetoothLeService extends Service {
    private final static String TAG = "BluetoothService";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    public static final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public static final String EXTRA_DATA_BLUETOOTH = "com.example.bluetooth.le.EXTRA_DATA_BLUETOOTH";


    NotificationManager notificationManager;

    Notification notification;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {
        }


        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:"
                        + mBluetoothGatt.discoverServices());

                notification.setLatestEventInfo(BluetoothLeService.this,"ECG_ANALYSIS蓝牙服务",getResources().getString(R.string.MainActivity_String_DeviceAddressWhichOnConnect)+
                        " "+((MyApplication)getApplication()).getBLEConnectNowDeviceAddress(),null);
                notificationManager.notify(1,notification);


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");

                notification.setLatestEventInfo(BluetoothLeService.this,"ECG_ANALYSIS蓝牙服务",getResources().getString(R.string.main_bt_unconnect),null);
                notificationManager.notify(1, notification);
                broadcastUpdate(intentAction);
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            else
                Log.w(TAG, "onServicesDiscovered received: " + status);
        }

        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(
                        data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                //Log.w("information of data onCharacteristicRead",
                 //       stringBuilder.toString());
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            byte data = characteristic.getValue()[0];

            /*System.out.print("Data1:");
            for(byte x :characteristic.getValue())  System.out.print(x+" ");
            System.out.println("");*/

            int counter = (data)&(0x0f);

           // Log.w("information onCharacteristicChanged",
            //        "" + "sample counter:"+counter);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();

        int j;

        float icg[] = new float[2], ecg[] = new float[2];
        for (j = 0; j < 2; j++) { //3->2

            icg[j]=data[9*j+2]*65536+data[9*j+3]*256+data[9*j+4];

            int dataecg1 = data[9 * j + 5];
            int dataecg2 = data[9 * j + 6];
            int dataecg3 = data[9 * j + 7];
            if(dataecg1<0) dataecg1=dataecg1+256;
            if(dataecg2<0) dataecg2=dataecg2+256;
            if(dataecg3<0) dataecg3=dataecg3+256;
            ecg[j] = dataecg1  + dataecg2 * 256
                    + dataecg3* 65536;
           // Log.w("information of ecg&icg", "qiguaiecg3:"+dataecg1+" "+dataecg2+" "+dataecg3);

            if (icg[j]>8388607)
               icg[j]=icg[j]-8388608*2;
            if (ecg[j] > 8388607)
                ecg[j] = ecg[j] - 8388608 * 2;

            icg[j]=(float) (-icg[j]*2420/8388608/6/0.0298);
            //Log.w("information of ecg&icg", "qiguaiecg2:"+ecg[j]);
            ecg[j] = ecg[j] * 2420 / 8388608 / 6;

            //基线滤波
            float ecgFilter= filter_base_ecg1(ecg[j]);
            //滑动平均
            float ecgMean = filter_5_ecg1(ecgFilter);

            ecg[j]=ecgMean;

        }

        int counter = (data[0])&(0x0f);
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));

            intent.putExtra(EXTRA_DATA,stringBuilder.toString());
            //intent.putExtra(EXTRA_DATA_BLUETOOTH,data);
            intent.putExtra("ecg", ecg);
            intent.putExtra("icg", icg);
        }
        sendBroadcast(intent);
    }







//----------------------------Service LifeCycle Start----------------------------//
//-------------------------------------------------------------------------//

    public void onCreate() {
        super.onCreate();

        notificationManager =  (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new Notification(R.drawable.main_log,"BLE蓝牙服务已经开启",System.currentTimeMillis());
        notification.setLatestEventInfo(this,"ECG_ANALYSIS蓝牙服务","未连接蓝牙设备",null);
        notificationManager.notify(1,notification);
        ((MyApplication)getApplication()).setBlEServiceStart(true);
    }

    public void onDestroy() {
        super.onDestroy();
        ((MyApplication)getApplication()).setBlEServiceStart(false);
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean onUnbind(Intent intent) {
        //close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }


    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    public boolean connect(final String address,boolean autoConnect,boolean connectTryToUseOldGattForever) {

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG,"BluetoothAdapter not initialized or unspecified address");
            return false;
        }

        if ( mBluetoothDeviceAddress != null	&& address.equals(mBluetoothDeviceAddress)&& mBluetoothGatt != null) {
            Log.d(TAG,"Trying to use an existing mBluetoothGatt for connection");
            if (mBluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        mBluetoothGatt = device.connectGatt(this, autoConnect, mGattCallback);

        if(((MyApplication)getApplication()).getBLEConnectNowDeviceAddress().equals(address)==false)
            ((MyApplication)getApplication()).setBLEConnectNowDeviceAddress(address);

        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        System.out.println("device.getBondState==" + device.getBondState());
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }


//----------------------------Service LifeCycle End----------------------------//
//-------------------------------------------------------------------------//



    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;
        return mBluetoothGatt.getServices();
    }

    public synchronized void writeDescriptor(BluetoothGattDescriptor descriptor) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        if (mBluetoothGatt.writeDescriptor(descriptor))
            Log.w("information writeDescriptor", "success");
    }

    public BluetoothGattService getSerices(UUID uuidIrtServ) {
        return mBluetoothGatt.getService(uuidIrtServ);
    }

    public synchronized boolean writeCharacteristic(
            BluetoothGattCharacteristic configCharacteristic) {
        return mBluetoothGatt.writeCharacteristic(configCharacteristic);
    }



//----------------------------Math Function Start----------------------------//
//-------------------------------------------------------------------------//

    // 基线滤波器
    static float ecg1h0 = 0f, ecg1h1 = 0f, ecg1h2 = 0f, ecg1h3 = 0f, ecg1h4 = 0f,
            ecg1h5 = 0f;

    public float filter_base_ecg1(float x) {
        ecg1h5 = ecg1h4;
        ecg1h4 = ecg1h3;
        ecg1h3 = ecg1h2;
        ecg1h2 = ecg1h1;
        ecg1h1 = ecg1h0;
        ecg1h0 = (float) (x + 0.950957 * ecg1h5);
        return (float) (0.975478 * ecg1h0 - 0.975478 * ecg1h5);
    }

    // 五点滑动平均
    static int ecg1n = 5;
    static float ecg1value_buf[] = new float[ecg1n];
    static int ecg1ii = 0;

    // 五点滑动平均
    static float filter_5_ecg1(float f) {

        int count;
        float sum = 0;
        ecg1value_buf[ecg1ii++] = f;
        if (ecg1ii == ecg1n)
            ecg1ii = 0;
        for (count = 0; count < ecg1n; count++)
            sum += ecg1value_buf[count];
        return (float) (sum / ecg1n);
    }
//----------------------------Math Function End----------------------------//
//-------------------------------------------------------------------------//
}
