package com.ata.provider.transfer;


import com.ata.model.AtaPacket;

/**
 * Created by raven on 2015/5/15.
 */
public interface MessageListener {


    void onPacketReceive(AtaPacket packet);
   void onSocketTimeOut(int port);
   void onError(int reason);
    /*
    public void onMessageReceive(String str);
    public void onResultReceive(Task task);
    public void onTaskReceive(Task task);
    public void onDeviceInfoReceive(DeviceInfo deviceInfo);
    public void onServerInfoReceive(AtaPacket packet);
    */
}
