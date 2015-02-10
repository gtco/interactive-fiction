package org.gtco.jifi;

public class StoryObject {

    private long m_attributes;
    private int m_parent;
    private int m_sibling;
    private int m_child;
    private int m_properties;

    public StoryObject(long attributes, int parent, int sibling, int child, int properties) {
        m_attributes = attributes;
        m_parent = parent;
        m_sibling = sibling;
        m_child = child;
        m_properties = properties;
    }

    public String toString() {
        return Long.toBinaryString(m_attributes) + ", " + m_properties + ", "
                + m_parent + ", " + m_sibling + ", " + m_child;
    }

    public long getAttributes() {
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
        return false;
    }

}
