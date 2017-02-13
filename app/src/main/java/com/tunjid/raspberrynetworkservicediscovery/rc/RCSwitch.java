package com.tunjid.raspberrynetworkservicediscovery.rc;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.Closeable;
import java.io.IOException;

/**
 * Java port of RCSwitch - Arduino libary for remote control outlet switches
 * by Suat Özgür. https://github.com/sui77/rc-switch/
 * <p>
 * Created by tj.dahunsi on 2/10/17.
 */

public class RCSwitch implements Closeable {

    private static final String TAG = "RCSwitch";

    private static final int RCSWITCH_MAX_CHANGES = 67;

    private static int nReceiveTolerance = 60;
    private static int nReceivedBitlength = 0;
    private static int nReceivedProtocol = 0;

    private static long nReceivedValue = 0;
    private static long nReceivedDelay = 0;

    private static final int nSeparationLimit = 4300;

    // Interrupt vars
    private static int changeCount = 0;
    private static int repeatCount = 0;
    private static long lastTime = 0;

    /*
     * timings[0] contains sync timing, followed by a number of bits
     */
    private static long[] timings = new long[RCSWITCH_MAX_CHANGES];

    private static final Protocol PROTOCOLS[] = {
            new Protocol(false, 350, new HighLow(1, 31), new HighLow(1, 3), new HighLow(3, 1)),    // protocol 1
            new Protocol(false, 650, new HighLow(1, 10), new HighLow(1, 2), new HighLow(2, 1)),    // protocol 2
            new Protocol(false, 100, new HighLow(30, 71), new HighLow(4, 11), new HighLow(9, 6)),    // protocol 3
            new Protocol(false, 380, new HighLow(1, 6), new HighLow(1, 3), new HighLow(3, 1)),    // protocol 4
            new Protocol(false, 500, new HighLow(6, 14), new HighLow(1, 2), new HighLow(2, 1)),    // protocol 5
            new Protocol(true, 450, new HighLow(23, 1), new HighLow(1, 2), new HighLow(2, 1))      // protocol 6 (HT6P20B)
    };

    private int nRepeatTransmit;

    private String interruptPinName;
    private String transmitterPinName;

    private Protocol protocol;

    private Gpio transmitter;
    private Gpio interruptReceiver;

    private final GpioCallback interruptCallback = new InterruptCallback();

    private PeripheralManagerService manager = new PeripheralManagerService();

    public RCSwitch() {
        this.setRepeatTransmit(10);
        this.setProtocol(1);
        // Android things GPI0 interface is incredibly slow.
        // The receive tolerance is 60% on Arduino, here it has to be > 400%,
        // and many bits will be droped.
        this.setReceiveTolerance(400);
        nReceivedValue = 0;
    }

    /**
     * Sets the protocol to send.
     */
    void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Sets the protocol to send, from a list of predefined protocols
     */
    void setProtocol(int nProtocol) {
        if (nProtocol < 1 || nProtocol > PROTOCOLS.length) {
            nProtocol = 1;  // TODO: trigger an error, e.g. "bad protocol" ???
        }
        this.protocol = new Protocol(PROTOCOLS[nProtocol - 1]);
    }

    /**
     * Sets the protocol to send with pulse length in microseconds.
     */
    void setProtocol(int nProtocol, int nPulseLength) {
        setProtocol(nProtocol);
        this.setPulseLength(nPulseLength);
    }

    /**
     * Sets pulse length in microseconds
     */
    void setPulseLength(int nPulseLength) {
        this.protocol.pulseLength = nPulseLength;
    }

    /**
     * Sets Repeat Transmits
     */
    void setRepeatTransmit(int nRepeatTransmit) {
        this.nRepeatTransmit = nRepeatTransmit;
    }

    /**
     * Set Receiving Tolerance
     */
    void setReceiveTolerance(int nPercent) {
        nReceiveTolerance = nPercent;
    }

    /**
     * Enable transmissions
     *
     * @param transmitterPin Arduino Pin to which the sender is connected to
     */
    public void enableTransmit(String transmitterPin) {
        this.transmitterPinName = transmitterPin;

        try {
            transmitter = manager.openGpio(transmitterPin);
            transmitter.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Disable transmissions
     */
    void disableTransmit() {
        this.transmitterPinName = null;
    }

    /**
     * Switch a remote switch on (Type D REV)
     *
     * @param sGroup Code of the switch group (A,B,C,D)
     * @param nDevice Number of the switch itself (1..3)
     */
    void switchOn(char sGroup, int nDevice) {
        this.sendTriState(this.getCodeWordD(sGroup, nDevice, true));
    }

    /**
     * Switch a remote switch off (Type D REV)
     *
     * @param sGroup Code of the switch group (A,B,C,D)
     * @param nDevice Number of the switch itself (1..3)
     */
    void switchOff(char sGroup, int nDevice) {
        this.sendTriState(this.getCodeWordD(sGroup, nDevice, false));
    }

    /**
     * Switch a remote switch on (Type C Intertechno)
     *
     * @param sFamily Familycode (a..f)
     * @param nGroup Number of group (1..4)
     * @param nDevice Number of device (1..4)
     */
    void switchOn(char sFamily, int nGroup, int nDevice) {
        this.sendTriState(this.getCodeWordC(sFamily, nGroup, nDevice, true));
    }

    /**
     * Switch a remote switch off (Type C Intertechno)
     *
     * @param sFamily Familycode (a..f)
     * @param nGroup Number of group (1..4)
     * @param nDevice Number of device (1..4)
     */
    void switchOff(char sFamily, int nGroup, int nDevice) {
        this.sendTriState(this.getCodeWordC(sFamily, nGroup, nDevice, false));
    }

    /**
     * Switch a remote switch on (Type B with two rotary/sliding switches)
     *
     * @param nAddressCode Number of the switch group (1..4)
     * @param nChannelCode Number of the switch itself (1..4)
     */
    void switchOn(int nAddressCode, int nChannelCode) {
        this.sendTriState(this.getCodeWordB(nAddressCode, nChannelCode, true));
    }

    /**
     * Switch a remote switch off (Type B with two rotary/sliding switches)
     *
     * @param nAddressCode Number of the switch group (1..4)
     * @param nChannelCode Number of the switch itself (1..4)
     */
    void switchOff(int nAddressCode, int nChannelCode) {
        this.sendTriState(this.getCodeWordB(nAddressCode, nChannelCode, false));
    }

    /**
     * Switch a remote switch on (Type A with 10 pole DIP switches)
     *
     * @param sGroup Code of the switch group (refers to DIP switches 1..5 where "1" = on and "0" = off, if all DIP switches are on it's "11111")
     * @param sDevice Code of the switch device (refers to DIP switches 6..10 (A..E) where "1" = on and "0" = off, if all DIP switches are on it's "11111")
     */
    void switchOn(char[] sGroup, char[] sDevice) {
        this.sendTriState(this.getCodeWordA(sGroup, sDevice, true));
    }

    /**
     * Switch a remote switch off (Type A with 10 pole DIP switches)
     *
     * @param sGroup Code of the switch group (refers to DIP switches 1..5 where "1" = on and "0" = off, if all DIP switches are on it's "11111")
     * @param sDevice Code of the switch device (refers to DIP switches 6..10 (A..E) where "1" = on and "0" = off, if all DIP switches are on it's "11111")
     */
    void switchOff(char[] sGroup, char[] sDevice) {
        this.sendTriState(this.getCodeWordA(sGroup, sDevice, false));
    }


    /**
     * Returns a char[13], representing the code word to be send.
     */
    char[] getCodeWordA(char[] sGroup, char[] sDevice, boolean bStatus) {
        char[] sReturn = new char[13];
        int nReturnPos = 0;

        for (int i = 0; i < 5; i++) {
            sReturn[nReturnPos++] = (sGroup[i] == '0') ? 'F' : '0';
        }

        for (int i = 0; i < 5; i++) {
            sReturn[nReturnPos++] = (sDevice[i] == '0') ? 'F' : '0';
        }

        sReturn[nReturnPos++] = bStatus ? '0' : 'F';
        sReturn[nReturnPos++] = bStatus ? 'F' : '0';

        sReturn[nReturnPos] = '\0';
        return sReturn;
    }

    /**
     * Encoding for type B switches with two rotary/sliding switches.
     * <p>
     * The code word is a tristate word and with following bit pattern:
     * <p>
     * +-----------------------------+-----------------------------+----------+------------+
     * | 4 bits address              | 4 bits address              | 3 bits   | 1 bit      |
     * | switch group                | switch number               | not used | on / off   |
     * | 1=0FFF 2=F0FF 3=FF0F 4=FFF0 | 1=0FFF 2=F0FF 3=FF0F 4=FFF0 | FFF      | on=F off=0 |
     * +-----------------------------+-----------------------------+----------+------------+
     *
     * @param nAddressCode Number of the switch group (1..4)
     * @param nChannelCode Number of the switch itself (1..4)
     * @param bStatus Whether to switch on (true) or off (false)
     * @return char[13], representing a tristate code word of length 12
     */
    char[] getCodeWordB(int nAddressCode, int nChannelCode, boolean bStatus) {
        char[] sReturn = new char[13];
        int nReturnPos = 0;

        if (nAddressCode < 1 || nAddressCode > 4 || nChannelCode < 1 || nChannelCode > 4) {
            return null;
        }

        for (int i = 1; i <= 4; i++) {
            sReturn[nReturnPos++] = (nAddressCode == i) ? '0' : 'F';
        }

        for (int i = 1; i <= 4; i++) {
            sReturn[nReturnPos++] = (nChannelCode == i) ? '0' : 'F';
        }

        sReturn[nReturnPos++] = 'F';
        sReturn[nReturnPos++] = 'F';
        sReturn[nReturnPos++] = 'F';

        sReturn[nReturnPos++] = bStatus ? 'F' : '0';

        sReturn[nReturnPos] = '\0';
        return sReturn;
    }


    /**
     * Like getCodeWord (Type C = Intertechno)
     */
    char[] getCodeWordC(char sFamily, int nGroup, int nDevice, boolean bStatus) {
        char[] sReturn = new char[13];
        int nReturnPos = 0;

        int nFamily = (int) sFamily - 'a';
        if (nFamily < 0 || nFamily > 15 || nGroup < 1 || nGroup > 4 || nDevice < 1 || nDevice > 4) {
            return null;
        }

        // encode the family into four bits
        sReturn[nReturnPos++] = (nFamily & 1) != 0 ? 'F' : '0';
        sReturn[nReturnPos++] = (nFamily & 2) != 0 ? 'F' : '0';
        sReturn[nReturnPos++] = (nFamily & 4) != 0 ? 'F' : '0';
        sReturn[nReturnPos++] = (nFamily & 8) != 0 ? 'F' : '0';

        // encode the device and group
        sReturn[nReturnPos++] = ((nDevice - 1) & 1) != 0 ? 'F' : '0';
        sReturn[nReturnPos++] = ((nDevice - 1) & 2) != 0 ? 'F' : '0';
        sReturn[nReturnPos++] = ((nGroup - 1) & 1) != 0 ? 'F' : '0';
        sReturn[nReturnPos++] = ((nGroup - 1) & 2) != 0 ? 'F' : '0';

        // encode the status code
        sReturn[nReturnPos++] = '0';
        sReturn[nReturnPos++] = 'F';
        sReturn[nReturnPos++] = 'F';
        sReturn[nReturnPos++] = bStatus ? 'F' : '0';

        sReturn[nReturnPos] = '\0';
        return sReturn;
    }

    /**
     * Encoding for the REV Switch Type
     * <p>
     * The code word is a tristate word and with following bit pattern:
     * <p>
     * +-----------------------------+-------------------+----------+--------------+
     * | 4 bits address              | 3 bits address    | 3 bits   | 2 bits       |
     * | switch group                | device number     | not used | on / off     |
     * | A=1FFF B=F1FF C=FF1F D=FFF1 | 1=0FF 2=F0F 3=FF0 | 000      | on=10 off=01 |
     * +-----------------------------+-------------------+----------+--------------+
     * <p>
     * Source: http://www.the-intruder.net/funksteckdosen-von-rev-uber-arduino-ansteuern/
     *
     * @param sGroup Name of the switch group (A..D, resp. a..d)
     * @param nDevice Number of the switch itself (1..3)
     * @param bStatus Whether to switch on (true) or off (false)
     * @return char[13], representing a tristate code word of length 12
     */
    char[] getCodeWordD(char sGroup, int nDevice, boolean bStatus) {
        char[] sReturn = new char[13];
        int nReturnPos = 0;

        // sGroup must be one of the letters in "abcdABCD"
        int nGroup = (sGroup >= 'a') ? (int) sGroup - 'a' : (int) sGroup - 'A';
        if (nGroup < 0 || nGroup > 3 || nDevice < 1 || nDevice > 3) {
            return null;
        }

        for (int i = 0; i < 4; i++) {
            sReturn[nReturnPos++] = (nGroup == i) ? '1' : 'F';
        }

        for (int i = 1; i <= 3; i++) {
            sReturn[nReturnPos++] = (nDevice == i) ? '1' : 'F';
        }

        sReturn[nReturnPos++] = '0';
        sReturn[nReturnPos++] = '0';
        sReturn[nReturnPos++] = '0';

        sReturn[nReturnPos++] = bStatus ? '1' : '0';
        sReturn[nReturnPos++] = bStatus ? '0' : '1';

        sReturn[nReturnPos] = '\0';
        return sReturn;
    }

    /**
     * @param sCodeWord a tristate code word consisting of the letter 0, 1, F
     */
    void sendTriState(char[] sCodeWord) {
        // turn the tristate code word into the corresponding bit pattern, then send it
        long code = 0;
        int length = 0;
        for (char p : sCodeWord) {
            code <<= 2L;
            switch (p) {
                case '0':
                    // bit pattern 00
                    break;
                case 'F':
                    // bit pattern 01
                    code |= 1L;
                    break;
                case '1':
                    // bit pattern 11
                    code |= 3L;
                    break;
            }
            length += 2;
        }
        send(code, length);
    }

    /**
     * Transmit the first 'length' bits of the integer 'code'. The
     * bits are sent from MSB to LSB, i.e., first the bit at position length-1,
     * then the bit at position length-2, and so on, till finally the bit at position 0.
     */
    void send(long code, int length) {
        if (TextUtils.isEmpty(transmitterPinName)) return;

        // make sure the receiver is disabled while we transmit
        String nReceiverInterrupt_backup = interruptPinName;
        boolean interruptEnabled = !TextUtils.isEmpty(nReceiverInterrupt_backup);

        if (interruptEnabled) {
            this.disableReceive();
        }

        for (int nRepeat = 0; nRepeat < nRepeatTransmit; nRepeat++) {
            for (int i = length - 1; i >= 0; i--) {
                if ((code & (1L << i)) != 0) this.transmit(protocol.one);
                else this.transmit(protocol.zero);
            }
            this.transmit(protocol.syncFactor);
        }

        // enable receiver again if we just disabled it
        if (interruptEnabled) {
            this.enableReceive(nReceiverInterrupt_backup);
        }
    }

    /**
     * Transmit a single high-low pulse.
     */
    void transmit(HighLow pulses) {
        boolean firstLogicLevel = (this.protocol.invertedSignal) ? false : true;
        boolean secondLogicLevel = (this.protocol.invertedSignal) ? true : false;

        new TransmitterThread(firstLogicLevel, secondLogicLevel,
                transmitter, pulses, protocol).start();
    }

    /**
     * Enable receiving data
     */
    public void enableReceive(String pinName) {
        this.interruptPinName = pinName;
        enableReceive();
    }

    void enableReceive() {
        if (!TextUtils.isEmpty(interruptPinName)) {
            nReceivedValue = 0;
            nReceivedBitlength = 0;

            try {
                interruptReceiver = manager.openGpio(interruptPinName);
                interruptReceiver.setDirection(Gpio.DIRECTION_IN);
                interruptReceiver.setEdgeTriggerType(Gpio.EDGE_BOTH);

                // GPIO callback uses a Handler internally.
                // Spurn a new thread and prepare a looper on it for the Handler.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Looper.prepare();
                            interruptReceiver.registerGpioCallback(interruptCallback);
                            Looper.loop();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Disable receiving data
     */
    void disableReceive() {
        try {
            interruptReceiver.unregisterGpioCallback(interruptCallback);
            interruptReceiver.close();
            interruptReceiver = null;
            interruptPinName = null;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAvailable() {
        return nReceivedValue != 0;
    }

    public void resetAvailable() {
        nReceivedValue = 0;
    }

    public long getReceivedValue() {
        return nReceivedValue;
    }

    public int getReceivedBitlength() {
        return nReceivedBitlength;
    }

    public long getReceivedDelay() {
        return nReceivedDelay;
    }

    public int getReceivedProtocol() {
        return nReceivedProtocol;
    }

    long[] getReceivedRawdata() {
        return timings;
    }

    /* helper function for the receiveProtocol method */
    static long diff(long A, long B) {
        long res = A - B;
        return (res < 0) ? -res : res;
    }

    @Override
    public void close() throws IOException {
        if (transmitter != null) transmitter.close();
        if (interruptReceiver != null) interruptReceiver.close();
    }

    private static class TransmitterThread extends Thread {

        boolean firstLogicLevel;
        boolean secondLogicLevel;

        Gpio transmitter;
        HighLow pulses;
        Protocol protocol;

        public TransmitterThread(boolean firstLogicLevel, boolean secondLogicLevel,
                                 Gpio transmitter, HighLow pulses, Protocol protocol) {
            this.firstLogicLevel = firstLogicLevel;
            this.secondLogicLevel = secondLogicLevel;
            this.transmitter = transmitter;
            this.pulses = pulses;
            this.protocol = protocol;
        }

        @Override
        public void run() {
            super.run();
            try {
                transmitter.setValue(firstLogicLevel);
                sleep(protocol.pulseLength * pulses.high);
                transmitter.setValue(secondLogicLevel);
                sleep(protocol.pulseLength * pulses.low);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static class InterruptCallback extends GpioCallback {

        @Override
        public boolean onGpioEdge(Gpio gpio) {

            long time = System.nanoTime() / 1000;
            long duration = time - lastTime;

            if (duration > nSeparationLimit) {
                // A long stretch without signal level change occurred. This could
                // be the gap between two transmission.
                if (diff(duration, timings[0]) < 200) {
                    // This long signal is close in length to the long signal which
                    // started the previously recorded timings; this suggests that
                    // it may indeed by a a gap between two transmissions (we assume
                    // here that a sender will send the signal multiple times,
                    // with roughly the same gap between them).
                    repeatCount++;
                    if (repeatCount == 2) {
                        for (int i = 1; i <= PROTOCOLS.length; i++) {
                            if (receiveProtocol(i, changeCount)) {
                                // receive succeeded for protocol i
                                break;
                            }
                        }
                        repeatCount = 0;
                    }
                }
                changeCount = 0;
            }

            // detect overflow
            if (changeCount >= RCSWITCH_MAX_CHANGES) {
                changeCount = 0;
                repeatCount = 0;
            }

            timings[changeCount++] = duration;
            lastTime = time;

            return true;
        }

        @Override
        public void onGpioError(Gpio gpio, int error) {
            Log.w(TAG, gpio + ": Error event " + error);
        }

        boolean receiveProtocol(int protocolNumber, int changeCount) {
            Protocol pro = new Protocol(PROTOCOLS[protocolNumber - 1]);

            long code = 0;
            //Assuming the longer pulse length is the pulse captured in timings[0]
            final int syncLengthInPulses = ((pro.syncFactor.low) > (pro.syncFactor.high)) ? (pro.syncFactor.low) : (pro.syncFactor.high);
            final long delay = timings[0] / syncLengthInPulses;
            final long delayTolerance = delay * nReceiveTolerance / 100;

    /* For protocols that start low, the sync period looks like
     *               _________
     * _____________|         |XXXXXXXXXXXX|
     *
     * |--1st dur--|-2nd dur-|-Start data-|
     *
     * The 3rd saved duration starts the data.
     *
     * For protocols that start high, the sync period looks like
     *
     *  ______________
     * |              |____________|XXXXXXXXXXXXX|
     *
     * |-filtered out-|--1st dur--|--Start data--|
     *
     * The 2nd saved duration starts the data
     */
            final int firstDataTiming = (pro.invertedSignal) ? (2) : (1);

            for (int i = firstDataTiming; i < changeCount - 1; i += 2) {
                code <<= 1;
                if (diff(timings[i], delay * pro.zero.high) < delayTolerance &&
                        diff(timings[i + 1], delay * pro.zero.low) < delayTolerance) {
                    // zero
                }
                else if (diff(timings[i], delay * pro.one.high) < delayTolerance &&
                        diff(timings[i + 1], delay * pro.one.low) < delayTolerance) {
                    // one
                    code |= 1;
                }
                else {
                    // Failed
                    /*StringBuilder log = new StringBuilder();
                    log.append("Failed to receive on protocol " + protocolNumber + " in loop ");
                    log.append("Delay: " + delay + " Tolerance: " + delayTolerance);
                    log.append(" Zero high diff: " + diff(timings[i], delay * pro.zero.high) + " Zero low diff: " + diff(timings[i + 1], delay * pro.zero.low));
                    log.append(" One high diff: " + diff(timings[i], delay * pro.one.high) + " One low diff: " + diff(timings[i + 1], delay * pro.one.low));
                    log.append("\n");
                    log.append("\n");

                    Log.d(TAG, log.toString());*/

                    return false;
                }
            }

            if (changeCount > 7) {    // ignore very short transmissions: no device sends them, so this must be noise
                nReceivedValue = code;
                nReceivedBitlength = (changeCount - 1) / 2;
                nReceivedDelay = delay;
                nReceivedProtocol = protocolNumber;

                StringBuilder builder = new StringBuilder();
                builder.append("Received ");
                builder.append(nReceivedValue);
                builder.append(" / ");
                builder.append(nReceivedBitlength);
                builder.append(" bit Protocol: ");
                builder.append(nReceivedProtocol);
                builder.append("\n");
                builder.append("Delay (Pulse Length): ");
                builder.append(nReceivedDelay);

                Log.d(TAG, builder.toString());

                return true;
            }

            return false;
        }
    }
}