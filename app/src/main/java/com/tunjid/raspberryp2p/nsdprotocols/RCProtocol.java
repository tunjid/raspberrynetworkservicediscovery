package com.tunjid.raspberryp2p.nsdprotocols;

import android.text.TextUtils;

import com.tunjid.rc.RCSwitch;

/**
 * A protocol for communicating with RF 433 MhZ devices
 * <p>
 * Created by tj.dahunsi on 2/11/17.
 */

public class RCProtocol implements CommsProtocol {

    public static final String ENABLE_SNIFFER = "enable sniffer";
    public static final String SNIFF = "sniff";

    private static final String INTERRUPT_PIN = "BCM21";
    private static final String TRANSMITTER_PIN = "BCM21";

    RCSwitch rcSwitch = new RCSwitch();

    public RCProtocol() {
    }

    @Override
    public String processInput(String input) {
        StringBuilder output = new StringBuilder();

        if (input != null) {
            switch (input) {
                case ENABLE_SNIFFER:
                    rcSwitch.enableReceive(INTERRUPT_PIN);
                    return "Sniffer Enabled";
                case SNIFF:
                    if (rcSwitch.isAvailable() && rcSwitch.getReceivedValue() != 0) {
                        output.append("Received ");
                        output.append(rcSwitch.getReceivedValue());
                        output.append(" / ");
                        output.append(rcSwitch.getReceivedBitlength());
                        output.append("bit ");
                        output.append("Protocol: ");
                        output.append("\n");
                        output.append(rcSwitch.getReceivedProtocol());
                        output.append("Delay (Pulse Length): ");
                        output.append("\n");
                        output.append(rcSwitch.getReceivedDelay());
                    }
                    else if (rcSwitch.isAvailable() && rcSwitch.getReceivedValue() == 0) {
                        output.append("Unkown Encoding");
                    }
                    rcSwitch.resetAvailable();

                    break;
            }
        }
        return TextUtils.isEmpty(output) ? "Shrug" : output.toString();
    }
}
