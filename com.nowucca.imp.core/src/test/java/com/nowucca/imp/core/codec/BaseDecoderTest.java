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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 */
public class BaseDecoderTest {

    ImapRequestDecoder decoder;
    EmbeddedChannel channel;

    @Before
    public void setUp() throws Exception {
        channel = new EmbeddedChannel(decoder = new ImapRequestDecoder());
    }

    @After
    public void tearDown() throws Exception {
        channel.finish();
    }

    protected void assertByteBufsEqual(ByteBuf expected, ByteBuf buffer) {
        Assert.assertTrue(format("Expected bytes '%s'\nreceived:\n               '%s'",
                        ByteBufUtil.hexDump(expected), ByteBufUtil.hexDump(buffer)),
                ByteBufUtil.equals(expected, buffer));
    }

    protected void writeToChannel(String input) {
        final ByteBuf buf = directBuffer(1000);
        buf.writeBytes(input.getBytes(UTF8.charset()));
        channel.writeInbound(buf);
    }

    protected ImapRequest expectSuccessfulRequest(String tag, String commandName) {
        final ImapRequest imapRequest = (ImapRequest) channel.readInbound();
        assertNotNull(imapRequest);
        assertEquals(tag, imapRequest.getTag());
        assertNotNull(imapRequest.getCommand());
        assertEquals(commandName.toUpperCase(), imapRequest.getCommand().getCommandName());
        return imapRequest;
    }

    protected void expectInvalidRequest(Class<?> exceptionClass) {
        final ImapRequest imapRequest = (ImapRequest) channel.readInbound();
        assertNotNull(imapRequest);
        assertThat(imapRequest, instanceOf(InvalidImapRequest.class));
        final InvalidImapRequest invalidRequest = (InvalidImapRequest) imapRequest;
        final DecoderResult result = invalidRequest.getDecoderResult();
        assertThat(result, notNullValue());
        assertThat(result.cause(), instanceOf(exceptionClass));
    }
}
