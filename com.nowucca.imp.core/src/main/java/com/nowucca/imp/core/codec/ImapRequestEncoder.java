/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.beetstra.jutf7.CharsetProvider;
import com.nowucca.imp.core.message.command.AppendCommand;
import com.nowucca.imp.core.message.command.ImapCommand;
import com.nowucca.imp.core.message.command.ImapRequest;
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
                    out.writeBytes("APPEND".getBytes(US_ASCII));
                    out.writeByte(SP);
                    encodeMailboxName(appendCommand.getMailboxName(), out);
                    if (appendCommand.getFlags() != null) {
                        out.writeByte(SP);
                        encodeFlags(appendCommand.getFlags(), out);
                    }
                    if (appendCommand.getDateTime() != null) {
                        out.writeByte(SP);
                        encodeDateTime(appendCommand.getDateTime(), out);
                    }
                    out.writeByte(SP);
                    encodeLiteral(appendCommand.getMessageLiteral(), out);
                    out.writeByte(CR);
                    out.writeByte(LF);
                    break;
                }

                default: {
                    throw new UnsupportedOperationException(format("No support for %s command.", command.getKind()));
                }

            }
        }
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
            if (prependSpace) { out.writeByte(SP); }
            out.writeBytes(ImapCodecConstants.SEEN_ALL_CAPS_BYTES);
            prependSpace = true;
        }
        if (flags.contains(Flags.Flag.DELETED)) {
            if (prependSpace) { out.writeByte(SP); }
            out.writeBytes(ImapCodecConstants.DELETED_ALL_CAPS_BYTES);
            prependSpace = true;
        }
        if (flags.contains(Flags.Flag.DRAFT)) {
            if (prependSpace) { out.writeByte(SP); }
            out.writeBytes(ImapCodecConstants.DRAFT_ALL_CAPS_BYTES);
            prependSpace = true;
        }
        if (flags.contains(Flags.Flag.RECENT)) {
            if (prependSpace) { out.writeByte(SP); }
            out.writeBytes(ImapCodecConstants.RECENT_ALL_CAPS_BYTES);
            prependSpace = true;
        }
        if (flags.contains(Flags.Flag.FLAGGED)) {
            if (prependSpace) { out.writeByte(SP); }
            out.writeBytes(ImapCodecConstants.FLAGGED_ALL_CAPS_BYTES);
            prependSpace = true;
        }

        final String[] userFlags = flags.getUserFlags();

        for (String userFlag: userFlags) {
            if (prependSpace) { out.writeByte(SP); }
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
}
