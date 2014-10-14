/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.ImapRequest;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SelectCommandDecoderTest extends BaseDecoderTest {

    @Test
    public void shouldNotProduceResultWithUnterminatedCommandString() throws Exception {
        writeToChannel("A001 SELECT INBOX");
        final ImapRequest imapRequest = (ImapRequest) channel.readInbound();
        assertNull(imapRequest);
    }


    @Test
    public void shouldParseTaggedCommand() throws Exception {
        writeToChannel("A001 SELECT INBOX\r\n");
        final ImapRequest request = expectSuccessfulRequest("A001", "SELECT");
        assertEquals("INBOX", request.getCommand().getArguments().get(0).toString());
    }

    @Test
    public void shouldParseQuotedArguments() throws Exception {
        writeToChannel("A001 SELECT \"INBOX\"\r\n");
        final ImapRequest request = expectSuccessfulRequest("A001", "SELECT");
        assertEquals("INBOX", request.getCommand().getArguments().get(0).toString());
    }

    @Test
    public void shouldParseLiteralArguments() throws Exception {
        writeToChannel("A001 SELECT {5}\r\n");
        writeToChannel("INBOX\r\n");
        final ImapRequest request = expectSuccessfulRequest("A001", "SELECT");
        assertEquals("INBOX", request.getCommand().getArguments().get(0).toString());
    }

    @Test
    public void shouldSeeIllegalArgumentExceptionWhenPresentingInvalidMailboxName() throws Exception {
        writeToChannel("A001 SELECT MAILBOX(NAME\r\n");
        expectInvalidRequest(IllegalArgumentException.class);
    }


    @Test
    public void shouldParseFragmentedCommandName() throws Exception {
        writeToChannel("A006 SEL");
        writeToChannel("ECT MAILBOXNAME\r\n");
        expectSuccessfulRequest("A006", "SELECT");
    }


}
