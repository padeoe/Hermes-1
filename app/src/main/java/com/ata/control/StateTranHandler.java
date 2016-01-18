package com.ata.control;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ata.control.state.StateMachine;
import com.ata.control.state.TransferState;
import com.ata.control.task.TaskControl;
import com.ata.model.task.Task;
import com.ata.provider.task.TaskInfo;

import java.util.Timer;

import java.util.TimerTask;
import java.util.logging.LogRecord;

/**
 * Created by raven on 2015/11/8.
 */
public class StateTranHandler extends Handler {
    private StateMachine mStateMachine;
    private Timer timer =null;
    private String TAG="stateTranCheck";
    private  PlantFormControl mControl;
    private TaskControl mManager;
    public StateTranHandler(StateMachine stateMachine,PlantFormControl plantFormControl,TaskControl Manager){
        timer =new Timer();
        mStateMachine=stateMachine;
        mControl=plantFormControl;
        mManager =Manager;
    }
    public void waitForRedivide(){
        if(timer==null)timer=new Timer(true);
        TimerTask task =new TimerTask() {
            @Override
            public void run() {
                Message message =obtainMessage();
                message.arg1=1;
                sendMessage(message);
            }
        };
        timer.schedule(task,mControl.CHECKINTERVALS,mControl.CHECKINTERVALS);
    }
    public void handleMessage(Message msg) {
        TransferState currentState = mStateMachine.getCurrentState();
        TaskInfo currentTask =mControl.getCurrentTask();
        if(msg.arg1==1){
            Log.d(TAG, "checking whether to reallocate");
            if(currentState!= mStateMachine.getTaskRunningState()||currentTask==null||mManager.isFinished(currentTask.getTaskName())){
                timer.cancel();
                timer=null;
                Log.d(TAG, "timer canceled due to task finished");
                return;
            }
            if(currentState== mStateMachine.getTaskRunningState()&&currentTask.dividable()) {
                timer.cancel();
                timer=null;
                Log.d(TAG, "timer canceled");
                mControl.setState(mStateMachine.getCollectState());

            }
        }
        super.handleMessage(msg);
    };


}
