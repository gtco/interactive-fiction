package org.gtco.jifi;

import java.util.ArrayList;
import java.util.List;

public class Zstring extends Zchar {
    private String text = "";

    /**
     * Retrive a zstring terminated with an end marker
     */
    public Zstring(MemoryMap mm, int address, int size, AbbreviationTable abbrTable) {
        this.mm = mm;
        this.abbrTable = abbrTable;

        if (size != 0) {
            List<Byte> decoded = decode(address);

            if (decoded.size() > 0) {
                text = convert(decoded);
            }
        }
    }

    /**
     * Retrieve a single 3 byte string
     */
    public Zstring(MemoryMap mm, int address) {
        this.mm = mm;

        List<Byte> decoded = decode2(address);

        if (decoded.size() > 0) {
            text = convert(decoded);
        }
    }


    protected List<Byte> decode(int address) {
        this.start = address;
        this.end = address;
        byte[] buf = new byte[3];
        List<Byte> list = new ArrayList<Byte>();

        int text = 0;
        boolean end_marker = true;
        int alpha = 0;
        int abbr_offset = 0;

        do {
            text = mm.getWord(end);
            buf = Zchar.createFromWord(text);

            // ZSCII
            if (buf[0] == 5 && buf[1] == 6 && (buf[2] > 0)) {
                end += 2;
                text = mm.getWord(end);
                byte[] zb = Zchar.createFromWord(text);
                int x = ((buf[2] & 0x1f) << 5) + (zb[0] & 0x1f);
                list.add((byte) x);
            } else {
                for (int j = 0; j < buf.length; j++) {
                    if (alpha == 3) {
                        int abbr_num = abbr_offset + buf[j];
                        list.addAll(abbrTable.getBytes(abbr_num));
                        abbr_offset = 0;
                        alpha = 0;
                    } else if (buf[j] == 0) {
                        list.add((byte) 32);
                    } else if (buf[j] == 1) {
                        abbr_offset = 0;
                        alpha = 3;
                    } else if (buf[j] == 2) {
                        abbr_offset = 32;
                        alpha = 3;
                    } else if (buf[j] == 3) {
                        abbr_offset = 64;
                        alpha = 3;
                    } else if (buf[j] == 4) {
                        alpha = 1;
                    } else if (buf[j] == 5) {
                        alpha = 2;
                    } else if (buf[j] > 5 && buf[j] < 32) {
                        list.add(Zchar.decodeCharacter(buf[j], alpha));
                        alpha = 0;
                    }
                }
            }

            end_marker = ((text & END_MARKER) == END_MARKER);
            end += 2;

        }
        while (!end_marker);

        return list;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }

    protected List<Byte> decode2(int address) {
        this.start = address;
        this.end = address;
        byte[] buf = new byte[3];
        List<Byte> list = new ArrayList<Byte>();

        int text = 0;
        int alpha = 0;
        int abbr_offset = 0;

        text = mm.getWord(end);
        buf = Zchar.createFromWord(text);

        // ZSCII
        if (buf[0] == 5 && buf[1] == 6) {
            end += 2;
            text = mm.getWord(end);
            byte[] zb = Zchar.createFromWord(text);
            int x = ((buf[2] & 0x1f) << 5) + (zb[0] & 0x1f);
            list.add((byte) x);
        } else {
            for (int j = 0; j < buf.length; j++) {
                if (alpha == 3) {
                    int abbr_num = abbr_offset + buf[j];
                    list.addAll(abbrTable.getBytes(abbr_num));
                    abbr_offset = 0;
                    alpha = 0;
                } else if (buf[j] == 0) {
                    list.add((byte) 32);
                } else if (buf[j] == 1) {
                    abbr_offset = 0;
                    alpha = 3;
                } else if (buf[j] == 2) {
                    abbr_offset = 32;
                    alpha = 3;
                } else if (buf[j] == 3) {
                    abbr_offset = 64;
                    alpha = 3;
                } else if (buf[j] == 4) {
                    alpha = 1;
                } else if (buf[j] == 5) {
                    alpha = 2;
                } else if (buf[j] > 5 && buf[j] < 32) {
                    list.add(Zchar.decodeCharacter(buf[j], alpha));
                    alpha = 0;
                }
            }
        }


        return list;
    }


}
