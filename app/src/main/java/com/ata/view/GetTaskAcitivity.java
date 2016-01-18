package com.ata.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ata.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 这个活动在试图发布一个任务时被创建，返回一个包含了创建任务所需信息的bundle
 */
public class GetTaskAcitivity extends Activity {
    public static  final String EXEPATH="exe";
    public static  final String ARGPATH="arg";
    public static final  String TASKNAME="name";
    public static final String CLASSNAME ="class";
    public static final String START ="start";
    public  static  final  String END ="end";
    private EditText exe=null;
    private EditText argu=null;
    private EditText EditTaskName =null;
    private EditText EditClassName =null;
    private EditText start=null;
    private EditText end =null;
    private  String TaskName="MinCutTask";
    private String className ="com.ata.min_cutalg.Karger_MinCutAlg";

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_task_acitivity);

        String path=Environment.getExternalStorageDirectory().getAbsolutePath();
        String exepth=path+"/app-release.apk";
        String argupth =path+"/graph.txt";
        exe=(EditText)findViewById(R.id.editText_exe);
        argu=(EditText)findViewById(R.id.editText_argu);
        exe.setText(exepth.toCharArray(), 0, exepth.length());
        argu.setText(argupth.toCharArray(),0,argupth.length());
        EditClassName =(EditText)findViewById(R.id.editText_class);
        EditTaskName =(EditText)findViewById(R.id.editText_taskName);
        EditClassName.setText(className.toCharArray(),0,className.length());
        EditTaskName.setText(TaskName.toCharArray(), 0, TaskName.length());

        start =(EditText)findViewById(R.id.editText_start);
        start.setText("0".toCharArray(),0,1);
        end=(EditText)findViewById(R.id.editText_end);
        end.setText("100".toCharArray(),0,1);
    //    Button getExe=(Button)findViewById(R.id.button);
      //  Button getArg=(Button)findViewById(R.id.button2);
        Button submit=(Button)findViewById(R.id.button_submit_task);
    /*
        getExe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        getArg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        */
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String exePath=exe.getText().toString();
                String arguPath=argu.getText().toString();
                TaskName= EditTaskName.getText().toString();
                className=EditClassName.getText().toString();

                int value_start =Integer.parseInt(start.getText().toString());

                int value_end =Integer.parseInt(end.getText().toString());

                Intent intent =new Intent();
                intent.putExtra(EXEPATH, exePath);
                intent.putExtra(ARGPATH, arguPath);
                intent.putExtra(TASKNAME, TaskName);
                intent.putExtra(CLASSNAME,className);
                intent.putExtra(START,value_start);
                intent.putExtra(END, value_end);
                GetTaskAcitivity.this.setResult(MainActivity.RESULT_TASKINFO, intent);
                GetTaskAcitivity.this.finish();
                return;
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_get_task_acitivity, menu);
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
}
