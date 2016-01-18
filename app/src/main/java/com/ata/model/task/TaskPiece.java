package com.ata.model.task;

import android.util.Log;

import com.ata.provider.task.TaskPartition;
import com.ata.control.task.TaskControl;
import com.ata.util.FileOperation;
import com.ata.util.sysInfoService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Created by raven on 2015/5/31.
 */
public class TaskPiece implements TaskPartition {
    public static final int ADDED =1;
    public static final int PREDIS =2;
    public static final int INDIS =3;
    public static final int RUNNING=4;
    public static final int FINISHED=5;
    private int start;
    private int end;
    private byte[] ResultFile;
    private String resultPath;
    private String TaskName;
    private int status;

    public TaskPiece(int s,int e,String taskName){
        assert s>=0&&e>=s;
        start=s;
        end=e;
        ResultFile=null;
        TaskName=taskName;
        status=ADDED;
        resultPath=buildPath(taskName,s,e);
        File result =new File(resultPath);
        File dir =result.getParentFile();
        if(!dir.exists())
            dir.mkdirs();
    }
    /*
    此处需要修改，getDataDir返回正确的值依赖于初始化顺序
     */
    public static String buildPath(String TaskName,int start,int end){
        return sysInfoService.getDataDir()+"/ata/"+TaskName+"/result/"+start+"_"+end+".txt";
    }

    @Override
    public synchronized int getStatus() {
        return status;
    }

    @Override
    public synchronized void setStatus(int s) {
        status=s;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public synchronized boolean isFinished() {
        return status==FINISHED;
    }

    @Override
    public synchronized void setFinished() {
        status=FINISHED;
    }


    @Override
    public String getResultFilePath() {
        return resultPath;
    }

    @Override
    public void RecoverFromTransfer()  {
        OutputStream os = null;
        BufferedOutputStream bs=null;
        Log.d(TaskControl.TAG, "restoring task pieces"+this.toString());
        if(ResultFile!=null) {
            Log.d(TaskControl.TAG,"length:"+ResultFile.length);

            try {
                File file = new File(resultPath);
                File dir =file.getParentFile();
                if (!dir.exists() ) {
                    dir.mkdirs();
                }
                if(!file.exists()){
                    file.createNewFile();
                }
                os = new FileOutputStream(file);
                bs = new BufferedOutputStream(os);
                bs.write(ResultFile);
                bs.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (os != null)
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if (bs != null)
                    try {
                        bs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }else {
            File result =new File(resultPath);
            File dir =result.getParentFile();
            if(!dir.exists())
                dir.mkdirs();
            Log.d(TaskControl.TAG,"result file is null!");
        }
    }

    @Override
    public void BeforeTransfer() {
        if(ResultFile==null){
            ResultFile= FileOperation.fileToByte(new File(resultPath));
        }
    }





    @Override
    public void reSet(int s, int e) {
        assert s>=0&&e>=s;
        if(isFinished()){

            reSetFile(s, e);

        }else {
            start =s;
            end=e;
            resultPath=buildPath(TaskName, s, e);
        }
    }

    @Override
    public void Merge(TaskPartition another) {
        assert end+1==another.getStart();
        assert isFinished()&&another.isFinished()||(!isFinished()&&!another.isFinished());
        if(!isFinished()){
            reSet(start,another.getEnd());

        }else {
            String newPath=buildPath(TaskName,start,another.getEnd());
            File newfile =new File(newPath);

            File parent =newfile.getParentFile();
            if(!parent.exists()){
                parent.mkdirs();
            }
            if(!newfile.exists()){
                try {
                    newfile.createNewFile();
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
            File f =new File(resultPath);
            File f2=new File(another.getResultFilePath());

            FileOperation.myMergeFiles(newfile,f,f2);
            if(f.exists())f.delete();
            if(f2.exists())f2.delete();
            resultPath =newPath;
            assert  another.getEnd()>=start;
            end =another.getEnd();

        }

    }

    private void reSetFile(int s,int e){
        int relativeStart =s-start;
        int relativeEnd =e-start;
        start =s;end =e;
        File f =new File(resultPath);
        if(!f.exists())return;
        else {
            f.renameTo(new File(buildPath(TaskName,s,e)));
            FileOperation.ResetFile(f,relativeStart,relativeEnd);
        }
    }

    private  String getStringStatus(){
        switch (status){
            case FINISHED:return "finished";
            case ADDED:return "added";
            case PREDIS:return "pre distribution";
            case INDIS :return "in distribution";
            case RUNNING:return "running";
            default:return "unknown";
        }
    }
    @Override
    public String toString(){
        return new String("("+start+","+end+")"+getStringStatus());
    }


}
