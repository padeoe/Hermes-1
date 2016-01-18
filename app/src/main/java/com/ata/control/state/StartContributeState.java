package com.ata.control.state;

import android.util.Log;

import com.ata.control.PlantFormControl;
import com.ata.control.task.TaskControl;

/**
 * Created by raven on 2015/11/8.
 */ /*
准备接受任务状态
 */
public class StartContributeState extends TransferState {
    public StartContributeState(StateMachine stateMachine,PlantFormControl logic, TaskControl manager) {
        super(stateMachine,logic, manager);
        mLogic.setCurrentTask(null);
    }
    private String taskName=null;
    @Override
    public int getCurrentState() {
        return TransferState.StartContributeState;
    }
    @Override
    public void InitState() {
        super.InitState();
       mStateMachine.setBossAddress(null);
        Log.d(PlantFormControl.TAG, "entering state:" + "Contributing");

    }

    @Override
    public void onReceiveTransferACK(String task) {
        super.onReceiveTransferACK(task);
       // mLogic.stopListen();
        Log.d(PlantFormControl.TAG, "prepare for task data transfer:" + task);
        if(mLogic.getCurrentTask()==null||mLogic.getCurrentTask().getTaskName().equals(task)) {
            if (taskName.equals(task)) {
                taskName = null;
                mLogic.setState(mStateMachine.getWaitForDataState());
            }
        }else {
            String name=mLogic.getCurrentTask().getTaskName();
            if(mManager.isFinished(name)){
                Log.d(PlantFormControl.TAG,"current task :"+name+"finished resetting to new task:"+task);
                mLogic.setState(mStateMachine.getWaitForDataState());

            }else {
                Log.d(PlantFormControl.TAG,"can not get new task due to old task unfinished");
            }
        }
    }

    @Override
    public void onReceiveREQUEST(String task, String address) {
        super.onReceiveREQUEST(task, address);
        Log.d(PlantFormControl.TAG, "receiving task " + task + " request from" + address);


        if(mManager.addable(task)||mManager.isFinished(task))
        {    Log.d(PlantFormControl.TAG,"preparing to get task:"+task+"from :"+address);
            mStateMachine.setBossAddress(address);
             mLogic.sendACK(task, address);
            taskName=task;
        }else{
            Log.d(PlantFormControl.TAG,"task is unaddable");
        }
    }


}
