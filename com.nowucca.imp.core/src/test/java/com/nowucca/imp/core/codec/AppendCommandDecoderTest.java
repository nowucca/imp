/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.ImapRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.TooLongFrameException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.mail.Flags;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class AppendCommandDecoderTest extends BaseDecoderTest {


    @Test
    public void shouldParseSimpleAppendCommand() throws Exception {
        writeToChannel("A010 APPEND saved-messages (\\Seen) {310}\r\n");

        final ByteBuf expected = Unpooled.wrappedBuffer(ImapRequestDecoder.CONTINUATION_BYTES);
        final ByteBuf buffer = (ByteBuf) channel.readOutbound();
        assertByteBufsEqual(expected, buffer);

        writeToChannel("Date: Mon, 7 Feb 1994 21:52:25 -0800 (PST)\r\n");
        writeToChannel("From: Fred Foobar <foobar@Blurdybloop.COM>\r\n");
        writeToChannel("Subject: afternoon meeting\r\n");
        writeToChannel("To: mooch@owatagu.siam.edu\r\n");
        writeToChannel("Message-Id: <B27397-0100000@Blurdybloop.COM>\r\n");
        writeToChannel("MIME-Version: 1.0\r\n");
        writeToChannel("Content-Type: TEXT/PLAIN; CHARSET=US-ASCII\r\n");
        writeToChannel("\r\n");
        writeToChannel("Hello Joe, do you think we can meet at 3:30 tomorrow?\r\n");
        writeToChannel("\r\n");

        final ImapRequest appendRequest = expectSuccessfulRequest("A010", "APPEND");
        final List<?> arguments = appendRequest.getCommand().getArguments();
        assertEquals(4, arguments.size());


        assertThat(arguments.get(0), instanceOf(String.class));
        assertThat("saved-messages", equalTo(arguments.get(0)));

        assertThat(arguments.get(1), instanceOf(Flags.class));
        final Flags flags = (Flags) arguments.get(1);
        assertTrue(flags.contains(Flags.Flag.SEEN));
        assertEquals(1, flags.getSystemFlags().length);
        assertEquals(0, flags.getUserFlags().length);

        // no date-time
        assertNull(arguments.get(2));

        assertThat(arguments.get(3), instanceOf(ByteBuf.class));
        assertEquals(310, ((ByteBuf) arguments.get(3)).readableBytes());
    }

    @Test
    public void shouldParseComplexAppendCommand() throws Exception {
        writeToChannel("A010 APPEND saved-messages (\\Seen \\Answered) \"11-MAY-1972 12:50:01 +0800\" {44}\r\n");

        final ByteBuf expected = Unpooled.wrappedBuffer(ImapRequestDecoder.CONTINUATION_BYTES);
        final ByteBuf buffer = (ByteBuf) channel.readOutbound();
        assertByteBufsEqual(expected, buffer);

        writeToChannel("Date: Mon, 7 Feb 1994 21:52:25 -0800 (PST)\r\n");
        writeToChannel("\r\n");

        final ImapRequest appendRequest = expectSuccessfulRequest("A010", "APPEND");
        final List<?> arguments = appendRequest.getCommand().getArguments();
        assertEquals(4, arguments.size());


        assertThat(arguments.get(0), instanceOf(String.class));
        assertThat("saved-messages", equalTo(arguments.get(0)));

        assertThat(arguments.get(1), instanceOf(Flags.class));
        final Flags flags = (Flags) arguments.get(1);
        assertTrue(flags.contains(Flags.Flag.SEEN));
        assertTrue(flags.contains(Flags.Flag.ANSWERED));
        assertEquals(2, flags.getSystemFlags().length);
        assertEquals(0, flags.getUserFlags().length);

        // no date-time
        assertThat(arguments.get(2), instanceOf(Date.class));
        final Date dateTime = (Date) arguments.get(2);
        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        calendar.setTime(dateTime);
        assertEquals(11, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.MAY, calendar.get(Calendar.MONTH));
        assertEquals(1972, calendar.get(Calendar.YEAR));

        assertThat(arguments.get(3), instanceOf(ByteBuf.class));
        assertEquals(44, ((ByteBuf) arguments.get(3)).readableBytes());
    }


}