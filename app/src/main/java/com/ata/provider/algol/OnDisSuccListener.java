package com.ata.provider.algol;

import com.ata.provider.task.TaskSet;

/**
 * Created by raven on 2015/10/13.
 */
public interface OnDisSuccListener {
    void onDisSucceed(String taskName,TaskSet set,String peerName);

}
