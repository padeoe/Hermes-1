package com.ata.provider.algol;

import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskSet;

/**
 * Created by raven on 2015/7/11.
 */
//分发表，记录了设备和任务片之间的对应关系.
public interface DistributeTable {
     TaskSet getDeviceJob(String deviceAddress);
     boolean add(String deviceAddress,TaskSet p);
    boolean remove(String deviceAddress);
   boolean isEmpty();
    TaskInfo GetTask();
    void onDisSucceed(TaskSet set,String peerIP);
}

