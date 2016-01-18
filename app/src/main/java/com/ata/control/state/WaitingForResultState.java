package com.ata.control.state;

import android.util.Log;

import com.ata.control.AskingForResultRunnable;
import com.ata.control.PlantFormControl;
import com.ata.control.task.TaskControl;

/**
 * Created by raven on 2015/11/8.
 */ //等待结果状态
public class WaitingForResultState extends TransferState {
    public WaitingForResultState(StateMachine stateMachine,PlantFormControl logic, TaskControl manager) {
        super(stateMachine,logic, manager);
    }
    @Override
    public int getCurrentState() {
        return TransferState.WaitingForResultState;
    }
    @Override
    public void InitState() {
        super.InitState();
        //定期广播等待回应
        //打开服务器端监听连接
        Log.d(PlantFormControl.TAG, "entering state:" + "Waiting for Result");

            //开启一个用于查询和接受结果的线程
            String taskName = mLogic.getCurrentTask().getTaskName();

                Log.d(mLogic.TAG, "starting thread asking for result");
                Thread r = new Thread(new AskingForResultRunnable(mLogic, mManager, taskName));

                r.start();

            if(!mLogic.JoinInGroup())mLogic.discover();

           mLogic.setState(mStateMachine.getTaskRunningState());

    }



}
