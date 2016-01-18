package com.ata.model.transfer.wifip2p;

/**
 * Created by raven on 2015/5/13.
 */


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

import com.ata.control.PlantFormControl;


public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private Channel channel;
    private WifiP2pManager.ConnectionInfoListener mListener;
    private WifiP2pManager.PeerListListener mPeerListener;
    private MyWiFiP2pDevice mDevice;
    private  Wifip2pConnector mConnector;
    public MyWiFiP2pDevice getDevice() {
        if(mDevice==null)mDevice=new MyWiFiP2pDevice(null);

        return mDevice;
    }


    public WiFiDirectBroadcastReceiver(WifiP2pManager aManager,
                                       Channel aChannel,
                                       Wifip2pConnector connector,
                                       WifiP2pManager.ConnectionInfoListener connectionInfoListener,
                                       WifiP2pManager.PeerListListener peerListener         ) {
        super();
        manager=aManager;
        channel =aChannel;
        mConnector=connector;
        assert connectionInfoListener!=null;
        assert peerListener!=null;
        mListener=connectionInfoListener;
        mPeerListener=peerListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state==WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Log.d(PlantFormControl.TAG, "P2P state Enabled");
                mConnector.setWifiEnabled(true);
                mConnector.setInitFinished(true);
                //codes here
            }else{
                //codes here
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            requestPeers();
            Log.d(PlantFormControl.TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            Log.d(PlantFormControl.TAG, "P2P connection changed!");
            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
                //codes here
                manager.requestConnectionInfo(channel,mListener);

            } else {

                // It's a disconnect
                //codes here
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            Log.d(PlantFormControl.TAG, "this Device Changed");
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            mDevice=new MyWiFiP2pDevice(device);
            //codes here
        }
    }
    public void requestPeers(){
        if (manager != null) {
            manager.requestPeers(channel, mPeerListener );
        }
    }
}
