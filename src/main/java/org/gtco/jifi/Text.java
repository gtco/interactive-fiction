package org.gtco.jifi;

import java.util.*;

public class Text {

    public static final int END_MARKER = 0x8000;
    public static final int FIRST_CHAR = 0x7c00;
    public static final int SECOND_CHAR = 0x3e0;
    public static final int THIRD_CHAR = 0x1f;

    private MemoryMap m_map;
    private int m_abbrTable;

    protected static final String[] a2 = {"&", "\n", "0", "1",
            "2", "3", "4", "5", "6", "7", "8", "9", ".", ",", "!", "?", "_",
            "#", "'", "\"", "/", "\\", "-", ":", "(", ")"};

    protected static final byte[] a2_byte = {38, 13, 48, 49,
            50, 51, 52, 53, 54, 55, 56, 57, 46, 44, 33, 63, 95,
            35, 39, 34, 47, 92, 45, 58, 91, 93};


    public Text(MemoryMap mm) {
        m_map = mm;
        m_abbrTable = mm.getWord(MemoryMap.ABBR_TABLE);
    }

    public List decode(int address) {
        byte[] buf = new byte[3];
//        byte[] enc;
        List<Byte> list = new ArrayList<Byte>();

        int text = 0;
        boolean end_marker = true;
        int alpha = 0, abbr_offset = 0;
//        int count = 0;

        do {
            text = m_map.getWord(address);
            buf[0] = (byte) ((text & FIRST_CHAR) >> 10);
            buf[1] = (byte) ((text & SECOND_CHAR) >> 5);
            buf[2] = (byte) (text & THIRD_CHAR);
            end_marker = ((text & END_MARKER) == END_MARKER);
            address += 2;
            for (int j = 0; j < buf.length; j++) {
                if (buf[j] == 0) {
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
                    if (alpha == 0) {
                        list.add((byte) (buf[j] + 91));
                    } else if (alpha == 1) {
                        list.add((byte) (buf[j] + 59));
                        alpha = 0;
                    } else if (alpha == 2) {
                        list.add(a2_byte[(buf[j] - 6)]);
                        alpha = 0;
                    } else if (alpha == 3) {
                        int abbr_num = abbr_offset + buf[j];
                        list.addAll(lookupAbbreviation(abbr_num));
                        abbr_offset = 0;
                        alpha = 0;
                    }
                }
            }
        } while (!end_marker);

        return list;
    }

    private List<Byte> lookupAbbreviation(int abbreviationNumber) {
        byte[] buf = new byte[3];
//        byte[] enc;
        List<Byte> list = new ArrayList<Byte>();
        int offset = m_abbrTable + (abbreviationNumber * 2);
        int packed_address = m_map.getWord(offset);
        int address = packed_address * 2;

        int text = 0;
        boolean end_marker = true;

        do {
            text = m_map.getWord(address);
            buf[0] = (byte) ((text & FIRST_CHAR) >> 10);
            buf[1] = (byte) ((text & SECOND_CHAR) >> 5);
            buf[2] = (byte) (text & THIRD_CHAR);
            end_marker = ((text & END_MARKER) == END_MARKER);
            address += 2;

            int alpha = 0, abbr_offset = 0;
            for (int j = 0; j < buf.length; j++) {
                if (buf[j] == 0) {
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
                    if (alpha == 0) {
                        list.add((byte) (buf[j] + 91));
                    } else if (alpha == 1) {
                        list.add((byte) (buf[j] + 59));
                        alpha = 0;
                    } else if (alpha == 2) {
                        list.add(a2_byte[(buf[j] - 6)]);
                        alpha = 0;
                    } else if (alpha == 3) {
//                        int abbr_num = abbr_offset + buf[j];
                        // error
                        abbr_offset = 0;
                        alpha = 0;
                    }
                }
            }
        } while (!end_marker);

        return list;
    }

    public String convert(List<Byte> list) {
        String strRet = "";

        byte[] b = new byte[list.size()];

        for (int i = 0; i < list.size(); i++)
            b[i] = (Byte) list.get(i);

        try {
            strRet = new String(b, "US-ASCII");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return strRet;
    }
}