package com.tunjid.raspberryp2p.fragments;


import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.tunjid.raspberryp2p.R;
import com.tunjid.raspberryp2p.abstractclasses.AutoFragment;
import com.tunjid.raspberryp2p.services.ClientService;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClientFragment extends AutoFragment
        implements
        ServiceConnection,
        View.OnClickListener {

    private NsdServiceInfo service;
    private ClientService clientService;

    private ProgressDialog progressDialog;

    private EditText editText;

    public ClientFragment() {
        // Required empty public constructor
    }

    public static ClientFragment newInstance(NsdServiceInfo nsdServiceInfo) {
        ClientFragment fragment = new ClientFragment();
        Bundle bundle = new Bundle();

        bundle.putParcelable(ClientService.NSD_SERVICE_INFO_KEY, nsdServiceInfo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        service = getArguments().getParcelable(ClientService.NSD_SERVICE_INFO_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_client, container, false);
        View send = rootView.findViewById(R.id.send);
        editText = (EditText) rootView.findViewById(R.id.edit_text);

        send.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        floatingActionButton.setVisibility(View.GONE);

        Intent clientIntent = new Intent(getActivity(), ClientService.class);
        clientIntent.putExtra(ClientService.NSD_SERVICE_INFO_KEY, service);

        getActivity().bindService(clientIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = ProgressDialog.show(getActivity(),
                getString(R.string.connection_title),
                getString(R.string.connection_text), true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {

        clientService = ((ClientService.NsdClientBinder) binder).getClientService();

        if (progressDialog != null) progressDialog.dismiss();

        // We aren't bound to the same NSD service
        if (!service.equals(clientService.getCurrentService())) getActivity().onBackPressed();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        clientService = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                break;
            case R.id.send:
                if (clientService != null) {
                    clientService.sendMessage(editText.getText().toString());
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(this);
    }
}
