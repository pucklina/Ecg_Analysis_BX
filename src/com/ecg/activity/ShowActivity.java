package com.ecg.activity;

import static android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
import static java.util.UUID.fromString;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.CombinedXYChart.XYCombinedChartDef;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.ScatterChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.NoTitle;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import com.dd.CircularProgressButton;
import com.ecg.MyApplication;
import com.ecg.assist.ChartBuilder;
import com.ecg.db.Anno;
import com.ecg.db.TimeAnnoAdapter;
import com.ecg.jni.JniBdacServer;
import com.ecg.jni.JniFFTServer;
import com.ecg_analysis.R;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@EActivity(R.layout.acitvity_show)
public class ShowActivity extends Activity {


    private String tag = "ShowActivity"; //It uses for Log


    /*--------------------- Part 1 FindVeiewByID Start----------------------------
     ---------------------------------------------------------------------------*/
    @ViewById
    CircularProgressButton ShowActivity_Button_ConnectStart;
    @ViewById
    LinearLayout ShowActivity_LinerLayout_HRChart;
    @ViewById
    RelativeLayout ShowActivity_RelativeLayout_TimeChart;
    @ViewById
    RelativeLayout ShowActivity_RelativeLayout_FreqChart;
    @ViewById
    LinearLayout ShowActivity_LinerLayout_ECGChart;
    @ViewById
    Button ShowActivity_Button_ShowHRChart;
    @ViewById
    Button ShowActivity_Button_ShowTimeChart;
    @ViewById
    Button ShowActivity_Button_ShowFreqChart;
    @ViewById
    TextView ShowActivity_TextView_AnalysisText;
    @ViewById
    com.github.mikephil.charting.charts.BarChart TimeChart;
    @ViewById
    com.github.mikephil.charting.charts.LineChart HRChart;
    @ViewById
    com.github.mikephil.charting.charts.LineChart FreqChart;
    /*---------------------------------------------------------------------------
    --------------------- Part 1 FindVeiewByID End----------------------------*/

    /*--------------------- Part 2 ButtonClick Start----------------------------
     ---------------------------------------------------------------------------
     1 Function（ShowActivity_Button_ShowHRChart、ShowActivity_Button_ShowTimeChart、
     ShowActivity_Button_ShowFreqChart）decide which chart to show.
     2 Function(ShowActivity_Button_ConnectStart) click to connect bluetooth device.
     */

    private Boolean flagForTimeChartVisiable = false;
    private Boolean flagForFreqChartVisiable = false;
    private Boolean flagForHRChartVisiable = true;

    @Click
    void ShowActivity_Button_ShowHRChart() {
        flagForHRChartVisiable = true;
        flagForFreqChartVisiable = false;
        flagForTimeChartVisiable = false;
        ShowActivity_RelativeLayout_TimeChart.setVisibility(View.GONE);
        ShowActivity_LinerLayout_HRChart.setVisibility(View.VISIBLE);
        ShowActivity_RelativeLayout_FreqChart.setVisibility(View.GONE);
        ShowActivity_Button_ShowHRChart.setEnabled(false);
        ShowActivity_Button_ShowTimeChart.setEnabled(true);
        ShowActivity_Button_ShowFreqChart.setEnabled(true);

    }

    @Click
    void ShowActivity_Button_ShowTimeChart() {
        flagForTimeChartVisiable = true;
        flagForFreqChartVisiable = false;
        flagForHRChartVisiable = false;
        ShowActivity_RelativeLayout_TimeChart.setVisibility(View.VISIBLE);
        ShowActivity_LinerLayout_HRChart.setVisibility(View.GONE);
        ShowActivity_RelativeLayout_FreqChart.setVisibility(View.GONE);
        ShowActivity_Button_ShowHRChart.setEnabled(true);
        ShowActivity_Button_ShowTimeChart.setEnabled(false);
        ShowActivity_Button_ShowFreqChart.setEnabled(true);
    }

    @Click
    void ShowActivity_Button_ShowFreqChart() {
        flagForTimeChartVisiable = false;
        flagForFreqChartVisiable = true;
        flagForHRChartVisiable = false;
        ShowActivity_RelativeLayout_TimeChart.setVisibility(View.GONE);
        ShowActivity_LinerLayout_HRChart.setVisibility(View.GONE);
        ShowActivity_RelativeLayout_FreqChart.setVisibility(View.VISIBLE);
        ShowActivity_Button_ShowHRChart.setEnabled(true);
        ShowActivity_Button_ShowTimeChart.setEnabled(true);
        ShowActivity_Button_ShowFreqChart.setEnabled(false);

        updateAnalysisTV(null, 2);
    }

    private boolean flagForIfReceiverRegistered = false;//flagForIfReceiverRegistered:This Flag show that RegisterReceiver is successful or not
    private boolean flagForIfConnectSuccecceed = false;

    @Click
    void ShowActivity_Button_ConnectStart() {

        //ButtonProcess
        int ButtonProcess = ShowActivity_Button_ConnectStart.getProgress();

        if (ButtonProcess < 100 && ButtonProcess > 0) ;//When Button is Indeterminate,do nothing
        else {

            //if Reciever not Register,Register it;
            if(!flagForIfReceiverRegistered)
                if(registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())!=null)  flagForIfReceiverRegistered = true;


            //Begin Connect
            for (int i = 0; i < 3; i++) {
                if (mBluetoothLeService != null) {
                    Log.i("information onServiceConnected", ""
                            + mBluetoothLeService.getClass().toString());
                    final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                    Log.i("information", "Connect request result=" + result);
                    if (result) {
                        flagForIfConnectSuccecceed = true;
                        break;
                    } else if (i == 2) ShowActivity_Button_ConnectStart.setProgress(-1);
                }
            }

            //RegisterReceiver must success
            if (flagForIfConnectSuccecceed) {

                //if you click "connect again"
                if (ShowActivity_Button_ConnectStart.getProgress() == -1)
                    ShowActivity_Button_ConnectStart.setProgress(0);

                //if you click "connect"
                if (ShowActivity_Button_ConnectStart.getProgress() == 0) {
                    ShowActivity_Button_ConnectStart.setIndeterminateProgressMode(true); // turn on indeterminate progress
                    ShowActivity_Button_ConnectStart.setProgress(50);
                    WriteDevice();
                }
                //if you click "pause connect"
                else if (ShowActivity_Button_ConnectStart.getProgress() == 100) {
                    unregisterReceiver(mGattUpdateReceiver);
                    flagForIfReceiverRegistered = false;
                    ShowActivity_Button_ConnectStart.setProgress(0);
                }
            }
        }
    }
    /*---------------------------------------------------------------------------
    --------------------- Part 2 ButtonClick End----------------------------*/

    /*--------------------- Part 3 BluetoothService Start----------------------------
    ---------------------------------------------------------------------------*/


    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;

    //CallBackFunction for BindService
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Log.i("information", "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
  /*---------------------------------------------------------------------------
    --------------------- Part 3 BluetoothService End----------------------------*/

    /*--------------------- Part 4 BluetoothConnect Start----------------------------
      Function(ConnectDevice) try to start thread(Connect1 and Connect2) for 10 times
      at most.Only if ConnectStatue1 and ConnectStatue2 both are true,we consider bluetooth
      connect sucessful.But at some times,when all flags are true,the device show that connect
      is actually fail."So try to add another Flag to know whether the app receive the data
       successfully or not.

         Function(ConnectDevice1) and Function(ConnectDevice2) are try to connect to
         device and write config flag to it.
      ---------------------------------------------------------------------------*/

    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_ENTER_METHOD = "ENTER_METHOD";

    public final static UUID UUID_ACC_SERV = fromString("f000aa10-0451-4000-b000-000000000000"),
            UUID_ACC_DATA = fromString("f000aa11-0451-4000-b000-000000000000"),
            UUID_ACC_CONF = fromString("f000aa12-0451-4000-b000-000000000000"),
            CCC = fromString("00002902-0000-1000-8000-00805f9b34fb");


    private boolean flagForIfWriteDevice1Succeed = false; //Flag shows connect1 connect successfully or not
    private boolean flagForIfWriteDevice2Succeed = false;  //Flag shows connect2 connect successfully or not
    private boolean flagForIfWriteDevice1Finish = true;  //Flag shows connect1 thread running or not
    private boolean flagForIfWriteDevice2Finish = true;  //Flag shows connect2 thread running or not

    @Background
    void WriteDevice() {
        flagForIfWriteDevice1Succeed = false;
        flagForIfWriteDevice2Succeed = false;
        int count = 0;
        while (count != 10) { //try to connect for 10 times
            if (flagForIfWriteDevice1Finish && flagForIfWriteDevice2Finish) {
                if ((flagForIfWriteDevice1Succeed && flagForIfWriteDevice2Succeed
                        && flagForIfWriteDevice1Finish && flagForIfWriteDevice2Finish)) {
                    setCircleButton(100);
                    break;
                }
                flagForIfWriteDevice1Finish = false;
                flagForIfWriteDevice2Finish = false;
                WriteDevice1();
                WriteDevice2();
                count++;
            }

            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (flagForIfWriteDevice1Succeed && flagForIfWriteDevice2Succeed)
            setCircleButton(100); //when connect1 and connect2 connect successful
        else setCircleButton(-1);


    }

    @UiThread
    void setCircleButton(int process) {
        ShowActivity_Button_ConnectStart.setIndeterminateProgressMode(false);
        ShowActivity_Button_ConnectStart.setProgress(process);
    }

    @Background(delay = 500)
    void WriteDevice1() {

        Log.i(tag, "STATUE1 before:" + flagForIfWriteDevice1Succeed);
        //Tag connect1 is running
        if (!flagForIfWriteDevice1Succeed) {
            flagForIfWriteDevice1Finish = false;
            BluetoothGattService GattService = mBluetoothLeService
                    .getSerices(UUID_ACC_SERV);
            if (GattService != null) {
                try {
                    BluetoothGattCharacteristic dataCharacteristic = GattService
                            .getCharacteristic(UUID_ACC_DATA);
                    mBluetoothLeService.setCharacteristicNotification(
                            dataCharacteristic, true);
                    BluetoothGattDescriptor config = dataCharacteristic
                            .getDescriptor(CCC);
                    byte[] configValue = ENABLE_NOTIFICATION_VALUE;
                    boolean success = config.setValue(configValue);
                    if (success) {
                        Log.w("information changeNotification success",
                                "information changeNotification");
                        flagForIfWriteDevice1Succeed = true;
                    }
                    mBluetoothLeService.writeDescriptor(config);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("It ALL");
                    flagForIfWriteDevice1Succeed = false;
                } finally {
                    flagForIfWriteDevice1Finish = true;
                }
            } else {
                flagForIfWriteDevice1Succeed = false;
            }
        }
        flagForIfWriteDevice1Finish = true;

        Log.i(tag, "STATUE1:" + flagForIfWriteDevice1Succeed);
    }


    @Background(delay = 1000)
    void WriteDevice2() {

        Log.i(tag, "STATUE2 before:" + flagForIfWriteDevice2Succeed);

        if (!flagForIfWriteDevice2Succeed) {
            flagForIfWriteDevice2Finish = false;
            BluetoothGattService GattService = mBluetoothLeService
                    .getSerices(UUID_ACC_SERV);
            if (GattService != null) {
                try {
                    BluetoothGattCharacteristic configCharacteristic = GattService
                            .getCharacteristic(UUID_ACC_CONF);
                    byte[] code = new byte[]{0x01};
                    configCharacteristic.setValue(code);
                    mBluetoothLeService
                            .writeCharacteristic(configCharacteristic);
                    flagForIfWriteDevice2Succeed = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("It ALL");
                    flagForIfWriteDevice2Succeed = false;
                } finally {
                    flagForIfWriteDevice2Finish = true;
                }
            } else flagForIfWriteDevice2Succeed = false;
        }
        flagForIfWriteDevice2Finish = true;
        Log.i(tag, "STATUE2:" + flagForIfWriteDevice2Succeed);
    }
    /*--------------------- Part 4 BluetoothConnect End----------------------------
    ---------------------------------------------------------------------------*/

    /*--------------------- Part 5 BluetoothBroadcastReceiver Start----------------------------
        ---------------------------------------------------------------------------*/

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                final float ecg[] = intent.getFloatArrayExtra("ecg");
                ECGDataProcess(ecg);
            }
            if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                BluetoothDisconnetWarning();
            }
        }
    };

    @UiThread
    void BluetoothDisconnetWarning() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false); //cannot close
        dialog.setTitle("Warning！");
        dialog.setMessage("蓝牙连接异常断开，请重新连接");
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                BackToMainActivity();

            }
        });
        dialog.show();
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        //2015 4 12 add to detect bluetooth is disconnect
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.EXTRA_DATA_BLUETOOTH);
        return intentFilter;
    }

    @UiThread
    void BackToMainActivity() {
        finish();
        MainActivity_.intent(this).start();
    }
     /*--------------------- Part 5 BluetoothBroadcastReceiver End----------------------------
        ---------------------------------------------------------------------------*/

    /*--------------------- Part 6 AndriodActivity LifeCycle Start----------------------------
    ---------------------------------------------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//Forbid SCREEN off

        final Intent intent = getIntent();

        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        MyApplication.BLEDeviceName = mDeviceAddress;

        //store the DeviceAddress
        SharedPreferences.Editor editor = getSharedPreferences("DeviceAddress",MODE_PRIVATE).edit();
        editor.putString("DeviceAddress",mDeviceAddress);
        editor.commit();



    }

    private TimeAnnoAdapter timeAnnoAdapter;

    //What to do after onCreat and before on resume.
    @AfterViews
    public void init() {
        try {
            init_TimeChart();
            init_ECGChart();
            init_HRChart();
            init_FreqChart();
            ShowActivity_LinerLayout_ECGChart.addView(ECGChart, new LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        } catch (Exception e) {
            e.printStackTrace();
        }


        JniBdacServer.resetBDAC();
        timeAnnoAdapter = new TimeAnnoAdapter();//to help count RR data and draw RR-bar-chart
    }

    protected void onResume() {
        Log.e(tag,"onResume");
        super.onResume();
        //registerReceiver(from BLEService)
        if(registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())!=null)  flagForIfReceiverRegistered = true;
        //START Service 和 bindService
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        startService(gattServiceIntent);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    protected void onStop() {
        super.onStop();
        Log.e(tag,"onStop");
        if (flagForIfReceiverRegistered) {
            unregisterReceiver(mGattUpdateReceiver);
            flagForIfReceiverRegistered = false;
        }
        unbindService(mServiceConnection);
    }


    protected void onPause() {
        super.onPause();
        Log.e(tag,"onPause");
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.e(tag,"onDestroy");

        mBluetoothLeService = null;
    }
    /*--------------------- Part 6 AndriodActivity LifeCycle End----------------------------
   ---------------------------------------------------------------------------*/

    /*--------------------- Part 7 DataProcess Start------------------------
---------------------------------------------------------------------------*/
    private ArrayList<Float> ECGChartArray = new ArrayList<>();//ECG Data Array to update for ecgchart
    private ArrayList<Float> ECGChartTempArray = new ArrayList<>();//ECG Data Array to update for ecgchart in case changable
    //ECG Annotation Data Array to update for ecgchart
    private ArrayList<String> TypeArray = new ArrayList<>();
    private ArrayList<String> TempTypeArray = new ArrayList<>();
    private ArrayList<Integer> Rpeakposition = new ArrayList<>();
    private ArrayList<Integer> TempRpeakposition = new ArrayList<>();

    private int ECGCount = 0;//ECG Receive number

    //All ECG DATA IN ECGArrayMaxTime
    private ArrayList<Float> ECGArray = new ArrayList<>();
    private final int ECGArrayMaxTime = 60 * 1000;//unit:ms  10min
    private final int ECGDataFreq = 4;//unit ms 4ms
    private int ECGDataTotalNum;
    private final int ECGDataNumPerDel = 2000;
    private int ECGArrayDelCount = 0;

    //ECGArray to store the R-peak ecg value
    private ArrayList<Float> PeakECGArray = new ArrayList<>();

    //For freq chart
    private ArrayList<Double> RRArray = new ArrayList<>();
    private ArrayList<Double> RRArrayTemp = new ArrayList<>();
    private int RRArraySizeNow = 0;
    private int RRArraySizeAtLastFFTTime = 0;

    void ECGDataProcess(float ecg[]) {

        ECGDataTotalNum = ECGArrayMaxTime / ECGDataFreq;
        for (int i = 0; i < 2; i++) {
            ECGCount++;


            //ECG Cache Array
            if (ECGArray.size() > ECGDataTotalNum) {
                for (int j = 0; j < ECGDataNumPerDel; j++) {
                    ECGArray.remove(0);

                }
                ECGArrayDelCount++;
            }

            ECGArray.add(ecg[i]);

            Anno anno = JniBdacServer.BDAC((int) ecg[i] * 200);
            if (anno.dectTime != 0) {
                //updateAnalysisTV(anno); When HRChart Invisiable
                if (flagForHRChartVisiable || flagForTimeChartVisiable) updateAnalysisTV(anno, 1);
                //help to update Time Chart
                timeAnnoAdapter.insertAnno(anno);
                //updateRateMap
                if (!updateHRChartFlag) {
                    updateHRChartFlag = true;
                    updateRateMap(anno.RR);
                }

                //find Rpeak point
                Rpeakposition.add(ECGCount - (int) (JniBdacServer.getSampleCount() - anno.dectTime));
                TypeArray.add(anno.beatType);
                //int tempint = (int) (JniBdacServer.getSampleCount() - anno.dectTime);
                //PeakECGArray.add(ECGArray.get(ECGCount -ECGArrayDelCount*ECGDataNumPerDel - tempint));

                //A data per 4 ms and the most capcity is for 100000
                if (RRArray.size() < 256) RRArray.add(anno.RR);
                else {
                    RRArray.remove(0);
                    RRArray.add(anno.RR);
                }
                RRArraySizeNow++;

                //Log.w(tag, "size" + RRArray.size());
                //UpdateFreqChart for every 5 date
                if ( (RRArraySizeNow- RRArraySizeAtLastFFTTime > 5)) {

                    if (!updateFreqChartFlag) {
                        RRArrayTemp = (ArrayList<Double>)RRArray.clone();
                        updateFreqChartFlag = true;
                        RRArraySizeAtLastFFTTime = RRArraySizeNow;
                        updateFreqChart();
                    }

                }

            }

            //ECGChartArray
            ECGChartArray.add(ecg[i]);
            labeljustCount++;

            if (ECGChartArray.size() >= 1000) {
                ECGChartArray.remove(0);
                removecount++;
            }
        }


        while (true) {
            if (Rpeakposition.size() == 0) break;
            //Remove the R-peak point that should not show in this chart now
            //We judge if the first one poiont exceed the border,if it is,remove it and cotinue.
            //else break;
            if (Rpeakposition.get(0) - removecount < 0) {
                Rpeakposition.remove(0);
                TypeArray.remove(0);
                //TempPeakECGArray.remove(0);
            } else break;
        }

        if (!updateECGChartFlag) {
            updateECGChartFlag = true;
            ECGChartTempArray = (ArrayList<Float>)ECGChartArray.clone();
            TempTypeArray = (ArrayList<String>)TypeArray.clone();
            TempRpeakposition = (ArrayList<Integer>)Rpeakposition.clone();

            float maxNum = Collections.max(ECGChartTempArray);
            float minNum = Collections.min(ECGChartTempArray);

            //Adjust ECGChart Ylabel

            if (labeljustCount > 1200) {
                labeljustCount = 0;
                double maxscore, minscore;
                if (maxNum == minNum) {
                    maxscore = maxNum + 2;
                    minscore = maxNum - 2;
                } else {
                    maxscore = maxNum * 1.1 + (maxNum - minNum) / 3;
                    minscore = minNum - (maxNum - minNum) / 3;

                }

                rendererEcg.setYAxisMax(maxscore);
                rendererEcg.setYAxisMin(minscore);
            }
            updateECGChart();
        }
        if (!updateTimeChartFlag) {
            updateTimeChartFlag = true;
            updateTimeChart();
        }
    }
    /*--------------------- Part 7 DataProcess End------------------------
    ---------------------------------------------------------------------------*/

    /*--------------------- Part 8 Something on ECGChart Start----------------------------
  ---------------------------------------------------------------------------*/
    private XYMultipleSeriesRenderer rendererEcg = new XYMultipleSeriesRenderer();// 描绘器：一个用来给图表做渲染的句柄
    private XYSeries seriesEcg = new XYSeries("Ecg Signal");
    private XYSeries seriesR = new XYSeries("R-Peak");
    ;// 这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
    private XYMultipleSeriesDataset mDatasetEcg = new XYMultipleSeriesDataset();


    private GraphicalView ECGChart;

    private void init_ECGChart() {
        //two dataset,one is ECG the other is Annotation
        mDatasetEcg.addSeries(0, seriesEcg);
        mDatasetEcg.addSeries(1, seriesR);

        XYCombinedChartDef[] viewTypes = new XYCombinedChartDef[]{
                new XYCombinedChartDef(LineChart.TYPE, 0),
                new XYCombinedChartDef(ScatterChart.TYPE, 1)};
        int[] colors = {Color.BLACK, Color.RED};

        PointStyle[] styles = {PointStyle.POINT, PointStyle.CIRCLE};
        int Margins[] = new int[]{25, 15, 25, 15};

        ChartBuilder.buildRenderer(rendererEcg, colors, styles);
        ChartBuilder.setChartSettings(rendererEcg, "EcgSamples", "time/sec",
                "dbm", 0, 1000, 0, 2, Color.LTGRAY, Color.LTGRAY,
                getResources().getColor(R.color.fbutton_color_clouds),
                getResources().getColor(R.color.fbutton_color_clouds),
                getResources().getColor(R.color.fbutton_color_clouds),
                Margins, true);
        ECGChart = ChartFactory.getCombinedXYChartView(this, mDatasetEcg,
                rendererEcg, viewTypes);

        ECGChart.setClickable(false);

        rendererEcg.setXLabels(0);
        for (int i = 0; i < 11; i++) {
            float x;
            x = (float) i / 2;
            rendererEcg.addXTextLabel(i * 100, "" + x);
        }

    }

    private int tempCount = 0;
    private int labeljustCount = 0;
    private int removecount = 0;
    private Boolean updateECGChartFlag = false;

    @Background
    void updateECGChart() {
        updateECGChartFlag = true;


        //update every 10 ecg datas
        tempCount++;
        if (tempCount == 10) {
            mDatasetEcg = new XYMultipleSeriesDataset();
            //clear ECG Series
            //mDatasetEcg.removeSeries(0);
            seriesEcg.clear();
           // seriesEcg = new XYSeries("Ecg Signal");

            for (int i = 0; i < ECGChartTempArray.size(); i++) {
                seriesEcg.add(i, ECGChartTempArray.get(i));
            }

            mDatasetEcg.addSeries(0, seriesEcg);

            //clear ECG Annotation Series
           // mDatasetEcg.removeSeries(1);
            seriesR.clearSeriesValues();
            for (int i = seriesR.getAnnotationCount() - 1; i >= 0; i--)
                seriesR.removeAnnotation(i);

            double maxNum = seriesEcg.getMaxY();

            //Add annotation:Rpeakposition.get(i) is R-peak point is the X of the total ecg value
            //removecount is count that ECGChartTempArray has been move as it's Max capcity is 1000 now.
            for (int i = 0; i < TempRpeakposition.size(); i++) {
                seriesR.addAnnotation(TempTypeArray.get(i), TempRpeakposition.get(i) - removecount+2, maxNum * 1.1 + 0.2);
                seriesR.add(TempRpeakposition.get(i)+2 - removecount, maxNum * 1.1);
            }

            mDatasetEcg.addSeries(1, seriesR);
            ECGChartinvalidate();
            tempCount = 0;
        }
        updateECGChartFlag = false;
    }

    @UiThread
    void ECGChartinvalidate() {
        ECGChart.invalidate();
    }

    /*--------------------- Part 8 Something on ECGChart End----------------------------
  ---------------------------------------------------------------------------*/
 /*--------------------- Part 9 Something on HRChart Beigin----------------------------
  ---------------------------------------------------------------------------*/

    private ArrayList<String> HRChartxVals;
    private int[] HRChartyVals;
    private float HRChartYMax;
    private float HRChartYMin;
    //private int beatCount;
    int TimeChartNewValueCount;
    private Boolean updateHRChartFlag = false;

    private void init_HRChart() {
        HRChart.setBackgroundColor(getResources().getColor(R.color.fbutton_color_clouds));
        HRChart.setDrawBorders(false);
        HRChart.setDrawGridBackground(false);
        HRChart.setDescription("");
        HRChart.setTouchEnabled(false);
        HRChart.setClickable(false);
        HRChart.setDragEnabled(false);
        XAxis xAxis = HRChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.RED);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAdjustXLabels(true);

        HRChartxVals = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            HRChartxVals.add(String.valueOf(i));
        }

        YAxis yAxis = HRChart.getAxisRight();
        yAxis.setEnabled(false);

        yAxis = HRChart.getAxisLeft();
        yAxis.setTextSize(10f);
        yAxis.setTextColor(Color.RED);
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawGridLines(false);
        yAxis.setStartAtZero(false);

        HRChartyVals = new int[20];
        HRChartYMax = 0;
        HRChartYMin = 999;
    }

    @UiThread
    void HRChartinvalidate() {
        HRChart.invalidate();
    }

    @Background
    void updateRateMap(double RRinterval) {
        //Announce this thread is running
        updateHRChartFlag = true;
        //Get HeartRate
        int TempRate = ((int) (60 / RRinterval));

        //HR Y Chart count,the max capcity is 20
        if (HRChartyVals.length < 20) HRChartyVals[HRChartyVals.length] = TempRate;
        else {
            for (int i = 0; i < HRChartyVals.length - 1; i++) HRChartyVals[i] = HRChartyVals[i + 1];
            HRChartyVals[19] = TempRate;
        }

        ArrayList<Entry> yVals = new ArrayList<>();

        for (int i = 0; i < HRChartyVals.length; i++) {

            yVals.add(new Entry(HRChartyVals[i], i));
        }


        LineDataSet HRChartyDataSet = new LineDataSet(yVals, "HeartRate");
        LineData HRChartFinalData = new LineData(HRChartxVals, HRChartyDataSet);

        HRChartyDataSet.setCircleSize(5f);
        HRChartyDataSet.setLineWidth(1.75f); // 线宽
        HRChartyDataSet.setColor(Color.RED);// 显示颜色
        HRChartyDataSet.setCircleColor(Color.RED);// 圆形的颜色
        HRChartyDataSet.setHighLightColor(Color.RED); // 高亮的线的颜色

        HRChart.setData(HRChartFinalData);

        //Adjust the Y-label and use some method to reduce adjust times.
        float tempMax = HRChartyDataSet.getYMax();
        float tempMin = HRChartyDataSet.getYMin();
        if ((HRChartYMax - tempMax < 10) || (HRChartYMax - tempMax > 60) || (tempMin - HRChartYMin > 60) || (tempMin - HRChartYMin < 10)) {
            HRChart.getAxisLeft().setAxisMaxValue(tempMax + 40);
            HRChart.getAxisLeft().setAxisMinValue(tempMin - 40);
            HRChartYMax = tempMax + 40;
            HRChartYMin = tempMin - 40;
        }
        //Use UI-Thread to invalidate
        if (flagForHRChartVisiable) HRChartinvalidate();
        updateHRChartFlag = false;
    }

/*--------------------- Part 9 Something on HRChart End----------------------------
  ---------------------------------------------------------------------------*/
 /*--------------------- Part 10 Something on TimeChart Start------------------------
  ---------------------------------------------------------------------------*/

    private ArrayList<String> TimeChartxVals;
    private Boolean updateTimeChartFlag = false;

    private void init_TimeChart() {

        TimeChart.setTouchEnabled(false);
        TimeChart.setClickable(false);
        TimeChart.setDragEnabled(false);
        TimeChart.setDrawBorders(false);
        TimeChart.setBackgroundColor(getResources().getColor(R.color.fbutton_color_clouds));
        TimeChart.setDescription("");
        XAxis xAxis = TimeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(14f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAdjustXLabels(true);

        TimeChartxVals = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            TimeChartxVals.add(String.format("%.2f", (0.6 + 0.025 * i)));
        }

        YAxis yAxis = TimeChart.getAxisRight();
        yAxis.setEnabled(false);

        yAxis = TimeChart.getAxisLeft();
        yAxis.setTextSize(10f);
        yAxis.setTextColor(Color.RED);
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawGridLines(false);

        TimeChartNewValueCount = 0;

    }

    @Background
    void updateTimeChart() {
        updateTimeChartFlag = true;
        int temp[];
        temp = timeAnnoAdapter.getNumOfRRcount();
        float yvalue[] = new float[30];
        for (int i = 0; i < 30; i++)//0.6--1.2
        {
            yvalue[i] = (float) temp[i];
        }
        //beatCount = timeAnnoAdapter.getRRCount(100);
        //int type[] = new int[4];


        ArrayList<BarEntry> yVals = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            yVals.add(new BarEntry(yvalue[i], i));
        }

        BarDataSet TimeChartDataSet = new BarDataSet(yVals, "RR");
        BarData data = new BarData(TimeChartxVals, TimeChartDataSet);

        TimeChartDataSet.setBarSpacePercent(50);
        TimeChartDataSet.setColor(getResources().getColor(R.color.skyblue)); // 线宽
        TimeChartDataSet.setHighLightColor(getResources().getColor(R.color.lightskyblue));
        TimeChartDataSet.setDrawValues(false);

        TimeChart.setData(data);

        //If has more than one NewValueCount,invalidate();
        if (timeAnnoAdapter.getNewValueCount() - TimeChartNewValueCount >= 2) {
            if (flagForTimeChartVisiable) TimeChartinvalidate();
            TimeChartNewValueCount = timeAnnoAdapter.getNewValueCount();
        }
        updateTimeChartFlag = false;
    }

    @UiThread
    void TimeChartinvalidate() {
        TimeChart.invalidate();
    }

 /*--------------------- Part 10 Something on TimeChart End------------------------
  ---------------------------------------------------------------------------*/

    /*--------------------- Part 11 Something on FreqChart Start------------------------
      ---------------------------------------------------------------------------*/
    private ArrayList<String> FreqChartxVals;
    private Boolean updateFreqChartFlag = false;

    private void init_FreqChart() {
        FreqChart.setBackgroundColor(getResources().getColor(R.color.fbutton_color_clouds));
        FreqChart.setDrawBorders(false);
        FreqChart.setDescription("");
        FreqChart.setDrawGridBackground(false); //
        FreqChart.setTouchEnabled(false);
        FreqChart.setDragEnabled(false);
        FreqChart.setClickable(false);
        FreqChart.setNoDataText("We Need More Data To Paint Freq-Chart,You Will Receive Note When It Update At First Time");

        XAxis xAxis = FreqChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.RED);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAdjustXLabels(true);

        YAxis yAxis = FreqChart.getAxisRight();
        yAxis.setEnabled(false);
        yAxis = FreqChart.getAxisLeft();
        yAxis.setTextSize(10f);
        yAxis.setTextColor(Color.RED);
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawGridLines(false);

    }

    @UiThread
    void FreqChartinvalidate() {
        FreqChart.invalidate();
    }

    @Background
    void updateFreqChart() {

        updateFreqChartFlag = true;
        RRArrayTrans = new ArrayList<>();
        getRRArraytrans();
        if (RRArrayTemp.size() >= 256) {

            //NoticeFreqChartUpadateFirst();


            ArrayList<Entry> yVals = new ArrayList<>();
            FreqChartxVals = new ArrayList<>();
            filter();
            fd = new float[FFT_N];
            fd = JniFFTServer.fft(td, FFT_N);
            if (flagForFreqChartVisiable) updateAnalysisTV(null, 2);
            filterFD();
            yVals.clear();
            //java.text.DecimalFormat ff = new java.text.DecimalFormat("#0.###");
            //for (int i = 0; i < FFT_N; i++) {
            //    //Log.w(tag, "" + i + "as:" + ff.format(fd[i]));
            //
            //}
            float tempMax = 0f;
            for(float i:fd) if(i>tempMax) tempMax=i;

            for (int i = 0; i < FFT_N / 2; i++) {
                //double temp = 2*(double)i/(double)FFT_N;
                yVals.add(new Entry(fd[i]/tempMax,i));
                FreqChartxVals.add(String.valueOf(i)+"/256");

            }


            if (yVals.size() > FreqChartxVals.size()) ;
            else {
                LineDataSet FreqChartyDataSet = new LineDataSet(yVals, "HeartRate");
                LineData FreqChartFinalData = new LineData(FreqChartxVals, FreqChartyDataSet);

                FreqChartyDataSet.setDrawCircles(false);
                FreqChartyDataSet.setDrawCubic(true);
                FreqChartyDataSet.setCubicIntensity(0.4f);
                FreqChartyDataSet.setLineWidth(1f); // 线宽
                FreqChartyDataSet.setColor(Color.RED);// 显示颜色
                FreqChartyDataSet.setDrawValues(false);// 显示颜色

                FreqChart.setData(FreqChartFinalData);
                if (flagForFreqChartVisiable) FreqChartinvalidate();
            }

        }
        updateFreqChartFlag = false;
    }


    private ArrayList<Double> RRArrayTrans;
    private float comb[] = new float[]{0, 0, 0, 0, 0, 0};
    private float td[];
    private float fd[];
    private int FFT_N;


    private void filter() {
        //Adapt 512 points fft
        FFT_N = 512;
       // while (FFT_N < RRArrayTrans.size())
       //     FFT_N *= 2;
       // FFT_N /= 2;

        td = new float[FFT_N];

        //float avg = 0;
        //for (int i = 0; i < FFT_N; i++)
        //    avg += RRArrayTrans.get(i) / FFT_N;

        for (int i = 0; i < FFT_N; i++) {
            td[i] = (float) (RRArrayTrans.get(i) / 1.0);
            //Log.w("TD avg", "" + td[i]);
        }


        //梳状态滤波
        for (int i = 0; i < FFT_N; i++) {
            comb[5] = comb[4];
            comb[4] = comb[3];
            comb[3] = comb[2];
            comb[2] = comb[1];
            comb[1] = comb[0];
            comb[0] = (float) (td[i] + 0.950957 * comb[5]);
            float f = (float) (0.975478 * comb[0] - 0.975478 * comb[5]);
            td[i] = f;
        }
        float avg1 = 0;
        for (int i = 0; i < FFT_N; i++)
            avg1 += td[i] / FFT_N;

        for (int i = 0; i < FFT_N; i++) {
            td[i] =(td[i] - avg1);
        }
        //for (int i = 0; i < FFT_N; i++) ;
        // Log.w("TD", "td" + i + ":" + td[i]);
        /*
        FFT_N=128;
		td = new float[FFT_N];
		for(int i=0;i<FFT_N;i++)
		{
			td[i]=(float) Math.sin(2*Math.PI*i/FFT_N);
		}*/
    }

    private void filterFD() {
        int win = 15;
        if (FFT_N / 2 > win) {
            for (int i = win / 2; i < FFT_N / 2 - win / 2; i++) {
                float a = 0;
                for (int k = i - win / 2; k < i + win / 2; k++)
                    a += fd[k];
                fd[i] = a / win;
            }
            //float avg = 0;
            for (int i = 1; i < 7; i++) {
                fd[i] = i / 7 * fd[7];
            }
        }
    }

    private void getRRArraytrans() {
        Log.e(tag,"getRRArray"+RRArray.size());
        double ff, bb, mm;
        ff = 0;
        bb = RRArray.get(2);
        mm = 0.5;
        int i = 2;
        while (i < RRArrayTemp.size()) {
            while (mm < bb) {
                double res;
                res = (RRArrayTemp.get(i) - RRArrayTemp.get(i - 1)) * (mm - ff) / (bb - ff) + RRArrayTemp.get(i - 1);
                RRArrayTrans.add(res);
                mm += 0.5;
            }
            ff = bb;
            bb += RRArrayTemp.get(i);
            i++;
        }

        Log.e(tag,"getRRArraytrans"+RRArrayTrans.size());
        while(RRArrayTrans.size()!=512){
            RRArrayTrans.add(0.0);
        }
    }

    /*--------------------- Part 11 Something on FreqChart End------------------------
    ---------------------------------------------------------------------------*/
    /*--------------------- Part 12 TextView Start------------------------
    ---------------------------------------------------------------------------*/
    @UiThread
    void updateAnalysisTV(Anno anno, int type) {
        if (type == 1) {
            ShowActivity_TextView_AnalysisText.setText("QRSwidth\t:\t"
                    + String.format("%.2f", anno.qrsWidth) + " s" + "     "
                    + "BeatType\t:\t" + anno.beatType
                    + "       " + "RR-interval\t:\t"
                    + String.format("%.2f", anno.RR) + "s");
            ShowActivity_TextView_AnalysisText.setTextColor(Color.DKGRAY);
            ShowActivity_TextView_AnalysisText.setTextSize(15);
        } else {
            float VLF = 0;
            for (int i = (int) (FFT_N * 0.0033); i < (int) (FFT_N * 0.04); i++) {
                VLF += fd[i];
            }
            VLF /= FFT_N;
            //Log.w("log", "VLF=" + VLF);
            float LF = 0;
            for (int i = (int) (FFT_N * 0.04); i < (int) (FFT_N * 0.15); i++) {
                LF += fd[i];
            }
            LF /= FFT_N;
            //Log.w("log", "LF=" + LF);
            float HF = 0;
            for (int i = (int) (FFT_N * 0.15); i < (int) (FFT_N * 0.4); i++) {
                HF += fd[i];
            }
            HF /= FFT_N;
            //Log.w("log", "HF=" + HF);
            java.text.DecimalFormat ff = new java.text.DecimalFormat("#0.###");

            ShowActivity_TextView_AnalysisText.setTextSize(15);
            ShowActivity_TextView_AnalysisText.setTextColor(Color.DKGRAY);

            ShowActivity_TextView_AnalysisText.setText("RRArray.size:"+RRArray.size()+"  VLF: " + ff.format(VLF) + ";   LF: "
                    + ff.format(LF) + ";   HF: " + ff.format(HF) + "\nLF/HF: "
                    + ff.format(LF / HF));
        }
    }


 /*--------------------- Part 12 TextView End------------------------
    ---------------------------------------------------------------------------*/
}