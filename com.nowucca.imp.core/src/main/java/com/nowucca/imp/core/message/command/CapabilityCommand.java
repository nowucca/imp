/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.message.command;

import java.util.List;

/**
 */
public class CapabilityCommand implements ImapCommand {

    @Override
    public Kind getKind() {
        return Kind.CAPABILITY;
    }

    @Override
    public String getCommandName() {
        return "CAPABILITY";
    }

    @Override
    public List<String> getArguments() {
        return null;
    }
}
