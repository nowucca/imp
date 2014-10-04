/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.message.command;

import java.util.List;

/**
 */
public class StartTlsCommand implements ImapCommand {
    @Override
    public String getCommandName() {
        return "STARTTLS";
    }

    @Override
    public List<String> getArguments() {
        return null;
    }
}
