/**
 * Copyright (c) 2012-2014, Steven Atkinson. All rights reserved.
 */
package com.nowucca.imp.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.AppendableCharSequence;
import java.nio.charset.Charset;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import static java.lang.String.format;

/**
 */
public final class DecoderUtils {

    private static final Charset US_ASCII = Charset.forName("US-ASCII");
    static final byte[] CONTINUATION_BYTES = new byte[]{'+', '\r', '\n'};

    private DecoderUtils() {
    }

    public static char peekNextChar(ByteBuf in) {
        return (char) in.getByte(in.readerIndex());
    }

    public static char peekNextChar(ByteBuf in, int readerIndexOffset) {
        return (char) in.getByte(in.readerIndex() + readerIndexOffset);
    }

    public static String readAtom(ByteBuf in) {
        final StringBuilder atom = new StringBuilder();
        char next = peekNextChar(in);
        while (isATOM_CHAR(next) && !isWhitespace(next)) {
            atom.append((char) in.readByte());
            next = peekNextChar(in);
        }
        return atom.toString();
    }

    public static CharSequence readQuoted(ByteBuf in) {
        final AppendableCharSequence result = new AppendableCharSequence(128);
        readExpectedByte(in, '"');
        char c = (char) in.readByte();

        boolean escapedMode = false;
        do {
            if (escapedMode) {
                if (isQuotedSpecial(c)) {
                    escapedMode = false;
                }
                result.append(c);
            } else {
                if (c == '\\') {
                    escapedMode = true;
                } else {
                    if (!isQuotedChar(c)) {
                        throw new IllegalArgumentException(format("Illegal character %s", c));
                    }
                    result.append(c);
                }
            }

            c = (char) in.readByte();
        } while (c != '"' || escapedMode);


        return result;
    }

    /**
     * astring =  1*ASTRING-CHAR / quoted / literal
     *
     * @param in
     * @return
     */
    public static CharSequence readAString(ChannelHandlerContext ctx, ByteBuf in) {
        CharSequence aString;

        final char next = peekNextChar(in);
        switch(next) {
            case '"':
                aString = readQuoted(in);
                break;
            case '{':
                aString = readLiteral(ctx, in).toString(US_ASCII);
                break;
            default:
                aString = readAstringRaw(in);
        }
        return aString;
    }

    private static CharSequence readAstringRaw(ByteBuf in) {
        final AppendableCharSequence aString = new AppendableCharSequence(128);
        char next = peekNextChar(in);
        while (isASTRING_CHAR(next) && !isWhitespace(next)) {
            aString.append((char) in.readByte());
            next = peekNextChar(in);
        }
        return aString;
    }

    public static ByteBuf readLiteral(ChannelHandlerContext ctx, ByteBuf in) {
        ByteBuf result;

        readExpectedByte(in, '{');
        final long size = readNumber(in);
        readExpectedByte(in, '}');
        readCRLF(in);

        // send continuation command back to client
        ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(CONTINUATION_BYTES));

        if (size > Integer.MAX_VALUE) {
            //TODO use countdown and composite byte buffers to read large buffers
            throw new UnsupportedOperationException(format("Large literals not supported."));
        } else {
            result = readBytes(in, (int) size);
        }
        result.forEachByte(char8Validator);
        return result;
    }



    private static final Char8Validator char8Validator = new Char8Validator();
    private static final class Char8Validator implements ByteBufProcessor {
        @Override
        public boolean process(byte value) throws Exception {
            if (!isCHAR8((char) value)) {
                throw new IllegalArgumentException(format("Expected a CHAR8 character, found '%s'", value));
            }
            return true;
        }
    }


    public static CharSequence readBase64(ByteBuf in) {
        if (isBase64Char(peekNextChar(in))) {
            final AppendableCharSequence result = new AppendableCharSequence(128);
            do {
                final char c1 = (char) in.readByte();
                final char c2 = (char) in.readByte();
                final char c3 = (char) in.readByte();
                final char c4 = (char) in.readByte();
                if (isBase64Char(c1) && isBase64Char(c2) &&
                        (isBase64Char(c3) || c3 == '=') &&
                        (isBase64Char(c4) || c4 == '=')) {
                    result.append(c1).append(c2).append(c3).append(c4);
                } else {
                    throw new IllegalArgumentException(
                            format("Illegal characters found in base 64 sequence: %s%s%s%s", c1, c3, c3, c4));
                }
                if (c4 == '=') {
                    break;
                }
            } while (true);
            return result;
        } else {
            return "";
        }
    }

    private static boolean isBase64Char(char c) {
        return isAlphaChar(c) || isDigit(c) || c == '+' || c == '/';
    }

    public static void readExpectedByte(ByteBuf in, char expected) {
        final char c = (char) in.readByte();
        if (c != expected) {
            throw new IllegalArgumentException(format("Expected %s, read %s", expected, c));
        }
    }

    public static void readExpectedSpace(ByteBuf in) {
        readExpectedByte(in, ' ');
    }

    public static  void readCaseInsensitiveExpectedBytes(ByteBuf in, String expectedBytes) {
        final int expectedLength = expectedBytes.length();
        final String actual = in.readBytes(expectedLength).toString(US_ASCII);
        if (!expectedBytes.equalsIgnoreCase(actual)) {
            throw new IllegalArgumentException(format("Expected '%s' but read '%s'", expectedBytes, actual));
        }
    }


    public static long readNumber(ByteBuf in) {
        final StringBuilder digits = new StringBuilder();
        char c = peekNextChar(in);
        while (isDigit(c)) {
            digits.append((char) in.readByte());
            c = peekNextChar(in);
        }
        return Long.parseLong(digits.toString());
    }

    public static ByteBuf readBytes(ByteBuf in, int size) {
        return in.readBytes(size);
    }


    public static void readCRLF(ByteBuf in) {
        readExpectedByte(in, '\r');
        readExpectedByte(in, '\n');
    }


    public static boolean isSASLMechanismChar(char c) {
        return isUpperAlphaChar(c) || isDigit(c) || c == '-' || c == '_';
    }

    private static boolean isUpperAlphaChar(char c) {
        return c >= 0x41 && c <= 0x5A;
    }

    private static boolean isAlphaChar(char c) {
        return (c >= 0x41 && c <= 0x5A) ||
               (c >= 0x61 && c <= 0x7A);
    }

    public static boolean isDigit(char c) {
        return c >= 0x30 && c <= 0x39;
    }

    public static boolean isNonZeroDigit(char c) {
        return c >= 0x31 && c <= 0x39;
    }

    public static boolean isQuotedChar(char c) {
        return isTextChar(c) && !isQuotedSpecial(c);
    }

    public static boolean isTextChar(char c) {
        return isCHAR(c) && c != '\r' && c != '\n';
    }


    public static boolean isTagChar(char c) {
        return isASTRING_CHAR(c) && c != '+';
    }

    public static boolean isATOM_CHAR(char c) {
        return isCHAR(c) && !isAtomSpecial(c);
    }

    public static boolean isASTRING_CHAR(char c) {
        return isATOM_CHAR(c) || isRespSpecial(c);
    }

    public static boolean isCHAR(char c) {
        return c >= 0x01 && c <= 0x7f;
    }

    public static boolean isCHAR8(char c) {
        return c >= 0x01 && c <= 0xff;
    }

    public static boolean isAtomSpecial(char c) {
        return c == '(' || c == ')' || c == '{' || c == ' ' || c == Character.CONTROL ||
                isListWildcard(c) || isQuotedSpecial(c) || isRespSpecial(c);
    }

    public static boolean isListWildcard(char c) {
        return c == '*' || c == '%';
    }

    public static boolean isQuotedSpecial(char c) {
        return c == '"' || c == '\\';
    }

    public static boolean isRespSpecial(char c) {
        return c == ']';
    }

    public static boolean isWhitespace(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }


    public static ZonedDateTime readOptionalDateTime(ByteBuf in) {
        if (peekNextChar(in) != '"') {
            return null;
        }

        final CharSequence chars = readQuoted(in);
        /*
        date-time       = DQUOTE date-day-fixed "-" date-month "-" date-year
                  SP time SP zone DQUOTE

        date-day        = 1*2DIGIT
                    ; Day of month

        date-day-fixed  = (SP DIGIT) / 2DIGIT
                            ; Fixed-format version of date-day

        date-month      = "Jan" / "Feb" / "Mar" / "Apr" / "May" / "Jun" /
                          "Jul" / "Aug" / "Sep" / "Oct" / "Nov" / "Dec"

        date-text       = date-day "-" date-month "-" date-year

        date-year       = 4DIGIT

        time            = 2DIGIT ":" 2DIGIT ":" 2DIGIT
                    ; Hours minutes seconds

        zone            = ("+" / "-") 4DIGIT
                    ; Signed four-digit value of hhmm representing
                    ; hours and minutes east of Greenwich (that is,
                    ; the amount that the given time differs from
                    ; Universal Time).  Subtracting the timezone
                    ; from the given time will give the UT form.
                    ; The Universal Time zone is "+0000".
         */
        if (chars.toString().length() != 26) {
            throw new IllegalArgumentException(format("Expected valid date-time, found %s", chars));
        }

        final char day1 = chars.charAt(0);
        final char day2 = chars.charAt(1);
        final int day = decodeFixedDay(day1, day2);

        final char month1 = chars.charAt(3);
        final char month2 = chars.charAt(4);
        final char month3 = chars.charAt(5);
        final Month month = decodeMonth(month1, month2, month3);

        final char year1 = chars.charAt(7);
        final char year2 = chars.charAt(8);
        final char year3 = chars.charAt(9);
        final char year4 = chars.charAt(10);
        final int year = decodeYear(year1, year2, year3, year4);


        final char sign = chars.charAt(21);
        final char zonehour1 = chars.charAt(22);
        final char zonehour2 = chars.charAt(23);
        final char zoneminute1 = chars.charAt(24);
        final char zoneminute2 = chars.charAt(25);
        final int offset = decodeZone(sign, zonehour1, zonehour2, zoneminute1, zoneminute2);


        final char hour1 = chars.charAt(12);
        final char hour2 = chars.charAt(13);
        final int hour = applyHourOffset(offset, decodeNumber(hour1, hour2));

        final char minute1 = chars.charAt(15);
        final char minute2 = chars.charAt(16);
        final int minute = applyMinuteOffset(offset, decodeNumber(minute1, minute2));

        final char second1 = chars.charAt(18);
        final char second2 = chars.charAt(19);
        final int second = decodeNumber(second1, second2);

        return ZonedDateTime.of(year, month.getValue(), day, hour, minute, second, 0, ZoneId.of("GMT"));
    }

    public static int decodeFixedDay(final char dayHigh, final char dayLow) throws IllegalArgumentException {
        int result = decodeDigit(dayLow);
        switch (dayHigh) {
            case '0':
                return result;
            case '1':
                return result += 10;
            case '2':
                return result += 20;
            case '3':
                return result += 30;
            case ' ':
                return result;
        }
        throw new IllegalArgumentException(format("Expected SP, 0, 1, 2, or 3 but was %s", dayHigh));
    }

    private static final int ASCII_ZERO = '0';

    public static int decodeDigit(char c) {
        final int result = c - ASCII_ZERO;
        if (result < 0 || result > 9) {
            throw new IllegalArgumentException(format("Expected a digit but was '%s'", c));
        }
        return result;
    }

    private static final int JAN_BIT = 0x1;
    private static final int FEB_BIT = 0x2;
    private static final int MAR_BIT = 0x4;
    private static final int APR_BIT = 0x8;
    private static final int MAY_BIT = 0x10;
    private static final int JUN_BIT = 0x20;
    private static final int JUL_BIT = 0x40;
    private static final int AUG_BIT = 0x80;
    private static final int SEP_BIT = 0x100;
    private static final int OCT_BIT = 0x200;
    private static final int NOV_BIT = 0x400;
    private static final int DEC_BIT = 0x800;
    private static final int ALL_MONTH_BITS = JAN_BIT | FEB_BIT | MAR_BIT | APR_BIT | MAY_BIT | JUN_BIT |
                                              JUL_BIT | AUG_BIT | SEP_BIT | OCT_BIT | NOV_BIT | DEC_BIT;

    public static Month decodeMonth(final char monthFirstChar,
                                    final char monthSecondChar,
                                    final char monthThirdChar)  {
        final Month result;
        // Bitwise magic! Eliminate possibility by three switches
        int possibleMonths = ALL_MONTH_BITS;
        switch (monthFirstChar) {
            case 'J':
            case 'j':
                possibleMonths &= JAN_BIT | JUN_BIT | JUL_BIT;
                break;
            case 'F':
            case 'f':
                possibleMonths &= FEB_BIT;
                break;
            case 'M':
            case 'm':
                possibleMonths &= MAR_BIT | MAY_BIT;
                break;
            case 'A':
            case 'a':
                possibleMonths &= APR_BIT | AUG_BIT;
                break;
            case 'S':
            case 's':
                possibleMonths &= SEP_BIT;
                break;
            case 'O':
            case 'o':
                possibleMonths &= OCT_BIT;
                break;
            case 'N':
            case 'n':
                possibleMonths &= NOV_BIT;
                break;
            case 'D':
            case 'd':
                possibleMonths &= DEC_BIT;
                break;
            default:
                possibleMonths = 0;
                break;
        }
        switch (monthSecondChar) {
            case 'A':
            case 'a':
                possibleMonths &= JAN_BIT | MAR_BIT | MAY_BIT;
                break;
            case 'E':
            case 'e':
                possibleMonths &= FEB_BIT | SEP_BIT | DEC_BIT;
                break;
            case 'P':
            case 'p':
                possibleMonths &= APR_BIT;
                break;
            case 'U':
            case 'u':
                possibleMonths &= JUN_BIT | JUL_BIT | AUG_BIT;
                break;
            case 'C':
            case 'c':
                possibleMonths &= OCT_BIT;
                break;
            case 'O':
            case 'o':
                possibleMonths &= NOV_BIT;
                break;
            default:
                possibleMonths = 0;
                break;
        }
        switch (monthThirdChar) {
            case 'N':
            case 'n':
                possibleMonths &= JAN_BIT | JUN_BIT;
                break;
            case 'B':
            case 'b':
                possibleMonths &= FEB_BIT;
                break;
            case 'R':
            case 'r':
                possibleMonths &= MAR_BIT | APR_BIT;
                break;
            case 'Y':
            case 'y':
                possibleMonths &= MAY_BIT;
                break;
            case 'L':
            case 'l':
                possibleMonths &= JUL_BIT;
                break;
            case 'G':
            case 'g':
                possibleMonths &= AUG_BIT;
                break;
            case 'P':
            case 'p':
                possibleMonths &= SEP_BIT;
                break;
            case 'T':
            case 't':
                possibleMonths &= OCT_BIT;
                break;
            case 'V':
            case 'v':
                possibleMonths &= NOV_BIT;
                break;
            case 'C':
            case 'c':
                possibleMonths &= DEC_BIT;
                break;
            default:
                possibleMonths = 0;
                break;
        }
        switch (possibleMonths) {
            case JAN_BIT:
                result = Month.JANUARY;
                break;
            case FEB_BIT:
                result = Month.FEBRUARY;
                break;
            case MAR_BIT:
                result = Month.MARCH;
                break;
            case APR_BIT:
                result = Month.APRIL;
                break;
            case MAY_BIT:
                result = Month.MAY;
                break;
            case JUN_BIT:
                result = Month.JUNE;
                break;
            case JUL_BIT:
                result = Month.JULY;
                break;
            case AUG_BIT:
                result = Month.AUGUST;
                break;
            case SEP_BIT:
                result = Month.SEPTEMBER;
                break;
            case OCT_BIT:
                result = Month.OCTOBER;
                break;
            case NOV_BIT:
                result = Month.NOVEMBER;
                break;
            case DEC_BIT:
                result = Month.DECEMBER;
                break;
            default:
                throw new IllegalArgumentException(format("Expected month name but was %s",
                        monthFirstChar + monthSecondChar + monthThirdChar));
        }
        return result;
    }

    public static int decodeYear(final char milleniumChar, final char centuryChar,
                                 final char decadeChar, final char yearChar) {
        return (decodeDigit(milleniumChar) * 1000) + (decodeDigit(centuryChar) * 100) +
                (decodeDigit(decadeChar) * 10) + decodeDigit(yearChar);
    }

    private static int applyMinuteOffset(final int offset, final int minutes) {
        final int result = minutes - ((Math.abs(offset) % 100) * (offset == 0 ? 0 : offset > 0 ? 1 : -1));
        return result;
    }

    private static int applyHourOffset(final int offset, final int hours) {
        final int result = hours - (offset / 100);
        return result;
    }

    public static int decodeNumber(final char high, final char low)  {
        return (10 * decodeDigit(high)) + decodeDigit(low);
    }

    public static int decodeZone(char zoneDeterminent, char zoneDigitOne, char zoneDigitTwo,
                                 char zoneDigitThree, char zoneDigitFour)  {
        if (isInvalidZone(zoneDeterminent, zoneDigitOne, zoneDigitTwo, zoneDigitThree, zoneDigitFour)) {
            throw createTimeZoneException(zoneDeterminent, zoneDigitOne, zoneDigitTwo, zoneDigitThree, zoneDigitFour);
        }
        final int sign;
        if (zoneDeterminent == '+') {
            sign = 1;
        } else if (zoneDeterminent == '-') {
            sign = -1;
        } else {
            throw createTimeZoneException(zoneDeterminent, zoneDigitOne, zoneDigitTwo, zoneDigitThree, zoneDigitFour);
        }
        final int result = sign * ((1000 * decodeDigit(zoneDigitOne)) + (100 * decodeDigit(zoneDigitTwo)) +
                (10 * decodeDigit(zoneDigitThree)) + decodeDigit(zoneDigitFour));
        return result;
    }

    private static RuntimeException createTimeZoneException(char zoneDeterminent,
                                                            char zoneDigitOne,
                                                            char zoneDigitTwo,
                                                            char zoneDigitThree,
                                                            char zoneDigitFour) {
        return new IllegalArgumentException(format("Expected time-zone but was %s",
                zoneDeterminent + zoneDigitOne + zoneDigitTwo + zoneDigitThree + zoneDigitFour));
    }

    private static boolean isInvalidZone(char zoneDeterminent, char zoneDigitOne, char zoneDigitTwo,
                                         char zoneDigitThree, char zoneDigitFour) {
        final boolean result;
        result = !(zoneDeterminent == '+' || zoneDeterminent == '-') ||
                !(isSimpleDigit(zoneDigitOne) && isSimpleDigit(zoneDigitTwo) &&
                  isSimpleDigit(zoneDigitThree) && isSimpleDigit(zoneDigitFour));
        return result;
    }

    /**
     * Is the given character an ASCII digit.
     *
     * @param character
     *            character
     * @return true if ASCII 0-9, false otherwise
     */
    public static boolean isSimpleDigit(char character) {
        final boolean result = !(character < '0' || character > '9');
        return result;
    }



}
