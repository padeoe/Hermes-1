package com.ata.provider.transfer;

import com.ata.model.DeviceInfo;

import java.io.Serializable;

/**
 * Created by raven on 2015/5/25.
 */
public interface Device extends Serializable{
    public static final int Connected=0;
    public static final int Available=3;
    public static final int Failed=2;
    public static final int UnAvailable=4;
    public static final int Invited=1;
    public String getDeviceName();
    public void setDeviceName(String name);
    public void setDeviceMacAddress(String macAddress);
    public String getDeviceMacAddress();
    public DeviceInfo getDeviceInfo();
    public int getDeviceStatus();
    public void  setDeviceInfo(DeviceInfo info);

}
