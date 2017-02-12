package com.tunjid.raspberrynetworkservicediscovery.abstractclasses;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.helloworld.utils.baseclasses.BaseFragment;
import com.helloworld.utils.widget.FloatingActionButton;
import com.tunjid.raspberrynetworkservicediscovery.activities.AutoActivity;

/**
 * Base Fragment class
 * <p>
 * Created by tj.dahunsi on 2/5/17.
 */

public abstract class AutoFragment extends BaseFragment {

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
}
