package org.gtco.jifi;

public class Opcode {
    public final static int ARG_ONE = 0x80;
    public final static int ARG_ZERO = 0xb0;
    public final static int ARG_TWO = 0xc0;
    public final static int ARG_VAR = 0xe0;
    public final static int ARG_EXT = 0xbe;

    // $00 -- $1f long 2OP small constant, small constant
    // $20 -- $3f long 2OP small constant, variable
    // $40 -- $5f long 2OP variable, small constant
    // $60 -- $7f long 2OP variable, variable
    // $80 -- $8f short 1OP large constant
    // $90 -- $9f short 1OP small constant
    // $a0 -- $af short 1OP variable
    // $b0 -- $bf short 0OP
    // except $be extended opcode given in next byte
    // $c0 -- $df variable 2OP (operand types in next byte)
    // $e0 -- $ff variable VAR (operand types in next byte(s))

    // 2OP
    // %0abxxxxx
    public final static int JE = 0x01;
    public final static int JL = 0x02;
    public final static int JG = 0x03;
    public final static int DEC_CHK = 0x04;
    public final static int INC_CHK = 0x05;
    public final static int JIN = 0x06;
    public final static int TEST = 0x07;
    public final static int OR = 0x08;
    public final static int AND = 0x09;
    public final static int TEST_ATTR = 0x0a;
    public final static int SET_ATTR = 0x0b;
    public final static int CLEAR_ATTR = 0x0c;
    public final static int STORE = 0x0d;
    public final static int INSERT_OBJ = 0x0e;
    public final static int LOADW = 0x0f;
    public final static int LOADB = 0x10;
    public final static int GET_PROP = 0x11;
    public final static int GET_PROP_ADDR = 0x12;
    public final static int GET_NEXT_PROP = 0x13;
    public final static int ADD = 0x14;
    public final static int SUB = 0x15;
    public final static int MUL = 0x16;
    public final static int DIV = 0x17;
    public final static int MOD = 0x18;

    // 1OP
    // %10ttxxxx
    public final static int JZ = 0x00;
    public final static int GET_SIBLING = 0x01;
    public final static int GET_CHILD = 0x02;
    public final static int GET_PARENT = 0x03;
    public final static int GET_PROP_LEN = 0x04;
    public final static int INC = 0x05;
    public final static int DEC = 0x06;
    public final static int PRINT_ADDR = 0x07;
    // public final static int call_1s = 0x08;
    public final static int REMOVE_OBJ = 0x09;
    public final static int PRINT_OBJ = 0x0a;
    public final static int RET = 0x0b;
    public final static int JUMP = 0x0c;
    public final static int PRINT_PADDR = 0x0d;
    public final static int LOAD = 0x0e;
    public final static int NOT = 0x0f;

    // 0OP
    // %10ttxxxx (where tt equal %11)
    public final static int RTRUE = 0x00;
    public final static int RFALSE = 0x01;
    public final static int PRINT = 0x02;
    public final static int PRINT_RET = 0x03;
    public final static int NOP = 0x04;
    public final static int SAVE = 0x05;
    public final static int RESTORE = 0x06;
    public final static int RESTART = 0x07;
    public final static int RET_POPPED = 0x08;
    public final static int POP = 0x09;
    public final static int QUIT = 0x0a;
    public final static int NEW_LINE = 0x0b;
    public final static int SHOW_STATUS = 0x0c;
    public final static int VERIFY = 0x0d;
    // public final static int extended = 0x0e;
    // public final static int piracy = 0x0f;

    // 2OP/VAR
    // %11axxxx
    // if a is %1 then opcode is VAR
    // if a is %0 then opcode is 2OP
    public final static int CALL = 0x00;
    public final static int STOREW = 0x01;
    public final static int STOREB = 0x02;
    public final static int PUT_PROP = 0x03;
    public final static int SREAD = 0x04;
    public final static int PRINT_CHAR = 0x05;
    public final static int PRINT_NUM = 0x06;
    public final static int RANDOM = 0x07;
    public final static int PUSH = 0x08;
    public final static int PULL = 0x09;
    public final static int SPLIT_WINDOW = 0x0a;
    public final static int SET_WINDOW = 0x0b;
    // 0x0c - 0x012
    public final static int OUTPUT_STREAM = 0x13;
    public final static int INPUT_STREAM = 0x14;
    public final static int SOUND_EFFECT = 0x15;
}
