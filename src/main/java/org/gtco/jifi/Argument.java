package org.gtco.jifi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Argument {
    public static final Logger log = LoggerFactory.getLogger(Argument.class);

    public final static int LARGE_CONST = 0x00;
    public final static int SMALL_CONST = 0x01;
    public final static int VARIABLE = 0x02;
    public final static int OMITTED = 0x03;

    /*
     * Variable number $00 refers to the top of the stack $01 to $0f mean the
     * local variables of the current routine and $10 to $ff mean the global
     * variables
     */
    public static String[] typeNames = {"LG", "SM", "VAR", "OMIT"};
    private int type;
    private int value;

    public Argument(int type, int value) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public String getTypeName() {
        return typeNames[type];
    }

    public int getValue() {
        return value;
    }

    public int getValue(Environment e) {
        int n = 0;
        if (type != Argument.VARIABLE) {
            n = value;
            // if((value & 0x8000) == 0x8000)
            // {
            // // negative, convert two's complement
            // nRet = -(--nRet ^ 0xFFFF);
            // }
        } else {
            // int addr = value - 0x10;
            if (value > 0x0f) {
                // global
                n = e.getGlobalVariable(value);
            } else {
                // local
                n = e.getLocalVariable(value);
            }
        }

        if (n > 0xffff) {
            log.error("Overflow = " + n + ", type = " + type);
        }

        return n;
    }

    @Override
    public String toString() {
        return "(" + typeNames[type] + "=" + value + ")";
    }

    public static Argument createArgument(Environment e, Routine routine, int instruction) {
        Argument a = null;
        int t = ((instruction >> 4) & Argument.OMITTED);
        if (t == Argument.SMALL_CONST) {
            a = new Argument(Argument.SMALL_CONST, e.getMemoryMap().getByte(routine.next()));
        } else if (t == Argument.VARIABLE) {
            a = new Argument(Argument.VARIABLE, e.getMemoryMap().getByte(routine.next()));
        } else if (t == Argument.LARGE_CONST) {
            a = new Argument(Argument.LARGE_CONST, e.getMemoryMap().getWord(routine.nextWord()));
        }
        return a;
    }

    public static List<Argument> createArgumentList(Environment e, Routine routine, int types) {
        List<Argument> list = new ArrayList<Argument>();
        int t = 0, value = 0;
        for (int j = 6; j >= 0; j -= 2) {
            t = ((types >>> j) & Argument.OMITTED);
            if (t != Argument.OMITTED) {
                switch (t) {
                    case Argument.LARGE_CONST:
                        value = e.getMemoryMap().getWord(routine.nextWord());
                        break;
                    case Argument.SMALL_CONST:
                    case Argument.VARIABLE:
                        value = e.getMemoryMap().getByte(routine.next());
                        break;
                    default:
                        break;
                }
                Argument arg = new Argument(t, value);
                list.add(arg);
            }
        }
        return list;
    }

    public static List<Argument> createArgumentList(int instruction, int arg1, int arg2) {
        List<Argument> list = new ArrayList<Argument>();

        if ((instruction & 0x40) == 0) {
            list.add(new Argument(Argument.SMALL_CONST, arg1));
        } else {
            list.add(new Argument(Argument.VARIABLE, arg1));
        }

        if ((instruction & 0x20) == 0) {
            list.add(new Argument(Argument.SMALL_CONST, arg2));
        } else {
            list.add(new Argument(Argument.VARIABLE, arg2));
        }

        return list;
    }

}
