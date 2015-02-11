package org.gtco.jifi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Stack;

public class Environment {
    private static Logger log = LoggerFactory.getLogger(Environment.class);
    private MemoryMap map = null;
    private Stack<Routine> stack = null;
    private boolean done = false;
    private int globalOffset = 0;
    private Routine routine;
    private ObjectTable table = null;
    private Window window = null;
    private AbbreviationTable abbr = null;
    private int count = 0;
    private String lm = "";
    private Dictionary dictionary = null;

    public Environment(MemoryMap mm) {
        map = mm;
        // initialize global variable offset
        globalOffset = mm.getWord(MemoryMap.GLOBAL_VAR);
        // create initial routine (main)
        int initialPc = mm.getWord(MemoryMap.INITIAL_PC);
        log.info("Starting program execution at [" + Integer.toString(initialPc) + "]");
        Routine r = new Routine(initialPc, 0);
        // push on m_stack
        stack = new Stack<Routine>();
        stack.push(r);
        table = new ObjectTable(map);
        window = new Window();
        abbr = new AbbreviationTable(mm.getWord(MemoryMap.ABBR_TABLE), map);
        dictionary = new Dictionary(map);
    }

    public void add(Argument a1, Argument a2) {
        int sum = (a1.getValue(this) + a2.getValue(this)) % 0x10000;
        int dest = map.getByte(routine.next());
        routine.setLocal(dest, sum);
        log.debug("ADD:" + dest + "=" + Integer.toString(sum));
        routine.printLocals();
    }

    public void call_fv(List<Argument> args) {
        int paddr = args.get(0).getValue(this);

        if (paddr != 0) {
            int rpc = getPackedAddress(paddr);
            int localCount = map.getByte(rpc++);
            int localValue = 0;

            Routine r = new Routine(rpc, localCount);

            log.info("CALL : From " + Integer.toString(routine.getPc()) + " To " + Integer.toString(rpc - 1)
                    + " Locals [" + localCount + "]");

            for (int i = 1; i <= localCount; i++) {
                localValue = map.getWord(rpc);
                rpc += 2;
                if (i < args.size()) {
                    Argument a = args.get(i);
                    int value = a.getValue(this);

                    if (value > 0xffff) {
                        value &= 0xffff;
                    }

                    log.debug("[" + i + "] " + value);

                    r.setLocal(i, value);

//				if (a.getType() != Argument.VARIABLE)
//				{
//					log.info("[" + i + "]" + " adding argument by value");
//					r.setLocal(i, a.getValue());
//				}
//				else
//				{
//					log.info("[" + i + "]" + " adding argument by reference");
//					r.setLocal(i, routine.getLocal(a.getValue(this)));
//				}
                } else {
                    log.debug("[" + i + "] lv " + localValue);
                    r.setLocal(i, localValue);
                }
            }

            r.printLocals();
            log.info("Pushing new routine at [" + Integer.toString(rpc) + "] onto callstack, current routine PC ["
                    + Integer.toString(routine.getPc()) + "] "
                    + ", Value at PC [" + map.getByte(routine.getPc()) + "]");
            r.setPc(rpc);
            stack.push(r);
        } else {
            int destination = map.getByte(routine.next());
            storeResult(destination, 0);
        }
    }

    public void exec(int instruction) {
        int op = instruction & 0x0f; // %10ttxxxx
        log.warn(lm + " type:0OP opcode=0x" + Integer.toString(op));
        switch (op) {
            case Opcode.RTRUE:
                rtrue();
                break;
            case Opcode.RFALSE:
                rfalse();
                break;
            case Opcode.PRINT:
                print();
                break;
            case Opcode.PRINT_RET:
                print_ret();
                break;
            case Opcode.NOP:
                log.debug("NOP");
                break;
            case Opcode.SAVE:
                log.debug("SAVE");
                haltExecution(op);
                break;
            case Opcode.RESTORE:
                log.debug("RESTORE");
                haltExecution(op);
                break;
            case Opcode.RESTART:
                log.debug("RESTART");
                haltExecution(op);
                break;
            case Opcode.RET_POPPED:
                ret_popped();
                break;
            case Opcode.POP:
                log.debug("POP");
                break;
            case Opcode.QUIT:
                log.debug("QUIT");
                haltExecution(op);
                break;
            case Opcode.NEW_LINE:
                log.debug("NEW_LINE");
                window.println();
                break;
            case Opcode.SHOW_STATUS:
                log.debug("SHOW_STATUS");
                break;
            case Opcode.VERIFY:
                log.debug("VERIFY");
                break;
            default:
                log.error("not implemented, op=" + op);
                haltExecution(op);
                break;
        }
    }

    public void exec(int instruction, Argument argument) {
        // 1OP Opcodes
        int op = instruction & 0x0f; // %10ttxxxx
        log.warn(lm + " type:1OP opcode=0x" + Integer.toString(op) + ", Argument=" + argument);
        switch (op) {
            case Opcode.JZ:
                jz(argument);
                break;
            case Opcode.GET_SIBLING:
                get_sibling(argument);
                break;
            case Opcode.GET_CHILD:
                get_child(argument);
                break;
            case Opcode.GET_PARENT:
                get_parent(argument);
                break;
            case Opcode.GET_PROP_LEN:
                log.debug("get_prop_len");
                haltExecution(op);
                break;
            case Opcode.INC:
                inc(argument);
                break;
            case Opcode.DEC:
                dec(argument);
                break;
            case Opcode.PRINT_ADDR:
                log.debug("print_addr");
                haltExecution(op);
                break;
            // case Opcode.call_1s:
            // break;
            case Opcode.REMOVE_OBJ:
                log.debug("remove_obj");
                haltExecution(op);
                break;
            case Opcode.PRINT_OBJ:
                print_obj(argument);
                break;
            case Opcode.RET:
                ret(argument);
                break;
            case Opcode.JUMP:
                jump(argument);
                break;
            case Opcode.PRINT_PADDR:
                print_paddr(argument);
                break;
            case Opcode.LOAD:
                log.debug("load");
                haltExecution(op);
                break;
            case Opcode.NOT:
                log.debug("not");
                haltExecution(op);
                break;
            default:
                break;
        }
    }

    public void exec(int instruction, List<Argument> args) {
        // 2OP Opcodes
        int op = instruction & 0x1f; // %0abxxxxx
        log.warn(lm + " type:2OP opcode=0x" + Integer.toString(op) + ", ARGS=" + args);
        switch (op) {
            case Opcode.JE:
                je(args);
                break;
            case Opcode.JL:
                jl(args);
                break;
            case Opcode.JG:
                jg(args);
                break;
            case Opcode.DEC_CHK:
                dec_chk(args);
                break;
            case Opcode.INC_CHK:
                inc_chk(args);
                break;
            case Opcode.JIN:
                jin(args);
                break;
            case Opcode.TEST:
                test(args);
                break;
            case Opcode.OR:
                log.debug("or");
                haltExecution(op);
                break;
            case Opcode.AND:
                op_and(args.get(0), args.get(1));
                break;
            case Opcode.TEST_ATTR:
                test_attr(args.get(0), args.get(1));
                break;
            case Opcode.SET_ATTR:
                set_attr(args);
                break;
            case Opcode.CLEAR_ATTR:
                clear_attr(args);
                break;
            case Opcode.STORE:
                int destination = args.get(0).getValue(this);
                int value = args.get(1).getValue(this);
                log.info("STORE : destination [" + destination + "], value [" + value + "]");
                storeResult(destination, value);
                break;
            case Opcode.INSERT_OBJ:
                insert_obj(args);
                break;
            case Opcode.LOADW:
                loadw(args.get(0), args.get(1));
                break;
            case Opcode.LOADB:
                loadb(args.get(0), args.get(1));
                break;
            case Opcode.GET_PROP:
                get_prop(args);
                break;
            case Opcode.GET_PROP_ADDR:
                get_prop_addr(args);
                break;
            case Opcode.GET_NEXT_PROP:
                break;
            case Opcode.ADD:
                log.debug("add");
                add(args.get(0), args.get(1));
                break;
            case Opcode.SUB:
                log.debug("sub");
                sub(args.get(0), args.get(1));
                break;
            case Opcode.MUL:
                log.debug("mul");
                haltExecution(op);
                break;
            case Opcode.DIV:
                log.debug("div");
                haltExecution(op);
                break;
            case Opcode.MOD:
                log.debug("mod");
                mod(args);
                break;
        }
    }

    public void execv(int instruction, List<Argument> args) {
        // VAR Opcodes
        int op = instruction & 0x0f; // %11axxxx
        log.warn(lm + " type:VAR opcode=0x" + Integer.toString(op) + ", Arguments=" + args);
        switch (op) {
            case Opcode.CALL:
                call_fv(args);
                break;
            case Opcode.STOREW:
                storew(args);
                break;
            case Opcode.STOREB:
                storeb(args);
                break;
            case Opcode.PUT_PROP:
                log.debug("put_prop");
                table.setProperty(args.get(0), args.get(1), args.get(2));
                break;
            case Opcode.SREAD:
                sread(args);
                haltExecution(op);
                break;
            case Opcode.PRINT_CHAR:
                log.debug("print_char");
                print_char(args);
                break;
            case Opcode.PRINT_NUM:
                print_num(args);
                break;
            case Opcode.RANDOM:
                log.debug("random");
                haltExecution(op);
                break;
            case Opcode.PUSH:
                push(args);
                break;
            case Opcode.PULL:
                pull(args);
                break;
            case Opcode.SPLIT_WINDOW:
                log.debug("split_window");
                haltExecution(op);
                break;
            case Opcode.SET_WINDOW:
                log.debug("set_window");
                haltExecution(op);
                break;
            case Opcode.OUTPUT_STREAM:
                log.debug("output_stream");
                haltExecution(op);
                break;
            case Opcode.INPUT_STREAM:
                log.debug("input_stream");
                haltExecution(op);
                break;
            case Opcode.SOUND_EFFECT:
                log.debug("sound_effect");
                haltExecution(op);
                break;
            default:
                log.debug("default");
                haltExecution(op);
                break;
        }
    }

    public int getBranchOffset(int control) {
        int offset = -1;
                /*
                 * If bit 6 is set, then the branch occupies 1 byte only, and the
		 * "offset" is in the range 0 to 63, given in the bottom 6 bits. If bit
		 * 6 is clear, then the offset is a signed 14-bit number given in bits 0
		 * to 5 of the first byte followed by all 8 of the second.
		 */
		
		/*
		 *  Some instructions require a jump (or branch) to be made to another part of the Z-program
			depending on the outcome of some test. These instructions are followed by one or two bytes
			called a branch argument. Bit 7 of the ?rst byte indicates when a branch occurs, a %0 meaning
			that the branch logic is �reversed�: branch if the instruction doesn�t want to, don�t if it does. If
			bit 6 is %1, the branch argument consists of a single byte and the branch offset is given by its
			bottom 6 bits (unsigned, i.e. , from 0 to 63). If bit 6 is %0, the branch argument consists of two
			bytes, and the branch offset is given by the bottom 6 bits of the 1st byte followed by all bits of
			the second (signed, i.e. , from -8192 to 8191).
			
sub conditional_jump {
  my $control = GET_BYTE();
  my $offset = $control & 0x3f;
  # basic address is six low bits of the first byte.
  if (($control & 0x40) == 0) {
    # if "bit 6" is not set, address consists of the six (low) bits 
    # of the first byte plus the next 8 bits.
    $offset = ($offset << 8) + GET_BYTE();
    if (($offset & 0x2000) > 0) {
      # if the highest bit (formerly bit 6 of the first byte)
      # is set...
      $offset |= 0xc000;
      # turn on top two bits
      # FIX ME: EXPLAIN THIS
    }
  }
  
  if ($control & 0x80 ? $_[0] : !$_[0]) {
    # normally, branch occurs when condition is false.
    # however, if topmost bit is set, jump occurs when condition is true.
    if ($offset > 1) {
      # jump
      jump($offset);
    } else {
      # instead of jump, this is a RTRUE (1) or RFALSE (0)
      ret($offset);
    }
  }
}			
			
		 */

        offset = control & 0x3f;

        if ((control & 0x040) == 0) {
            // if "bit 6" is not set, address consists of the six (low) bits
            // of the first byte plus the next 8 bits.
            int n = map.getByte(routine.next());
            offset = (offset << 8) + n;
            if ((offset & 0x02000) > 0) {
                offset |= 0xc000;
            }
        }


//		boolean single = ((control & 0x40) == 0x40) ? true : false;
//		if (single)
//		{
//			offset = control & 0x3f;
//			/* m_pc = <address after branch data> + offset - 2; */
//		}
//		else
//		{
//			//TODO fix returns wrong 14 bit number 
//			if ((control & 0x20) != 0) /* negative 14-bit number */
//			{
//				offset = 0xC000 | ((control << 8) | ((routine.next() & 0xFF)));
//			}
//			else
//			{
//				offset = ((control & 0x3F) << 8) | ((routine.next()) & 0xFF);
//			}
//		}
        return offset;
    }

    public int getGlobalVariable(int variableNumber) {
        int addr = globalOffset + (2 * (variableNumber - 16));
        return map.getWord(addr);
    }

    public void dumpGlobals() {
        StringBuffer b = new StringBuffer(1024);
        for (int i = 0x10; i < 0xff; i++) {
            b.append(", G" + (i - 16) + "=" + getGlobalVariable(i));
            b.append((i > 0) && (i % 20 == 0) ? "\n" : "");
        }

        log.info(b.toString());
    }

    public int getLocalVariable(int addr) {
        return routine.getLocal(addr);
    }

    public int getPackedAddress(int word) {
        return (word * 2);
    }

    public void haltExecution(int op) {
        log.info("Halting execution at pc [" + Integer.toString(routine.getPc()) + ", op [" + op + "]");
        //dumpGlobals();
        done = true;
    }

    public void je(List<Argument> args) {
        int b = map.getByte(routine.next());
		/*
		 * If bit 7 of the first byte is 0, a branch occurs on false; if 1, then
		 * branch is on true.
		 */
        boolean condt = ((b & 0x80) == 0x80);
        int offset = getBranchOffset(b);
//		int addr = routine.getPc() + offset - 2;

        int a = args.get(0).getValue(this) & 0xff;
        log.debug("JE : a = " + a);
        int b1 = args.get(1).getValue(this) & 0xff;
        log.debug("JE : b1 = " + a);
        boolean eq = (a == b1);

        if (!eq && args.size() > 2) {
            int b2 = args.get(2).getValue(this);
            log.debug("JE : b2 = " + a);
            eq = (a == b2);
        }

        if (!eq && args.size() > 3) {
            int b3 = args.get(3).getValue(this);
            log.debug("JE : b3 = " + a);
            eq = (a == b3);
        }

        doBranch("JE", eq, condt, offset);
//		if (eq == condt)
//		{
//			routine.setPc(addr);
//			log.debug("JE, jumping: " + Integer.toString(addr) + ", offset " + offset);
//		}
//		else
//		{
//			log.debug("JE, branch failed: eq=" + eq + ", condt=" + condt);
//		}
    }

    public void jump(Argument a1) {

        int addr = 0;
        int n = a1.getValue(this);
        if ((n & 0x8000) == 0x8000) {
            // negative, convert two's complement
            n = -(--n ^ 0xFFFF);
        }
        addr = routine.getPc() + n - 2;
        routine.setPc(addr);
        log.debug("JUMP : arg [" + a1 + "] offset [" + n + "] addr [" + Integer.toString(addr) + "]");
    }

    public void jz(Argument a1) {
        int b = map.getByte(routine.next());
		/*
		 * If bit 7 of the first byte is 0, a branch occurs on false; if 1, then
		 * branch is on true.
		 */
        boolean condt = ((b & 0x80) == 0x80);
        int offset = getBranchOffset(b);
        int v = a1.getValue(this);
        log.debug("JZ, v = " + v);
        boolean eq = (v == 0);
        doBranch("JZ", eq, condt, offset);
    }

    public void loadw(Argument arg1, Argument arg2) {
		/*
		 * LOADW baddr n <result> --- 2OP:$F The result is the word at baddr +2 *n
		 * LOADB baddr n <result> --- 2OP:$10 The result is the byte at baddr +n.
		 * 
		 */

        int array = arg1.getValue(this);
        int index = arg2.getValue(this) & 0xffff;

        int result = map.getWord(array + (index * 2));
        int dest = map.getByte(routine.next());

        storeResult(dest, result);

        log.info("LOADW : " + arg1 + " " + arg2 + " result [" + result + "] dest [" + dest + "]");
    }

    public void loadb(Argument arg1, Argument arg2) {
        int result = map.getByte(arg1.getValue(this) + arg2.getValue(this));
        int dest = map.getByte(routine.next());

        storeResult(dest, result);

        log.info("LOADB : " + arg1 + " " + arg2 + " result [" + result + "] dest [" + dest + "]");
    }

    public void printGlobalVariables() {
        log.info("" + new Integer(globalOffset));
        for (int i = 16; i < 255; i++) {
            int value = getGlobalVariable(i);
            log.debug(i + " " + value);
        }
    }

    public void ret(Argument a1) {
        log.info("" + a1);
        // wip -> check the call stack to determine invocation method
        int value = a1.getValue(this);
        // remove top frame from stack
        stack.pop();

        routine = stack.peek();

        int dest = map.getByte(routine.next());

        log.info("RET : dest " + dest + " value " + Integer.toString(value));

        storeResult(dest, value);

        routine.printLocals();

    }

//	  $00 -- $1f  long      2OP     small constant, small constant
//	  $20 -- $3f  long      2OP     small constant, variable
//	  $40 -- $5f  long      2OP     variable, small constant
//	  $60 -- $7f  long      2OP     variable, variable
//	  $80 -- $8f  short     1OP     large constant
//	  $90 -- $9f  short     1OP     small constant
//	  $a0 -- $af  short     1OP     variable
//	  $b0 -- $bf  short     0OP
//	  except $be  extended opcode given in next byte
//	  $c0 -- $df  variable  2OP     (operand types in next byte)
//	  $e0 -- $ff  variable  VAR     (operand types in next byte(s))

    public void run() {


        table.load(abbr);

        //TODO Implement NEW_LINE, LOADB, AND, JZ, LOADW, AND, PRINT_NUM, INC_CHK, JUMP, LOADB, PRINT_CHAR, JUMP, RTRUE

        //dumpGlobals();

        while (!done && !stack.empty()) {
            lm = "";
            routine = stack.peek();
            int i = map.getByte(routine.next());
            count = count + 1;
            lm += "count:" + count + " pc:" + (Integer.toString(routine.getPc() - 1));

            if (i < 0x80 || (i >= 0xc0 && i <= 0xdf)) {        // 2op
                List<Argument> list = null;

                // short form
                if (i < 0x80) {
                    int arg1 = map.getByte(routine.next());
                    int arg2 = map.getByte(routine.next());
                    list = Argument.createArgumentList(i, arg1, arg2);
                }
                // $c0 -- $df variable 2OP (operand types in next byte)
                else if (i >= 0xc0 && i <= 0xdf) {
                    int operandTypes = map.getByte(routine.next());
                    list = Argument.createArgumentList(this, routine, operandTypes);
                }

                exec(i, list);
            } else if (i < 0xb0) {        // 1op
                exec(i, Argument.createArgument(this, routine, i));
            } else if (i < 0xc0) {
                if (i != 0xbe) {        // 0op
                    exec(i);
                } else {        // Ext
                    log.error("Extended Opcode, Not yet implemented");
                    done = true;
                }
            } else if (i >= 0xe0 && i <= 0xff) {
                // $e0 -- $ff variable VAR (operand types in next byte(s))
                int operandTypes = map.getByte(routine.next());
                List<Argument> list = Argument.createArgumentList(this, routine, operandTypes);
                execv(i, list);
            } else {
                log.error("Unknown Opcode");
                done = true;
            }
        }
    }

    public void setGlobalVariable(int destination, int value) {
        int addr = globalOffset + (2 * (destination - 16));
        map.setWord(addr, value);
    }

    public void storeResult(int destination, int value) {
        if (destination < 0x10) {
            routine.setLocal(destination, value);
        } else if (destination < 0xff) {
            setGlobalVariable(destination, value);
        } else {
            log.error("storeResult failed, invalid destination = " + destination);
        }
    }

    public void storew(List<Argument> args) {
		/* STOREW baddr n a --- VAR:$1 Store a in the word at baddr +2 *n. */
        int addr = args.get(0).getValue(this);
        int mult = args.get(1).getValue(this);
        int value = args.get(2).getValue(this);

        addr = addr + 2 * mult;
        map.setWord(addr, value);

        log.info("STOREW : storing " + Integer.toString(value) + " at addr " + Integer.toString(addr)
                + " plus 2 times " + mult);
    }

    public void storeb(List<Argument> args) {
		/* 
		 * store var a� 2OP:$D
		 * Set the variable with number var to a.
		 */
        int var = args.get(0).getValue(this);
        int a = args.get(1).getValue(this);

        log.info("STOREB : storing " + Integer.toString(a) + " in variable " + var);

        storeResult(var, a);
    }

    public void sub(Argument a1, Argument a2) {
        int difference = (a1.getValue(this) - a2.getValue(this)) % 0x10000;
        int dest = map.getByte(routine.next());
        // m_routine.setLocal(dest, difference);
        storeResult(dest, difference);
        log.info("SUB:" + dest + "=" + Integer.toString(difference));
        routine.printLocals();
    }

    public void test_attr(Argument a1, Argument a2) {
        int object = a1.getValue(this) & 0xff;
        int value = a2.getValue(this);
        boolean eq = table.testAttribute(object, value);

        int b = map.getByte(routine.next());
		/*
		 * If bit 7 of the first byte is 0, a branch occurs on false; if 1, then
		 * branch is on true.
		 */
        boolean condt = ((b & 0x80) == 0x80);
        int offset = getBranchOffset(b);
        int addr = routine.getPc() + offset - 2;

        log.info("TEST_ATTR : Testing Object = " + table.getObjectName(object)
                + ", Attribute = " + value
                + ", Result = " + eq
                + ", Condt = " + condt
                + ", Destination = " + Integer.toString(addr));

        doBranch("TEST_ATTR", eq, condt, offset);
//		if (eq == condt)
//		{
//			routine.setPc(addr);
//			log.debug("TEST_ATTR, jumping: " + Integer.toString(addr) + ", offset " + offset);
//		}
//		else
//		{
//			log.debug("TEST_ATTR, branch failed: eq=" + eq + ", condt=" + condt);
//		}
    }

    public void set_attr(List<Argument> args) {
        table.setAttribute(args.get(0).getValue(this), args.get(1).getValue(this));
    }

    public void clear_attr(List<Argument> args) {
        table.clearAttribute(args.get(0).getValue(this), args.get(1).getValue(this));
    }

    public void jin(List<Argument> args) {
		/*
		jin obj n <branch>� 2OP:$6
		Branch if n is the parent object of obj,or if n is 0 and the object has no parent. Equivalent
			to
				get_parent obj ST
				je ST n <branch>		

		*/

        int b = map.getByte(routine.next());
		/*
		 * If bit 7 of the first byte is 0, a branch occurs on false; if 1, then
		 * branch is on true.
		 */
        boolean condt = ((b & 0x80) == 0x80);
        int offset = getBranchOffset(b);
//		int addr = routine.getPc() + offset - 2;

        int child = args.get(0).getValue(this);
        int parent = args.get(1).getValue(this);
        boolean eq = table.isParent(parent, child);
        log.debug("JIN: child = " + child + ", parent = " + parent + ", isParent = " + eq);
        doBranch("JIN", eq, condt, offset);
//		if (eq == condt)
//		{
//			routine.setPc(addr);
//			log.info("JIN, jumping: " + Integer.toString(addr) + ", offset " + offset);
//		}
//		else
//		{
//			log.info("JIN, branch failed: eq=" + eq + ", condt=" + condt);
//		}
    }


    public void print() {
        int pc = routine.getPc();
        Zchar zs = new Zstring(map, pc, 1, abbr);
        String s = zs.toString();
        window.print(s);
        log.debug("print_text=" + s);
        pc = zs.getEnd();
        routine.setPc(pc);
    }

    public void print_num(List<Argument> args) {
        int i = args.get(0).getValue(this);
        String s = String.valueOf(i);
        log.debug("print_num=" + s);
        window.print(s);
    }

    public void print_char(List<Argument> args) {
        int i = args.get(0).getValue(this);
        Character c = (char) i;
        log.debug("print_char=" + c.toString());
        window.print(c.toString());
    }

    public void op_and(Argument arg1, Argument arg2) {
        int a = arg1.getValue(this);
        int b = arg2.getValue(this);
        int result = a & b;
        int dest = map.getByte(routine.next());

        storeResult(dest, result);

        log.info("AND : " + Integer.toBinaryString(a) + " " + Integer.toBinaryString(b)
                + " result [" + Integer.toBinaryString(result) + "] dest [" + dest + "]");
    }

    public void inc(Argument argument) {
/**
 * inc var � 1OP:$5
 Increment the value of the variable with number var by 1, modulo $10000. Equivalent to
 load var ST
 add ST 1 ST
 store var ST
 */
        int local = argument.getValue(this);
        int n = routine.getLocal(local);
        n = n + 1;
        storeResult(local, n);
    }

    public void dec(Argument argument) {
        int local = argument.getValue(this);
        int n = routine.getLocal(local);
        n = n - 1;
        storeResult(local, n);
    }

    public void inc_chk(List<Argument> args) {
        int b = map.getByte(routine.next());
		/*
		 * If bit 7 of the first byte is 0, a branch occurs on false; if 1, then
		 * branch is on true.
		 */
        boolean condt = ((b & 0x80) == 0x80) ? true : false;
        int offset = getBranchOffset(b);
        int local = args.get(0).getValue(this);
        int n = routine.getLocal(local);
        n = n + 1;
        n &= 0xffff;
        storeResult(local, n);

        int j = args.get(1).getValue(this);
        boolean eq = n > j;
        doBranch("INC_CHK", eq, condt, offset);
    }

    public void dec_chk(List<Argument> args) {
        int b = map.getByte(routine.next());
		/*
		 * If bit 7 of the first byte is 0, a branch occurs on false; if 1, then
		 * branch is on true.
		 */
        boolean condt = ((b & 0x80) == 0x80);
        int offset = getBranchOffset(b);
//		int addr = routine.getPc() + offset - 2;

        int local = args.get(0).getValue(this);
        int n = routine.getLocal(local);
        n = n + 1;
        storeResult(local, n);

        int j = args.get(1).getValue(this);
        boolean eq = n > j;

        doBranch("DEC_CHK", eq, condt, offset);
    }

    public MemoryMap getMemoryMap() {
        return map;
    }

    public void rtrue() {
        stack.pop();
        routine = stack.peek();
        int dest = map.getByte(routine.next());
        log.info("RTRUE, dest " + dest + " value " + Integer.toString(1));
        storeResult(dest, 1);
        routine.printLocals();
    }

    public void rfalse() {
        stack.pop();
        routine = stack.peek();
        int dest = map.getByte(routine.next());
        log.info("RFALSE, dest " + dest + " value " + Integer.toString(0));
        storeResult(dest, 0);
        routine.printLocals();
    }

    public void insert_obj(List<Argument> args) {
        table.insertObject(args.get(0).getValue(this), args.get(1).getValue(this));
    }

    public void push(List<Argument> args) {
        int n = args.get(0).getValue(this);
        storeResult(0, n);
    }

    public void pull(List<Argument> args) {
        int n = routine.getLocal(0);
        int local = args.get(0).getValue(this);
        storeResult(local, n);
    }

    public void get_parent(Argument argument) {
        int i = argument.getValue(this);
        Zobject zo = table.getObject(i);
        int parent = zo.getParent();
        int dest = map.getByte(routine.next());
        log.info("GET_PARENT [" + table.getObjectName(i) + "] -> [" + table.getObjectName(parent) + "]");
        storeResult(dest, parent);
    }

    public void get_sibling(Argument argument) {
        int i = argument.getValue(this);
        Zobject zo = table.getObject(i);
        int sibling = zo.getSibling();
        int dest = map.getByte(routine.next());
        storeResult(dest, sibling);
        int label = map.getByte(routine.next());
        boolean condt = ((label & 0x80) == 0x80);
        boolean eq = sibling != 0;
        int offset = getBranchOffset(label);

        log.info("GET_SIBLING, condt = " + condt + ", eq = " + eq + ", offset = " + offset);

        doBranch("GET_SIBLING", eq, condt, offset);
    }


    public void get_child(Argument argument) {
		/*
		get_child obj <result><branch>� 1OP:$2
		The result is the (?rst) child object of the given object, or 0 if it doesn�t exist. Branch if the
		result is not 0.
		*/

        int i = argument.getValue(this);
        Zobject zo = table.getObject(i);
        int child = zo.getChild();
        int dest = map.getByte(routine.next());
        storeResult(dest, child);
        int label = map.getByte(routine.next());
        boolean condt = ((label & 0x80) == 0x80);
        boolean eq = child != 0;
        int offset = getBranchOffset(label);

        log.info("GET_CHILD, condt = " + condt + ", eq = " + eq + ", offset = " + offset);

        doBranch("GET_CHILD", eq, condt, offset);
    }

    public void print_obj(Argument argument) {
        int i = argument.getValue(this);
        String n = table.getObjectName(i);
        log.debug("print_obj=" + n);
        window.print(n);

    }

    public void get_prop(List<Argument> args) {
		/*
		 * get_prop obj prop <result>� 2OP:$11
			The result is the 1st word (if the property length is 2) or byte (if it is one) of property prop
			on object obj, if it is present. Otherwise the result is the default property word stored in the
			property defaults table. The result is unspecified if the property is present but does not have
			length 1 or 2.	
		 */
        int obj = args.get(0).getValue(this);
        int prop = args.get(1).getValue(this);
        log.info("GET_PROP : Getting property [" + prop + "] for Object [" + obj + ":" + table.getObjectName(obj) + "], Property");
        int op = table.getObjectProperty(obj, prop);
        int dest = map.getByte(routine.next());
        storeResult(dest, op);
    }

    public void get_prop_addr(List<Argument> args) {
        //TODO test and verify address is correct
		
		/*
		 * get_prop_addr obj prop <result>� 2OP:$12
		 * The result is the address where the property prop of object obj begins. The property must
		 * be present on the object.
		 */
        int obj = args.get(0).getValue(this);
        int prop = args.get(1).getValue(this);
        log.info("GET_PROP_ADDR : Getting address of property [" + prop + "] for Object [" + obj + ":" + table.getObjectName(obj) + "], Property");
        int op = table.getObjectPropertyAddress(obj, prop);
        int dest = map.getByte(routine.next());
        storeResult(dest, op);
    }

    public void doBranch(String op, boolean eq, boolean condt, int offset) {
        if (eq == condt) {
            if (offset == 0) {
                log.info(op + ": doBranch, RFALSE");
                rfalse();
            } else if (offset == 1) {
                log.info(op + ": doBranch, RTRUE");
                rtrue();
            } else {
                int addr = routine.getPc() + offset - 2;
                log.info(op + " : jumping: " + Integer.toString(addr) + ", offset " + offset);
                routine.setPc(addr);
            }
        } else {
            log.info(op + " : branch failed: eq=" + eq + ", condt=" + condt);
        }
    }

    public void print_ret() {
        print();
        window.println();
        rtrue();
    }

    public void print_paddr(Argument arg) {
        int paddr = arg.getValue(this);
        int rpc = getPackedAddress(paddr);
        Zchar zs = new Zstring(map, rpc, 1, abbr);
        String s = zs.toString();
        log.debug("print_paddr=" + s);
        window.print(s);
    }

    public void jl(List<Argument> args) {
        int b = map.getByte(routine.next());
        boolean condt = ((b & 0x80) == 0x80);
        int offset = getBranchOffset(b);

        int s = args.get(0).getValue(this);
        int t = args.get(1).getValue(this);
        log.debug("JL : s=" + s + ", t=" + t);
        boolean eq = (s < t);

        doBranch("JL", eq, condt, offset);

    }

    public void jg(List<Argument> args) {
        int b = map.getByte(routine.next());
        boolean condt = ((b & 0x80) == 0x80);
        int offset = getBranchOffset(b);

        int s = args.get(0).getValue(this);
        int t = args.get(1).getValue(this);
        log.debug("JG : s=" + s + ", t=" + t);
        boolean eq = (s > t);

        doBranch("JG", eq, condt, offset);

    }

    public void ret_popped() {
        int value = routine.getLocal(0);

        stack.pop();
        routine = stack.peek();
        int dest = map.getByte(routine.next());

        log.info("RET_POPPED : dest =" + dest + ", value = " + Integer.toString(value));

        storeResult(dest, value);

        routine.printLocals();

    }

    public void sread(List<Argument> args) {
        int text = args.get(0).getValue(this);
//		int parse = args.get(1).getValue(this);		
//		int sz = map.getByte(text);
        int n = text + 1;

        //
        // Read input
        //
        BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
        String inputLine;
        try {
            inputLine = is.readLine();
            is.close();

            log.info("Read : input = " + inputLine + ", destination = " + text);

            for (byte b : inputLine.getBytes()) {
                map.setByte(n++, b);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Tokenize input
        tokenize(args);
    }

    public void tokenize(List<Argument> args) {
        int text = args.get(0).getValue(this);
//		int parse = args.get(1).getValue(this);
        int n = text + 1;
        StringBuilder buffer = new StringBuilder(map.getByte(text));
        int i = 0;

        while ((i = map.getByte(n++)) != 0) {
            buffer.append((char) i);
        }

        int tt = map.getWord(MemoryMap.TERMINATING_TABLE);

        if (tt > 0) {
            log.error("tokenize : failed to process non-zero terminating table!");
        }

        log.debug(buffer.toString());

    }

    public void mod(List<Argument> args) {
		/*
		 * mod s t <result>- 2OP:$18
		 * The result is s - t * floor (s/t) modulo $10000 It is an error if t is 0. Note that s and t are
		 * interpeted as signed numbers.
		 */

        int s = args.get(0).getValue(this);
        int t = args.get(1).getValue(this);
        int result = (s - t) * ((int) Math.floor(s / t));
        result %= 0x10000;
        int dest = map.getByte(routine.next());
        storeResult(dest, result);
    }

    public void test(List<Argument> args) {
        int b = map.getByte(routine.next());
        boolean condt = ((b & 0x80) == 0x80);
        int offset = getBranchOffset(b);

        int s = args.get(0).getValue(this);
        int t = args.get(1).getValue(this);
        log.debug("TEST : s=" + s + ", t=" + t);
        boolean eq = (s & t) == t;

        doBranch("TEST", eq, condt, offset);
    }

    public Logger getLog() {
        return log;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

}
