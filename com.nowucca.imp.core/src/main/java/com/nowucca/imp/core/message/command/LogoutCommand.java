/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.message.command;

import java.util.List;

/**
 */
public class LogoutCommand implements ImapCommand {

    @Override
    public Kind getKind() {
        return Kind.LOGOUT;
    }

    @Override
    public String getCommandName() {
        return "LOGOUT";
    }

    @Override
    public List<String> getArguments() {
        return null;
    }
}
