package com.tunjid.raspberrynetworkservicediscovery.rc;

/**
 * Protocol class
 * <p>
 * Created by tj.dahunsi on 2/10/17.
 */

public class Protocol {
    /** @brief if true inverts the high and low logic levels in the HighLow structs */
    boolean invertedSignal;
    int pulseLength;

    HighLow syncFactor;
    HighLow zero;
    HighLow one;

    public Protocol(boolean invertedSignal, int pulseLength, HighLow syncFactor, HighLow zero, HighLow one) {
        this.invertedSignal = invertedSignal;
        this.pulseLength = pulseLength;
        this.syncFactor = syncFactor;
        this.zero = zero;
        this.one = one;
    }

    public Protocol(Protocol source) {
        this.invertedSignal = source.invertedSignal;
        this.pulseLength = source.pulseLength;
        this.syncFactor = new HighLow(source.syncFactor.high, source.syncFactor.low);
        this.zero = new HighLow(source.zero.high, source.zero.low);
        this.one = new HighLow(source.one.high, source.one.low);
    }
}
