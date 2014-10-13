/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import com.nowucca.imp.core.message.command.AppendCommand;
import com.nowucca.imp.core.message.command.AuthenticateCommand;
import com.nowucca.imp.core.message.command.CapabilityCommand;
import com.nowucca.imp.core.message.command.ImapCommand;
import com.nowucca.imp.core.message.command.ImapRequest;
import com.nowucca.imp.core.message.command.InvalidImapRequest;
import com.nowucca.imp.core.message.command.LoginCommand;
import com.nowucca.imp.core.message.command.LogoutCommand;
import com.nowucca.imp.core.message.command.NoopCommand;
import com.nowucca.imp.core.message.command.StartTlsCommand;
import com.nowucca.imp.util.ModifiedUTF7;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.internal.AppendableCharSequence;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.mail.Flags;
import static com.nowucca.imp.core.codec.DecoderUtils.*;
import static com.nowucca.imp.core.codec.ImapCodecConstants.DEFAULT_MAXIMUM_SIZE;
import static com.nowucca.imp.core.codec.ImapCodecConstants.MAXIMUM_TAG_LENGTH;
import static com.nowucca.imp.core.codec.ImapRequestDecoder.State.READ_COMMAND_NAME;
import static com.nowucca.imp.core.codec.ImapRequestDecoder.State.READ_CRLF;
import static java.lang.String.format;

/**
 */
public class ImapRequestDecoder extends ReplayingDecoder<ImapRequestDecoder.State> {

    private static final Charset US_ASCII = Charset.forName("US-ASCII");

    public static enum State {
        READ_TAG,
        READ_COMMAND_NAME,
        READ_CRLF,
        INVALID_REQUEST
    }

    AppendableCharSequence tag;
    ImapCommand imapCommand;
    ImapRequest imapRequest;

    private final AppendableCharSequence seq = new AppendableCharSequence(128);
    private final TagParser tagParser = new TagParser(seq);
    private final QuotedStringParser quotedStringParser = new QuotedStringParser(seq);



    public ImapRequestDecoder() {
        super(State.READ_TAG);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case READ_TAG:
                try {
                    tag = tagParser.parse(in);
                    checkpoint(READ_COMMAND_NAME);

                } catch (Exception e) {
                    out.add(createInvalidRequest(e));
                    return;
                }

            case READ_COMMAND_NAME: {
                try {
                    final char c = peekNextChar(in);
                    switch(c) {
                        case 'A': {
                            final char c2 = peekNextChar(in, 1);
                            switch (c2) {
                                case 'P': {

                                    readCaseInsensitiveExpectedBytes(in, "APPEND");
                                    readExpectedByte(in, ' ');
                                    final String mailboxName = readMailboxName(ctx, in);
                                    readExpectedByte(in, ' ');
                                    final Flags flags = readOptionalAppendFlags(in);
                                    if (flags != null) {
                                        readExpectedByte(in, ' ');
                                    }
                                    final ZonedDateTime dateTime = readOptionalDateTime(in);
                                    if (dateTime != null) {
                                        readExpectedByte(in, ' ');
                                    }
                                    final ByteBuf data = readLiteral(ctx, in);

                                    imapCommand = new AppendCommand(mailboxName, flags, dateTime, data);
                                    checkpoint(READ_CRLF);
                                    break;
                                }
                                case 'U': {
                                    readCaseInsensitiveExpectedBytes(in, "AUTHENTICATE");
                                    readExpectedSpace(in);
                                    final CharSequence authenticationMechanism = readAuthenticationMechanismName(in);
                                    imapCommand = new AuthenticateCommand(authenticationMechanism);
                                    checkpoint(READ_CRLF);
                                    break;
                                }
                            }
                            break;
                        }
                        case 'C': {
                            readCaseInsensitiveExpectedBytes(in, "CAPABILITY");
                            imapCommand = new CapabilityCommand();
                            checkpoint(State.READ_CRLF);
                            break;
                        }
                        case 'L': {
                            final char c4 = peekNextChar(in, 3);
                            switch(c4) {
                                case 'I': {
                                    readCaseInsensitiveExpectedBytes(in, "LOGIN");
                                    readExpectedSpace(in);
                                    final CharSequence userId = readAString(ctx, in);
                                    readExpectedSpace(in);
                                    final CharSequence password = readAString(ctx, in);
                                    imapCommand = new LoginCommand(userId, password);
                                    checkpoint(State.READ_CRLF);
                                    break;
                                }
                                case 'O': {
                                    readCaseInsensitiveExpectedBytes(in, "LOGOUT");
                                    imapCommand = new LogoutCommand();
                                    checkpoint(State.READ_CRLF);
                                    break;
                                }
                            }
                            break;
                        }
                        case 'N': {
                            readCaseInsensitiveExpectedBytes(in, "NOOP");
                            imapCommand = new NoopCommand();
                            checkpoint(State.READ_CRLF);
                            break;
                        }
                        case 'S': {
                            readCaseInsensitiveExpectedBytes(in, "STARTTLS");
                            imapCommand = new StartTlsCommand();
                            checkpoint(State.READ_CRLF);
                            break;
                        }
                        case 'X': {
                            throw new UnsupportedOperationException(format("No extension commands are supported."));
                        }
                    }
                } catch (Exception e) {
                    out.add(createInvalidRequest(e));
                    return;
                }
            }


            case READ_CRLF: {
                try {
                    final byte b = in.readByte();
                    final byte c = in.readByte();
                    if (b != '\r' || c != '\n') {
                        throw new IllegalArgumentException(format("Expected CRLF, received %s.",
                                new String(new byte[]{b, c})));
                    }
                    out.add(new ImapRequest() {
                        @Override
                        public ImapCommand getCommand() {
                            return imapCommand;
                        }

                        @Override
                        public String getTag() {
                            return tag.toString();
                        }

                        @Override
                        public void setDecoderResult(DecoderResult result) {
                        }

                        @Override
                        public DecoderResult getDecoderResult() {
                            return DecoderResult.SUCCESS;
                        }
                    });
                    checkpoint(State.READ_TAG);
                    return;
                } catch (RuntimeException e) {
                    out.add(createInvalidRequest(e));
                    return;
                }
            }

            case INVALID_REQUEST: {
                // chew up any extra bytes after a bad request read has been attempted.
                in.skipBytes(actualReadableBytes());
                break;
            }
        }
    }

    /**
     * Authentication mechanism naming is defined in RFC 4422, Section 3.1.
     *
     * <pre>
     * sasl-mech    = 1*20mech-char
     * mech-char    = UPPER-ALPHA / DIGIT / HYPHEN / UNDERSCORE
     * ; mech-char is restricted to A-Z (uppercase only), 0-9, -, and _
     * ; from ASCII character set.
     *
     * UPPER-ALPHA  = %x41-5A  ; A-Z (uppercase only)
     * DIGIT        = %x30-39  ; 0-9
     * HYPHEN       = %x2D ; hyphen (-)
     * UNDERSCORE   = %x5F ; underscore (_)
     * </pre>
     * @return a syntactically legal SASL mechanism name
     */
    private AppendableCharSequence readAuthenticationMechanismName(ByteBuf in) {
        final AppendableCharSequence result = new AppendableCharSequence(20);

        char next = peekNextChar(in);
        while (isSASLMechanismChar(next)) {
            result.append((char) in.readByte());
            next = peekNextChar(in);
        }
        if (result.length() > 20) {
            throw new IllegalArgumentException(
                    format("Authentication mechanism %s exceeds 20 characters in length.", result.toString()));
        }
        return result;
    }


    private Flags readOptionalAppendFlags(ByteBuf in) {
        final char next = peekNextChar(in);
        if (next == '(') {
            return readFlags(in);
        }
        return null;
    }

    public Flags readFlags(ByteBuf in) {
        final Flags flags = new Flags();
        readExpectedByte(in, '(');

        char next = peekNextChar(in);
        if (next == ')') {
            readExpectedByte(in, ')');
            return flags;
        }

        do {
            final String flag = readFlag(in);
            setFlag(flag, flags);
            if (peekNextChar(in) != ')') {
                readExpectedByte(in, ' ');
            }
            next = peekNextChar(in);
        } while (next != ')');

        readExpectedByte(in, ')');
        return flags;
    }

    private String readFlag(ByteBuf in) {
        final char next = peekNextChar(in);
        if (next == '\\') {
            readExpectedByte(in, '\\');
            return "\\" + readAtom(in);
        } else {
            // flag-keyword from RFC-2501
            return readAtom(in);
        }
    }

    public static void setFlag(final String flagString, final Flags flags) {
        if (flagString.equalsIgnoreCase(ImapCodecConstants.ANSWERED_ALL_CAPS)) {
            flags.add(Flags.Flag.ANSWERED);
        } else if (flagString.equalsIgnoreCase(ImapCodecConstants.DELETED_ALL_CAPS)) {
            flags.add(Flags.Flag.DELETED);
        } else if (flagString.equalsIgnoreCase(ImapCodecConstants.DRAFT_ALL_CAPS)) {
            flags.add(Flags.Flag.DRAFT);
        } else if (flagString.equalsIgnoreCase(ImapCodecConstants.FLAGGED_ALL_CAPS)) {
            flags.add(Flags.Flag.FLAGGED);
        } else if (flagString.equalsIgnoreCase(ImapCodecConstants.SEEN_ALL_CAPS)) {
            flags.add(Flags.Flag.SEEN);
        } else {
            if (flagString.equalsIgnoreCase(ImapCodecConstants.RECENT_ALL_CAPS)) {
                throw new UnsupportedOperationException(format("Cannot set the \\RECENT flag per RFC 3501."));
            } else {
                // RFC3501 allows user flags
                flags.add(flagString);
            }
        }
    }

    private String readMailboxName(ChannelHandlerContext ctx, ByteBuf in) {
        return ModifiedUTF7.decode(readMailboxNameRaw(ctx, in));
    }

    private String readMailboxNameRaw(ChannelHandlerContext ctx, ByteBuf in) {
        String mailboxName;

        final char next = peekNextChar(in);
        switch(next) {
            case '"':
                mailboxName = quotedStringParser.parse(in).toString();
                break;
            case '{':
                mailboxName = readLiteral(ctx, in).toString(US_ASCII);
                break;
            default:
                mailboxName = readAtom(in);
        }

        if ("INBOX".equalsIgnoreCase(mailboxName)) {
            return ImapCodecConstants.INBOX;
        } else {
            return mailboxName;
        }
    }


    private ImapRequest createInvalidRequest(Exception cause) {
        checkpoint(State.INVALID_REQUEST);
        imapRequest = new InvalidImapRequest(cause);
        imapRequest.setDecoderResult(DecoderResult.failure(cause));
        final ImapRequest ret = imapRequest;
        imapRequest = null;
        return ret;
    }

    private abstract class BaseParser implements ByteBufProcessor {
        protected final AppendableCharSequence seq;
        protected int size;

        protected BaseParser(AppendableCharSequence seq) {
            this.seq = seq;
        }

        public AppendableCharSequence parse(ByteBuf buffer) {
            seq.reset();
            size = 0;
            final int i = buffer.forEachByte(this);
            buffer.readerIndex(i + 1);
            return seq;
        }

        protected Set<Character> terminatingCharacters() {
            return Collections.singleton(' ');
        }
        protected int maximumSize() { return DEFAULT_MAXIMUM_SIZE; }
        protected boolean isValidChar(char c) { return true; }

        @Override
        public boolean process(byte value) throws Exception {
            final char nextByte = (char) value;
            if (terminatingCharacters().contains(nextByte)) {
                return false;
            } else {
                if (size >= maximumSize()) {
                    throw new TooLongFrameException(
                            format("Exceeded maximum size of %d bytes", maximumSize()));
                }
                if (isValidChar(nextByte)) {
                    size++;
                    seq.append(nextByte);
                } else {
                    throw new IllegalArgumentException(format("Invalid character: %s", nextByte));
                }
                return true;
            }
        }
    }

    private final class TagParser extends BaseParser implements ByteBufProcessor {
        TagParser(AppendableCharSequence seq) {
            super(seq);
        }
        @Override
        protected boolean isValidChar(char c) {
            return isTagChar(c);
        }

        @Override
        protected int maximumSize() {
            return MAXIMUM_TAG_LENGTH;
        }
    }

    private final class QuotedStringParser extends BaseParser implements ByteBufProcessor {
        QuotedStringParser(AppendableCharSequence seq) {
            super(seq);
        }
        private boolean escapedMode;

        @Override
        protected boolean isValidChar(char c) {
            if (escapedMode) {
                final boolean result = isQuotedSpecial(c);
                if (result) {
                    escapedMode = false;
                }
                return result;
            } else {
                if (c == '\\') {
                    escapedMode = true;
                    return true;
                } else {
                    return c == '"' || isQuotedChar(c);
                }
            }
        }
    }


}
