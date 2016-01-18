package com.ata.model.algol;

import com.ata.provider.algol.DistributeTable;
import com.ata.provider.algol.OnDisSuccListener;
import com.ata.provider.transfer.Device;
import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskSet;

import java.util.HashMap;
import java.util.List;

/**
 * Created by raven on 2015/7/13.
 */
public class DistributeHashTable implements DistributeTable {
    private HashMap<String,TaskSet > mMap;
    private TaskInfo task;
    private List<Device> devices;
    private OnDisSuccListener listener;
    DistributeHashTable(TaskInfo taskInfo ,List<Device> deviceList,OnDisSuccListener l){
        task=taskInfo;
        devices=deviceList;
        listener=l;
        mMap=new HashMap<>();
    }
    @Override
    public TaskSet getDeviceJob(String deviceAddress) {
        return mMap.get(deviceAddress);
    }

    @Override
    public boolean add(String deviceName, TaskSet p) {
       if(mMap.put(deviceName,p)==null){
           return true;
       }else {
           return false;
       }
    }

    @Override
    public boolean remove(String deviceAddress) {
        if(mMap.remove(deviceAddress)!=null){
            return true;
        }else
            return false;
    }

    @Override
    public boolean isEmpty() {
        return mMap.isEmpty();
    }

    @Override
    public TaskInfo GetTask() {
        return task;
    }

    /*
    transfer peer 's ip address to peers name
     */
    @Override
    public void onDisSucceed(TaskSet set, String peerIP) {
        for(Device d:devices){
            if(d.getDeviceInfo().getIPAddress().equals(peerIP)){
                listener.onDisSucceed(GetTask().getTaskName(),set,d.getDeviceName());
                return;
            }
        }
    }
}
