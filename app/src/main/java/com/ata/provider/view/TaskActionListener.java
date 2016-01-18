package com.ata.provider.view;

import com.ata.provider.task.TaskInfo;

/**
 * Created by raven on 2015/11/8.
 */
public interface TaskActionListener {
    void  showTaskDetails(TaskInfo task);
    void  showTaskList();
    void  refresh();
}
