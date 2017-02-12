package com.tunjid.raspberryp2p.nsdprotocols;

import android.text.TextUtils;

import com.tunjid.raspberryp2p.rc.RCSwitch;

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
    public Data processInput(String input) {
        Data output = new Data();

        if (input != null) {
            switch (input) {
                case ENABLE_SNIFFER:
                    rcSwitch.enableReceive(INTERRUPT_PIN);
                    output.response = "Sniffer Enabled";
                    output.commands.add("sniff");

                    return output;
                case SNIFF:
                    StringBuilder builder = new StringBuilder();

                    if (rcSwitch.isAvailable() && rcSwitch.getReceivedValue() != 0) {

                        builder.append("Received ");
                        builder.append(rcSwitch.getReceivedValue());
                        builder.append(" / ");
                        builder.append(rcSwitch.getReceivedBitlength());
                        builder.append("bit ");
                        builder.append("Protocol: ");
                        builder.append("\n");
                        builder.append(rcSwitch.getReceivedProtocol());
                        builder.append("Delay (Pulse Length): ");
                        builder.append("\n");
                        builder.append(rcSwitch.getReceivedDelay());
                    }
                    else if (rcSwitch.isAvailable() && rcSwitch.getReceivedValue() == 0) {
                        builder.append("Unkown Encoding");
                    }
                    output.response = builder.toString();
                    output.commands.add("sniff");
                    rcSwitch.resetAvailable();

                    break;
            }
        }
        if (TextUtils.isEmpty(output.response)) output.response = "¯\\_(ツ)_/¯";
        return output;
    }
}
