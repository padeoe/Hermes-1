package com.ata.provider.algol;

import com.ata.provider.transfer.Device;
import com.ata.provider.task.TaskInfo;

import java.util.List;

/**
 * Created by raven on 2015/5/28.
 */
//进一步工作：实现算法！
public interface DivideStrategy {
     DistributeTable GetNewDivision();
   void updateData(List<Device> d);
     void LoadInfo();
    void SaveInfo();
     void SetTask(TaskInfo taskInfo);
}
