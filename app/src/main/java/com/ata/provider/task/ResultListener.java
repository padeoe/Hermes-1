package com.ata.provider.task;

/**
 * Created by raven on 2015/5/21.
 */
public interface ResultListener {
     void onPartOfResultFinished(String task, TaskPartition p);
}
