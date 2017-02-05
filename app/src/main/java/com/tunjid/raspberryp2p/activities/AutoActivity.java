package com.tunjid.raspberryp2p.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.helloworld.utils.baseclasses.BaseActivity;
import com.helloworld.utils.widget.FloatingActionButton;
import com.tunjid.raspberryp2p.R;
import com.tunjid.raspberryp2p.fragments.ServerListFragment;
import com.tunjid.raspberryp2p.services.ServerService;

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

        if (savedInstanceState == null) showFragment(ServerListFragment.newInstance());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(this);
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
