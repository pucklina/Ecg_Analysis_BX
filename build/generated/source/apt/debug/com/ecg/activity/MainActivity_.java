//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations 3.2.
//


package com.ecg.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.ecg_analysis.R.layout;
import info.hoang8f.widget.FButton;
import org.androidannotations.api.BackgroundExecutor;
import org.androidannotations.api.builder.ActivityIntentBuilder;
import org.androidannotations.api.view.HasViews;
import org.androidannotations.api.view.OnViewChangedListener;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class MainActivity_
    extends MainActivity
    implements HasViews, OnViewChangedListener
{

    private final OnViewChangedNotifier onViewChangedNotifier_ = new OnViewChangedNotifier();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier_);
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
        setContentView(layout.acticity_main);
    }

    private void init_(Bundle savedInstanceState) {
        OnViewChangedNotifier.registerOnViewChangedListener(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    public static MainActivity_.IntentBuilder_ intent(Context context) {
        return new MainActivity_.IntentBuilder_(context);
    }

    public static MainActivity_.IntentBuilder_ intent(android.app.Fragment fragment) {
        return new MainActivity_.IntentBuilder_(fragment);
    }

    public static MainActivity_.IntentBuilder_ intent(android.support.v4.app.Fragment supportFragment) {
        return new MainActivity_.IntentBuilder_(supportFragment);
    }

    @Override
    public void onViewChanged(HasViews hasViews) {
        MainActivity_LinearLayout_BLEDeviceList = ((LinearLayout) hasViews.findViewById(com.ecg_analysis.R.id.MainActivity_LinearLayout_BLEDeviceList));
        MainActivity_Button_CancelScan = ((FButton) hasViews.findViewById(com.ecg_analysis.R.id.MainActivity_Button_CancelScan));
        MainActivity_LinearLayout_Cancel = ((LinearLayout) hasViews.findViewById(com.ecg_analysis.R.id.MainActivity_LinearLayout_Cancel));
        MainActivity_ToggleButton_IfConnectAuto = ((ToggleButton) hasViews.findViewById(com.ecg_analysis.R.id.MainActivity_ToggleButton_IfConnectAuto));
        MainActivity_Button_ScanBLE = ((FButton) hasViews.findViewById(com.ecg_analysis.R.id.MainActivity_Button_ScanBLE));
        MainActivity_TextView_ShowConnectState = ((TextView) hasViews.findViewById(com.ecg_analysis.R.id.MainActivity_TextView_ShowConnectState));
        MainActivity_ListView_BLEDevice = ((ListView) hasViews.findViewById(com.ecg_analysis.R.id.MainActivity_ListView_BLEDevice));
        MainActivity_LinearLayout_Text = ((LinearLayout) hasViews.findViewById(com.ecg_analysis.R.id.MainActivity_LinearLayout_Text));
        MainActivity_RelativeLayout_ButtonSeries = ((RelativeLayout) hasViews.findViewById(com.ecg_analysis.R.id.MainActivity_RelativeLayout_ButtonSeries));
        if (MainActivity_Button_ScanBLE!= null) {
            MainActivity_Button_ScanBLE.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View view) {
                    MainActivity_.this.MainActivity_Button_ScanBLE();
                }

            }
            );
        }
        if (MainActivity_Button_CancelScan!= null) {
            MainActivity_Button_CancelScan.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View view) {
                    MainActivity_.this.MainActivity_Button_CancelScan();
                }

            }
            );
        }
        {
            View view = hasViews.findViewById(com.ecg_analysis.R.id.MainActivity_Button_ConnectAtNow);
            if (view!= null) {
                view.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View view) {
                        MainActivity_.this.MainActivity_Button_ConnectAtNow();
                    }

                }
                );
            }
        }
        if (MainActivity_ToggleButton_IfConnectAuto!= null) {
            MainActivity_ToggleButton_IfConnectAuto.setOnCheckedChangeListener(new OnCheckedChangeListener() {


                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    MainActivity_.this.MainActivity_ToggleButton_IfConnectAuto();
                }

            }
            );
        }
        if (MainActivity_ListView_BLEDevice!= null) {
            MainActivity_ListView_BLEDevice.setOnItemClickListener(new OnItemClickListener() {


                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MainActivity_.this.MainActivity_ListView_BLEDevice(position);
                }

            }
            );
        }
        init();
    }

    @Override
    public void ConnectDevice() {
        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0, "") {


            @Override
            public void execute() {
                try {
                    MainActivity_.super.ConnectDevice();
                } catch (Throwable e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }

        }
        );
    }

    @Override
    public void scanLeDevice(final boolean enable) {
        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 1000, "") {


            @Override
            public void execute() {
                try {
                    MainActivity_.super.scanLeDevice(enable);
                } catch (Throwable e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }

        }
        );
    }

    public static class IntentBuilder_
        extends ActivityIntentBuilder<MainActivity_.IntentBuilder_>
    {

        private android.app.Fragment fragment_;
        private android.support.v4.app.Fragment fragmentSupport_;

        public IntentBuilder_(Context context) {
            super(context, MainActivity_.class);
        }

        public IntentBuilder_(android.app.Fragment fragment) {
            super(fragment.getActivity(), MainActivity_.class);
            fragment_ = fragment;
        }

        public IntentBuilder_(android.support.v4.app.Fragment fragment) {
            super(fragment.getActivity(), MainActivity_.class);
            fragmentSupport_ = fragment;
        }

        @Override
        public void startForResult(int requestCode) {
            if (fragmentSupport_!= null) {
                fragmentSupport_.startActivityForResult(intent, requestCode);
            } else {
                if (fragment_!= null) {
                    fragment_.startActivityForResult(intent, requestCode);
                } else {
                    super.startForResult(requestCode);
                }
            }
        }

    }

}
