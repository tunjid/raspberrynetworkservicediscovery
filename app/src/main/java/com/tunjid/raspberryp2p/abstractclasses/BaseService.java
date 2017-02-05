package com.tunjid.raspberryp2p.abstractclasses;

import android.app.Service;
import android.support.annotation.CallSuper;

import com.tunjid.raspberryp2p.NsdHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Base service with rx operators
 * <p>
 * Created by tj.dahunsi on 2/5/17.
 */

public abstract class BaseService extends Service implements Observer<Integer> {

    protected NsdHelper nsdHelper;
    protected Disposable currentSocketDisposable;

    @Override
    public void onCreate() {
        super.onCreate();
        nsdHelper = new NsdHelper(this);
    }

    @Override
    public void onSubscribe(Disposable disposable) {
        currentSocketDisposable = disposable;
    }

    @Override
    public void onNext(Integer integer) {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tearDown();
    }

    @CallSuper
    protected void tearDown() {
        if (currentSocketDisposable != null) currentSocketDisposable.dispose();
        nsdHelper.tearDown();
    }

    protected PrintWriter createPrintWriter(Socket socket) throws IOException {
        return new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);
    }

    protected BufferedReader createBufferedReader(Socket socket)  throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
}
