package com.tunjid.raspberryp2p.services;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.tunjid.raspberryp2p.abstractclasses.BaseService;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ClientService extends BaseService {

    private static final String TAG = ClientService.class.getSimpleName();

    public static final String NSD_SERVICE_INFO_KEY = "current Service key";
    public static final String ACTION_SOCKET_CONNECTED = "com.tunjid.raspberryp2p.services.ClientService_socket_connected";

    private MessageThread messageThread;

    private final IBinder binder = new NsdClientBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void connect(NsdServiceInfo serviceInfo) {

        // Initialize current service if we are starting up the first time
        if (messageThread == null) {
            messageThread = new MessageThread(serviceInfo, this);
        }

        // We're binding to an entirely new service. Tear down the current state
        else if (!serviceInfo.equals(messageThread.service)) {
            tearDown();
            messageThread = new MessageThread(serviceInfo, this);
        }

        // If we're already connected to this service, return
        else return;

        messageThread.start();
    }

    public void sendMessage(String message) {
        messageThread.send(message);
    }

    protected void tearDown() {
        super.tearDown();

        Log.e(TAG, "Tearing down ClientServer");

        if (messageThread != null) messageThread.exit();

    }

    public class NsdClientBinder extends Binder {
        // Binder impl
        public ClientService getClientService() {
            return ClientService.this;
        }
    }

    private static class MessageThread extends Thread {

        NsdServiceInfo service;

        Socket currentSocket;
        PrintWriter out;

        Context context;

        MessageThread(NsdServiceInfo serviceInfo, Context context) {
            this.service = serviceInfo;
            this.context = context;
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "Initializing client-side socket. Host: " + service.getHost() + ", Port: " + service.getPort());

                currentSocket = new Socket(service.getHost(), service.getPort());

                out = createPrintWriter(currentSocket);
                BufferedReader in = createBufferedReader(currentSocket);

                Log.d(TAG, "Client-side socket initialized.");

                Intent intent = new Intent();
                intent.setAction(ACTION_SOCKET_CONNECTED);

                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                String fromServer;

                while ((fromServer = in.readLine()) != null) {
                    System.out.println("Server: " + fromServer);

                    if (fromServer.equals("Bye.")) break;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        void send(String message) {
            new MessageSender(message, this).start();
        }

        synchronized void exit() {
            try {
                Log.d(TAG, "Exiting message thread.");
                currentSocket.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class MessageSender extends Thread {

        String message;
        MessageThread messageThread;

        MessageSender(String message, MessageThread messageThread) {
            this.message = message;
            this.messageThread = messageThread;
        }

        @Override
        public void run() {
            try {
                messageThread.out.println(message);
            }
            catch (Exception e) {
                Log.d(TAG, "Error3", e);
            }
            Log.d(TAG, "Client sent message: " + message);
        }
    }
}
