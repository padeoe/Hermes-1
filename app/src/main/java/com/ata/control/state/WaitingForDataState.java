package com.ata.control.state;

import android.util.Log;

import com.ata.control.PlantFormControl;
import com.ata.control.task.TaskControl;
import com.ata.model.transfer.wifip2p.Wifip2pTransporter;
import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskPartition;
import com.ata.provider.task.TaskSet;

/**
 * Created by raven on 2015/11/8.
 */ /*
接受方等待传输状态
 */
public class WaitingForDataState extends TransferState {
    public WaitingForDataState(StateMachine stateMachine,PlantFormControl logic, TaskControl manager) {
        super(stateMachine,logic, manager);
    }
    @Override
    public int getCurrentState() {
        return TransferState.WaitingForDataState;
    }
    /*
    注意这里可能是一个新的对象.
     */
    @Override
    public void onRemoteTaskFound(TaskInfo task) {
        super.onRemoteTaskFound(task);
        Log.d(PlantFormControl.TAG, "receiving task details :" + task.getTaskName() + " " +
                task.getTaskPartition().toString());


        TaskInfo current=mLogic.getCurrentTask();

        if(current!=null&&current.equal(task)){

           TaskSet set=task.getTaskPartition();
            Log.d(PlantFormControl.TAG,"current set is"+current.getTaskPartition().toString());
            for(TaskPartition par:set.toList()){
                mManager.addPieces(current.getTaskName(),par);
            }
            Log.d(PlantFormControl.TAG,"updating task allocation:"+task.getTaskName()+" current set" +
                    "is"+current.getTaskPartition().toString() );

        }
    //    TaskSet set=current.getTaskPartition();
        //   set.cancelDis();
        else {

            mLogic.setCurrentTask(task);
            if (mManager.addable(task.getTaskName()))
                mManager.addTask(task);
        }
            mManager.RecordWorks(task.getTaskName(),task.getTaskPartition().toList());

        mLogic.setState(mStateMachine.getTaskRunningState());
    }
    @Override
    public void InitState() {
        super.InitState();
        Log.d(PlantFormControl.TAG, "entering state:" + "Waiting for data");
        mLogic.Receive(Wifip2pTransporter.taskPort, false, mLogic.TASKTIMEOUT,mStateMachine.getBossAddress());
    }


}
