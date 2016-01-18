package com.ata.control;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


import com.ata.control.state.StateMachine;
import com.ata.control.state.TransferState;
import com.ata.control.transfer.TransferControl;
import com.ata.model.algol.SimpleStrategy;
import com.ata.provider.algol.DistributeTable;
import com.ata.provider.algol.DivideStrategy;
import com.ata.provider.algol.OnDisSuccListener;
import com.ata.model.AtaPacket;
import com.ata.model.DeviceInfo;
import com.ata.provider.transfer.ConnectionListener;
import com.ata.provider.transfer.Device;
import com.ata.provider.transfer.MessageListener;
import com.ata.provider.transfer.TransportCondition;
import com.ata.model.transfer.wifip2p.Wifip2pTransporter;
import com.ata.provider.task.TaskSet;
import com.ata.control.task.TaskControl;
import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskPartition;
import com.ata.provider.view.TransferActionListener;
import com.ata.util.sysInfoService;
import com.ata.view.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raven on 2015/7/9.
 * 这是主要的控制类，它沟通了视图，网络，本地任务管理。
 * 通过一个状态机模型（com.ata.contol.state.StateMachine）管理状态，决定了控制逻辑
 */
public class PlantFormControl {
    public static final int ACKTIMEOUT=5000;//等待响应的时间
    public static final int RESULTTIMEOUT=0;//等待结果的时间
    public static final int TASKTIMEOUT=5000;//等待任务数据的时间
    public static final int CHECKINTERVALS=5000;//检查是否重新开始分发的时间
    //error codes
    public static final int DIS_OUT_OF_TIME=0;
    public static final int NOT_CONNECTED=1;
    public static final int NO_DEVICE =2;
    public static final int ERROR_MESSAGE=3;
    public static final String  TAG="TransportLogic";
    private volatile boolean discovering=false;//指示搜索线程是否在运行
    private static PlantFormControl logic;
    private TransferControl mTransportCenter;//当前传输模型的控制器
    private DivideStrategy mStrategy;//当前算法
    private TaskControl mManager;//当前本地任务管理的控制器

    private Context mContext;
    private boolean joinInGroup;//指示当前是否加入了一个设备组

    TaskInfo currentTask;//当前任务
    private List<Device> peerList;//当前设备列表
    private StateMachine mStateMachine;//状态机


    private DistributeTable table;//当前的分发表
    private List<Device> Ackedpeers;//准备接受任务的设备列表
    private sysInfoService mService;//封装了一些获得本地信息的函数
    private static StateTranHandler stateTranHandler;

    public volatile int AckedPeersNum=0;//响应的设备数




    //考虑加入状态
    public class DiscoverRunnable implements Runnable{
        int DiscoverIntervals=5000;
       // Random r=new Random();
        @Override
        public void run() {
            try {
                discovering=true;
                Log.d(PlantFormControl.TAG,"discover initialize");
                while (! Thread.currentThread().isInterrupted()){
                    if(!JoinInGroup()) {
                        //  Log.d(TAG,"starting discover");
                        mTransportCenter.Discover();
                        Thread.sleep(DiscoverIntervals);

                    }
                    else {
                        Log.d(PlantFormControl.TAG,"already join in group!");
                        discovering=false;
                        Thread.currentThread().interrupt();
                    }

                }
            }catch (InterruptedException e){
                e.printStackTrace();

            }
        }
    }

    /*
    工厂方法
     */
    public static PlantFormControl getTransportLogic(Context ctx,TaskControl manager){
        if(logic==null){
            logic=new PlantFormControl(ctx,manager);

        }
        return logic;
    }

    private PlantFormControl(
            Context context,
            TaskControl manager
           ){

        mContext=context;
        mService =new sysInfoService(mContext);

        mManager=manager;
        mStrategy=new SimpleStrategy(new OnDisSuccListener() {
            @Override
            public void onDisSucceed(String taskName,TaskSet set, String peerName) {
                mManager.RecordDisWorks(taskName,set,peerName);
                ((TransferActionListener)mContext).onDisSuccess(taskName, peerName, set);
            }
        });
        mStateMachine =new StateMachine(this,manager);
        stateTranHandler =new StateTranHandler(mStateMachine,this,manager);

        //将传输逻辑包装后传递给下层模块
        mTransportCenter=new TransferControl(TransferControl.WifiP2p,context, new MessageListener(){

            @Override
            public void onPacketReceive(AtaPacket packet) {
                TransferState currentState = mStateMachine.getCurrentState();
                try {
                    switch(packet.getmType()){
                        case AtaPacket.DEVICEINFO:
                            Device d=(Device)packet.getmContent();
                            currentState.onDeviceInfoFound(d);
                            break;
                        case AtaPacket.RESULT:
                            TaskInfo result=(TaskInfo)packet.getmContent();
                            result.RecoverFromTransfer();
                          //  Log.d(TAG,"result found from  net");
                            if(mManager.getTask(result.getTaskName())!=null) {
                                TaskSet set =result.getTaskPartition();
                                String name =result.getTaskName();
                                List<TaskPartition> list =set.toList();
                                ((TransferActionListener)mContext).onRemoteResultFound(name,set);
                                for (TaskPartition p:list) {
                                    Log.d(PlantFormControl.TAG,"result matched from net:"+p.toString());
                                    currentState.onResultFound(name, p);
                                }
                            }
                            break;
                        case AtaPacket.TASKINFO:
                            TaskInfo task=(TaskInfo)packet.getmContent();
                            task.RecoverFromTransfer();
                            currentState.onRemoteTaskFound(task);

                            break ;
                        case AtaPacket.ACK:
                            String name=packet.getmMessage();
                            Device device=(Device)packet.getmContent();
                            currentState.onReceiveACK(name,device);break;
                        case AtaPacket.REQUEST:
                            String taskName=packet.getmMessage();
                            String address=packet.getSourceAddress();
                            currentState.onReceiveREQUEST(taskName,address);break;
                        case AtaPacket.RESREQUEST:
                            String ReqTaskName=packet.getmMessage();
                            String BossAddress=packet.getSourceAddress();
                            TaskSet set =(TaskSet)packet.getmContent();
                            currentState.onReceiveRESREQUEST(ReqTaskName,BossAddress,set);break;
                        case AtaPacket.TRANSFERACK:
                            String TransferTaskName=packet.getmMessage();
                            currentState.onReceiveTransferACK(TransferTaskName);break;
                        default:break;
                    }
                }catch (ClassCastException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onSocketTimeOut(int port) {
                TransferState currentState = mStateMachine.getCurrentState();

                currentState.onSocketTimeOut(port);
            }

            @Override
            public void onError(int reason) {

            }
        }
                ,new ConnectionListener(){

            @Override
            public void onConnectedSuccess(TransportCondition condition) {
                setJoinInGroup(true);
                Log.d(TAG, "group formed!");
            }

            @Override
            public void onConnectionLost() {
                Log.d(TAG,"connection lost!");
                peerList=null;
                onPeersAvailable(peerList);
                setJoinInGroup(false);
            }

            @Override
            public void onPeersAvailable(List<Device> peers) {
                updateDeviceInfo(peers);
                if(peers!=null){
                for(Device d:peers) {
                    if(d.getDeviceStatus()==Device.Available) {
                        Log.d(TAG,"is available:"+d.getDeviceMacAddress()+"name:"+d.getDeviceName());
                        mTransportCenter.Connect(d);
                    }
                }
                }
                MainActivity activity =(MainActivity)mContext;
                (activity).onPeersAvailable(peers);

            }
        });
        //初始化状态

        peerList=null;
        AckedPeersNum=0;
        Ackedpeers=null;
        joinInGroup=false;

    }
    public void  setState(TransferState state){
        mStateMachine.setNextState(state);
    }

    public void waitingForRedivide(){
        Log.d(TAG,"start thread checking whether task is redividable");
        stateTranHandler.waitForRedivide();

    }
    public synchronized boolean  JoinInGroup(){
        return peerList!=null&&joinInGroup;
    }
    public synchronized void setJoinInGroup(boolean b){
        joinInGroup=b;
    }
    public void setCurrentTask(TaskInfo taskInfo){
        currentTask=taskInfo;
    }
    public TaskInfo getCurrentTask(){
        return currentTask;
    }
    public void  initBeforeTran(){

        discover();
        startListen(0);


    }
    public void  discover(){
        if(!discovering) {
            Thread thread = new Thread(new DiscoverRunnable());
            thread.start();
        }
     }
    public void start(){

        mStrategy.LoadInfo();
        mTransportCenter.start();
        discover();
    }
    public void stop(){
        mStrategy.SaveInfo();
        mTransportCenter.stop();
    }
    public void StartContributing(){
        initBeforeTran();
        setState(mStateMachine.getStartContributeState());
    }
    //用于提供给taskManager通知本机结果信息
    public void onLocalResultFound(String TaskName,TaskPartition taskPartitions) {
        if (mManager.isFinished(TaskName)) {

            if (mManager.isPublisher(mManager.getTask(TaskName))) {
                TaskInfo taskInfo=mManager.getTask(TaskName);
                Log.d(TAG, "Task " + TaskName + "Finished:"+taskInfo.getTaskPartition());
            } else {
                //接受的任务已经完成了
                setState(mStateMachine.getStartContributeState());
            }
        }
    }
    //进入分发状态
    public void onLocalTaskFound(TaskInfo task){
        TaskInfo record=mManager.getTask(task.getTaskName());
        if(record!=null&&record.getTaskPartition().covered(task.getTaskPartition().toList(),false)) {
            Log.d(TAG, "adding task  already covered");

        }else {
            mManager.RecordWorks(task.getTaskName(), task.getTaskPartition().toList());

            if(record!=null) {
                task.getTaskPartition().merge(record.getTaskPartition());

            }

            setCurrentTask(task);
            if (mManager.addable(task.getTaskName()))
                mManager.addTask(task);

            setState(mStateMachine.getCollectState());

        }
    }

    public void addAckedDevice(Device d){
        if(Ackedpeers==null)Ackedpeers=new ArrayList<>();
        if(!Ackedpeers.contains(d))Ackedpeers.add(d);
        if(peerList!=null) {
            for (Device device : peerList) {
                Log.d(TAG, "device address in list " + device.getDeviceMacAddress());
                if (d.getDeviceName().equals(device.getDeviceName())) {
                    if (d.getDeviceInfo() != null) {
                        Log.d(TAG, "updating deviceinfo: IP:" + d.getDeviceInfo().getIPAddress());
                        /*update Mac together
                        *  2015/11/9
                        * */
                        device.setDeviceMacAddress(d.getDeviceMacAddress());

                        device.setDeviceInfo(d.getDeviceInfo());
                    } else {
                        Log.d(TAG, "DeviceInfo is null!");
                    }
                    return;
                }
            }
            peerList.add(d);
        }else {
            peerList =new ArrayList<>();
            peerList.add(d);
        }
        ((TransferActionListener)mContext).onPeersAvailable(peerList);
    }
    public Device getLocalDevice(){
        Device d=mTransportCenter.GetLocalDevice();
//      通过service补完本机设备信息
       /*
        String deviceUniqName =sysInfoService.getDeviceUniqueName();
        if(d.getDeviceName()==null||!d.getDeviceName().equals(deviceUniqName))
        {
            Log.d(TAG, "resetting name from " + d.getDeviceName() + " to " + deviceUniqName);

            d.setDeviceName(deviceUniqName);
        }

        WifiManager wifi = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifi.getConnectionInfo();
        String macAddress =wInfo.getMacAddress();
        if(d.getDeviceMacAddress()==null||!macAddress.equals(d.getDeviceMacAddress()))
        {
            Log.d(TAG, "resetting device mac address from " + d.getDeviceMacAddress() + " to " + macAddress);
            d.setDeviceMacAddress(macAddress);
        }*/
        DeviceInfo info=mService.GetDeviceInfo();
        info.setIPAddress(mTransportCenter.getThisDeviceIpAddress());
        d.setDeviceInfo(info);
        return d;
    }
    public  static String getDeviceStatus(int status){
        switch (status){
            case Device.Connected:return "Connected";
            case Device.Available:return "Available";
            case Device.Failed:return "Failed";
            case Device.UnAvailable:return "UnAvailable";
            case Device.Invited:return "Invited";
            default :return "unKnown";
        }
    }
    public void onError(int reason){
        //setState(getInitState());
    }
    public boolean dividable(){
        boolean dividable = currentTask.dividable()&&JoinInGroup();
        if(!dividable)currentTask.CancelDis();
        return dividable;
    }
    public void sendResultTo(AtaPacket packet, String address){
        mTransportCenter.SendPacket(Wifip2pTransporter.ResultPort, false, packet, address, TASKTIMEOUT);
    }
    public void Receive(int port,boolean isServer,int timeout,String address) {
        mTransportCenter.Receive(port, isServer, address, timeout);
    }


    public void startListen(int t){mTransportCenter.StartListen(t);}
 /* void stopListen(){
        mTransportCenter.StopListen();
    }
*/


    public boolean prepareForDistributing(){
        addAckedDevice(getLocalDevice());
    //    printDeviceList();
        mStrategy.updateData(Ackedpeers);
        mStrategy.SetTask(currentTask);
        table= mStrategy.GetNewDivision();
         if(table==null) return false;
        String localAddress=getLocalDevice().getDeviceInfo().getIPAddress();
        TaskSet p=table.getDeviceJob(localAddress);
        Log.d(TAG,"local task is "+p.toString());
        currentTask.addTaskPartition(p);
        currentTask.CancelDis();
        table.remove(localAddress);
        return true;
    }
   public boolean DistributeFinished(){
        Ackedpeers=null;
        AckedPeersNum =0;
        return  table.isEmpty();
    }
    //收发信息用到的函数
   public void broadCastTaskInfo(TaskInfo taskInfo){
        Log.d(TAG,"Broadcasting taskinfo:"+taskInfo.getTaskName());
        AtaPacket packet=new AtaPacket(AtaPacket.REQUEST,null, taskInfo.getTaskName());
        packet.setSource(getLocalDevice().getDeviceName(), getLocalDevice().getDeviceInfo().getIPAddress());
        mTransportCenter.BroadCast(packet);
    }
  public   void DistributeTask() {
        Log.d(TAG, "Distributing task:" + table.GetTask().getTaskName());
        mTransportCenter.Distribute(table, TASKTIMEOUT);
    }
   public void sendACK(String taskName,String address){
        Device d=getLocalDevice();
        Log.d(TAG,"Local device IPaddress is"+d.getDeviceInfo().getIPAddress());
        Log.d(TAG, "Sending ack to " + address);
        AtaPacket p = new AtaPacket(AtaPacket.ACK, d, taskName);
        mTransportCenter.SendPacket(Wifip2pTransporter.controlPort, false, p, address, ACKTIMEOUT);
    }
   public void broadcastCollectionFinished(String taskName){
        Log.d(TAG,"Broadcasting TransferACK"+taskName);
        AtaPacket packet=new AtaPacket(AtaPacket.TRANSFERACK,null, taskName);
        packet.setSource(getLocalDevice().getDeviceName(), getLocalDevice().getDeviceInfo().getIPAddress());
        mTransportCenter.BroadCast(packet);
    }
   public void AskingForResult(String taskName){
        Log.d(TAG,"Asking for result:"+taskName);
        TaskInfo taskInfo=mManager.getTask(taskName);
        Log.d(TAG,"currentset: "+taskInfo.getTaskPartition().toString());
        if(taskInfo==null){
            Log.d(TAG,"error! can not ask result for task unadded");
        }
        else {
            AtaPacket packet = new AtaPacket(AtaPacket.RESREQUEST, taskInfo.getTaskPartition(), taskName);
            packet.setSource(getLocalDevice().getDeviceName(), getLocalDevice().getDeviceInfo().getIPAddress());
            mTransportCenter.BroadCast(packet);
        }
    }

    //显示设备列表到控制台
    private void printDeviceList(){
        for(Device d:peerList ) {
            Log.d(TAG, " deviceinfo: " + d.getDeviceName()
                    + " " + d.getDeviceMacAddress()
                    + " " + getDeviceStatus(d.getDeviceStatus())
                    + " IP: " + d.getDeviceInfo().getIPAddress()
                    + " ability:"+d.getDeviceInfo().getAbility()
            );
        }
    }

   public void updateDeviceInfo(List<Device> peers){
        for(Device d:peers ) {
        Log.d(TAG, "updating deviceInfo: " + d.getDeviceName() + " "+d.getDeviceMacAddress()+" "+getDeviceStatus(
                d.getDeviceStatus()));
    }
        if(peerList==null){
            peerList=peers;
            return;
        }

        for(Device device:peers){
            for (Device old:peerList){
                if (device.getDeviceName().equals(old.getDeviceName())){
                    if(device.getDeviceInfo()==null){
                        device.setDeviceInfo(old.getDeviceInfo());
                    }
                    if(device.getDeviceMacAddress()==null){
                        device.setDeviceMacAddress(old.getDeviceMacAddress());
                    }
                }

            }

        }
        peerList=peers;
    }

}


