package org.gtco.jifi;

public class StoryObject {

    private String m_attributes;
    private int m_parent;
    private int m_sibling;
    private int m_child;
    private int m_properties;

    public StoryObject(long attributes, int parent, int sibling, int child, int properties) {
        m_attributes = Long.toBinaryString(attributes);
        m_parent = parent;
        m_sibling = sibling;
        m_child = child;
        m_properties = properties;
    }

    public String toString() {
        return m_attributes + ", " + m_properties + ", "
                + m_parent + ", " + m_sibling + ", " + m_child;
    }

    public String getAttributes() {
        return m_attributes;
    }

    public int getParent() {
        return m_parent;
    }

    public int getSibling() {
        return m_sibling;
    }

    public int getChild() {
        return m_child;
    }

    public int getProperties() {
        return m_properties;
    }

    public boolean isAttributeSet(int attribute) {


        boolean b = false;

        if (m_attributes.charAt(attribute) == '1') {
            b = true;
        }

        return b;

    }


}
