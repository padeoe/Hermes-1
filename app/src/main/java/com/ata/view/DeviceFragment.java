package com.ata.view;

import android.app.ListFragment;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ata.R;
import com.ata.provider.task.TaskInfo;
import com.ata.provider.transfer.Device;
import com.ata.provider.view.DeviceActionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raven on 2015/11/8.
 * 这是一个简陋的设备列表，每一条只包含设备名，通过DeviceActionListener提供的接口与控制器交互
 */
public class DeviceFragment extends ListFragment {
    private View mContentView = null;
    private List<Device >peers =new ArrayList<>();
    public DeviceFragment(){

    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
      Device device =(Device)getListAdapter().getItem(position);
        ((DeviceActionListener)getActivity()).showDetails(device);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);
        return mContentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new DeviceListAdapter(getActivity(), R.layout.row_device, peers));

    }

    public void onPeersAvailable(List<Device>devices){
        peers.clear();
        if(devices!=null)
            peers.addAll(devices);
        ((DeviceListAdapter) getListAdapter()).notifyDataSetChanged();

    }
    private class DeviceListAdapter extends ArrayAdapter<Device> {
        private List<Device> items;

        public DeviceListAdapter(Context context, int resource, List<Device> objects) {
            super(context, resource, objects);
            items=objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_device, null);
            }
            Device device = items.get(position);
            if (device != null) {
                TextView deviceName = (TextView) v.findViewById(R.id.device_name);

                if (deviceName != null) {
                    deviceName.setText(device.getDeviceName());
                }
                else {
                   Log.d(MainActivity.TAG,"error device name view");
                }
            }

            return v;

        }
    }

}
