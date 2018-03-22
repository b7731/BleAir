package com.natynki.bleair;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class sensorListAdapter extends RecyclerView.Adapter<sensorListAdapter.MyViewHolder> {
    private final List<Sensor> sensorList;

    sensorListAdapter(List<Sensor> sensorList) {
        this.sensorList = sensorList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sensor_data_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Sensor sensor = sensorList.get(position);
        holder.name.setText(sensor.getName());
        holder.data.setText(sensor.getData());
        holder.suure.setText(sensor.getSuure());
    }

    @Override
    public int getItemCount() {
        return sensorList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView name;
        public final TextView data;
        final TextView suure;

        private MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            data = view.findViewById(R.id.data);
            suure = view.findViewById(R.id.suure);
        }

        @Override
        public void onClick(View view) {
            //   Sensor sensor = sensorList.get(getPosition());
        }
    }

}