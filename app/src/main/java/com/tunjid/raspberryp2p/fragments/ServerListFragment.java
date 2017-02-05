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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.helloworld.utils.baseclasses.BaseFragment;
import com.tunjid.raspberryp2p.services.ClientService;
import com.tunjid.raspberryp2p.adapters.NSDAdapter;
import com.tunjid.raspberryp2p.NsdHelper;
import com.tunjid.raspberryp2p.R;
import com.tunjid.raspberryp2p.abstractclasses.DiscoveryListener;
import com.tunjid.raspberryp2p.abstractclasses.ResolveListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServerListFragment extends BaseFragment
        implements
        ServiceConnection,
        NSDAdapter.ServiceClickedListener {

    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;

    private NsdHelper nsdHelper;
    private List<NsdServiceInfo> services = new ArrayList<>();

    public ServerListFragment() {
        // Required empty public constructor
    }

    public static ServerListFragment newInstance() {
        ServerListFragment fragment = new ServerListFragment();
        Bundle bundle = new Bundle();

        fragment.setArguments(bundle);
        return fragment;
    }

    private DiscoveryListener discoveryListener = new DiscoveryListener() {
        @Override
        public void onServiceFound(NsdServiceInfo service) {
            super.onServiceFound(service);
            nsdHelper.getmNsdManager().resolveService(service, resolveListener);
        }
    };

    private ResolveListener resolveListener = new ResolveListener() {
        @Override
        public void onServiceResolved(NsdServiceInfo service) {
            super.onServiceResolved(service);

            services.add(service);

            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_server_list, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.list);

        NSDAdapter adapter = new NSDAdapter(services);
        adapter.setAdapterListener(this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nsdHelper = new NsdHelper(getContext());

        nsdHelper.initializeDiscoveryListener(discoveryListener);
        nsdHelper.initializeResolveListener(resolveListener);

        nsdHelper.discoverServices();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.menu_client, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                nsdHelper.discoverServices();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        if (progressDialog != null) progressDialog.dismiss();

        ClientService clientService = ((ClientService.NsdClientBinder) binder).getClientService();
        showFragment(ClientFragment.newInstance(clientService.getService()));
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        // Not used
    }

    @Override
    public void onServiceClicked(NsdServiceInfo serviceInfo) {
        progressDialog = ProgressDialog.show(getActivity(), "",
                "Loading. Please wait...", true);

        Intent clientIntent = new Intent(getActivity(), ClientService.class);
        clientIntent.putExtra(ClientService.NSD_SERVICE_INFO_KEY, serviceInfo);

        getActivity().bindService(clientIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(this);
    }
}
