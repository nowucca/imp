/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.beetstra.jutf7.CharsetProvider;
import com.nowucca.imp.core.message.command.AppendCommand;
import com.nowucca.imp.core.message.command.AuthenticateCommand;
import com.nowucca.imp.core.message.command.CapabilityCommand;
import com.nowucca.imp.core.message.command.ImapCommand;
import com.nowucca.imp.core.message.command.ImapRequest;
import com.nowucca.imp.core.message.command.LoginCommand;
import com.nowucca.imp.util.ModifiedUTF7;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.mail.Flags;
import static com.nowucca.imp.core.codec.ImapCharacterConstants.*;
import static java.lang.String.format;

/**
 */
public class ImapRequestEncoder extends MessageToByteEncoder<ImapRequest> {

    private static final Charset US_ASCII = Charset.forName("US-ASCII");
    private static final String X_MODIFIED_UTF_7 = "X-MODIFIED-UTF-7";
    private static final Charset X_MODIFIED_UTF_7_CHARSET = new CharsetProvider().charsetForName(X_MODIFIED_UTF_7);

    @Override
    protected void encode(ChannelHandlerContext ctx, ImapRequest msg, ByteBuf out) throws Exception {
        if (msg != null) {
            // tag
            out.writeBytes(msg.getTag().getBytes(US_ASCII));
            out.writeByte(' ');

            // command
            final ImapCommand command = msg.getCommand();

            switch(command.getKind()) {
                case APPEND: {
                    final AppendCommand appendCommand = (AppendCommand) command;
                    encodeCommandName(out, appendCommand);
                    encodeSpace(out);
                    encodeMailboxName(appendCommand.getMailboxName(), out);
                    if (appendCommand.getFlags() != null) {
                        encodeSpace(out);
                        encodeFlags(appendCommand.getFlags(), out);
                    }
                    if (appendCommand.getDateTime() != null) {
                        encodeSpace(out);
                        encodeDateTime(appendCommand.getDateTime(), out);
                    }
                    encodeSpace(out);
                    encodeLiteral(appendCommand.getMessageLiteral(), out);
                    encodeCRLF(out);
                    break;
                }

                case AUTHENTICATE: {
                    final AuthenticateCommand authenticateCommand = (AuthenticateCommand) command;
                    encodeCommandName(out, authenticateCommand);
                    encodeSpace(out);
                    encodeAuthenticationMechanismName(out, authenticateCommand);
                    encodeCRLF(out);
                    break;
                }

                case CAPABILITY: {
                    final CapabilityCommand capabilityCommand = (CapabilityCommand) command;
                    encodeCommandName(out, capabilityCommand);
                    encodeCRLF(out);
                    break;
                }

                case LOGIN: {
                    final LoginCommand loginCommand = (LoginCommand) command;
                    encodeCommandName(out, loginCommand);

                }

                default: {
                    throw new UnsupportedOperationException(format("No support for %s command.", command.getKind()));
                }

            }
        }
    }

    private void encodeAuthenticationMechanismName(ByteBuf out, AuthenticateCommand authenticateCommand) {
        out.writeBytes(authenticateCommand.getAuthenticationMechanismName().toString().getBytes(US_ASCII));
    }


    private void encodeLiteral(ByteBuf messageLiteral, ByteBuf out) {
        out.writeByte('{');
        out.writeBytes(String.valueOf(messageLiteral.readableBytes()).getBytes());
        out.writeByte('}');
        out.writeByte(CR);
        out.writeByte(LF);
        out.writeBytes(messageLiteral);
    }

    private static final DateTimeFormatter RFC_3501_DATE_TIME =
            DateTimeFormatter.ofPattern("\"dd-MMM-yyyy HH:mm:ss xxxx\"", Locale.US);

    private void encodeDateTime(ZonedDateTime dateTime, ByteBuf out) {
        out.writeBytes(dateTime.format(RFC_3501_DATE_TIME).getBytes(US_ASCII));
    }

    private void encodeFlags(Flags flags, ByteBuf out) {
        out.writeByte('(');

        boolean prependSpace = false;

        if (flags.contains(Flags.Flag.ANSWERED)) {
            out.writeBytes(ImapCodecConstants.ANSWERED_ALL_CAPS_BYTES);
            prependSpace = true;
        }
        if (flags.contains(Flags.Flag.SEEN)) {
            if (prependSpace) {
                out.writeByte(SP);
            }
            out.writeBytes(ImapCodecConstants.SEEN_ALL_CAPS_BYTES);
            prependSpace = true;
        }
        if (flags.contains(Flags.Flag.DELETED)) {
            if (prependSpace) {
                out.writeByte(SP);
            }
            out.writeBytes(ImapCodecConstants.DELETED_ALL_CAPS_BYTES);
            prependSpace = true;
        }
        if (flags.contains(Flags.Flag.DRAFT)) {
            if (prependSpace) {
                out.writeByte(SP);
            }
            out.writeBytes(ImapCodecConstants.DRAFT_ALL_CAPS_BYTES);
            prependSpace = true;
        }
        if (flags.contains(Flags.Flag.RECENT)) {
            if (prependSpace) {
                out.writeByte(SP);
            }
            out.writeBytes(ImapCodecConstants.RECENT_ALL_CAPS_BYTES);
            prependSpace = true;
        }
        if (flags.contains(Flags.Flag.FLAGGED)) {
            if (prependSpace) {
                out.writeByte(SP);
            }
            out.writeBytes(ImapCodecConstants.FLAGGED_ALL_CAPS_BYTES);
            prependSpace = true;
        }

        final String[] userFlags = flags.getUserFlags();

        for (String userFlag: userFlags) {
            if (prependSpace) {
                out.writeByte(SP);
            }
            out.writeBytes(userFlag.getBytes(US_ASCII));
            prependSpace = true;
        }

        out.writeByte(')');
    }


    //TODO: write this out quoted, literal or as an atom?
    private void encodeMailboxName(String mailboxName, ByteBuf out) {
        if (ImapCodecConstants.INBOX.equalsIgnoreCase(mailboxName)) {
            out.writeBytes("INBOX".getBytes());
        } else {
            out.writeBytes(ModifiedUTF7.encode(mailboxName).getBytes(X_MODIFIED_UTF_7_CHARSET));
        }
    }

    private void encodeSpace(ByteBuf out) {
        out.writeByte(SP);
    }

    private void encodeCRLF(ByteBuf out) {
        out.writeByte(CR);
        out.writeByte(LF);
    }

    private void encodeCommandName(ByteBuf out, ImapCommand command) {
        writeAsciiBytes(out, command.getCommandName());
    }

    private void writeAsciiBytes(ByteBuf out, String s) {
        out.writeBytes(s.getBytes(US_ASCII));
    }

    private void writeAsciiBytes(ByteBuf out, CharSequence s) {
        out.writeBytes(s.toString().getBytes(US_ASCII));
    }
}
