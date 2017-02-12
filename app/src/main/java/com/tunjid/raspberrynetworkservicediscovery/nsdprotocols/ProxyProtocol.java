package com.tunjid.raspberrynetworkservicediscovery.nsdprotocols;

import java.io.IOException;

/**
 * A protocol that proxies requests to aanother {@link CommsProtocol}of a user's choosing
 * <p>
 * Created by tj.dahunsi on 2/11/17.
 */

public class ProxyProtocol implements CommsProtocol {

    private static final String CHOOSER = "choose";
    private static final String KNOCK_KNOCK = "Knock Knock Jokes";
    private static final String RC = "RC Sniffer";

    private boolean choosing;

    private CommsProtocol commsProtocol;

    public ProxyProtocol() {
    }

    @Override
    public Data processInput(String input) {
        Data output = new Data();

        // First connection
        if (input == null || input.equals(CHOOSER)) {
            choosing = true;
            output.response = "Please choose the server you want, Knock Knock jokes, or an RCSniffer";
            output.commands.add(KNOCK_KNOCK);
            output.commands.add(RC);
            return output;
        }

        if (choosing) {
            switch (input) {
                case KNOCK_KNOCK:
                    commsProtocol = new KnockKnockProtocol();
                    break;
                case RC:
                    commsProtocol = new RCProtocol();
                    break;
                default:
                    output.response = "Invalid command. Please choose the server you want, Knock Knock jokes, or an RCSniffer";
                    output.commands.add(KNOCK_KNOCK);
                    output.commands.add(RC);
                    return output;
            }
            choosing = false;

            String result = "Chose Protocol: " + commsProtocol.getClass().getSimpleName();
            result += "\n";
            result += "\n";

            output = commsProtocol.processInput(null);
            output.response = result + output.response;

            return output;
        }
        return commsProtocol.processInput(input);
    }

    @Override
    public void close() throws IOException {
        if (commsProtocol != null) commsProtocol.close();
    }
}
