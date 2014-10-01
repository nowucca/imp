/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.ImapRequest;
import com.nowucca.imp.core.message.command.InvalidImapRequest;
import com.nowucca.imp.util.UTF8;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.TooLongFrameException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static io.netty.buffer.Unpooled.directBuffer;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class ImapRequestDecoderTest {

    ImapRequestDecoder decoder;
    EmbeddedChannel channel;

    @Before
    public void setUp() throws Exception {
        channel = new EmbeddedChannel(decoder = new ImapRequestDecoder());
    }

    @After
    public void tearDown() throws Exception { channel.finish(); }

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


    private void writeToChannel(String input) {
        final ByteBuf buf = directBuffer(1000);
        buf.writeBytes(input.getBytes(UTF8.charset()));
        channel.writeInbound(buf);
    }

    private void expectSuccessfulRequest(String tag, String commandName) {
        final ImapRequest imapRequest = (ImapRequest) channel.readInbound();
        assertNotNull(imapRequest);
        assertEquals(tag, imapRequest.getTag());
        assertNotNull(imapRequest.getCommand());
        assertEquals(commandName, imapRequest.getCommand().getCommandName());
    }

    private void expectInvalidRequest(Class<?> exceptionClass) {
        final ImapRequest imapRequest = (ImapRequest) channel.readInbound();
        assertNotNull(imapRequest);
        assertThat(imapRequest, instanceOf(InvalidImapRequest.class));
        final InvalidImapRequest invalidRequest = (InvalidImapRequest) imapRequest;
        final DecoderResult result = invalidRequest.getDecoderResult();
        assertThat(result, notNullValue());
        assertThat(result.cause(), instanceOf(exceptionClass));
    }

}
