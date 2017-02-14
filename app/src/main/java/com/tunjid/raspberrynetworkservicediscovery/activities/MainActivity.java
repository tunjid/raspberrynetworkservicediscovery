package com.tunjid.raspberrynetworkservicediscovery.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import com.tunjid.raspberrynetworkservicediscovery.R;
import com.tunjid.raspberrynetworkservicediscovery.abstractclasses.BaseActivity;
import com.tunjid.raspberrynetworkservicediscovery.fragments.ServerFragment;

public class MainActivity extends BaseActivity {

    private FloatingActionButton floatingActionButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        if (savedInstanceState == null) showFragment(ServerFragment.newInstance());
    }


    public FloatingActionButton getFloatingActionButton() {
        return floatingActionButton;
    }
}
