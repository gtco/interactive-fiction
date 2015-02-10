package org.gtco.jifi;

public class Opcode {


    public final static int ONE_OP = 0x80;
    public final static int ZER_OP = 0xb0;
    public final static int TWO_OP = 0xc0;
    public final static int VAR_OP = 0xe0;
    public final static int EXT_OP = 0xbe;

    //	$00 -- $1f  long      2OP     small constant, small constant
    //	$20 -- $3f  long      2OP     small constant, variable
    //	$40 -- $5f  long      2OP     variable, small constant
    //	$60 -- $7f  long      2OP     variable, variable
    //	$80 -- $8f  short     1OP     large constant
    //	$90 -- $9f  short     1OP     small constant
    //	$a0 -- $af  short     1OP     variable
    //	$b0 -- $bf  short     0OP
    //	except $be  extended opcode given in next byte
    //	$c0 -- $df  variable  2OP     (operand types in next byte)
    //	$e0 -- $ff  variable  VAR     (operand types in next byte(s))

    // 2OP
    // %0abxxxxx
    public final static int je = 0x01;
    public final static int jl = 0x02;
    public final static int jg = 0x03;
    public final static int dec_chk = 0x04;
    public final static int inc_chk = 0x05;
    public final static int jin = 0x06;
    public final static int test = 0x07;
    public final static int or = 0x08;
    public final static int and = 0x09;
    public final static int test_attr = 0x0a;
    public final static int set_attr = 0x0b;
    public final static int clear_attr = 0x0c;
    public final static int store = 0x0d;
    public final static int insert_obj = 0x0e;
    public final static int loadw = 0x0f;
    public final static int loadb = 0x10;
    public final static int get_prop = 0x11;
    public final static int get_prop_addr = 0x12;
    public final static int get_next_prop = 0x13;
    public final static int add = 0x14;
    public final static int sub = 0x15;
    public final static int mul = 0x16;
    public final static int div = 0x17;
    public final static int mod = 0x18;

    // 1OP
    // %10ttxxxx
    public final static int jz = 0x00;
    public final static int get_sibling = 0x01;
    public final static int get_child = 0x02;
    public final static int get_parent = 0x03;
    public final static int get_prop_len = 0x04;
    public final static int inc = 0x05;
    public final static int dec = 0x06;
    public final static int print_addr = 0x07;
    //	public final static int call_1s			= 0x08;
    public final static int remove_obj = 0x09;
    public final static int print_obj = 0x0a;
    public final static int ret = 0x0b;
    public final static int jump = 0x0c;
    public final static int print_paddr = 0x0d;
    public final static int load = 0x0e;
    public final static int not = 0x0f;

    // 0OP
    // %10ttxxxx (where tt equal %11)
    public final static int rtrue = 0x00;
    public final static int rfalse = 0x01;
    public final static int print = 0x02;
    public final static int print_ret = 0x03;
    public final static int nop = 0x04;
    public final static int save = 0x05;
    public final static int restore = 0x06;
    public final static int restart = 0x07;
    public final static int ret_popped = 0x08;
    public final static int pop = 0x09;
    public final static int quit = 0x0a;
    public final static int new_line = 0x0b;
    public final static int show_status = 0x0c;
    public final static int verify = 0x0d;
//	public final static int extended		= 0x0e;
//	public final static int piracy			= 0x0f;

    // 2OP/VAR
    // %11axxxx
    // if a is %1 then opcode is VAR
    // if a is %0 then opcode is 2OP
    public final static int call = 0x00;
    public final static int storew = 0x01;
    public final static int storeb = 0x02;
    public final static int put_prop = 0x03;
    public final static int sread = 0x04;
    public final static int print_char = 0x05;
    public final static int print_num = 0x06;
    public final static int random = 0x07;
    public final static int push = 0x08;
    public final static int pull = 0x09;
    public final static int split_window = 0x0a;
    public final static int set_window = 0x0b;
    // 0x0c - 0x012
    public final static int output_stream = 0x13;
    public final static int input_stream = 0x14;
    public final static int sound_effect = 0x15;

}