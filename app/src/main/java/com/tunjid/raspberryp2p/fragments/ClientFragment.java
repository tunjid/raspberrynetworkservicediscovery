package com.tunjid.raspberryp2p.fragments;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.tunjid.raspberryp2p.R;
import com.tunjid.raspberryp2p.abstractclasses.AutoFragment;
import com.tunjid.raspberryp2p.adapters.ChatAdapter;
import com.tunjid.raspberryp2p.services.ClientService;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClientFragment extends AutoFragment
        implements
        ServiceConnection,
        View.OnClickListener {

    private boolean isReceiverRegistered;

    private NsdServiceInfo service;
    private ClientService clientService;

    private ProgressDialog progressDialog;

    private EditText editText;
    private RecyclerView recyclerView;

    private List<String> responses = new ArrayList<>();

    private final IntentFilter clientServiceFilter = new IntentFilter();

    private final BroadcastReceiver clientServiceReceiever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ClientService.ACTION_SOCKET_CONNECTED:
                    if (progressDialog != null) progressDialog.dismiss();
                    break;
                case ClientService.ACTION_SERVER_RESPONSE:
                    String response = intent.getStringExtra(ClientService.DATA_SERVER_RESPONSE);

                    responses.add(response);
                    recyclerView.getAdapter().notifyItemInserted(responses.size() - 1);
                    recyclerView.smoothScrollToPosition(responses.size() - 1);
                    break;
            }
        }
    };

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

        clientServiceFilter.addAction(ClientService.ACTION_SOCKET_CONNECTED);
        clientServiceFilter.addAction(ClientService.ACTION_SERVER_RESPONSE);

        service = getArguments().getParcelable(ClientService.NSD_SERVICE_INFO_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_client, container, false);
        View send = rootView.findViewById(R.id.send);
        editText = (EditText) rootView.findViewById(R.id.edit_text);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.list);

        recyclerView.setAdapter(new ChatAdapter(responses));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        send.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        floatingActionButton.setVisibility(View.GONE);

        Intent clientIntent = new Intent(getActivity(), ClientService.class);
        getActivity().bindService(clientIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = ProgressDialog.show(getActivity(),
                getString(R.string.connection_title),
                getString(R.string.connection_text), true, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        editText = null;
        recyclerView = null;
        progressDialog = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {

        clientService = ((ClientService.NsdClientBinder) binder).getClientService();

        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(clientServiceReceiever, clientServiceFilter);
            isReceiverRegistered = true;
        }

        clientService.connect(service);
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
                    String message = editText.getText().toString();
                    clientService.sendMessage(message);

                    responses.add(message);
                    recyclerView.getAdapter().notifyItemInserted(responses.size() - 1);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(this);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(clientServiceReceiever);
    }
}
