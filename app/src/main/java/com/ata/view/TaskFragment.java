package com.ata.view;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ata.R;
import com.ata.model.task.Task;
import com.ata.provider.task.TaskInfo;
import com.ata.provider.transfer.Device;
import com.ata.provider.view.DeviceActionListener;
import com.ata.provider.view.TaskActionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raven on 2015/11/8.
 * 这是一个简陋的任务列表，每一条列表内容只包含任务名，通过TaskActionListener提供的接口与控制器进行交互
 */

public class TaskFragment extends ListFragment {
    private View mContentView = null;
    private List<TaskInfo>taskList =new ArrayList<>();

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
       TaskInfo taskInfo =(TaskInfo)getListAdapter().getItem(position);
        ((TaskActionListener)getActivity()).showTaskDetails(taskInfo);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.task_list, null);
        ((TaskActionListener)getActivity()).refresh();
        return mContentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new TaskListAdapter(getActivity(), R.layout.row_task, taskList));

    }

    public void onTasksAvailable(List<TaskInfo>taskInfos){
        taskList.clear();
        if(taskInfos!=null)
            taskList.addAll(taskInfos);
        ((TaskListAdapter) getListAdapter()).notifyDataSetChanged();

    }
    private class TaskListAdapter extends ArrayAdapter<TaskInfo> {
        private List<TaskInfo> items;

        public TaskListAdapter(Context context, int resource, List<TaskInfo> objects) {
            super(context, resource, objects);
            items=objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_task, null);
            }
            TaskInfo task = items.get(position);
            if (task != null) {
                TextView TaskName = (TextView) v.findViewById(R.id.task_name);

                if (TaskName != null) {
                    TaskName.setText(task.getTaskName());
                }
                else {
                    Log.d(MainActivity.TAG," error task view");
                }
            }

            return v;

        }
    }

}
