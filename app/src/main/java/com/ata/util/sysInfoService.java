package com.ata.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import com.ata.model.DeviceInfo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
/*
 * 通过binder访问系统信息;
 */

public class sysInfoService  {
    private static Context mContext;
    public sysInfoService(Context context) {
        mContext=context;
    }
    public static String getDataDir(){
        if(mContext!=null)
            return mContext.getFilesDir().getAbsolutePath();
        else
            return null;
    }
    public static String getDeviceUniqueName(){
        return Build.MODEL+Build.SERIAL;
    }
    public DeviceInfo GetDeviceInfo(){
        DeviceInfo info=new DeviceInfo(getBatteryInfo(),
                getMemoryInfo(),
                getCurCpuFreq()
                );
        return info;
    }
    private long getMemoryInfo(){
        final ActivityManager activityManager = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);
        return info.availMem;
    }
    private int getBatteryInfo(){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        return level;
    }


    // 实时获取CPU当前频率（单位KHZ）
    private  String getCurCpuFreq() {
        String result = "N/A";
        try {
            FileReader fr = new FileReader(
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            result = text.trim();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}