package com.tunjid.raspberrynetworkservicediscovery.fragments;


import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
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

import com.tunjid.raspberrynetworkservicediscovery.NsdHelper;
import com.tunjid.raspberrynetworkservicediscovery.R;
import com.tunjid.raspberrynetworkservicediscovery.abstractclasses.AutoFragment;
import com.tunjid.raspberrynetworkservicediscovery.abstractclasses.DiscoveryListener;
import com.tunjid.raspberrynetworkservicediscovery.abstractclasses.ResolveListener;
import com.tunjid.raspberrynetworkservicediscovery.adapters.NSDAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServerListFragment extends AutoFragment
        implements
        NSDAdapter.ServiceClickedListener {

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
            nsdHelper.getmNsdManager().resolveService(service, getResolveListener());
        }
    };


    private ResolveListener getResolveListener() {
        return new ResolveListener() {
            @Override
            public void onServiceResolved(NsdServiceInfo service) {
                super.onServiceResolved(service);

                if (!services.contains(service)) services.add(service);

                if (recyclerView != null) recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        nsdHelper = new NsdHelper(getContext());
        nsdHelper.initializeDiscoveryListener(discoveryListener);
        //nsdHelper.initializeResolveListener(resolveListener);

        nsdHelper.discoverServices();
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

        floatingActionButton.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
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
    public void onServiceClicked(NsdServiceInfo serviceInfo) {
        showFragment(ClientFragment.newInstance(serviceInfo));
    }

    @Override
    public boolean isSelf(NsdServiceInfo serviceInfo) {
        return nsdHelper.getServiceName().equals(serviceInfo.getServiceName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nsdHelper.tearDown();
    }
}
