package com.apulsetech.sample.bluetooth.barcode.barcodescansample.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.apulsetech.lib.remote.type.RemoteDevice;
import com.apulsetech.sample.bluetooth.barcode.barcodescansample.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import androidx.recyclerview.widget.RecyclerView;

public class DeviceListAdapter extends BaseAdapter {

    private List<RemoteDevice> items;
    private Map<String,RemoteDevice> itemFilter;
    private RecyclerView recyclerView;

    public DeviceListAdapter() {
        this.items = new ArrayList<>();
        this.itemFilter = new HashMap<>();
        this.recyclerView = null;
    }

    public void add(RemoteDevice device) {
        String addr = device.getAddress();

        if(this.itemFilter.containsKey(addr)) {
            device = this.itemFilter.get(addr); // 찾은 장치에 필터에 있던 장치를 왜 넣는거지?
            this.itemFilter.remove(addr);
            this.items.remove(device);
        }
        this.itemFilter.put(addr, device);
        this.items.add(device);
    }

    public String getAddress(int position) {return this.items.get(position).getAddress();}

    public void clear(){
        this.items.clear();
        this.itemFilter.clear();
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public Object getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null) {
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_bluetooth_device,parent,false); // item_bluetooth_device가 부모객체가 되어 그 밑 하위 객체들을 사용할 수 있다.
            holder = new ViewHolder(convertView);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.display(this.items.get(position));
        return convertView;
    }

    class ViewHolder {
        private TextView txtName;
        private TextView txtAddress;

        public ViewHolder(@NonNull View itemView) {
            txtName = itemView.findViewById(R.id.name);
            txtAddress = itemView.findViewById(R.id.address);
            itemView.setTag(this);
        }

        public void display(RemoteDevice device) {
            txtName.setText(device.getName());
            txtAddress.setText(device.getAddress());
        }
    }




}
