/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.message.command;

import java.util.List;

/**
 */
public class NoopCommand implements ImapCommand {
    @Override
    public String getCommandName() {
        return "NOOP";
    }

    @Override
    public List<String> getArguments() {
        return null;
    }
}
