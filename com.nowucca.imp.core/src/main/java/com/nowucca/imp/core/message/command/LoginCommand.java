/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.message.command;

import java.util.Arrays;
import java.util.List;

/**
 */
public class LoginCommand implements ImapCommand {

    private CharSequence userId;
    private CharSequence password;

    public LoginCommand(CharSequence userId, CharSequence password) {
        this.userId = userId;
        this.password = password;
    }

    @Override
    public String getCommandName() {
        return "LOGIN";
    }

    @Override
    public List<?> getArguments() {
        return Arrays.asList(userId, password);
    }
}
