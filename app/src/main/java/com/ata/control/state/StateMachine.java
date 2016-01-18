package com.ata.control.state;

import com.ata.control.PlantFormControl;
import com.ata.control.task.TaskControl;

/**
 * Created by raven on 2015/11/8.
 */
public class StateMachine {

    private InitState initState;
    private CollectingPeerInfoState collectState;
    private SendingDataState sendDataState;
    private StartContributeState startContributeState;
    private TaskRunningState taskRunningState;
    private WaitingForDataState waitForDataState;
    private WaitingForResultState waitForResultState;
    private TransferState currentState=null;
    private TransferState nextState=null;

    public synchronized boolean getMachineRunningTag() {
        return MachineRunningTag;
    }

    public synchronized void setMachineRunningTag(Boolean machineRunningTag) {
        MachineRunningTag = machineRunningTag;
    }

    private boolean MachineRunningTag=false;

    private String bossAddress;
    public void setNextState(TransferState state){
        nextState=state;
        runMachine();
    }
    private  void  runMachine(){
        if(getMachineRunningTag()!=true) {
           setMachineRunningTag(true);
            while (currentState != nextState) {
                currentState=nextState;
                currentState.InitState();
            }
            setMachineRunningTag(false);
        }
    }
    public String getBossAddress() {
        return bossAddress;
    }

    public void setBossAddress(String bossAddress) {
        this.bossAddress = bossAddress;
    }

    public String getCurrentStateName(){
        TransferState currentState =getCurrentState();

        switch (currentState.getCurrentState()){
            case TransferState.InitState:return "InitState";
            case TransferState.CollectingPeerInfoState:return "CollectingState";
            case  TransferState.SendingDataState:return "SendingDataState";
            case TransferState.TaskRunningState:return "TaskRunningState";
            case TransferState.StartContributeState:return "StartContribute";
            case TransferState.WaitingForDataState:return "WaitingForDataState";
            case TransferState.WaitingForResultState:return "WaitingForResultState";
            default:return "UnknownState";
        }
    }

    public StateMachine(PlantFormControl control,TaskControl manager){
        initState=new InitState(this,control,manager);
        collectState=new CollectingPeerInfoState(this,control,manager);
        sendDataState=new SendingDataState(this,control,manager);
        taskRunningState =new TaskRunningState(this,control,manager);
        startContributeState=new StartContributeState(this,control,manager);
        waitForDataState=new WaitingForDataState(this,control,manager);
        waitForResultState=new WaitingForResultState(this,control,manager);
        currentState=nextState=initState;
    }
    public TransferState getCurrentState(){
        return  currentState;
    }
    public InitState getInitState() {
        return initState;
    }
    public CollectingPeerInfoState getCollectState() {
        return collectState;
    }
    public SendingDataState getSendDataState() {
        return sendDataState;
    }
    public TaskRunningState getTaskRunningState() {
        return taskRunningState;
    }
    public StartContributeState getStartContributeState() {
        return startContributeState;
    }
    public WaitingForDataState getWaitForDataState() {
        return waitForDataState;
    }
    public WaitingForResultState getWaitForResultState() {
        return waitForResultState;
    }

}
