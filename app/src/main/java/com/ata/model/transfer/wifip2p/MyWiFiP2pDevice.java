package com.ata.model.transfer.wifip2p;

import android.net.wifi.p2p.WifiP2pDevice;

import com.ata.model.DeviceInfo;
import com.ata.provider.transfer.Device;
import com.ata.util.sysInfoService;

/**
 * Created by raven on 2015/5/25.
 */
public class MyWiFiP2pDevice implements Device{
    private String deviceName;
    private String deviceMacAddress;
    private int status;
    private DeviceInfo mDeviceInfo=null;
    public MyWiFiP2pDevice(WifiP2pDevice device){
        if(device==null) {
            deviceName= sysInfoService.getDeviceUniqueName();
            deviceMacAddress =null;
            status =Device.Available;
            mDeviceInfo =new DeviceInfo();
        }else {
            deviceName = device.deviceName;
            deviceMacAddress = device.deviceAddress;
            status = device.status;
            mDeviceInfo = new DeviceInfo();
        }
    }
    @Override
    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public void setDeviceName(String name) {
        deviceName=name;
    }

    @Override
    public void setDeviceMacAddress(String macAddress) {
        deviceMacAddress=macAddress;
    }

    @Override
    /*
    this deviceAddress is A MAC address!
     */
    public String getDeviceMacAddress() {
        return deviceMacAddress;
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return mDeviceInfo;
    }

    @Override
    public int getDeviceStatus() {
        return status;
    }

    @Override
    public void setDeviceInfo(DeviceInfo info) {
        mDeviceInfo=info;
    }
    @Override
    public String toString(){

        String device=deviceName+" ability:"+mDeviceInfo.getAbility();
        return device;
    }
}
