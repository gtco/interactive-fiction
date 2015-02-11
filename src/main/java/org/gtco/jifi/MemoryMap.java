package org.gtco.jifi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class MemoryMap {

    public static final int HEADER_LEN = 0x040;
    public final static int VERSION_NUMBER = 0x00;
    public final static int FLAGS = 0x01;
    public final static int HIGH_MEM = 0x04;
    public final static int INITIAL_PC = 0x06;
    public final static int DICTIONARY = 0x08;
    public final static int OBJECT_TABLE = 0x0A;
    public final static int GLOBAL_VAR = 0x0C;
    public final static int STATIC_MEM = 0x0E;
    public final static int FLAGS_2 = 0x10;
    public final static int ABBR_TABLE = 0x18;
    public final static int FILE_LENGTH = 0x1A;
    public final static int FILE_CHKSUM = 0x1C;
    public final static int INTERP_NUM = 0x1E;
    public final static int INTERP_VER = 0x1F;
    public final static int SCREEN_HEIGHT = 0x20;
    public final static int SCREEN_WIDTH = 0x21;
    public final static int SCREEN_WIDTH_U = 0x22;
    public final static int SCREEN_HEIGHT_U = 0x24;
    public final static int FONT_WIDTH_U = 0x26;
    public final static int FONT_HEIGHT_U = 0x27;
    public final static int ROUTINES_OFFSET = 0x28;
    public final static int STATIC_STRINGS_OFFSET = 0x2A;
    public final static int BACKGROUND_DEF = 0x2C;
    public final static int FOREGROUND_DEF = 0x2D;
    public final static int TERMINATING_TABLE = 0x2E;
    public final static int PIXEL_WIDTH = 0x30; // stream 3
    public final static int REVISION_NUMBER = 0x32;
    public final static int ALPHABET_TABLE = 0x34;
    public final static int HEADER_EXT_TABLE = 0x36;
    private static Logger log = LoggerFactory.getLogger(MemoryMap.class);

    // limit for a z-file (bytes)
    // public static final int LIMIT = 0x10000;
//	private int dmem = 0; // dynamic
//	private int smem = 0; // static
//	private int hmem = 0; // high

    private File file;
    private byte[] buffer;

    public MemoryMap() {
    }

    public int getByte(int offset) {
        return (buffer[offset] & 0xff);
    }

    public int getWord(int offset) {
        byte msb = buffer[offset];
        byte lsb = buffer[offset + 1];
        int addr = ((msb & 0xff) << 8) + (lsb & 0xff);
        return addr;
    }

    public boolean load(String filename) {
        file = new File(filename);
        if (file.exists()) {
            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                ByteArrayOutputStream ba = new ByteArrayOutputStream();
                int d = -1;
                d = bis.read();
                while (d >= 0) {
                    if (d >= 0) {
                        ba.write(d);
                    }
                    d = bis.read();
                }
                bis.close();
                ba.close();
                buffer = ba.toByteArray();
                log.info("filename = " + filename + ", size = " + ba.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void setWord(int offset, int value) {
        byte msb = (byte) (value >> 8);
        byte lsb = (byte) (value & 0xff);
        buffer[offset] = msb;
        buffer[offset + 1] = lsb;
    }

    public void setByte(int offset, int value) {
        buffer[offset] = (byte) value;
    }

}
