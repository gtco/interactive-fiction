package org.gtco.jifi;

public class PropertyTable {

    private int m_address;
    private MemoryMap m_map;

    private int m_length;
//	private String m_name;
//	private Vector m_properties;

    public PropertyTable(int address, MemoryMap mm) {
        m_address = address;
        m_map = mm;
        m_length = mm.getByte(address);
    }

    public int getAddress() {
        return m_address;
    }

    public int getTextLength() {
        return m_length;
    }

    public void setValue(int property, int value) {

    }

    public String getName() {
        String strRet = null;
        if (m_length > 0) {
            Text d = new Text(m_map);
/*            
            int textAddr = address + 1;
            Vector name = new Vector();
            name.addAll(d.decode(textAddr));
            System.out.println("(" + length + ") Name [" + d.convert(name) + "]");
  */
            strRet = d.convert(d.decode(m_address + 1));
        }
        return strRet;
    }
}