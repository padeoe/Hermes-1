package com.ata.view;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ata.R;
import com.ata.control.PlantFormControl;
import com.ata.control.task.TaskControl;
import com.ata.model.task.Task;
import com.ata.model.task.TaskList;
import com.ata.model.task.TaskPiece;
import com.ata.provider.task.ExecuteModule;
import com.ata.provider.task.TaskInfo;
import com.ata.provider.task.TaskPartition;
import com.ata.provider.task.TaskSet;
import com.ata.provider.transfer.Device;
import com.ata.provider.view.DeviceActionListener;
import com.ata.provider.view.TaskActionListener;
import com.ata.provider.view.TransferActionListener;
import com.ata.util.FileOperation;
import com.ata.util.sysInfoService;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TransferActionListener,DeviceActionListener,TaskActionListener{
    public static final int RESULT_TASKINFO =10;
    public static final int REQUEST_TASKINFO =10;
    private PlantFormControl mControl;/*这是主要的控制器，负责控制网络传输模型*/
    private TaskControl mManager;/*这是本地任务的控制器*/


    //view control
    private  TaskFragment tf=null;/*这是任务列表的fragment*/
    private  DeviceFragment df=null;/*这是设备列表的fragement*/
    private Fragment cur=null;/*通过一个按钮动态切换当前显示的列表*/
    //for distribution

    public static final String TAG ="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mManager= TaskControl.getTaskManager(ExecuteModule.DexExecutor, this);
        mManager.LoadInfo();
        mControl =PlantFormControl.getTransportLogic(this, mManager);
        setDefaultFragment();
        init_bottomButton();
        Toast.makeText(this,"Please ensure WiFi open！",Toast.LENGTH_LONG).show();
        mControl.start();

    }
    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();


    }
    @Override
    protected  void onDestroy(){
        super.onDestroy();
        mControl.stop();
        mManager.SaveInfo();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void init_bottomButton(){
        Button tab_tl=(Button)findViewById(R.id.button_tlist);
        Button tab_dl =(Button)findViewById(R.id.button_dlist);
        tab_tl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTaskList();
            }
        });
        tab_dl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeviceList();
            }
        });
    }
    private void setDefaultFragment()
    {
        showTaskList();
        showDeviceList();
    }
    @Override
    public void showDetails(Device d) {
        Toast.makeText(this,"device:"+d.toString(),Toast.LENGTH_LONG).show();
    }

    @Override
    public void showDeviceList() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if(df==null)
         df =new DeviceFragment();
        cur=df;
        transaction.replace(R.id.id_content, df);
        transaction.commit();
    }

    @Override
    public void showTaskDetails(TaskInfo task) {
        Toast.makeText(this,"task:"+task.toString(),Toast.LENGTH_LONG).show();
    }

    @Override
    public void showTaskList() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if(tf==null)
            tf=new TaskFragment();
        cur=tf;
//        refresh();
        transaction.replace(R.id.id_content, tf);
        transaction.commit();
    }

    @Override
    public void refresh() {
        if(cur==tf) {
            List<TaskInfo> t = mManager.getTaskList();
            tf.onTasksAvailable(t);
        }else if(cur==df){

        }
    }

    @Override
    public void onTaskFinished(String taskName) {

        TaskInfo taskInfo =mManager.getTask(taskName);
        long time =System.currentTimeMillis()-mManager.getTaskStartTime(taskName);
        Toast.makeText(MainActivity.this, "Task " + taskName + "finished! in "+time+"ms", Toast.LENGTH_LONG).show();
              /*  try open result file*/
        if(taskInfo!=null&&mManager.isPublisher(taskInfo)) {
            TaskSet t = taskInfo.getTaskPartition();

            String resultdir = Environment.getExternalStorageDirectory() + "/ata/" + taskName + "/result";
            File f = new File(resultdir);
            TaskPartition p = t.toList().get(0);

            String name = p.getStart()+"_"+p.getEnd()+".txt";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                t.CopyResultTo(f);
                Log.d(TAG, "cp result to sd card root dir");
                Intent intent1 = FileOperation.openFile(resultdir + File.separator + name);
                startActivity(intent1);
            } else {
                Log.d(TAG, "no sd card available");
            }

        }
    }

    @Override
    public void onDisSuccess(String task, String peer, TaskSet set) {
        Toast.makeText(this,"distribute "+task+" to "+peer+":"+set.toString(),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRemoteResultFound(String task, TaskSet result) {
        Toast.makeText(this,task+" receive result :"+result.toString(),Toast.LENGTH_LONG).show();
    }


    @Override
    public void start_distribution(Intent intent) {
        String exePath=intent.getStringExtra(GetTaskActivity.EXEPATH);
        String argPath=intent.getStringExtra(GetTaskActivity.ARGPATH);
        String taskName=intent.getStringExtra(GetTaskActivity.TASKNAME);
        String className =intent.getStringExtra(GetTaskActivity.CLASSNAME);

        int start =intent.getIntExtra(GetTaskActivity.START, 0);
        int end =intent.getIntExtra(GetTaskActivity.END, 9);
        int totalLength=end-start+1;

        File exe=new File(exePath);
        if(!exe.exists()){
            //报错
            Toast.makeText(this, "executable file not exists!", Toast.LENGTH_SHORT).show();
            return;

        }File arg=null;
        if(argPath!=null) {
            arg = new File(argPath);
            if (!arg.exists()) {
                Toast.makeText(this, "argument file not exists!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String name = sysInfoService.getDeviceUniqueName();
        int MinPieceSize=50;
        TaskInfo mTask=new Task(exe,arg,className,taskName,name,MinPieceSize);
        TaskPiece p=new TaskPiece(start,end,taskName);
        TaskSet list =new TaskList(taskName);
        list.add(p);
        mTask.setTaskPartition(list);
        mControl.onLocalTaskFound(mTask);
    }

    @Override
    public void start_contribution() {
        mControl.StartContributing();
    }

    @Override
    public void onPeersAvailable(List<Device> devices) {
        df.onPeersAvailable(devices);
    }

    @Override
    public void onTaskAvailable(List<TaskInfo> taskInfos) {
        tf.onTasksAvailable(taskInfos);
    }

    @Override
    public void discover() {
        mControl.discover();
    }
}
