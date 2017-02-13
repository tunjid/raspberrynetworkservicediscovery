package com.tunjid.raspberrynetworkservicediscovery.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;

import com.tunjid.raspberrynetworkservicediscovery.R;
import com.tunjid.raspberrynetworkservicediscovery.abstractclasses.BaseActivity;
import com.tunjid.raspberrynetworkservicediscovery.fragments.ServerFragment;
import com.tunjid.raspberrynetworkservicediscovery.services.ServerService;

public class AutoActivity extends BaseActivity
        implements ServiceConnection {

    private FloatingActionButton floatingActionButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        Intent server = new Intent(this, ServerService.class);
        bindService(server, this, BIND_AUTO_CREATE);

        if (savedInstanceState == null) showFragment(ServerFragment.newInstance());
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    public FloatingActionButton getFloatingActionButton() {
        return floatingActionButton;
    }
}
