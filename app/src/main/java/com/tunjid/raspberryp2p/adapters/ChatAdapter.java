package com.tunjid.raspberryp2p.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.helloworld.utils.baseclasses.BaseRecyclerViewAdapter;
import com.helloworld.utils.baseclasses.BaseViewHolder;
import com.tunjid.raspberryp2p.R;

import java.util.List;

/**
 * Adapter for showing open NSD services
 * <p>
 * Created by tj.dahunsi on 2/4/17.
 */

public class ChatAdapter extends BaseRecyclerViewAdapter<ChatAdapter.TextViewHolder, BaseRecyclerViewAdapter.AdapterListener> {

    private List<String> responses;

    public ChatAdapter(List<String> list) {
        this.responses = list;
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_nsd_list, parent, false);
        return new TextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, int position) {
        holder.bind(responses.get(position));
    }

    @Override
    public int getItemCount() {
        return responses.size();
    }

    static class TextViewHolder extends BaseViewHolder<BaseRecyclerViewAdapter.AdapterListener>{

        TextView textView;

        TextViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
        }

        void bind(String text) {
            textView.setText(text);
        }

    }
}
