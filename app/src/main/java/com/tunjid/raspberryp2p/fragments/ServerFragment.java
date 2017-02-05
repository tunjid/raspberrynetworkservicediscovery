package com.tunjid.raspberryp2p.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.helloworld.utils.baseclasses.BaseFragment;
import com.tunjid.raspberryp2p.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServerFragment extends BaseFragment
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                showFragment(ServerListFragment.newInstance());
                break;
        }
    }
}
