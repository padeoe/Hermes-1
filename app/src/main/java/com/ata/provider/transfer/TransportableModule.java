package com.ata.provider.transfer;

import android.content.Context;

import com.ata.provider.algol.DistributeTable;
import com.ata.model.AtaPacket;


/**
 * Created by raven on 2015/5/13.
 */
public abstract class TransportableModule {

    public abstract void Init(MessageListener Listener, Context context);
    public abstract boolean SendPacket(AtaPacket packet,TransportCondition condition);
    //分发一系列具有不同目的的包到各自目的地
    public abstract boolean Distribute(DistributeTable table,TransportCondition condition);
    public abstract boolean Receive(TransportCondition condition);
    public abstract boolean BroadCast(AtaPacket packet);
    public abstract boolean StartListen(int timeout);
    public abstract boolean StopListen();
    public abstract String getIpAddress();
    //protected MessageListener mMessageListener;
    public static final int MSGPACKET=1;
    public static final int MSGTIMEOUT=2;
    public static final int MSGERROR=3;
    public static final int MSGSUCCESS=4;
}
