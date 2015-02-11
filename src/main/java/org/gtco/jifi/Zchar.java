package org.gtco.jifi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Zchar {

    public static final int END_MARKER = 0x8000;
    public static final int FIRST_CHAR = 0x7c00;
    public static final int SECOND_CHAR = 0x3e0;
    public static final int THIRD_CHAR = 0x1f;

    protected static final byte[] A2_BYTE = {
            38, 13, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 46,
            44, 33, 63, 95, 35, 39, 34, 47, 92, 45, 58, 40, 41
    };

    protected int start = 0;
    protected int end = 0;
    protected MemoryMap mm = null;
    protected AbbreviationTable abbrTable = null;
    protected static final Logger log = LoggerFactory.getLogger(Zchar.class);


    public static byte[] createFromWord(int word) {
        byte buf[] = new byte[3];
        buf[0] = (byte) ((word & FIRST_CHAR) >> 10);
        buf[1] = (byte) ((word & SECOND_CHAR) >> 5);
        buf[2] = (byte) (word & THIRD_CHAR);
        return buf;
    }

    public static Byte decodeCharacter(byte b, int alphabet) {
        Byte character = (byte) 32;

        if (alphabet == 0) {
            character = (byte) (b + 91);
        } else if (alphabet == 1) {
            character = (byte) (b + 59);
        } else if (alphabet == 2) {
            character = A2_BYTE[(b - 6)];
        } else {
            log.error("Cannot process character, alphabet=" + alphabet);
        }

        return character;
    }

    protected String convert(List<Byte> list) {
        String s = "";
        byte[] b = new byte[list.size()];

        for (int i = 0; i < list.size(); i++) {
            b[i] = list.get(i);
        }

        try {
            s = new String(b, "US-ASCII");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return s;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

}
