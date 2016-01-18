package com.ata.model.task.test;

import com.ata.model.task.TaskPiece;
import com.ata.provider.task.TaskPartition;

/**
 * Created by raven on 2015/8/29.
 */
public class  VirtualPiece implements TaskPartition {
    int start;
    int end;
    private int status;
    public VirtualPiece(int s,int e,boolean F){
        assert s>=0&&e>=s;
        start=s;
        end=e;
        if(!F)
        status= TaskPiece.ADDED;
        else {
            status=TaskPiece.FINISHED;
        }
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int s) {
        status=s;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public boolean isFinished() {
        return status==TaskPiece.FINISHED;
    }

    @Override
    public void setFinished() {
        status=TaskPiece.FINISHED;
    }

    @Override
    public String getResultFilePath() {
        return null;
    }

    @Override
    public void RecoverFromTransfer() {

    }

    @Override
    public void BeforeTransfer() {

    }



    @Override
    public void reSet(int s, int e) {
        start=s;
        end=e;
    }

    @Override
    public void Merge(TaskPartition another) {
        assert end+1==another.getStart();
        end =another.getEnd();
    }
    private  String getStringStatus(){
        switch (status){
            case TaskPiece.FINISHED:return "finished";
            case TaskPiece.ADDED:return "added";
            case TaskPiece.PREDIS:return "pre distribution";
            case TaskPiece.INDIS :return "in distribution";
            case TaskPiece.RUNNING:return "running";
            default:return "unknown";
        }
    }
    @Override
    public String toString(){
        return new String("("+start+","+end+")"+getStringStatus());
    }



}
