/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.ImapRequest;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LoginCommandDecoderTest extends BaseDecoderTest {

    @Test
    public void shouldNotProduceResultWithUnterminatedCommandString() throws Exception {
        writeToChannel("A001 LOGIN USERID");
        final ImapRequest imapRequest = (ImapRequest) channel.readInbound();
        assertNull(imapRequest);
    }


    @Test
    public void shouldParseTaggedCommand() throws Exception {
        writeToChannel("A001 LOGIN USERID PASSWORD\r\n");
        final ImapRequest request = expectSuccessfulRequest("A001", "LOGIN");
        assertEquals("USERID", request.getCommand().getArguments().get(0).toString());
        assertEquals("PASSWORD", request.getCommand().getArguments().get(1).toString());

    }

    @Test
    public void shouldParseQuotedArguments() throws Exception {
        writeToChannel("A001 LOGIN \"USERID\" \"PASSWORD\"\r\n");
        final ImapRequest request = expectSuccessfulRequest("A001", "LOGIN");
        assertEquals("USERID", request.getCommand().getArguments().get(0).toString());
        assertEquals("PASSWORD", request.getCommand().getArguments().get(1).toString());

    }

    @Test
    public void shouldParseLiteralArguments() throws Exception {
        writeToChannel("A001 LOGIN {6}\r\n");
        writeToChannel("USERID {8}\r\n");
        writeToChannel("PASSWORD");
        writeToChannel("\r\n");
        final ImapRequest request = expectSuccessfulRequest("A001", "LOGIN");
        assertEquals("USERID", request.getCommand().getArguments().get(0).toString());
        assertEquals("PASSWORD", request.getCommand().getArguments().get(1).toString());
    }

    @Test
    public void shouldSeeIllegalArgumentExceptionWhenPresentingInvalidUserId() throws Exception {
        writeToChannel("A001 LOGIN USER(NAME PASSWORD\r\n");
        expectInvalidRequest(IllegalArgumentException.class);
    }

    @Test
    public void shouldSeeIllegalArgumentExceptionWhenPresentingInvalidPassword() throws Exception {
        writeToChannel("A001 LOGIN USER(NAME PASSWORD\r\n");
        expectInvalidRequest(IllegalArgumentException.class);
    }

    @Test
    public void shouldParseFragmentedCommandName() throws Exception {
        writeToChannel("A006 LOG");
        writeToChannel("IN USERNAME PASSWORD\r\n");
        expectSuccessfulRequest("A006", "LOGIN");
    }


}
