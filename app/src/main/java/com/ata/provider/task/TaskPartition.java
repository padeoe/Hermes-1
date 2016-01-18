package com.ata.provider.task;

import java.io.Serializable;

/**
 * Created by raven on 2015/5/21.
 */
public interface TaskPartition extends Serializable {
    int getStatus();
    void setStatus(int s);
   int getStart();
     int getEnd();
    boolean isFinished();
    void  setFinished();

     String getResultFilePath();
     void RecoverFromTransfer() ;
    void BeforeTransfer();

    void reSet(int start,int end);
    void Merge(TaskPartition another);

}
