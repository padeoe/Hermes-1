package com.ata.control.state;

import android.util.Log;

import com.ata.control.PlantFormControl;
import com.ata.model.AtaPacket;
import com.ata.provider.transfer.Device;
import com.ata.provider.task.TaskSet;
import com.ata.control.task.TaskControl;
import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskPartition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raven on 2015/7/11.
 */
public  abstract  class TransferState {


    protected PlantFormControl mLogic;
    protected TaskControl mManager;
    protected StateMachine mStateMachine;
    public static final int InitState=0;
    public static final int CollectingPeerInfoState=1;
    public static final int SendingDataState=2;
    public static final int WaitingForResultState=3;
    public static final int StartContributeState=4;
    public static final int WaitingForDataState=5;
    public static final int TaskRunningState =6;

    public void onSocketTimeOut(int port) {

    }
    public int getCurrentState(){
        return  -1;
    }
    public TransferState(StateMachine stateMachine,PlantFormControl logic,TaskControl manager){
        mLogic=logic;
        mManager=manager;
        mStateMachine = stateMachine;
    }
    /*
    定义一些事件和其处理方法
     */

    public void ClearState(){

    }
    public void InitState()  {

    }
    public  void onRemoteTaskFound(TaskInfo task){

    }

    public void onDeviceInfoFound(Device d){
        mLogic.addAckedDevice(d);

    }
    public void onReceiveACK(String taskName,Device device){

    }
    public void onReceiveTransferACK(String taskName){

    }
    public void onReceiveREQUEST(String taskName,String address){

    }

    public void onResultFound(String TaskName, TaskPartition taskPartitions) {

            mManager.addPieces(TaskName, taskPartitions);
            mLogic.onLocalResultFound(TaskName,taskPartitions);
    }
    public void onReceiveRESREQUEST(String taskName, String bossAddress,TaskSet set) {

        if(!bossAddress.equals(mLogic.getLocalDevice().getDeviceInfo().getIPAddress())){
        TaskInfo taskInfo=mManager.getTask(taskName);
        if(taskInfo!=null&&mManager.isFinished(taskName)){
            Log.d(PlantFormControl.TAG, "Receiving result request from:" + bossAddress + " " + taskName + " ");
            Log.d(PlantFormControl.TAG,"taskSet:"+taskInfo.getTaskPartition().toString());
            List<TaskPartition> localFinished =new ArrayList<>();

            for(TaskPartition par:taskInfo.getTaskPartition().toList()){
                if(par.isFinished()){
                    localFinished.add(par);
                }
            }
            if(!set.covered(localFinished,false)){
                //试图将本机任务返回发布者
                TaskInfo tmp = taskInfo.clone(false, true);

                for (TaskPartition p : tmp.getTaskPartition().toList())
                    p.BeforeTransfer();
                AtaPacket packet = new AtaPacket(AtaPacket.RESULT, tmp, "");
                Log.d(mLogic.TAG, "sending result of" + taskName + "to " + bossAddress);
                mLogic.sendResultTo(packet, bossAddress);
            }else {
                Log.d(mLogic.TAG,"local result already sent!");
            }
        }else {
            if(taskInfo==null)
                Log.d(mLogic.TAG,"not tasks I have");
            else
                Log.d(mLogic.TAG,"task Not Finished");
        }
        }
        else {
           // Log.d(mLogic.TAG,"receiving request from self");
        }
    }

}
