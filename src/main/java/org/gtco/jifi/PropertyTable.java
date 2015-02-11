package org.gtco.jifi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyTable {
    private int address = 0;
    private static Logger log = LoggerFactory.getLogger(PropertyTable.class);
    private Zchar name = null;
    Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
    Map<Integer, Integer> addressMap = new HashMap<Integer, Integer>();

    public PropertyTable(int address, MemoryMap mm, AbbreviationTable abbr) {
        this.address = address;
        int size = mm.getByte(address);
        int z = address + 1;
        this.name = new Zstring(mm, z, size, abbr);

        if (size == 0) {
            log.debug("size = 0");
        }

        loadProperties(mm, size != 0 ? this.name.getEnd() : this.address + 1);
    }

    public int getAddress() {
        return address;
    }

    public String getName() {
        return name.toString();
    }

    public void setValue(int property, int value) {
        if (map.containsKey(property)) {
            List<Integer> list = new ArrayList<Integer>();
            list.add(value);
            map.put(property, list);
        } else {
            log.error("No Property [" + property + "] for Object [" + name + "]");
        }
    }

    private void loadProperties(MemoryMap mm, int address) {
        log.debug("loadProperties, starting at " + address);
        int a = address;

        int x = mm.getByte(a);
        while (x != 0) {
            List<Integer> list = new ArrayList<Integer>();
            int id = x & 0x1F;

            addressMap.put(id, a);

            int numberOfBytes = (x >> 5) + 1;

            for (int i = 0; i < numberOfBytes; i++) {
                list.add(mm.getByte(++a));
            }

            map.put(id, list);

            x = mm.getByte(++a);
        }
    }

}
