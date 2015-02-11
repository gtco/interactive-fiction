package org.gtco.jifi;

public class Window {
    public void print(String... strings) {
        for (String s : strings) {
            System.out.print(s);
        }
    }

    public void println() {
        System.out.println();
    }
}
