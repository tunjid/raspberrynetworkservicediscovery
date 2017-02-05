package com.tunjid.raspberryp2p.services;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.tunjid.raspberryp2p.abstractclasses.BaseService;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class ClientService extends BaseService implements ObservableOnSubscribe<Integer> {

    private static final String TAG = ClientService.class.getSimpleName();

    public static final String NSD_SERVICE_INFO_KEY = "service key";

    private Socket currentSocket;
    private NsdServiceInfo service;

    private final IBinder binder = new NsdClientBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        service = intent.getParcelableExtra(NSD_SERVICE_INFO_KEY);

        Observable.create(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);

        return binder;
    }

    public NsdServiceInfo getService() {
        return service;
    }

    public void sendMessage(String msg) {
        try {
            createPrintWriter(currentSocket).println(msg);
        }
        catch (UnknownHostException e) {
            Log.d(TAG, "Unknown Host", e);
        }
        catch (IOException e) {
            Log.d(TAG, "I/O Exception", e);
        }
        catch (Exception e) {
            Log.d(TAG, "Error3", e);
        }
        Log.d(TAG, "Client sent message: " + msg);
    }

    protected void tearDown() {
        super.tearDown();

        try {
            currentSocket.close();
        }
        catch (IOException ioe) {
            Log.e(TAG, "Error when closing server currentSocket.");
        }
    }

    @Override
    public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {

        if (!emitter.isDisposed()) {

            try {
                if (currentSocket == null) {
                    currentSocket = new Socket(service.getHost(), service.getPort());

                    BufferedReader in = createBufferedReader(currentSocket);

                    Log.d(TAG, "Client-side socket initialized.");

                    String fromServer;

                    while ((fromServer = in.readLine()) != null) {
                        System.out.println("Server: " + fromServer);

                        if (fromServer.equals("Bye.")) break;
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class NsdClientBinder extends Binder {
        // Binder impl
        public ClientService getClientService() {
            return ClientService.this;
        }
    }
}
