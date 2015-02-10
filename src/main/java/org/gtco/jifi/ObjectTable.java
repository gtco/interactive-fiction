package org.gtco.jifi;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectTable {

    // Versions 1-3
    public final static int PROPERTY_DEFAULT = 0x1f;
    public final static int OBJECT_MAX = 0xff;
    public final static int OBJECT_LENGTH = 0x09;
    // Versions 4+
    public final static int PROPERTY_DEFAULT_4 = 0x3f;
    public final static int OBJECT_MAX_4 = 0xffff;
    public final static int OBJECT_LENGTH_4 = 0x0e;

    // byte address stored in the word
    // at 0x0a in the header
    private int m_address;
    private MemoryMap m_map;

    // Property defaults table
    private int m_defaults[];

    // Object Tree
    private Vector<StoryObject> m_objects;

    static Logger m_log = LoggerFactory.getLogger(ObjectTable.class);

    public ObjectTable(MemoryMap mm) {
        m_address = mm.getWord(MemoryMap.OBJECT_TABLE);
        m_map = mm;
        m_defaults = new int[PROPERTY_DEFAULT];
        m_objects = new Vector<StoryObject>(OBJECT_MAX + 1);
        m_objects.setSize(OBJECT_MAX + 1);
    }

    public void setProperty(Argument index, Argument property, Argument value) {
        StoryObject s = (StoryObject) m_objects.get(index.getValue());
        PropertyTable tbl = new PropertyTable(s.getProperties(), m_map);
        tbl.setValue(property.getValue(), value.getValue());
        m_log.debug("\"" + tbl.getName() + "\" " + s);
    }

    public void load() {
        // load property defaults table
        for (int i = 0; i < PROPERTY_DEFAULT; i++)
            m_defaults[i] = m_map.getWord((m_address + (i * 2)));

        int n = m_address + (PROPERTY_DEFAULT * 2);
        boolean done = false;
        int object_index = 0, i = 0, parent = 0, sibling = 0, child = 0, properties = 0;
        long attributes = 0;
        int stopAddr = 0;

        // load object tree
        while (!done && (object_index < OBJECT_MAX)) {
            object_index++;
            //1 & 2
            i = m_map.getWord(n);
            n += 2;
            attributes += (i << 16);
            //3 & 4
            i = m_map.getWord(n);
            n += 2;
            attributes += i;
            //5
            parent = m_map.getByte(n++);
            //6
            sibling = m_map.getByte(n++);
            //7
            child = m_map.getByte(n++);
            //8 & 9
            properties = m_map.getWord(n);
            n += 2;

            StoryObject obj = new StoryObject(attributes, parent, sibling, child, properties);
            PropertyTable ptable = new PropertyTable(properties, m_map);

/*
            m_log.debug("--------------------");
            m_log.debug(ptable.getName());
            m_log.debug("table address=" + ptable.getAddress() + ", text-length="
            	+ ptable.getTextLength());
            m_log.debug(obj);
*/
            m_objects.setElementAt(obj, object_index);

            // Stop if the current address equals the property list value for
            // the first object (i.e. we've gone to far)
            if (n == ((StoryObject) m_objects.elementAt(1)).getProperties())
                done = true;
        }
    }
}