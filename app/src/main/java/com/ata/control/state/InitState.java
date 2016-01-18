package com.ata.control.state;

import android.util.Log;

import com.ata.control.PlantFormControl;
import com.ata.control.task.TaskControl;
import com.ata.model.transfer.wifip2p.Wifip2pTransporter;

/**
 * Created by raven on 2015/11/8.
 */ //初始状态
public class InitState extends TransferState {
    public InitState(StateMachine stateMachine,PlantFormControl logic, TaskControl manager) {

        super(stateMachine,logic, manager);
    }

    @Override
    public void InitState() {
        super.InitState();

        Log.d(mLogic.TAG,"entering state:init state");


    }

    @Override
    public void onSocketTimeOut(int port) {
        switch (port){
            case Wifip2pTransporter.broadCastPort:
                //监听端口超时
                Log.d(mLogic.TAG,"no available device");
                mLogic.onError(mLogic.NO_DEVICE);

                break;

        }
    }

    @Override
    public int getCurrentState() {
        return TransferState.InitState;
    }
}
