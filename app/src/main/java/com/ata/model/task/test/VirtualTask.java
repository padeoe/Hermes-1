package com.ata.model.task.test;

import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskSet;

/**
 * Created by raven on 2015/11/14.
 */
public class VirtualTask  implements TaskInfo{
    String name;
    String publisher;
    boolean finished;
    TaskSet set;
    public  VirtualTask(String n){
        name=n;
    }
    @Override
    public String getExeFilePath() {
        return null;
    }

    @Override
    public String getArguFilePath() {
        return null;
    }

    @Override
    public String getMainClassName() {
        return null;
    }

    @Override
    public void setTaskPartition(TaskSet p) {
        set=p;
    }

    @Override
    public void addTaskPartition(TaskSet p) {
        set.merge(p);
    }

    @Override
    public TaskSet getTaskPartition() {
        return set;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public String getTaskPublisher() {
        return publisher;
    }

    @Override
    public String getTaskName() {
        return name;
    }

    @Override
    public TaskInfo clone(boolean removeRel, boolean removeFile) {
        return null;
    }

    @Override
    public boolean equal(TaskInfo taskInfo) {
        return false;
    }

    @Override
    public void RecoverFromTransfer() {

    }

    @Override
    public boolean dividable() {
        return false;
    }

    @Override
    public boolean CancelDis() {
        return false;
    }
}
