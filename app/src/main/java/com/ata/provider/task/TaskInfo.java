package com.ata.provider.task;

import java.io.Serializable;

/**
 * Created by raven on 2015/5/24.
 */
public interface TaskInfo  extends Serializable{
 String getExeFilePath();
 String getArguFilePath();
 String  getMainClassName();
 void setTaskPartition(TaskSet p);
 void addTaskPartition(TaskSet p);
 TaskSet getTaskPartition();
 boolean isFinished();
 String getTaskPublisher();
 String getTaskName();
 TaskInfo clone(boolean removeRel,boolean removeFile);
 boolean equal(TaskInfo taskInfo);
 void RecoverFromTransfer();
 boolean dividable();
 boolean  CancelDis();
}
