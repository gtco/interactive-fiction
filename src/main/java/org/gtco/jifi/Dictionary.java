package org.gtco.jifi;

import java.util.ArrayList;
import java.util.List;

public class Dictionary {
    private MemoryMap mm = null;
    private List<Character> inputCodes = new ArrayList<Character>();
    private int el = 0;
    private int ec = 0;
    private int es = 0;

    private List<String> words = null;

    public Dictionary(MemoryMap mm) {
        this.mm = mm;
        int address = this.mm.getWord(MemoryMap.DICTIONARY);

        // # of input codes
        int n = this.mm.getByte(address);
        // keyboard input codes
        for (int i = 1; i <= n; i++) {
            inputCodes.add((char) this.mm.getByte(address + i));
        }

        // entry length
        el = this.mm.getByte(address + n + 1);
        // number-of-entries
        ec = this.mm.getWord(address + n + 2);
        es = address + n + 4;
        words = new ArrayList<String>();

        loadEntries();
    }

    public int getEntryLength() {
        return el;
    }

    public int getEntryCount() {
        return ec;
    }

    public List<Character> getInputCodes() {
        return inputCodes;
    }

    private void loadEntries() {
        for (int i = 0; i < ec; i++) {
            int offset = (i * el) + es;
            Zstring z1 = new Zstring(this.mm, offset);
            Zstring z2 = new Zstring(this.mm, offset + 2);
            words.add(z1.getText().toLowerCase() + z2.getText().toLowerCase());
        }
    }

    public int findWord(String s) {
        int n = -1;

        if (words.contains(s)) {
            n = words.indexOf(s) + 1;
        }

        return n;
    }
}
