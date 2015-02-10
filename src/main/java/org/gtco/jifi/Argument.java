package org.gtco.jifi;

public class Argument {

    public final static int LARGE_CONST = 0x00;
    public final static int SMALL_CONST = 0x01;
    public final static int VARIABLE = 0x02;
    public final static int OMITTED = 0x03;

    /* Variable number $00 refers to the top of the stack
       $01 to $0f mean the local variables of the current routine
       and $10 to $ff mean the global variables */

    public static String[] m_typeNames = {"LG", "SM", "VAR", "OMIT"};

    private int m_type;
    private int m_value;

    public Argument(int type, int value) {
        m_type = type;
        m_value = value;
    }

    public int getType() {
        return m_type;
    }

    public String getTypeName() {
        return m_typeNames[m_type];
    }

    public int getValue(Environment e) {
        int value = 0;
        if (m_type != Argument.VARIABLE) {
            value = m_value;
            //			if((value & 0x8000) == 0x8000)
            //			{
            //				// negative, convert two's complement
            //				nRet = -(--nRet ^ 0xFFFF);
            //			}
        } else {
            //int addr = value - 0x10;
            if (m_value > 0x0f) {
                // global
                value = e.getGlobalVariable(m_value);
            } else {
                // local
                value = e.getLocalVariable(m_value);
            }
        }
        return value;
    }

    public int getValue() {
        return m_value;
    }

    public String toString() {
        return "(" + m_typeNames[m_type] + "=" + m_value + ")";
    }
}