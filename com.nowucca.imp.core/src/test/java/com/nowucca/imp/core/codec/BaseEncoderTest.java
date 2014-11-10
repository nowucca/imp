/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.ImapRequest;
import com.nowucca.imp.core.message.command.InvalidImapRequest;
import com.nowucca.imp.util.UTF8;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import static io.netty.buffer.Unpooled.directBuffer;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

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
        assertTrue(format("Expected bytes '%s'\nreceived:\n               '%s'",
                        ByteBufUtil.hexDump(expected), ByteBufUtil.hexDump(actual)),
                ByteBufUtil.equals(expected, actual));
    }
}
