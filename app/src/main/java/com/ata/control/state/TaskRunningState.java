package com.ata.control.state;

import android.util.Log;

import com.ata.control.PlantFormControl;
import com.ata.control.task.TaskControl;
import com.ata.provider.task.TaskInfo;

/**
 * Created by raven on 2015/11/8.
 */ /*
本地任务已经接受，开始运行
 */
public class TaskRunningState extends TransferState{
    public TaskRunningState(StateMachine stateMachine,PlantFormControl logic, TaskControl manager) {
        super(stateMachine,logic, manager);
    }
    @Override
    public int getCurrentState() {
        return TransferState.TaskRunningState;
    }

    @Override
    public void InitState() {
        super.InitState();
        Log.d(PlantFormControl.TAG, "entering state:" + "Task Running");
        TaskInfo currentTask=mLogic.getCurrentTask();
        if (currentTask != null) {

            mManager.runTask(mLogic.getCurrentTask());

            /*
            解除被分发线程锁上的任务段
             */
          mManager.cancelDis(currentTask);
            if(mLogic.getCurrentTask().dividable()) {
                mLogic.waitingForRedivide();
            }
        }
    }




}
