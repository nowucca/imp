/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.AuthenticateCommand;
import com.nowucca.imp.core.message.command.ImapCommand;
import com.nowucca.imp.core.message.command.ImapRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.core.util.Charsets;
import org.junit.Test;

/**
 */
public class AuthenticateCommandEncoderTest extends BaseEncoderTest {

    @Test
    public void shouldEncodeCommand() throws Exception {

        final AuthenticateCommand command = new AuthenticateCommand("MECHNAME");

        final ImapRequest imapRequest = new ImapRequest() {
            @Override
            public ImapCommand getCommand() {
                return command;
            }

            @Override
            public String getTag() {
                return "A001";
            }

        };
        channel.writeOutbound(imapRequest);
        final ByteBuf buffer =  (ByteBuf) readFromChannel();

        final ByteBuf expected = Unpooled.wrappedBuffer("A001 AUTHENTICATE MECHNAME\r\n".getBytes(Charsets.US_ASCII));

        assertEquals(expected, buffer);

    }

}
