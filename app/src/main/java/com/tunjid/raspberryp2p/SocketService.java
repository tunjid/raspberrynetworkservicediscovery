package com.tunjid.raspberryp2p;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


public class SocketService extends Service {

    private final IBinder binder = new NsdBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class NsdBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }
    /*
    NsdHelper helper = new NsdHelper();

    @Override
    public void onCreate() {
        super.onCreate();
        helper.registerService(this);
    }



    @Override
    public boolean onUnbind(Intent intent) {
        helper.tearDown();
        return super.onUnbind(intent);
    }

*/
}
