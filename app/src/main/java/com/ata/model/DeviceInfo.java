package com.ata.model;

/**
 * Created by raven on 2015/5/28.
 */

import java.io.Serializable;

/**
 * @ClassName: DeviceInfo
 * @Description:进一步对device进行封装，这是device内的只读信息
 * @author jiangming
 * @date 2015-3-20 上午12:31:40
 *
 */
public class DeviceInfo implements Serializable{
    private int BatteryLevel;
    private long AvilMem;
    private String CurCpuFreq;
    private long ability;//考虑通过一个综合的分数衡量设备的性能
    private String IPAddress=null;
    public DeviceInfo() {
        BatteryLevel=0;
        AvilMem=0;
        CurCpuFreq=null;
        ability=0;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public  DeviceInfo(int BatteryLvl,long mem,String cpuFreq){
        BatteryLevel=BatteryLvl;
        AvilMem=mem;
        CurCpuFreq=cpuFreq;
        IPAddress=null;
        CalculateAbility();
    }
    public DeviceInfo(int BatteryLvl,long mem,String cpuFreq,String IP){
        BatteryLevel=BatteryLvl;
        AvilMem=mem;
        CurCpuFreq=cpuFreq;
        IPAddress=IP;
        CalculateAbility();
    }
    private void CalculateAbility(){
        ability=Integer.parseInt(getCurCpuFreq());
    }
    public long getAbility(){
        return  ability;
    }
    public int getBatteryLevel(){
        return BatteryLevel;
    }
    public long getAvilMem(){
        return AvilMem;
    }
    public String getCurCpuFreq(){
        return CurCpuFreq;
    }
}