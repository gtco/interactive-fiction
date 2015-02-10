package org.gtco;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Atr {


    private List<Integer> bytes = new ArrayList<Integer>();


    public boolean load(String fn) {

        FileInputStream in = null;

        try {

            in = new FileInputStream(fn);

            int c;

            while ((c = in.read()) != -1) {
                bytes.add(c);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();

        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return false;
    }


    public void printHeader() {

        if (bytes != null && bytes.size() > 16) {

            Integer magic = getValue(bytes.get(1), bytes.get(0));
            Integer paragraphs = getValue(bytes.get(3), bytes.get(2));
            Integer sectorSize = getValue(bytes.get(5), bytes.get(4));

            printWord(magic);
            printWord(paragraphs);
            printWord(sectorSize);

        }
    }

    public Integer getValue(Integer msb, Integer lsb) {
        return (msb * 256) + lsb;
    }

    public void printWord(Integer i) {
        System.out.println("Word = 0x0" + Integer.toHexString(i));
    }

}
