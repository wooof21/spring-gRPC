package com.exmaple.timeout;

import com.exmaple.common.AbstractChannelTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * eager channel creation
 *
 * channel.getState(false) -> returns IDLE
 * channel.getState(true)  -> returns CONNECTING and the channel is created eagerly
 */
public class EagerChannelTest extends AbstractChannelTest {

    private static final Logger log = LoggerFactory.getLogger(EagerChannelTest.class);

    @Test
    public void eagerChannelCreation() {
        log.info("{}", channel.getState(true));
    }

}
