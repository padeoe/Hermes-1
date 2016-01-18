package com.ata.model.transfer.wifip2p;

import android.net.wifi.p2p.WifiP2pInfo;

import com.ata.provider.transfer.TransportCondition;

/**
 * Created by raven on 2015/5/15.
 */
public class Wifip2pTransportCondition implements TransportCondition {
    WifiP2pInfo mInfo;
    boolean isGroupOwner=false;
    String currentPeerAddress=null;
    boolean isServer;
    int timeout=5000;
    int port=0;
    public Wifip2pTransportCondition(WifiP2pInfo Info){
        if(Info!=null) {
            mInfo = Info;
            isGroupOwner = mInfo.isGroupOwner;
        }
        else {
            mInfo=null;
            isGroupOwner=false;
        }
    }

    @Override
    public String GetPeerAddress() {
       // if(!mInfo.isGroupOwner)
            return currentPeerAddress;
       /// else
          // return null;
    }
    /*
    @Override
    public boolean isGroupOwner() {
        return isGroupOwner;
    }

    @Override
    public boolean Prepared() {
        return currentPeerAddress!=null&&port!=0;
    }
    */
    @Override
    public void SetPeerAddress(String address) {
        currentPeerAddress=address;
    }

    @Override
    public void SetAsServer(boolean server) {
        isServer=server;
    }

    @Override
    public boolean IsServer() {
        return isServer;
    }

    @Override
    public void SetTimeout(int t) {
        timeout=t;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int p) {
        port=p;
    }

    public  WifiP2pInfo getInfo(){
        return  mInfo;
    }

}
