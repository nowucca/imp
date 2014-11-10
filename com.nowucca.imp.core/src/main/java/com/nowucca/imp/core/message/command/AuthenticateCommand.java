/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.message.command;

import java.util.Arrays;
import java.util.List;

/**
 */
public class AuthenticateCommand implements ImapCommand {

    private CharSequence authenticationMechanismName;

    public AuthenticateCommand(CharSequence authenticationMechanismName) {
        this.authenticationMechanismName = authenticationMechanismName;
    }

    @Override
    public Kind getKind() {
        return Kind.AUTHENTICATE;
    }

    @Override
    public String getCommandName() {
        return "AUTHENTICATE";
    }

    @Override
    public List<?> getArguments() {
        return Arrays.asList(authenticationMechanismName);
    }

    public CharSequence getAuthenticationMechanismName() {
        return authenticationMechanismName;
    }
}
