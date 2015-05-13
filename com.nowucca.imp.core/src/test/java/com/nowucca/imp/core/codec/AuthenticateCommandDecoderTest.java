/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.ImapRequest;
import org.junit.Test;
import static org.junit.Assert.*;

public class AuthenticateCommandDecoderTest extends BaseDecoderTest {

    @Test
    public void shouldNotProduceResultWithUnterminatedCommandString() throws Exception {
        writeToChannel("A001 AUTHENTICATE SAML20");
        final ImapRequest imapRequest = (ImapRequest) channel.readInbound();
        assertNull(imapRequest);
    }


    @Test
    public void shouldParseTaggedCommand() throws Exception {
        writeToChannel("A001 AUTHENTICATE SAML20\r\n");
        final ImapRequest request = expectSuccessfulRequest("A001", "AUTHENTICATE");
        assertEquals("SAML20", request.getCommand().getArguments().get(0).toString());
    }

    @Test
    public void shouldSeeIllegalArgumentExceptionWhenPresentingInvalidMechanismName() throws Exception {
        writeToChannel("A001 AUTHENTICATE GS2-*\r\n");
        expectInvalidRequest(IllegalArgumentException.class);
    }

    @Test
    public void shouldParseFragmentedCommandName() throws Exception {
        writeToChannel("A006 START");
        writeToChannel("TLS\r\n");
        expectSuccessfulRequest("A006", "STARTTLS");
    }

    @Test
    public void shouldSeeIllegalArgumentExceptionWhenPresentingUnexpectedNonCRLFTerminator() throws Exception {
        writeToChannel("A006 STARTTLSZZ");
        expectInvalidRequest(IllegalArgumentException.class);
    }

}
