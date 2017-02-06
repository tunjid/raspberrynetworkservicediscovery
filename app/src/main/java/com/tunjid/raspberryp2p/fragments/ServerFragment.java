package com.tunjid.raspberryp2p.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjid.raspberryp2p.R;
import com.tunjid.raspberryp2p.abstractclasses.AutoFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServerFragment extends AutoFragment
        implements View.OnClickListener {

    public ServerFragment() {
        // Required empty public constructor
    }

    public static ServerFragment newInstance() {
        ServerFragment fragment = new ServerFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_server, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        floatingActionButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                showFragment(ServerListFragment.newInstance());
                break;
        }
    }
}
