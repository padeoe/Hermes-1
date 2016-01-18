package com.ata.provider.task;

import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskPartition;

/**
 * Created by raven on 2015/5/21.
 */
public abstract class ExecuteModule {
    public static final  String DexExecutor ="DexExecutor";
    public static final String VirtualExecutor="VirtualExecutor";
    public  abstract void RunTask(TaskInfo task);
    public abstract void RunTask(TaskInfo task,TaskPartition p);
   // public abstract void InitListener(ResultListener listener);
}
