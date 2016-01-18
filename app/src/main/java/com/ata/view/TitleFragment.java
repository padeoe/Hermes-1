package com.ata.view;

/**
 * Created by raven on 2015/11/4.
 */
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ata.R;
import com.ata.provider.view.TaskActionListener;
import com.ata.provider.view.TransferActionListener;
/*这是顶部的frament，包含了4个按钮，包括搜索附近设备，发布任务，接受任务，刷新列表,通过TransferActionListener提供的接口与控制器交互*/
/*from:http://blog.csdn.net/lmj623565791/article/details/37970961*/
public class TitleFragment extends Fragment
{

    private Button sch,dis,con,ref;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==MainActivity.REQUEST_TASKINFO&&resultCode==MainActivity.RESULT_TASKINFO){
            ((TransferActionListener) getActivity()).start_distribution(data);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_title, container, false);
        sch=(Button)view.findViewById(R.id.button_sch);
        dis=(Button)view.findViewById(R.id.button_dis);
        con=(Button)view.findViewById(R.id.button_con);
        ref =(Button)view.findViewById(R.id.button_ref);
        sch.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getActivity(),
                        "开始搜索  ",
                        Toast.LENGTH_SHORT).show();
                ((TransferActionListener)getActivity()).discover();
            }
        });
        con.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getActivity(),
                        "接受任务  ",
                        Toast.LENGTH_SHORT).show();
                ((TransferActionListener)getActivity()).start_contribution();
            }
        });
        dis.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getActivity(),
                        "分发任务  ",
                        Toast.LENGTH_SHORT).show();
                Intent intent =new Intent(getActivity(),GetTaskAcitivity.class);

                startActivityForResult(intent, MainActivity.REQUEST_TASKINFO);
            }
        });
        ref.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TaskActionListener)getActivity()).refresh();
            }
        });
        return view;
    }
}