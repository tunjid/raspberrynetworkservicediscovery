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

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.schedulers.Schedulers;


public class ClientService extends BaseService {

    private static final String TAG = ClientService.class.getSimpleName();

    public static final String NSD_SERVICE_INFO_KEY = "currentService key";

    private Socket currentSocket;
    private NsdServiceInfo currentService;

    private final IBinder binder = new NsdClientBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        NsdServiceInfo parceledService = intent.getParcelableExtra(NSD_SERVICE_INFO_KEY);

        // Initialize current servoce if we are starting up the first time
        if (currentService == null) currentService = parceledService;

            // If we're already connected to this NsdServiceInfo, return
        else if (parceledService.equals(currentService)) return binder;

            // We're binding to an entirely new service. Tear down the current state
        else tearDown();

        Completable.create(this).subscribeOn(Schedulers.io()).subscribe(this);

        return binder;
    }

    public NsdServiceInfo getCurrentService() {
        return currentService;
    }

    public void sendMessage(String message) {
        Completable.create(new MessageSender(message, currentSocket))
                .subscribeOn(Schedulers.io())
                .subscribe();
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
    public void subscribe(CompletableEmitter emitter) throws Exception {
        try {
            if (currentSocket == null) {
                currentSocket = new Socket(currentService.getHost(), currentService.getPort());

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

    public class NsdClientBinder extends Binder {
        // Binder impl
        public ClientService getClientService() {
            return ClientService.this;
        }
    }

    static class MessageSender implements CompletableOnSubscribe {

        String message;
        Socket socket;

        MessageSender(String message, Socket socket) {
            this.message = message;
            this.socket = socket;
        }

        @Override
        public void subscribe(CompletableEmitter emitter) throws Exception {
            try {
                createPrintWriter(socket).println(message);
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
            Log.d(TAG, "Client sent message: " + message);
        }
    }
}
