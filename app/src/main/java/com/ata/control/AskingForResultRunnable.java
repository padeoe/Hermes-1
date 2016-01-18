package com.ata.control;

import android.util.Log;

import com.ata.control.task.TaskControl;
import com.ata.model.transfer.wifip2p.Wifip2pTransporter;

/**
 * Created by raven on 2015/11/8.
 */
public class AskingForResultRunnable implements Runnable {
        private PlantFormControl mLogic;
        private TaskControl mManager;
        private String taskName;
        private static boolean RunningFlag;

        public AskingForResultRunnable(PlantFormControl logic, TaskControl manager, String task) {
            mLogic = logic;
            mManager = manager;
            taskName = task;

        }

        @Override
        public void run() {
            if (!RunningFlag) {
                RunningFlag=true;
                mLogic.Receive(Wifip2pTransporter.ResultPort, true, PlantFormControl.RESULTTIMEOUT, null);

                while (true) {
                    if (mManager.getTaskStatus(taskName) == mManager.STATE_FINISHED
                            || Thread.currentThread().isInterrupted()) {
                        Log.d(mLogic.TAG, "task Finished! or thread canceled");
                        RunningFlag=false;
                        break;
                    } else if (mManager.allResultReceived(mManager.getTask(taskName))) {
                        Log.d(mLogic.TAG, "stoping asking  for result due all task distributed finished ");
                        RunningFlag=false;
                        break;
                    }
                    if (mLogic.JoinInGroup())
                        mLogic.AskingForResult(mLogic.getCurrentTask().getTaskName());
                    else {
                        Log.d(mLogic.TAG, "stop asking for result due to no peers nearby");
                    }
                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            } else {
                Log.d(mLogic.TAG, "already Running!");
            }
        }
    }

