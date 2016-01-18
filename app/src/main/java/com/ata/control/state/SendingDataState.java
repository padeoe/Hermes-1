package com.ata.control.state;

import android.util.Log;
import android.widget.Toast;

import com.ata.control.PlantFormControl;
import com.ata.control.task.TaskControl;
import com.ata.model.transfer.wifip2p.Wifip2pTransporter;
import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskSet;

import java.util.List;

/**
 * Created by raven on 2015/11/8.
 */ //发送方传输状态
public class SendingDataState extends TransferState {
    public SendingDataState(StateMachine stateMachine,PlantFormControl logic, TaskControl manager) {
        super(stateMachine,logic, manager);
    }
    @Override
    public int getCurrentState() {
        return TransferState.SendingDataState;
    }
    @Override
    public void InitState() {
        super.InitState();

        Log.d(PlantFormControl.TAG, "entering state:" + "Sending TaskData");

            if( mLogic.prepareForDistributing()){

                mLogic.broadcastCollectionFinished(mLogic.getCurrentTask().getTaskName());
                mLogic.DistributeTask();
            }else {
                Log.d(mLogic.TAG,"an error occured: task is undividable now");
                mLogic.setState(mStateMachine.getTaskRunningState());

             }
    }

    @Override
    public void onSocketTimeOut(int port) {
        switch (port){
            case Wifip2pTransporter.broadCastPort:

                break;
            case Wifip2pTransporter.taskPort:
                if(mLogic.DistributeFinished()){

                    if(mManager.isPublisher(mLogic.getCurrentTask()))
                        mLogic.setState(mStateMachine.getWaitForResultState());
                    else {

                        mLogic.setState(mStateMachine.getTaskRunningState());
                    }
                }else {
                    mLogic.getCurrentTask().CancelDis();
                    mLogic.onError(mLogic.DIS_OUT_OF_TIME);
                }
                break;
        }
    }
}
