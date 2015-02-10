package org.gtco;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        Atr a = new Atr();

        boolean b = a.load("toolkit.atr");

        a.printHeader();

        System.out.println("b=" + b);


    }


}
