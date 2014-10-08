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

public class ImapRequestDecoderTest extends BaseDecoderTest {

    @Test
    public void shouldNotProduceResultWithUnterminatedCommandString() throws Exception {
        writeToChannel("A001 NOOP");
        final ImapRequest imapRequest = (ImapRequest) channel.readInbound();
        assertNull(imapRequest);
    }

    @Test
    public void shouldParseTaggedNoopCommand() throws Exception {
        writeToChannel("A001 NOOP\r\n");
        expectSuccessfulRequest("A001", "NOOP");
    }

    @Test
    public void shouldParseTaggedCapabilityCommand() throws Exception {
        writeToChannel("A002 CAPABILITY\r\n");
        expectSuccessfulRequest("A002", "CAPABILITY");
    }

    @Test
    public void shouldParseTaggedCapabilityCommandCaseInsensitive() throws Exception {
        writeToChannel("A002 CAPABILITY\r\n");
        expectSuccessfulRequest("A002", "CaPaBiLiTy");
    }

    @Test
    public void shouldParseTaggedLogoutCommand() throws Exception {
        writeToChannel("A003 LOGOUT\r\n");
        expectSuccessfulRequest("A003", "LOGOUT");
    }

    @Test
    public void shouldSeeTooLongFrameExceptionWhenTagTooLong() throws Exception {
        final StringBuilder longTag = new StringBuilder(ImapCodecConstants.MAXIMUM_TAG_LENGTH + 1);
        for (int i = 0; i < ImapCodecConstants.MAXIMUM_TAG_LENGTH + 1; i++) {
            longTag.append('A');
        }
        writeToChannel(longTag.toString() + "  NOOP\r\n");
        expectInvalidRequest(TooLongFrameException.class);
    }

    @Test
    public void shouldParseFragmentedCommandName() throws Exception {
        writeToChannel("A006 NO");
        writeToChannel("OP\r\n");
        expectSuccessfulRequest("A006", "NOOP");
    }


    @Test
    public void shouldSeeUnsupportedOperationExceptionWhenPresentingXtendedCommand() throws Exception {
        writeToChannel("A005 XCOMMAND\r\n");
        expectInvalidRequest(UnsupportedOperationException.class);
    }


    @Test
    public void shouldSeeIllegalArgumentExceptionWhenPresentingUnexpectedCommandWithValidPrefix() throws Exception {
        writeToChannel("A006 NOTNOOP\r\n");
        expectInvalidRequest(IllegalArgumentException.class);
    }

    @Test
    public void shouldSeeIllegalArgumentExceptionWhenPresentingUnexpectedNonCRLFTerminator() throws Exception {
        writeToChannel("A006 NOOPZZ");
        expectInvalidRequest(IllegalArgumentException.class);
    }


}
