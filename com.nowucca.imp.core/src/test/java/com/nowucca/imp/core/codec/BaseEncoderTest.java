/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

/**
 */
public class BaseEncoderTest {

    ImapRequestEncoder encoder;
    EmbeddedChannel channel;

    @Before
    public void setUp() throws Exception {
        channel = new EmbeddedChannel(encoder = new ImapRequestEncoder());
    }

    @After
    public void tearDown() throws Exception { channel.finish(); }

    protected Object readFromChannel() {
        return channel.readOutbound();
    }

    protected void assertEquals(ByteBuf expected, ByteBuf actual) {
        assertTrue(format("\nExpected bytes '%s'\nreceived:\n               '%s'",
                        ByteBufUtil.hexDump(expected), ByteBufUtil.hexDump(actual)),
                ByteBufUtil.equals(expected, actual));
    }
}
