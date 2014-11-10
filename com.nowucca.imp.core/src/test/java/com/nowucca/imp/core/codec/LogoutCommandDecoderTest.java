/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import org.junit.Test;

public class LogoutCommandDecoderTest extends BaseDecoderTest {


    @Test
    public void shouldParseTaggedLogoutCommandCaseInsensitive() throws Exception {
        writeToChannel("A002 LOGOUT\r\n");
        expectSuccessfulRequest("A002", "LogOuT");
    }

    @Test
    public void shouldParseTaggedLogoutCommand() throws Exception {
        writeToChannel("A003 LOGOUT\r\n");
        expectSuccessfulRequest("A003", "LOGOUT");
    }

    @Test
    public void shouldParseFragmentedCommandName() throws Exception {
        writeToChannel("A006 LOG");
        writeToChannel("OUT\r\n");
        expectSuccessfulRequest("A006", "LOGOUT");
    }

}
