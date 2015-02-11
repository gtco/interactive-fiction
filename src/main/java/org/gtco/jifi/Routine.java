package org.gtco.jifi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

public class Routine {

    private static Logger log = LoggerFactory.getLogger(Routine.class);
    // private final static int LOCAL_COUNT = 0x10;
    private int pc;
    private Stack<Integer> stack;
    private int[] locals;
    // private Object m_invocationMethod;
    private int numberOfArgs;
    private int numberOfLocals;

    Routine(int pc, int localCount) {
        this.pc = pc;
        this.numberOfLocals = localCount;
        this.locals = new int[localCount + 1];
        this.stack = new Stack<Integer>();
    }

    public int getLocal(int index) {
        if (index == 0) {
            // Stack
            return pop();
        } else if (index > 0 && index <= numberOfLocals) {
            return locals[index];
        } else {
            log.error("Index out of range");
            return 0;
        }
    }

    public int getNumberOfArgs() {
        return numberOfArgs;
    }

    public int getPc() {
        return pc;
    }

    private int peek() {
        return stack.peek().intValue();
    }

    private int pop() {
        return stack.pop().intValue();
    }

    public void printLocals() {
        String s = "";
        if (!stack.empty()) {
            s += ("Stack (" + peek() + ") ");
        }

        for (int j = 1; j <= numberOfLocals; j++) {
            if (j != 1) {
                s += ", ";
            }
            s += Integer.toHexString(locals[j]);
        }
        log.info("Locals (" + s + ")");
    }

    private void push(int value) {
        stack.push(value);
    }

    public void setLocal(int index, int value) {
        if (index == 0) {
            push(value);
        } else if (index <= numberOfLocals) {
            locals[index] = value;
        }
    }

    public void setNumberOfArgs(int argumentCount) {
        numberOfArgs = argumentCount;
    }

    public void setPc(int pc) {
        this.pc = pc;
    }

    public int next() {
        int n = pc;
        pc++;
        return n;
    }

    public int nextWord() {
        int n = pc;
        pc += 2;
        return n;
    }

}
