package com.ata.model.transfer.wifip2p;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

import com.ata.provider.transfer.ConnectModule;
import com.ata.provider.transfer.ConnectionListener;
import com.ata.provider.transfer.Device;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by raven on 2015/5/15.
 */
public  class Wifip2pConnector extends ConnectModule {
    //WifiP2pConnection needed variable
    public static final String TAG="WifiP2pConnector";
    private WiFiDirectBroadcastReceiver mReceiver=null;
    private IntentFilter mIntentFilter=null;
    private WifiP2pInfo minfo=null;
    private Context mContext;
    private Wifip2pTransportCondition condition;
    private WifiP2pManager mManager=null;
    private Channel mChannel=null;

    public synchronized boolean isInitFinished() {
        return initFinished;
    }

    public synchronized void setInitFinished(boolean initFinished) {
        this.initFinished = initFinished;
    }

    private boolean initFinished=false;
    private boolean wifiEnabled=false;

    public  synchronized boolean isWifiEnabled() {
        return wifiEnabled;
    }

    public synchronized void setWifiEnabled(boolean wifiEnabled) {
        this.wifiEnabled = wifiEnabled;
    }

    //Listeners
    private ConnectionListener mListener;
    private WifiP2pManager.PeerListListener mPeerListener;
    private WifiP2pManager.ConnectionInfoListener mConnectionInfoListener;

   @Override
    public void Init(ConnectionListener Listener, final Context context) {
        mListener=Listener;
        mContext=context;
        mConnectionInfoListener=new WifiP2pManager.ConnectionInfoListener(){
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                //codes heer on Connection
                // The owner IP is now known.

               condition= new Wifip2pTransportCondition(info);
                //reply to upper module
                mListener.onConnectedSuccess(condition);

                //how about the disconnect?
                if(!info.groupFormed)
                    mListener.onConnectionLost();
            }
        };
        mPeerListener =new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                //codes here to get peers info
           // Toast.makeText(context,"peersAvailable",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "peers found:"+peers.getDeviceList().size());
                if(!peers.getDeviceList().isEmpty()) {
                    List<WifiP2pDevice> peersList=new ArrayList<WifiP2pDevice>();
                    peersList.clear();
                    peersList.addAll(peers.getDeviceList());
                    MyWiFiP2pDevice device;
                    List<Device> devices=new ArrayList<Device>() ;
                    for(WifiP2pDevice p:peersList){
                        device=new MyWiFiP2pDevice(p);
                        devices.add(device);
                    }
                    mListener.onPeersAvailable(devices);
                }
            }
        };
       mManager =(WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
       assert  mManager!=null;
       mChannel=mManager.initialize(mContext, mContext.getMainLooper(), null);
       assert  mChannel!=null;
       mReceiver =new WiFiDirectBroadcastReceiver(mManager, mChannel,this,mConnectionInfoListener,mPeerListener);
       mIntentFilter=new IntentFilter();
       mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
       mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
       mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
       mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


       }


    @Override
    public Device getLocalDevice()  {


        return mReceiver.getDevice();
    }


    @Override
    public boolean TryConnect(Device device){

        return WifiP2pConnect(device);
    }


    @Override
    public void Discover(){
        while (!isWifiEnabled()){
            Log.d(TAG,"Please open Wifi!");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (!isInitFinished()) {
          Log.d(TAG,"waiting for initialize");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "pairs discoverd!");
               // mReceiver.requestPeers();
                //Toast.makeText(mContext,"pairs Discovered!",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "no pairs discovered!");
                //Toast.makeText(mContext,"No pairs Discovered",Toast.LENGTH_LONG).show();

            }
        });
    }
    private boolean WifiP2pConnect(Device device){
        WifiP2pConfig config =new WifiP2pConfig();
        config.deviceAddress =device.getDeviceMacAddress();
        config.wps.setup = WpsInfo.PBC;
        mManager.connect(mChannel, config, new ActionListener(){

            @Override
            public void onSuccess() {
                Log.d(TAG, "Connect success!");
                 // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
         //       Toast.makeText(mContext,"Connection success!",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Connect fail.");
             //   Toast.makeText(mContext, "Connect failed. Retry.",
                  //      Toast.LENGTH_SHORT).show();
            }

        });

        return true;
    }
    @Override
    public void Start(){
        mContext.registerReceiver(mReceiver, mIntentFilter);


    }
    @Override
    public void Stop(){

        mContext.unregisterReceiver(mReceiver);

    }


}
