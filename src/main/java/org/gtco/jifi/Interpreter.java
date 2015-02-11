package org.gtco.jifi;

/**
 * @author gcopeland
 *         <p/>
 *         To change this generated comment edit the template variable "typecomment":
 *         Window>Preferences>Java>Templates. To enable and disable the creation of type
 *         comments go to Window>Preferences>Java>Code Generation.
 */
public class Interpreter {

    public static void main(String[] args) {
        MemoryMap mm = new MemoryMap();

        if (mm.load(args[0])) {
            Environment e = new Environment(mm);
            e.run();
        }
    }

    /**
     * Constructor for Interpreter.
     */
    public Interpreter() {
        super();
    }
}
