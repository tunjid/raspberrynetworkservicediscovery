package com.tunjid.raspberrynetworkservicediscovery.abstractclasses;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;

import com.tunjid.raspberrynetworkservicediscovery.activities.AutoActivity;

/**
 * Base Fragment class
 * <p>
 * Created by tj.dahunsi on 2/5/17.
 */

public abstract class BaseFragment extends Fragment {

    protected FloatingActionButton floatingActionButton;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        floatingActionButton = ((AutoActivity) getActivity()).getFloatingActionButton();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        floatingActionButton.setOnClickListener(null);
    }

    public String getStableTag() {
        return getClass().getSimpleName();
    }

    public boolean showFragment(BaseFragment fragment) {
        return ((BaseActivity) getActivity()).showFragment(fragment);
    }
}
