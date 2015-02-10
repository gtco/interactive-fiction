package org.gtco.jifi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gcopeland
 *         <p/>
 *         To change this generated comment edit the template variable "typecomment":
 *         Window>Preferences>Java>Templates.
 *         To enable and disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public class Interpreter {

    private static Logger log = LoggerFactory.getLogger(Interpreter.class);

    /**
     * Constructor for Interpreter.
     */
    public Interpreter() {
        super();
    }

    public static void main(String[] args) {
/*
                int i = 0, j = 0;
		int x2 = 0;

		int mult = 16;

		char[] mem = { (char)0xffff, (char)0xffff, (char)0x0f, (char)0x00 };

		while((mem[i / mult] & (1 << (i % mult))) > 0)
		{
			i++;
		}

		int ndx = (i - (i % mult)) / mult;
		int before = mem[ndx];
//		int after = mem[ndx] = (char) ((mem[ndx] << 1) + 1);

		System.out.println("index=" + ndx
			+ ", before="	+ Integer.toBinaryString(before)
			+ ", after="	+ Integer.toBinaryString(after));

*/
        MemoryMap mm = new MemoryMap();

        String file = args[0];

        if (mm.load(file)) {
            log.debug("Loaded memory map successfully for file = " + file );
            Environment e = new Environment(mm);
            e.run();
        } else {
            log.error("Failed to load memory map for file = " + file);
        }
    }
}
