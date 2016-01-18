package com.ata.control.transfer;

import android.content.Context;

import com.ata.provider.algol.DistributeTable;
import com.ata.provider.transfer.ConnectModule;
import com.ata.provider.transfer.ConnectionListener;
import com.ata.provider.transfer.Device;
import com.ata.provider.transfer.MessageListener;
import com.ata.provider.transfer.TransportCondition;
import com.ata.provider.transfer.TransportableModule;
import com.ata.model.AtaPacket;
import com.ata.model.transfer.wifip2p.Wifip2pConnector;
import com.ata.model.transfer.wifip2p.Wifip2pTransportCondition;
import com.ata.model.transfer.wifip2p.Wifip2pTransporter;

/**
 * Created by raven on 2015/5/15.
 */
public class TransferControl {
    public static  final String WifiP2p="WifiP2p";
    TransportableModule mTransporter=null;
    ConnectModule mConnector=null;
    Context mContext;

    public TransferControl(String Type,
                           Context context,
                           MessageListener messageListener,
                           ConnectionListener connectionListener
    ){
        mContext=context;

        if(Type==WifiP2p){

            mConnector=new Wifip2pConnector();
            mConnector.Init(connectionListener,context );

            mTransporter=new Wifip2pTransporter();
            mTransporter.Init(messageListener,context);
        }

    }

    public void Connect(Device device){mConnector.TryConnect(device);}

    public boolean SendPacket(int port,boolean isServer,AtaPacket packet,String address,int timeout){
            TransportCondition Condition=new Wifip2pTransportCondition(null);
            Condition.SetAsServer(isServer);
            Condition.SetPeerAddress(address);
            Condition.SetTimeout(timeout);
            Condition.setPort(port);
            //   Toast.makeText(mContext,"wrapper sending:"+str,Toast.LENGTH_LONG).show();
            return  mTransporter.SendPacket(packet,Condition);


    }
    public boolean Distribute(DistributeTable table,int timeout){
        TransportCondition Condition=new Wifip2pTransportCondition(null);
        Condition.SetTimeout(timeout);
        Condition.SetAsServer(true);
        return  mTransporter.Distribute(table,Condition);
    }
    public boolean Receive(int port,boolean isServer,String address,int timeout) {

        TransportCondition Condition=new Wifip2pTransportCondition(null);
        Condition.SetPeerAddress(address);
        Condition.SetTimeout(timeout);
        Condition.SetAsServer(isServer);
        Condition.setPort(port);
        mTransporter.Receive(Condition);
        return  true;
    }
    public void start(){mConnector.Start();mTransporter.StartListen(0);}

    public void stop(){mConnector.Stop();mTransporter.StopListen();}

    public void Discover(){mConnector.Discover();}
    public void BroadCast(AtaPacket packet){
        mTransporter.BroadCast(packet);
    }
    public void StartListen(int timeout){
        mTransporter.StartListen(timeout);
    }
    public void StopListen(){
        mTransporter.StopListen();
    }

    public String getThisDeviceIpAddress(){
        return mTransporter.getIpAddress();
    }
    public Device GetLocalDevice(){
        return mConnector.getLocalDevice();
    }
}
