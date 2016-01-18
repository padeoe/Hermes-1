package com.ata.model.task;

import android.util.Log;

import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskPartition;
import com.ata.provider.task.TaskSet;
import com.ata.util.FileOperation;
import com.ata.util.sysInfoService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by raven on 2015/5/28.
 */
public class Task implements TaskInfo {
    private static  final String TAG="taskallocation";

    private byte[] exeFile=null;
    private byte[] arguFile=null;
    private TaskSet set =null;
    private String MainClassName;
    private String BossName;
    private String exeFilePath;
    private  String arguFilePath;
    private volatile boolean Finished;
    private String TaskName;
    private int minPieceSize;

    public Task(File exe,File Argu,String ClassName,String taskName,String publisher,int MinPieceSize){
        exeFile= FileOperation.fileToByte(exe);

        exeFilePath= getStorePath()+File.separator+"ata"+File.separator+taskName+File.separator+"exeFile.apk";
        arguFile=FileOperation.fileToByte(Argu);
        arguFilePath=getStorePath()+File.separator+"ata"+File.separator+taskName+File.separator+"ArguFile.txt";
        set =new TaskList(taskName);
        RecoverFromTransfer();
        MainClassName=ClassName;
        Finished=false;
        TaskName=taskName;
        BossName=publisher;
        minPieceSize=MinPieceSize;
    }
    public Task(Task task,boolean removeFile,boolean removeRel){

        if(!removeFile){
            exeFile=task.exeFile.clone();
            arguFile=task.arguFile.clone();
        }
        else {
            exeFile=null;
            arguFile=null;
        }
        if(removeRel){
            set=null;
        }else {
            set =new TaskList(getTaskName());
            for(TaskPartition par:task.set.toList()) {
                if(par.isFinished()){
                    set.add(par);
                }
            }
        }
        exeFilePath=task.exeFilePath;

        arguFilePath=task.arguFilePath;
        minPieceSize=task.minPieceSize;
        Finished=task.Finished;
        TaskName=task.TaskName;
        BossName=task.BossName;
        MainClassName=task.getMainClassName();
    }
    private  String getStorePath(){
        return sysInfoService.getDataDir();
    }

    @Override
    public String getExeFilePath() {
        return exeFilePath;
    }

    @Override
    public String getArguFilePath() {
        return arguFilePath;
    }

    @Override
    public String getMainClassName() {
        return MainClassName;
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


    private void CheckFinished() {
        Finished=true;
        List<TaskPartition> partitions =set.toList();
        for(TaskPartition t:partitions){
            if (!t.isFinished()){
                Finished=false;
                break;
            }
        }
    }

    @Override
    public boolean isFinished() {
        CheckFinished();
        return Finished;
    }

    @Override
    public String getTaskPublisher() {
        return BossName;
    }


    @Override
    public String toString(){
            return TaskName +" class name:"+MainClassName+" bossname:"+BossName+
                    set.toString();
    }
    @Override
    public String getTaskName() {
        return  TaskName;
    }

    @Override
    public TaskInfo clone(boolean removeRel, boolean removeFile) {
        Task task=new Task(this,removeFile,removeRel);
       return task;
    }

    @Override
    public boolean equal(TaskInfo taskInfo) {
        return TaskName.equals(taskInfo.getTaskName());
    }


    @Override
    public void RecoverFromTransfer() {
        OutputStream os = null;
        BufferedOutputStream bs = null;
        List<TaskPartition> partitions =set.toList();
        if(partitions!=null){
            for(TaskPartition p:partitions){
                p.RecoverFromTransfer();
            }
        }


        if(arguFile!=null) {

            try {
                File file = new File(arguFilePath);
                File dir = file.getParentFile();

                if (!dir.exists() ) {
                    if( dir.mkdirs())
                    Log.d(TAG, "dir made:"+dir.getAbsolutePath());
                    else
                        Log.d(TAG,"dir made fail!");
                }

                if(!file.exists()){
                    Log.d(TAG,"creating file:"+file.getAbsolutePath());
                    file.createNewFile();
                }
                os = new FileOutputStream(file);
                bs = new BufferedOutputStream(os);
                bs.write(arguFile);
                bs.flush();
                Log.d(TAG, "argument file restored size:"+arguFile.length);

            } catch (FileNotFoundException e) {
                Log.d(TAG,"argument file not found!");

                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG,Log.getStackTraceString(e));

                e.printStackTrace();
            } finally {

                if (bs != null)
                    try {
                        bs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } if (os != null)
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        if(exeFile!=null) {

            try {
                File file = new File(exeFilePath);
                File dir = file.getParentFile();


                if (!dir.exists()) {
                    dir.mkdirs();
                }
                if(!file.exists()){
                    Log.d(TAG, "creating file:" + file.getAbsolutePath());
                    file.createNewFile();
                }
                os = new FileOutputStream(file);
                bs = new BufferedOutputStream(os);
                bs.write(exeFile);
                bs.flush();
                Log.d(TAG, "exefile restored size:"+exeFile.length);

            } catch (FileNotFoundException e) {
                Log.d(TAG," executable file not found!");

                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG," io error!");

                e.printStackTrace();
            } finally {
                if (bs != null)
                try {
                    bs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (os != null)
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }
        }
    }

    @Override
    public boolean dividable() {
        boolean dividable =set.getUnfinishedLength()>minPieceSize;
        return dividable;
    }

    @Override
    public boolean CancelDis() {
        return set.cancelDis();
    }


}
