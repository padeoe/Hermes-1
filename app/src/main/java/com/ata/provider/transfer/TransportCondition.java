package com.ata.provider.transfer;

/**
 * Created by raven on 2015/5/15.
 */
public interface TransportCondition {
    String GetPeerAddress();
   void SetPeerAddress(String address);
    void SetAsServer(boolean server);
   boolean IsServer();
     void SetTimeout(int t);
    int getTimeout();
    int getPort();
   void setPort(int port);

}
