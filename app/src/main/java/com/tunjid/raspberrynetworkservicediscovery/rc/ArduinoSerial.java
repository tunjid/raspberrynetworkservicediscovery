package com.tunjid.raspberrynetworkservicediscovery.rc;

import android.os.Handler;
import android.os.HandlerThread;
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

    private static final int BAUD_RATE = 115200;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;

    private static final String TAG = ArduinoSerial.class.getSimpleName();
    private static final String UART_NAME = "UART0";

    private volatile String read = null;
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
                while ((count = uart.read(buffer, buffer.length)) > 0) {
                    Log.d(TAG, "Read " + count + " bytes from peripheral");
                    for (byte b : buffer) Log.d(TAG, "Read byte" + b);
                }
                read = new String(buffer);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return true;

        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            super.onUartDeviceError(uart, error);
            Log.w(TAG, uart + ": Error event " + error);
        }
    };

    public ArduinoSerial() {
        try {
            PeripheralManagerService manager = new PeripheralManagerService();
            serial = manager.openUartDevice(UART_NAME);
            serial.setBaudrate(BAUD_RATE);
            serial.setDataSize(DATA_BITS);
            serial.setParity(UartDevice.PARITY_NONE);
            serial.setStopBits(STOP_BITS);

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
        catch (Exception e) {
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


    /**
     * Access and configure the requested UART device for 8N1.
     *
     * @param name Name of the UART peripheral device to open.
     * @param baudRate Data transfer rate. Should be a standard UART baud,
     * such as 9600, 19200, 38400, 57600, 115200, etc.
     * @throws IOException if an error occurs opening the UART port.
     */
    private void openUart(String name, int baudRate) throws IOException {
        PeripheralManagerService manager = new PeripheralManagerService();

        serial = manager.openUartDevice(name);
        // Configure the UART
        serial.setBaudrate(baudRate);
        serial.setDataSize(DATA_BITS);
        serial.setParity(UartDevice.PARITY_NONE);
        serial.setStopBits(STOP_BITS);

        //serial.registerUartDeviceCallback(callback, inputHandler);
    }

    @Override
    public void close() throws IOException {
        serial.unregisterUartDeviceCallback(callback);
        serial.close();
        serial = null;
        Log.d(TAG, "Closed ArduinoSerial");
    }
}
