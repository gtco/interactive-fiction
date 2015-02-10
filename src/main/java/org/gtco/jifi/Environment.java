package org.gtco.jifi;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Environment {

    private int m_pc = 0;
    private MemoryMap m_map = null;
    private Stack<Routine> m_stack = null;
    private boolean m_done = false;
    private int m_globalOffset = 0;

    private Routine m_routine;
    private ObjectTable m_table = null;

    private Logger m_log = LoggerFactory.getLogger(Environment.class);

    public Environment(MemoryMap mm) {
        m_map = mm;
        // initialize global variable offset
        m_globalOffset = mm.getWord(MemoryMap.GLOBAL_VAR);
        // create initial routine (main)
        Routine r = new Routine(mm.getWord(MemoryMap.INITIAL_PC), 0);
        // push on m_stack
        m_stack = new Stack<Routine>();
        m_stack.push(r);
        m_table = new ObjectTable(m_map);
    }

    public void haltExecution() {
        m_log.warn("Halting execution, pc=" + Integer.toHexString(m_pc));
        m_done = true;
    }

    public int getPackedAddress(int word) {
        return (word * 2);
    }

    public int getLocalVariable(int addr) {
        return m_routine.getLocal(addr);
    }

    public void printGlobalVariables() {
        m_log.debug(""+m_globalOffset);
        for (int i = 16; i < 255; i++) {
            int value = getGlobalVariable(i);
            m_log.debug(i + " " + value);
        }
    }

    public int getGlobalVariable(int variableNumber) {
        int addr = m_globalOffset + (2 * (variableNumber - 16));
        return m_map.getWord(addr);
    }

    public void setGlobalVariable(int destination, int value) {
        int addr = m_globalOffset + (2 * (destination - 16));
        m_map.setWord(addr, value);
    }

    public Vector createArgumentList(int types) {
        Vector<Argument> v = new Vector<Argument>();
        int t = 0, value = 0;
        for (int j = 6; j >= 0; j -= 2) {
            t = ((types >>> j) & Argument.OMITTED);
            if (t != Argument.OMITTED) {
                switch (t) {
                    case Argument.LARGE_CONST:
                        value = m_map.getWord(m_pc);
                        m_pc += 2;
                        break;
                    case Argument.SMALL_CONST:
                    case Argument.VARIABLE:
                        value = m_map.getByte(m_pc++);
                        break;
                    default:
                        break;
                }
                Argument arg = new Argument(t, value);
                v.addElement(arg);
            }
        }
        return v;
    }

    public Argument createArgument(int instruction) {
        Argument a = null;
        int t = ((instruction >> 4) & Argument.OMITTED);
        if (t == Argument.SMALL_CONST)
            a = new Argument(Argument.SMALL_CONST, m_map.getByte(m_pc++));
        else if (t == Argument.VARIABLE)
            a = new Argument(Argument.VARIABLE, m_map.getByte(m_pc++));
        else if (t == Argument.LARGE_CONST) {
            a = new Argument(Argument.LARGE_CONST, m_map.getWord(m_pc));
            m_pc += 2;
        }
        return a;
    }

    public Vector createArgumentList(int instruction, int arg1, int arg2) {
        Vector<Argument> v = new Vector<Argument>();

        if ((instruction & 0x40) == 0)
            v.addElement(new Argument(Argument.SMALL_CONST, arg1));
        else
            v.addElement(new Argument(Argument.VARIABLE, arg1));

        if ((instruction & 0x20) == 0)
            v.addElement(new Argument(Argument.SMALL_CONST, arg2));
        else
            v.addElement(new Argument(Argument.VARIABLE, arg2));

        return v;
    }

    public void storeResult(int destination, int value) {
        if (destination < 0x10) {
            m_routine.setLocal(destination, value);
        } else if (destination < 0xff) {
            setGlobalVariable(destination, value);
        } else {
            // error
        }
    }

    public void exec(int instruction, Argument a1) {
        // 1OP Opcodes
        int op = instruction & 0x0f;        // %10ttxxxx
        m_log.debug("instruction=" + instruction + ", argument=" + a1);
        switch (op) {
            case Opcode.jz:
                jz(a1);
                break;
            case Opcode.get_sibling:
                haltExecution();
                break;
            case Opcode.get_child:
                haltExecution();
                break;
            case Opcode.get_parent:
                haltExecution();
                break;
            case Opcode.get_prop_len:
                haltExecution();
                break;
            case Opcode.inc:
                haltExecution();
                break;
            case Opcode.dec:
                haltExecution();
                break;
            case Opcode.print_addr:
                haltExecution();
                break;
//			case Opcode.call_1s:
//				break;
            case Opcode.remove_obj:
                haltExecution();
                break;
            case Opcode.print_obj:
                haltExecution();
                break;
            case Opcode.ret:
                ret(a1);
                break;
            case Opcode.jump:
                jump(a1);
                break;
            case Opcode.print_paddr:
                haltExecution();
                break;
            case Opcode.load:
                haltExecution();
                break;
            case Opcode.not:
                haltExecution();
                break;
            default:
                break;
        }
    }

    public void exec(int instruction, Vector args) {
        // VAR Opcodes
        int op = instruction & 0x0f;        // %11axxxx
        m_log.debug("instruction=" + instruction + ", arguments=" + args);
        switch (op) {
            case Opcode.call:
                call_fv(args);
                break;
            case Opcode.storew:
                storew(args);
                break;
            case Opcode.storeb:
                m_log.debug("storeb");
                haltExecution();
                break;
            case Opcode.put_prop:
                m_log.debug("put_prop");
                m_table.setProperty((Argument) args.get(0), (Argument) args.get(1),
                        (Argument) args.get(2));
                break;
            case Opcode.sread:
                m_log.debug("sread");
                haltExecution();
                break;
            case Opcode.print_char:
                m_log.debug("print_char");
                haltExecution();
                break;
            case Opcode.print_num:
                m_log.debug("print_num");
                haltExecution();
                break;
            case Opcode.random:
                m_log.debug("random");
                haltExecution();
                break;
            case Opcode.push:
                m_log.debug("push");
                haltExecution();
                break;
            case Opcode.pull:
                m_log.debug("pull");
                haltExecution();
                break;
            case Opcode.split_window:
                m_log.debug("split_window");
                haltExecution();
                break;
            case Opcode.set_window:
                m_log.debug("set_window");
                haltExecution();
                break;
            case Opcode.output_stream:
                m_log.debug("output_stream");
                haltExecution();
                break;
            case Opcode.input_stream:
                m_log.debug("input_stream");
                haltExecution();
                break;
            case Opcode.sound_effect:
                m_log.debug("sound_effect");
                haltExecution();
                break;
        }
    }

    public void exec(int instruction, int arg1, int arg2) {
        // 2OP Opcodes
        int op = instruction & 0x1f;        // %0abxxxxx
        Vector args = createArgumentList(instruction, arg1, arg2);
        m_log.debug("instruction=" + instruction + ", arguments=" + args);
        switch (op) {
            case Opcode.je:
                je((Argument) args.elementAt(0), (Argument) args.elementAt(1));
                break;
            case Opcode.jl:
                haltExecution();
                break;
            case Opcode.jg:
                haltExecution();
                break;
            case Opcode.dec_chk:
                haltExecution();
                break;
            case Opcode.inc_chk:
                haltExecution();
                break;
            case Opcode.jin:
                haltExecution();
                break;
            case Opcode.test:
                haltExecution();
                break;
            case Opcode.or:
                haltExecution();
                break;
            case Opcode.and:
                haltExecution();
                break;
            case Opcode.test_attr:
                haltExecution();
                break;
            case Opcode.set_attr:
                haltExecution();
                break;
            case Opcode.clear_attr:
                haltExecution();
                break;
            case Opcode.store:
                storeResult(arg1, arg2);
                break;
            case Opcode.insert_obj:
                haltExecution();
                break;
            case Opcode.loadw:
                loadw((Argument) args.elementAt(0), (Argument) args.elementAt(1));
                break;
            case Opcode.loadb:
                haltExecution();
                break;
            case Opcode.get_prop:
                haltExecution();
                break;
            case Opcode.get_prop_addr:
                haltExecution();
                break;
            case Opcode.get_next_prop:
                break;
            case Opcode.add:
                add((Argument) args.elementAt(0), (Argument) args.elementAt(1));
                break;
            case Opcode.sub:
                sub((Argument) args.elementAt(0), (Argument) args.elementAt(1));
                break;
            case Opcode.mul:
                haltExecution();
                break;
            case Opcode.div:
                haltExecution();
                break;
            case Opcode.mod:
                haltExecution();
                break;
        }
    }

    public int getBranchOffset(int b) {
        int offset = -1;
        /*  If bit 6 is set, then the branch occupies 1 byte only,
            and the "offset" is in the range 0 to 63, given in the bottom 6 bits.
            If bit 6 is clear, then the offset is a signed 14-bit number given
            in bits 0 to 5 of the first byte followed by all 8 of the second.  */
        boolean single = ((b & 0x40) == 0x40) ? true : false;
        if (single) {
            offset = b & 0x3f;
            /* m_pc = <address after branch data> + offset - 2; */
        } else {
            // wip
        }
        return offset;
    }

    public void jz(Argument a1) {
        int b = m_map.getByte(m_pc++);
        /*  If bit 7 of the first byte is 0, a branch occurs on false;
            if 1, then branch is on true. */
        boolean condt = ((b & 0x80) == 0x80) ? true : false;
        int offset = getBranchOffset(b);
        int addr = m_pc + offset - 2;
        boolean eq = (a1.getValue(this) == 0);
        if (eq == condt) {
            m_pc = addr;
            m_log.debug("jumping: " + Integer.toHexString(addr) + ", offset " + offset);
        } else {
            m_log.debug("branch failed: eq=" + eq + ", condt=" + condt);
        }
        m_routine.setPc(m_pc);
    }

    public void jump(Argument a1) {

        int addr = 0;
        int n = a1.getValue(this);
        if ((n & 0x8000) == 0x8000) {
            // negative, convert two's complement
            n = -(--n ^ 0xFFFF);
        }
        addr = m_pc + n - 2;
        m_routine.setPc(addr);
        m_log.debug("arg [" + a1 + "] offset [" + n + "] addr [" +
                Integer.toHexString(addr) + "]");
    }

    public void loadw(Argument arg1, Argument arg2) {
        /*  LOADW baddr n <result> --- 2OP:$F The result is the word at baddr +2 *n.
            LOADB baddr n <result> --- 2OP:$10 The result is the byte at baddr +n. */
        int result = arg1.getValue(this) + 2 * arg2.getValue(this);
        int dest = m_map.getByte(m_pc++);

        storeResult(dest, result);

        m_log.debug(arg1 + " " + arg2 + " result [" + result + "] dest [" + dest + "]");
        m_routine.setPc(m_pc);
    }

    public void storew(Vector args) {
        /*  STOREW baddr n a    --- VAR:$1 Store a in the word at baddr +2 *n. */
        int addr = ((Argument) args.elementAt(0)).getValue(this);
        int mult = ((Argument) args.elementAt(1)).getValue(this);
        int value = ((Argument) args.elementAt(2)).getValue(this);

        addr = addr + 2 * mult;
        m_map.setWord(addr, value);

        m_log.debug("storing " + Integer.toHexString(value) +
                " at addr " + Integer.toHexString(addr) + " plus 2 times " + mult);

        m_routine.setPc(m_pc);
    }

    public void je(Argument a1, Argument a2) {
        int b = m_map.getByte(m_pc++);
        /*  If bit 7 of the first byte is 0, a branch occurs on false;
            if 1, then branch is on true. */
        boolean condt = ((b & 0x80) == 0x80) ? true : false;
        int offset = getBranchOffset(b);
        int addr = m_pc + offset - 2;
        boolean eq = (a1.getValue(this) == a2.getValue(this));
        if (eq == condt) {
            m_pc = addr;
            m_log.debug("jumping: " + Integer.toHexString(addr) + ", offset " + offset);
        } else {
            m_log.debug("branch failed: eq=" + eq + ", condt=" + condt);
        }
        m_routine.setPc(m_pc);
    }

    public void sub(Argument a1, Argument a2) {
        int difference = (a1.getValue(this) - a2.getValue(this)) % 0x10000;
        int pc = m_routine.getPc();
        int dest = m_map.getByte(pc++);
        m_routine.setPc(pc);
        //m_routine.setLocal(dest, difference);
        storeResult(dest, difference);
        m_log.debug("VAR:" + dest + "=" + Integer.toHexString(difference));
        m_routine.printLocals();
    }

    public void add(Argument a1, Argument a2) {
        int sum = (a1.getValue(this) + a2.getValue(this)) % 0x10000;
        int pc = m_routine.getPc();
        int dest = m_map.getByte(pc++);
        m_routine.setPc(pc);
        m_routine.setLocal(dest, sum);
        m_log.debug("VAR:" + dest + "=" + Integer.toHexString(sum));
        m_routine.printLocals();
    }

    public void ret(Argument a1) {
        m_log.debug(""+a1);
        // wip -> check the call stack to determine invocation method
        int value = a1.getValue(this);
        // remove top frame from stack
        m_stack.pop();

        m_routine = (Routine) m_stack.peek();
        m_pc = m_routine.getPc();
        int dest = m_map.getByte(m_pc++);

        m_log.debug("dest " + dest + " value " + Integer.toHexString(value));

        storeResult(dest, value);

        m_routine.printLocals();
        m_routine.setPc(m_pc);
    }

    // wip: ensure m_pc integrity from routine to routine
    public void call_fv(Vector args) {

        int paddr = ((Argument) args.elementAt(0)).getValue();
        int rpc = getPackedAddress(paddr);
        int localCount = m_map.getByte(rpc++);
        int localValue = 0;

        Routine r = new Routine(rpc, localCount);

        m_log.debug("from " + Integer.toHexString(m_routine.getPc()) + " to " + Integer.toHexString(rpc - 1));
//		 + ", locals " + localCount + ", arguments " + args);
        for (int i = 1; i <= localCount; i++) {
            localValue = m_map.getWord(rpc);
            rpc += 2;
            if (i < args.size()) {
                Argument a = (Argument) args.elementAt(i);
                if (a.getType() != Argument.VARIABLE) {
//					m_log.debug("[" + i + "]" + " adding argument by value");
                    r.setLocal(i, a.getValue());
                } else {
//					m_log.debug("[" + i + "]" + " adding argument by reference");
                    r.setLocal(i, m_routine.getLocal(a.getValue()));
                }
            } else {
//				m_log.debug("[" + i + "]" + " adding local");
                r.setLocal(i, localValue);
            }
        }
//		r.printLocals();
//		m_log.debug("Pushing new routine at [" + Integer.toHexString(rpc) + "] onto callstack");
//		m_log.debug("Current Routine m_pc [" + Integer.toHexString(m_routine.getPc()) +
//			"] Value at m_pc [" + m_map.getByte(m_routine.getPc()) + "]");
        r.setPc(rpc);
//		m_log.debug("Pushing new routine at [" + Integer.toHexString(rpc) + "] onto callstack");
        m_stack.push(r);
    }

    public void run() {

        m_log.debug("Loading object table");

        m_table.load();

        m_log.debug("Starting eval loop");

        while (!m_done && !m_stack.empty()) {
            // current routine
            m_routine = (Routine) m_stack.peek();
            // load
            m_pc = m_routine.getPc();


            m_log.debug("--------------------");
            m_log.debug("pc=" + Integer.toHexString(m_pc));

            int i = m_map.getByte(m_pc++);
            // interpret
            if (i < 0x80) // 2op
            {
                int operand1 = m_map.getByte(m_pc++);
                int operand2 = m_map.getByte(m_pc++);
                m_routine.setPc(m_pc);
                exec(i, operand1, operand2);
                //				m_done = true;
            } else if (i < 0xb0) //1op
            {
                //				int operand1 = m_map.getByte(m_pc++);
                Argument a1 = createArgument(i);
                m_routine.setPc(m_pc);
                exec(i, a1);
                //				m_done = true;
            } else if (i < 0xc0) { // 0op, ext
                if (i != 0xbe) {
                    //				execute(opcode);
                } else {
                    // Extended Opcode, Not yet implemented
                }
                m_done = true;
            } else {
                //	$c0 -- $df  variable  2OP     (operand types in next byte)
                //	$e0 -- $ff  variable  VAR     (operand types in next byte(s))
                int operandTypes = m_map.getByte(m_pc++);
                Vector v = createArgumentList(operandTypes);
                // always save m_pc before execute
                m_routine.setPc(m_pc);
                exec(i, v);
            }

        }

        m_log.debug("Ending eval loop, done = " + m_done + ", stack size = " + (m_stack != null ? m_stack.size() : "null stack"));
    }

}
