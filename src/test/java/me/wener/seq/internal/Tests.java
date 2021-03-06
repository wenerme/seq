package me.wener.seq.internal;

import com.google.common.base.Throwables;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class Tests {
    public static void expected(LongSupplier src, long... numbers) {
        long[] actuals = new long[numbers.length];
        for (int i = 0; i < actuals.length; i++) {
            actuals[i] = src.getAsLong();
        }
        try {
            assertArrayEquals(numbers, actuals);
        } catch (Throwable e) {
            System.out.printf("Failed %s <> %s\n", Arrays.toString(actuals), Arrays.toString(numbers));
            Throwables.propagate(e);
        }
    }
}
