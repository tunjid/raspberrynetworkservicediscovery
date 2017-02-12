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

public class ChatAdapter extends BaseRecyclerViewAdapter<ChatAdapter.TextViewHolder, ChatAdapter.ChatAdapterListener> {

    private List<String> responses;

    public ChatAdapter(List<String> list) {
        this.responses = list;
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_responses, parent, false);
        return new TextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TextViewHolder holder, int position) {
        holder.bind(responses.get(position),  getAdapterListener());
    }

    @Override
    public int getItemCount() {
        return responses.size();
    }

    public interface ChatAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onTextClicked(String text);
    }

    static class TextViewHolder extends BaseViewHolder<ChatAdapterListener>
            implements View.OnClickListener {

        String text;
        TextView textView;

        TextViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
            textView.setOnClickListener(this);
        }

        void bind(String text, ChatAdapterListener listener) {
            this.text = text;

            textView.setText(text);
            adapterListener = listener;
        }

        @Override
        public void onClick(View v) {
            adapterListener.onTextClicked(text);
        }
    }
}
