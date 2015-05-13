/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.CapabilityCommand;
import com.nowucca.imp.core.message.command.ImapCommand;
import com.nowucca.imp.core.message.command.ImapRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.core.util.Charsets;
import org.junit.Test;

/**
 */
public class CapabilityCommandEncoderTest extends BaseEncoderTest {

    @Test
    public void shouldEncodeCommand() throws Exception {

        channel.writeOutbound(new ImapRequest() {
            @Override
            public ImapCommand getCommand() {
                return new CapabilityCommand();
            }
            @Override
            public String getTag() {
                return "A001";
            }
        });

        final ByteBuf expected = Unpooled.wrappedBuffer("A001 CAPABILITY\r\n".getBytes(Charsets.US_ASCII));

        assertEquals(expected, (ByteBuf) readFromChannel());

    }

}

