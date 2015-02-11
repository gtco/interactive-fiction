package org.gtco.jifi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectTable {

    // Versions 1-3
    public final static int PROPERTY_DEFAULT = 0x1f; // 31
    public final static int OBJECT_MAX = 0xff; // 255
    public final static int OBJECT_LENGTH = 0x09;

    private static Logger log = LoggerFactory.getLogger(ObjectTable.class);
    // byte address stored in the word
    // at 0x0a in the header
    private int address;

    private MemoryMap mm;

    // Property defaults table
    private List<Integer> defaults;
    // Object Tree
    private List<Zobject> objects;
    private Map<Integer, PropertyTable> objectProperties = new HashMap<Integer, PropertyTable>();

    public ObjectTable(MemoryMap mm) {
        this.mm = mm;
        address = this.mm.getWord(MemoryMap.OBJECT_TABLE);
        defaults = new ArrayList<Integer>();
        objects = new ArrayList<Zobject>();

        defaults.add(0, Integer.MAX_VALUE);

        for (int i = 0; i < OBJECT_MAX + 1; i++) {
            objects.add(new Zobject((i + 1), "", 0, 0, 0, 0));
        }
    }

    public void load(AbbreviationTable abbr) {
        // load property defaults table
        for (int i = 0; i < PROPERTY_DEFAULT; i++) {
            defaults.add(i + 1, mm.getWord((address + (i * 2))));
        }

        int n = address + (PROPERTY_DEFAULT * 2);
        boolean done = false;

        int object_index = 0;
        int parent = 0;
        int sibling = 0;
        int child = 0;
        int properties = 0;

        // load object tree
        while (!done && (object_index < OBJECT_MAX)) {
            object_index++;

            String a = loadAttributes(n);
            n += 4;

            // 5
            parent = mm.getByte(n++);
            // 6
            sibling = mm.getByte(n++);
            // 7
            child = mm.getByte(n++);
            // 8 & 9
            properties = mm.getWord(n);
            n += 2;

            Zobject zo = new Zobject(object_index, a, parent, sibling, child, properties);
            PropertyTable propertyTable = new PropertyTable(properties, mm, abbr);
            log.debug("" + zo);
            log.debug("Object [" + object_index + "], Name [" + propertyTable.getName() + "],"
                    + " PropertyTable (Address= " + propertyTable.getAddress() + ")");

            objects.set(object_index, zo);
            objectProperties.put(object_index, propertyTable);

            // Stop if the current address equals the property list value for
            // the first object (i.e. we've gone to far)
            if (n == (objects.get(1)).getProperties()) {
                log.info("Found " + object_index + " objects");
                done = true;
            }

            log.debug("---");

        }
    }

    public void setProperty(Argument index, Argument key, Argument value) {
        PropertyTable table = objectProperties.get(index.getValue());
        table.setValue(key.getValue(), value.getValue());
    }

    public boolean testAttribute(int object, int attribute) {
        Zobject zo = objects.get(object);
        return zo.isAttributeSet(attribute);
    }

    public Zobject getObject(int index) {
        return objects.get(index);
    }

    public String getObjectName(int index) {
        return objectProperties.get(index).getName();
    }

    public void insertObject(int target, int destination) {
                /*
                 *  insert_obj obj1 obj2� 2OP:$E
		 *  
			Remove obj1 from its current location in the object tree, and insert it as the 1st child of obj 2,
			before all other children.
			
			All obj1�s children move with it.
			
			Object obj1 is 1st removed from its current location, as with remove_obj obj1.
			
			It is then made the (1st) child of obj2, with the formerly 1st child as its (next) sibling.
			
			Note that a consistent non-recursive object tree is made recursive by this instruction
			if and only if obj1 occurs in the (finite) parent chain that begins with obj2. In this case the
			emulator might print a warning message, since this is probably a bug.		
		 */

        Zobject z1 = objects.get(target);
        Zobject z2 = objects.get(destination);

        PropertyTable z1p = objectProperties.get(target);
        PropertyTable z2p = objectProperties.get(destination);

        z2.addChild(z1);

        log.info("INSERT_OBJ, Success : [" + z1p.getName() + "] -> [" + z2p.getName() + "]");

    }

    public void setAttribute(int object, int attribute) {
        Zobject zo = objects.get(object);
        log.info("Set Attribute, " + objectProperties.get(object).getName() + "");
        zo.setAttribute(attribute);
    }

    public void clearAttribute(int object, int attribute) {
        Zobject zo = objects.get(object);
        log.info("Clear Attribute, " + objectProperties.get(object).getName() + "");
        zo.clearAttribute(attribute);
    }

    public boolean isParent(int p, int c) {
        Zobject child = objects.get(c);

        if (p == 0 && child.getParent() == 0) {
            return true;
        }

        Zobject parent = objects.get(p);
        log.info("IsParent, Parent [" + p + ":" + objectProperties.get(p).getName()
                + "], Child [" + c + ":" + objectProperties.get(c).getName() + "]");
        return parent.isParent(child);
    }

    public int getObjectProperty(int obj, int prop) {
        int n = 0;
        PropertyTable pt = objectProperties.get(obj);

        pt.map.containsKey(prop);
        List<Integer> list = pt.map.get(prop);
        if (list != null && list.size() > 1) { //TODO check length of list
            n = (short) ((list.get(0) << 8) & 0xff00) | (list.get(1) & 0xff);
        } else if (list != null && list.size() > 0) {
            n = list.get(0);
        } else {
            n = defaults.get(prop);
        }

        return n;
    }

    public int getObjectPropertyAddress(int obj, int prop) {
        int n = 0;

        PropertyTable pt = objectProperties.get(obj);

        // ???
        if (pt.addressMap.containsKey(prop)) {
            n = pt.addressMap.get(prop);
            log.error("Property Table contains address = " + n + " for property = " + prop);
        } else {
            log.error("Property Table missing address for property = " + prop);
        }

        return n;
    }

    private String loadAttributes(int offset) {
        StringBuilder sb = new StringBuilder();
        for (int n = 0; n < 4; n++) {
            sb.append(toRightSize(Integer.toBinaryString(mm.getByte(offset + n)), 8));
        }
        return sb.toString();
    }

    private int padLength(final int length, final int rightSize) {
        int leftLen = length % rightSize;
        return (leftLen == 0) ? 0 : (rightSize - leftLen);
    }

    private String toRightSize(String s, int sz) {
        StringBuilder sb = new StringBuilder();
        int x = padLength(s.length(), sz);
        for (int i = 0; i < x; i++) {
            sb.append("0");
        }
        sb.append(s);
        return sb.toString();
    }

}
