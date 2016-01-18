package com.ata.model.algol;

import android.util.Log;

import com.ata.provider.algol.DistributeTable;
import com.ata.provider.algol.DivideStrategy;
import com.ata.provider.algol.OnDisSuccListener;
import com.ata.provider.transfer.Device;
import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskSet;

import java.util.List;

/**
 * Created by raven on 2015/7/13.
 */
public class SimpleStrategy implements DivideStrategy {
    private List<Device> deviceList=null;
    private DistributeTable mTable=null;
    private TaskInfo task;
    private String TAG ="taskallocation";
    private OnDisSuccListener mlistner;

    public SimpleStrategy(OnDisSuccListener listener){
        mlistner=listener;
    }
    @Override
    public DistributeTable GetNewDivision() {
        mTable=new DistributeHashTable(task,deviceList,mlistner);
        if(deviceList==null)
              return null;

        else {
            if(task.dividable()) {
                TaskSet set = task.getTaskPartition();
                int n = deviceList.size();
                float[] percentages = new float[n];
                for (int i = 0; i < n; i++) {
                    percentages[i] = 1.0f / n;
                }
                Log.d(TAG, "current set :" + set.toString());
                TaskSet[] sets = set.RemoveAndDivide(percentages);
                if (sets == null) return null;
                for (int i = 0; i < n; i++) {
                    Device d = deviceList.get(i);
                    mTable.add(d.getDeviceInfo().getIPAddress(), sets[i]);
                }
                return mTable;
            }else {
                Log.d(TAG,"task is undividable");
                return null;
            }
       }
    }

    @Override
    public void updateData(List<Device> d) {
        deviceList=d;
    }

    @Override
    public void LoadInfo() {

    }

    @Override
    public void SaveInfo() {

    }

    @Override
    public void SetTask(TaskInfo taskInfo) {
        task=taskInfo;
    }


}
