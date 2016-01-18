package com.ata.provider.view;

import android.content.Intent;
import android.os.Bundle;

import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskSet;
import com.ata.provider.transfer.Device;

import java.util.List;

/**
 * Created by raven on 2015/11/8.
 * MainActivity继承此接口，被控制器，任务，设备列表（Fragment）调用
 */
public interface TransferActionListener {
    void  start_distribution(Intent intent);
    void  start_contribution();
    void  onPeersAvailable(List<Device>devices);
    void  onTaskAvailable(List<TaskInfo> taskInfos);
    void  discover();
    void  onTaskFinished(String task);
    void  onDisSuccess(String task,String peer,TaskSet set);
    void  onRemoteResultFound(String task ,TaskSet result);
}
