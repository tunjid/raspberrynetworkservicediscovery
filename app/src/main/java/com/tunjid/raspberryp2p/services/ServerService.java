package com.tunjid.raspberryp2p.services;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.tunjid.raspberryp2p.KnockKnockProtocol;
import com.tunjid.raspberryp2p.abstractclasses.BaseService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.schedulers.Schedulers;


public class ServerService extends BaseService {

    private static final String TAG = ServerService.class.getSimpleName();

    private ServerSocket serverSocket;

    private final IBinder binder = new NsdServerBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        nsdHelper.initializeRegistrationListener();

        // Since discovery will happen via Nsd, we don't need to care which port is
        // used, just grab an available one and advertise it via Nsd.
        try {
            serverSocket = new ServerSocket(0);
            nsdHelper.registerService(serverSocket.getLocalPort());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Completable.create(this).subscribeOn(Schedulers.io()).subscribe(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    protected void tearDown() {
        super.tearDown();

        try {
            if (serverSocket != null) serverSocket.close();
        }
        catch (IOException ioe) {
            Log.e(TAG, "Error when closing server currentSocket.");
        }
    }

    @Override
    public void subscribe(CompletableEmitter emitter) throws Exception {
        try {
            Log.d(TAG, "ServerSocket Created, awaiting connection.");

            while (true) {
                // Create new clients for every connection received
                new Client(serverSocket.accept());
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Error creating ServerSocket: ", e);
            e.printStackTrace();
        }
    }

    public class NsdServerBinder extends Binder {
        // Binder impl
    }

    private static class Client {

        Client(Socket socket) {
            Log.d(TAG, "Connected to new client");

            if (socket != null && socket.isConnected()) {
                try {

                    PrintWriter out = createPrintWriter(socket);
                    BufferedReader in = createBufferedReader(socket);

                    String inputLine, outputLine;

                    // Initiate conversation with client
                    KnockKnockProtocol kkp = new KnockKnockProtocol();
                    outputLine = kkp.processInput(null);

                    out.println(outputLine);

                    while ((inputLine = in.readLine()) != null) {
                        outputLine = kkp.processInput(inputLine);
                        out.println(outputLine);

                        Log.d(TAG, "Read from client stream: " + inputLine);

                        if (outputLine.equals("Bye.")) break;
                    }


                    in.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
