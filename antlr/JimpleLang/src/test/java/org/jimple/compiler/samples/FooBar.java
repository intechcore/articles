package org.jimple.compiler.samples;


import org.apache.commons.lang3.Validate;

public class FooBar {

    public static void main(final String[] args) {
        long counter = 0;
        long lastFactorial = 0;
        long startTime = System.currentTimeMillis();

        while (counter < 1_000_000) {
            lastFactorial = factorial(counter % 20 + lastFactorial % 5);
            counter = counter + 1;
        }

        long time = System.currentTimeMillis() - startTime;
        System.out.println("lastFactorial: " + lastFactorial);
        System.out.println("Time: " + time);

        Validate.isTrue(121645100408832000L == lastFactorial);
    }

    static long factorial(long n) {
        if (n == 0) {
            return 1;
        }
        return n * factorial(n - 1);
    }
}
