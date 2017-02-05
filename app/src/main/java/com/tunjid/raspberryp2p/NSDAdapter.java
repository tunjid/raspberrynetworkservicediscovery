package com.tunjid.raspberryp2p;

import android.net.nsd.NsdServiceInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by tj.dahunsi on 2/4/17.
 */

public class NSDAdapter extends RecyclerView.Adapter<NSDAdapter.NSDViewHolder> {

    List<NsdServiceInfo> infoList;

    protected NSDAdapter(List<NsdServiceInfo> list) {
        this.infoList = list;
    }

    @Override
    public NSDViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_nsd_list, parent);
        return new NSDViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NSDViewHolder holder, int position) {
        holder.bind(infoList.get(position));
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    static class NSDViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public NSDViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.text);
        }

        void bind(NsdServiceInfo info) {
            textView.setText(info.getServiceName());
        }
    }
}
