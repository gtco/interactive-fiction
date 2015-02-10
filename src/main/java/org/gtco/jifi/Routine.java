package org.gtco.jifi;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Routine {

//  private final static int LOCAL_COUNT = 0x10;

    private int m_pc;
    private Stack<Integer> m_stack;
    private int[] m_locals;
    //	private Object m_invocationMethod;
    private int m_argumentCount;
    private int m_localCount;

    private Logger m_log = LoggerFactory.getLogger(Routine.class);

    Routine(int pc, int localCount) {
        m_pc = pc;
        m_localCount = localCount;
        m_locals = new int[localCount + 1];
        m_stack = new Stack();
    }

    private void push(int value) {
        m_stack.push(value);
    }

    private int pop() {
        return (Integer) m_stack.pop();
    }

    private int peek() {
        return (Integer) m_stack.peek();
    }

    public int getPc() {
        return m_pc;
    }

    public void setPc(int pc) {
        m_pc = pc;
    }

    public int getArgumentCount() {
        return m_argumentCount;
    }

    public void setArgumentCount(int argumentCount) {
        m_argumentCount = argumentCount;
    }

    public void setLocal(int index, int value) {
        if (index == 0) {
            push(value);
        } else if (index <= m_localCount) {
            m_locals[index] = value;
        }
    }

    public int getLocal(int index) {
        if (index == 0) {
            // Stack
            return pop();
        } else if (index > 0 && index <= m_localCount) {
            return m_locals[index];
        } else {
            return 0;
        }
    }

    public void printLocals() {
        String locals = "";
        if (!m_stack.empty())
            m_log.debug("stack (" + peek() + ")");

        for (int j = 1; j <= m_localCount; j++) {
            if (j != 1)
                locals += ", ";
            locals += Integer.toHexString(m_locals[j]);
        }
        m_log.debug("(" + locals + ")");
    }
}