package com.tunjid.raspberryp2p.services;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.tunjid.raspberryp2p.CommsProtocol;
import com.tunjid.raspberryp2p.KnockKnockProtocol;
import com.tunjid.raspberryp2p.NsdHelper;
import com.tunjid.raspberryp2p.abstractclasses.BaseService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerService extends BaseService {

    private static final String TAG = ServerService.class.getSimpleName();

    private ClientThread clientThread;
    private final IBinder binder = new NsdServerBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        clientThread = new ClientThread(nsdHelper);
        clientThread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    protected void tearDown() {
        super.tearDown();
        clientThread.tearDown();
    }

    public class NsdServerBinder extends Binder {
        // Binder impl
    }

    static class ClientThread extends Thread {

        volatile boolean isRunning;

        private ServerSocket serverSocket;

        ClientThread(NsdHelper helper) {

            helper.initializeRegistrationListener();

            // Since discovery will happen via Nsd, we don't need to care which port is
            // used, just grab an available one and advertise it via Nsd.
            try {
                serverSocket = new ServerSocket(0);
                helper.registerService(serverSocket.getLocalPort());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            isRunning = true;

            while (isRunning) {
                try {
                    Log.d(TAG, "ServerSocket Created, awaiting connection.");
                    // Create new clients for every connection received
                    new Client(serverSocket.accept());
                }
                catch (Exception e) {
                    Log.e(TAG, "Error creating ServerSocket: ", e);
                    e.printStackTrace();
                }
            }
        }

        void tearDown() {
            isRunning = false;
            try {
                Log.d(TAG, "Attempting to close server socket.");
                serverSocket.close();
            }
            catch (Exception e) {
                Log.e(TAG, "Error closing ServerSocket: ", e);
                e.printStackTrace();
            }
        }
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
                    CommsProtocol commsProtocol = new KnockKnockProtocol();
                    outputLine = commsProtocol.processInput(null);

                    out.println(outputLine);

                    while ((inputLine = in.readLine()) != null) {
                        outputLine = commsProtocol.processInput(inputLine);
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
