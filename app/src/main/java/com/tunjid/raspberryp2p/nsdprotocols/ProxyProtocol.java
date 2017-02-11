package com.tunjid.raspberryp2p.nsdprotocols;

/**
 * A protocol that proxies requests to aanother {@link CommsProtocol}of a user's choosing
 * <p>
 * Created by tj.dahunsi on 2/11/17.
 */

public class ProxyProtocol implements CommsProtocol {

    private static final String CHOOSER = "choose";
    private static final String KNOCK_KNOCK = "1";
    private static final String RC = "2";

    private boolean choosing;

    private CommsProtocol commsProtocol;

    public ProxyProtocol() {
    }

    @Override
    public String processInput(String input) {

        // First connection
        if (input == null || input.equals(CHOOSER)) {
            choosing = true;
            return "Please choose the server you want, 1 for Knock Knock jokes, 2 for an RCSniffer";
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
                    return "Invalid selection. Choose 1 for Knock Knock jokes, 2 for an RCSniffer";
            }
            choosing = false;

            String result = "Chose Protocol: " + commsProtocol.getClass().getSimpleName();
            result += "\n";
            result += commsProtocol.processInput(null);

            return result;
        }
        return commsProtocol.processInput(input);
    }
}
