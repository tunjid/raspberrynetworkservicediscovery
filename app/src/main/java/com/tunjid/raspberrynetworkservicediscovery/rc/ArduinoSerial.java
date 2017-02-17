package com.tunjid.raspberrynetworkservicediscovery.rc;

import android.os.Looper;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.Closeable;
import java.io.IOException;

/**
 * Class for speaking to an Arduino over UART
 * <p>
 * Created by tj.dahunsi on 2/14/17.
 */

public class ArduinoSerial implements Closeable {

    private static final String TAG = ArduinoSerial.class.getSimpleName();
    private static final String UART_NAME = "UART0";

    private String read = null;
    private UartDevice serial;
    private final UartDeviceCallback callback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            Log.d(TAG, "Data is available!");

            try {
                // Maximum amount of data to read at one time
                final int maxCount = 20;
                byte[] buffer = new byte[maxCount];

                int count;
                while ((count = serial.read(buffer, buffer.length)) > 0) {
                    Log.d(TAG, "Read " + count + " bytes from peripheral");
                }
                read = new String(buffer);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return true;

        }
    };

    public ArduinoSerial() {
        try {
            PeripheralManagerService manager = new PeripheralManagerService();
            serial = manager.openUartDevice(UART_NAME);
            serial.setBaudrate(9600);
            serial.setDataSize(8);
            serial.setParity(UartDevice.PARITY_NONE);
            serial.setStopBits(1);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Looper.prepare();
                        serial.registerUartDeviceCallback(callback);
                        Looper.loop();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        catch (IOException e) {
            Log.d(TAG, "Unable to access UART device", e);
        }
    }

    public boolean write(String string) {
        try {
            return serial.write(string.getBytes(), string.length()) == string.length();
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String read() {
        long now = System.currentTimeMillis();
        while (System.currentTimeMillis() - now < 5000) ; // Do nothing
        String result = read == null ? "Timeout" : read;
        read = null;
        return result;
    }

    @Override
    public void close() throws IOException {
        serial.unregisterUartDeviceCallback(callback);
        serial.close();
        serial = null;
    }
}
