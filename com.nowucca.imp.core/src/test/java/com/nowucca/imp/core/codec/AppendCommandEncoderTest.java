/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.AppendCommand;
import com.nowucca.imp.core.message.command.ImapCommand;
import com.nowucca.imp.core.message.command.ImapRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import javax.mail.Flags;
import org.apache.logging.log4j.core.util.Charsets;
import org.junit.Test;

/**
 */
public class AppendCommandEncoderTest extends BaseEncoderTest {

    @Test
    public void shouldEncodeAppendCommand() throws Exception {
        final String mailboxName = "trash";
        final Flags flags = new Flags("SENT");
        final ZonedDateTime zonedDateTime = ZonedDateTime.of(1972, 5, 11, 0, 50, 3, 0,
                ZoneId.ofOffset("GMT", ZoneOffset.ofHours(10)));
        final ByteBuf literal = Unpooled.wrappedBuffer("Hello world".getBytes(Charsets.US_ASCII));
        final AppendCommand appendCommand = new AppendCommand(mailboxName, flags, zonedDateTime, literal);

        final ImapRequest imapRequest = new ImapRequest() {
            @Override
            public ImapCommand getCommand() {
                return appendCommand;
            }

            @Override
            public String getTag() {
                return "A001";
            }

        };
        channel.writeOutbound(imapRequest);
        final ByteBuf buffer =  (ByteBuf) readFromChannel();

        final ByteBuf expected = Unpooled.wrappedBuffer(("A001 APPEND trash (SENT) \"11-May-1972 00:50:03 +1000\" " +
                "{11}\r\nHello world\r\n").getBytes(Charsets.US_ASCII));

        assertEquals(expected, buffer);

    }

}
