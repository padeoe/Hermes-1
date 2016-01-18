package com.ata.model.transfer.wifip2p;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ata.model.algol.DistributeHashTable;
import com.ata.provider.algol.DistributeTable;
import com.ata.model.AtaPacket;
import com.ata.control.PlantFormControl;
import com.ata.provider.transfer.MessageListener;
import com.ata.provider.transfer.TransportCondition;
import com.ata.provider.transfer.TransportableModule;
import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskSet;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

/**
 * Created by raven on 2015/5/15.
 */
public class Wifip2pTransporter extends TransportableModule {
    private MessageListener mListener;
    private Context mcontext;
    public static final String TAG="Wifi direct";
    public static final int broadCastPort =8888;
    public static final int taskPort =8889;
    public static final int controlPort=8887;
    public static final int ResultPort =8886;

    //for broadcast
    private WifiManager.MulticastLock lock;
    private SimpleHandler mHandler;
    private boolean ListenThreadFlag=false;
    private Thread BroadCastListenThread;
    private Thread BroadCastSendThread;
    private ByteArrayOutputStream bo;
    private ObjectOutputStream oo;
    private DatagramSocket dgSocket;
    class SimpleHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1==MSGSUCCESS){
                DistributeTable dt =(DistributeTable)msg.obj;
                Bundle bundle =msg.getData();
                String peerName=bundle.getString("peerName");
                TaskSet set =(TaskSet)bundle.getSerializable("set");
                dt.onDisSucceed(set,peerName);
                return;
            }

            AtaPacket Atapacket = (AtaPacket)msg.obj;
            switch (msg.arg1) {

                case MSGPACKET:   mListener.onPacketReceive(Atapacket);break;
                case MSGTIMEOUT: mListener.onSocketTimeOut(msg.arg2);break;
                default:mListener.onError(PlantFormControl.ERROR_MESSAGE);
            }
        }
        public void sendPacket(AtaPacket packet){
            Message msg=mHandler.obtainMessage();
            msg.arg1=MSGPACKET;
            msg.obj=packet;
            mHandler.sendMessage(msg);
        }
        public void sendSocketTimeout(int port) {
            Message msg=mHandler.obtainMessage();
            msg.arg1=MSGTIMEOUT;
            msg.arg2=port;
            mHandler.sendMessage(msg);
        }
        public void sendDisSuccess(TaskSet set,String peerName,DistributeTable table){
            Message msg=mHandler.obtainMessage();
            msg.obj =table;
            msg.arg1=MSGSUCCESS;
            Bundle bundle =new Bundle();
            bundle.putSerializable("set",set);
            bundle.putString("peerName", peerName);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }
    class ListenRunnable implements Runnable {
        ByteArrayInputStream bi=null;
        ObjectInputStream oi=null;
        DatagramSocket dgSocket=null;
        private WifiManager.MulticastLock mlock;
        private SimpleHandler mHandler;
        private int timeout;
        public ListenRunnable(WifiManager.MulticastLock Lock,SimpleHandler Handler,int t){
            mlock=Lock;
            mHandler=Handler;
            timeout=t;
        }
        @Override
        public void run() {
            try {
                mlock.acquire();
                dgSocket = new DatagramSocket(
                        Wifip2pTransporter.broadCastPort);
                dgSocket.setSoTimeout(timeout);
                dgSocket.setBroadcast(true);
                while (true) {
                    if(Thread.interrupted()){
                        ListenThreadFlag=false;
                        break;
                    }

                    byte[] by = new byte[10240];
                    DatagramPacket packet = new DatagramPacket(by, by.length);
                    dgSocket.receive(packet);
                    by = packet.getData();
                    bi = new ByteArrayInputStream(by);
                    oi = new ObjectInputStream(bi);
                    AtaPacket reply = (AtaPacket) oi.readObject();
                    mHandler.sendPacket(reply);
                }
            }
                catch (SocketTimeoutException e){
                    Log.d(TAG,"listening socket timeout");
                    mHandler.sendSocketTimeout(Wifip2pTransporter.broadCastPort);

                }
                catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }finally {
                    mlock.release();
                    try {
                        if(bi!=null)
                            bi.close();
                    }
                    catch (IOException e){
                    }
                    try {
                        if(oi!=null)
                            oi.close();
                    }
                    catch (IOException e){
                    }
                    try {
                        if(dgSocket!=null)
                            dgSocket.close();
                    }
                    catch (Exception e){
                    }
                }

        }
    }
    class BroadcastRunnable implements Runnable{
        AtaPacket mPacket;
        public BroadcastRunnable(AtaPacket packet){
            mPacket=packet;
        }
        @Override
        public void run() {
            byte[] bytes=null;
            try {
                lock.acquire();
                DatagramSocket dgSocket=new DatagramSocket();


                // object to bytearray
                bo = new ByteArrayOutputStream();
                oo = new ObjectOutputStream(bo);
                oo.writeObject(mPacket);
                InetAddress local = null;
                try {
                    local = InetAddress.getByName("192.168.49.255");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                bytes = bo.toByteArray();
                DatagramPacket dgPacket=new DatagramPacket(bytes,bytes.length,
                        local,Wifip2pTransporter.broadCastPort);
                dgSocket.send(dgPacket);

            } catch (Exception e) {
                e.printStackTrace();

            }finally {

                lock.release();
                try {
                    if (bo!=null)
                        bo.close();
                } catch (IOException e) {

                }
                try {
                    if (oo!=null)
                        oo.close();
                } catch (IOException e) {

                }
                try {
                    if(dgSocket!=null){
                        dgSocket.close();
                    }
                }catch (Exception e){

                }
            }
        }
    }
    class ClientRunnable implements Runnable{
        String host;
        int timeout;
        int port;
        ObjectInputStream mmInputStream=null;
        ObjectOutputStream mmOutputStream=null;
        Socket socket;
        SimpleHandler mHandler;
        AtaPacket packetToSend;
        ClientRunnable(String address, int Port,int t, SimpleHandler handler, AtaPacket packet){
            host=address;
            port=Port;
            timeout=t;
            mHandler=handler;
            packetToSend=packet;
        }
        @Override
        public void run() {
            Log.d(TAG,"try open client socket address:"+host);
            try {
                socket =new Socket();
                socket.bind(null);
                socket.connect(new InetSocketAddress(
                        host
                        , port)
                        , timeout);


                mmOutputStream =new ObjectOutputStream(socket.getOutputStream());
                mmInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

                if(packetToSend!=null) {
                    mmOutputStream.writeObject(packetToSend);
                    AtaPacket reply = (AtaPacket) mmInputStream.readObject();
                    if(reply.getmType()==AtaPacket.REPLY){
                        Log.d(TAG,"server receive success");
                    }
                }
                else {
                    AtaPacket reply = (AtaPacket) mmInputStream.readObject();
                    AtaPacket sendback =new AtaPacket(AtaPacket.REPLY,null,null);
                    mmOutputStream.writeObject(sendback);
                    mHandler.sendPacket(reply);
                }
               //
             //   }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }  catch (SocketTimeoutException e) {
                mHandler.sendSocketTimeout(port);

            }  catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally
            {
                try {
                    if(socket!=null)
                        socket.close();
                }catch (IOException e){}
                try {
                    if(mmInputStream!=null)
                        mmInputStream.close();
                }catch (IOException e){}
                try {
                    if(mmOutputStream!=null)
                        mmOutputStream.close();
                }catch (IOException e){}
                socket=null;
                mmInputStream=null;
                mmOutputStream=null;
            }
        }
    }
    class ServerRunnable implements Runnable{
        int timeout;
        int mPort;
        ObjectInputStream mmInputStream=null;
        ObjectOutputStream mmOutputStream=null;
        Socket msocket;
        ServerSocket ssocket;
        SimpleHandler mHandler;
        AtaPacket packetToSend;
        ServerRunnable(int port,int t, SimpleHandler handler, AtaPacket packet){
            mPort=port;
            timeout=t;
            mHandler=handler;
            packetToSend=packet;
        }
        @Override
        public void run() {


            try {
                    try {ssocket = new ServerSocket(mPort);

                    }catch (IOException e){
                        Log.d(TAG,"port already served!");
                        return;
                    }


                    ssocket.setSoTimeout(timeout);
                    while (true) {
                    msocket = ssocket.accept();


                    Log.d(TAG, " accept client socket address:" + msocket.getInetAddress().getHostAddress());
                    mmInputStream = new ObjectInputStream(new BufferedInputStream(msocket.getInputStream()));
                    mmOutputStream = new ObjectOutputStream(msocket.getOutputStream());
                    if (packetToSend == null) {
                        AtaPacket reply = (AtaPacket) mmInputStream.readObject();
                        AtaPacket sendback =new AtaPacket(AtaPacket.REPLY,null,null);
                        mmOutputStream.writeObject(sendback);
                        mHandler.sendPacket(reply);
                    } else {
                        mmOutputStream.writeObject(packetToSend);
                        AtaPacket reply = (AtaPacket) mmInputStream.readObject();
                        if(reply.getmType()==AtaPacket.REPLY){
                            Log.d(TAG," client receive success!");
                        }
                    }

                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }  catch (SocketTimeoutException e) {
                mHandler.sendSocketTimeout(mPort);

            }  catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally
            {
                try {
                if (msocket != null) {
                    msocket.close();

                }
                 }catch (IOException e){}

                    try {
                        if(ssocket!=null) {
                            ssocket.close();

                        }
                  }catch (IOException e){}
                try {
                    if(mmInputStream!=null)
                        mmInputStream.close();
                }catch (IOException e){}
                try {
                    if(mmOutputStream!=null)
                        mmOutputStream.close();
                }catch (IOException e){}
                ssocket = null;
                mmInputStream=null;
                mmOutputStream=null;
            }
        }
    }
    class DistributeRunable implements Runnable{
        private TaskInfo task;
        private int timeout;
        private int port;
        private SimpleHandler mHandler;
        private DistributeTable table;
        private ServerSocket mSocket;
        private ObjectInputStream mmInputStream=null;
        private ObjectOutputStream mmOutputStream=null;
        public DistributeRunable(TaskInfo aTask,int t,SimpleHandler handler,DistributeTable aTable){
            port=Wifip2pTransporter.taskPort;
            task=aTask.clone(true,false);
            timeout=t;
            mHandler=handler;
            table=aTable;
        }
        @Override
        public void run() {
            Socket Client;
            try{

                mSocket = new ServerSocket(port);
                mSocket.setSoTimeout(timeout);


                while (true)

                {
                    if (!table.isEmpty()) {
                        Client  = mSocket.accept();

                        mmInputStream =new ObjectInputStream(Client.getInputStream());
                        mmOutputStream =new ObjectOutputStream(Client.getOutputStream());
                        String address = Client.getInetAddress().getHostAddress();
                        Log.d(TAG,"In distribution: Client accepted:"+address);
                        TaskSet p = table.getDeviceJob(address);

                        if (p != null) {
                            task.setTaskPartition(p);
                            table.remove(address);
                            Log.d(TAG,"found pieces for client:"+p);

                        } else {
                            //该地址不在分发表里,可能是错过了时点
                            AtaPacket reply=(AtaPacket)mmInputStream.readObject();
                            mHandler.sendPacket(reply);
                            return;
                        }
                        //进行分发
                        AtaPacket packet = new AtaPacket(AtaPacket.TASKINFO, task, null);
                        mmOutputStream.writeObject(packet);
                        AtaPacket reply=(AtaPacket)mmInputStream.readObject();
                        if(reply.getmType()==AtaPacket.REPLY) {
                            Log.d(TAG, "distribute success");
                            mHandler.sendDisSuccess(p,address,table);
                        }
                        try {
                            Client.close();
                        }catch (IOException e){

                        }
                        Client=null;
                    } else {
                        Log.d(TAG,"empty distribution table!");
                        //分发表已空，已经分发完成
                        mHandler.sendSocketTimeout(Wifip2pTransporter.taskPort);
                        break;
                    }
                }

            }

         catch (SocketTimeoutException e) {
           mHandler.sendSocketTimeout(Wifip2pTransporter.taskPort);
           } catch (IOException e) {
        e.printStackTrace();
           } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
            }
            try {
                if(mSocket!=null) {
                    mSocket.close();

                }
            }catch (IOException e){}
            try {
                if(mmInputStream!=null)
                    mmInputStream.close();
            }catch (IOException e){}
            try {
                if(mmOutputStream!=null)
                    mmOutputStream.close();
            }catch (IOException e){}
            mSocket = null;
            mmInputStream=null;
            mmOutputStream=null;
            }
    }
    @Override
    public  void Init(MessageListener Listener, Context context) {
        mListener=Listener;
        mcontext=context;
        mHandler=new SimpleHandler();

        WifiManager manager = (WifiManager) mcontext
                .getSystemService(Context.WIFI_SERVICE);
         lock= manager.createMulticastLock("test wifi");

    }



    @Override
    public boolean SendPacket(AtaPacket packet, TransportCondition condition) {
        if(condition==null)
            return  false;
        int t=condition.getTimeout();
        boolean IsServer=condition.IsServer();
        int port=condition.getPort();
        if(IsServer){


                    Thread thread = new Thread(new ServerRunnable(port, t,
                            mHandler, packet));
                    thread.start();


            return true;
        }else {
            Thread thread=new Thread(new ClientRunnable(condition.GetPeerAddress(),port,t,
                    mHandler,packet) );
            thread.start();
            return  true;
        }

    }

    @Override
    public boolean Distribute(DistributeTable table, TransportCondition condition) {
        if(condition==null)
            return  false;
        int t=condition.getTimeout();
        boolean IsServer=condition.IsServer();

        if(IsServer) {
                 TaskInfo task = table.GetTask();

                    Thread r = new Thread(new DistributeRunable(task, t, mHandler, table));
                    r.start();


        }
        return false;
    }

    @Override
    public boolean Receive(TransportCondition condition) {

        if(condition==null)
            return  false;
        int t=condition.getTimeout();
        boolean IsServer=condition.IsServer();
        int port=condition.getPort();
        if(IsServer){
            Log.d(TAG,"as Server receiving port:"+port);
                //  ServerSendAsyncTask task=new ServerSendAsyncTask(condition,mcontext,mListener,
                Thread thread = new Thread(new ServerRunnable(port,t,
                        mHandler, null));
                thread.start();

            return true;
        }else {
            Log.d(TAG,"as Client receiving from:"+condition.GetPeerAddress());

            //is a client connected to the group owner
            Thread thread=new Thread(new ClientRunnable(condition.GetPeerAddress(),port,t,
                    mHandler,null) );
            thread.start();
            return  true;
        }
    }

    @Override
    public synchronized boolean BroadCast(AtaPacket packet) {

           BroadCastSendThread = new Thread(new BroadcastRunnable(packet));

           BroadCastSendThread.start();

           return true;

   }

    @Override
    public synchronized boolean  StartListen(int timeout) {
        if(!ListenThreadFlag) {
            Log.d(TAG,"starting listening for broadcast");
            ListenThreadFlag = true;
            BroadCastListenThread=new Thread(new ListenRunnable(lock,mHandler,timeout) );
            BroadCastListenThread.start();
            ListenThreadFlag=false;
        }
        return false;
    }

    @Override
    public synchronized boolean StopListen() {
        if(ListenThreadFlag) {
            BroadCastListenThread.interrupt();
            ListenThreadFlag=false;
        }
        return false;
    }
    //Get this method from stackoverflow.com
    @Override
    public  String getIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());
        /*
         * for (NetworkInterface networkInterface : interfaces) { Log.v(TAG,
         * "interface name " + networkInterface.getName() + "mac = " +
         * getMACAddress(networkInterface.getName())); }
         */

            for (NetworkInterface intf : interfaces) {
             //   if (!getMACAddress(intf.getName()).equalsIgnoreCase(
                 //       Settings.Global.)) {
                    // Log.v(TAG, "ignore the interface " + intf.getName());
                    // continue;
               // }
                if (!intf.getName().contains("p2p"))
                    continue;

              //  Log.v(TAG,
                //        intf.getName() + "   " + getMACAddress(intf.getName()));

                List<InetAddress> addrs = Collections.list(intf
                        .getInetAddresses());

                for (InetAddress addr : addrs) {
                    // Log.v(TAG, "inside");

                    if (!addr.isLoopbackAddress()) {
                        // Log.v(TAG, "isnt loopback");
                        String sAddr = addr.getHostAddress().toUpperCase();
                      //  Log.v(TAG, "ip=" + sAddr);

                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);

                        if (isIPv4) {
                            if (sAddr.contains("192.168.49.")) {
                                Log.v(TAG, "ip = " + sAddr);
                                return sAddr;
                            }
                        }

                    }

                }
            }

        } catch (Exception ex) {
            Log.v(TAG, "error in parsing");
        } // for now eat exceptions
        Log.v(TAG, "returning empty ip address");
        return "";
    }

    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName))
                        continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null)
                    return "";
                StringBuilder buf = new StringBuilder();
                for (int idx = 0; idx < mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length() > 0)
                    buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
        /*
         * try { // this is so Linux hack return
         * loadFileAsString("/sys/class/net/" +interfaceName +
         * "/address").toUpperCase().trim(); } catch (IOException ex) { return
         * null; }
         */
    }



}
