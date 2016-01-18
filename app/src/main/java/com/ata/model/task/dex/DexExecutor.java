package com.ata.model.task.dex;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ata.model.task.TaskPiece;
import com.ata.provider.task.TaskSet;
import com.ata.provider.task.ExecuteModule;
import com.ata.control.task.TaskControl;
import com.ata.provider.task.ResultListener;
import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskPartition;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
/**
 * Created by raven on 2015/5/21.
 */
public class DexExecutor extends ExecuteModule {
    private Context mContext;
    private final File outputDir;
    private ResultListener mListener;

    class SimpleHandler extends Handler {
        ResultListener mListener;

        public SimpleHandler(ResultListener resultListener) {
            mListener = resultListener;
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String taskName = bundle.getString("TaskName");
            TaskPartition p = (TaskPartition) bundle.getSerializable("TaskPartition");
            mListener.onPartOfResultFinished(taskName, p);
        }
        public void sendResult(String taskName, TaskPartition p) {
            Message msg = obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString("TaskName", taskName);
            bundle.putSerializable("TaskPartition", p);
            msg.setData(bundle);

            sendMessage(msg);
        }

    }

    class Executable implements Runnable{
        private TaskInfo task;
        private SimpleHandler mHandler;
        public Executable(TaskInfo taskInfo,ResultListener listener){
            task=taskInfo;
            mHandler =new SimpleHandler(listener);
        }
        @Override
        public void run() {
            int start = 0;
            int end = 0;
            String className;
            File exe, arg, resul;
            Object lib;
            //gain the task partition
            exe = new File(task.getExeFilePath());
            arg = new File(task.getArguFilePath());
            className = task.getMainClassName();
            DexClassLoader cl = new DexClassLoader(exe.getAbsolutePath(),
                    outputDir.getAbsolutePath(), null, mContext.getClassLoader());
            Class libProviderClazz = null;


            try {

                libProviderClazz = cl.loadClass(className);

                lib = libProviderClazz.newInstance();
                Method[] methods = lib.getClass().getDeclaredMethods();

                Method run = null;
                for (Method m : methods) {
                    if (m.getName().equals("run")) {
                        run = m;
                    }
                }
                run.setAccessible(true);
                TaskSet set = task.getTaskPartition();
                set.cutToMinPieces(100);
                TaskPartition p ;
                while ((p = set.getAnUnFinishedPiece()) != null)
                    if (p.getStatus() == TaskPiece.PREDIS||p.getStatus()==TaskPiece.INDIS) {
                        Log.d(TaskControl.TAG, "found a piece occupied by dis thread");
                        return;
                    } else {
                        resul = new File(p.getResultFilePath());
                        start = p.getStart();
                        end = p.getEnd();
                        Log.d(TaskControl.TAG, "start running  tasks:" + p.toString());
                        run.invoke(lib, arg, start, end, resul);
                        Log.d(TaskControl.TAG, "task partition:" + p + " finished");
                        //lib.run(arg, start, end, resul);
                        p.setStatus(TaskPiece.FINISHED);

                        mHandler.sendResult(task.getTaskName(), p);
                    }
                Log.d(TaskControl.TAG,"all piece finished:"+task.getTaskPartition().toString());



            } catch(ClassNotFoundException e){
               Log.d(TaskControl.TAG,"class not found");
                e.printStackTrace();


            } catch (InstantiationException e) {
                Log.d(TaskControl.TAG, "can not new instance");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Log.d(TaskControl.TAG, "illegal access");
                e.printStackTrace();
            }catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }





    public DexExecutor(Context context,ResultListener listener){
        mContext=context;
        mListener=listener;
        outputDir=mContext.getDir("dex",0);
    }
    @Override
    public void RunTask( TaskInfo task){
        Thread r=new Thread(new Executable(task,mListener));
        r.start();
    }

    @Override
    public void RunTask(TaskInfo task, TaskPartition p) {

    }



}
