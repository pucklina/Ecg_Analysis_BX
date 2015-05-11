package com.ecg;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ecg.activity.MainActivity;
import com.ecg_analysis.R;

import org.androidannotations.annotations.SystemService;

/**
 * Created by dongfangyuxiao on 15/5/5.
 */
public class MyApplication extends Application {
    private boolean BlEServiceStart = false;
    private String BLEAutoConnectDeviceAddress ="";
    private String BLEConnectNowDeviceAddress ="";
    public static Context context;

    public boolean isBlEServiceStart() {
        return BlEServiceStart;
    }

    public String getBLEAutoConnectDeviceAddress() {
        return BLEAutoConnectDeviceAddress;
    }

    public String getBLEConnectNowDeviceAddress() {
        return BLEConnectNowDeviceAddress;
    }

    public void setBlEServiceStart(boolean blEServiceStart) {
        BlEServiceStart = blEServiceStart;
    }

    public void setBLEAutoConnectDeviceAddress(String bLEDeviceAddress) {
        BLEAutoConnectDeviceAddress = bLEDeviceAddress;
    }
    public void setBLEConnectNowDeviceAddress(String bLEDeviceAddress) {
        BLEConnectNowDeviceAddress = bLEDeviceAddress;
    }

    private NotificationManager notificationManager;
    private Notification notification;

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
