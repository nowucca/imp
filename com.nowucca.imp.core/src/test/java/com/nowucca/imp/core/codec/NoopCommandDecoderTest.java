/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.ImapRequest;
import org.junit.Test;
import static org.junit.Assert.assertNull;

public class NoopCommandDecoderTest extends BaseDecoderTest {

    @Test
    public void shouldNotProduceResultWithUnterminatedCommandString() throws Exception {
        writeToChannel("A001 NOOP");
        final ImapRequest imapRequest = channel.readInbound();
        assertNull(imapRequest);
    }

    @Test
    public void shouldParseTaggedNoopCommand() throws Exception {
        writeToChannel("A001 NOOP\r\n");
        expectSuccessfulRequest("A001", "NOOP");
    }

    @Test
    public void shouldParseTaggedNoopCommandCaseInsensitive() throws Exception {
        writeToChannel("A002 NOOP\r\n");
        expectSuccessfulRequest("A002", "Noop");
    }

}
