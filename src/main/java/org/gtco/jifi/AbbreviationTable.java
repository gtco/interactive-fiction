package org.gtco.jifi;

import java.util.ArrayList;
import java.util.List;

public class AbbreviationTable extends Zchar {
    private int address = 0;
    List<String> strings = new ArrayList<String>();
    List<List<Byte>> bytes = new ArrayList<List<Byte>>();

    public AbbreviationTable(int address, MemoryMap mm) {
        this.address = address;
        this.mm = mm;

        for (int i = 0; i < 96; i++) {
            List<Byte> li = load(i);
            bytes.add(li);
            String s = convert(li);
            strings.add(s);
        }
    }

    public List<Byte> getBytes(int i) {
        return bytes.get(i);
    }

    private List<Byte> load(int abbreviationNumber) {
        List<Byte> list = new ArrayList<Byte>();

        byte[] buf = null;
        int offset = address + (abbreviationNumber * 2);
        int packed_address = mm.getWord(offset);
        int a = packed_address * 2;
        int w = 0;
        boolean isEnd = true;
        int alpha = 0;

        do {
            w = mm.getWord(a);
            buf = Zchar.createFromWord(w);
            isEnd = ((w & END_MARKER) == END_MARKER);
            a += 2;

            for (int j = 0; j < buf.length; j++) {
                if (buf[j] == 0) {
                    list.add((byte) 32);
                } else if (buf[j] == 1 || buf[j] == 2 || buf[j] == 3) {
                    log.warn("No abbreviations should be present in the table.");
                    alpha = 3;
                } else if (buf[j] == 4) {
                    alpha = 1;
                } else if (buf[j] == 5) {
                    alpha = 2;
                } else if (buf[j] > 5 && buf[j] < 32) {
                    list.add(decodeCharacter(buf[j], alpha));
                    alpha = 0;
                }
            }
        }
        while (!isEnd);

        return list;
    }


}
