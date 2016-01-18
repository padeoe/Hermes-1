package com.ata.control.state;

import android.util.Log;

import com.ata.control.PlantFormControl;
import com.ata.control.task.TaskControl;
import com.ata.model.transfer.wifip2p.Wifip2pTransporter;
import com.ata.provider.transfer.Device;

/**
 * Created by raven on 2015/11/8.
 */ //分发任务前收集信息的状态
public class   CollectingPeerInfoState extends TransferState {

    public CollectingPeerInfoState(StateMachine stateMachine,PlantFormControl logic, TaskControl manager) {
        super(stateMachine,logic, manager);
    }
    @Override
    public int getCurrentState() {
        return TransferState.CollectingPeerInfoState;
    }
    @Override
    public void InitState() {
        super.InitState();
        Log.d(PlantFormControl.TAG, "entering state:" + "collecting info");
        mLogic.AckedPeersNum=0;
       // if(mLogic.){

         //   mLogic.onError(mLogic.NOT_CONNECTED);
         //   mLogic.setState(mLogic.getWaitForResultState());
     //   }
        //广播任务信息
        if(mLogic.dividable()) {
            mLogic.broadCastTaskInfo(mLogic.getCurrentTask());
            mLogic.Receive(Wifip2pTransporter.controlPort, true, mLogic.ACKTIMEOUT, null);

        }else {

            if(!mLogic.JoinInGroup()){
                Log.d(mLogic.TAG,"we havnt join in a group");

                mLogic.discover();
            }else {
                Log.d(mLogic.TAG,"undividable task!");
            }
            mLogic.setState(mStateMachine.getTaskRunningState());
        }
    }

    @Override
    public void onReceiveACK(String taskName,Device device) {
        super.onReceiveACK(taskName, device);
        Log.d(PlantFormControl.TAG, "Receiving ACK from" + device.getDeviceName() + "address:" + device.getDeviceInfo().getIPAddress());
        //获得了一个设备对于taskName 任务的回应
        mLogic.addAckedDevice(device);
        mLogic.AckedPeersNum++;
        //响应人数达到最大搜索人数
        /*
        if(mLogic.AckedPeersNum==mLogic.getPeerList().size()){
            onSocketTimeOut(Wifip2pTransporter.controlPort);
        }
        */

    }



    @Override
    public void onSocketTimeOut(int port) {
        switch (port){
            case Wifip2pTransporter.broadCastPort:


                break;
            case Wifip2pTransporter.controlPort:
              //超时表示与周围设备的信息交换完成了，进入传输状态
                if(mLogic.AckedPeersNum!=0&&mLogic.dividable())
                    mLogic.setState(mStateMachine.getSendDataState());
                else {
                    mLogic.getCurrentTask().CancelDis();
                    if(mLogic.getCurrentTask()!=null){
                        mLogic.setState(mStateMachine.getTaskRunningState());
                    }else {
                        return;
                    }
                }
                break;
        }
    }
}
