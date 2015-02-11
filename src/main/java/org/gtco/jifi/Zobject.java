package org.gtco.jifi;

public class Zobject {

    private int index = 0;
    private String attributes;
    private int parent;
    private int sibling;
    private int child;
    private int properties;

    public Zobject(int index, String attributes, int parent, int sibling, int child, int properties) {
        this.index = index;
        this.attributes = attributes;
        this.parent = parent;
        this.sibling = sibling;
        this.child = child;
        this.properties = properties;
    }


    public String getAttributes() {
        return attributes;
    }

    public int getChild() {
        return child;
    }

    public int getParent() {
        return parent;
    }

    public int getProperties() {
        return properties;
    }

    public int getSibling() {
        return sibling;
    }

    public int getIndex() {
        return index;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public void setSibling(int sibling) {
        this.sibling = sibling;
    }

    public boolean isAttributeSet(int index) {
        boolean b = false;
        if (attributes != null && attributes.length() > index) {
            String s = attributes.substring(index, index + 1);
            b = s.equalsIgnoreCase("1");
        }

        return b;
    }

    public void setAttribute(int index) {
        if (attributes != null && attributes.length() > index) {
            byte[] b = attributes.getBytes();

            b[index] = '1';

            String s = new String(b);

            attributes = s;
        }
    }

    public void clearAttribute(int index) {
        if (attributes != null && attributes.length() > index) {
            byte[] b = attributes.getBytes();

            b[index] = '0';

            String s = new String(b);

            attributes = s;
        }
    }

    @Override
    public String toString() {
        return properties + ", " + parent + ", " + sibling + ", " + child + ", " + attributes;
    }

    public boolean addChild(Zobject obj) {
        if (child == 0) {
            child = obj.getIndex();
            obj.setParent(this.index);
            obj.setSibling(0);
        } else {
            int s = child;
            child = obj.getIndex();
            obj.setParent(this.index);
            obj.setSibling(s);
        }

        return true;
    }

    public boolean isParent(Zobject obj) {
        boolean b = false;

        if (index == obj.getParent()) {
            b = true;
        }

        return b;
    }
}
