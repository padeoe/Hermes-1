package com.ata.control.task;

import android.content.Context;
import android.util.Log;

import com.ata.model.task.Task;
import com.ata.model.task.TaskList;
import com.ata.model.task.TaskPiece;
import com.ata.model.task.test.VirtualPiece;
import com.ata.model.task.test.VirtualTask;
import com.ata.provider.task.ExecuteModule;
import com.ata.provider.task.TaskSet;
import com.ata.model.task.dex.DexExecutor;
import com.ata.provider.task.ResultListener;
import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskPartition;
import com.ata.provider.view.TaskActionListener;
import com.ata.provider.view.TransferActionListener;
import com.ata.util.sysInfoService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by raven on 2015/5/31.
 */
public class TaskControl {
    public static String executorType;
    public static final String TAG="taskallocation";
    public static String ReCordStoreDir;
    public static final int STATE_UNKNOWN=0;
    public static final int STATE_ADDED=1;
    public static final int STATE_RUNNING=2;
    public static final int STATE_FINISHED=3;

    private HashMap<String,TaskState> TaskStateList;
    private HashMap<String,TaskSet> TaskRecord;
    private TaskState unknown;
    private ExecuteModule mExecutor;
    private Context mContext;
    private ResultListener mListener;
    private static TaskControl t;
    public static TaskControl getTaskManager(String ExecutorType,Context ctx){
        if(t==null)
        {
            t = new TaskControl();
            t.Init(ExecutorType, ctx);

        }return  t;
    }
    private TaskControl(){
        TaskRecord =new HashMap<>();
        TaskStateList=new HashMap<>();
        unknown =new TaskState(STATE_UNKNOWN,null);
    }
    private void Init(String ExecutorType,Context context){
        executorType=ExecutorType;
        mContext=context;
        ReCordStoreDir=mContext.getFilesDir().getAbsolutePath()+"/ata/data";
        mListener=new ResultListener() {
            @Override
            public void onPartOfResultFinished(String task, TaskPartition p) {
                Log.d(TaskControl.TAG, "checking :" + p);


                if(CheckFinished(task)){
                 taskFinished(task);
                }
            }


        };
        if(ExecutorType.equals(ExecuteModule.DexExecutor)){
            mExecutor=new DexExecutor(mContext,mListener);
        }
    }
    public void addTask(TaskInfo task){

        Log.d(TAG, "Adding task " + task.getTaskName());
        TaskState t=getTaskState(task.getTaskName());
        if(t.status==STATE_UNKNOWN){
            Log.d(TAG, "as a new task ");
            TaskStateList.put(task.getTaskName(), new TaskState(STATE_ADDED, task));

        }else {
            TaskInfo cur =t.taskInfo;
            TaskSet set=task.getTaskPartition();
            cur.addTaskPartition(set);
            t.status=STATE_ADDED;
         //   Log.d(TAG, "can not add " + task.getTaskName());
           // return;
        }
        ((TransferActionListener)mContext).onTaskAvailable(getTaskList());

    }
    public void runTask(TaskInfo task){
        Log.d(TAG, "Running task " + task.getTaskName());
      TaskState t= getTaskState(task.getTaskName());
        if(!t.runnable()){
            Log.d(TAG, "can not run " + task.getTaskName());
            return;
        }

        else {
            t.status =STATE_RUNNING;
            mExecutor.RunTask(task);
        }
    }
    private void taskFinished(String taskName){
        Log.d(TAG, "In taskManager:task " + taskName + "finished!");
        TaskState s=getTaskState(taskName);
        if(s.status==STATE_UNKNOWN){
            Log.d(TAG, "error: task " + taskName + "not record reported finished ");
            return ;
        }else {
            TaskSet set=s.taskInfo.getTaskPartition();
            set.MergePieces();
            s.status=STATE_FINISHED;
        }

        ((TransferActionListener)mContext).onTaskFinished(taskName);

    }
    public synchronized boolean CheckFinished(String taskName){
        Log.d(TAG,"checking "+taskName);
        boolean Finished;
        TaskState ts= getTaskState(taskName);
        if(ts.status==STATE_UNKNOWN) return  false;
        if(ts.status!=STATE_FINISHED) {
            TaskInfo t=ts.taskInfo;
            if (!isPublisher(t)) {
                Log.d(TAG, "we are not publisher ");
                Finished = t.isFinished();

                return Finished;
            } else {
                Log.d(TAG,t.isFinished()+" "+allResultReceived(t));
                Finished = t.isFinished() && allResultReceived(t);

                return Finished;
            }
        }else
            return true;
    }
    public synchronized boolean isFinished(String taskName){
        TaskState ts=getTaskState(taskName);
        if(ts.status==STATE_UNKNOWN) return false;

        return  ts.taskInfo.isFinished();
    }
    public synchronized boolean addable(String taskName){

        TaskState ts =getTaskState(taskName);
        return ts.addable();

    }
    public synchronized boolean runnable(String taskName){

        TaskState ts =getTaskState(taskName);
        return ts.runnable();

    }
    /*
    注意这里的参数带来的任务名相同而对象异态
     */
    public void addPieces(String t, TaskPartition p){

        TaskInfo task =getTask(t);
        if(task==null) return;
        if(getTaskStatus(t)==STATE_FINISHED&&!p.isFinished()){
            Log.d(TAG,"task status set to state added due to new piece found");
            setTaskStatus(t,STATE_ADDED);
        }
        TaskSet partitionList=task.getTaskPartition();
        partitionList.add(p);
        CheckFinished(task.getTaskName());
    }
    public  long getTaskStartTime(String taskName){
        TaskState ts =TaskStateList.get(taskName);
        if(ts==null){
            Log.d(TAG,"error! not existed:"+taskName);
            return  System.currentTimeMillis();
        }else
            return ts.startTime;
    }
    public synchronized int getTaskStatus(String taskName){
            TaskState ts =TaskStateList.get(taskName);
           if(ts==null){
               Log.d(TAG,"error! not existed:"+taskName);
               return  STATE_UNKNOWN;
           }else
            return ts.status;
    }
    private TaskState getTaskState(String taskName){
        TaskState ts=TaskStateList.get(taskName);
        if(ts==null)return  unknown;
        else  return  ts;
    }
    private void setTaskStatus(String taskName,int status){

            TaskState ts =getTaskState(taskName);
            if(ts.status==STATE_UNKNOWN)return;
            ts.status=status;

    }
    public TaskInfo getTask(String taskName) {

        TaskState ts =getTaskState(taskName);
        if(ts.status==STATE_UNKNOWN)return null;
        return  ts.taskInfo;
    }
    /*
    这个函数用于检查是否有被分发线程占据的任务，若有则解锁之。
     */
    public void cancelDis(TaskInfo info){

        Log.d(TAG,info.getTaskPartition().toString());
        if(info.CancelDis()){
            if(getTaskStatus(info.getTaskName())==STATE_RUNNING){
                setTaskStatus(info.getTaskName(),STATE_ADDED);
                Log.d(TAG,"cancel dis");
                runTask(info);
            }
        }
    }

    public boolean isPublisher (TaskInfo task){
        return task.getTaskPublisher().equals(sysInfoService.getDeviceUniqueName());
    }
    public void LoadInfo(){
       /*
        FileInputStream fi=null,fi1=null;
        ObjectInputStream oi=null,oi1=null;
        File dir =new File(ReCordStoreDir);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File f =new File(ReCordStoreDir+File.separator+"TaskList.tasks");            File f2= new File(ReCordStoreDir+File.separator+"TaskList.record");
        File f1= new File(ReCordStoreDir+File.separator+"TaskList.record");
        if(f.exists()) {
            try {
                 fi = new FileInputStream(f);
                 fi1 =new FileInputStream(f1);
                try {

                     oi = new ObjectInputStream(fi);
                     oi1 =new ObjectInputStream(fi1);
                    HashMap ti = (HashMap) oi.readObject();
                    HashMap ri =(HashMap)oi1.readObject();

                    TaskStateList = ti;
                    TaskRecord =ri;
                    Log.d(TAG, "loading tasks and record");
                    for(TaskState ts:TaskStateList.values()){
                        if(ts.status==STATE_RUNNING){
                            ts.status=STATE_ADDED;
                            TaskInfo t =ts.taskInfo;
                            for(TaskPartition p:t.getTaskPartition().toList()){
                                if(p.getStatus()== TaskPiece.RUNNING){
                                    p.setStatus(TaskPiece.ADDED);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.d(TAG, e.toString());
                } catch (ClassNotFoundException e) {
                    Log.d(TAG, e.toString());
                }
            } catch (FileNotFoundException e) {
                Log.d(TAG, e.toString());
            }finally {
                if(oi!=null)
                    try {
                        oi.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if(oi1!=null){
                    try {
                        oi1.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(fi!=null){
                    try {
                        fi.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(fi1!=null){
                    try {
                        fi1.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        }
        */
    }
    public void SaveInfo(){
        FileOutputStream fo=null,fo2=null;
        ObjectOutputStream oo=null,oo2=null;
        File dir =new File(ReCordStoreDir);
        if(!dir.exists()){
            dir.mkdirs();
        }

            File f=new File(ReCordStoreDir+File.separator+"TaskList.tasks");
            File f2= new File(ReCordStoreDir+File.separator+"TaskList.record");
            try {
                 fo=new FileOutputStream(f);
                 fo2 =new FileOutputStream(f2);
                try {
                     oo=new ObjectOutputStream(fo);
                     oo2=new ObjectOutputStream(fo2);
                    oo.writeObject(TaskStateList);
                    oo2.writeObject(TaskRecord);
                    Log.d(TAG, "saving taskstatelist  to:" + f.getAbsolutePath());
                    Log.d(TAG,"saving record to :"+f2.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (FileNotFoundException e) {
                Log.d(TAG, e.toString());
            }finally {
                if(oo!=null)
                    try {
                        oo.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                if(oo2!=null){
                    try {
                        oo2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(fo!=null){
                    try {
                        fo.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(fo2!=null){
                    try {
                        fo2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    }
    public void RecordWorks(String taskName,List<TaskPartition >p) {

        TaskSet rec =getRecord(taskName);


        if (rec == null) {
            rec = new TaskList(taskName);
            TaskRecord.put(taskName,rec);
        } else {
            Log.d(TAG, "resetting work record:"+taskName);

        }
        for (TaskPartition par : p) {
            VirtualPiece vp = new VirtualPiece(par.getStart(), par.getEnd(), false);
            rec.add(vp);
            Log.d(TAG, "total work record:" + par);

        }

    }
    public TaskSet getRecord(String task){
        TaskSet Record =TaskRecord.get(task);

        return Record;
    }
    public boolean allResultReceived(TaskInfo t){
        TaskState ts =getTaskState(t.getTaskName());
        if(ts.status==STATE_UNKNOWN){
            return  false;
        }

        if(ts.disedWorks==null) {
            Log.d(TAG,"null diswork!");
            return  true;
        }

        else {
            TaskSet s=getRecord(t.getTaskName());
            Log.d(TAG,"record is"+s.toString());
            assert  s!=null;
           TaskSet set =t.getTaskPartition();
            return set.covered(s.toList(),false);
        }
    }
    public static int getLength(List<TaskPartition> p){
        int length=0;
        for(TaskPartition par:p){
            length+=par.getEnd()-par.getStart()+1;
        }
        return length;
    }
    /*
        任务分发记录
     */
    public void RecordDisWorks(String taskName,TaskSet set,String peerName){

            Log.d(TAG,"recording distributed works for task "+taskName+" to "+peerName);

            TaskState ts =TaskStateList.get(taskName);
            if(ts==null) {
                Log.d(TAG,"error! task:"+taskName);
                return  ;

            }
            if(ts.disedWorks==null) {
                ts.disedWorks = new HashMap<>();
            }
            ts.disedWorks.put(set,peerName);

    }
    public List<TaskSet> getDisWorks(String taskName){


            TaskState ts =TaskStateList.get(taskName);
            if(ts==null) return  null;

            return new ArrayList<>(ts.disedWorks.keySet());

    }
    public String getAccordPeerName(String taskName,TaskSet set){

            TaskState ts = TaskStateList.get(taskName);
            if (ts == null) return null;
            return ts.disedWorks.get(set);
    }

    public synchronized List<TaskInfo> getTaskList(){
        ArrayList<TaskInfo> t=new ArrayList<>();
        for(TaskState ts:TaskStateList.values()){
            t.add(ts.taskInfo);
        }
        return  t;
    }

}
 class TaskState implements Serializable{
    public int status;
    public long startTime;
    public TaskInfo taskInfo;
    public HashMap<TaskSet,String> disedWorks;/*记录历史分发信息*/
    public TaskState(int s, TaskInfo t){
        status =s;
        taskInfo=t;
        startTime =System.currentTimeMillis();
        disedWorks=null;
    }

    public synchronized boolean addable(){
        return (status==TaskControl.STATE_UNKNOWN||status==TaskControl.STATE_ADDED||status==TaskControl.STATE_FINISHED);
    }
    public synchronized boolean runnable(){
        return (status==TaskControl.STATE_ADDED);
    }
}